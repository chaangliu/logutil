import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 日志工具，可以设置是否打印，是否保存到SD卡。
 * 2018-11-13
 */
public class LogUtil {
    /**
     * 默认的TAG
     */
    private static final String TAG = "LogUtil";
    /**
     * 系统下载文件夹
     */
    private static final String DOWNLOADS_PATH;
    /**
     * Log前缀
     */
    private static final String LOG_PREFIX;
    /**
     * Log保存的路径
     */
    private static final String LOG_DIR;
    /**
     * 是否打印log
     */
    private static boolean sDebugMode = false;
    /**
     * 是否保存log到disk
     */
    private static boolean sSaveToDisk = false;
    /**
     * 写入文件的等级，＞=level等级的log将被写入文件
     */
    public static int level = Log.WARN;

    static {
        DOWNLOADS_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
        LOG_PREFIX = setLogPrefix("");
        LOG_DIR = setLogPath("");
    }

    public static void v(String msg) {
        trace(Log.VERBOSE, TAG, msg);
    }

    public static void v(String tag, String msg) {
        trace(Log.VERBOSE, tag, msg);
    }

    public static void v(String tag, String msg, Throwable tr) {
        trace(Log.VERBOSE, tag, msg, tr);
    }

    public static void d(String msg) {
        trace(Log.DEBUG, TAG, msg);
    }

    public static void d(String tag, String msg) {
        trace(Log.DEBUG, tag, msg);
    }

    public static void d(String tag, String msg, Throwable tr) {
        trace(Log.DEBUG, tag, msg, tr);
    }

    public static void i(String msg) {
        trace(Log.INFO, TAG, msg);
    }

    public static void i(String tag, String msg) {
        trace(Log.INFO, tag, msg);
    }

    public static void i(String tag, String msg, Throwable tr) {
        trace(Log.INFO, tag, msg, tr);
    }

    public static void w(String msg) {
        trace(Log.WARN, TAG, msg);
    }

    public static void w(String tag, String msg) {
        trace(Log.WARN, tag, msg);
    }

    public static void w(String tag, String msg, Throwable tr) {
        trace(Log.WARN, tag, msg, tr);
    }

    public static void e(String msg) {
        trace(Log.ERROR, TAG, msg);
    }

    public static void e(String tag, String msg) {
        trace(Log.ERROR, tag, msg);
    }

    public static void e(String tag, String msg, Throwable tr) {
        trace(Log.ERROR, tag, msg, tr);
    }


    /**
     * Debug模式开关，控制是否打印日志。
     *
     * @param shouldPrintLogs 是否打印日志。true为打印。不设置的话默认不打印。
     */
    public static void setDebuggable(boolean shouldPrintLogs) {
        sDebugMode = shouldPrintLogs;
    }

    /**
     * 保存到Disk的开关。如果打开，默认>=WARN级别的日志会被保存。
     *
     * @param shouldSaveToDisk 是否保存到Disk
     */
    public static void setSaveLogToDisk(boolean shouldSaveToDisk) {
        sSaveToDisk = shouldSaveToDisk;
    }

    /**
     * 设置日志文件名前缀
     *
     * @param prefix 日志文件的前缀。(e.g. prefix-20121212.log)
     * @return 日志的前缀。
     */
    public static String setLogPrefix(final String prefix) {
        return TextUtils.isEmpty(prefix) ? "LogUtil-" : prefix + "-";
    }

    /**
     * 设置日志文件存放路径
     *
     * @param subPath 子路径("/Downloads/subPath")
     * @return 日志文件存放的路径。
     */
    public static String setLogPath(final String subPath) {
        return TextUtils.isEmpty(subPath) ? DOWNLOADS_PATH + "/logs" : subPath;
    }


    /**
     * 如果是debug模式，打印log。保存满足log等级的log到SD卡。
     *
     * @param type 日志类型
     * @param tag  TAG
     * @param msg  MSG
     */
    private static void trace(final int type, String tag, final String msg) {
        trace(type, tag, msg, null);
    }

    /**
     * 如果是debug模式，打印log。保存满足log等级的log到SD卡。
     *
     * @param type 日志类型
     * @param tag  TAG
     * @param msg  MSG
     * @param tr   Throwable
     */
    private static void trace(final int type, final String tag,
                              final String msg, final Throwable tr) {
        if (sDebugMode) {
            switch (type) {
                case Log.VERBOSE:
                    Log.v(tag, msg);
                    break;
                case Log.DEBUG:
                    Log.d(tag, msg);
                    break;
                case Log.INFO:
                    Log.i(tag, msg);
                    break;
                case Log.WARN:
                    Log.w(tag, msg);
                    break;
                case Log.ERROR:
                    Log.e(tag, msg);
                    break;
            }
        }
        if (sSaveToDisk && type >= level) {
            writeLog(type, msg + '\n' + Log.getStackTraceString(tr));
        }
    }

    /**
     * 写入log到SD卡。
     *
     * @param type 日志类型
     * @param msg  日志信息
     */
    private static void writeLog(int type, String msg) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return;
        }
        try {
            SparseArray<String> logMap = new SparseArray<>();
            logMap.put(Log.VERBOSE, " VERBOSE ");
            logMap.put(Log.DEBUG, " DEBUG ");
            logMap.put(Log.INFO, " INFO ");
            logMap.put(Log.WARN, " WARN ");
            logMap.put(Log.ERROR, " ERROR ");

            msg = "\r\n" + getDateFormat(DateFormatter.SS.getValue()) + logMap.get(type) + msg;
            String fileName = LOG_PREFIX + getDateFormat(DateFormatter.DD.getValue()) + ".log";
            recordLog(LOG_DIR, fileName, msg, true);
        } catch (Exception e) {
            LogUtil.e(LOG_PREFIX, e.getMessage());
        }
    }

    /**
     * 创建文件夹、文件，并判断写入文件的方式。
     *
     * @param logDir   文件路径
     * @param fileName 文件名
     * @param msg      Log内容
     * @param append   if <code>true</code>, then bytes will be written to the end of the file rather than the beginning
     */
    private static void recordLog(String logDir, String fileName, String msg, boolean append) {
        try {
            createDir(logDir);
            final File saveFile = new File(logDir + "/" + fileName);
            if (!append && saveFile.exists()) {
                saveFile.delete();
                saveFile.createNewFile();
                write(saveFile, msg, append);
            } else if (append && saveFile.exists()) {
                write(saveFile, msg, append);
            } else if (!saveFile.exists()) {
                saveFile.createNewFile();
                write(saveFile, msg, append);
            }
        } catch (IOException e) {
            recordLog(logDir, fileName, msg, append);
        }
    }

    private static String getDateFormat(String pattern) {
        final DateFormat format = new SimpleDateFormat(pattern, Locale.CHINA);
        return format.format(new Date());
    }

    private static void createDir(String dir) {
        final File file = new File(dir);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    /**
     * 写入Log到SDK卡。
     *
     * @param file   文件名
     * @param msg    Log信息
     * @param append if <code>true</code>, then bytes will be written to the end of the file rather than the beginning
     */
    private static void write(final File file, final String msg, final boolean append) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                final FileOutputStream fos;
                try {
                    fos = new FileOutputStream(file, append);
                    try {
                        fos.write(msg.getBytes());
                    } catch (IOException e) {
                        LogUtil.e(TAG, "write log failed", e);
                    } finally {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            LogUtil.d(TAG, "Exception closing stream: ", e);
                        }
                    }
                } catch (FileNotFoundException e) {
                    LogUtil.e(TAG, "write fail failed", e);
                }
                return null;
            }
        }.execute();
    }

    public enum DateFormatter {
        NORMAL("yyyy-MM-dd HH:mm"),
        DD("yyyy-MM-dd"),
        SS("yyyy-MM-dd HH:mm:ss");
        private String value;

        DateFormatter(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}

