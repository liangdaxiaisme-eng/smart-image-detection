"""
简易检测模块 - 使用OpenCV Haar Cascade作为基础检测
后续可替换为YOLOv8模型
"""
import os
import cv2
import numpy as np


class Detector:
    def __init__(self, model_path=None):
        """初始化检测器"""
        # 加载OpenCV预训练的人脸/身体检测器（在系统自带，无需下载）
        self.face_cascade = cv2.CascadeClassifier(
            cv2.data.haarcascades + "haarcascade_frontalface_default.xml"
        )
        self.body_cascade = cv2.CascadeClassifier(
            cv2.data.haarcascades + "haarcascade_fullbody.xml"
        )
        self.upper_cascade = cv2.CascadeClassifier(
            cv2.data.haarcascades + "haarcascade_upperbody.xml"
        )

        self.class_names = {
            0: "person",
            1: "face",
            2: "upper_body"
        }

    def detect(self, image_path, conf_threshold=0.25):
        """
        执行检测
        返回: [{class_id, class_name, confidence, bbox: [x1,y1,x2,y2]}, ...]
        """
        img = cv2.imread(image_path)
        if img is None:
            return []
        gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
        h, w = img.shape[:2]

        detections = []

        # 人脸检测
        faces = self.face_cascade.detectMultiScale(
            gray, scaleFactor=1.1, minNeighbors=5, minSize=(30, 30)
        )
        for (x, y, fw, fh) in faces:
            conf = min(0.95, 0.5 + (fw * fh) / (w * h) * 2)
            if conf < conf_threshold:
                continue
            detections.append({
                "class_id": 1,
                "class_name": "face",
                "confidence": round(conf, 4),
                "bbox": [float(x), float(y), float(x + fw), float(y + fh)]
            })

        # 上半身检测
        uppers = self.upper_cascade.detectMultiScale(
            gray, scaleFactor=1.1, minNeighbors=3, minSize=(50, 50)
        )
        for (x, y, uw, uh) in uppers:
            conf = min(0.85, 0.3 + (uw * uh) / (w * h) * 3)
            if conf < conf_threshold:
                continue
            detections.append({
                "class_id": 2,
                "class_name": "upper_body",
                "confidence": round(conf, 4),
                "bbox": [float(x), float(y), float(x + uw), float(y + uh)]
            })

        # 全身检测
        bodies = self.body_cascade.detectMultiScale(
            gray, scaleFactor=1.1, minNeighbors=3, minSize=(100, 200)
        )
        for (x, y, bw, bh) in bodies:
            conf = min(0.8, 0.3 + (bw * bh) / (w * h) * 3)
            if conf < conf_threshold:
                continue
            detections.append({
                "class_id": 0,
                "class_name": "person",
                "confidence": round(conf, 4),
                "bbox": [float(x), float(y), float(x + bw), float(y + bh)]
            })

        return detections

    def draw_boxes(self, image_path, detections, output_path):
        """在图片上绘制检测框和标签"""
        img = cv2.imread(image_path)
        if img is None:
            img = cv2.imdecode(np.frombuffer(open(image_path, "rb").read(), np.uint8), cv2.IMREAD_COLOR)

        colors = {
            0: (255, 0, 0),    # person - 蓝色
            1: (0, 255, 0),    # face - 绿色
            2: (0, 255, 255),  # upper_body - 黄色
        }

        for det in detections:
            x1, y1, x2, y2 = map(int, det["bbox"])
            cls_id = det["class_id"]
            cls_name = det["class_name"]
            conf = det["confidence"]
            color = colors.get(cls_id, (255, 255, 255))

            # 画框
            cv2.rectangle(img, (x1, y1), (x2, y2), color, 2)

            # 标签
            label = f"{cls_name} {conf:.2f}"
            (label_w, label_h), baseline = cv2.getTextSize(
                label, cv2.FONT_HERSHEY_SIMPLEX, 0.6, 2
            )
            y1_label = max(y1 - label_h - 5, 0)
            cv2.rectangle(img, (x1, y1_label), (x1 + label_w, y1), color, -1)
            cv2.putText(img, label, (x1, y1 - 5), cv2.FONT_HERSHEY_SIMPLEX,
                        0.6, (255, 255, 255), 2)

        cv2.imwrite(output_path, img)
        return output_path

    def get_statistics(self, detections):
        """统计检测结果"""
        total = len(detections)
        per_class = {}
        for det in detections:
            name = det["class_name"]
            per_class[name] = per_class.get(name, 0) + 1

        sorted_class = sorted(per_class.items(), key=lambda x: -x[1])

        return {
            "total": total,
            "per_class": [{"class_name": k, "count": v} for k, v in sorted_class]
        }


if __name__ == "__main__":
    d = Detector()
    print(f"检测器已初始化")
    print(f"可用检测器: {list(d.class_names.values())}")
