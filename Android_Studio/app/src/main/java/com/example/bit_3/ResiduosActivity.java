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

        YAxis rightYAxis = horizontalBarChart.getAxisRight();
        rightYAxis.setDrawAxisLine(true); // Dibuja la línea del eje Y derecho
        rightYAxis.setDrawGridLines(false); // No dibuja las líneas de cuadrícula
        rightYAxis.setDrawLabels(true); // Habilita las etiquetas (nombres de los materiales)

        rightYAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < labels.size()) {
                    return labels.get(index); // Retorna el nombre del material correspondiente
                }
                return ""; // Para índices que no corresponden a un label
            }
        });

    }




    private void loadDataFromFirestore() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -30);
        Date thirtyDaysAgo = calendar.getTime();

        db.collection("recolecciones")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Map<String, Integer> materialCounts = new HashMap<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String fechaRecoleccion = document.getString("fechaRecoleccion");
                            try {
                                Date recoleccionDate = sdf.parse(fechaRecoleccion);
                                if (recoleccionDate != null && !recoleccionDate.before(thirtyDaysAgo)) {
                                    Map<String, Object> materials = (Map<String, Object>) document.get("materiales");
                                    if (materials != null) {
                                        for (Object value : materials.values()) {
                                            if (value instanceof Map) {
                                                Map<String, Object> material = (Map<String, Object>) value;
                                                String materialName = (String) material.get("nombre");
                                                Long count = (Long) material.get("cantidad");
                                                if (materialName != null && count != null) {
                                                    materialCounts.put(materialName, materialCounts.getOrDefault(materialName, 0) + count.intValue());
                                                }
                                            }
                                        }
                                    }
                                }
                            } catch (ParseException e) {
                                Log.e("ResiduosActivity", "Error parsing date", e);
                            }
                        }
                        updateChart(materialCounts);
                    } else {
                        Log.d("ResiduosActivity", "Error getting documents: ", task.getException());
                    }
                });
    }




    private void updateChart(Map<String, Integer> materialCounts) {
        List<BarEntry> entries = new ArrayList<>();
        labels = new ArrayList<>();

        // Convertir el mapa a una lista para poder ordenarla
        List<Map.Entry<String, Integer>> list = new ArrayList<>(materialCounts.entrySet());

        // Ordenar alfabéticamente por la clave (nombre del material)
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        });

        // Crear los datos del gráfico con las entradas ordenadas
        for (int i = 0; i < list.size(); i++) {
            entries.add(new BarEntry(i, list.get(i).getValue()));
            labels.add(list.get(i).getKey());
        }

        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS); // Asigna colores múltiples

        BarData data = new BarData(dataSet);
        horizontalBarChart.setData(data);

        YAxis rightAxis = horizontalBarChart.getAxisRight();
        rightAxis.setDrawLabels(true);
        rightAxis.setValueFormatter(new IndexAxisValueFormatter(labels));

        // Importante: Asegúrate de que el eje Y derecho muestre las etiquetas para cada entrada
        rightAxis.setLabelCount(labels.size(), false);

        horizontalBarChart.invalidate();


    }





}
