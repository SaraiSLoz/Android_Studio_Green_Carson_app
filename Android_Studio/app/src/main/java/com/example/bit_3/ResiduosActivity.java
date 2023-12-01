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
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
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
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.AxisBase;



import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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

abstract class PdfChartGeneratorResiduos extends Context {

    // Esta función genera un PDF a partir de un gráfico
    @SuppressLint("NewApi")
    public static void generatePdfFromChart(Context context, HorizontalBarChart pie, String pdfFileName) {
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
        canvas.drawText("Materiales: ",290, 30,paint);
        canvas.drawBitmap(scaledPieBitmap, 150, 50, null);
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
    public static Bitmap scalePie(HorizontalBarChart chart, float scalePercent) {
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

    private static Bitmap getChartBitmap(HorizontalBarChart chart) {
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

public class ResiduosActivity extends AppCompatActivity {
    HorizontalBarChart horizontalBarChart;
    private FirebaseFirestore db;
    private List<BarEntry> entries;

    private List<String> labels;

    private BarDataSet dataSet;
    private Button descarga;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.residuos);

        db = FirebaseFirestore.getInstance();
        horizontalBarChart = findViewById(R.id.horizontalBarChart);

        setupChart();
        loadDataFromFirestore();

        descarga = findViewById(R.id.button_descarga);
        descarga.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarAlertDialog();
            }
        });

        entries = new ArrayList<>(); // Inicializa la lista de entradas
        labels = new ArrayList<>(); // Inicializa la lista de etiquetas

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
        PdfChartGeneratorResiduos.generatePdfFromChart(this, horizontalBarChart, "grafica_residuos");
    }

    private void setupChart() {
        horizontalBarChart.getDescription().setEnabled(false);

        Legend legend = horizontalBarChart.getLegend();
        legend.setEnabled(false);

        ValueFormatter integerFormatter = new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                // Format the value as an integer, removing the ".0" part
                return String.valueOf((int) value);
            }
        };

        YAxis leftYAxis = horizontalBarChart.getAxisLeft();
        leftYAxis.setDrawLabels(true);
        leftYAxis.setDrawAxisLine(true);
        leftYAxis.setDrawGridLines(true);
        leftYAxis.setValueFormatter(integerFormatter);

        YAxis rightYAxis = horizontalBarChart.getAxisRight();
        rightYAxis.setDrawLabels(true);
        rightYAxis.setDrawAxisLine(true);
        rightYAxis.setDrawGridLines(true);
        rightYAxis.setValueFormatter(integerFormatter);

        XAxis xAxis = horizontalBarChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // Coloca las etiquetas en la parte inferior
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawLabels(true); // Asegúrate de que las etiquetas se dibujen
        xAxis.setGranularity(1f); // Solo un valor por intervalo
        xAxis.setGranularityEnabled(true);
        xAxis.setLabelCount(26);


        // Configura el ValueFormatter para el eje X para mostrar las etiquetas correctas
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                // Calcula el índice inverso para obtener las etiquetas en orden inverso
                int index = labels.size() - 1 - (int) value;
                if (index < 0 || index >= labels.size()) {
                    return ""; // En caso de un índice no válido, devuelve una cadena vacía
                }
                return labels.get(index);
            }
        });

        // Refresh the chart to apply changes
        horizontalBarChart.invalidate();
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
                            String estado = document.getString("estado"); // Get the state of the collection
                            try {
                                Date recoleccionDate = sdf.parse(fechaRecoleccion);
                                // Check if the recoleccionDate is within the last 30 days and the state is "Completada"
                                if (recoleccionDate != null && !recoleccionDate.before(thirtyDaysAgo) && "Completada".equals(estado)) {
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



        horizontalBarChart.notifyDataSetChanged();
        horizontalBarChart.invalidate(); // Refresh the chart
    }

}