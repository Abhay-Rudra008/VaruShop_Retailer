package com.example.varushopretailer.activity

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import com.example.varushopretailer.R
import com.example.varushopretailer.databinding.ActivityAnalyticsBinding
import com.example.varushopretailer.helper.BaseActivity
import com.example.varushopretailer.modal.GraphData
import com.example.varushopretailer.viewmodal.AnalyticsViewModel
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AnalyticsActivity : BaseActivity() {

    private lateinit var binding: ActivityAnalyticsBinding
    private val viewModel: AnalyticsViewModel by viewModels()

    private var currentGraphTimeFilter = "MONTH"
    private var currentPieTimeFilter = "MONTH"

    private var lastDrawnPieData = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnalyticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupCharts()
        setupPieChart()
        setupFilters()
        observeViewModel()

        viewModel.loadGraphData(currentGraphTimeFilter, "REVENUE")
        viewModel.loadPieChartData(currentPieTimeFilter)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupFilters() {
        binding.chipGroupGraphTime.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            currentGraphTimeFilter = getFilterStringFromId(checkedIds.first())
            viewModel.loadGraphData(currentGraphTimeFilter, "REVENUE")
        }

        binding.chipGroupPieTime.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            currentPieTimeFilter = getFilterStringFromId(checkedIds.first())
            viewModel.loadPieChartData(currentPieTimeFilter)
        }
    }

    private fun getFilterStringFromId(chipId: Int): String {
        return when (chipId) {
            R.id.chipGraphTimeToday, R.id.chipPieTimeToday -> "TODAY"
            R.id.chipGraphTimeWeek, R.id.chipPieTimeWeek -> "WEEK"
            R.id.chipGraphTimeMonth, R.id.chipPieTimeMonth -> "MONTH"
            R.id.chipGraphTimeYear, R.id.chipPieTimeYear -> "YEAR"
            R.id.chipGraphTimeAll, R.id.chipPieTimeAll -> "ALL_TIME"
            else -> "MONTH"
        }
    }

    private fun setupCharts() {
        binding.lineChart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            setTouchEnabled(true)
            setScaleEnabled(false)

            axisRight.isEnabled = false
            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = Color.LTGRAY
                setDrawAxisLine(false)
                textColor = Color.GRAY
                axisMinimum = 0f
            }

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                setDrawAxisLine(false)
                textColor = Color.GRAY
                granularity = 1f
            }
        }
    }

    private fun setupPieChart() {
        binding.orderPieChart.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            holeRadius = 50f
            transparentCircleRadius = 55f
            setDrawEntryLabels(false)

            legend.apply {
                verticalAlignment = Legend.LegendVerticalAlignment.CENTER
                horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                orientation = Legend.LegendOrientation.VERTICAL
                setDrawInside(false)
                textSize = 13f
                textColor = Color.DKGRAY
            }
        }
    }

    @SuppressLint("DefaultLocale", "SetTextI18n")
    private fun observeViewModel() {

        viewModel.graphState.observe(this) { state ->
            state?.let {
                binding.tvAnalyticsTotal.text = "₹${formatCurrency(it.totalValue)}"
                displayLineChart(it.graphDataList)
            }
        }

        viewModel.pieState.observe(this) { state ->
            state?.let {
                displayPieChart(it)
            }
        }

        viewModel.isLoading.observe(this) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }
    }

    private fun displayLineChart(dataList: List<GraphData>) {
        if (dataList.isEmpty()) {
            binding.lineChart.clear()
            binding.lineChart.setNoDataText("No revenue data available for this period")
            return
        }

        val entries = dataList.mapIndexed { index, data ->
            val yValue = data.value
            Entry(index.toFloat(), yValue)
        }

        val brandColor = ContextCompat.getColor(this, R.color.brand_primary)
        val dataSet = LineDataSet(entries, "Revenue").apply {
            mode = LineDataSet.Mode.CUBIC_BEZIER
            color = brandColor
            lineWidth = 3f
            setDrawCircles(true)
            setCircleColor(brandColor)
            circleRadius = 4f
            setDrawValues(false)
            setDrawFilled(true)
            fillAlpha = 50
            fillColor = brandColor
        }

        binding.lineChart.apply {
            data = LineData(dataSet)
            xAxis.valueFormatter = IndexAxisValueFormatter(dataList.map { it.label })
            xAxis.labelCount = dataList.size.coerceAtMost(5)
            animateX(800)
            invalidate()
        }
    }

    private fun displayPieChart(state: AnalyticsViewModel.PieState) {
        val newDataFingerprint =
            "${state.deliveredOrders}-${state.pendingOrders}-${state.cancelledOrders}-${state.returnedOrders}"

        if (lastDrawnPieData == newDataFingerprint) return
        lastDrawnPieData = newDataFingerprint

        val entries = ArrayList<PieEntry>()
        val colors = ArrayList<Int>()

        if (state.deliveredOrders > 0) {
            entries.add(PieEntry(state.deliveredOrders.toFloat(), "Delivered"))
            colors.add("#4CAF50".toColorInt())
        }
        if (state.pendingOrders > 0) {
            entries.add(PieEntry(state.pendingOrders.toFloat(), "Pending"))
            colors.add("#FFC107".toColorInt())
        }
        if (state.cancelledOrders > 0) {
            entries.add(PieEntry(state.cancelledOrders.toFloat(), "Cancelled"))
            colors.add("#F44336".toColorInt())
        }
        if (state.returnedOrders > 0) {
            entries.add(PieEntry(state.returnedOrders.toFloat(), "Returned"))
            colors.add("#9C27B0".toColorInt())
        }

        if (entries.isEmpty()) {
            binding.orderPieChart.clear()
            binding.orderPieChart.centerText = "No Orders"
            return
        }

        val dataSet = PieDataSet(entries, "").apply {
            this.colors = colors
            valueTextSize = 12f
            valueTextColor = Color.WHITE
            sliceSpace = 2f
            selectionShift = 5f
        }

        binding.orderPieChart.apply {
            data = PieData(dataSet)
            centerText = "Total\n${entries.sumOf { it.value.toDouble() }.toInt()}"
            setCenterTextSize(16f)
            animateY(1000, com.github.mikephil.charting.animation.Easing.EaseInOutQuad)
            invalidate()
        }
    }

    @SuppressLint("DefaultLocale")
    private fun formatCurrency(amount: String?): String {
        val value = amount?.toDoubleOrNull() ?: 0.0
        return String.format("%,.0f", value)
    }
}
