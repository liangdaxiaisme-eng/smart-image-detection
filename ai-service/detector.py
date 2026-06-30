"""
YOLO 目标检测模块 - 基于 Ultralytics YOLOv8
"""
import os
import cv2
import numpy as np
from ultralytics import YOLO


class Detector:
    def __init__(self, model_path=None):
        """初始化 YOLO 检测器"""
        if model_path is None:
            model_path = os.path.join(os.path.dirname(__file__), "yolov8n.pt")
        
        print(f"[INFO] 加载 YOLO 模型: {model_path}")
        self.model = YOLO(model_path)
        self.class_names = self.model.names
        print(f"[INFO] 模型加载完成, 支持 {len(self.class_names)} 个类别")

    def detect(self, image_path, conf_threshold=0.25):
        """
        执行 YOLO 目标检测
        
        返回:
            [{class_id, class_name, confidence, bbox: [x1,y1,x2,y2]}, ...]
        """
        results = self.model(image_path, conf=conf_threshold, save=False, verbose=False)
        
        detections = []
        for result in results:
            boxes = result.boxes
            for box in boxes:
                cls_id = int(box.cls[0])
                cls_name = self.class_names[cls_id]
                confidence = round(float(box.conf[0]), 4)
                x1, y1, x2, y2 = box.xyxy[0].tolist()
                
                detections.append({
                    "class_id": cls_id,
                    "class_name": cls_name,
                    "confidence": confidence,
                    "bbox": [round(x1, 1), round(y1, 1), round(x2, 1), round(y2, 1)]
                })
        
        return detections

    def draw_boxes(self, image_path, detections, output_path):
        """在图片上绘制检测框和标签"""
        img = cv2.imread(image_path)
        if img is None:
            img = cv2.imdecode(np.frombuffer(open(image_path, "rb").read(), np.uint8), cv2.IMREAD_COLOR)
        
        # 使用 YOLO 自带绘图（效果更好）
        results = self.model(image_path, verbose=False)
        if results:
            result_img = results[0].plot()
            cv2.imwrite(output_path, result_img)
            return output_path
        
        # 兜底：手动绘制
        colors = np.random.randint(0, 255, (80, 3)).tolist()
        for det in detections:
            x1, y1, x2, y2 = map(int, det["bbox"])
            cls_id = det["class_id"]
            cls_name = det["class_name"]
            conf = det["confidence"]
            color = colors[cls_id % len(colors)]
            
            cv2.rectangle(img, (x1, y1), (x2, y2), color, 2)
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
    print(f"检测器就绪, 类别数: {len(d.class_names)}")
