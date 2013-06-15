
package gremblor.ecm.strat

import com.xeiam.xchange.dto.marketdata.Ticker

import gremblor.ecm.models.Order
import gremblor.ecm.models.Order.NilOrder
import gremblor.ecm.models.Purse

/**
 * SPI to be implemented by trading strategies.
 *
 * A Strategy processes a stream of incoming information in conjunction
 * with other info (e.g., historical data it requests). The Strategy is
 * given a callback for each new incoming datum, and is parameterized by
 * a Purse: a balance sheet containing the bitcoins and dollars it has to
 * tade with.
 *
 * The callback functions may emit an Order to be fulfilled by the
 * order execution engine. Creation of Orders is managed by the Purse.
 * If no action should be taken, emit a NilOrder.
 */
abstract class Strategy(purse: Purse) {
  /**
   * Consider trading based on new ticker information.
   *
   * @param ticker the ticker received
   * @return an order to execute based on this information.
   */
  def onTicker(ticker: Ticker): Order
}
