package com.example.bit_3;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import androidx.annotation.Nullable;
import android.util.Pair;


import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import androidx.annotation.Nullable;
import android.animation.ValueAnimator;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;



import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.AxisBase;



import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.text.ParseException;
import java.util.TreeMap;



import androidx.appcompat.app.AppCompatActivity;
public class ResiduosActivity extends AppCompatActivity {
    ImageButton atras;
    private HorizontalBarChart horizontalBarChart;
    private FirebaseFirestore db;
    private List<BarEntry> entries;

    private List<String> labels;

    private BarDataSet dataSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.residuos);

        db = FirebaseFirestore.getInstance();
        horizontalBarChart = findViewById(R.id.horizontalBarChart);

        setupChart();
        loadDataFromFirestore();

        entries = new ArrayList<>(); // Inicializa la lista de entradas
        labels = new ArrayList<>(); // Inicializa la lista de etiquetas

        atras = findViewById(R.id.atras_b);
        atras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ResiduosActivity.this, MenuActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setupChart() {
        horizontalBarChart.getDescription().setEnabled(false);

        Legend legend = horizontalBarChart.getLegend();
        legend.setEnabled(false); // This will hide the legend

        YAxis rightYAxis = horizontalBarChart.getAxisRight();
        rightYAxis.setDrawAxisLine(true);
        rightYAxis.setDrawGridLines(false);
        rightYAxis.setDrawLabels(true);

        rightYAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < labels.size()) {
                    return labels.get(index);
                }
                return "";
            }
        });

        // Other chart setup code...
    }




    private void loadDataFromFirestore() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -30);
        Date thirtyDaysAgo = calendar.getTime();

        db.collection("recolecciones")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Map<String, Integer> materialCounts = new HashMap<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String fechaRecoleccion = document.getString("fechaRecoleccion");
                            try {
                                Date recoleccionDate = sdf.parse(fechaRecoleccion);
                                if (recoleccionDate != null && !recoleccionDate.before(thirtyDaysAgo)) {
                                    Map<String, Object> materials = (Map<String, Object>) document.get("materiales");
                                    if (materials != null) {
                                        for (Object materialObj : materials.values()) {
                                            if (materialObj instanceof Map) {
                                                Map<String, Object> material = (Map<String, Object>) materialObj;
                                                String materialName = (String) material.get("nombre");
                                                Long count = (Long) material.get("cantidad");
                                                if (materialName != null && count != null) {
                                                    materialCounts.merge(materialName, count.intValue(), Integer::sum);
                                                }
                                            }
                                        }
                                    }
                                }
                            } catch (ParseException e) {
                                Log.e("ResiduosActivity", "Error parsing date", e);
                            }
                        }

                        // Log the materials and their counts
                        for (Map.Entry<String, Integer> entry : materialCounts.entrySet()) {
                            Log.d("MaterialCount", "Material: " + entry.getKey() + ", Count: " + entry.getValue());
                        }

                        updateChart(materialCounts);
                    } else {
                        Log.d("ResiduosActivity", "Error getting documents: ", task.getException());
                    }
                });
    }




    private void updateChart(Map<String, Integer> materialCounts) {
        // Convert the materialCounts map to a list of entries
        List<Map.Entry<String, Integer>> materialEntries = new ArrayList<>(materialCounts.entrySet());

        // Sort the entries alphabetically by key (material name)
        Collections.sort(materialEntries, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        });

        // Log the sorted material names and their counts
        for (Map.Entry<String, Integer> entry : materialEntries) {
            Log.d("SortedMaterialEntry", "Material: " + entry.getKey() + ", Count: " + entry.getValue());
        }

        // Create the chart entries and labels based on the sorted entries
        List<BarEntry> chartEntries = new ArrayList<>();
        labels = new ArrayList<>(); // Reset labels to match the sorted order
        int maxIndex = materialEntries.size() - 1;
        for (int i = 0; i <= maxIndex; i++) {
            // Invert the index for the bar entry
            int invertedIndex = maxIndex - i;
            Map.Entry<String, Integer> entry = materialEntries.get(i);
            chartEntries.add(new BarEntry(invertedIndex, entry.getValue()));
            labels.add(entry.getKey());
        }

        BarDataSet dataSet = new BarDataSet(chartEntries, "Material Counts");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        BarData data = new BarData(dataSet);
        horizontalBarChart.setData(data);

        YAxis rightAxis = horizontalBarChart.getAxisRight();
        rightAxis.setDrawLabels(true);
        rightAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        rightAxis.setGranularity(1f); // Set granularity to one to avoid skipping labels
        rightAxis.setGranularityEnabled(true); // Enable granularity

        rightAxis.setLabelCount(labels.size(), false); // Ensure all labels are shown

        horizontalBarChart.notifyDataSetChanged();
        horizontalBarChart.invalidate(); // Refresh the chart
    }








}
