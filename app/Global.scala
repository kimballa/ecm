// Copyright 2013 Gremblor Heavy Industries

import play.api._

import gremblor.ecm.tasks.TickerThread

/** Application-global settings and initialization. */
object Global extends GlobalSettings {

  private var mTickerThread: Option[TickerThread] = None

  /**
   * Startup function called as the app begins.
   * Initialize our background tasks, etc.
   */
  override def onStart(app: Application) {
    super.onStart(app)

    // Retrieve ticker information in background threads.
    val tickers = new TickerThread()
    mTickerThread = Some(new TickerThread)
    tickers.start()
  }

  override def onStop(app: Application) {
    if (mTickerThread.isDefined) {
      mTickerThread.get.shutdownTickers()
    }
    super.onStop(app)
  }
}
