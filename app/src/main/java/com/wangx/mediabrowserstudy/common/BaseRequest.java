package com.wangx.mediabrowserstudy.common;

import com.wangx.mediabrowserstudy.pool.WorkerFactory;

import java.util.Map;

/**
 * @Author: xujie.wang
 * @Email: xujie.wang@17zuoye.com
 * @Date: 2017/5/24
 * @Project: MediaBrowserStudy
 */

public abstract class BaseRequest<Result> {
    public abstract Map<String, String> buildParams();

    public abstract String getUrl();

    public abstract Result parseResult(String json);

}
