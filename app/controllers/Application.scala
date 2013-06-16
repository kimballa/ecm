package controllers

import java.util.Date

import play.api._
import play.api.mvc._

import com.xeiam.xchange.dto.marketdata.Ticker

import gremblor.ecm.data.TimeGranularity
import gremblor.ecm.models.TickerModel

object Application extends Controller {

  def index = Action {

    val now: Long = System.currentTimeMillis
    val oneHourAgo: Long = now - (60 * 60 * 1000)
    val oneWeekAgo: Long = now - (7 * 24 * 60 * 60 * 1000)

    val nowDate: Date = new Date(now)
    val oneHourAgoDate: Date = new Date(oneHourAgo)
    val oneWeekAgoDate: Date = new Date(oneWeekAgo)

    val lastHourQuotes: List[TickerModel] = TickerModel.smoothedTimeSeries(oneHourAgoDate,
        nowDate, TimeGranularity.Minute)
    val lastHourTickers: List[Ticker] = lastHourQuotes.map( model => model.toTicker() )

    val lastWeekQuotes: List[TickerModel] = TickerModel.smoothedTimeSeries(oneWeekAgoDate,
        nowDate, TimeGranularity.Hour)
    val lastWeekTickers: List[Ticker] = lastWeekQuotes.map( model => model.toTicker() )

    // Most recent ticker value.
    val curTicker: Ticker = TickerModel.recent(1).head.toTicker

    // And render the page with the current ticker symbol.
    Ok(gremblor.ecm.views.html.index(None, curTicker, lastHourTickers, lastWeekTickers))
  }

}
