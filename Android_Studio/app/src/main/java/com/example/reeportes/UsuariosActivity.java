package com.bit3.reeportes;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

abstract class PdfChartGeneratorUsers extends Context {

    // Esta función genera un PDF a partir de un gráfico
    @SuppressLint("NewApi")
    public static void generatePdfFromChart(Context context, BarChart chart, PieChart pie, String pdfFileName) {
        // Crea un documento PDF
        PdfDocument pdfDocument = new PdfDocument();

        // Configura el tamaño de la página
        int width = 612; //612 y 792
        int height = 792;

        // Configura la página
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(width, height, 2).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        float scalePercent = 30f;

        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(15f);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        Paint paint2 = new Paint();
        paint2.setColor(Color.BLACK);
        paint2.setTextSize(8f);
        // Dibuja el contenido del gráfico en la página
        Canvas canvas = page.getCanvas();
        Bitmap scaledChartBitmap = scaleChart(chart, scalePercent);
        Bitmap scaledPieBitmap = scalePie(pie, scalePercent);
        canvas.drawText("Recolecciones",290, 80 ,paint);
        canvas.drawBitmap(scaledChartBitmap, 150, 100, null);
        Bitmap bitmappie = getPieBitMap(pie);
        canvas.drawText("Usuarios",290, 440,paint);
        canvas.drawBitmap(scaledPieBitmap, 150, 450, null);
        // Añadir hora actual al PDF
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        canvas.drawText("Fecha de creación: " + timeStamp, 100, height - 20, paint2);
        // Finaliza la página
        pdfDocument.finishPage(page);

        // Guarda el documento en un archivo PDF
        savePdf(context, pdfDocument, pdfFileName);

        // Cierra el documento
        pdfDocument.close();

        mostrarAlertDialog(context);
    }

    private static void mostrarAlertDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle("Documento descargado con éxito")
                .setMessage("El pdf fue guardado en Descargas")
                .setPositiveButton("Entendido", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss(); // Cierra el diálogo
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    public static Bitmap scaleChart(BarChart chart, float scalePercent) {
        // Convierte la gráfica a un mapa de bits
        Bitmap chartBitmap = getChartBitmap(chart);

        // Obtiene las dimensiones originales
        int originalWidth = chartBitmap.getWidth();
        int originalHeight = chartBitmap.getHeight();

        // Calcula las nuevas dimensiones después de escalar
        int newWidth = (int) (originalWidth * scalePercent / 100);
        int newHeight = (int) (originalHeight * scalePercent / 100);

        // Escala el mapa de bits
        Bitmap scaledChartBitmap = Bitmap.createScaledBitmap(chartBitmap, newWidth, newHeight, true);

        return scaledChartBitmap;
    }
    public static Bitmap scalePie(PieChart chart, float scalePercent) {
        // Convierte la gráfica a un mapa de bits
        Bitmap chartBitmap = getPieBitMap(chart);

        // Obtiene las dimensiones originales
        int originalWidth = chartBitmap.getWidth();
        int originalHeight = chartBitmap.getHeight();

        // Calcula las nuevas dimensiones después de escalar
        int newWidth = (int) (originalWidth * scalePercent / 100);
        int newHeight = (int) (originalHeight * scalePercent / 100);

        // Escala el mapa de bits
        Bitmap scaledChartBitmap = Bitmap.createScaledBitmap(chartBitmap, newWidth, newHeight, true);

        return scaledChartBitmap;
    }

    private static Bitmap getPieBitMap(PieChart pie) {
        pie.setDrawingCacheEnabled(true);
        pie.buildDrawingCache(true);
        Bitmap bitmap = Bitmap.createBitmap(pie.getDrawingCache());
        pie.setDrawingCacheEnabled(false);
        return bitmap;
    }


    // Esta función convierte el gráfico a un mapa de bits para su posterior uso en el PDF
    private static Bitmap getChartBitmap(BarChart chart) {
        chart.setDrawingCacheEnabled(true);
        chart.buildDrawingCache(true);
        Bitmap bitmap = Bitmap.createBitmap(chart.getDrawingCache());
        chart.setDrawingCacheEnabled(false);
        return bitmap;
    }

    // Esta función guarda el documento PDF en el almacenamiento externo
    @RequiresApi(api = Build.VERSION_CODES.O)
    private static void savePdf(Context context, PdfDocument pdfDocument, String pdfFileName) {
        File directory = new File(Environment.getExternalStorageDirectory(), "Download");
        directory.mkdirs();

        File file = new File(directory, pdfFileName + ".pdf");

        try {
            pdfDocument.writeTo(Files.newOutputStream(file.toPath()));
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error al guardar el PDF", Toast.LENGTH_SHORT).show();
        }
    }

}
public class UsuariosActivity extends AppCompatActivity {
    Button descarga;
    BarChart barChart;

    PieChart pieChart;

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
        descarga = findViewById(R.id.button_des);

        // Setup the chart
        setupChart();
        setupPieChart();
        // Load data from Firestore
        loadDataFromFirestore();
        loadStatusDataFromFirestore();

        descarga.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarAlertDialog();
            }
        });


    }

    private void mostrarAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Descargar Documento")
                .setMessage("¿Estas seguro de que deseas descargar este documento? ")
                .setPositiveButton("Descargar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        descargarPDF();
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss(); // Cierra el diálogo
                    }
                })

        ;
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    @SuppressLint("NewApi")
    private void descargarPDF() {
        PdfChartGeneratorUsers.generatePdfFromChart(this, barChart, pieChart,"grafica_users");
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
        int[] colors = new int[]{Color.rgb(67, 160, 71), Color.rgb(239, 83, 80)}; // RGB values for green and red
        dataSet.setColors(colors);
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



