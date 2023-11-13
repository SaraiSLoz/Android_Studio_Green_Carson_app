package com.example.bit_3;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class menu extends AppCompatActivity {

    Button Usuario, Recolectores, Centros, Residuos;
    @SuppressLint("MissingInflatedId")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);
        Usuario = findViewById(R.id.Usuario_but);
        Recolectores = findViewById(R.id.Recolectores_but);
        Centros = findViewById(R.id.Centros_but);
        Residuos = findViewById(R.id.Residuos_but);


        Usuario.setOnClickListener(v -> setContentView(R.layout.usuarios));
        Recolectores.setOnClickListener(v -> setContentView(R.layout.recolectores));
        Centros.setOnClickListener(v -> setContentView(R.layout.centros));
        Residuos.setOnClickListener(v -> setContentView(R.layout.residuos));
    }
}
