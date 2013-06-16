
package gremblor.ecm.data

import scala.collection.Traversable

import java.math.BigDecimal
import java.util.Date

import com.xeiam.xchange.dto.marketdata.Ticker

import gremblor.ecm.models.TickerModel

/**
 * Time series representation of a data set.
 *
 * Implements the Traversable interface by deferring to the underlying data.
 */
class TimeSeries(
    val granularity: TimeGranularity,
    private val mDataSeries: List[(Date, BigDecimal)])
    extends Traversable[(Date, BigDecimal)] {

  /** Get the raw data */
  def getData(): List[(Date, BigDecimal)] = { mDataSeries }

  def foreach[U](f: ((Date, BigDecimal)) => U): Unit = { mDataSeries.foreach[U](f) }

}

object TimeSeries {
  // Methods to get various timeseries' of ticker data

  /**
   * Get a time series of the avg last price per time granularity unit in the range
   * specified.
   */
  def getTickerLast(start: Date, end: Date, granularity: TimeGranularity): TimeSeries = {
    TickerModel.columnTimeSeries(start, end, granularity, "last")
  }

  def of(granularity: TimeGranularity, values: List[(Date, BigDecimal)]): TimeSeries = {
    new TimeSeries(granularity, values)
  }
}

