package com.outsmart.actor

import akka.actor._
import akka.actor.Terminated
import write.GracefulStop

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
trait FinalCountDown extends Actor with ActorLogging {
	import context._

	val lastWill : () => Unit = () => {}

	def killChild(child : ActorRef, andThen : () => Unit) {
		watch(child)
		become(waitForDeath(child, andThen))
		child ! GracefulStop

		def waitForDeath(toWait : ActorRef, andThen : () => Unit) : Receive = {
			case Terminated(ref) =>
				if (ref == toWait)
					andThen()
		}
	}


	def onBlackSpot() {
		children foreach watch
		// from now on receive only bad news
		become(lastMoments)
	}

	final def lastMoments : Receive = {

		case Terminated(ref) =>
			log.debug("another one bites the dust {}" + ref.path)

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
