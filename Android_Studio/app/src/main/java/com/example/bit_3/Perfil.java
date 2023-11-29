package com.example.bit_3;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/** @noinspection ALL */
public class Perfil extends AppCompatActivity {

    TextView correo, nombre;
    EditText actual, nuevo;
    String fieldValue, documentId;
    CollectionReference collectionRef;
    Button sesion, cambiar_contr, aceptar;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser user = auth.getCurrentUser();
    AuthCredential credential;
    String userUid;
    ImageButton cerrar;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);
        nombre = findViewById(R.id.nombre_t);
        correo = findViewById(R.id.correo_t);
        userUid = SharedPreferencesUtil.getUserUidFromSharedPreferences(this);
        correo.setText(userUid);
        documentId = user.getUid();
        readSingleField(documentId);
        sesion = findViewById(R.id.cerrar_sesion);
        cambiar_contr = findViewById(R.id.contra);

        sesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mostrarAlertDialog();
            }
        });


        cambiar_contr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Perfil.this, Popup.class);
                startActivity(intent);
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
                    System.out.println("El documento con ID " + documentId + " no existe.");
                }
            } else {
                System.out.println("Error al obtener datos: " + task.getException());
            }
        });
    }
}
