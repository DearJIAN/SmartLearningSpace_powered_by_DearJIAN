// static/script.js

let pollingInterval = null;
let isPaused = false;
let myChart = null;
let chartInterval = null;

document.addEventListener('DOMContentLoaded', function() {
    loadModels();
    loadExamples();
    initChart();
});

// --- 【核心修改】ECharts 初始化与自适应 ---
function initChart() {
    const chartDom = document.getElementById('mainChart');
    const chartContainer = document.getElementById('chartWindow'); // 获取外部容器

    if(!chartDom) return;

    myChart = echarts.init(chartDom);

    const colors = [
        new echarts.graphic.LinearGradient(0, 0, 0, 1, [{offset: 0, color: '#83bff6'}, {offset: 0.5, color: '#188df0'}, {offset: 1, color: '#188df0'}]),
        new echarts.graphic.LinearGradient(0, 0, 0, 1, [{offset: 0, color: '#20bf55'}, {offset: 1, color: '#01baef'}]),
        new echarts.graphic.LinearGradient(0, 0, 0, 1, [{offset: 0, color: '#fbc2eb'}, {offset: 1, color: '#a6c1ee'}]),
        new echarts.graphic.LinearGradient(0, 0, 0, 1, [{offset: 0, color: '#ff9a9e'}, {offset: 1, color: '#fecfef'}]),
        new echarts.graphic.LinearGradient(0, 0, 0, 1, [{offset: 0, color: '#a18cd1'}, {offset: 1, color: '#fbc2eb'}])
    ];

    const option = {
        title: { text: '实时类别统计', left: 'center' },
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
        xAxis: { type: 'category', data: [] },
        yAxis: { type: 'value', minInterval: 1 },
        series: [{
            name: '数量', type: 'bar', data: [],
            itemStyle: {
                color: function(params) {
                    return colors[params.dataIndex % colors.length];
                },
                borderRadius: [5, 5, 0, 0]
            },
            label: { show: true, position: 'top' }
        }]
    };
    myChart.setOption(option);

    // 【终极适配方案】使用 ResizeObserver 监听容器大小变化
    // 只要 chartWindow 的 div 大小变了（不管是因为浏览器缩放，还是 CSS 媒体查询），图表都会自动重绘
    const resizeObserver = new ResizeObserver(() => {
        myChart.resize();
    });

    if (chartContainer) {
        resizeObserver.observe(chartContainer);
    }
}

function toggleChart() {
    const win = document.getElementById('chartWindow');
    if (win.style.display === 'flex') {
        win.style.display = 'none';
        if (chartInterval) clearInterval(chartInterval);
    } else {
        document.getElementById('chatWindow').style.display = 'none';
        win.style.display = 'flex';
        // 打开瞬间强制重绘一次，防止渲染错位
        setTimeout(() => myChart.resize(), 50);
        startChartPolling();
    }
}

function startChartPolling() {
    if (chartInterval) clearInterval(chartInterval);
    updateChartData();
    chartInterval = setInterval(updateChartData, 500);
}

function updateChartData() {
    fetch('/api/stats')
    .then(res => res.json())
    .then(data => {
        const categories = Object.keys(data);
        const counts = Object.values(data);
        myChart.setOption({ xAxis: { data: categories }, series: [{ data: counts }] });
    })
    .catch(err => console.error(err));
}

// --- 常规逻辑 ---
function loadModels() {
    fetch('/api/models').then(res => res.json()).then(files => {
        const dataList = document.getElementById('modelOptions');
        const inputPath = document.getElementById('modelInput');
        if(dataList) {
            dataList.innerHTML = "";
            files.forEach(f => {
                const opt = document.createElement('option');
                opt.value = f;
                dataList.appendChild(opt);
            });
            if (files.length > 0 && inputPath && !inputPath.value) inputPath.value = files[0];
        }
    }).catch(err => console.error(err));
}

function loadExamples() {
    fetch('/api/examples').then(res => res.json()).then(paths => {
        const dataList = document.getElementById('pathOptions');
        if(dataList) {
            dataList.innerHTML = "";
            paths.forEach(p => {
                const opt = document.createElement('option');
                opt.value = p;
                dataList.appendChild(opt);
            });
        }
    });
}

function toggleInput(type) {
    document.getElementById('pathInputGroup').style.display = (type === 'path') ? 'block' : 'none';
}

function clearResults() {
    if(!confirm("确定要清空所有检测结果吗？")) return;
    fetch('/api/clear_results', {method: 'POST'})
    .then(res => res.json())
    .then(data => { document.querySelector('#resultTable tbody').innerHTML = ""; });
}

function startSystem() {
    const inputType = document.querySelector('input[name="inputType"]:checked').value;
    const pathInput = document.getElementById('pathInput').value;
    const modelInput = document.getElementById('modelInput').value;
    if (inputType === 'path' && !pathInput) { alert("❌ 请输入或选择路径！"); return; }
    if (!modelInput) { alert("❌ 请输入或选择模型！"); return; }
    const data = {
        input_type: inputType, path_input: pathInput, model_name: modelInput,
        conf_thres: document.getElementById('confThres').value,
        iou_thres: document.getElementById('iouThres').value,
        imgsz: document.getElementById('imgSz').value,
        save_txt: document.getElementById('saveTxt').checked,
        save_conf: document.getElementById('saveConf').checked,
        save_crop: document.getElementById('saveCrop').checked,
    };
    fetch('/api/start', { method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify(data) })
    .then(res => res.json())
    .then(res => {
        if(res.status === "started") {
            const img = document.getElementById('videoStream');
            const ph = document.getElementById('placeholder');
            isPaused = false;
            const btnPause = document.getElementById('btnPause');
            btnPause.innerText = "⏸️ 暂停 (Pause)";
            btnPause.style.backgroundColor = "#fd7e14";
            img.src = "/video_feed?" + new Date().getTime();
            img.style.display = "block";
            ph.style.display = "none";
            startPollingResults(false);
        }
    }).catch(err => alert("启动失败: " + err));
}

function pauseSystem() {
    fetch('/api/pause', {method: 'POST'}).then(res => res.json()).then(data => {
        const btn = document.getElementById('btnPause');
        if (data.is_paused) { btn.innerText = "▶️ 继续 (Resume)"; btn.style.backgroundColor = "#17a2b8"; }
        else { btn.innerText = "⏸️ 暂停 (Pause)"; btn.style.backgroundColor = "#fd7e14"; }
    });
}

function stopSystem() {
    fetch('/api/stop', {method: 'POST'}).then(() => {
        const img = document.getElementById('videoStream');
        const ph = document.getElementById('placeholder');
        img.style.display = "none"; img.src = ""; ph.style.display = "flex";

        // 【修改点】确保文字是白色（样式已改，这里只改结构）
        ph.innerHTML = "<div style='font-size:3rem'>🛑</div><p>检测已停止</p>";

        stopPollingResults();
        let attempt = 0;
        const finalCheckInterval = setInterval(() => { fetchResults(); attempt++; if (attempt >= 6) clearInterval(finalCheckInterval); }, 500);
    });
}

function startPollingResults(clearTable = false) {
    if (pollingInterval) clearInterval(pollingInterval);
    if (clearTable) document.querySelector('#resultTable tbody').innerHTML = "";
    pollingInterval = setInterval(() => { fetchResults(); }, 1000);
}
function stopPollingResults() { if (pollingInterval) clearInterval(pollingInterval); }

function fetchResults() {
    fetch('/api/results').then(res => res.json()).then(paths => {
        const tbody = document.querySelector('#resultTable tbody');
        const currentRows = tbody.children.length;
        if (paths.length > currentRows) {
            for (let i = currentRows; i < paths.length; i++) {
                const tr = document.createElement('tr');
                const tdIndex = document.createElement('td'); tdIndex.innerText = i + 1;
                const tdPath = document.createElement('td'); tdPath.innerText = paths[i];
                tr.appendChild(tdIndex); tr.appendChild(tdPath);
                tbody.appendChild(tr);
            }
            const wrapper = document.querySelector('.table-wrapper');
            wrapper.scrollTop = wrapper.scrollHeight;
        }
    });
}

// --- AI Chat ---
function toggleChat() {
    const win = document.getElementById('chatWindow');
    if (win.style.display === 'flex') {
        win.style.display = 'none';
    } else {
        document.getElementById('chartWindow').style.display = 'none';
        win.style.display = 'flex';
        setTimeout(() => document.getElementById('chatInput').focus(), 100);
    }
}
function handleEnter(e) { if (e.key === 'Enter') sendMessage(); }
async function sendMessage() {
    const input = document.getElementById('chatInput');
    const msg = input.value.trim();
    if (!msg) return;
    appendMessage('user', msg);
    input.value = '';
    const botMsgDiv = appendMessage('bot', '✨ AI 正在思考中...');
    botMsgDiv.classList.add('loading-rainbow');
    let fullMarkdownText = "";
    let isFirstChunk = true;
    try {
        const response = await fetch('/api/chat/stream', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({message: msg})
        });
        if (!response.ok) {
            const detail = await response.text().catch(() => '');
            throw new Error(detail || `HTTP ${response.status}`);
        }
        const reader = response.body?.getReader();
        if (!reader) {
            throw new Error('当前浏览器不支持流式响应');
        }
        const decoder = new TextDecoder("utf-8");
        let buffer = "";
        while (true) {
            const { done, value } = await reader.read();
            if (done) break;
            buffer += decoder.decode(value, {stream: true});
            const lines = buffer.split(/\r?\n/);
            buffer = lines.pop() || "";
            for (const raw of lines) {
                const line = raw.trim();
                if (!line || line.startsWith('sessionId:') || line.startsWith('done:')) continue;
                if (line.startsWith('error:')) {
                    throw new Error(line.slice('error:'.length).trim() || 'AI 回复失败');
                }
                if (!line.startsWith('delta:')) continue;
                if (isFirstChunk) {
                    botMsgDiv.classList.remove('loading-rainbow');
                    botMsgDiv.innerHTML = "";
                    isFirstChunk = false;
                }
                fullMarkdownText += line.slice('delta:'.length);
                botMsgDiv.innerHTML = marked.parse(fullMarkdownText);
                const body = document.getElementById('chatBody');
                body.scrollTop = body.scrollHeight;
            }
        }
        if (isFirstChunk) {
            botMsgDiv.classList.remove('loading-rainbow');
            botMsgDiv.innerHTML = marked.parse('我刚刚没有组织出合适的回答，你可以换个方式再问一次。');
        }
    } catch (err) {
        botMsgDiv.classList.remove('loading-rainbow');
        botMsgDiv.innerHTML += `<br><span style="color:red">[❌ 网络/API 错误: ${err}]</span>`;
    }
}
function appendMessage(role, text) {
    const body = document.getElementById('chatBody');
    const div = document.createElement('div');
    div.className = `chat-msg ${role}`;
    div.innerText = text;
    body.appendChild(div);
    body.scrollTop = body.scrollHeight;
    return div;
}
