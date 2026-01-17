# app.py
import os
import sys
from flask import Flask, render_template, Response, request, jsonify, stream_with_context
from web_inference import WebDetector
from ai_api import DoubaoClient

app = Flask(__name__)
detector = WebDetector()
ai_client = DoubaoClient()

# --- 路径配置 ---
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
MODELS_DIR = os.path.join(BASE_DIR, 'models')
EXAMPLES_DIR = os.path.join(BASE_DIR, 'example')

# --- 启动自检 ---
print("-" * 50)
print(f"【系统自检】正在检查文件夹路径...")
print(f"1. 项目根目录: {BASE_DIR}")
print(f"2. 模型文件夹: {MODELS_DIR}")

if not os.path.exists(MODELS_DIR):
    print(f"   [❌ 错误] models 文件夹不存在！请在 app.py 同级目录下创建 models 文件夹。")
    os.makedirs(MODELS_DIR)
else:
    files = [f for f in os.listdir(MODELS_DIR) if f.endswith('.pt')]
    print(f"   [✅ 正常] 发现 {len(files)} 个模型文件: {files}")

print(f"3. 示例文件夹: {EXAMPLES_DIR}")
if not os.path.exists(EXAMPLES_DIR):
    os.makedirs(EXAMPLES_DIR)
print("-" * 50)


# --- 路由定义 ---

@app.route('/')
def index():
    return render_template('index.html')


@app.route('/video_feed')
def video_feed():
    return Response(detector.gen_frames(), mimetype='multipart/x-mixed-replace; boundary=frame')


@app.route('/api/models')
def list_models():
    try:
        if not os.path.exists(MODELS_DIR):
            return jsonify([])
        files = [f for f in os.listdir(MODELS_DIR)
                 if (f.lower().endswith('.pt') or f.lower().endswith('.pth'))]
        return jsonify(files)
    except Exception as e:
        print(f"Error reading models: {e}")
        return jsonify([])


@app.route('/api/examples')
def list_examples():
    try:
        if not os.path.exists(EXAMPLES_DIR):
            return jsonify([])
        files = [f for f in os.listdir(EXAMPLES_DIR) if os.path.isfile(os.path.join(EXAMPLES_DIR, f))]
        paths = [os.path.join('example', f).replace('\\', '/') for f in files]
        return jsonify(paths)
    except Exception:
        return jsonify([])


@app.route('/api/start', methods=['POST'])
def start_detection():
    data = request.json

    model_input = data.get('model_name', '').strip()
    if os.path.sep in model_input or '/' in model_input:
        full_model_path = model_input
    else:
        full_model_path = os.path.join(MODELS_DIR, model_input)

    input_type = data.get('input_type')
    source = 'camera' if input_type == 'camera' else data.get('path_input', '').strip()

    params = {
        "model_path": full_model_path,
        "source": source,
        "conf_thres": float(data.get('conf_thres', 0.25)),
        "iou_thres": float(data.get('iou_thres', 0.45)),
        "imgsz": int(data.get('imgsz', 640)),
        "save_txt": data.get('save_txt', False),
        "save_conf": data.get('save_conf', False),
        "save_crop": data.get('save_crop', False)
    }

    detector.update_params(params)
    return jsonify({"status": "started"})


@app.route('/api/pause', methods=['POST'])
def pause_detection():
    is_paused = detector.toggle_pause()
    return jsonify({"status": "paused" if is_paused else "resumed", "is_paused": is_paused})


@app.route('/api/stop', methods=['POST'])
def stop_detection():
    detector.stop_event = True
    return jsonify({"status": "stopped"})


@app.route('/api/results')
def get_results():
    with detector.results_lock:
        return jsonify(detector.recent_results)


@app.route('/api/clear_results', methods=['POST'])
def clear_results():
    detector.clear_results()
    return jsonify({"status": "cleared"})


# --- 新增：获取实时统计数据 API ---
@app.route('/api/stats')
def get_stats():
    with detector.stats_lock:
        return jsonify(detector.current_stats)


@app.route('/api/chat', methods=['POST'])
def chat_with_ai():
    data = request.json
    user_msg = data.get('message', '')
    if not user_msg:
        return "请输入内容", 400

    def generate():
        for chunk in ai_client.get_response_stream(user_msg):
            yield chunk

    return Response(stream_with_context(generate()), mimetype='text/plain')


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)