// (c) Copyright 2013 Gremblor Heavy Industries.

package gremblor.ecm.mtgox

import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

import com.xeiam.xchange.Exchange
import com.xeiam.xchange.ExchangeFactory
import com.xeiam.xchange.currency.Currencies
import com.xeiam.xchange.dto.marketdata.OrderBookUpdate
import com.xeiam.xchange.dto.marketdata.Ticker
import com.xeiam.xchange.dto.marketdata.Trade
import com.xeiam.xchange.mtgox.v2.MtGoxExchange
import com.xeiam.xchange.mtgox.v2.service.streaming.MtGoxStreamingConfiguration
import com.xeiam.xchange.service.streaming.ExchangeEvent
import com.xeiam.xchange.service.streaming.ExchangeEventType
import com.xeiam.xchange.service.streaming.ExchangeStreamingConfiguration
import com.xeiam.xchange.service.streaming.StreamingExchangeService

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import gremblor.ecm.models.TickerModel

/**
 * Data source that continuously listens to MtGox for quotes, trades, etc.
 *
 * TODO(aaron): Make this extend ExecutorEngine
 * Note: From 6/14--6/16 it didn't seem like this was working. This is not used for now.
 */
class MtGoxStreamingDataSource {

  throw new RuntimeException(
      "Streaming Data Source is disabled and should really be an ExecutorEngine")


  private val LOG: Logger = LoggerFactory.getLogger(classOf[MtGoxStreamingDataSource])

  private val mMtGoxExchange: Exchange = ExchangeFactory.INSTANCE.createExchange(
      classOf[MtGoxExchange].getName())

  private val mMaxReconnects: Int = 999
  private val mReconnectWaitTimeMillis: Int = 1000
  private val mTimeoutMillis = 30000


  private val mBtcUsdConfiguration: ExchangeStreamingConfiguration =
      new MtGoxStreamingConfiguration(mMaxReconnects, mReconnectWaitTimeMillis, mTimeoutMillis,
          Currencies.BTC, Currencies.USD);

  private val mExchangeDataService: StreamingExchangeService =
      mMtGoxExchange.getStreamingExchangeService(mBtcUsdConfiguration)

  // Executor service that will run the listener thread.
  private val mExecutorService: ExecutorService =
      Executors.newSingleThreadExecutor()

  // Start a background thread to listen for market data events.
  mExecutorService.submit(new MarketDataRunnable())

  /** isRunning is true as long as the thread should be active. */
  private var mIsRunning: Boolean = true


  /** Tell the thread loop to shut down. */
  def shutdown(): Unit = {
    this.synchronized {
      mIsRunning = false
      mExecutorService.shutdown()
      mExchangeDataService.disconnect() // Throws InterruptedException in runnable.
    }
  }

  /**
   * Encapsulates some market data monitoring behavior.
   */
  private class MarketDataRunnable extends Runnable {
    override def run(): Unit = {
      var isRunning: Boolean = MtGoxStreamingDataSource.this.synchronized { mIsRunning }
      LOG.debug("MarketDataRunnable started")
      while (isRunning) {
        try {
          LOG.debug("Listening for exchange event...")
          val exchangeEvent: ExchangeEvent = mExchangeDataService.getNextEvent
          val curTime: Long = System.currentTimeMillis

          LOG.debug("Got exchange event: " + exchangeEvent.toString)
          exchangeEvent.getEventType match {
            case ExchangeEventType.TICKER => {
              val ticker: Ticker = exchangeEvent.getPayload.asInstanceOf[Ticker]
              TickerModel.createAndSave(ticker)
              LOG.debug(ticker.toString)
            }
            case ExchangeEventType.TRADE => {
              val trade: Trade = exchangeEvent.getPayload.asInstanceOf[Trade]
              // TODO(aaron) :Handle trade.
              LOG.debug(trade.toString)
            }
            case ExchangeEventType.DEPTH => {
              val orderBookUpdate: OrderBookUpdate =
                  exchangeEvent.getPayload.asInstanceOf[OrderBookUpdate]
              // TODO(aaron): Handle order book depth
              LOG.debug(orderBookUpdate.toString)
            }
            case _ => {
              System.out.println("Unknown event type: " + exchangeEvent.getEventType.toString)
            }
          }

        } catch {
          case ie: InterruptedException => {
            /* Check for isRunning and loop. */
            LOG.debug("Interruption in exchange listener: " + ie.getMessage)
          }
          case exn: Exception => {
            LOG.error("Exception in market data service runnable: " + exn.getMessage)
            throw exn // Let the runnable die and be restarted.
          }
        }

        isRunning = MtGoxStreamingDataSource.this.synchronized { mIsRunning }
      }

      LOG.info("MarketDataRunnable service shut down (isRunning=false)")
    }
  }
}
