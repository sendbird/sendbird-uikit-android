package com.sendbird.uikit.log;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

class LoggerConfig {

    private int printLoggerLevel;

    private Tag defaultTag;

    private String stackPrefix;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US);

    private Set<String> ignoreStackSet;

    static final int DEV = 1;
    static final int VERBOSE = Log.VERBOSE;
    static final int DEBUG = Log.DEBUG;
    static final int INFO = Log.INFO;
    static final int WARN = Log.WARN;
    static final int ERROR = Log.ERROR;
    static final int ASSERT = Log.ASSERT;

    private String getTraceInfo() {
        StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
        return getTraceInfo(stacks);
    }

    private String getTraceInfo(StackTraceElement[] stacks) {
        StackTraceElement stack = null;
        String loggerName = this.getClass().getCanonicalName();
        String className;
        boolean startTracking = false;
        for (StackTraceElement stack1 : stacks) {
            className = stack1.getClassName();
            if (className.equalsIgnoreCase(loggerName)) {
                startTracking = true;
            }
            if (!startTracking) {
                continue;
            }

            if (className.startsWith(loggerName) || ignoreStackSet.contains(className)) {
                continue;
            }

            stack = stack1;
            break;
        }

        if (stack == null) {
            return null;
        }

        String[] klass = stack.getClassName().split("\\.");
        String method = stack.getMethodName();
        int line = stack.getLineNumber();
        return String.format(Locale.US, "[%s %s:%s():%d]", dateFormat.format(System.currentTimeMillis()), klass[klass.length - 1], method, line);
    }

    Tag getDefaultTag() {
        return defaultTag;
    }

    private String getMessageWithTrace(String message) {
        String traceInfo = getTraceInfo();
        return String.format("%s %s", traceInfo == null ? "" : traceInfo, message);
    }

    boolean isPrintLoggable(int level) {
        return (level >= printLoggerLevel);
    }

    static class Builder {

        private int printLoggerLevel = DEV;

        private Tag defaultTag = Tag.DEFAULT;

        private String stackPrefix;

        private Set<String> ignoreStackSet = new HashSet<>();

        Builder setDefaultTag(Tag tag) {
            defaultTag = tag;
            return this;
        }

        Builder setPrintLoggerLevel(int level) {
            printLoggerLevel = level;
            return this;
        }

        Builder setIgnoreSet(Set<String> set) {
            if (set == null) {
                return this;
            }
            ignoreStackSet = set;
            return this;
        }

        Builder setStackPrefix(String prefix) {
            stackPrefix = prefix;
            return this;
        }

        LoggerConfig build() {
            LoggerConfig loggerConfig = new LoggerConfig();
            loggerConfig.defaultTag = defaultTag;
            loggerConfig.printLoggerLevel = printLoggerLevel;
            loggerConfig.stackPrefix = stackPrefix;
            loggerConfig.ignoreStackSet = ignoreStackSet;
            return loggerConfig;
        }
    }


    String getMessage(boolean withStack, String msg) {
        return withStack ? getMessageWithTrace(msg) : msg;
    }
}