package com.os.actor

import akka.actor.{Props, ActorSystem, Actor}
import akka.pattern.ask
import read.{ReadMasterActor, MeasurementReadRequest}
import spray.routing._
import spray.http.MediaTypes._
import org.joda.time.Interval
import com.typesafe.config.ConfigFactory
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
trait WebService extends HttpService {

	implicit val timeout: Timeout = Duration(100, "sec") // for the actor 'asks' we use below

	val route = {
		// extract URI path element as Int
		pathPrefix(PathElement) { customer: String =>
			pathPrefix(PathElement) { location: String =>
				pathPrefix(PathElement) { wire: String =>
					get {
						parameters("from".as[Long] ? 0L, "to".as[Long] ? Long.MaxValue) { (fromTime: Long, toTime: Long) =>
							respondWithMediaType(`application/json`) { // XML is marshalled to `text/xml` by default, so we simply override here
								complete {
									val readRequest = new MeasurementReadRequest(customer, location, wire, Array[Interval](new Interval(fromTime, toTime)))


									val system = ActorSystem("prod", ConfigFactory.load().getConfig("prod"))
									val readMaster = system.actorOf(Props[ReadMasterActor], "readMaster")

									val res: Iterable[MeasuredValue] = Await.result((readMaster ? readRequest).mapTo[Iterable[MeasuredValue]], timeout.duration)


									val tsd = new TimeSeriesData()
									for(mv <- res)
										tsd.put(new Timestamp(mv.timestamp), mv.energy)

									tsd.toJSONString
									// marshal custom object with in-scope marshaller
									/*<h1>customer</h1>*/
								}
							}
						}
					}
				}
			}
		}
	}

}