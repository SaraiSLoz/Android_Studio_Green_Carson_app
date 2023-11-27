package com.example.bit_3;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import androidx.annotation.Nullable;
import android.animation.ValueAnimator;


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
import com.github.mikephil.charting.animation.Easing;

import com.github.mikephil.charting.utils.ColorTemplate;


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


public class UsuariosActivity extends AppCompatActivity {
    ImageButton atras;
    private BarChart barChart;

    private PieChart pieChart;

    private boolean isFirstLoad = true;

    private FirebaseFirestore db;
    private BarDataSet dataSet; // Add this line
    private List<BarEntry> entries; // And this line


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.usuarios);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Setup the chart
        setupChart();
        setupPieChart();
        // Load data from Firestore
        loadDataFromFirestore();
        loadStatusDataFromFirestore();

        // Button to go back
        atras = findViewById(R.id.atras_b);
        atras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UsuariosActivity.this, MenuActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setupChart() {
        barChart = findViewById(R.id.barChart);
        barChart.getDescription().setEnabled(false);
        barChart.setFitBars(true);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(getAgeLabels()));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        Legend legend = barChart.getLegend();
        legend.setEnabled(true); // Enable the legend if you want to customize it
        legend.setForm(Legend.LegendForm.NONE); // No form (shape), only text
        legend.setTextSize(12f);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setYOffset(5f); // Adjust the offset as needed
    }


    private List<String> getAgeLabels() {
        List<String> labels = new ArrayList<>();
        for (int i = 0; i <= 100; i++) {
            labels.add(String.valueOf(i));
        }
        return labels;
    }

    private void setupPieChart() {

        pieChart = findViewById(R.id.statusPieChart);

        // Configure PieChart appearance
        pieChart.getDescription().setEnabled(false);
        pieChart.setUsePercentValues(true);

        pieChart.setEntryLabelTextSize(12f);
        pieChart.setEntryLabelColor(Color.BLACK);


        Legend legend = pieChart.getLegend();
        //legend.setEnabled(false);
    }

    private void loadDataFromFirestore() {
        db.collection("usuarios").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("FirestoreData", "Listen failed.", e);
                    return;
                }

                Map<String, Long> ageData = new HashMap<>();
                for (QueryDocumentSnapshot doc : snapshots) {
                    if (doc.get("edad") != null) {
                        String ageString = doc.getString("edad");
                        Long ageCount = ageData.getOrDefault(ageString, 0L);
                        ageData.put(ageString, ageCount + 1);
                    }
                }
                updateChartWithFirestoreData(ageData);
            }
        });
    }

    private void loadStatusDataFromFirestore() {
        db.collection("usuarios").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("FirestoreData", "Listen failed.", e);
                    return;
                }

                int activeCount = 0;
                int inactiveCount = 0;
                for (QueryDocumentSnapshot doc : snapshots) {
                    Number statusNumber = doc.getLong("status"); // Using Number to be more generic
                    if (statusNumber != null) {
                        int status = statusNumber.intValue();
                        if (status == 1) {
                            activeCount++;
                        } else if (status == 0) {
                            inactiveCount++;
                        }
                    }
                }
                updatePieChart(activeCount, inactiveCount);
            }
        });
    }


    private void updateChartWithFirestoreData(Map<String, Long> ageData) {
        if (entries == null) {
            entries = new ArrayList<>();
        }

        Set<Integer> updatedAges = new HashSet<>();

        for (Map.Entry<String, Long> entry : ageData.entrySet()) {
            int age = Integer.parseInt(entry.getKey());
            float count = entry.getValue().floatValue();
            updatedAges.add(age);

            BarEntry barEntry = findBarEntryByAge(age);
            if (barEntry != null) {
                // If the bar already exists, animate the change if the count is different
                if (barEntry.getY() != count) {
                    ValueAnimator animator = ValueAnimator.ofFloat(barEntry.getY(), count);
                    animator.setDuration(500); // Duration of the animation
                    animator.addUpdateListener(animation -> {
                        barEntry.setY((Float) animation.getAnimatedValue());
                        dataSet.notifyDataSetChanged();
                        barChart.invalidate(); // Invalidate on animation update to redraw the bar
                    });
                    animator.start();
                }
            } else {
                // If the bar doesn't exist, add it to the entries
                entries.add(new BarEntry(age, count));
            }
        }

        // Remove any bars that no longer have data
        entries.removeIf(barEntry -> !updatedAges.contains((int) barEntry.getX()));

        // Adjust the Y-axis based on the updated entries
        float maxY = entries.stream().max(Comparator.comparing(BarEntry::getY)).get().getY();
        maxY += 1; // Add a buffer to the maximum value

        // Update the Y-axis with the new maximum value
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setAxisMinimum(0); // Start at zero
        leftAxis.setAxisMaximum(maxY); // New maximum value with buffer

        // Same for the right Y-axis, if it's being used
        YAxis rightAxis = barChart.getAxisRight();
        rightAxis.setAxisMinimum(0);
        rightAxis.setAxisMaximum(maxY);

        // Calculate the new range for the X-axis
        float maxX = entries.stream().max(Comparator.comparing(BarEntry::getX)).get().getX();
        float minX = entries.stream().min(Comparator.comparing(BarEntry::getX)).get().getX();
        barChart.getXAxis().setAxisMinimum(minX);
        barChart.getXAxis().setAxisMaximum(maxX + 1); // +1 to ensure the last bar is fully visible

        // Update or create the dataset and chart
        if (dataSet == null) {
            dataSet = new BarDataSet(entries, "Edades");
            dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
            dataSet.setValueTextColor(Color.BLACK);
            dataSet.setValueTextSize(16f);

            dataSet.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    // Return the value as an integer without decimal places
                    return String.valueOf((int) value);
                }
            });


            BarData barData = new BarData(dataSet);
            barChart.setData(barData);
            barChart.animateY(1000); // Animate the chart on the first load
        } else {
            dataSet.notifyDataSetChanged();
            barChart.notifyDataSetChanged();
            // No need to re-animate the entire chart here, as individual bar updates are animated
        }

        barChart.invalidate(); // Refresh the chart to show updated data
    }

    // Helper method to find a BarEntry by age
    private BarEntry findBarEntryByAge(int age) {
        for (BarEntry entry : entries) {
            if ((int) entry.getX() == age) {
                return entry;
            }
        }
        return null;
    }

    private void updatePieChart(int activeCount, int inactiveCount) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(activeCount, "Activo"));
        entries.add(new PieEntry(inactiveCount, "Inactivo"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS); // or use your own color array
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);

        // Set the value formatter to display values as percentages
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                // Assuming 'value' is already calculated as a percentage
                return String.format("%.1f%%", value); // One decimal place
            }
        });

        PieData data = new PieData(dataSet);
        pieChart.setData(data);

        // Set use percent values to true to display the values as percentages
        pieChart.setUsePercentValues(true);

        if (isFirstLoad) {
            pieChart.animateY(1400); // Animate the chart on the first load
            isFirstLoad = false; // Set the flag to false after the first load
        }

        pieChart.invalidate(); // Refresh the chart
    }




}



