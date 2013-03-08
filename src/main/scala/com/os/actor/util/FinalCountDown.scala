package com.os.actor.util

import akka.actor._
import akka.actor.Terminated
import com.os.actor.GracefulStop
import concurrent.{Future, Await, Promise}
import concurrent.duration._
import util.Try
import akka.pattern.{GracefulStopSupport}

/**
 * @author Vadim Bobrov
*/

/**
 * A marker trait to indicate that this is the last actor and
 * the entire actor system needs to be shut down when done
 */
trait LastMohican


/**
 * This trait adds functionality to wait for the children actors to finish their
 * work and then stop itself or the system
 */
trait FinalCountDown extends Actor with ActorLogging with GracefulStopSupport {
	import context._

	val lastWill : () => Unit = () => {}


	/**
	 * kill an actor synchronously (wait until dead)
	 * @param actorRef to kill
	 * @return true or akka.pattern.AskTimeoutException
	 */
	protected def syncKill(actorRef : ActorRef, timeout: FiniteDuration = 1 minute): Try[Boolean] = {
		Try[Boolean] {
			val stopped = gracefulStop(actorRef, timeout)(system)
			Await.result(stopped, timeout)
			true
		}
	}


	/**
	 * execute this function to start shutdown procedure
	 * @param depressionMode if in depressionMode - no messages from children or elsewhere will be received!!
	 */
	protected def waitAndDie(depressionMode: Boolean = true) {
		children foreach watch
		become(if (depressionMode) badNews else badNews orElse receive)
		if (depressionMode)
			log.debug("stopped listening to messages")

		// if there are no children already - no Terminated will come in
		theEnd()
	}

	private def theEnd() {
		if (children.isEmpty) {
			// all children done - safe to commit suicide
			// but execute last will first
			lastWill()

			if (isInstanceOf[LastMohican])
				context.system.shutdown()
			else
				// send a poison pill rather than stop to process
				// messages received if not in depression
				self ! PoisonPill
		}
	}

	private final def badNews : Receive = {

		case Terminated(ref) =>
			log.debug("another one bites the dust {}", ref.path)
			theEnd()
	}
}
