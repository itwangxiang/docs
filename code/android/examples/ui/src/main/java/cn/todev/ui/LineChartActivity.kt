package cn.todev.ui

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.formatter.IValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import kotlinx.android.synthetic.main.activity_line_chart.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class LineChartActivity : AppCompatActivity() {

    private val mLineChartDateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    private var mLineChartCount = 120
    private var mLineChartRefreshTime = System.currentTimeMillis()
    private var mLineChartValues = mutableMapOf<Long, Int>()
    private lateinit var mLineDataSetTime: LineDataSet
    private lateinit var mLineDataSetData: LineDataSet

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_line_chart)

        initLinerChart()

        btnTwo.setOnClickListener {
            setLinerChartXCount(60 * 2)
        }

        btnTen.setOnClickListener {
            setLinerChartXCount(60 * 10)
        }

        btn30.setOnClickListener {
            setLinerChartXCount(60 * 30)
        }

        btn60.setOnClickListener {
            setLinerChartXCount(60 * 60)
        }
    }

    private fun initLinerChart() {
        chart.description = null //设置描写
        chart.legend.isEnabled = false //设置图例关
        chart.setDrawBorders(false) //设置是否显示边界
        chart.setBackgroundColor(Color.WHITE) //设置背景色

        //设置触摸(关闭影响下面3个属性)
        chart.setTouchEnabled(true)
        //设置是否可以拖拽
        chart.isDragEnabled = true
        //设置是否可以缩放
        chart.setScaleEnabled(false)
        chart.isScaleXEnabled = true
        //设置是否能扩大扩小
        chart.setPinchZoom(false)

        chart.marker = MyMarkerView(this, R.layout.ui_marker_view).apply { chartView = chart }

        //X轴
        chart.xAxis.run {
            //设置竖网格
            setDrawGridLines(false)
            //设置X轴线
            setDrawAxisLine(false)
            //设置X轴文字在底部显示
            position = XAxis.XAxisPosition.BOTTOM
            //设置X轴文字
            textColor = Color.parseColor("#434B61")
            textSize = 9f
            //设置X轴避免图表或屏幕的边缘的第一个和最后一个轴中的标签条目被裁剪
            setAvoidFirstLastClipping(true)

            //设置X轴值
            valueFormatter = IAxisValueFormatter { value, _ ->
                mLineChartDateFormat.format(mLineChartRefreshTime - (mLineChartCount - value.toInt()) * 1000)
            }
        }

        //Y轴(左)
        chart.axisLeft.run {
            //设置Y轴线
            setDrawAxisLine(false)
            //设置Y轴文字在内部显示
            setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
            //设置Y轴文字
            textColor = Color.parseColor("#434B61")
            textSize = 12f
            valueFormatter = IAxisValueFormatter { value, _ -> String.format("%.1f", value / 10) }

            axisMinimum = 301f
            axisMaximum = 429f
            enableGridDashedLine(5f, 2f, 0f)
            addLimitLine(LimitLine(385f).apply {
                lineColor = Color.parseColor("#FF3912")
                enableDashedLine(10f, 10f, 0f)
            })
            addLimitLine(LimitLine(370f).apply {
                lineColor = Color.parseColor("#00E69D")
                enableDashedLine(10f, 10f, 0f)
            })
        }

        //Y轴(右)
        chart.axisRight.isEnabled = false

        //设置时间标签
        mLineDataSetTime = LineDataSet(mutableListOf(), null)
        //设置数据标签
        mLineDataSetData = LineDataSet(mutableListOf(), null)
                .apply {
                    valueFormatter = IValueFormatter { value, _, _, _ -> String.format("%.1f", value / 10) }
                    color = Color.parseColor("#FFA73A")
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                    lineWidth = 1.5f
                    setDrawCircles(false)
                }

        chart.data = LineData(mLineDataSetTime, mLineDataSetData).apply {
            isHighlightEnabled = true
        }

        //设置默认数据
        setLinerChartXCount(120)

        GlobalScope.launch {
            while (true) {
                delay(1000)

                runOnUiThread {
                    val data = Random().nextInt(399 - 361) + 361
                    addDateAndRefreshLinerChart(data)
                }
            }
        }
    }

    private fun setLinerChartXCount(count: Int) {
        mLineChartCount = count
        mLineChartRefreshTime = System.currentTimeMillis()

        mLineDataSetTime.clear()
        mLineDataSetData.clear()

        for (i in 0..mLineChartCount) {
            mLineDataSetTime.addEntry(Entry(i.toFloat(), 0f))
            val time = mLineChartRefreshTime / 1000 - (mLineChartCount - i)
            if (mLineChartValues.containsKey(time)) {
                mLineDataSetData.addEntry(Entry(i.toFloat(), mLineChartValues[time]!!.toFloat(), time))
            }
        }

        mLineDataSetTime.notifyDataSetChanged()
        mLineDataSetData.notifyDataSetChanged()
        chart.data.notifyDataChanged()
        chart.notifyDataSetChanged()
        chart.highlightValues(null)
    }

    private fun addDateAndRefreshLinerChart(data: Int) {
        mLineChartValues[System.currentTimeMillis() / 1000] = data


        var highlightTime = 0L
        var highlightNewX = 0F
        chart.highlighted?.takeIf { !it.isNullOrEmpty() }?.run {
            val highlight = this[0]
            mLineDataSetData.getEntriesForXValue(highlight.x)?.takeIf { !it.isNullOrEmpty() }?.run {
                highlightTime = this[0].data as Long
            }
        }

        mLineChartRefreshTime = System.currentTimeMillis()
        mLineDataSetData.clear()
        for (i in 0..mLineChartCount) {
            val time = mLineChartRefreshTime / 1000 - (mLineChartCount - i)
            if (mLineChartValues.containsKey(time)) {
                mLineDataSetData.addEntry(Entry(i.toFloat(), mLineChartValues[time]!!.toFloat(), time))
                if (time == highlightTime) highlightNewX = i.toFloat()
            }
        }

        if (highlightNewX <= 0) {
            chart.highlightValues(null)
        } else {
            chart.highlightValues(arrayOf(Highlight(highlightNewX, 1, 1)))
        }
    }

}