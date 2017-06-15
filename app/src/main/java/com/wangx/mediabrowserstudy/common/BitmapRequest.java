package com.wangx.mediabrowserstudy.common;

import java.io.InputStream;
import java.util.Map;

/**
 * @Author: xujie.wang
 * @Email: xujie.wang@17zuoye.com
 * @Date: 2017/5/25
 * @Project: MediaBrowserStudy
 */

public class BitmapRequest extends BaseRequest<InputStream> {
    private String mUrl;

    public BitmapRequest(String url){

        mUrl = url;
    }

    @Override
    public Map<String, String> buildParams() {
        return null;
    }

    @Override
    public String getUrl() {
        return mUrl;
    }

    @Override
    public InputStream parseResult(String json) {

        return null;
    }

}
