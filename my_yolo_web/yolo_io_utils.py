# yolo_io_utils.py
from __future__ import annotations
from pathlib import Path
from typing import List, Tuple, Sequence, Dict, Optional
import re
import cv2
import os

IMAGE_EXTS = {
    ".jpg", ".jpeg", ".png", ".bmp", ".gif", ".tiff", ".webp",
    ".heif", ".raw", ".ico", ".jfif"
}

VIDEO_EXTS = {
    ".mp4", ".avi", ".mov", ".mkv", ".flv", ".webm", ".wmv",
    ".mpeg", ".mpg", ".3gp", ".ts"
}


def contains_chinese(s: str) -> bool:
    return bool(re.search(r"[\u4e00-\u9fff]", s))


def ensure_dir(p: Path | str) -> Path:
    p = Path(p)
    p.mkdir(parents=True, exist_ok=True)
    return p


def create_exp_dir(base: Path | str = "runs", prefix: str = "exp") -> Path:
    base = ensure_dir(base)
    exp = base / prefix
    if not exp.exists():
        exp.mkdir()
        return exp
    i = 1
    while True:
        cand = base / f"{prefix}{i}"
        if not cand.exists():
            cand.mkdir()
            return cand
        i += 1


def ensure_labels_dir(exp_dir: Path | str) -> Path:
    return ensure_dir(Path(exp_dir) / "labels")


def ensure_crops_dir(exp_dir: Path | str) -> Path:
    return ensure_dir(Path(exp_dir) / "crops")


def split_media(dir_path: Path | str) -> Tuple[List[Path], List[Path]]:
    dir_path = Path(dir_path)
    images, videos = [], []
    if not dir_path.exists():
        return [], []
    for f in dir_path.iterdir():
        if not f.is_file():
            continue
        ext = f.suffix.lower()
        if ext in IMAGE_EXTS:
            images.append(f)
        elif ext in VIDEO_EXTS:
            videos.append(f)
    return images, videos


def build_videowriter(output_path: Path | str, fps: float, size: tuple[int, int],
                      fourcc: str = "mp4v") -> cv2.VideoWriter:
    output_path = str(output_path)
    fourcc_val = cv2.VideoWriter_fourcc(*fourcc)
    return cv2.VideoWriter(output_path, fourcc_val, fps, size)


def save_annotated_image(im_bgr, save_path: Path | str) -> None:
    cv2.imwrite(str(save_path), im_bgr)


def save_txt(txt_path: Path | str, boxes, classes, confs=None, save_conf=False) -> None:
    txt_path = Path(txt_path)
    ensure_dir(txt_path.parent)
    with txt_path.open("w", encoding="utf-8") as f:
        if save_conf and confs is not None:
            for loc, cls, conf in zip(boxes, classes, confs):
                x1, y1, x2, y2 = map(int, map(round, loc))
                f.write(f"{int(cls)} {x1} {y1} {x2} {y2} {float(conf):.4f}\n")
        else:
            for loc, cls in zip(boxes, classes):
                x1, y1, x2, y2 = map(int, map(round, loc))
                f.write(f"{int(cls)} {x1} {y1} {x2} {y2}\n")


def save_crops(crops_root: Path | str, base_stem: str, frame_bgr, boxes, classes, names_map) -> None:
    crops_root = ensure_dir(crops_root)
    H, W = frame_bgr.shape[:2]
    for i, (loc, cls) in enumerate(zip(boxes, classes)):
        cls_idx = int(cls)
        if names_map is None:
            class_name = str(cls_idx)
        elif isinstance(names_map, dict):
            class_name = names_map.get(cls_idx, str(cls_idx))
        else:
            class_name = (names_map[cls_idx] if 0 <= cls_idx < len(names_map) else str(cls_idx))

        class_dir = ensure_dir(Path(crops_root) / class_name)
        x1, y1, x2, y2 = map(int, map(round, loc))
        x1, x2 = max(0, min(x1, W)), max(0, min(x2, W))
        y1, y2 = max(0, min(y1, H)), max(0, min(y2, H))
        if x2 <= x1 or y2 <= y1: continue
        crop = frame_bgr[y1:y2, x1:x2]
        out_path = class_dir / f"{base_stem}_{class_name}_{i}.jpg"
        cv2.imwrite(str(out_path), crop)