package com.wangx.mediabrowserstudy.pool;

/**
 * @Author: xujie.wang
 * @Email: xujie.wang@17zuoye.com
 * @Date: 2017/6/15
 * @Project: MediaBrowserStudy
 */

public interface TaskController {
    void cancel();

    boolean isCancel();

    boolean done();

    void interrupt();

}
