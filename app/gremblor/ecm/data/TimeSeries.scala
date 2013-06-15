
package gremblor.ecm.data

import java.util.Date

import com.xeiam.xchange.dto.marketdata.Ticker

/**
 * Time series representation of a data set.
 */
class TimeSeries(start: Date, end: Date, granularity: TimeGranularity) {

  private val mDataSeries: List[Ticker] = List() // TODO: Do this for realz.

}

