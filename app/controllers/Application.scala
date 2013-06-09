package controllers

import play.api._
import play.api.mvc._

import gremblor.ecm.models.TickerModel
import com.xeiam.xchange.dto.marketdata.Ticker

object Application extends Controller {

  def index = Action {
    val recentQuotes: List[TickerModel] = TickerModel.recent(30)
    val recentTickers: List[Ticker] = recentQuotes.map( model => model.toTicker() )

    val curTicker: Ticker = (
      if (recentTickers.isEmpty) {
        Ticker.TickerBuilder.newInstance().build() }
      else {
        recentTickers.head
      }
    )

    // And render the page with the current ticker symbol.
    Ok(gremblor.ecm.views.html.index(None, curTicker, recentTickers))
  }

}
