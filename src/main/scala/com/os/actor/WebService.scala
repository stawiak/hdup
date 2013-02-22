package com.os.actor

import akka.actor.{ActorLogging, PoisonPill, Actor}
import akka.pattern.ask
import read.{MQLHandlerAware, ReadMasterAware, RollupReadRequest, MeasurementReadRequest}
import spray.routing._
import spray.http.MediaTypes._
import org.joda.time.Interval
import com.os.measurement.TimedValue
import concurrent.Await
import java.sql.Timestamp
import akka.util.Timeout
import concurrent.duration._
import scala.Predef._
import util.{SettingsUse, GracefulStop}
import com.os.Settings
import spray.http.MediaType
import com.os.exchange.TimeSeriesData
import java.net.URLDecoder.decode
import scala.util.{Failure, Try, Success}

/**
 * @author Vadim Bobrov
 */

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class WebServiceActor extends Actor with ActorLogging with SettingsUse with WebService {

	// the HttpService trait defines only one abstract member, which
	// connects the services environment to the enclosing actor or test
	def actorRefFactory = context

	// this actor only runs our route, but you could add
	// other things here, like request stream processing
	// or timeout handling
	def receive = runRoute(route) orElse {
		case GracefulStop =>
			self ! PoisonPill
	}
}


// this trait defines our service behavior independently from the service actor
trait WebService extends HttpService with ReadMasterAware with TopAware with MQLHandlerAware {
	this: Actor with ActorLogging with SettingsUse =>

	implicit val timeout: Timeout = 60 seconds

	val route = {
		get {
			path("stop") {
				complete {
					top ! GracefulStop
					"shutting down"
				}
			} ~
			path("query") {
				respondWithMediaType(`text/html`) {
					complete {
							<html>
								<body>
									<form name="mql" action="run" method="get">
										<textarea name="mql" rows="20" cols="80">enter your query here...</textarea>
										<br/>
										<input type="submit" value="Submit"/>
									</form>
								</body>
							</html>
					}
				}
			} ~
			path("run") {
				parameters("mql".as[String]) { mql: String =>
					val jsonAccepted = extract(_.request.isMediaTypeAccepted(`application/json`))
					val csvAccepted = extract(_.request.isMediaTypeAccepted(`text/csv`))

					val res = mqlRequest(mql, `text/csv`)
					res match {
						case Success(s) =>
							csvAccepted {  csv: Boolean => respondWithMediaType(`text/csv`) { complete {s} } }
						case Failure(e) =>
							{ complete {e.getMessage()} }
					}

					//TODO: implement json
					/*
					jsonAccepted {  json =>
						respondWithMediaType(`application/json`) {
							complete {
								mqlRequest(mql, `application/json`)
							}
						}
					} */
				}
			} ~
			pathPrefix("energy") { restRequest(Settings.TableName) } ~
			pathPrefix("current") { restRequest(Settings.CurrentTableName) } ~
			pathPrefix("vamps") { restRequest(Settings.VampsTableName) } ~
			pathPrefix("interpolated") { restRequest(Settings.MinuteInterpolatedTableName) } ~
			pathPrefix("energy") {
				pathPrefix(PathElement) { customer: String =>
					pathPrefix(PathElement) { location: String =>
						parameters("from".as[Long] ? 0L, "to".as[Long] ? Long.MaxValue) { (fromTime: Long, toTime: Long) =>
							respondWithMediaType(`application/json`) {
								complete {
									readRequest(Settings.RollupTableName, decode(customer, "UTF-8"), decode(location, "UTF-8"), fromTime, toTime)
								}
							}
						}
					}
				}
			}
		}
	}

	private def restRequest(tableName: String) = {
		pathPrefix(PathElement) { customer: String =>
			pathPrefix(PathElement) { location: String =>
				pathPrefix(PathElement) { wireid: String =>
					parameters("from".as[Long] ? 0L, "to".as[Long] ? Long.MaxValue) { (fromTime: Long, toTime: Long) =>
						respondWithMediaType(`application/json`) { // XML is marshalled to `text/xml` by default, so we simply override here
							complete {
								readRequest(tableName, decode(customer, "UTF-8"), decode(location, "UTF-8"), fromTime, toTime, Option(decode(wireid, "UTF-8")))
							}
						}
					}
				}
			}
		}
	}

	private def mqlRequest(mql: String, mediaType: MediaType): Try[String] = {
		Success
		val res = Await.result((mqlHandler ? mql), timeout.duration)
		res match {
			case values: Iterable[Map[String, Any]] =>
				val sb = new StringBuilder
				//TODO: implement JSON
				if (mediaType == `text/csv`)
					values foreach (mv => sb.append(mv.values.mkString("", ",", "\n")))
				if (sb.length == 0)
					sb.append("empty")
				Success(sb.result())
			case t: Throwable =>
				log.error(t, t.getMessage)
				Failure(t)
		}
	}

	private def readRequest(tableName: String, customer: String, location: String, fromTime: Long, toTime: Long, wireid: Option[String] = None): String = {
		val readRequest =
			if (wireid.isDefined)
				new MeasurementReadRequest(tableName, customer, location, wireid.get, new Interval(fromTime, toTime))
			else
				new RollupReadRequest(customer, location, new Interval(fromTime, toTime))


		val tsd = new TimeSeriesData()


		try {
			Await.result((readMaster ? readRequest).mapTo[Iterable[TimedValue]], timeout.duration) foreach (mv => tsd.put(new Timestamp(mv.timestamp), mv.value))
		} catch {
			case e: Exception =>
				log.error(e, e.getMessage)
		}


		tsd.toJSONString
	}

}