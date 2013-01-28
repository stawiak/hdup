package com.os.actor

import akka.actor.Actor
import spray.routing._
import spray.http.MediaTypes._

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

	val route = {
		// extract URI path element as Int
		pathPrefix(PathElement) { customer =>
			pathPrefix(PathElement) { location =>
				pathPrefix(PathElement) { wire =>
					path("") {
						get {
							respondWithMediaType(`application/json`) { // XML is marshalled to `text/xml` by default, so we simply override here
								complete {
									"" + customer + "_" + location + "_" + wire
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