package com.example.foodorderapp.ui.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.*;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.foodorderapp.R;
import com.example.foodorderapp.data.model.ReportStats;
import com.example.foodorderapp.data.repository.ReportRepository;
import com.example.foodorderapp.ui.admin.ReportViewModel;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.chip.ChipGroup;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReportFragment extends Fragment {

    private ReportViewModel viewModel;

    // Views
    private ProgressBar progressBar;
    private TextView    tvTotalOrders, tvRevenue, tvDelivered, tvCancelled;
    private BarChart    barChart;
    private PieChart    pieChart;
    private LinearLayout layoutTopRestaurants, paymentLegend;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_report, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ReportViewModel.class);

        bindViews(view);
        setupPeriodChips(view);
        setupBarChart();
        setupPieChart();
        observeData();
    }

    // ── Bind views ────────────────────────────────────────────────
    private void bindViews(View v) {
        progressBar           = v.findViewById(R.id.progress_bar);
        tvTotalOrders         = v.findViewById(R.id.tv_total_orders);
        tvRevenue             = v.findViewById(R.id.tv_revenue);
        tvDelivered           = v.findViewById(R.id.tv_delivered);
        tvCancelled           = v.findViewById(R.id.tv_cancelled);
        barChart              = v.findViewById(R.id.bar_chart);
        pieChart              = v.findViewById(R.id.pie_chart);
        layoutTopRestaurants  = v.findViewById(R.id.layout_top_restaurants);
        paymentLegend         = v.findViewById(R.id.payment_legend);

        v.findViewById(R.id.btn_export).setOnClickListener(btn ->
                Toast.makeText(requireContext(),
                        "Tính năng xuất CSV đang phát triển",
                        Toast.LENGTH_SHORT).show()
        );
    }

    // ── Filter kỳ ────────────────────────────────────────────────
    private void setupPeriodChips(View view) {
        ChipGroup chips = view.findViewById(R.id.chip_period);
        chips.setOnCheckedStateChangeListener((group, ids) -> {
            if (ids.isEmpty()) return;
            int id = ids.get(0);
            if      (id == R.id.chip_today) viewModel.loadReport(ReportRepository.Period.TODAY);
            else if (id == R.id.chip_week)  viewModel.loadReport(ReportRepository.Period.WEEK);
            else if (id == R.id.chip_month) viewModel.loadReport(ReportRepository.Period.MONTH);
        });
    }

    // ════════════════════════════════════════════════════════════
    //  BIỂU ĐỒ CỘT — Doanh thu theo ngày
    // ════════════════════════════════════════════════════════════
    private void setupBarChart() {
        barChart.setDrawBarShadow(false);
        barChart.setDrawValueAboveBar(true);
        barChart.getDescription().setEnabled(false);
        barChart.setPinchZoom(false);
        barChart.setDrawGridBackground(false);
        barChart.getLegend().setEnabled(false);
        barChart.setNoDataText("Đang tải dữ liệu...");
        barChart.animateY(600);

        // Trục X
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextSize(10f);

        // Trục Y trái
        barChart.getAxisLeft().setDrawGridLines(true);
        barChart.getAxisLeft().setGridColor(0xFFEEEEEE);
        barChart.getAxisLeft().setValueFormatter(new ValueFormatter() {
            @Override public String getFormattedValue(float value) {
                if (value >= 1_000_000)
                    return String.format(Locale.US, "%.0fM", value / 1_000_000);
                if (value >= 1_000)
                    return String.format(Locale.US, "%.0fK", value / 1_000);
                return String.valueOf((int) value);
            }
        });

        // Trục Y phải ẩn
        barChart.getAxisRight().setEnabled(false);
    }

    private void renderBarChart(Map<String, Long> revenueByDay) {
        List<BarEntry>  entries = new ArrayList<>();
        List<String>    labels  = new ArrayList<>();
        int i = 0;

        for (Map.Entry<String, Long> entry : revenueByDay.entrySet()) {
            entries.add(new BarEntry(i, entry.getValue()));
            labels.add(entry.getKey()); // "dd/MM"
            i++;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Doanh thu");
        dataSet.setColor(0xFF007AFF);
        dataSet.setValueTextColor(0xFF1C1C1E);
        dataSet.setValueTextSize(9f);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override public String getBarLabel(BarEntry e) {
                long v = (long) e.getY();
                if (v == 0) return "";
                if (v >= 1_000_000)
                    return String.format(Locale.US, "%.0fM", v / 1_000_000.0);
                return String.format(Locale.US, "%.0fK", v / 1_000.0);
            }
        });

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.6f);

        barChart.getXAxis().setValueFormatter(
                new IndexAxisValueFormatter(labels));
        barChart.getXAxis().setLabelCount(labels.size());
        barChart.setData(data);
        barChart.invalidate();
    }

    // ════════════════════════════════════════════════════════════
    //  BIỂU ĐỒ TRÒN — Phương thức thanh toán
    // ════════════════════════════════════════════════════════════
    private void setupPieChart() {
        pieChart.getDescription().setEnabled(false);
        pieChart.setUsePercentValues(true);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(50f);
        pieChart.setTransparentCircleRadius(53f);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setEntryLabelColor(Color.WHITE);
        pieChart.setEntryLabelTextSize(11f);
        pieChart.getLegend().setEnabled(false);
        pieChart.animateY(700);
        pieChart.setNoDataText("Đang tải...");
    }

    private void renderPieChart(Map<String, Long> paymentCount) {
        List<PieEntry> entries = new ArrayList<>();
        long total = 0;
        for (Long v : paymentCount.values()) total += v;
        if (total == 0) return;

        // Màu cho từng phương thức
        int[] colors = { 0xFF854F0B, 0xFFEF9F27, 0xFF185FA5 };
        String[] keys = { "COD", "MoMo", "VNPay" };
        int[] usedColors = new int[keys.length];
        int ci = 0;

        for (int k = 0; k < keys.length; k++) {
            long v = paymentCount.getOrDefault(keys[k], 0L);
            if (v > 0) {
                entries.add(new PieEntry(v, keys[k]));
                usedColors[ci++] = colors[k];
            }
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        int[] finalColors = new int[ci];
        System.arraycopy(usedColors, 0, finalColors, 0, ci);
        dataSet.setColors(finalColors);
        dataSet.setSliceSpace(2f);
        dataSet.setValueTextSize(11f);
        dataSet.setValueTextColor(Color.WHITE);

        pieChart.setData(new PieData(dataSet));
        pieChart.invalidate();

        // Vẽ legend thủ công
        paymentLegend.removeAllViews();
        String[] methods = { "COD", "MoMo", "VNPay" };
        int[] legendColors = { 0xFF854F0B, 0xFFEF9F27, 0xFF185FA5 };

        for (int m = 0; m < methods.length; m++) {
            long count = paymentCount.getOrDefault(methods[m], 0L);
            if (count == 0) continue;
            long pct = total > 0 ? (count * 100 / total) : 0;

            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);

            LinearLayout.LayoutParams dotParams =
                    new LinearLayout.LayoutParams(14, 14);
            dotParams.setMargins(0, 0, 10, 0);
            View dot = new View(requireContext());
            dot.setBackgroundColor(legendColors[m]);
            dot.setLayoutParams(dotParams);

            TextView label = new TextView(requireContext());
            label.setText(methods[m] + " — " + count + " đơn (" + pct + "%)");
            label.setTextSize(12f);
            label.setTextColor(0xFF636366);

            row.addView(dot);
            row.addView(label);

            LinearLayout.LayoutParams rowParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
            rowParams.setMargins(0, 4, 0, 4);
            row.setLayoutParams(rowParams);
            paymentLegend.addView(row);
        }
    }

    // ════════════════════════════════════════════════════════════
    //  TOP 5 NHÀ HÀNG
    // ════════════════════════════════════════════════════════════
    private void renderTopRestaurants(
            List<ReportStats.RestaurantStat> list) {
        layoutTopRestaurants.removeAllViews();

        for (int i = 0; i < list.size(); i++) {
            ReportStats.RestaurantStat r = list.get(i);
            View row = buildRestaurantRow(i + 1, r);
            layoutTopRestaurants.addView(row);

            // Divider
            if (i < list.size() - 1) {
                View divider = new View(requireContext());
                LinearLayout.LayoutParams dp =
                        new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, 1);
                dp.setMargins(0, 6, 0, 6);
                divider.setLayoutParams(dp);
                divider.setBackgroundColor(0xFFEEEEEE);
                layoutTopRestaurants.addView(divider);
            }
        }
    }

    private View buildRestaurantRow(int rank,
                                    ReportStats.RestaurantStat r) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);

        // Rank badge
        TextView tvRank = new TextView(requireContext());
        tvRank.setText(String.valueOf(rank));
        tvRank.setTextSize(13f);
        tvRank.setTextColor(rank == 1 ? 0xFFFF9500 : 0xFF8A8A8E);
        tvRank.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams rp =
                new LinearLayout.LayoutParams(32, LinearLayout.LayoutParams.WRAP_CONTENT);
        tvRank.setLayoutParams(rp);

        // Tên + rating
        LinearLayout info = new LinearLayout(requireContext());
        info.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams ip =
                new LinearLayout.LayoutParams(0,
                        LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        info.setLayoutParams(ip);

        TextView tvName = new TextView(requireContext());
        tvName.setText(r.name);
        tvName.setTextSize(13f);
        tvName.setTextColor(0xFF1C1C1E);
        tvName.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView tvSub = new TextView(requireContext());
        tvSub.setText("★ " + String.format(Locale.US, "%.1f", r.rating)
                + "  ·  " + r.totalOrders + " đơn");
        tvSub.setTextSize(11f);
        tvSub.setTextColor(0xFF8A8A8E);

        info.addView(tvName);
        info.addView(tvSub);

        // Doanh thu
        TextView tvRev = new TextView(requireContext());
        tvRev.setText(formatRevenue(r.totalRevenue));
        tvRev.setTextSize(13f);
        tvRev.setTextColor(0xFF007AFF);
        tvRev.setTypeface(null, android.graphics.Typeface.BOLD);

        row.addView(tvRank);
        row.addView(info);
        row.addView(tvRev);
        return row;
    }

    // ════════════════════════════════════════════════════════════
    //  OBSERVE DATA
    // ════════════════════════════════════════════════════════════
    private void observeData() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            progressBar.setVisibility(
                    Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE);
        });

        viewModel.getReportStats().observe(getViewLifecycleOwner(), stats -> {
            if (stats == null) return;
            renderMetricCards(stats);
            if (stats.revenueByDay != null)
                renderBarChart(stats.revenueByDay);
            if (stats.paymentMethodCount != null)
                renderPieChart(stats.paymentMethodCount);
            if (stats.topRestaurants != null)
                renderTopRestaurants(stats.topRestaurants);
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), err -> {
            if (err != null && !err.isEmpty())
                Toast.makeText(requireContext(), err, Toast.LENGTH_SHORT).show();
        });
    }

    // ── Render 4 metric cards ─────────────────────────────────────
    private void renderMetricCards(ReportStats stats) {
        tvTotalOrders.setText(String.valueOf(stats.totalOrders));
        tvRevenue.setText(formatRevenue(stats.totalRevenue));
        tvDelivered.setText(String.valueOf(stats.deliveredOrders));
        tvCancelled.setText(String.valueOf(stats.cancelledOrders));
    }

    // ── Format helpers ────────────────────────────────────────────
    private String formatRevenue(long amount) {
        if (amount >= 1_000_000_000L)
            return String.format(Locale.US, "%.1fB đ", amount / 1_000_000_000.0);
        if (amount >= 1_000_000L)
            return String.format(Locale.US, "%.1fM đ", amount / 1_000_000.0);
        if (amount >= 1_000L)
            return String.format(Locale.US, "%.0fK đ", amount / 1_000.0);
        return NumberFormat.getNumberInstance(Locale.US).format(amount) + " đ";
    }
}