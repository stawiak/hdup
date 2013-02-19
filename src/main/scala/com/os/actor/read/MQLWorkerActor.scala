package com.os.actor.read

import akka.actor.{ActorLogging, Actor}
import com.os.mql.parser.MQLParser
import concurrent.Future
import com.os.measurement.TimedValue
import akka.pattern.ask
import akka.pattern.pipe
import akka.util.Timeout
import concurrent.duration._
import com.os.mql.executor.MQLCommand

/**
 * @author Vadim Bobrov
 */
class MQLWorkerActor(val parser: MQLParser) extends Actor with ReadMasterAware with ActorLogging {
	import context._
	implicit val timeout: Timeout = 60 seconds

	override def receive: Receive = {
		case mql: String =>
			log.debug("mql received\n{}", mql)

			val res: Either[Throwable, Traversable[MQLCommand]] = try {
				Right(parser.parse(mql))
			} catch {
				case t: Throwable => Left(t)
			}

			res match {
				case Right(commands) =>
					Future.traverse(commands)(command => (readMaster ? command.readRequest).mapTo[Iterable[TimedValue]]  map(command.include(_)) map (command.enrich(_)) ).map(_.flatten) pipeTo sender
				case Left(t) =>
					sender ! t
			}

	}

}
