
package gremblor.ecm.util

import java.io.StringWriter
import java.io.PrintWriter

/**
 * Utility methods for working with exceptions.
 */
object ExceptionUtils {

  def stackTraceToString(t: Throwable): String = {
    val sw: StringWriter = new StringWriter()
    t.printStackTrace(new PrintWriter(sw))
    sw.toString()
  }
}
