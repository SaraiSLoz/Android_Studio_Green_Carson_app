package com.example.bit_3;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    Button sesion;
    EditText email,password;

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("recolectores");

    @SuppressLint("MissingInflatedId")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);

        email = findViewById(R.id.usuario);
        password = findViewById(R.id.password_t);
        sesion = findViewById(R.id.button_sesion);

        sesion.setOnClickListener(v -> {
            String emailUser = email.getText().toString().trim();
            String passUser = password.getText().toString().trim();

            if(emailUser.isEmpty() && passUser.isEmpty()){
                Toast.makeText(LoginActivity.this,"Ingresa los datos", Toast.LENGTH_SHORT).show();
            }
            else{
                boolean us = loginUser(emailUser);
                boolean ps = loginPassword(passUser);
                if(us && ps){
                    setContentView(R.layout.menu);
                }
            }
        });




    }

    private boolean loginUser(String data) {
        final String[] valor = new String[1];
        databaseReference.child("administradores").child("nombre").addListenerForSingleValueEvent(new ValueEventListener() {

            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    valor[0] = snapshot.getValue(String.class); // Obtén el valor de la base de datos
                } else {
                    Toast.makeText(LoginActivity.this,"Usuario o contrasena no válidos", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LoginActivity.this, "Error al acceder a la base de datos", Toast.LENGTH_SHORT).show();
            }
        });

        return valor[0].equals(data);
    }

    private boolean loginPassword(String data) {
        final String[] valor = new String[1];
        databaseReference.child("administradores").child("contraseña").addListenerForSingleValueEvent(new ValueEventListener() {

            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    valor[0] = snapshot.getValue(String.class); // Obtén el valor de la base de datos
                } else {
                    Toast.makeText(LoginActivity.this,"Usuario o contrasena no válidos", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LoginActivity.this, "Error al acceder a la base de datos", Toast.LENGTH_SHORT).show();
            }
        });

        return valor[0].equals(data);
    }

}
