package net.cc.stardust.yolo.onnx;


import android.os.Build;

import net.cc.stardust.yolo.BaseYoloInstance;
import net.cc.stardust.yolo.ModelInitParams;
import net.cc.stardust.yolo.YoloInstance;
import net.cc.stardust.yolo.YoloInstanceFactory;

import androidx.annotation.RequiresApi;

/**
 * OnnxYoloV8实例创建工厂
 *
 * @author TonyJiangWJ
 * @since 2025/1/5
 */
public class OnnxYoloInstanceFactory implements YoloInstanceFactory<ModelInitParams> {
    /**
     * 创建YoloInstance实例
     *
     * @param modelInitParams 初始化参数
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public YoloInstance createInstance(ModelInitParams modelInitParams) {
        OnnxYoloV8Predictor predictor = new OnnxYoloV8Predictor(modelInitParams.getModelPath());
        predictor.setLabels(modelInitParams.getLabels());
        predictor.setShapeSize(modelInitParams.getImageSize(), modelInitParams.getImageSize());
        return new BaseYoloInstance(predictor);
    }
}
