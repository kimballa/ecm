// (c) Copyright 2013 Gremblor Heavy Industries

package gremblor.ecm.models

import java.math.BigDecimal
import java.util.Date

import anorm._
import anorm.SqlParser._

import com.xeiam.xchange.dto.marketdata.Ticker

import play.api.db._
import play.api.Play.current

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
    last: BigDecimal)

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
          TickerModel(id, timestamp, tradeSymbol, quoteSymbol, bid, ask, high, low, volume, last)
    }
  }

  def all(): List[TickerModel] = DB.withConnection { implicit c =>
    SQL("SELECT * FROM ticker").as(tickerModel *)
  }

  def create(inputTicker: com.xeiam.xchange.dto.marketdata.Ticker) {
    DB.withConnection { implicit c =>
      SQL("""
        | INSERT INTO ticker (timestamp, tradeSymbol, quoteSymbol, bid, ask, high, low,
        |     volume, last)
        | VALUES ({timestamp}, {tradeSymbol}, {quoteSymbol}, {bid}, {ask}, {high}, {low},
        |     {volume}, {last})
        |""".stripMargin).on(
          'timestamp -> inputTicker.getTimestamp(),
          'tradeSymbol -> inputTicker.getTradableIdentifier(),
          'quoteSymbol -> inputTicker.getBid().getCurrencyUnit().toString(),
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

