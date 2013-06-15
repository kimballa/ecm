// Copyright 2013 Gremblor Heavy Industries

package gremblor.ecm.tasks

import gremblor.ecm.models.TickerModel
import gremblor.ecm.mtgox.MtGoxTicker
import com.xeiam.xchange.dto.marketdata.Ticker

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class TickerThread extends Thread {
  private val LOG: Logger = LoggerFactory.getLogger(classOf[TickerThread])

  setDaemon(true)

  /** isRunning is true as long as the thread should be active. */
  private var mIsRunning: Boolean = true

  /** Polling interval (2.5 seconds) */
  private val mUpdateInterval: Int = 2500


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
  override def run(): Unit = {
    var isRunning: Boolean = this.synchronized { mIsRunning }
    var prevTicker: Option[Ticker] = None

    var mtGoxTicker: MtGoxTicker = new MtGoxTicker
    var tickerStartTime: Long = System.currentTimeMillis

    LOG.debug("Started TickerThread")
    while (isRunning) {
      val curTime: Long = System.currentTimeMillis
      if (curTime - tickerStartTime > mTickerLifetime) {
        // Close the current ticker instance and start a new one.
        mtGoxTicker = new MtGoxTicker
        tickerStartTime = curTime
      }

      // Get the most recent value and save it.
      val ticker: Ticker = mtGoxTicker.getQuote()

      // If the ticker appears to be the same as the previously-retrieved value, skip it.
      // Under some circumstances (e.g., server restart), this may still record duplicate entries.
      val doUpdate = prevTicker.isEmpty || !equalTickers(prevTicker.get, ticker)

      if (doUpdate) {
        // Save the current ticker value since it's new.
        LOG.debug("New ticker from TickerThread: " + ticker)
        TickerModel.createAndSave(ticker)
      }

      prevTicker = Some(ticker)

      Thread.sleep(mUpdateInterval)
      isRunning = this.synchronized { mIsRunning }
    }
  }
}
