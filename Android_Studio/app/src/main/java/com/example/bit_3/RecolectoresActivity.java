package com.example.bit_3;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;



import java.util.ArrayList;
import java.util.List;

public class RecolectoresActivity extends AppCompatActivity {

    ImageButton atras;
    private BarChart barChart;

    private PieChart pieChart;

    private boolean isFirstLoad = true;

    private FirebaseFirestore db;
    private BarDataSet dataSet; // Add this line
    private List<BarEntry> entries; // And this line

    public RecolectoresActivity(BarChart barChart) {
        this.barChart = barChart;
    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recolectores);

        db = FirebaseFirestore.getInstance();

        // Setup the chart
        //setupChart();
        setupPieChart();
        // Load data from Firestore
        //loadDataFromFirestore();
        loadStatusDataFromFirestore();

        atras = findViewById(R.id.atras_b);

        atras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RecolectoresActivity.this, MenuActivity.class);
                startActivity(intent);
            }
        });
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

    private void loadStatusDataFromFirestore() {
        db.collection("recolectores").addSnapshotListener(new EventListener<QuerySnapshot>() {
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

    private void updatePieChart(int activeCount, int inactiveCount) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(activeCount, "Active"));
        entries.add(new PieEntry(inactiveCount, "Inactive"));

        PieDataSet dataSet = new PieDataSet(entries, "User Status");
        int[] colors = new int[]{Color.rgb(67, 160, 71), Color.rgb(239, 83, 80)}; // RGB values for green and red
        dataSet.setColors(colors); // Apply the custom colors
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);

        // Set the value formatter to display values as percentages
        dataSet.setValueFormatter(new ValueFormatter() {
            @SuppressLint("DefaultLocale")
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