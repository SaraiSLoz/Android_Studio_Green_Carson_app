package com.example.bit_3;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class Menu extends AppCompatActivity {

    Button Usuario, Recolectores, Centros, Residuos;
    @SuppressLint("MissingInflatedId")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);
        Usuario = findViewById(R.id.Usuario_but);
        Recolectores = findViewById(R.id.Recolectores_but);
        Centros = findViewById(R.id.Centros_but);
        Residuos = findViewById(R.id.Residuos_but);


    }

    public void irARecolectores(View v)  {
        Intent i = new Intent(this, RecolectoresActivity.class);
        startActivity(i);
    }
}
