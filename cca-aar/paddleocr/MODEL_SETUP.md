# RapidOCR 模型安装指南

## 📦 背景

本项目已从 **PaddleLite** 迁移到 **RapidOCR (ONNX Runtime)**,解决了 NDK 26+ 的兼容性问题。

## ⚠️ 需要手动下载模型

由于 HuggingFace 和 GitHub 的访问限制,模型文件需要**手动下载**。

### 1️⃣ 下载模型文件

从以下来源下载 PP-OCRv3 模型:

#### 方案 A:百度网盘 (推荐,速度快)
1. 访问 RapidOCR GitHub: https://github.com/RapidAI/RapidOCR
2. 查看 README 中的百度网盘链接
3. 下载以下文件:
   - `ch_PP-OCRv3_det_infer.onnx` (检测模型,约 8MB)
   - `ch_PP-OCRv3_rec_infer.onnx` (识别模型,约 4MB)
   - `ch_ppocr_mobile_v2.0_cls_infer.onnx` (方向分类模型,可选)

#### 方案 B:Google Drive
查看 RapidOCR GitHub Releases 中的 Google Drive 链接

#### 方案 C:HuggingFace (需要登录)
```bash
# 需要先安装 huggingface-cli 并登录
pip install huggingface_hub
huggingface-cli login

# 下载模型
huggingface-cli download RapidAI/PP-OCRv3 ch_PP-OCRv3_det_infer.onnx
huggingface-cli download RapidAI/PP-OCRv3 ch_PP-OCRv3_rec_infer.onnx
huggingface-cli download RapidAI/PP-OCRv3 ch_ppocr_mobile_v2.0_cls_infer.onnx
```

### 2️⃣ 放置模型文件

将下载的 `.onnx` 文件放到以下目录:

```
/Users/huangchao/Work/GitHub/auto-js/cca-aar/paddleocr/models/
```

完整目录结构应该是:
```
cca-aar/paddleocr/
├── models/
│   ├── ch_PP-OCRv3_det_infer.onnx    ← 放置在这里
│   ├── ch_PP-OCRv3_rec_infer.onnx    ← 放置在这里
│   └── ch_ppocr_mobile_v2.0_cls_infer.onnx  ← (可选)放置在这里
├── src/
├── build.gradle
└── ...
```

### 3️⃣ 验证模型文件

检查模型文件大小是否正确:
```bash
cd /Users/huangchao/Work/GitHub/auto-js/cca-aar/paddleocr/models/
ls -lh *.onnx
```

预期输出:
- `ch_PP-OCRv3_det_infer.onnx`: ~8 MB
- `ch_PP-OCRv3_rec_infer.onnx`: ~4 MB
- `ch_ppocr_mobile_v2.0_cls_infer.onnx`: ~2 MB (如果下载了)

### 4️⃣ 构建项目

模型放置完成后,运行:
```bash
./gradlew :cca-aar:paddleocr:assembleDebug
```

## 🔧 已完成的迁移工作

✅ **build.gradle** 
- 添加了 `onnxruntime-android:1.19.2` 依赖
- 移除了 NDK 版本要求
- 禁用了 CMake 编译(ONNX Runtime 已预编译)

✅ **依赖兼容性**
- 解决了 NDK 26+ LLD 链接器符号表问题
- Java/Kotlin JVM 目标版本统一为 1.8

## 📝 下一步

模型就绪后,需要创建 Java 封装代码来调用 ONNX Runtime。

参考:
- [RapidOCR Android 示例](https://github.com/RapidAI/RapidOCRAndroidOnnx)
- [ONNX Runtime Android API](https://onnxruntime.ai/docs/get-started/with-java.html)

## 🆘 常见问题

### Q: 下载速度很慢怎么办?
A: 使用百度网盘或其他国内镜像源

### Q: 需要下载所有三个模型吗?
A: 不一定。det 和 rec 是必须的,cls (方向分类)是可选的

### Q: 如何验证模型是否有效?
A: 使用 Python 脚本测试:
```python
from onnxruntime import InferenceSession

session = InferenceSession('ch_PP-OCRv3_det_infer.onnx')
print(f"Model inputs: {session.get_inputs()}")
print("Model loaded successfully!")
```

### Q: 如果不想使用 RapidOCR 怎么办?
A: 可以考虑其他方案:
- **Google ML Kit**: `implementation 'com.google.mlkit:text-recognition:16.0.0'`
- **Tesseract OCR**: `implementation 'com.rmtheis:tess-two:9.1.0'`

## 📚 参考资料

- RapidOCR GitHub: https://github.com/RapidAI/RapidOCR
- ONNX Runtime: https://onnxruntime.ai/
- PP-OCR v3 论文: https://github.com/PaddlePaddle/PaddleOCR
