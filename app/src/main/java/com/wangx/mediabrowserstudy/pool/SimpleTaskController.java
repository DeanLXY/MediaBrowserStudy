package com.wangx.mediabrowserstudy.pool;

import java.util.concurrent.Future;

/**
 * @Author: xujie.wang
 * @Email: xujie.wang@17zuoye.com
 * @Date: 2017/6/15
 * @Project: MediaBrowserStudy
 */

public class SimpleTaskController implements TaskController {
    private Future<?> mFuture;

    public SimpleTaskController(Future<?> future) {
        mFuture = future;
    }

    @Override
    public void cancel() {
        mFuture.cancel(true);
    }

    @Override
    public boolean isCancel() {
        return mFuture.isCancelled();
    }

    @Override
    public boolean done() {
        return mFuture.isDone();
    }

    @Override
    public void interrupt() {

    }

}
