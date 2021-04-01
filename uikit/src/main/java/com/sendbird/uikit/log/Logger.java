package com.sendbird.uikit.log;

import android.util.Log;

import com.sendbird.uikit.BuildConfig;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class Logger {
    private Logger() {
    }

    private static LoggerConfig loggerConfig = initLogConfig();
    private static final int LOG_SEGMENT_SIZE = 2000;
    private static int logLevel = LoggerConfig.DEV;

    public static void setLogLevel(int logLevel) {
        Logger.logLevel = logLevel;
        loggerConfig = initLogConfig();
    }

    private static LoggerConfig initLogConfig() {
        int printLogLevel = LoggerConfig.DEV;
        if (logLevel == LoggerConfig.DEV) {
            if (!BuildConfig.DEBUG) {
                printLogLevel = LoggerConfig.WARN;
            }
        } else {
            printLogLevel = logLevel;
        }

        LoggerConfig.Builder builder = new LoggerConfig.Builder();
        builder = builder.setDefaultTag(Tag.DEFAULT).setStackPrefix(Tag.DEFAULT.tag()).setPrintLoggerLevel(printLogLevel);

        Set<String> set = new HashSet<String>();
        set.add(Logger.class.getName());
        builder.setIgnoreSet(set);
        return builder.build();
    }

    private static int printLog(Tag tag, int logLevel, String msg) {
        if (msg == null) {
            return 0;
        }
        boolean withStack = true;
        String message = loggerConfig.getMessage(withStack, msg);
        if (!loggerConfig.isPrintLoggable(logLevel)) {
            return 0;
        }
        if (message == null) {
            message = loggerConfig.getMessage(withStack, msg);
        }
        if (message == null) {
            return 0;
        }

        String tagMsg = tag.tag();
        int messageLen = message.length();
        int curIdx = 0, depth = 0;

        int totalPrintLen = 0;
        while (curIdx < messageLen) {
            int remainLen = messageLen - curIdx;
            if (remainLen > LOG_SEGMENT_SIZE) {
                remainLen = LOG_SEGMENT_SIZE;
            }

            totalPrintLen += printLogPartially(logLevel, tagMsg, message.substring(curIdx, curIdx + remainLen), depth++);
            curIdx += remainLen;
        }
        return totalPrintLen;
    }

    private static int printLogPartially(int logLevel, String tagMsg, String msg, int depth) {
        int msgLen = msg.length();

        int writtenLen = 0;
        String prefix = "";
        if (depth > 0) {
            prefix = String.format(Locale.US, "Cont(%d) ", depth);
        }

        final String printMsg;
        if (msgLen > LOG_SEGMENT_SIZE) {
            printMsg = msg.substring(0, LOG_SEGMENT_SIZE);
        } else {
            printMsg = msg;
        }

        switch (logLevel) {
            case LoggerConfig.DEV:
            case LoggerConfig.DEBUG:
                writtenLen = Log.d(tagMsg, prefix + printMsg);
                break;

            case LoggerConfig.VERBOSE:
                writtenLen = Log.v(tagMsg, prefix + printMsg);
                break;

            case LoggerConfig.INFO:
                writtenLen = Log.i(tagMsg, prefix + printMsg);
                break;

            case LoggerConfig.WARN:
                writtenLen = Log.w(tagMsg, prefix + printMsg);
                break;

            case LoggerConfig.ERROR:
                writtenLen = Log.e(tagMsg, prefix + printMsg);
                break;
        }

        return writtenLen;
    }

    public static String getCallerTraceInfo(@SuppressWarnings("rawtypes")
    Class klass) {
        if (!loggerConfig.isPrintLoggable(LoggerConfig.DEBUG)) {
            return "unknown caller";
        }
        StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
        String threadName = Thread.currentThread().getName();
        StackTraceElement stack = null;
        String className = null;
        String callerClassName = klass.getName();
        boolean found = false;
        for (StackTraceElement stack1 : stacks) {
            className = stack1.getClassName();
            if (className.startsWith(callerClassName)) {
                found = true;
            } else if (found) {
                stack = stack1;
                break;
            }
        }

        if (stack == null) {
            return "";
        }

        String klassName = stack.getClassName();
        String method = stack.getMethodName();
        int line = stack.getLineNumber();
        return String.format(Locale.US, "{%s}-[%s.%s():%d]", threadName, klassName, method, line);
    }

    private static String getStackTraceString(Throwable tr) {
        if (tr == null) return "";
        return Log.getStackTraceString(tr);
    }

    public static int vt(Tag tag, Throwable tr) {
        return vt(tag, getStackTraceString(tr));
    }

    public static int vt(Tag tag, String msg, Throwable tr) {
        return vt(tag, "%s\n%s", msg, getStackTraceString(tr));
    }

    private static int vt(Tag tag, String format, Object... args) {
        if (loggerConfig.isPrintLoggable(LoggerConfig.VERBOSE)) {
            String message = null;
            if (args != null && args.length > 0) {
                message = String.format(format, args);
            } else {
                message = format;
            }
            return printLog(tag, LoggerConfig.VERBOSE, message);
        } else {
            return 0;
        }
    }

    public static int vt(Tag tag, String msg) {
        return printLog(tag, LoggerConfig.VERBOSE, msg);
    }

    // verbose with default tag
    public static int v(String format, Object... args) {
        return vt(loggerConfig.getDefaultTag(), format, args);
    }

    public static int v(Throwable tr) {
        return vt(loggerConfig.getDefaultTag(), tr);
    }

    public static int v(String msg, Throwable tr) {
        return vt(loggerConfig.getDefaultTag(), msg, tr);
    }

    public static int dt(Tag tag, Throwable tr) {
        return dt(tag, getStackTraceString(tr));
    }

    public static int dt(Tag tag, String msg, Throwable tr) {
        return dt(tag, "%s\n%s", msg, getStackTraceString(tr));
    }

    private static int dt(Tag tag, String format, Object... args) {
        if (loggerConfig.isPrintLoggable(LoggerConfig.DEBUG)) {
            String message = null;
            if (args != null && args.length > 0) {
                message = String.format(format, args);
            } else {
                message = format;
            }
            return printLog(tag, LoggerConfig.DEBUG, message);
        } else {
            return 0;
        }
    }

    public static int dt(Tag tag, String msg) {
        return printLog(tag, LoggerConfig.DEBUG, msg);
    }

    public static int d(String format, Object... args) {
        return dt(loggerConfig.getDefaultTag(), format, args);
    }

    public static int d(String msg) {
        return dt(loggerConfig.getDefaultTag(), msg);
    }

    public static int d(Throwable tr) {
        return dt(loggerConfig.getDefaultTag(), tr);
    }

    public static int d(String msg, Throwable tr) {
        return dt(loggerConfig.getDefaultTag(), msg, tr);
    }

    public static int it(Tag tag, Throwable tr) {
        return it(tag, getStackTraceString(tr));
    }

    public static int it(Tag tag, String msg, Throwable tr) {
        return it(tag, "%s\n%s", msg, getStackTraceString(tr));
    }

    private static int it(Tag tag, String format, Object... args) {
        if (loggerConfig.isPrintLoggable(LoggerConfig.INFO)) {
            String message = null;
            if (args != null && args.length > 0) {
                message = String.format(format, args);
            } else {
                message = format;
            }
            return printLog(tag, LoggerConfig.INFO, message);
        } else {
            return 0;
        }
    }

    public static int it(Tag tag, String msg) {
        return printLog(tag, LoggerConfig.INFO, msg);
    }

    public static int i(String format, Object... args) {
        return it(loggerConfig.getDefaultTag(), format, args);
    }

    public static int i(Throwable tr) {
        return it(loggerConfig.getDefaultTag(), tr);
    }

    public static int i(String msg, Throwable tr) {
        return it(loggerConfig.getDefaultTag(), msg, tr);
    }

    public static int wt(Tag tag, Throwable tr) {
        return wt(tag, getStackTraceString(tr));
    }

    public static int wt(Tag tag, String msg, Throwable tr) {
        return wt(tag, "%s\n%s", msg, getStackTraceString(tr));
    }

    private static int wt(Tag tag, String format, Object... args) {
        if (loggerConfig.isPrintLoggable(LoggerConfig.WARN)) {
            String message = String.format(format, args);
            return printLog(tag, LoggerConfig.WARN, message);
        } else {
            return 0;
        }
    }

    public static int wt(Tag tag, String msg) {
        return printLog(tag, LoggerConfig.WARN, msg);
    }

    public static int w(String format, Object... args) {
        return wt(loggerConfig.getDefaultTag(), format, args);
    }

    public static int w(String msg) {
        return wt(loggerConfig.getDefaultTag(), msg);
    }

    public static int w(Throwable tr) {
        return wt(loggerConfig.getDefaultTag(), tr);
    }

    public static int w(String msg, Throwable tr) {
        return wt(loggerConfig.getDefaultTag(), msg, tr);
    }

    public static int et(Tag tag, Throwable tr) {
        return et(tag, getStackTraceString(tr));
    }

    public static int et(Tag tag, String msg, Throwable tr) {
        return et(tag, "%s\n%s", msg, getStackTraceString(tr));
    }

    private static int et(Tag tag, String format, Object... args) {
        if (loggerConfig.isPrintLoggable(LoggerConfig.ERROR)) {
            String message = null;
            if (args != null && args.length > 0) {
                message = String.format(format, args);
            } else {
                message = format;
            }
            return printLog(tag, LoggerConfig.ERROR, message);
        } else {
            return 0;
        }
    }

    public static int et(Tag tag, String msg) {
        return printLog(tag, LoggerConfig.ERROR, msg);
    }

    public static int e(String format, Object... args) {
        return et(loggerConfig.getDefaultTag(), format, args);
    }

    public static int e(String msg) {
        return et(loggerConfig.getDefaultTag(), msg);
    }

    public static int e(Throwable tr) {
        return et(loggerConfig.getDefaultTag(), tr);
    }

    public static int e(String msg, Throwable tr) {
        return et(loggerConfig.getDefaultTag(), msg, tr);
    }

    public static int devt(Tag tag, Throwable tr) {
        return devt(tag, getStackTraceString(tr));
    }

    public static int devt(Tag tag, String msg, Throwable tr) {
        return devt(tag, "%s\n%s", msg, getStackTraceString(tr));
    }

    private static int devt(Tag tag, String format, Object... args) {
        if (loggerConfig.isPrintLoggable(LoggerConfig.DEV)) {
            String message = null;
            if (args != null && args.length > 0) {
                message = String.format(format, args);
            } else {
                message = format;
            }
            return printLog(tag, LoggerConfig.DEV, message);
        } else {
            return 0;
        }
    }

    public static int devt(Tag tag, String msg) {
        return printLog(tag, LoggerConfig.DEV, msg);
    }

    public static int dev(String format, Object... args) {
        return devt(loggerConfig.getDefaultTag(), format, args);
    }

    public static int dev(String msg) {
        return devt(loggerConfig.getDefaultTag(), msg);
    }

    public static int dev(Throwable tr) {
        return devt(loggerConfig.getDefaultTag(), tr);
    }

    public static int dev(String msg, Throwable tr) {
        return devt(loggerConfig.getDefaultTag(), msg, tr);
    }
}
