package com.example.bit_3;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.pdf.PdfDocument;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

abstract class PdfChartGenerator extends Context {

    // Esta función genera un PDF a partir de un gráfico

    @SuppressLint("NewApi")
    public static void generatePdfFromChart(Context context, LineChart chart, PieChart pie, String pdfFileName) {
        // Crea un documento PDF
        PdfDocument pdfDocument = new PdfDocument();

        // Configura el tamaño de la página
        int width = chart.getWidth(); //612 y 792
        int height = chart.getHeight()+ pie.getHeight() + 150;

        // Configura la página
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(width, height, 2).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(50f);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        // Dibuja el contenido del gráfico en la página
        Canvas canvas = page.getCanvas();
        Bitmap bitmap = getChartBitmap(chart);
        canvas.drawText("Recolecciones",150, 50 ,paint);
        canvas.drawBitmap(bitmap, 0, 20, null);
        Bitmap bitmappie = getPieBitMap(pie);
        canvas.drawText("Usuarios",150, chart.getHeight()+50 ,paint);
        canvas.drawBitmap(bitmappie, 0, chart.getHeight()+70, null);
        // Finaliza la página
        pdfDocument.finishPage(page);

        // Guarda el documento en un archivo PDF
        savePdf(context, pdfDocument, pdfFileName);

        // Cierra el documento
        pdfDocument.close();

        // Muestra un mensaje al usuario
        Toast.makeText(context, "PDF generado correctamente", Toast.LENGTH_SHORT).show();
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
    private void mostrarAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Documento descargado con éxito")
                .setMessage("El pdf fue generado y guardado exitosamente")
                .setPositiveButton("Entendido", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss(); // Cierra el diálogo
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
public class RecolectoresActivity extends AppCompatActivity {

    Button descargarPDFButton;
    private LineChart collectionTimeLineChart;

    private PieChart pieChart;

    private boolean isFirstLoad = true;
    private boolean isFirstLoad2 = true;

    private FirebaseFirestore db;
    private List<BarEntry> entries; // And this line

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recolectores);

        db = FirebaseFirestore.getInstance();

        // Setup the chart
        setupLineChart();
        setupPieChart();
        // Load data from Firestore
        loadCollectionTimesDataFromFirestore();
        loadStatusDataFromFirestore();


        descargarPDFButton = findViewById(R.id.descargarPDFButton);
        descargarPDFButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                descargarPDF();
            }
        });
    }
    @SuppressLint("NewApi")
    private void descargarPDF() {
        PdfChartGenerator.generatePdfFromChart(this, collectionTimeLineChart, pieChart, "graficas5");
        Toast.makeText(this, "PDF generado correctamente y guardado", Toast.LENGTH_SHORT).show();
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

    private void setupLineChart() {
        collectionTimeLineChart = findViewById(R.id.collectionTimeLineChart);
        collectionTimeLineChart.getDescription().setEnabled(false);
        collectionTimeLineChart.setDrawGridBackground(false);
        collectionTimeLineChart.setTouchEnabled(true);
        collectionTimeLineChart.setDragEnabled(true);
        collectionTimeLineChart.setScaleEnabled(true);
        collectionTimeLineChart.setPinchZoom(true);

        XAxis xAxis = collectionTimeLineChart.getXAxis();
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                // Assuming value is the hour of the day
                int hour = (int) value;
                return String.format(Locale.getDefault(), "%02d:00", hour); // Format as "HH:00"
            }
        });
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        // ... other setup for line chart ...
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

    private void loadCollectionTimesDataFromFirestore() {
        db.collection("recolecciones").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("FirestoreData", "Listen failed.", e);
                    return;
                }

                // Create a map to hold the count of collections for each hour
                Map<Integer, Integer> collectionTimes = new HashMap<>();

                for (QueryDocumentSnapshot doc : snapshots) {
                    String timeString = doc.getString("horaRecoleccionFinal");
                    if (timeString != null && !timeString.isEmpty()) {

                        int hour = Integer.parseInt(timeString.split(":")[0]);
                        collectionTimes.put(hour, collectionTimes.getOrDefault(hour, 0) + 1);
                    }
                }

                updateLineChart(collectionTimes);
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("ObsoleteSdkInt")
    private void updateLineChart(Map<Integer, Integer> collectionTimes) {
        List<Entry> entries = new ArrayList<>();

        for (Map.Entry<Integer, Integer> timeEntry : collectionTimes.entrySet()) {
            entries.add(new Entry(timeEntry.getKey(), timeEntry.getValue()));
        }

        // Sort the entries by hour
        entries.sort(new Comparator<Entry>() {
            @Override
            public int compare(Entry e1, Entry e2) {
                return Float.compare(e1.getX(), e2.getX());
            }
        });

        LineDataSet dataSet = new LineDataSet(entries, "Cantidad de Recolecciones");
        dataSet.setColor(Color.BLACK);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(3f);
        dataSet.setCircleColor(Color.BLACK);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawFilled(true);

        // Define the gradient
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            // As the chart may not have been laid out yet, we need to get its height in a layout pass
            new Handler().post(() -> {
                int height = collectionTimeLineChart.getHeight();
                GradientDrawable gradientDrawable = new GradientDrawable(
                        GradientDrawable.Orientation.BOTTOM_TOP,
                        new int[]{Color.RED, Color.TRANSPARENT}
                );
                gradientDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
                gradientDrawable.setShape(GradientDrawable.RECTANGLE);
                gradientDrawable.setDither(true);
                dataSet.setFillDrawable(gradientDrawable);
                collectionTimeLineChart.invalidate(); // Invalidate to update the chart
            });
        }

        LineData lineData = new LineData(dataSet);
        collectionTimeLineChart.setData(lineData);
        // Animate the chart after setting the data
        if (isFirstLoad2) {
            collectionTimeLineChart.animateXY(1500, 1500, Easing.EaseInOutQuad);
            isFirstLoad2 = false;
        }
        collectionTimeLineChart.invalidate(); // Refresh the chart
    }




}