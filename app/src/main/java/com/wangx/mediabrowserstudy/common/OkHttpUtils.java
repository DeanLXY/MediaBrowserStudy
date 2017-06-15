package com.wangx.mediabrowserstudy.common;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * @Author: xujie.wang
 * @Email: xujie.wang@17zuoye.com
 * @Date: 2017/5/24
 * @Project: MediaBrowserStudy
 */

public class OkHttpUtils {
    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient();

    public <Result> Result request(BaseRequest<Result> request) {
        try {
            Request simpleRequest = new Request.Builder().url(request.getUrl()).build();
            String json = HTTP_CLIENT.newCall(simpleRequest).execute().body().string();
            Result result = request.parseResult(json);
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
