// Copyright 2013 Gremblor Heavy Industries

import play.api._

import java.math.BigDecimal
import java.util.Date

import com.xeiam.xchange.dto.marketdata.Ticker

import org.joda.money.BigMoney
import org.joda.money.CurrencyUnit

import gremblor.ecm.mtgox.MtGoxStreamingDataSource
import gremblor.ecm.models.TickerModel
import gremblor.ecm.tasks.TickerThread

/** Application-global settings and initialization. */
object Global extends GlobalSettings {

  private var mDataSource: Option[MtGoxStreamingDataSource] = None

  /**
   * Startup function called as the app begins.
   * Initialize our background tasks, etc.
   */
  override def onStart(app: Application) {
    super.onStart(app)

    bootstrapDatabase()

    // Retrieve market information in background threads.
    mDataSource = Some(new MtGoxStreamingDataSource)
    val tickerThread = new TickerThread()
    tickerThread.start
  }

  /**
   * Populate test database with initial conditions
   */
  private def bootstrapDatabase(): Unit = {
    // There must be at least one BTC price point in the database.
    val maybeTicker: Option[TickerModel] = TickerModel.any
    if (!maybeTicker.isDefined) {
      val ticker: Ticker = Ticker.TickerBuilder.newInstance()
          .withTimestamp(new Date(System.currentTimeMillis))
          .withBid(BigMoney.of(CurrencyUnit.of("USD"), 0))
          .withAsk(BigMoney.of(CurrencyUnit.of("USD"), 0))
          .withHigh(BigMoney.of(CurrencyUnit.of("USD"), 0))
          .withLow(BigMoney.of(CurrencyUnit.of("USD"), 0))
          .withLast(BigMoney.of(CurrencyUnit.of("USD"), 0))
          .withVolume(new BigDecimal(0))
          .withTradableIdentifier("BTC")
          .build()
      TickerModel.createAndSave(ticker)
    }
  }

  override def onStop(app: Application) {
    if (mDataSource.isDefined) {
      mDataSource.get.shutdown()
    }
    super.onStop(app)
  }
}
