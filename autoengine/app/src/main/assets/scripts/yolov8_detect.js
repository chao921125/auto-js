// AI生成
// YOLOv8 目标检测示例
"auto";
auto.waitFor();

// 申请截图权限
images.requestScreenCapture();
sleep(500);

// 创建检测器（需要提前推送 .onnx 模型到设备）
let modelPath = "/sdcard/autojs/models/yolov8n.onnx";
let labels = ["person", "bicycle", "car", "motorcycle", "airplane", "bus", "train", "truck", "boat", "traffic light", "fire hydrant", "stop sign", "bench", "bird", "cat", "dog", "backpack", "umbrella", "handbag", "tie", "suitcase", "frisbee", "skis", "snowboard", "sports ball", "kite", "baseball bat", "baseball glove", "skateboard", "surfboard", "tennis racket", "bottle", "wine glass", "cup", "fork", "knife", "spoon", "bowl", "banana", "apple", "sandwich", "orange", "broccoli", "carrot", "hot dog", "pizza", "donut", "cake", "chair", "couch", "potted plant", "bed", "dining table", "toilet", "tv", "laptop", "mouse", "remote", "keyboard", "cell phone", "microwave", "oven", "toaster", "sink", "refrigerator", "book", "clock", "vase", "scissors", "teddy bear", "hair drier", "toothbrush"];

let detector = ml.createDetector(modelPath, labels, 0.5, 0.45);
if (!detector) {
    log("模型加载失败，请确保模型文件存在: " + modelPath);
    exit();
}

log("YOLOv8 检测器已就绪");

// 截图并检测
let img = images.captureScreen();
let results = ml.detect(detector, img);

log("检测到 " + results.length + " 个目标");
for (let i = 0; i < results.length; i++) {
    let r = results[i];
    log("[" + (i+1) + "] " + r.label + " (" + (r.score * 100).toFixed(1) + "%) 位置: " + r.bounds);
}

// 释放资源
ml.release(detector);
