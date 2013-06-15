
package gremblor.ecm.data

/** How granular we analyze data in a TimeSeries. */
class TimeGranularity { }

object TimeGranularity {
  case object Raw extends TimeGranularity()
  case object Second extends TimeGranularity()
  case object Minute extends TimeGranularity()
  case object Hour extends TimeGranularity()
  case object Day extends TimeGranularity()
}
