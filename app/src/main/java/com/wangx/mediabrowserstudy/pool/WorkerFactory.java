package com.wangx.mediabrowserstudy.pool;


import android.os.Handler;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: xujie.wang
 * @Email: xujie.wang@17zuoye.com
 * @Date: 2017/5/24
 * @Project: MediaBrowserStudy
 */

public class WorkerFactory {
    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "WorkerFactory #" + mCount.getAndIncrement());
        }
    };
    private static final BlockingQueue<Runnable> sPoolWorkQueue =
            new LinkedBlockingQueue<Runnable>(128);
    private static volatile WorkerFactory instance = null;
    private final ThreadPoolExecutor mPoolExecutor;

    //统一处理
    private Handler mHandler = new Handler();

    private WorkerFactory() {
        mPoolExecutor = new ThreadPoolExecutor(
                WorkerFactoryConfiguration.CORE_POOL_SIZE,
                WorkerFactoryConfiguration.MAXIMUM_POOL_SIZE,
                WorkerFactoryConfiguration.KEEP_ALIVE_SECONDS, TimeUnit.SECONDS,
                sPoolWorkQueue,
                sThreadFactory);
        mPoolExecutor.allowsCoreThreadTimeOut();
    }

    public static WorkerFactory getInstance() {
        if (instance == null) {
            synchronized (WorkerFactory.class) {
                if (instance == null) {
                    instance = new WorkerFactory();
                }
            }
        }
        return instance;
    }


    public <Result> TaskController submit(final WorkerTask<Result> workerTask) {
         return submit(mPoolExecutor, workerTask);
    }


    private  <Result> TaskController submit(ExecutorService executor , final WorkerTask<Result> workerTask) {
        Callable<Result> callable = new Callable<Result>() {
            @Override
            public Result call() throws Exception {
                return workerTask.doInBackground();
            }
        };
        return new SimpleTaskController(executor.submit(new FutureTask<Result>(callable) {
            @Override
            protected void done() {
                try {
                    workerTask.getCallBack().onPostExecute(get());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }));
    }
    public interface CallBack<R> {
        void onPostExecute(R r);
    }

}
