package com.os.actor

import akka.actor.{ActorLogging, PoisonPill, Actor}
import akka.pattern.ask
import read.{MQLHandlerAware, ReadMasterAware, RollupReadRequest, MeasurementReadRequest}
import service.TimeWindowAware
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

/**
 * @author Vadim Bobrov
 */

case object Monitor
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
trait WebService extends HttpService with ReadMasterAware with TimeWindowAware with TopAware with MQLHandlerAware {
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
			path("stats") {
				respondWithMediaType(`text/html`) {
					complete {
						val timeWindowStats = try {
								Await.result((timeWindow ? Monitor).mapTo[Map[String, Int]], timeout.duration)
							} catch {
								case e: Exception =>
									Map("length" -> "n/a")
							}

							<html>
								<body>
									<h1>Stats</h1>
									<p>
										window length: <b>{timeWindowStats("length")}</b>
									</p>
								</body>
							</html>
					}
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
					csvAccepted {  csv =>
						respondWithMediaType(`text/csv`) {
							complete {
								mqlRequest(mql, `text/csv`)
							}
						}
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
									readRequest(Settings.RollupTableName, customer, location, fromTime, toTime)
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
								readRequest(tableName, customer, location, fromTime, toTime, Option(wireid))
							}
						}
					}
				}
			}
		}
	}

	private def mqlRequest(mql: String, mediaType: MediaType): String = {
		val sb = new StringBuilder

		try {
			if (mediaType == `text/csv`)
				Await.result((mqlHandler ? mql).mapTo[Iterable[Map[String, Any]]], timeout.duration) foreach (mv => sb.append(mv.values.mkString("", ",", "\n")))
			if (mediaType == `application/json`)
				//TODO: implement JSON
				Await.result((mqlHandler ? mql).mapTo[Iterable[Map[String, Any]]], timeout.duration) foreach (mv => sb.append(mv.values.mkString("", ",", "\n")))

		} catch {
			case e: Exception =>
				log.error(e, e.getMessage)
		}

		if (sb.length == 0)
			sb.append("empty")
		sb.result()
	}

	private def readRequest(tableName: String, customer: String, location: String, fromTime: Long, toTime: Long, wireid: Option[String] = None): String = {
		val readRequest =
			if (wireid.isDefined)
				new MeasurementReadRequest(tableName, customer, location, wireid.get, Array[Interval](new Interval(fromTime, toTime)))
			else
				new RollupReadRequest(customer, location, Array[Interval](new Interval(fromTime, toTime)))


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