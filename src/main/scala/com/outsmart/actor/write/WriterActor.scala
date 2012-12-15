package com.outsmart.actor.write

import akka.actor.Actor
import com.outsmart.dao.Writer

/**
  * @author Vadim Bobrov
  */
class WriterActor(val writer : Writer) extends Actor {

   protected def receive: Receive = {
     case work : WriteWork => println
   }

 }
