package com.example.bit_3;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Popup extends AppCompatActivity {
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser user = auth.getCurrentUser();
    Button aceptar;
    EditText actual, nuevo;

    String actual_s,nuevo_s, correo ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup);
        correo = SharedPreferencesUtil.getUserUidFromSharedPreferences(this);
        aceptar = findViewById(R.id.aceptar_b);
        actual = findViewById(R.id.contrs_actual);
        nuevo = findViewById(R.id.contra_nueva);

        aceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actual_s = actual.getText().toString().trim();
                nuevo_s =nuevo.getText().toString().trim();
                cambiar_contrasena(actual_s,nuevo_s);
            }
        });



    }
    private void mostrarAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Contraseña Actualizada exitosamente")
                .setMessage("Se actualizó correctamente la contraseña")
                .setPositiveButton("Entendido", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss(); // Cierra el diálogo
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    private void cambiar_contrasena(String actual, String nuevo) {
        AuthCredential credential = EmailAuthProvider.getCredential(correo, actual);
        user.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        cambiarContraseñaNueva(nuevo);
                    } else {
                        Toast.makeText(this, "Error de autenticación. Verifica tu contraseña actual.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void cambiarContraseñaNueva(String nuevo) {
        user.updatePassword(nuevo)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        mostrarAlertDialog();
                        Intent intent = new Intent(Popup.this, Perfil.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(this, "Error al cambiar la contraseña.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
