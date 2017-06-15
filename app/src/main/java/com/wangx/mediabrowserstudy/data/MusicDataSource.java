package com.wangx.mediabrowserstudy.data;

import com.wangx.mediabrowserstudy.common.MusicListRequest;
import com.wangx.mediabrowserstudy.common.OkHttpUtils;
import com.wangx.mediabrowserstudy.pool.TaskController;
import com.wangx.mediabrowserstudy.pool.WorkerFactory;
import com.wangx.mediabrowserstudy.pool.WorkerTask;

/**
 * @Author: xujie.wang
 * @Email: xujie.wang@17zuoye.com
 * @Date: 2017/6/15
 * @Project: MediaBrowserStudy
 */

public class MusicDataSource {
    public  TaskController request(WorkerFactory.CallBack<String> callBack){
        return  WorkerFactory.getInstance().submit(new WorkerTask<String>(callBack) {
            @Override
            public String doInBackground() {
                return new OkHttpUtils().request(new MusicListRequest());
            }
        });
    }
}
