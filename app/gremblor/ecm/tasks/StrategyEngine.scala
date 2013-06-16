
package gremblor.ecm.tasks

/**
 * Engine that receives external inputs from data sources and feeds them into all
 * running strategies. Each Strategy has an independent purse attached to it that
 * it can trade from. Orders issued by Strategy instances are executed by the
 * TradingEngine, which is a parameter of this one.
 */
class StrategyEngine(private val mTradingEngine: TradingEngine) {
  // TODO: Write this to accept inputs from the pricing feeds.
}
