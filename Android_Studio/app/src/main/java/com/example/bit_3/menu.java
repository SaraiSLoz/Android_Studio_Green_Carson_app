package com.example.bit_3;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class menu extends AppCompatActivity {

    Button Usuario, Recolectores, Centros, Residuos;
    //
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Usuario.setOnClickListener(v -> setContentView(R.layout.usuarios));
        Recolectores.setOnClickListener(v -> setContentView(R.layout.recolectores));
        Centros.setOnClickListener(v -> setContentView(R.layout.centros));
        Residuos.setOnClickListener(v -> setContentView(R.layout.residuos));
    }
}
