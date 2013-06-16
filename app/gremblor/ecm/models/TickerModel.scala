// (c) Copyright 2013 Gremblor Heavy Industries

package gremblor.ecm.models

import java.math.BigDecimal
import java.util.Date
import java.util.NoSuchElementException

import anorm._
import anorm.SqlParser._

import com.xeiam.xchange.dto.marketdata.Ticker
import org.joda.money.BigMoney
import org.joda.money.CurrencyUnit

import play.api.db._
import play.api.Play.current

import gremblor.ecm.data.TimeGranularity
import gremblor.ecm.data.TimeSeries

/**
 * Represents the ticker information for a tradeable item.
 *
 * This class is intended to be persisted in anorm. You should use
 * xchange's Ticker class to manipulate these.
 */
case class TickerModel(id: Long,
    timestamp: Date,
    tradeSymbol: String, // What's being traded (BTC)
                         // Max length 8 chars.
    quoteSymbol: String, // Expressed in these units (USD)
    bid: BigDecimal, // All stored as DECIMAL(20, 8)
    ask: BigDecimal,
    high: BigDecimal,
    low: BigDecimal,
    volume: BigDecimal,
    last: BigDecimal) {


  /**
   * Deserializes this TickerModel into an xeiam Ticker.
   *
   * @return an xeiam Ticker representing this data.
   */
  def toTicker(): Ticker = {
    Ticker.TickerBuilder.newInstance()
        .withBid(toMoney(bid, quoteSymbol))
        .withAsk(toMoney(ask, quoteSymbol))
        .withHigh(toMoney(high, quoteSymbol))
        .withLow(toMoney(low, quoteSymbol))
        .withVolume(volume)
        .withLast(toMoney(last, quoteSymbol))
        .withTradableIdentifier(tradeSymbol)
        .withTimestamp(timestamp)
        .build()
  }

  override def equals(that: Any): Boolean = {
    that match {
      case t: TickerModel => {
        timestamp.equals(t.timestamp) &&
            tradeSymbol == t.tradeSymbol &&
            quoteSymbol == t.quoteSymbol &&
            bid.equals(t.bid) &&
            ask.equals(t.ask) &&
            high.equals(t.high) &&
            low.equals(t.low) &&
            volume.equals(t.volume) &&
            last.equals(t.last)
      }
      case _ => false
    }
  }

  override def hashCode(): Int = {
    timestamp.hashCode ^ tradeSymbol.hashCode ^ bid.hashCode ^ volume.hashCode ^ last.hashCode
  }

  /**
   * Convert a BigDecimal and a currency unit to a joda BigMoney.
   *
   * @param value the amount of money.
   * @param currency the string representing the currency identifier.
   * @return the BigMoney representing the value and currency.
   */
  private def toMoney(value: BigDecimal, currency: String): BigMoney = {
    BigMoney.of(CurrencyUnit.of(currency.trim()), value)
  }
}

object TickerModel {

  // Anorm Mapping from ResultSet to TickerModel.
  val tickerModel = {
    get[Long]("id") ~
    get[Date]("timestamp") ~
    get[String]("tradeSymbol") ~
    get[String]("quoteSymbol") ~
    get[BigDecimal]("bid") ~
    get[BigDecimal]("ask") ~
    get[BigDecimal]("high") ~
    get[BigDecimal]("low") ~
    get[BigDecimal]("volume") ~
    get[BigDecimal]("last") map {
      case id ~ timestamp ~ tradeSymbol ~ quoteSymbol ~ bid ~ ask ~ high ~ low ~ volume ~ last =>
          TickerModel(id, timestamp, tradeSymbol.trim(), quoteSymbol.trim(),
              bid, ask, high, low, volume, last)
    }
  }

  /** Return a list of all TickerModel objects. */
  def all(): List[TickerModel] = DB.withConnection { implicit c =>
    SQL("SELECT * FROM ticker").as(tickerModel *)
  }

  /** Return the number of TickerModel objects in the database. */
  def count(): Long = DB.withConnection { implicit c =>
    val firstRow = SQL("SELECT COUNT(*) AS cnt FROM ticker").apply().head
    firstRow[Long]("cnt")
  }

  /** Return the first TickerModel we can find in the database, or None. */
  def any(): Option[TickerModel] = DB.withConnection { implicit c =>
    try {
      Some(SQL("SELECT * FROM ticker LIMIT 1").as(tickerModel *).head)
    } catch {
      case nsee: NoSuchElementException => { None }
    }
  }

  /**
   * Return the postgresql aggregate function to use for smoothing a given column.
   */
  private def aggregateFuncForCol(colName: String): String = {
    colName match {
      case "last" => "AVG"
      case "bid" => "AVG"
      case "ask" => "AVG"
      case "low" => "MIN"
      case "high" => "MAX"
      case _ => throw new RuntimeException("Cannot get time series of ticker col: " + colName)
    }
  }

  /**
   * Return a List of TickerModel objects that represent tickers for the smoothed values
   * of all columns over the time range and granularity specified.
   */
  def smoothedTimeSeries(start: Date, end: Date, granularity: TimeGranularity): List[TickerModel] =
      DB.withConnection { implicit c =>

    // Convert colName and granularity into components of the SQL statement to generate.
    val dateTruncArg: String = TimeGranularity.sqlDateTruncArg(granularity)

    SQL("SELECT 0 as id, DATE_TRUNC(" + dateTruncArg + ", timestamp) AS timestamp, " +
        "'BTC' AS tradeSymbol, 'USD' AS quoteSymbol, AVG(bid) AS bid, AVG(ask) AS ask, " +
        "MAX(high) AS high, MIN(low) AS low, AVG(volume) AS volume, AVG(last) AS last " +
        "FROM ticker " +
        "WHERE timestamp >= {start} AND timestamp < {end} " +
        "GROUP BY DATE_TRUNC(" + dateTruncArg + ", timestamp) ORDER BY timestamp")
        .on("start" -> start).on("end" -> end).as(tickerModel *)
  }


  /**
   * Return a TimeSeries containing smoothed values for a given column of the tickers.
   */
  def columnTimeSeries(start: Date, end: Date, granularity: TimeGranularity, colName: String)
      : TimeSeries = DB.withConnection { implicit c =>

    // Convert colName and granularity into components of the SQL statement to generate.
    val aggregateFn: String = aggregateFuncForCol(colName)
    val dateTruncArg: String = TimeGranularity.sqlDateTruncArg(granularity)

    val rows = SQL("SELECT DATE_TRUNC(" + dateTruncArg + ", timestamp) AS ts, " +
        aggregateFn + "(" + colName + " ) AS series FROM ticker " +
        "WHERE timestamp >= {start} AND timestamp < {end} " +
        "GROUP BY ts ORDER BY ts").on("start" -> start).on("end" -> end).apply()

    // Return a TimeSeries object.
    TimeSeries.of(
        granularity,
        // For each returned row, extract (ts, series) and convert the stream to a list.
        (rows.map { row => (row[Date]("ts"), row[BigDecimal]("series")) }).toList)
  }

  /**
   * Get the most recent 'max' ticker values.
   *
   * @param max the maximum number of values to return
   * @return a list of ticker values in descending timestamp order.
   */
  def recent(max: Int): List[TickerModel] = DB.withConnection { implicit c =>
    SQL("SELECT * FROM ticker ORDER BY timestamp DESC LIMIT {max}").on(
      'max -> max).as(tickerModel *)
  }

  /**
   * Create a new TickerModel and persist it to the database.
   */
  def createAndSave(inputTicker: com.xeiam.xchange.dto.marketdata.Ticker) {
    DB.withConnection { implicit c =>
      SQL("""
        | INSERT INTO ticker (timestamp, tradeSymbol, quoteSymbol, bid, ask, high, low,
        |     volume, last)
        | VALUES ({timestamp}, {tradeSymbol}, {quoteSymbol}, {bid}, {ask}, {high}, {low},
        |     {volume}, {last})
        |""".stripMargin).on(
          'timestamp -> inputTicker.getTimestamp(),
          'tradeSymbol -> inputTicker.getTradableIdentifier(),
          'quoteSymbol -> inputTicker.getBid().getCurrencyUnit().getCode(),
          'bid -> inputTicker.getBid().getAmount(),
          'ask -> inputTicker.getAsk().getAmount(),
          'high -> inputTicker.getHigh().getAmount(),
          'low -> inputTicker.getLow().getAmount(),
          'volume -> inputTicker.getVolume(),
          'last -> inputTicker.getLast().getAmount()
        ).executeUpdate()
    }
  }
}

