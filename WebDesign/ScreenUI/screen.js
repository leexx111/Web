const focusAgencies = [
  { name: "彩虹桥托儿所", riskText: "高风险", riskClass: "risk-high", riskType: "容量超限", area: "黄浦区", people: "132 / 120", camera: "82%", score: 58, coord: [121.4903, 31.2222] },
  { name: "阳光宝贝托育", riskText: "高风险", riskClass: "risk-high", riskType: "环境异常", area: "浦东新区", people: "76 / 80", camera: "77%", score: 61, coord: [121.5677, 31.2451] },
  { name: "乐芽托育点", riskText: "中风险", riskClass: "risk-mid", riskType: "设备离线", area: "闵行区", people: "48 / 60", camera: "69%", score: 73, coord: [121.3817, 31.1128] },
  { name: "金色童年", riskText: "中风险", riskClass: "risk-mid", riskType: "巡检待复核", area: "徐汇区", people: "98 / 110", camera: "91%", score: 82, coord: [121.4462, 31.1883] },
  { name: "启明星托育园", riskText: "低风险", riskClass: "risk-low", riskType: "运行正常", area: "静安区", people: "118 / 130", camera: "97%", score: 95, coord: [121.4534, 31.2363] }
];

const topRiskData = [
  { name: "彩虹桥托儿所", type: "容量超限", level: "高风险" },
  { name: "阳光宝贝托育", type: "环境异常", level: "高风险" },
  { name: "乐芽托育点", type: "设备离线", level: "中风险" }
];

const agencyDisplayCoords = {
  "彩虹桥托儿所": [121.485, 31.214],
  "阳光宝贝托育": [121.668, 31.226],
  "乐芽托育点": [121.418, 31.078],
  "金色童年": [121.442, 31.164],
  "启明星托育园": [121.458, 31.248]
};

const chartTheme = {
  text: "#e8f4ff",
  subText: "#88add8",
  axis: "rgba(132,178,242,.28)",
  split: "rgba(132,178,242,.12)",
  cyan: "#41f0ff",
  blue: "#2f79ff",
  green: "#6fffd2",
  yellow: "#ffb75c",
  red: "#ff8f70",
  labelLine: "rgba(137,174,218,.48)",
  pieBorder: "rgba(170,210,255,.14)"
};

const shanghaiGeoJsonUrls = [
  "https://geo.datav.aliyun.com/areas_v3/bound/310000_full.json"
];

let charts = {};
let focusIndex = 0;
let currentFocusIndex = 0;
let shanghaiMapReady = false;
let shanghaiMapMode = "echarts";
let entrySnapshot = {
  inCount: 632,
  outCount: 605,
  leaveCount: 3,
  visitCount: 26
};
const mapSpreadOffsets = [
  [-0.132, 0.094],
  [0.128, 0.082],
  [-0.108, -0.102],
  [0.114, -0.088],
  [0.008, 0.136]
];
const fallbackDistrictLayout = {
  "静安区": { x: 41, y: 24, w: 16, h: 12, r: 12 },
  "黄浦区": { x: 49, y: 36, w: 12, h: 10, r: 10 },
  "徐汇区": { x: 34, y: 42, w: 18, h: 14, r: 16 },
  "闵行区": { x: 23, y: 58, w: 26, h: 18, r: 18 },
  "浦东新区": { x: 58, y: 24, w: 24, h: 40, r: 18 }
};

function pad(num) { return String(num).padStart(2, "0"); }
function weekText(day) { return ["星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"][day]; }

function pointColorByRisk(riskClass) {
  if (riskClass === "risk-high") return "#ffb08e";
  if (riskClass === "risk-mid") return "#ffd07c";
  return "#6fffd2";
}

function getSpreadCoord(item, index) {
  return agencyDisplayCoords[item.name] || item.coord;
}

function buildMapRegions(activeArea) {
  const areas = [...new Set(focusAgencies.map((item) => item.area))];
  return areas.map((area) => {
    const isActive = area === activeArea;
    return {
      name: area,
      itemStyle: isActive ? {
        areaColor: "#1d5485",
        borderColor: "#ffd18a",
        borderWidth: 2.4,
        shadowBlur: 28,
        shadowColor: "rgba(255, 183, 92, 0.42)",
        shadowOffsetY: -10
      } : {
        areaColor: "#113459",
        borderColor: "#8ecbff",
        borderWidth: 1.2,
        shadowBlur: 12,
        shadowColor: "rgba(65, 240, 255, 0.12)"
      },
      label: isActive ? {
        show: true,
        color: "#fff3d4",
        fontSize: 16,
        fontWeight: 700,
        offset: [0, -8],
        backgroundColor: "rgba(8, 26, 48, 0.82)",
        borderColor: "rgba(255, 209, 138, 0.46)",
        borderWidth: 1,
        borderRadius: 6,
        padding: [4, 8],
        textBorderColor: "rgba(6, 20, 38, 0.9)",
        textBorderWidth: 2
      } : {
        show: true,
        color: "rgba(237,246,255,0.92)",
        fontSize: 11,
        textBorderColor: "rgba(6,22,42,0.85)",
        textBorderWidth: 2
      }
    };
  });
}

function updateTime() {
  const now = new Date();
  document.getElementById("currentTime").textContent = `${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`;
  document.getElementById("currentDate").textContent = `${now.getFullYear()}年${pad(now.getMonth() + 1)}月${pad(now.getDate())}日 ${weekText(now.getDay())}`;
}

function getCurrentTimeSlot(date = new Date()) {
  const hour = date.getHours();
  if (hour < 9) return "06:00-09:00";
  if (hour < 12) return "09:00-12:00";
  if (hour < 15) return "12:00-15:00";
  if (hour < 18) return "15:00-18:00";
  return "18:00-21:00";
}

function updateScreenScale() {
  const designWidth = 1920;
  const designHeight = 1080;
  const scale = Math.min(window.innerWidth / designWidth, window.innerHeight / designHeight);
  document.documentElement.style.setProperty("--screen-scale", Math.max(scale, 0.5).toFixed(4));
}

function animateStats() {
  document.querySelectorAll(".stat-value[data-target]").forEach((el) => {
    const target = Number(el.dataset.target) || 0;
    const duration = 1400;
    const start = performance.now();
    function tick(now) {
      const p = Math.min((now - start) / duration, 1);
      const value = Math.floor(target * (1 - Math.pow(1 - p, 3)));
      el.textContent = value.toLocaleString();
      if (p < 1) requestAnimationFrame(tick);
    }
    requestAnimationFrame(tick);
  });
}

function renderTopRiskList() {
  const box = document.getElementById("topRiskList");
  box.innerHTML = topRiskData.map((item, index) => `
    <div class="top-risk-card">
      <div class="rank">${index + 1}</div>
      <div class="name">${item.name}</div>
      <div class="meta"><span>${item.type}</span><span class="tag">${item.level}</span></div>
    </div>
  `).join("");
}

function axisCommon() {
  return {
    axisLine: { lineStyle: { color: chartTheme.axis } },
    axisLabel: { color: chartTheme.subText },
    axisTick: { show: false }
  };
}

async function initShanghaiMap() {
  const dom = document.getElementById("shanghaiMap");
  if (!dom) return;

  charts.shanghaiMap = echarts.init(dom);
  for (const url of shanghaiGeoJsonUrls) {
    try {
      const res = await fetch(url, { cache: "force-cache" });
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      const geoJson = await res.json();
      echarts.registerMap("shanghai", geoJson);
      shanghaiMapReady = true;
      shanghaiMapMode = "echarts";
      renderShanghaiMap(currentFocusIndex);
      return;
    } catch (error) {
      console.warn("Failed to load shanghai map source:", url, error);
    }
  }

  shanghaiMapMode = "fallback";
  renderFallbackMap(currentFocusIndex);
}

function renderShanghaiMap(currentIndex) {
  if (!shanghaiMapReady || !charts.shanghaiMap) return;

  const pointData = focusAgencies.map((item, idx) => ({
    name: item.name,
    value: [...getSpreadCoord(item, idx), item.score],
    riskClass: item.riskClass,
    riskType: item.riskType,
    area: item.area,
    camera: item.camera,
    people: item.people
  }));

  const focusItem = focusAgencies[currentIndex] || focusAgencies[0];
  const regions = buildMapRegions(focusItem.area);

  charts.shanghaiMap.setOption({
    animationDurationUpdate: 600,
    tooltip: {
      trigger: "item",
      formatter(params) {
        const data = params.data;
        if (!data) return params.name;
        return `${data.name}<br/>区域：${data.area}<br/>风险：${data.riskType}<br/>在园：${data.people}<br/>监控在线率：${data.camera}`;
      }
    },
    geo: {
      map: "shanghai",
      roam: false,
      zoom: 1.62,
      silent: true,
      center: [121.575, 31.17],
      itemStyle: {
        areaColor: "#113459",
        borderColor: "#8ecbff",
        borderWidth: 1.2,
        shadowColor: "rgba(65, 240, 255, 0.18)",
        shadowBlur: 20
      },
      emphasis: {
        itemStyle: {
          areaColor: "#18446f"
        }
      },
      regions,
      label: {
        show: true,
        color: "rgba(237,246,255,0.92)",
        fontSize: 11,
        textBorderColor: "rgba(6,22,42,0.85)",
        textBorderWidth: 2
      }
    },
    series: [
      {
        name: "托育机构",
        type: "scatter",
        coordinateSystem: "geo",
        data: pointData,
        symbol: "diamond",
        symbolSize(value, params) {
          const itemName = params && params.data ? params.data.name : "";
          return itemName === focusItem.name ? 18 : 14;
        },
        itemStyle: {
          color(params) {
            return pointColorByRisk(params.data.riskClass);
          },
          shadowBlur: 18,
          shadowColor: "rgba(173, 211, 255, 0.72)",
          borderColor: "#f0f7ff",
          borderWidth: 1
        },
        label: {
          show: true,
          formatter(params) {
            return params.name === focusItem.name ? `{focus|${params.name}}` : `{normal|${params.name}}`;
          },
          rich: {
            focus: {
              color: "#fff7df",
              fontSize: 18,
              fontWeight: 700,
              padding: [7, 12],
              backgroundColor: "rgba(18, 48, 82, 0.96)",
              borderColor: "rgba(255, 209, 138, 0.58)",
              borderWidth: 1,
              borderRadius: 8,
              textShadowColor: "rgba(255, 183, 92, 0.35)",
              textShadowBlur: 10
            },
            normal: {
              color: "#f3f9ff",
              fontSize: 13,
              padding: [4, 8],
              backgroundColor: "rgba(7, 24, 45, 0.68)",
              borderColor: "rgba(133, 194, 255, 0.4)",
              borderWidth: 1,
              borderRadius: 6
            }
          },
          position: "right",
          distance: 10
        },
        zlevel: 2
      },
      {
        name: "当前关注",
        type: "effectScatter",
        coordinateSystem: "geo",
        data: [{
          name: focusItem.name,
          value: [...getSpreadCoord(focusItem, currentIndex), focusItem.score],
          area: focusItem.area,
          riskType: focusItem.riskType,
          people: focusItem.people,
          camera: focusItem.camera
        }],
        symbolSize: 24,
        rippleEffect: {
          period: 4,
          scale: 5,
          brushType: "stroke"
        },
        itemStyle: {
          color: "#e8f3ff",
          borderColor: "#ffd18a",
          borderWidth: 2,
          shadowBlur: 28,
          shadowColor: "rgba(255, 183, 92, 0.65)"
        },
        label: {
          show: true,
          formatter: "重点园所",
          color: "#fff1d5",
          fontSize: 13,
          fontWeight: 700,
          position: "top",
          backgroundColor: "rgba(8, 24, 42, 0.82)",
          borderColor: "rgba(255, 209, 138, 0.42)",
          borderWidth: 1,
          borderRadius: 999,
          padding: [4, 10],
          textBorderColor: "rgba(7,22,40,0.9)",
          textBorderWidth: 2
        },
        zlevel: 3
      }
    ]
  }, true);
}

function renderFallbackMap(currentIndex) {
  const dom = document.getElementById("shanghaiMap");
  if (!dom) return;

  const focusItem = focusAgencies[currentIndex] || focusAgencies[0];
  const districtHtml = Object.entries(fallbackDistrictLayout).map(([name, layout]) => {
    const isActive = name === focusItem.area;
    return `
      <div
        class="fallback-district${isActive ? " active" : ""}"
        style="left:${layout.x}%;top:${layout.y}%;width:${layout.w}%;height:${layout.h}%;border-radius:${layout.r}px;"
      >
        <span>${name}</span>
      </div>
    `;
  }).join("");

  const pointHtml = focusAgencies.map((item) => {
    const district = fallbackDistrictLayout[item.area];
    const x = district.x + district.w / 2;
    const y = district.y + district.h / 2;
    const isActive = item.name === focusItem.name;
    return `
      <div class="fallback-point ${item.riskClass}${isActive ? " active" : ""}" style="left:${x}%;top:${y}%;">
        <i></i>
        <label>${item.name}</label>
      </div>
    `;
  }).join("");

  dom.innerHTML = `
    <div class="map-fallback-shell" aria-label="上海机构分布示意图">
      <div class="map-fallback-note">已切换为本地示意地图</div>
      <div class="fallback-outline"></div>
      ${districtHtml}
      ${pointHtml}
    </div>
  `;
}

function initStatusPie() {
  const dom = document.getElementById("statusPie");
  if (!dom) return;
  charts.statusPie = echarts.init(dom);
  charts.statusPie.setOption({
    color: [chartTheme.green, chartTheme.cyan, chartTheme.yellow, chartTheme.red],
    tooltip: { trigger: "item" },
    legend: {
      bottom: 0,
      textStyle: { color: chartTheme.subText, fontSize: 11 },
      itemWidth: 9,
      itemHeight: 9,
      itemGap: 10
    },
    series: [{
      type: "pie",
      radius: ["44%", "76%"],
      center: ["50%", "40%"],
      avoidLabelOverlap: true,
      label: { show: false },
      labelLine: { show: false },
      itemStyle: { borderColor: chartTheme.pieBorder, borderWidth: 3 },
      data: [
        { value: 98, name: "正常运行" },
        { value: 16, name: "预警机构" },
        { value: 6, name: "高风险" },
        { value: 8, name: "设备离线" }
      ]
    }]
  });
}

function initEntryBar() {
  const dom = document.getElementById("entryBar");
  if (!dom) return;
  charts.entryBar = echarts.init(dom);
  updateEntryBar();
}

function updateEntryBar() {
  if (!charts.entryBar) return;
  const currentSlot = getCurrentTimeSlot();
  const categories = ["在园", "离园", "请假"];
  const values = [entrySnapshot.inCount, entrySnapshot.outCount, entrySnapshot.leaveCount];
  const colors = [
    new echarts.graphic.LinearGradient(0, 0, 0, 1, [
      { offset: 0, color: "#8ff7ff" },
      { offset: 1, color: "#49b3ff" }
    ]),
    new echarts.graphic.LinearGradient(0, 0, 0, 1, [
      { offset: 0, color: "#7fb8ff" },
      { offset: 1, color: "#2a67ff" }
    ]),
    new echarts.graphic.LinearGradient(0, 0, 0, 1, [
      { offset: 0, color: "#ffd18a" },
      { offset: 1, color: "#ff8f70" }
    ])
  ];

  charts.entryBar.setOption({
    title: {
      text: `当前时段 ${currentSlot}`,
      left: "center",
      top: 2,
      textStyle: {
        color: chartTheme.text,
        fontSize: 11,
        fontWeight: 600
      }
    },
    grid: { left: 20, right: 12, top: 28, bottom: 18 },
    tooltip: {
      trigger: "axis",
      axisPointer: { type: "shadow" },
      formatter(params) {
        const item = params[0];
        return `${item.axisValue}<br/>当前时段：${currentSlot}<br/>数量：${item.value}`;
      }
    },
    xAxis: {
      type: "category",
      data: categories,
      axisLabel: { color: chartTheme.subText, fontSize: 11 },
      axisLine: { lineStyle: { color: chartTheme.axis } },
      axisTick: { show: false }
    },
    yAxis: {
      type: "value",
      splitLine: { lineStyle: { color: chartTheme.split } },
      axisLabel: { color: chartTheme.subText, fontSize: 10 }
    },
    series: [{
      type: "bar",
      barWidth: 24,
      data: values.map((value, index) => ({
        value,
        itemStyle: {
          color: colors[index],
          borderRadius: [8, 8, 0, 0],
          shadowBlur: 14,
          shadowColor: "rgba(65, 240, 255, 0.18)"
        }
      })),
      label: {
        show: true,
        position: "top",
        color: chartTheme.text,
        fontSize: 11,
        fontWeight: 700
      }
    }]
  });

  const slotLabel = document.getElementById("entryCurrentSlot");
  if (slotLabel) slotLabel.textContent = `当前时段 ${currentSlot}`;
}

function initWarningTrend() {
  const dom = document.getElementById("warningTrend");
  if (!dom) return;
  charts.warningTrend = echarts.init(dom);
  charts.warningTrend.setOption({
    tooltip: { trigger: "axis" },
    grid: { left: 34, right: 14, top: 22, bottom: 24 },
    xAxis: { type: "category", data: ["周一", "周二", "周三", "周四", "周五", "周六", "周日"], axisLabel: { color: chartTheme.subText, fontSize: 10 }, axisLine: { lineStyle: { color: chartTheme.axis } }, axisTick: { show: false } },
    yAxis: { type: "value", splitLine: { lineStyle: { color: chartTheme.split } }, axisLabel: { color: chartTheme.subText, fontSize: 10 } },
    series: [{
      type: "line",
      smooth: true,
      data: [13, 16, 14, 19, 17, 21, 15],
      symbolSize: 6,
      lineStyle: { width: 3, color: chartTheme.yellow },
      itemStyle: { color: chartTheme.yellow, borderColor: "#fff2d4", borderWidth: 2 },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: "rgba(255,183,92,.28)" },
          { offset: 1, color: "rgba(255,183,92,.04)" }
        ])
      }
    }]
  });
}

function initRegionBar() {
  const dom = document.getElementById("regionBar");
  if (!dom) return;
  charts.regionBar = echarts.init(dom);
  charts.regionBar.setOption({
    tooltip: { trigger: "axis", axisPointer: { type: "shadow" } },
    grid: { left: 34, right: 10, top: 20, bottom: 26 },
    xAxis: { type: "category", data: ["黄浦区", "徐汇区", "浦东新区", "静安区", "闵行区"], axisLabel: { color: chartTheme.subText, fontSize: 10, interval: 0 }, axisLine: { lineStyle: { color: chartTheme.axis } }, axisTick: { show: false } },
    yAxis: { type: "value", min: 60, max: 100, splitLine: { lineStyle: { color: chartTheme.split } }, axisLabel: { color: chartTheme.subText, fontSize: 10 } },
    series: [{
      type: "bar",
      barWidth: 18,
      data: [88, 90, 86, 92, 89],
      itemStyle: {
        borderRadius: [8, 8, 0, 0],
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: chartTheme.yellow }, { offset: 1, color: chartTheme.blue }])
      }
    }]
  });
}

function initRiskTypeBar() {
  const dom = document.getElementById("riskTypeBar");
  if (!dom) return;
  charts.riskTypeBar = echarts.init(dom);
  charts.riskTypeBar.setOption({
    grid: { left: 62, right: 18, top: 8, bottom: 8 },
    tooltip: { trigger: "axis", axisPointer: { type: "shadow" } },
    xAxis: { type: "value", splitLine: { lineStyle: { color: chartTheme.split } }, axisLabel: { color: chartTheme.subText, fontSize: 10 } },
    yAxis: { type: "category", data: ["容量预警", "环境异常", "设备离线", "晨检异常", "视频离线"], axisLine: { show: false }, axisTick: { show: false }, axisLabel: { color: chartTheme.subText, fontSize: 10 } },
    series: [{
      type: "bar",
      barWidth: 10,
      data: [9, 7, 6, 4, 3],
      itemStyle: {
        borderRadius: [0, 10, 10, 0],
        color: new echarts.graphic.LinearGradient(1, 0, 0, 0, [{ offset: 0, color: chartTheme.red }, { offset: 1, color: chartTheme.yellow }])
      },
      label: { show: true, position: "right", color: chartTheme.text, fontSize: 10 }
    }]
  });
}

function initClosurePie() {
  const dom = document.getElementById("closurePie");
  if (!dom) return;
  charts.closurePie = echarts.init(dom);
  charts.closurePie.setOption({
    color: [chartTheme.green, chartTheme.cyan, chartTheme.red],
    tooltip: { trigger: "item" },
    legend: {
      left: "center",
      bottom: 0,
      textStyle: { color: chartTheme.subText, fontSize: 10 },
      itemWidth: 9,
      itemHeight: 9,
      itemGap: 12
    },
    series: [{
      type: "pie",
      radius: ["44%", "76%"],
      center: ["46%", "42%"],
      avoidLabelOverlap: true,
      label: { show: false },
      labelLine: { show: false },
      itemStyle: { borderColor: chartTheme.pieBorder, borderWidth: 3 },
      data: [
        { value: 35, name: "已处置" },
        { value: 5, name: "复核中" },
        { value: 2, name: "待派单" }
      ]
    }]
  });
}

function setFocusAgency(index) {
  const item = focusAgencies[index];
  if (!item) return;
  currentFocusIndex = index;

  document.getElementById("focusAgencyName").textContent = item.name;
  const levelEl = document.getElementById("focusRiskLevel");
  levelEl.textContent = item.riskText;
  levelEl.className = `risk-tag ${item.riskClass}`;
  document.getElementById("focusRiskType").textContent = item.riskType;
  document.getElementById("focusArea").textContent = item.area;
  document.getElementById("focusPeople").textContent = item.people;
  document.getElementById("focusCamera").textContent = item.camera;
  document.getElementById("focusScore").textContent = item.score;
  document.getElementById("focusScoreBar").style.width = `${item.score}%`;
  document.getElementById("focusTime").textContent = document.getElementById("currentTime").textContent;
  document.getElementById("mapFocusDistrict").textContent = item.area;
  document.getElementById("mapFocusAgency").textContent = item.name;

  if (shanghaiMapMode === "fallback") {
    renderFallbackMap(index);
  } else {
    renderShanghaiMap(index);
  }
}

function cycleFocusAgency() {
  setFocusAgency(focusIndex);
  focusIndex = (focusIndex + 1) % focusAgencies.length;
}

function randomKpis() {
  document.getElementById("kpiVideo").textContent = String(120 + Math.floor(Math.random() * 12));
  document.getElementById("kpiHandle").textContent = String(34 + Math.floor(Math.random() * 8));
  document.getElementById("kpiRectify").textContent = `${84 + Math.floor(Math.random() * 5)}%`;
  document.getElementById("kpiOnline").textContent = `${(95.8 + Math.random() * 1.1).toFixed(1)}%`;
  document.getElementById("kpiSafety").textContent = (90.8 + Math.random() * 1.4).toFixed(1);
  entrySnapshot.inCount = 620 + Math.floor(Math.random() * 25);
  entrySnapshot.outCount = 590 + Math.floor(Math.random() * 25);
  entrySnapshot.leaveCount = 2 + Math.floor(Math.random() * 3);
  entrySnapshot.visitCount = 22 + Math.floor(Math.random() * 8);
  updateEntryBar();
}

function bindResize() {
  window.addEventListener("resize", () => {
    updateScreenScale();
    Object.values(charts).forEach((chart) => chart && chart.resize());
  });

  if (typeof ResizeObserver !== "undefined") {
    const observer = new ResizeObserver(() => {
      Object.values(charts).forEach((chart) => chart && chart.resize());
    });
    Object.keys(charts).forEach((key) => {
      const chart = charts[key];
      if (chart && chart.getDom()) observer.observe(chart.getDom());
    });
  }
}

function init() {
  updateScreenScale();
  updateTime();
  setInterval(updateTime, 1000);
  animateStats();
  renderTopRiskList();

  initStatusPie();
  initEntryBar();
  initWarningTrend();
  initRegionBar();
  initRiskTypeBar();
  initClosurePie();
  initShanghaiMap();

  cycleFocusAgency();
  setInterval(cycleFocusAgency, 5000);
  setInterval(randomKpis, 4000);
  bindResize();
  setTimeout(() => Object.values(charts).forEach((chart) => chart && chart.resize()), 180);
}

document.addEventListener("DOMContentLoaded", init);
