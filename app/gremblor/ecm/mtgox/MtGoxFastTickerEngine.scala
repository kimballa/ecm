// Copyright 2013 Gremblor Heavy Industries

package gremblor.ecm.mtgox

import java.math.BigDecimal
import java.util.Date

import scala.util.parsing.json._

import scalaj.http.Http

import com.xeiam.xchange.dto.marketdata.Ticker

import org.joda.money.BigMoney
import org.joda.money.CurrencyUnit
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import gremblor.ecm.models.TickerModel
import gremblor.ecm.tasks.ExecutorEngine
import gremblor.ecm.util.JsonMatchers._

/**
 * Executor-based engine that runs a polling loop to check for tickers from MtGox
 * using http://data.mtgox.com/api/2/BTCUSD/money/ticker_fast which allows
 * much faster polling.
 */
class MtGoxFastTickerEngine extends ExecutorEngine {
  private val LOG: Logger = LoggerFactory.getLogger(classOf[MtGoxFastTickerEngine])

  override def runnableInstance: Runnable = new TickerPollingRunnable

  private val mFastTickerUrl: String = "http://data.mtgox.com/api/2/BTCUSD/money/ticker_fast"

  /** Polling interval (1 second) */
  private val mUpdateInterval: Int = 1000

  /** @reutrn true if the tickers are equal. */
  private def equalTickers(t1: Ticker, t2: Ticker): Boolean = {
    // NOTE: This is the same as the one in MtGoxTickerPollingEngine except that it ignores
    // timestamp, since the fast_ticker API always returns a new timestamp.
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

      var tickerStartTime: Long = System.currentTimeMillis

      LOG.debug("Started MtGox fast ticker listener: polling for updates.")
      while (!isShutdown()) {
        try {
          // Get the most recent fast quote from MtGox.
          val fastTickerJson = Http(mFastTickerUrl).asString

          // Get the most recent "full" ticker model from the database to populate vol/high/low.
          // TODO: Switch to the latest "real" ticker from the slow poll event pipe; otherwise
          // since our timestamps can be ahead of theirs, we might not pick up the latest true
          // high/low/volume.
          val mostRecentFullTicker: Ticker = TickerModel.recent(1).head.toTicker

          val latestFastTicker: Ticker = parse(fastTickerJson, mostRecentFullTicker)

          // If the ticker appears to be the same as the previously-retrieved value, skip it.
          // Under some circumstances (e.g., server restart), this may still record duplicate
          // entries.
          val doUpdate = prevTicker.isEmpty || !equalTickers(prevTicker.get, latestFastTicker)

          if (doUpdate) {
            // Save the current ticker value since it's new.
            LOG.debug("New ticker from MtGox Fast Ticker: " + latestFastTicker)
            TickerModel.createAndSave(latestFastTicker)
          }

          prevTicker = Some(latestFastTicker)

          Thread.sleep(mUpdateInterval)
        } catch {
          case ie: InterruptedException => { /* check to see if we're still running and loop. */ }
        }
      }
    }

    /**
     * Create a Ticker from the fast ticker data, filling in missing details
     * for min/max/vol from lastFullTicker.
     */
    private def parse(fastTickerData: String, lastFullTicker: Ticker): Ticker = {
      // Parse the top-level object into 'top'.
      val top = JMap(JSON.parseFull(fastTickerData).get)
      val data = JMap(top("data"))
      val lastMap = JMap(data("last")) // JSON in data.last, etc.
      val buyMap = JMap(data("buy"))
      val sellMap = JMap(data("sell"))
      val timestamp: Long = JLongStr(data("now")) / 1000 // timestamp is in microseconds.

      val last: BigMoney = parseMoney(lastMap)
      val bid: BigMoney = parseMoney(buyMap)
      val ask: BigMoney = parseMoney(sellMap)

      Ticker.TickerBuilder.newInstance()
          .withAsk(ask)
          .withBid(bid)
          .withHigh(lastFullTicker.getHigh())
          .withLast(last)
          .withLow(lastFullTicker.getLow())
          .withTimestamp(new Date(timestamp))
          .withTradableIdentifier(last.getCurrencyUnit().getCode())
          .withVolume(lastFullTicker.getVolume())
          .build()
    }

    /**
     * Parse a money JSON structure from MtGox. This is a JSON record
     * with the following relevant fields:
     *   value_int: long value containing the real money val * 10000
     *   currency: string containing currency code.
     */
    private def parseMoney(data: Map[String, Any]): BigMoney = {
      val value: Long = JLongStr(data("value_int"))
      val currencyCode: String = JString(data("currency"))
      val valueBigDec: BigDecimal = (new BigDecimal(value)).movePointLeft(5).setScale(8)
      BigMoney.of(CurrencyUnit.of(currencyCode), valueBigDec)
    }
  }
}
