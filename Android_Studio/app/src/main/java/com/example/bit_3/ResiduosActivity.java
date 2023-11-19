package com.example.bit_3;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class ResiduosActivity extends AppCompatActivity {
    ImageButton atras;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.residuos);

        atras = findViewById(R.id.atras_b);
        atras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ResiduosActivity.this, MenuActivity.class);
                startActivity(intent);
            }
        });
    }
}
