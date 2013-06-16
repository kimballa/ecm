
package gremblor.ecm.tasks

import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import gremblor.ecm.util.ExceptionUtils._

/**
 * Base class to assist in the execution of tasks via a self-contained
 * ExecutorService.
 *
 * <p>Subclasses should override <tt>runnableInstance</tt> to provide
 * a new Runnable to submit to the Executor loop. The Runnable should
 * check <tt>isShutdown</tt> at convenient intervals to self-terminate
 * if it runs in an internal loop.
 *
 * <p>The ExecutorService used will be single-threaded. You can override this by
 * overriding <tt>getExecutorService</tt>.</p>
 *
 * <p>If isAutoRestart() returns true, the Runnable exits due to an uncaught Throwable, a new
 * instance will be created, and the task will be restarted. This is the default behavior;
 * you may override isAutoRestart() to return false if you do not want this behavior.</p>
 *
 * <p>If you override shutdown() to include any service-specific shutdown
 * functions (like adding an interrupt() call), you must call super.shutdown()
 * first. ExecutorEngine.shutdown() will synchronize on the current object;
 * you should do so as well, like so:</p>
 *
 * <div><code><tt>
 * override def shutdown(): Unit = {
 *   this.synchronized {
 *     super.shutdown() // Set isShutdown to true.
 *     mMyServiceObject.notifyOfImpendingShutdown() // Interrupt my internal thread loop.
 *   }
 * </tt></code></div>
 */
abstract class ExecutorEngine {
  private val LOG: Logger = LoggerFactory.getLogger(getClass().getName() + ".executorEngine")

  /** Specify the ExecutorService to be used. By default, it is single-threaded. */
  protected def getExecutorService: ExecutorService = {
    LOG.debug("Creating SingleThreadExecutor")
    Executors.newSingleThreadExecutor()
  }

  /** The ExecutorService instance actually used. */
  private val mExecutorService: ExecutorService = getExecutorService

  // Start a background thread to listen for market data events.
  mExecutorService.submit(new AutoRestartRunnable)

  /**
   * Return true if Runnables should be recreated and re-executed if they exit abnormally.
   * You may override this method to return false. It will only be called once.
   */
  protected def isAutoRestart() = { true }

  /** User-supplied method to instantiate a Runnable to submit to the ExecutorService. */
  def runnableInstance: Runnable

  /** isRunning is true as long as the thread should be active. */
  private var mIsRunning: Boolean = true

  /**
   * Return true if the user has requested that we shutdown. Runnables should poll
   * this value and exit promptly upon notification of shutdown.
   */
  protected def isShutdown(): Boolean = { !mIsRunning }

  /**
   * Tell any thread loops in the Runnable to shut down.
   *
   * <p>See note in class scaladoc regarding overriding this method.</p>
   */
  def shutdown(): Unit = {
    this.synchronized {
      mIsRunning = false
      mExecutorService.shutdown()
    }
  }

  /**
   * Wrap the user's Runnable in one that retries if an exception gets loose.
   */
  private class AutoRestartRunnable extends Runnable {
    // Memoize this value once.
    private val mAutoRestart = isAutoRestart()

    override def run(): Unit = {
      // Try to run the user's runnable exactly once.
      // If it succeeds, we just return Unit.
      // If it throws anything, we log what's thrown.
      // If mAutoRestart was initialized to true, we then go from the top, resubmitting the
      // user's run() call after initializing a new runnableInstance
      do {
        try {
          val theRunnable = runnableInstance // get a Runnable from the user-supplied method.
          LOG.debug("Launching Runnable instance: " + theRunnable.toString())
          theRunnable.run()
          return
        } catch {
          case throwable: Throwable => {
            LOG.error("Service threw an uncaught exception: " + throwable.getMessage)
            LOG.error(stackTraceToString(throwable))
          }
        }
      } while (mAutoRestart)
    }
  }
}
