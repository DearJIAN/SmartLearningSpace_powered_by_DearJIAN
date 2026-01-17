<template>
  <div ref="chartRef" style="width: 100%; height: 350px;"></div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import axios from 'axios'

const chartRef = ref(null)
let chartInstance = null

// 封装获取数据的逻辑
const fetchData = async (roomId) => {
  if (!chartInstance) return
  if (!roomId) {
    console.warn('fetchData: roomId is missing')
    return
  }

  try {
    const response = await axios.get('http://127.0.0.1:8080/api/stats/trend', {
      params: { roomId: roomId }
    })
    
    if (response.data.code === 200 && response.data.data) {
      const trendData = response.data.data
      
      // 提取时间点和专注度数据
      const timePoints = trendData.map(item => item.time_point)
      const focusValues = trendData.map(item => item.avg_focus)
      
      // 更新图表数据
      chartInstance.setOption({
        xAxis: {
          data: timePoints
        },
        series: [
          {
            data: focusValues
          }
        ]
      })
    } else {
      // 数据为空时的处理 (清空图表)
      chartInstance.setOption({
        xAxis: { data: [] },
        series: [{ data: [] }]
      })
      console.log('暂无趋势数据')
    }
  } catch (error) {
    // 网络错误处理
    console.error('获取趋势数据失败:', error)
  }
}

onMounted(async () => {
  if (chartRef.value) {
    // 初始化 ECharts 实例
    chartInstance = echarts.init(chartRef.value)

    // 初始化基础配置
    const option = {
      title: {
        text: '专注度趋势',
        left: 'center',
        textStyle: {
          fontSize: 16,
          fontWeight: 'normal'
        }
      },
      tooltip: {
        trigger: 'axis',
        formatter: '{b}<br/>专注度: {c}'
      },
      grid: {
        left: '5%',
        right: '5%',
        bottom: '10%',
        top: '15%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: [],
        axisLine: {
          lineStyle: {
            color: '#999'
          }
        }
      },
      yAxis: {
        type: 'value',
        min: 0,
        max: 1,
        axisLabel: {
          formatter: '{value}'
        },
        axisLine: {
          lineStyle: {
            color: '#999'
          }
        },
        splitLine: {
          lineStyle: {
            type: 'dashed'
          }
        }
      },
      series: [
        {
          name: '专注度',
          type: 'line',
          smooth: true,
          data: [],
          itemStyle: {
            color: '#409EFF'
          },
          lineStyle: {
            width: 2,
            color: '#409EFF'
          },
          areaStyle: {
            color: {
              type: 'linear',
              x: 0,
              y: 0,
              x2: 0,
              y2: 1,
              colorStops: [
                {
                  offset: 0,
                  color: 'rgba(64, 158, 255, 0.3)'
                },
                {
                  offset: 1,
                  color: 'rgba(64, 158, 255, 0.05)'
                }
              ]
            }
          }
        }
      ]
    }

    // 先设置初始配置
    chartInstance.setOption(option)

    // 调用获取数据的函数 (默认加载 101 教室)
    await fetchData(101)

    // 监听窗口大小变化
    window.addEventListener('resize', handleResize)
  }
})

const handleResize = () => {
  if (chartInstance) {
    chartInstance.resize()
  }
}

onUnmounted(() => {
  // 清理资源
  if (chartInstance) {
    window.removeEventListener('resize', handleResize)
    chartInstance.dispose()
    chartInstance = null
  }
})

// 暴露 fetchData 方法给父组件调用
defineExpose({
  fetchData
})
</script>

<style scoped>
/* 可以添加额外样式 */
</style>
