package com.os.actor

import akka.actor.Actor
import akka.pattern.ask
import read.{ReadMasterAware, RollupReadRequest, MeasurementReadRequest}
import spray.routing._
import spray.http.MediaTypes._
import org.joda.time.Interval
import com.os.measurement.MeasuredValue
import concurrent.Await
import com.os.rest.exchange.TimeSeriesData
import java.sql.Timestamp
import akka.util.Timeout
import concurrent.duration.Duration

/**
 * @author Vadim Bobrov
 */


// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class WebServiceActor extends Actor with WebService {

	// the HttpService trait defines only one abstract member, which
	// connects the services environment to the enclosing actor or test
	def actorRefFactory = context

	// this actor only runs our route, but you could add
	// other things here, like request stream processing
	// or timeout handling
	def receive = runRoute(route)
}


// this trait defines our service behavior independently from the service actor
trait WebService extends HttpService with ReadMasterAware {
	this: Actor =>

	implicit val timeout: Timeout = Duration(100, "sec") // for the actor 'asks' we use below

	val route = {
		get {
			path("stop") {
				complete {
					context.system.shutdown()
					"shutting down"
				}
			}
		} ~
		get {
			pathPrefix(PathElement) { customer: String =>
				pathPrefix(PathElement) { location: String =>

					pathPrefix(PathElement) { wireid: String =>
						parameters("from".as[Long] ? 0L, "to".as[Long] ? Long.MaxValue) { (fromTime: Long, toTime: Long) =>
							respondWithMediaType(`application/json`) { // XML is marshalled to `text/xml` by default, so we simply override here
								complete {
									readRequest(customer, location, fromTime, toTime, Option(wireid))
								}
							}
						}
					} ~
					parameters("from".as[Long] ? 0L, "to".as[Long] ? Long.MaxValue) { (fromTime: Long, toTime: Long) =>
						respondWithMediaType(`application/json`) { // XML is marshalled to `text/xml` by default, so we simply override here
							complete {
								readRequest(customer, location, fromTime, toTime)
							}
						}
					}



				}
			}

		}
	}


	private def readRequest(customer: String, location: String, fromTime: Long, toTime: Long, wireid: Option[String] = None): String = {
		val readRequest =
			if (wireid.isDefined)
				new MeasurementReadRequest(customer, location, wireid.get, Array[Interval](new Interval(fromTime, toTime)))
			else
				new RollupReadRequest(customer, location, Array[Interval](new Interval(fromTime, toTime)))


		val tsd = new TimeSeriesData()


		Await.result((readMaster ? readRequest).mapTo[Iterable[MeasuredValue]], timeout.duration) foreach (mv => tsd.put(new Timestamp(mv.timestamp), mv.energy))


		tsd.toJSONString
	}

}