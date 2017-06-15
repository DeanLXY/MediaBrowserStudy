package com.wangx.mediabrowserstudy.common;

import android.content.Context;

import com.bumptech.glide.Glide;

/**
 * @Author: xujie.wang
 * @Email: xujie.wang@17zuoye.com
 * @Date: 2017/5/25
 * @Project: MediaBrowserStudy
 */

public class GlideApp {
    public void downloadBitmap(Context context, String url){
        Glide.with(context)
                .download(url);
    }
}
