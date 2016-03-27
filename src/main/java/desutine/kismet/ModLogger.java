package desutine.kismet;

import net.minecraftforge.fml.common.FMLLog;
import org.apache.logging.log4j.Level;

public class ModLogger {
  static org.apache.logging.log4j.Logger logger = FMLLog.getLogger();

  /**
   * A severe error that will prevent the application from continuing.
   */
  public static void fatal(Object message) {
    log(Level.FATAL, message);
  }

  public static void log(Level level, Object message) {
    logger.log(level, message.toString());
  }

  /**
   * A severe error that will prevent the application from continuing.
   */
  public static void fatal(Object message, Throwable ex) {
    log(Level.FATAL, message, ex);
  }

  private static void log(Level level, Object message, Throwable ex) {
    logger.log(level, message.toString(), ex);
  }

  /**
   * An error in the application, possibly recoverable.
   */
  public static void error(Object message, Throwable ex) {
    log(Level.ERROR, message, ex);
  }

  /**
   * An error in the application, possibly recoverable.
   */
  public static void error(Object message) {
    log(Level.ERROR, message);
  }

  /**
   * An event that might possible lead to an error.
   */
  public static void warning(Object message) {
    log(Level.WARN, message);
  }

  /**
   * An event for informational purposes.
   */
  public static void info(Object message) {
    log(Level.INFO, message);
  }

  /**
   * A general debugging event.
   */
  public static void debug(Object message) {
    log(Level.DEBUG, message);
  }

  /**
   * A fine-grained debug message, typically capturing the flow through the application.
   */
  public static void trace(Object message) {
    log(Level.TRACE, message);
  }
}
