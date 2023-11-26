package com.example.bit_3;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class MenuActivity extends AppCompatActivity {

    Button usuario, recolectores, centros, residuos;
    ImageButton perfil;
    String userUid;
    @SuppressLint("MissingInflatedId")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);
        usuario = findViewById(R.id.Usuario_but);
        recolectores = findViewById(R.id.Recolectores_but);
        centros = findViewById(R.id.Centros_but);
        residuos = findViewById(R.id.Residuos_but);
        perfil = findViewById(R.id.perfil_b);
        userUid = SharedPreferencesUtil.getUserUidFromSharedPreferences(this);
        recolectores.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MenuActivity.this, RecolectoresActivity.class);
                startActivity(intent);
            }
        });
        usuario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MenuActivity.this, UsuariosActivity.class);
                startActivity(intent);
            }
        });
        centros.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MenuActivity.this, CentrosActivity.class);
                startActivity(intent);
            }
        });
        residuos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MenuActivity.this, ResiduosActivity.class);
                startActivity(intent);
            }
        });
        perfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MenuActivity.this, Perfil.class);
                startActivity(intent);
            }
        });
    }
 }


