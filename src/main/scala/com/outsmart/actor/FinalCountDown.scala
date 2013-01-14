package com.outsmart.actor

import akka.actor._
import akka.actor.Terminated

/**
 * @author Vadim Bobrov
*/

case object GracefulStop
/**
 * A marker trait to indicate that this is the last actor and
 * the entire actor system needs to be shut down when done
 */
trait LastMohican


/**
 * This trait adds functionality to wait for the children actors to finish their
 * work and then stop itself or the system
 */
trait FinalCountDown extends Actor with ActorLogging {
	import context._

	val lastWill : () => Unit = () => {}

	/**
	 * kill a child actor and do andThen when it's dead
	 */
	def killChild(child : ActorRef, andThenDo : () => Unit) {
		watch(child)
		become(waitForDeath(child, andThenDo))
		log.debug("sending graceful stop to " + child.path)
		child ! GracefulStop

		def waitForDeath(toWait : ActorRef, andThenDo : () => Unit) : Receive = {
			case Terminated(ref) =>
				log.debug("death of " + ref.path + " while waiting for "  + toWait.path)
				andThenDo()
		}
	}


	/**
	 * execute this function to start shutdown procedure
	 */
	def onBlackSpot() {
		children foreach watch
		// from now on receive only bad news
		become(lastMoments)
	}

	final def lastMoments : Receive = {

		case Terminated(ref) =>
			log.debug("another one bites the dust {}", ref.path)

			if (children.isEmpty) {
				// all children done - safe to commit suicide
				// but execute last will first
				lastWill()

				if (isInstanceOf[LastMohican])
					context.system.shutdown()
				else
					stop(self)
			}

	}
}
