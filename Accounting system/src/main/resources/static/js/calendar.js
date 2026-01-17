// Lightweight calendar implementation (moved out of Thymeleaf template to avoid parsing issues)
console.log('[calendar.js] loaded');
let currentViewMode = 'all'; // all, loss, gain, balance
let heatMapData = {};
let eventsByDate = {}; // cache events keyed by YYYY-MM-DD; cells will not render events
let currentDate = new Date(); // current focused date (month)
let eventModal = null;
try {
    if (window.bootstrap && document.getElementById('eventModal')) {
        eventModal = new bootstrap.Modal(document.getElementById('eventModal'));
    }
} catch (e) {
    console.warn('[calendar.js] bootstrap modal init failed', e);
}

// Icon mapping: category id -> Font Awesome class
const ICON_MAP = {
    '1': 'fa-wallet',
    '2': 'fa-gift',
    '3': 'fa-utensils',
    '4': 'fa-bus',
    '5': 'fa-shopping-bag',
    '6': 'fa-home'
};
// Name mapping: category name (partial match) -> Font Awesome class
const NAME_MAP = {
    '交通': 'fa-bus',
    '餐饮': 'fa-utensils',
    '购物': 'fa-shopping-bag',
    '住房': 'fa-home',
    '礼物': 'fa-gift',
    '工资': 'fa-wallet',
    '奖金': 'fa-coins',
    'bonus': 'fa-coins',
    'salary': 'fa-wallet',
    'transport': 'fa-bus'
};

// Utilities
function pad(n) {
    return n < 10 ? '0' + n : '' + n;
}

function formatDay(d) {
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
}

function normalizeDateKey(raw) {
    if (!raw) return raw;
    // take only date portion before T
    const datePart = (raw + '').split('T')[0];
    const parts = datePart.split('-');
    if (parts.length >= 3) {
        const y = parts[0];
        const mo = pad(Number(parts[1]));
        const d = pad(Number(parts[2]));
        return `${y}-${mo}-${d}`;
    }
    // fallback: try Date parsing
    const dt = new Date(datePart);
    if (isNaN(dt)) return datePart;
    return formatDay(dt);
}

// --- NEW: read current filter values from DOM ---
function getCalendarFilters() {
    let type = '';
    let categoryId = '';
    try {
        const tEl = document.getElementById('typeFilter');
        const cEl = document.getElementById('catFilter');
        if (tEl) type = tEl.value || '';
        if (cEl) categoryId = cEl.value || '';
    } catch (e) {
        console.warn('[calendar.js] getCalendarFilters failed', e);
    }
    return {type, categoryId, viewMode: currentViewMode};
}

function monthRangeFor(date) {
    const y = date.getFullYear(), m = date.getMonth();
    const start = new Date(y, m, 1);
    const end = new Date(y, m + 1, 0); // last day
    return {start, end};
}

// Render calendar container (header + grid)
function renderCalendar() {
    try {
        const el = document.getElementById('calendar');
        if (!el) {
            console.warn('[calendar.js] calendar container #calendar not found');
            return;
        }
        el.innerHTML = '';

        // header
        const header = document.createElement('div');
        header.className = 'd-flex justify-content-between align-items-center mb-2';
        const title = document.createElement('div');
        title.className = 'fw-bold';
        title.id = 'calendarTitle';
        const nav = document.createElement('div');
        nav.innerHTML = `
            <div class="btn-group btn-group-sm" role="group">
                <button class="btn btn-outline-secondary" id="calPrev">‹</button>
                <button class="btn btn-outline-secondary" id="calToday">今天</button>
                <button class="btn btn-outline-secondary" id="calNext">›</button>
            </div>
        `;
        header.appendChild(title);
        header.appendChild(nav);
        el.appendChild(header);

        document.getElementById('calPrev')?.addEventListener('click', () => {
            currentDate.setMonth(currentDate.getMonth() - 1);
            refreshAll();
        });
        document.getElementById('calNext')?.addEventListener('click', () => {
            currentDate.setMonth(currentDate.getMonth() + 1);
            refreshAll();
        });
        document.getElementById('calToday')?.addEventListener('click', () => {
            currentDate = new Date();
            refreshAll();
        });

        // weekday header
        const weekNames = ['日', '一', '二', '三', '四', '五', '六'];
        const wk = document.createElement('div');
        wk.className = 'd-flex';
        weekNames.forEach(n => {
            const c = document.createElement('div');
            c.style.flex = '1';
            c.style.padding = '8px 6px';
            c.className = 'cal-weekday';
            c.innerText = n;
            wk.appendChild(c);
        });
        el.appendChild(wk);

        // grid
        const grid = document.createElement('div');
        grid.id = 'calGrid';
        grid.style.display = 'grid';
        grid.style.gridTemplateColumns = 'repeat(7, 1fr)';
        grid.style.gap = '10px';
        grid.style.marginTop = '8px';
        el.appendChild(grid);

        populateDays();
    } catch (err) {
        console.error('[calendar.js] renderCalendar failed', err);
    }
}

function populateDays() {
    const grid = document.getElementById('calGrid');
    if (!grid) return;
    grid.innerHTML = '';
    const {start, end} = monthRangeFor(currentDate);
    const firstWeekDay = start.getDay();
    const totalDays = end.getDate();

    // fill leading blanks (empty placeholders)
    for (let i = 0; i < firstWeekDay; i++) {
        const cell = document.createElement('div');
        cell.className = 'p-2';
        cell.style.minHeight = '90px';
        cell.style.background = 'transparent';
        grid.appendChild(cell);
    }

    for (let d = 1; d <= totalDays; d++) {
        const dateObj = new Date(start.getFullYear(), start.getMonth(), d);
        const dateStr = formatDay(dateObj);

        const cell = document.createElement('div');
        cell.className = 'p-2 fc-day';
        cell.style.minHeight = '110px';
        // center content
        cell.style.display = 'flex';
        cell.style.flexDirection = 'column';
        cell.style.justifyContent = 'center';
        cell.style.alignItems = 'center';
        cell.style.background = '#ebebeb';
        cell.style.borderRadius = '15px';
        cell.style.cursor = 'pointer';
        cell.dataset.date = dateStr;

        // day meta
        const meta = document.createElement('div');
        meta.className = 'day-meta';
        meta.style.display = 'flex';
        meta.style.flexDirection = 'column';
        meta.style.alignItems = 'center';
        meta.style.gap = '6px';
        const dateLabel = document.createElement('div');
        dateLabel.className = 'date-label';
        dateLabel.style.fontSize = '1.15rem';
        dateLabel.style.lineHeight = '1';
        dateLabel.innerText = d;
        const amt = document.createElement('div');
        amt.className = 'day-amount';
        amt.innerText = '￥0.00';
        meta.appendChild(dateLabel);
        meta.appendChild(amt);
        cell.appendChild(meta);

        // events container
        const eventsWrap = document.createElement('div');
        eventsWrap.className = 'events small mt-2';
        cell.appendChild(eventsWrap);

        // click to open drawer + mark selection
        cell.addEventListener('click', function (e) {
            if (e.target.closest('.day-event')) return;
            // clear previous selection
            document.querySelectorAll('.fc-day.selected').forEach(el => el.classList.remove('selected'));
            cell.classList.add('selected');
            openDrawer(dateStr);
        });

        grid.appendChild(cell);
    }

    // after building days, fetch events & heatmap
    const startStr = formatDay(start);
    const endStr = formatDay(end);
    fetchHeatMap(startStr, endStr);
    fetchEvents(startStr, endStr);

    // update title
    const titleEl = document.getElementById('calendarTitle');
    const y = currentDate.getFullYear(), m = currentDate.getMonth() + 1;
    titleEl.innerText = `${y} 年 ${m} 月`;
}

// --- modified: include filters and trigger UI update after fetching events ---
function fetchEvents(startStr, endStr) {
    const {type, categoryId} = getCalendarFilters();
    let url = `/api/calendar/events?start=${startStr}&end=${endStr}`;
    if (type) url += `&type=${encodeURIComponent(type)}`;
    if (categoryId) url += `&categoryId=${encodeURIComponent(categoryId)}`;

    // Fetch events for the month and cache them by date. We intentionally do not render
    // event items inside calendar cells (the drawer will load full details on click).
    fetch(url).then(r => r.json()).then(data => {
        eventsByDate = {};
        if (!Array.isArray(data)) return;
        data.forEach(ev => {
            const raw = ev.start ? (ev.start.split('T')[0]) : ev.date || ev.startDate;
            const st = normalizeDateKey(raw);
            if (!st) return;
            if (!eventsByDate[st]) eventsByDate[st] = [];
            eventsByDate[st].push(ev);
        });
        // Update calendar cells from event cache (if heatMapData is not present for a date,
        // this provides a responsive per-cell amount summary).
        try {
            updateCellsFromEvents();
            // also re-apply heatmap styles so any existing heatMapData still wins
            applyHeatMapStyles();
        } catch (e) {
            console.warn('[calendar.js] post-fetch update failed', e);
        }
    }).catch(() => {
    });
}

// --- modified: refetch both events and heatmap (keeps previous behavior) ---
function refetchEvents() {
    const {start, end} = monthRangeFor(currentDate);
    const s = formatDay(start), e = formatDay(end);
    fetchEvents(s, e);
    fetchHeatMap(s, e);
}

// --- modified: changeViewMode should trigger a refetch to update heatmap + cells ---
function changeViewMode(mode) {
    currentViewMode = mode;
    // update visuals immediately
    applyHeatMapStyles();
    // refetch server-side heatmap if server supports viewMode filtering
    refetchEvents();
}

// --- modified: include filters when fetching heatmap so heatmap reflects left controls ---
function fetchHeatMap(start, end) {
    const {type, categoryId, viewMode} = getCalendarFilters();
    let url = `/api/calendar/heatmap?start=${start}&end=${end}`;
    if (type) url += `&type=${encodeURIComponent(type)}`;
    if (categoryId) url += `&categoryId=${encodeURIComponent(categoryId)}`;
    // include viewMode as hint for server-side aggregation (optional)
    if (viewMode) url += `&viewMode=${encodeURIComponent(viewMode)}`;

    fetch(url)
        .then(r => r.json()).then(data => {
        heatMapData = data || {};
        applyHeatMapStyles();
    })
        .catch(() => {
            heatMapData = {};
            applyHeatMapStyles();
        });
}

function interpColor(hex1, hex2, t) {
    // 使用对数曲线偏移 t，使小值更靠近 0（浅色更多），映射保持在 [0,1]
    const biased = (t <= 0) ? 0 : Math.log1p(9 * t) / Math.log1p(9); // concave, 0..1 -> 0..1
    const n1 = parseInt(hex1.slice(1), 16), n2 = parseInt(hex2.slice(1), 16);
    const r1 = (n1 >> 16) & 255, g1 = (n1 >> 8) & 255, b1 = n1 & 255;
    const r2 = (n2 >> 16) & 255, g2 = (n2 >> 8) & 255, b2 = n2 & 255;
    const r = Math.round(r1 + (r2 - r1) * biased), g = Math.round(g1 + (g2 - g1) * biased),
        b = Math.round(b1 + (b2 - b1) * biased);
    return '#' + ((1 << 24) + (r << 16) + (g << 8) + b).toString(16).slice(1);
}

function applyHeatMapStyles() {
    // clear
    document.querySelectorAll('#calGrid [data-date]').forEach(cell => {
        cell.style.backgroundColor = '';
        const existing = cell.querySelector('.balance-label');
        if (existing) existing.remove();
        const amtEl = cell.querySelector('.day-amount');
        if (amtEl) {
            amtEl.innerText = '0.00';
            amtEl.style.color = '#8b8f8f';
        }
    });

    // color ranges (light -> deep)
    // Choose light colors near-white for small amounts, deep saturated colors for large amounts
    const redLight = '#fff5f6'; // near-white with tiny red tint for small expenses
    const redDeep = '#fc9aa5';  // saturated red for large expenses
    const greenLight = '#f3fff6'; // near-white with tiny green tint for small incomes
    const greenDeep = '#90dbba'; // saturated green for large incomes

    // Dynamically compute the largest absolute amount in the current heatMapData
    let maxAbs = 0;
    Object.keys(heatMapData || {}).forEach(d => {
        const day = heatMapData[d];
        if (!day) return;
        const inc = parseFloat(day.income || 0);
        const exp = parseFloat(day.expense || 0);
        let a = null;
        if (day.balance != null) a = parseFloat(day.balance);
        else a = inc - exp;
        if (Number.isFinite(a)) maxAbs = Math.max(maxAbs, Math.abs(a));
    });
    const MAX_SCALE = Math.max(1, maxAbs);

    Object.keys(heatMapData).forEach(date => {
        const dayData = heatMapData[date];
        const cel = document.querySelector(`#calGrid [data-date='${date}']`);
        if (!cel || !dayData) return;
        const income = parseFloat(dayData.income || 0);
        const expense = parseFloat(dayData.expense || 0);
        // prefer explicit balance if provided
        let amount = null;
        if (dayData.balance != null) amount = parseFloat(dayData.balance);
        else amount = income - expense;

        // don't override selected cell's background
        if (cel.classList.contains('selected')) {
            const amtEl = cel.querySelector('.day-amount');
            if (amtEl) {
                const val = Number(amount || 0);
                const sign = val > 0 ? '+' : (val < 0 ? '-' : '');
                amtEl.innerText = (sign ? sign : '') + Math.abs(val).toFixed(2);
                amtEl.style.color = val > 0 ? '#3db587' : (val < 0 ? '#dd606e' : '#8b8f8f');
            }
            return;
        }

        // default background
        cel.style.background = '#ebebeb';

        if (amount == null || amount === 0) {
            const amtEl = cel.querySelector('.day-amount');
            if (amtEl) {
                amtEl.innerText = '0.00';
                amtEl.style.color = '#8b8f8f';
            }
            return;
        }

        const absv = Math.min(1, Math.abs(amount) / MAX_SCALE);
        const t = absv;
        let bg, amtText, color;
        if (amount > 0) {
            bg = interpColor(greenLight, greenDeep, t);
            amtText = '+ ' + Math.abs(amount).toFixed(2);
            color = '#3db587';
        } else {
            bg = interpColor(redLight, redDeep, t);
            amtText = '- ' + Math.abs(amount).toFixed(2);
            color = '#dd606e';
        }
        cel.style.background = bg;
        const amtEl = cel.querySelector('.day-amount');
        if (amtEl) {
            amtEl.innerText = amtText;
            amtEl.style.color = color;
        }
    });
}

function refreshAll() {
    renderCalendar();
}

function openDrawer(dateStr) {
    const body = document.getElementById('drawerBody');
    const headerDate = document.getElementById('drawerDate');
    const headerTotal = document.getElementById('drawerTotal');
    headerDate.innerText = dateStr;
    headerTotal.innerText = '合计：￥0.00';
    body.innerHTML = '<div class="text-muted small">加载中...</div>';

    // Prefer cached monthly events if available to avoid an extra request
    const cached = eventsByDate && eventsByDate[dateStr];
    if (Array.isArray(cached) && cached.length > 0) {
        // ensure cached items look like transactions (have amount or category/title)
        const looksLikeTx = cached.every(ev => ev && (ev.amount != null || ev.category || ev.title));
        if (looksLikeTx) {
            renderDayDetails(dateStr, cached);
            return;
        }
        // otherwise fall through to fetch full day details
    }

    // include filters in day fetch so drawer displays filtered transactions
    const {type, categoryId} = getCalendarFilters();
    let dayUrl = `/api/calendar/day?date=${dateStr}`;
    if (type) dayUrl += `&type=${encodeURIComponent(type)}`;
    if (categoryId) dayUrl += `&categoryId=${encodeURIComponent(categoryId)}`;

    fetch(dayUrl).then(r => r.json()).then(data => {
        renderDayDetails(dateStr, data);
    }).catch(() => {
        const sample = [{time: '09:20', category: '餐饮', remark: '早餐', amount: -12.5}, {
            time: '12:10',
            category: '工资',
            remark: '报销',
            amount: +200
        }];
        renderDayDetails(dateStr, sample);
    });
}

// --- NEW: update cell amounts from events cache when heatMapData not available ---
function updateCellsFromEvents() {
    // iterate over date cells and set day-amount based on eventsByDate
    document.querySelectorAll('#calGrid [data-date]').forEach(cell => {
        const date = cell.dataset.date;
        const amtEl = cell.querySelector('.day-amount');
        // if heatMapData contains this date, prefer heatmap rendering
        if (heatMapData && heatMapData[date]) return;
        let amt = 0;
        let found = false;
        const list = eventsByDate && eventsByDate[date];
        if (Array.isArray(list)) {
            list.forEach(ev => {
                let val = null;
                if (ev.amount !== undefined && ev.amount !== null) val = Number(ev.amount);
                else if (ev.amt !== undefined && ev.amt !== null) val = Number(ev.amt);
                else if (ev.value !== undefined && ev.value !== null) val = Number(ev.value);
                // try negative/positive signs in title/category as fallback (best-effort)
                if (!Number.isFinite(val) && ev.title) {
                    const m = (ev.title + '').match(/([+-]?\d+(?:\.\d+)?)/);
                    if (m) val = Number(m[1]);
                }
                if (Number.isFinite(val)) {
                    amt += val;
                    found = true;
                }
            });
        }
        if (amtEl) {
            if (!found || amt === 0) {
                amtEl.innerText = '0.00';
                amtEl.style.color = '#8b8f8f';
            } else {
                const sign = amt > 0 ? '+ ' : '- ';
                amtEl.innerText = sign + Math.abs(amt).toFixed(2);
                amtEl.style.color = amt > 0 ? '#3db587' : '#dd606e';
            }
        }
    });
}

// --- Accept different response shapes
function renderDayDetails(dateStr, list) {
    const body = document.getElementById('drawerBody');
    const headerTotal = document.getElementById('drawerTotal');

    // Accept different response shapes
    if (!Array.isArray(list)) {
        if (list && Array.isArray(list.records)) list = list.records;
        else if (list && Array.isArray(list.data)) list = list.data;
        else list = [];
    }

    if (list.length === 0) {
        body.innerHTML = '<div class="text-center text-muted small py-3">当天无流水</div>';
        headerTotal.innerText = '合计：￥0.00';
        return;
    }

    let total = 0;
    body.innerHTML = '';

    list.forEach(rawTx => {
        const tx = rawTx || {};
        // raw category/title may contain amount; prefer explicit numeric fields
        let rawCategory = (tx.category || tx.title || '') + '';
        rawCategory = rawCategory.trim();

        // time/remark
        const time = (tx.time || tx.t || '') + '';
        const remark = (tx.remark || tx.note || '') + '';

        // try explicit numeric amount fields first
        let amountVal = null;
        if (tx.amount !== undefined && tx.amount !== null) amountVal = Number(tx.amount);
        else if (tx.amt !== undefined && tx.amt !== null) amountVal = Number(tx.amt);
        else if (tx.value !== undefined && tx.value !== null) amountVal = Number(tx.value);

        let hasNumeric = Number.isFinite(amountVal);

        // if not found, try to extract trailing amount from rawCategory
        let cleanedType = rawCategory;
        if (!hasNumeric && rawCategory) {
            // match patterns like "[住房] -166.00", "餐饮 -12.5", "住房+200" etc.
            const m = rawCategory.match(/^(.*?)\s*[￥¥]?\s*([+-]?\d+(?:\.\d+)?)(?:\s*$)/);
            if (m) {
                cleanedType = (m[1] || '').trim();
                const parsed = Number(m[2]);
                if (Number.isFinite(parsed)) {
                    amountVal = parsed;
                    hasNumeric = true;
                }
            }
        }

        // final typeText: strip surrounding brackets if present
        let typeText = (cleanedType || '').replace(/^\s*\[(.*)\]\s*$/, '$1').trim();
        if (!typeText && rawCategory) {
            // if cleaning removed everything, fall back to original without numbers
            typeText = rawCategory.replace(/[\[\]]/g, '').replace(/[+-]?\d+(?:\.\d+)?/, '').trim();
        }

        // format amount text
        let amountText = '';
        if (hasNumeric) {
            amountText = (amountVal >= 0 ? '+' : '-') + Math.abs(amountVal).toFixed(2);
        } else {
            amountText = tx.amountText || '';
        }

        // build DOM
        const item = document.createElement('div');
        item.className = 'tx-item' + (hasNumeric ? (amountVal >= 0 ? ' positive' : ' negative') : '');

        const left = document.createElement('div');
        left.className = 'left';
        // determine icon class by id or name; fall back to arrow/circle
        let iconClass = '';
        const catId = tx.categoryId || tx.catId || tx.cid || (tx.category && (/^\d+$/.test(tx.category) ? tx.category : null));
        if (catId && ICON_MAP[String(catId)]) iconClass = ICON_MAP[String(catId)];
        else {
            const nm = (typeText || cleanedType || rawCategory || '').toString();
            for (const k in NAME_MAP) {
                if (k && nm.indexOf(k) !== -1) {
                    iconClass = NAME_MAP[k];
                    break;
                }
            }
        }
        if (!iconClass) {
            if (hasNumeric) iconClass = amountVal >= 0 ? 'fa-arrow-up' : 'fa-arrow-down';
            else iconClass = 'fa-circle';
        }
        const iconHtml = `<i class="fas ${iconClass}"></i>`;
        left.innerHTML = `<div class="avatar">${iconHtml}</div><div class="meta"><div class="type">${typeText}</div><div class="time-remark">${time} ${remark}</div></div>`;

        const right = document.createElement('div');
        right.className = 'amount';
        right.innerHTML = `<div class="fw-bold">${amountText || ''}</div>`;

        item.appendChild(left);
        item.appendChild(right);
        body.appendChild(item);

        if (hasNumeric) {
            total += Number(amountVal);
        }
    });

    headerTotal.innerText = '合计：' + (total >= 0 ? '+' : '') + '￥' + Math.abs(total).toFixed(2);
}

// initial boot
if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', renderCalendar);
else renderCalendar();
