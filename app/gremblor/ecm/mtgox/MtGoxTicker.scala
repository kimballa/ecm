// (c) Copyright 2013 Gremblor Heavy Industries.

package gremblor.ecm.mtgox

import com.xeiam.xchange.Exchange
import com.xeiam.xchange.ExchangeFactory
import com.xeiam.xchange.currency.Currencies
import com.xeiam.xchange.dto.marketdata.Ticker
import com.xeiam.xchange.mtgox.v2.MtGoxExchange
import com.xeiam.xchange.service.polling.PollingMarketDataService

/**
 * Stock Ticker that accesses MtGox for quotes.
 *
 * <p>Use the getQuote() method to get the latest quote.</p>
 */
class MtGoxTicker {

  private val mMtGoxExchange: Exchange = ExchangeFactory.INSTANCE.createExchange(
      classOf[MtGoxExchange].getName())

  private val mMarketDataService: PollingMarketDataService =
      mMtGoxExchange.getPollingMarketDataService()

  /** Return the latest price quote. */
  def getQuote(): Ticker = {
    mMarketDataService.getTicker(Currencies.BTC, Currencies.USD)
  }
}
