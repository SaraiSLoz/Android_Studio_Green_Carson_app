package com.example.bit_3;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class RecolectoresActivity extends AppCompatActivity {

    ImageButton atras;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recolectores);

        atras = findViewById(R.id.atras_b);

        atras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RecolectoresActivity.this, MenuActivity.class);
                startActivity(intent);
            }
        });
    }
}