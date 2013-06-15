
package gremblor.ecm.models

import org.joda.money.BigMoney
import org.joda.money.CurrencyUnit

/**
 * A set of resources available to a Strategy for trading.
 *
 * Orders cause a lock on the associated funds until complete.
 * Balances that are encumbered by orders should be held in reserve. Of course,
 * when submitting market orders, encumbrance may only be an estimate.
 *
 * We also track overage: if an order manages to exceed all the funds allotted
 * to the purse, we determine how much 'debt' the purse has to the total account.
 * This is true of every currency in the purse (USD, BTC..) When the purse is capable
 * of refunding the debt, it should do so (to ensure that "winnings" from borrowed funds
 * don't accumulate in the purse).
 */
class Purse(private val initialUsd: BigMoney, private val initialBtc: BigMoney)  {

  // Total amount of USD and BTC available in the purse.
  private var mUsd: BigMoney = initialUsd
  private var mBtc: BigMoney = initialBtc

  // Amount of USD we accidentally spent that didn't belong to this purse.
  private var mUsdDebt: BigMoney = BigMoney.zero(CurrencyUnit.of("USD"))

  // Amount of BTC we accidentally spent that didn't belong to this purse.
  private var mBtcDebt: BigMoney = BigMoney.zero(CurrencyUnit.of("BTC"))

  // Amount of USD we expect to trade away shortly.
  private var mUsdCommitted: BigMoney = BigMoney.zero(CurrencyUnit.of("USD"))

  // Amount of BTC we expect to trade away shortly.
  private var mBtcCommitted: BigMoney = BigMoney.zero(CurrencyUnit.of("BTC"))

  // Amount of USD we expect to receive from unfilled trades.
  private var mUsdExpected: BigMoney = BigMoney.zero(CurrencyUnit.of("USD"))

  // Amount of BTC we expect to receive from unfilled trades.
  private var mBtcExpected: BigMoney = BigMoney.zero(CurrencyUnit.of("BTC"))

  /**
   * Return the total balance of USD actually available in the purse.
   * This value may change as funds committed to pending trades are deducted,
   * or receipts expected from pending trades are deposited.
   */
  def getUsdBalance(): BigMoney = mUsd

  /**
   * Return the total balance of BTC actually available in the purse.
   * This value may change as funds committed to pending trades are deducted,
   * or receipts expected from pending trades are deposited.
   */
  def getBtcBalance(): BigMoney = mBtc

  /** Return amount of USD available to trade. */
  def getAvailableUsd(): BigMoney = { mUsd.minus(mUsdCommitted).minus(mUsdDebt) }

  /** Return amount of BTC available to trade. */
  def getAvailableBtc(): BigMoney = { mBtc.minus(mBtcCommitted).minus(mBtcDebt) }

  /**
   * Return the amount of USD expected to be in the purse after all outstanding
   * trades are executed. May differ from the actual settled amount.
   */
  def getExpectedUsd(): BigMoney = { mUsd.minus(mUsdCommitted).plus(mUsdExpected).minus(mUsdDebt) }

  def getExpectedBtc(): BigMoney = { mBtc.minus(mBtcCommitted).plus(mBtcExpected).minus(mBtcDebt) }

}
