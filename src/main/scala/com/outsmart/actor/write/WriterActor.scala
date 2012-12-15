package com.outsmart.actor.write

import akka.actor.Actor
import com.outsmart.dao.Writer
import com.outsmart.util.Util
import Util.withOpenClose

/**
  * @author Vadim Bobrov
  */
class WriterActor(val writer : Writer) extends Actor {

   protected def receive: Receive = {
     case work: WriteWork =>
       withOpenClose(writer) {
         work.measurements foreach writer.write
       }
       sender ! WorkDone()

   }

 }
