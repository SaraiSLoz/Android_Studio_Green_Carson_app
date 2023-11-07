package com.example.bit_3;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    Button sesion = findViewById(R.id.button_sesion);
    TextView textView = findViewById(R.id.baseDatos);

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        FirebaseApp.initializeApp(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sesion.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, android.R.menu.class);
            startActivity(intent);
        });
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("recolectores");

        databaseReference.child("recolectores").child("nombre").addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String valor = snapshot.getValue(String.class); // Obtén el valor de la base de datos
                    textView.setText(valor); // Establece el valor en el TextView
                } else {
                    textView.setText("El dato no existe en la base de datos");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}