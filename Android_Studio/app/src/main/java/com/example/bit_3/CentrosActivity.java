package com.example.bit_3;

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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

abstract class PdfChartGeneratorCentros extends Context {

    // Esta función genera un PDF a partir de un gráfico
    @SuppressLint("NewApi")
    public static void generatePdfFromChart(Context context, PieChart pie, BarChart chart,String tx1, String tx2, String pdfFileName) {
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

        Bitmap scaledPieBitmap = scalePie(pie, scalePercent);
        canvas.drawText("Número de centros en servicio: ",50, 60 ,paint);
        canvas.drawText(tx1,50, 80 ,paint);
        canvas.drawText("Centros Totales: ",50, 100 ,paint);
        canvas.drawText(tx2,50, 120 ,paint);
        canvas.drawText("Estado de los Centros: ",200, 160,paint);
        canvas.drawBitmap(scaledPieBitmap, 150, 170, null);
        Bitmap scaledChartBitmap = scaleChart(chart, scalePercent);
        canvas.drawText("Cantidad de centros por categoría:  ",200, 500,paint);
        canvas.drawBitmap(scaledChartBitmap, 150, 520, null);
        // Añadir hora actual al PDF
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        canvas.drawText("Fecha de creación: " + timeStamp, 50, height - 20, paint2);
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

        builder.setTitle("Documento Descargado con éxito")
                .setMessage("El pdf fue guardado en Descargas.")
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
public class CentrosActivity extends AppCompatActivity {

    Button descarga;
    private LineChart collectionTimeLineChart;

    private PieChart pieChart;

    private BarDataSet dataSet; // Add this line

    BarChart barChart;

    private boolean isFirstLoad = true;
    private boolean isFirstLoad2 = true;

    private FirebaseFirestore db;
    private List<BarEntry> entries; // And this line

    private TextView activeCentersTextView;
    private TextView totalCentersTextView;
    String text1,text2;
    CharSequence text_c, text1_c;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.centros);

        db = FirebaseFirestore.getInstance();

        // Setup the chart
        //setupLineChart();
        setupPieChart();
        setupChart();
        // Load data from Firestore
        //loadCollectionTimesDataFromFirestore();
        loadStatusDataFromFirestore();
        loadDataBarChart();

        // Initialize TextViews
        activeCentersTextView = findViewById(R.id.activeCenters); // Replace with your actual TextView ID
        totalCentersTextView = findViewById(R.id.totalCenters); // Replace with your actual TextView ID

        // Load data from Firestore
        loadCentersData();

        descarga = findViewById(R.id.descarga_b);
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
        PdfChartGeneratorCentros.generatePdfFromChart(this, pieChart, barChart,text1, text2, "grafica_centros");
    }


    private void loadCentersData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("centros")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w("CentrosActivity", "Listen failed.", e);
                            return;
                        }

                        int activeCount = 0;
                        int totalCount = 0;
                        if (snapshots != null) {
                            for (QueryDocumentSnapshot doc : snapshots) {
                                totalCount++;
                                Boolean status = doc.getBoolean("estado");
                                if (Boolean.TRUE.equals(status)) {
                                    activeCount++;
                                }
                            }
                            updateCollectorsCount(activeCount, totalCount);
                        }
                    }
                });
    }



    private void updateCollectorsCount(int active, int total) {
        // Update the text views on the main thread
        runOnUiThread(() -> {
            activeCentersTextView.setText(String.valueOf(active));
            totalCentersTextView.setText(String.valueOf(total));
            text_c = activeCentersTextView.getText();
            text1 = text_c.toString();
            text1_c = totalCentersTextView.getText();
            text2 = text1_c.toString();
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
        db.collection("centros").addSnapshotListener(new EventListener<QuerySnapshot>() {
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
                    Boolean status = doc.getBoolean("estado");
                    if (Boolean.TRUE.equals(status)) {
                        activeCount++;
                    } else if (Boolean.FALSE.equals(status)) {
                        inactiveCount++;
                    }
                }
                updatePieChart(activeCount, inactiveCount);
            }
        });
    }





    private void updatePieChart(int activeCount, int inactiveCount) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(activeCount, "Activo"));
        entries.add(new PieEntry(inactiveCount, "Inactivo"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        int[] colors = new int[]{Color.rgb(67, 160, 71), Color.rgb(239, 83, 80)}; // RGB values for green and red
        dataSet.setColors(colors); // Apply the custom colors
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

    private void setupChart() {
        barChart = findViewById(R.id.barChart);
        barChart.getDescription().setEnabled(false);
        barChart.setFitBars(true);

        XAxis xAxis = barChart.getXAxis();
        //xAxis.setValueFormatter(new IndexAxisValueFormatter(getAgeLabels()));
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


    private void loadDataBarChart() {
        db.collection("categorias").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                // Crear un mapa para almacenar el conteo de centros por categoría
                Map<String, Integer> categoryCounts = new HashMap<>();
                List<String> categoryNames = new ArrayList<>(); // Lista para guardar los nombres de las categorías
                int totalCategories = task.getResult().size();
                AtomicInteger processedCategories = new AtomicInteger(0);

                // Recorrer los documentos de categorías
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String categoryName = document.getId(); // Usar el ID del documento como nombre de la categoría
                    categoryNames.add(categoryName); // Agregar a la lista de nombres de categorías

                    // Ahora, para cada categoría, contar los centros asociados
                    db.collection("centros").whereEqualTo("categoria", categoryName).get()
                            .addOnCompleteListener(centrosTask -> {
                                if (centrosTask.isSuccessful() && centrosTask.getResult() != null) {
                                    // Agregar la cantidad de centros al mapa usando el ID del documento como clave
                                    categoryCounts.put(categoryName, centrosTask.getResult().size());

                                    // Comprobar si todas las categorías han sido procesadas
                                    if (processedCategories.incrementAndGet() == totalCategories) {
                                        updateChart(categoryCounts, categoryNames);
                                    }
                                }
                            });
                }
            }
        });
    }

    private void updateChart(Map<String, Integer> categoryCounts, List<String> categoryNames) {
        List<BarEntry> chartEntries = new ArrayList<>();

        int index = 0;
        for (String categoryName : categoryNames) {
            if (categoryCounts.containsKey(categoryName)) {
                chartEntries.add(new BarEntry(index, categoryCounts.get(categoryName)));
                index++;
            }
        }

        BarDataSet barDataSet = new BarDataSet(chartEntries, ""); // Título en blanco
        barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);

        barChart.setExtraBottomOffset(10f); // Aumenta el margen inferior

        XAxis xAxis = barChart.getXAxis();
        xAxis.setLabelRotationAngle(90); // Rota las etiquetas en 90 grados
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawLabels(true);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);

        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                String categoryName = categoryNames.get((int) value);
                StringBuilder verticalLabel = new StringBuilder();
                for (char c : categoryName.toCharArray()) {
                    verticalLabel.append(c).append("\n");
                }
                return verticalLabel.toString().trim();
            }
        });

        barChart.notifyDataSetChanged();
        barChart.animateY(1000); // Animate the chart on the first load
        barChart.invalidate(); // Refresca el gráfico
    }


}