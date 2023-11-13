package com.example.bit_3;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    Button sesion;
    EditText email, password;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    //DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("recolectores");

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

            if (emailUser.isEmpty() && passUser.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Ingresa los datos", Toast.LENGTH_SHORT).show();
            } else {
                loginUserWithEmailAndPassword(emailUser, passUser);
            }
        });

    }
        private void loginUserWithEmailAndPassword (String email, String password){
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Inicio de sesión exitoso
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // El usuario está autenticado, puedes pasar a la siguiente actividad o realizar otras acciones
                                setContentView(R.layout.menu);
                            }
                        } else {
                            // Si el inicio de sesión falla, muestra un mensaje al usuario
                            Toast.makeText(LoginActivity.this, "Inicio de sesión fallido", Toast.LENGTH_SHORT).show();
                        }
                    });

        }

    }
