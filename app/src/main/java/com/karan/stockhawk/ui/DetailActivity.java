package com.karan.stockhawk.ui;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.karan.stockhawk.R;
import com.karan.stockhawk.data.Contract;

import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import au.com.bytecode.opencsv.CSVReader;
import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class DetailActivity extends AppCompatActivity {

    @BindView(R.id.stock_name_textView)
    TextView stockTextView;
    @BindView(R.id.value_textView)
    TextView stockValueTextView;
    @BindView(R.id.stock_line_chart)
    LineChart stockHistoryChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        if (getIntent().hasExtra(MainActivity.EXTRA_STOCK_NAME_KEY)) {
            String stockSymbol = getIntent().getStringExtra(MainActivity.EXTRA_STOCK_NAME_KEY);
            String stockValue = getIntent().getStringExtra(MainActivity.EXTRA_STOCK_VALUE_KEY);
            stockTextView.setText(stockSymbol);
            stockTextView.setContentDescription(stockSymbol);
            stockValueTextView.setText(stockValue);
            stockValueTextView.setContentDescription(stockValue);
            drawHistoryGraph(stockSymbol);
        } else {
            Timber.e("Error fetching stock name from intent");
        }

    }

    private void drawHistoryGraph(String symbol) {

        stockHistoryChart.setNoDataText(getString(R.string.chart_no_data));

        //Fetching stock history data
        String stockHistoryData = null;
        Cursor cursor = getContentResolver().query(Contract.Quote.makeUriForStock(symbol), null, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
            stockHistoryData = cursor.getString(cursor.getColumnIndex(Contract.Quote.COLUMN_HISTORY));
            cursor.close();
        }

        //Parsing history data to a csv format to draw the graph
        List<String[]> tempList = new ArrayList<>();
        CSVReader reader = null;
        if (stockHistoryData != null) {
            reader = new CSVReader(new StringReader(stockHistoryData));
        }
        try {
            tempList.addAll(reader.readAll());
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<Entry> entries = new ArrayList<>();
        final List<Long> xAxisValues = new ArrayList<>();

        final int MATERIAL_GREEN = 0xFF00C853;

        for (int i = 0; i < tempList.size(); i++) {
            xAxisValues.add(Long.valueOf(tempList.get(i)[0]));
            entries.add(new Entry(i, Float.valueOf(tempList.get(i)[1])));
            LineDataSet lineDataSet = new LineDataSet(entries, symbol);

            lineDataSet.setDrawCircles(false);
            lineDataSet.setDrawFilled(true);
            lineDataSet.setFillColor(MATERIAL_GREEN);
            lineDataSet.setColor(MATERIAL_GREEN);
            lineDataSet.setLineWidth(1f);
            lineDataSet.setValueTextSize(8f);
            lineDataSet.setValueTextColor(Color.LTGRAY);
            lineDataSet.enableDashedHighlightLine(10f, 5f, 0f);
            lineDataSet.setHighlightLineWidth(1f);
            lineDataSet.setHighLightColor(Color.LTGRAY);
            lineDataSet.setFormLineWidth(1f);
            lineDataSet.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            lineDataSet.setFormSize(15.f);

            LineData lineData = new LineData(lineDataSet);

            XAxis xAxis = stockHistoryChart.getXAxis();
            xAxis.setValueFormatter(new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    Date date = new Date(xAxisValues.get(xAxisValues.size() - (int) value - 1));
                    return new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(date);
                }
            });

            Legend legend = stockHistoryChart.getLegend();
            legend.setEnabled(false);

            stockHistoryChart.animateXY(750, 750);
            stockHistoryChart.setKeepPositionOnRotation(true);
            stockHistoryChart.getDescription().setEnabled(false);
            stockHistoryChart.getAxisLeft().setTextColor(Color.WHITE);
            stockHistoryChart.getAxisRight().setTextColor(Color.WHITE);
            stockHistoryChart.getXAxis().setTextColor(Color.WHITE);
            stockHistoryChart.getLegend().setTextColor(Color.WHITE);
            stockHistoryChart.setContentDescription(getString(R.string.chart_desc));
            stockHistoryChart.setData(lineData);

        }

    }

}
