package com.bertvanbrakel.android.lang;

import android.util.Log;

public final class Logger {

    private final LogAdapter adapter;
    private final String logName;
    private static final boolean inNormalJdk;

    static {
        inNormalJdk = !System.getProperty("java.vm.name").toLowerCase().contains("dalvik");
    }

    public Logger(final Class<?> klass) {
        this("athena." + klass.getSimpleName());
    }

    public Logger(final String logName) {
        this.logName = trimToMaxLength(logName);
        if (inNormalJdk) {
            //possibly using a normal JVM to run tests in
            adapter = new ConsoleLogAdapter(ConsoleLogAdapter.LEVEL.DEBUG,this.logName);
        } else {
            adapter = new AndroidLogAdapter(this.logName);
        }
    }

    /**
     * Android limits on log name lengths means we have to trim the user supplied ones
     *
     * @param logName
     * @return
     */
    private static String trimToMaxLength(final String logName) {
        //limited to 23 chars
        final int start = logName.length() - 23;
        if (start < 0) {
            return logName;
        } else {
            // try not to cut off package names in the middle. Rather use the
            // next package name up if possible
            final String shortName = logName.substring(start, logName.length());
            if (start == 0 || logName.charAt(start - 1) == '.') {
                return shortName;
            } else {
                final int lastDot = shortName.indexOf('.');
                if (lastDot == -1) {
                    return shortName;
                } else {
                    return shortName.substring(lastDot + 1, shortName.length());
                }
            }
        }
    }

    public void trace(final String msg) {
        adapter.trace(msg);
    }

    public void trace(final String msg, final Throwable t) {
        adapter.trace(msg, t);
    }

    public void debug(final String msg) {
        adapter.debug(msg);
    }

    public void debug(final String msg, final Throwable t) {
        adapter.debug(msg, t);
    }

    public void info(final String msg) {
        adapter.info(msg);
    }

    public void info(final String msg, final Throwable t) {
        adapter.info(msg, t);
    }

    public void warn(final String msg) {
        adapter.warn(msg);
    }

    public void warn(final String msg, final Throwable t) {
        adapter.warn(msg, t);
    }

    public void error(final String msg) {
        adapter.error(msg);
    }

    public void error(final String msg, final Throwable t) {
        adapter.error(msg, t);
    }

    public void fatal(final String msg) {
        adapter.fatal(msg);
    }

    public void fatal(final String msg, final Throwable t) {
        adapter.fatal(msg, t);
    }

    public boolean isTraceEnabled() {
        return adapter.isTraceEnabled();
    }

    public boolean isDebugEnabled() {
        return adapter.isDebugEnabled();
    }

    public boolean isInfoEnabled() {
        return adapter.isInfoEnabled();
    }

    public boolean isWarnEnabled() {
        return adapter.isWarnEnabled();
    }

    public boolean isErrorEnabled() {
        return adapter.isErrorEnabled();
    }

    public boolean isFatalEnabled() {
        return adapter.isFatalEnabled();
    }

    public String getLogName() {
        return logName;
    }

    private static interface LogAdapter {
        public void trace(final String msg);

        public void trace(final String msg, final Throwable t);

        public void debug(final String msg);

        public void debug(final String msg, final Throwable t);

        public void info(final String msg);

        public void info(final String msg, final Throwable t);

        public void warn(final String msg);

        public void warn(final String msg, final Throwable t);

        public void error(final String msg);

        public void error(final String msg, final Throwable t);

        public void fatal(final String msg);

        public void fatal(final String msg, final Throwable t);

        public boolean isTraceEnabled();

        public boolean isDebugEnabled();

        public boolean isInfoEnabled();

        public boolean isWarnEnabled();

        public boolean isErrorEnabled();

        public boolean isFatalEnabled();
    }

    private static class AndroidLogAdapter implements LogAdapter {
        private final String logName;

        public AndroidLogAdapter(final String logName) {
            this.logName = logName;
        }

        @Override
        public void trace(final String msg) {
            Log.v(logName, msg);
        }

        @Override
        public void trace(final String msg, final Throwable t) {
            Log.v(logName, msg, t);
        }

        @Override
        public void debug(final String msg) {
            Log.d(logName, msg);
        }

        @Override
        public void debug(final String msg, final Throwable t) {
            Log.d(logName, msg, t);
        }

        @Override
        public void info(final String msg) {
            Log.i(logName, msg);
        }

        @Override
        public void info(final String msg, final Throwable t) {
            Log.i(logName, msg, t);
        }

        @Override
        public void warn(final String msg) {
            Log.w(logName, msg);
        }

        @Override
        public void warn(final String msg, final Throwable t) {
            Log.w(logName, msg, t);
        }

        @Override
        public void error(final String msg) {
            Log.e(logName, msg);
        }

        @Override
        public void error(final String msg, final Throwable t) {
            Log.e(logName, msg, t);
        }

        @Override
        public void fatal(final String msg) {
            Log.wtf(logName, msg);
        }

        @Override
        public void fatal(final String msg, final Throwable t) {
            Log.wtf(logName, msg, t);
        }

        @Override
        public boolean isTraceEnabled() {
            return Log.isLoggable(logName, Log.VERBOSE);
        }

        @Override
        public boolean isDebugEnabled() {
            return Log.isLoggable(logName, Log.DEBUG);
        }

        @Override
        public boolean isInfoEnabled() {
            return Log.isLoggable(logName, Log.INFO);
        }

        @Override
        public boolean isWarnEnabled() {
            return Log.isLoggable(logName, Log.WARN);
        }

        @Override
        public boolean isErrorEnabled() {
            return Log.isLoggable(logName, Log.ERROR);
        }

        @Override
        public boolean isFatalEnabled() {
            return true;
        }
    }

    private static class ConsoleLogAdapter implements LogAdapter {

        static enum LEVEL {
            TRACE(0), DEBUG(1), INFO(2), WARN(3), ERROR(5), FATAL(6);
            final int level;

            LEVEL(final int level) {
                this.level = level;
            }

            public boolean isEnabledForLevel(final LEVEL l) {
                return l.level >= this.level;
            }
        }

        private final String logName;
        private final LEVEL level;

        public ConsoleLogAdapter(final LEVEL level, final String logName) {
            this.logName = logName;
            this.level = level;
        }

        @Override
        public void trace(final String msg) {
            log(LEVEL.TRACE, msg, null);
        }

        @Override
        public void trace(final String msg, final Throwable t) {
            log(LEVEL.TRACE, msg, t);
        }

        @Override
        public void debug(final String msg) {
            log(LEVEL.DEBUG, msg, null);
        }

        @Override
        public void debug(final String msg, final Throwable t) {
            log(LEVEL.DEBUG, msg, t);
        }

        @Override
        public void info(final String msg) {
            log(LEVEL.INFO, msg, null);
        }

        @Override
        public void info(final String msg, final Throwable t) {
            log(LEVEL.INFO, msg, t);
        }

        @Override
        public void warn(final String msg) {
            log(LEVEL.WARN, msg, null);
        }

        @Override
        public void warn(final String msg, final Throwable t) {
            log(LEVEL.WARN, msg, t);
        }

        @Override
        public void error(final String msg) {
            log(LEVEL.ERROR, msg, null);
        }

        @Override
        public void error(final String msg, final Throwable t) {
            log(LEVEL.ERROR, msg, t);
        }

        @Override
        public void fatal(final String msg) {
            log(LEVEL.FATAL, msg, null);
        }

        @Override
        public void fatal(final String msg, final Throwable t) {
            log(LEVEL.FATAL, msg, t);
        }

        @Override
        public boolean isTraceEnabled() {
            return isEnabled(LEVEL.TRACE);
        }

        @Override
        public boolean isDebugEnabled() {
            return isEnabled(LEVEL.DEBUG);
        }

        @Override
        public boolean isInfoEnabled() {
            return isEnabled(LEVEL.INFO);
        }

        @Override
        public boolean isWarnEnabled() {
            return isEnabled(LEVEL.WARN);
        }

        @Override
        public boolean isErrorEnabled() {
            return isEnabled(LEVEL.ERROR);
        }

        @Override
        public boolean isFatalEnabled() {
            return isEnabled(LEVEL.FATAL);
        }

        private boolean isEnabled(final LEVEL l) {
            return this.level.isEnabledForLevel(l);
        }

        private void log(final LEVEL l, final String msg, final Throwable t) {
            if (this.level.isEnabledForLevel(l)) {
                System.out.println("[" + l.name() + "] " + logName + " " + msg);
                if (t != null) {
                    t.printStackTrace(System.out);
                }
            }
        }
    }

}
