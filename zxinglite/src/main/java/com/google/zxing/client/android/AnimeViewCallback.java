package com.google.zxing.client.android;

import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.camera.CameraManager;

/**
 * 获取点状
 * Created by weixing9920@163.com on 2017/10/15.
 */

public interface AnimeViewCallback {

    void addPossibleResultPoint(ResultPoint point);

    void setCameraManager(CameraManager cameraManager);

    void drawViewfinder();
}
