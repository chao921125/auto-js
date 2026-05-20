package net.cc.stardust.runtime.api;

import android.os.Build;

import net.cc.stardust.yolo.ModelInitParams;
import net.cc.stardust.yolo.YoloInstance;
import net.cc.stardust.yolo.ncnn.NcnnInitParams;
import net.cc.stardust.yolo.ncnn.NcnnYoloInstanceFactory;
import net.cc.stardust.yolo.onnx.OnnxYoloInstanceFactory;

import java.util.List;

import androidx.annotation.RequiresApi;

/**
 * @author TonyJiangWJ
 * @since 2024/6/1
 */
@RequiresApi(api = Build.VERSION_CODES.N)
public class Yolo {
    private static final String TAG = "Yolo";

    private final NcnnYoloInstanceFactory ncnnFactory = new NcnnYoloInstanceFactory();
    private final OnnxYoloInstanceFactory onnxFactory = new OnnxYoloInstanceFactory();

    public YoloInstance createOnnx(String modelPath, List<String> labels, Integer imageSize) {
        ModelInitParams params = new ModelInitParams();
        params.setModelPath(modelPath);
        params.setLabels(labels);
        params.setImageSize(imageSize);
        return onnxFactory.createInstance(params);
    }


    public YoloInstance createNcnn(String paramPath, String binPath, List<String> labels, Integer imageSize, boolean useGpu) {
        NcnnInitParams params = new NcnnInitParams();
        params.setParamPath(paramPath);
        params.setBinPath(binPath);
        params.setLabels(labels);
        params.setImageSize(imageSize);
        params.setUseGpu(useGpu);
        return ncnnFactory.createInstance(params);
    }

}
