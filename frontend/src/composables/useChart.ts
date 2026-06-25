import { ref, onMounted, onBeforeUnmount, type Ref } from 'vue'
import * as echarts from 'echarts'
import type { EChartsOption } from 'echarts'

export function useChart(chartRef: Ref<HTMLElement | null>, options: EChartsOption) {
  const chartInstance = ref<echarts.ECharts | null>(null)

  const initChart = () => {
    if (!chartRef.value) return
    chartInstance.value = echarts.init(chartRef.value)
    chartInstance.value.setOption(options)
  }

  const updateChart = (newOptions: EChartsOption) => {
    chartInstance.value?.setOption(newOptions, true)
  }

  const resizeChart = () => {
    chartInstance.value?.resize()
  }

  onMounted(() => {
    initChart()
    window.addEventListener('resize', resizeChart)
  })

  onBeforeUnmount(() => {
    window.removeEventListener('resize', resizeChart)
    chartInstance.value?.dispose()
  })

  return { chartInstance, initChart, updateChart, resizeChart }
}
