package org.ei.opensrp.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by coder on 4/18/17.
 */
public class BackGroundThreadExecutor {
    private static final String TAG=BackGroundThreadExecutor.class.getCanonicalName();
    static int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
    static int KEEP_ALIVE_TIME = 1;
    static TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
    static BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<Runnable>();
    public static ExecutorService executorService = new ThreadPoolExecutor(NUMBER_OF_CORES,
            NUMBER_OF_CORES*2,
            KEEP_ALIVE_TIME,
            KEEP_ALIVE_TIME_UNIT,
            taskQueue,
            new BackgroundThreadFactory());


    private static class BackgroundThreadFactory implements ThreadFactory {
        private static int sTag = 1;

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable);
            thread.setName("CustomThread" + sTag);
            thread.setPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

            // A exception handler is created to log the exception from threads
            thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread thread, Throwable ex) {
                    android.util.Log.e(TAG, thread.getName() + " encountered an error: " + ex.getMessage());
                }
            });
            return thread;
        }
    }
}
