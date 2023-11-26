package com.example.bit_3;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/** @noinspection FieldMayBeFinal*/
public class Perfil extends AppCompatActivity {

    TextView correo,nombre;
    String fieldValue,documentId;
    CollectionReference collectionRef;
    Button sesion,si,no;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    String userUid;
    public void readSingleField(String documentId) {
        // Obtén la referencia al documento
        DocumentReference docRef = db.collection("administradores").document(documentId);

        // Obtén solo el campo específico del documento
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // El documento existe, obtén el valor del campo específico
                    fieldValue = document.get("nombre").toString();
                    nombre.setText(fieldValue);


                } else {
                    // El documento no existe
                    // Manejar el caso en que el documento no existe
                    System.out.println("El documento con ID " + documentId + " no existe.");
                }
            } else {
                // Error al obtener los datos
                // Manejar el error
                System.out.println("Error al obtener datos: " + task.getException());
            }
        });
    }
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);
        nombre = findViewById(R.id.nombre_t);
        correo = findViewById(R.id.correo_t);
        userUid = SharedPreferencesUtil.getUserUidFromSharedPreferences(this);
        correo.setText(userUid);
        FirebaseUser currentUser = auth.getCurrentUser();
        documentId = currentUser.getUid();
        readSingleField(documentId);
        sesion = findViewById(R.id.cerrar_sesion);
        sesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mostrarAlertDialog();
            }
        });


}
    private void mostrarAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Cerrar Sesión")
                .setMessage("¿Quieres continuar?")
                .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        auth.signOut();
                        Intent intent = new Intent(Perfil.this, LoginActivity.class);
                        startActivity(intent);
                        dialog.dismiss(); // Cierra el diálogo
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss(); // Cierra el diálogo
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}

