// Copyright 2013 Gremblor Heavy Industries

import play.api._

import java.math.BigDecimal
import java.util.Date

import com.xeiam.xchange.dto.marketdata.Ticker

import org.joda.money.BigMoney
import org.joda.money.CurrencyUnit

import gremblor.ecm.models.TickerModel
import gremblor.ecm.mtgox.MtGoxTickerPollingEngine
import gremblor.ecm.mtgox.MtGoxFastTickerEngine
import gremblor.ecm.tasks.ExecutorEngine
import gremblor.ecm.tasks.StrategyEngine
import gremblor.ecm.tasks.WishfulThinkingTradingEngine

/** Application-global settings and initialization. */
object Global extends GlobalSettings {

  // trade strategy engine that receives input data and acts on it.
  private var mStrategyEngine: Option[StrategyEngine] = None

  /** Set of ExecutorEngines to start and stop with the app. */
  private var mExecutorEngines: Option[List[ExecutorEngine]] = None

  /**
   * To be called by onStart(); initializes a list of ExecutorEngines to run.
   */
  private def getEngines(): List[ExecutorEngine] = {
    List(
      new MtGoxTickerPollingEngine, // Use the xeiam MtGox polling API.
      new MtGoxFastTickerEngine // Use the MtGox JSON fast ticker API.
    )
  }

  /**
   * Startup function called as the app begins.
   * Initialize our background tasks, etc.
   */
  override def onStart(app: Application) {
    super.onStart(app)

    bootstrapDatabase()

    // Create various engine instances. These automatically start background threads.

    // Create the strategy engine to accept input data.
    mStrategyEngine = Some(new StrategyEngine(new WishfulThinkingTradingEngine))

    mExecutorEngines = Some(getEngines())
  }

  /**
   * Populate test database with initial conditions
   */
  private def bootstrapDatabase(): Unit = {
    // There must be at least one BTC price point in the database.
    // TODO(aaron): Poll for the latest real value, not just a zero.
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
    if (mExecutorEngines.isDefined) {
      mExecutorEngines.get.foreach { engine : ExecutorEngine =>
        engine.shutdown()
      }
    }
    super.onStop(app)
  }
}
