package com.os.actor


import scala.concurrent.duration._
import akka.pattern.ask
import akka.util.Timeout
import akka.actor._
import spray.io.{IOBridge, IOExtension}
import spray.can.server.HttpServer
import spray.util._
import spray.http._
import HttpMethods._
import MediaTypes._


/**
 * @author Vadim Bobrov
 */
class HttpServiceActor extends Actor with SprayActorLogging {
	implicit val timeout: Timeout = 1 second

	def receive = {
		case HttpRequest(GET, "/", _, _, _) =>
			sender ! index

		case HttpRequest(GET, "/server-stats", _, _, _) =>
			val client = sender
			(context.actorFor("../http-server") ? HttpServer.GetStats).onSuccess {
				case x: HttpServer.Stats => client ! statsPresentation(x)
			}

		case h @ HttpRequest(GET, "/io-stats", _, _, _) =>
			val client = sender
			(IOExtension(context.system).ioBridge() ? IOBridge.GetStats).onSuccess {
				case IOBridge.StatsMap(map) => client ! statsPresentation(map)
			}

		case request @ HttpRequest(GET, uri, _, _, _) if uri.startsWith("/vamps") =>
			val params = request.queryParams
			val restParts = request.uri.split("/")
			params foreach (log.debug("params {}", _))
			restParts foreach (log.debug("restParts {}", _))

		case HttpRequest(GET, "/stop", _, _, _) =>
			sender ! HttpResponse(entity = "Shutting down in 1 second ...")
			context.system.scheduler.scheduleOnce(1 second span, new Runnable { def run() { context.system.shutdown() } })

		case _: HttpRequest => sender ! HttpResponse(status = 404, entity = "Unknown resource!")

		// this Timeout will be renamed to Timedout soon
		case spray.http.Timeout(HttpRequest(_, "/timeout/timeout", _, _, _)) =>
			log.info("Dropping Timeout message")

		case spray.http.Timeout(HttpRequest(method, uri, _, _, _)) =>
			sender ! HttpResponse(
				status = 500,
				entity = "The " + method + " request to '" + uri + "' has timed out..."
			)

	}

	////////////// helpers //////////////

	lazy val index = HttpResponse(
		entity = HttpBody(`text/html`,
			<html>
				<body>
					<h1>Welcome to <i>CHAOS</i>!</h1>
					<ul>
						<li><a href="/stop">/Stop chaos</a></li>
						<li><a href="/stats">/Chaos statistics</a></li>
						<li><a href="/server-stats">/Server statistics</a></li>
						<li><a href="/io-stats">/IO statistics</a></li>
						<li><a href="/query">/Type in MQL query</a></li>
						<li><a href="/run">/Run MQL query in query param</a></li>
					</ul>
				</body>
			</html>.toString
		)
	)

	def statsPresentation(s: HttpServer.Stats) = HttpResponse(
		entity = HttpBody(`text/html`,
			<html>
				<body>
					<h1>HttpServer Stats</h1>
					<table>
						<tr><td>uptime:</td><td>{s.uptime.formatHMS}</td></tr>
						<tr><td>totalRequests:</td><td>{s.totalRequests}</td></tr>
						<tr><td>openRequests:</td><td>{s.openRequests}</td></tr>
						<tr><td>maxOpenRequests:</td><td>{s.maxOpenRequests}</td></tr>
						<tr><td>totalConnections:</td><td>{s.totalConnections}</td></tr>
						<tr><td>openConnections:</td><td>{s.openConnections}</td></tr>
						<tr><td>maxOpenConnections:</td><td>{s.maxOpenConnections}</td></tr>
						<tr><td>requestTimeouts:</td><td>{s.requestTimeouts}</td></tr>
						<tr><td>idleTimeouts:</td><td>{s.idleTimeouts}</td></tr>
					</table>
				</body>
			</html>.toString
		)
	)

	def statsPresentation(map: Map[ActorRef, IOBridge.Stats]) = HttpResponse(
		entity = HttpBody(`text/html`,
			<html>
				<body>
					<h1>IOBridge Stats</h1>
					<table>
						{
						def extractData(t: (ActorRef, IOBridge.Stats)) = t._1.path.elements.last :: t._2.productIterator.toList
						val data = map.toSeq.map(extractData).sortBy(_.head.toString).transpose
						val headers = Seq("IOBridge", "uptime", "bytesRead", "bytesWritten", "connectionsOpened", "commandsExecuted")
						headers.zip(data).map { case (header, items) =>
							<tr><td>{header}:</td>{items.map(x => <td>{x}</td>)}</tr>
						}
						}
					</table>
				</body>
			</html>.toString
		)
	)

}