
package gremblor.ecm.models

/**
 * What kind of order is being placed in an Order.
 */
class OrderType() { }

object OrderType {
  case object MarketBuy extends OrderType()
  case object MarketSell extends OrderType()
  case object LimitBuy extends OrderType()
  case object LimitSell extends OrderType()
  case object Nil extends OrderType()
}
