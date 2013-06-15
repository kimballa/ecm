
package gremblor.ecm.models

/**
 * The state of an order being submitted to a trading market.
 */
class OrderStatus() { }

object OrderStatus {
  /** Newly created, not submitted. */
  case object Pending extends OrderStatus()

  /** Submitted to the market but not filled. */
  case object Submitted extends OrderStatus()

  /** At least one trade has occurred but the whole order is not handled yet. */
  case object PartialFill extends OrderStatus()

  /** This order has been successfully executed and is complete. (Final state.) */
  case object Filled extends OrderStatus()

  /** We canceled this order without any fulfillment. (Final state.) */
  case object Canceled extends OrderStatus()

  /** We canceled this order after being partially filled. (Final state.) */
  case object CanceledPartialFill extends OrderStatus()

  /** This order was rejected by the market. (Final state.) */
  case object Invalid extends OrderStatus()

  /** This order was submitted but we have since lost the ability to track its execution. */
  case object Unknown extends OrderStatus()
}
