package com.example.bit_3;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Perfil extends AppCompatActivity {

    TextView correo;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);
        if (user != null) {
            String email = user.getEmail();
            correo.setText(email);
        } else {
            // El usuario no está autenticado, realiza alguna acción en consecuencia.
        }
    }
}