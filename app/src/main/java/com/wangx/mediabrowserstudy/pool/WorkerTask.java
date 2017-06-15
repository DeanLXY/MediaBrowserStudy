package com.wangx.mediabrowserstudy.pool;

/**
 * @Author: xujie.wang
 * @Email: xujie.wang@17zuoye.com
 * @Date: 2017/5/24
 * @Project: MediaBrowserStudy
 */

public  abstract class WorkerTask<V>   {
    private WorkerFactory.CallBack<V> mCallBack;

    public WorkerTask(WorkerFactory.CallBack<V>  mCallBack){

        this.mCallBack = mCallBack;
    }

    public WorkerFactory.CallBack getCallBack() {
        return mCallBack;
    }

    public abstract V doInBackground();
}
