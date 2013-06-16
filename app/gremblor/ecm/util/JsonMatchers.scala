
package gremblor.ecm.util

/**
 * Helpers that will match elements of JSON objects parsed by Scala's JSON lib
 * and type-cast them appropriately. See
 * http://stackoverflow.com/questions/4170949/how-to-parse-json-in-scala-using-standard-scala-classes
 */
object JsonMatchers {
  def JMap(v: Any): Map[String, Any] = { v.asInstanceOf[Map[String, Any]] }
  def JList(v: Any): List[Any] = { v.asInstanceOf[List[Any]] }
  def JString(v: Any): String = { v.asInstanceOf[String] }
  def JLong(v: Any): Long = { v.asInstanceOf[Long] }

  /** Convert a JSON String into a Long by parsing it in base 10 and return the value. */
  def JLongStr(v: Any): Long = { v.asInstanceOf[String].toLong }
}
