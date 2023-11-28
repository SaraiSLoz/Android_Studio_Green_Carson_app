package com.example.bit_3;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
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
import android.widget.TextView;
import android.widget.Toast;


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

import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;


import androidx.appcompat.app.AppCompatActivity;

abstract class PdfChartGeneratorCentros extends Context {

    // Esta función genera un PDF a partir de un gráfico
    @SuppressLint("NewApi")
    public static void generatePdfFromChart(Context context, PieChart pie, String tx1, String tx2, String pdfFileName) {
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
        canvas.drawText(tx1,50, 70 ,paint);
        canvas.drawText("Centros Totales: ",50, 90 ,paint);
        canvas.drawText(tx2,50, 100 ,paint);
        canvas.drawText("Estado de los Centros: ",290, 150,paint);
        canvas.drawBitmap(scaledPieBitmap, 150, 160, null);
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
                .setMessage("El pdf fue guardado con éxito en Descargas")
                .setPositiveButton("Entendido", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss(); // Cierra el diálogo
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    public static Bitmap scaleChart(LineChart chart, float scalePercent) {
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
    private static Bitmap getChartBitmap(LineChart chart) {
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

        if (!directory.exists()) {
            directory.mkdirs();
        }

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
        // Load data from Firestore
        //loadCollectionTimesDataFromFirestore();
        loadStatusDataFromFirestore();

        // Initialize TextViews
        activeCentersTextView = findViewById(R.id.activeCenters); // Replace with your actual TextView ID
        totalCentersTextView = findViewById(R.id.totalCenters); // Replace with your actual TextView ID

        // Load data from Firestore
        loadCentersData();

        descarga = findViewById(R.id.descarga_b);
        descarga.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                descargarPDF();
            }
        });

    }

    private void mostrarAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Documento descargado")
                .setMessage("El pdf fue generado")
                .setPositiveButton("Entendido", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss(); // Cierra el diálogo
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    @SuppressLint("NewApi")
    private void descargarPDF() {

        PdfChartGeneratorCentros.generatePdfFromChart(this, pieChart, text1, text2, "grafica_centros");
        mostrarAlertDialog();
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





}