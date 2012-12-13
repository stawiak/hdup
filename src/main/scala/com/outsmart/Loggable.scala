package com.outsmart

/**
 * @author Vadim Bobrov
 */
trait Loggable {
  self =>

  //val logger = Slf4jLoggerFactory.getLogger(self.getClass())

  /*
    Note the use of the parameterless function msg: => T as input parameter for the debug method. The main reason why we use the isDebugEnabled() check
    is to make sure that the String that is logged is only computed if the debug level is enabled. So if the debug method would only accept a String
    as input parameter, the log message would always get computed no matter whether the debug loglevel is enabled or not, which is not desirable. By
    passing the parameterless function msg: => T instead of a String however, we get exactly what we want: the msg function that will return the String
    to be logged is only invoked when the isDebugEnabled check succeeds. If the isDebugEnabled check fails the msg function is never called and therefore
    no unnecessary String is computed.
  */
  def debug[T](msg: => T):Unit = {
    //if (logger.isDebugEnabled()) logger.debug(msg.toString)
  }
}