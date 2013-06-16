// Copyright 2013 Gremblor Heavy Industries

package gremblor.ecm.mtgox

import com.xeiam.xchange.dto.marketdata.Ticker

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import gremblor.ecm.models.TickerModel
import gremblor.ecm.tasks.ExecutorEngine

/**
 * Executor-based engine that runs a polling loop to check for tickers from MtGox
 * using the xeiam library.
 */
class MtGoxTickerPollingEngine extends ExecutorEngine {
  private val LOG: Logger = LoggerFactory.getLogger(classOf[MtGoxTickerPollingEngine])

  override def runnableInstance: Runnable = new TickerPollingRunnable

  /** Polling interval (5 seconds) */
  private val mUpdateInterval: Int = 5000

  /** Ticker lifetime (start a new high/low window) in milliseconds. */
  private val mTickerLifetime: Int = 120000

  /** @reutrn true if the tickers are equal. */
  private def equalTickers(t1: Ticker, t2: Ticker): Boolean = {
    t1.getTimestamp().equals(t2.getTimestamp) &&
      t1.getLast().equals(t2.getLast()) &&
      t1.getBid().equals(t2.getBid()) &&
      t1.getAsk().equals(t2.getAsk()) &&
      t1.getHigh().equals(t2.getHigh()) &&
      t1.getLow().equals(t2.getLow()) &&
      t1.getVolume().equals(t2.getVolume()) &&
      t1.getTradableIdentifier().equals(t2.getTradableIdentifier)
  }

  /** Main thread loop. Poll for ticker update info. */
  private class TickerPollingRunnable extends Runnable {
    override def run(): Unit = {
      var prevTicker: Option[Ticker] = None

      var mtGoxTicker: MtGoxTicker = new MtGoxTicker
      var tickerStartTime: Long = System.currentTimeMillis

      LOG.debug("Started MtGox main ticker listener: polling for updates.")
      while (!isShutdown()) {
        try {
          val curTime: Long = System.currentTimeMillis
          if (curTime - tickerStartTime > mTickerLifetime) {
            // Close the current ticker instance and start a new one.
            mtGoxTicker = new MtGoxTicker
            tickerStartTime = curTime
          }

          // Get the most recent value and save it.
          val ticker: Ticker = mtGoxTicker.getQuote()

          // If the ticker appears to be the same as the previously-retrieved value, skip it.
          // Under some circumstances (e.g., server restart), this may still record duplicate
          // entries.
          val doUpdate = prevTicker.isEmpty || !equalTickers(prevTicker.get, ticker)

          if (doUpdate) {
            // Save the current ticker value since it's new.
            LOG.debug("New ticker from MtGox TickerPollingEngine: " + ticker)
            TickerModel.createAndSave(ticker)
          }

          prevTicker = Some(ticker)

          Thread.sleep(mUpdateInterval)
        } catch {
          case ie: InterruptedException => { /* check to see if we're still running and loop. */ }
        }
      }
    }
  }
}
