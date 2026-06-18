"""
智能图像检测系统 - YOLO检测服务
Flask API Server，监听5000端口
"""
import os
import uuid
import json
from flask import Flask, request, jsonify, send_from_directory


from detector import Detector

app = Flask(__name__)


# 配置
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
UPLOAD_DIR = os.path.join(BASE_DIR, "uploads")
RESULT_DIR = os.path.join(BASE_DIR, "static", "results")
os.makedirs(UPLOAD_DIR, exist_ok=True)
os.makedirs(RESULT_DIR, exist_ok=True)

ALLOWED_EXTENSIONS = {"png", "jpg", "jpeg", "bmp", "webp"}
MAX_FILE_SIZE = 50 * 1024 * 1024  # 50MB

# 初始化检测器
detector = None


def get_detector():
    global detector
    if detector is None:
        print("正在加载YOLO模型...")
        detector = Detector()
        print(f"YOLO模型加载完成，支持{len(detector.class_names)}个类别")
    return detector


def allowed_file(filename):
    return "." in filename and \
        filename.rsplit(".", 1)[1].lower() in ALLOWED_EXTENSIONS


@app.route("/health", methods=["GET"])
def health():
    """健康检查"""
    d = get_detector()
    return jsonify({
        "status": "ok",
        "model": "yolov8n",
        "classes": len(d.class_names)
    })


@app.route("/detect", methods=["POST"])
def detect():
    """目标检测API"""
    if "file" not in request.files:
        return jsonify({"code": 400, "msg": "请上传图片文件"}), 400

    file = request.files["file"]
    if file.filename == "":
        return jsonify({"code": 400, "msg": "文件名为空"}), 400

    if not allowed_file(file.filename):
        return jsonify({"code": 400, "msg": "仅支持png/jpg/jpeg/bmp格式"}), 400

    # 获取置信度阈值
    try:
        conf_threshold = float(request.form.get("conf_threshold", 0.25))
    except:
        conf_threshold = 0.25

    # 保存上传文件
    ext = file.filename.rsplit(".", 1)[1].lower()
    filename = f"{uuid.uuid4().hex}.{ext}"
    filepath = os.path.join(UPLOAD_DIR, filename)
    file.save(filepath)

    file_size = os.path.getsize(filepath)
    if file_size > MAX_FILE_SIZE:
        os.remove(filepath)
        return jsonify({"code": 400, "msg": "文件大小超过50MB限制"}), 400

    try:
        # 执行检测
        d = get_detector()
        detections = d.detect(filepath, conf_threshold)

        # 绘制检测结果图
        result_filename = f"result_{filename}"
        result_path = os.path.join(RESULT_DIR, result_filename)
        d.draw_boxes(filepath, detections, result_path)

        # 统计数据
        statistics = d.get_statistics(detections)

        # 清理临时文件
        os.remove(filepath)

        return jsonify({
            "code": 200,
            "data": {
                "detections": detections,
                "result_image_url": f"/static/results/{result_filename}",
                "statistics": statistics
            }
        })
    except Exception as e:
        return jsonify({"code": 500, "msg": f"检测失败：{str(e)}"}), 500


@app.route("/static/results/<filename>")
def result_image(filename):
    """获取结果图"""
    return send_from_directory(RESULT_DIR, filename)


if __name__ == "__main__":
    print("=" * 50)
    print("智能图像检测 - YOLO推理服务")
    print(f"监听端口: 5000")
    print(f"上传目录: {UPLOAD_DIR}")
    print(f"结果目录: {RESULT_DIR}")
    print("=" * 50)
    app.run(host="0.0.0.0", port=5000, debug=False, threaded=True)
