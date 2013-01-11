package com.outsmart.actor.service

import akka.actor.{Props, ActorRef}
import com.outsmart.measurement._
import com.outsmart.actor.write.WriterMasterAwareActor

/**
 * Rollup by customer and location
 *
 * @author Vadim Bobrov
 */
class AggregatorActor(val boundary: Int = 60000) extends WriterMasterAwareActor {

	import context._
	var interpolatorFactory  : String => ActorRef = DefaultInterpolatorFactory.get

	protected def receive: Receive = {

		case msmt : Measurement => interpolatorFactory(msmt.wireid) ! msmt


	}

	object DefaultInterpolatorFactory {
		var interpolators = Map[String, ActorRef]()

		def get(wireid : String) : ActorRef = {
			if (!interpolators.contains(wireid)) {
				log.info("creating new interpolator for " + wireid)
				interpolators += (wireid -> actorOf(Props(new InterpolatorActor())))
			}

			interpolators(wireid)
		}
	}


 }
