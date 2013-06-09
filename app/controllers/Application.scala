package controllers

import play.api._
import play.api.mvc._

import gremblor.ecm.models.TickerModel
import gremblor.ecm.mtgox.MtGoxTicker
import com.xeiam.xchange.dto.marketdata.Ticker

object Application extends Controller {

  def index = Action {
    val mtGoxTicker: MtGoxTicker = new MtGoxTicker
    val ticker: Ticker = mtGoxTicker.getQuote()

    // Save the current ticker value.
    TickerModel.create(ticker)

    // And render the page with the current ticker symbol.
    Ok(gremblor.ecm.views.html.index(None, ticker))
  }

}
