
package gremblor.ecm.models

import java.math.BigDecimal
import java.util.Date

import org.joda.money.BigMoney
import org.joda.money.CurrencyUnit

/**
 * A data structure representing an order to be submitted to the market.
 */
class Order(
    val orderType: OrderType,
    val symbol: String, // What to buy or sell (must be "BTC")
    val quantity: BigDecimal, // How many units of the symbol to buy or sell
    val expectedPrice: BigMoney, // How much we expect this to cost or pay out.
                                 // Market orders may execute differently than this.
                                 // Our prices should always be in USD.
    val limitPrice: Option[BigMoney], // For limit orders
    val orderTimestamp: Date // When this order was created
    ) {


  // TODO: Track all info about fulfillments in a separate object.

  /** Timestamp at which an order is marked Filled, Canceled, CanceledPartialFill, or Invalid. */
  var completionTime: Option[Date] = None

  /** Status of this order (Pending, Submitted, Filled, etc. */
  var orderStatus: OrderStatus = OrderStatus.Pending

}

object Order {
  /** An "Order" that intends to take no action. */
  object NilOrder extends Order(
      OrderType.Nil, "BTC", BigDecimal.ZERO,
      BigMoney.zero(CurrencyUnit.of("USD")), None, new Date())
}
