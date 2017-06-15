package com.wangx.mediabrowserstudy.common;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: xujie.wang
 * @Email: xujie.wang@17zuoye.com
 * @Date: 2017/5/24
 * @Project: MediaBrowserStudy
 */

public abstract class SimpleRequest<Result> extends BaseRequest<String> {
    @Override
    public Map<String, String> buildParams() {
        return new HashMap<String, String>();
    }

    @Override
    public String parseResult(String json) {
        return json;
    }
}
