/**
 * @author Vadim Bobrov
 */
import concurrent.Future
import concurrent.ExecutionContext.Implicits.global

/**
 * @author Vadim Bobrov
 */

val a = Future {"res"}

a map identity

