# web_inference.py
import cv2
import time
import threading
from pathlib import Path
from ultralytics import YOLO
from collections import Counter
from yolo_io_utils import (
    create_exp_dir, ensure_labels_dir, ensure_crops_dir,
    split_media, build_videowriter, save_annotated_image, save_txt, save_crops,
    IMAGE_EXTS, VIDEO_EXTS
)


class WebDetector:
    def __init__(self):
        self.model = None
        self.model_path = None
        self.is_running = False
        self.is_paused = False
        self.stop_event = False

        # 结果列表
        self.recent_results = []
        self.results_lock = threading.Lock()

        # --- 新增：实时统计数据 ---
        self.current_stats = {}
        self.stats_lock = threading.Lock()

        # 默认参数
        self.params = {
            "source": "",
            "model_path": "yolo11n.pt",
            "conf_thres": 0.25,
            "iou_thres": 0.45,
            "imgsz": 640,
            "save_txt": False,
            "save_conf": False,
            "save_crop": False
        }

    def clear_results(self):
        with self.results_lock:
            self.recent_results = []
            print("[Info] 后端结果列表已清空")

    def update_params(self, new_params):
        if "model_path" in new_params and new_params["model_path"] != self.model_path:
            try:
                self.model = YOLO(new_params["model_path"])
                self.model_path = new_params["model_path"]
                print(f"模型已加载: {self.model_path}")
            except Exception as e:
                print(f"模型加载失败: {e}")

        self.params.update(new_params)

    def _infer_single_frame(self, frame, stem, exp_dir, labels_dir, crops_root):
        results = self.model.predict(
            frame,
            conf=float(self.params["conf_thres"]),
            iou=float(self.params["iou_thres"]),
            imgsz=int(self.params["imgsz"]),
            agnostic_nms=True,
            verbose=False
        )[0]
        im0 = results.plot()

        boxes = results.boxes.xyxy.tolist()
        confs = results.boxes.conf.tolist()
        clss = results.boxes.cls.tolist()
        names = results.names

        # --- 统计当前帧类别 ---
        detected_names = [names[int(cls)] for cls in clss]
        stats_counter = Counter(detected_names)
        with self.stats_lock:
            self.current_stats = dict(stats_counter)

        if self.params["save_txt"] and labels_dir:
            txt_path = labels_dir / f"{stem}.txt"
            save_txt(txt_path, boxes, clss, confs, self.params["save_conf"])

        if self.params["save_crop"] and crops_root:
            save_crops(crops_root, stem, results.orig_img, boxes, clss, names)

        return im0

    def toggle_pause(self):
        self.is_paused = not self.is_paused
        return self.is_paused

    def gen_frames(self):
        self.is_running = True
        self.stop_event = False
        self.is_paused = False

        # 清空统计
        with self.stats_lock:
            self.current_stats = {}

        source = self.params["source"]
        exp_dir = create_exp_dir('runs', 'web_exp')
        labels_dir = ensure_labels_dir(exp_dir) if self.params["save_txt"] else None
        crops_root = ensure_crops_dir(exp_dir) if self.params["save_crop"] else None

        # --- 摄像头模式 ---
        if source == "camera":
            cap = cv2.VideoCapture(0)
            vid_writer = None
            out_path = exp_dir / f"camera_{int(time.time())}.mp4"

            try:
                while self.is_running and cap.isOpened() and not self.stop_event:
                    if self.is_paused:
                        time.sleep(0.1)
                        continue

                    success, frame = cap.read()
                    if not success: break

                    im0 = self._infer_single_frame(frame, f"cam_{time.time()}", exp_dir, labels_dir, crops_root)

                    if vid_writer is None:
                        h, w = im0.shape[:2]
                        vid_writer = build_videowriter(out_path, 25, (w, h))
                    vid_writer.write(im0)

                    ret, buffer = cv2.imencode('.jpg', im0)
                    yield (b'--frame\r\nContent-Type: image/jpeg\r\n\r\n' + buffer.tobytes() + b'\r\n')

            finally:
                cap.release()
                if vid_writer:
                    vid_writer.release()
                    with self.results_lock:
                        self.recent_results.append(str(out_path))

        # --- 文件/文件夹模式 ---
        else:
            path_obj = Path(source)
            files_to_process = []

            if path_obj.is_dir():
                imgs, vids = split_media(path_obj)
                files_to_process = imgs + vids
            elif path_obj.is_file():
                files_to_process = [path_obj]

            if not files_to_process:
                err_img = self._make_err_img()
                yield (b'--frame\r\nContent-Type: image/jpeg\r\n\r\n' + cv2.imencode('.jpg', err_img)[
                    1].tobytes() + b'\r\n')
                self.is_running = False
                return

            for file_path in files_to_process:
                if not self.is_running or self.stop_event: break

                if file_path.suffix.lower() in VIDEO_EXTS:
                    cap = cv2.VideoCapture(str(file_path))
                    out_name = exp_dir / f"{file_path.stem}_out.mp4"
                    vid_writer = None

                    try:
                        while cap.isOpened() and self.is_running and not self.stop_event:
                            if self.is_paused:
                                time.sleep(0.1)
                                continue

                            success, frame = cap.read()
                            if not success: break

                            im0 = self._infer_single_frame(frame, f"{file_path.stem}_{int(time.time())}", exp_dir,
                                                           labels_dir, crops_root)

                            if vid_writer is None:
                                h, w = im0.shape[:2]
                                vid_writer = build_videowriter(out_name, 30, (w, h))
                            vid_writer.write(im0)

                            ret, buffer = cv2.imencode('.jpg', im0)
                            yield (b'--frame\r\nContent-Type: image/jpeg\r\n\r\n' + buffer.tobytes() + b'\r\n')

                    finally:
                        cap.release()
                        if vid_writer:
                            vid_writer.release()
                            with self.results_lock:
                                self.recent_results.append(str(out_name))
                else:
                    # 图片模式
                    while self.is_paused and self.is_running and not self.stop_event:
                        time.sleep(0.1)

                    frame = cv2.imread(str(file_path))
                    if frame is None: continue

                    im0 = self._infer_single_frame(frame, file_path.stem, exp_dir, labels_dir, crops_root)
                    save_path = exp_dir / f"{file_path.stem}_res.jpg"
                    save_annotated_image(im0, save_path)

                    with self.results_lock:
                        self.recent_results.append(str(save_path))

                    for _ in range(10):
                        if not self.is_running or self.stop_event: break
                        ret, buffer = cv2.imencode('.jpg', im0)
                        yield (b'--frame\r\nContent-Type: image/jpeg\r\n\r\n' + buffer.tobytes() + b'\r\n')
                        time.sleep(0.1)

        self.is_running = False

    def _make_err_img(self):
        import numpy as np
        img = np.zeros((480, 640, 3), dtype=np.uint8)
        cv2.putText(img, "No Files Found", (50, 240), cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 0, 255), 2)
        return img