
package gremblor.ecm.data

/** How granular we analyze data in a TimeSeries. */
class TimeGranularity { }

object TimeGranularity {
  case object Raw extends TimeGranularity()
  case object Second extends TimeGranularity()
  case object Minute extends TimeGranularity()
  case object Hour extends TimeGranularity()
  case object Day extends TimeGranularity()

  /**
   * Return the argument to the postgresql DATE_TRUNC() function for a given granularity.
   */
  def sqlDateTruncArg(granularity: TimeGranularity): String = {
    granularity match {
      case TimeGranularity.Second => "'second'"
      case TimeGranularity.Minute => "'minute'"
      case TimeGranularity.Hour => "'hour'"
      case TimeGranularity.Day => "'day'"
      case _ => "'unknown'"
    }
  }
}
