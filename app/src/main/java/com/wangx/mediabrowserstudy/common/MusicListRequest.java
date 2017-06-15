package com.wangx.mediabrowserstudy.common;

import com.wangx.mediabrowserstudy.data.MusicProvider;

import java.util.Map;

/**
 * @Author: xujie.wang
 * @Email: xujie.wang@17zuoye.com
 * @Date: 2017/5/24
 * @Project: MediaBrowserStudy
 */

public class MusicListRequest extends SimpleRequest<String> {

    @Override
    public String getUrl() {
        return MusicProvider.CATELOG_URL;
    }

}
