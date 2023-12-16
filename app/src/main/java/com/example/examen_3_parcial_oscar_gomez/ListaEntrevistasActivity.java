package com.example.examen_3_parcial_oscar_gomez;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.examen_3_parcial_oscar_gomez.configuracion.Entrevista;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListaEntrevistasActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EntrevistaAdapter entrevistaAdapter;
    private List<Entrevista> listaEntrevistas;
    private Button btnRegresar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_entrevistas);

        recyclerView = findViewById(R.id.recyclerViewEntrevistas);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        btnRegresar = findViewById(R.id.btnRegresar);
        obtenerListaEntrevistas();

        btnRegresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent); // Inicia la nueva actividad
            }
        });

        entrevistaAdapter = new EntrevistaAdapter(this, listaEntrevistas);
        recyclerView.setAdapter(entrevistaAdapter);

        entrevistaAdapter.setOnItemClickListener(new EntrevistaAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                // Manejar clics normales si es necesario

            }

            @Override
            public void onEliminarClick(int position) {
                eliminarEntrevista(position);
            }

            @Override
            public void onReproducirClick(int position) {
                reproducirAudio(position);
            }
        });
    }

    private void obtenerListaEntrevistas() {
        listaEntrevistas = new ArrayList<>();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("entrevistas")
                .get()
                .addOnCompleteListener(task -> {
                    try {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Entrevista entrevista = document.toObject(Entrevista.class);
                                listaEntrevistas.add(entrevista);
                            }
                            // Invertir el orden de la lista
                            Collections.reverse(listaEntrevistas);
                            actualizarRecyclerView();
                        } else {
                            Toast.makeText(
                                    ListaEntrevistasActivity.this,
                                    "Error al obtener documentos: " + task.getException(),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    } catch (Exception e) {
                        Log.e("ListaEntrevistasActivity", "Error en onComplete: " + e.getMessage(), e);
                        Toast.makeText(ListaEntrevistasActivity.this, "Error en onComplete: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void actualizarRecyclerView() {
        if (listaEntrevistas != null && !listaEntrevistas.isEmpty()) {
            entrevistaAdapter.notifyDataSetChanged();

        } else {
            Toast.makeText(ListaEntrevistasActivity.this, "No hay datos disponibles", Toast.LENGTH_SHORT).show();
        }
    }

    private void eliminarEntrevista(int position) {
        Log.d("EliminarEntrevista", "Intento de eliminar entrevista en posición: " + position);

        if (listaEntrevistas != null && position >= 0 && position < listaEntrevistas.size()) {
            Entrevista entrevista = listaEntrevistas.get(position);

            if (entrevista != null && entrevista.getId() != null && !entrevista.getId().isEmpty()) {
                String entrevistaId = entrevista.getId();
                Log.d("EliminarEntrevista", "ID del documento a eliminar: " + entrevistaId);

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("entrevistas").document(entrevistaId)
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            // Elimina el elemento de la lista local
                            listaEntrevistas.remove(position);

                            // Notifica al adaptador sobre el cambio
                            entrevistaAdapter.notifyItemRemoved(position);

                            Toast.makeText(ListaEntrevistasActivity.this, "Entrevista eliminada con éxito", Toast.LENGTH_SHORT).show();
                            Log.d("EliminarEntrevista", "Entrevista eliminada con éxito en Firebase");
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(
                                    ListaEntrevistasActivity.this,
                                    "Error al eliminar entrevista: " + e.getMessage(),
                                    Toast.LENGTH_SHORT
                            ).show();
                            Log.e("EliminarEntrevista", "Error al eliminar entrevista", e);
                        });

            } else {
                Toast.makeText(ListaEntrevistasActivity.this, "Entrevista, ID o fecha nulo o vacío", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(ListaEntrevistasActivity.this, "Posición no válida", Toast.LENGTH_SHORT).show();
        }
    }

    private void reproducirAudio(int position) {
        Log.d("ListaEntrevistasActivity", "Intento de reproducir audio en posición: " + position);

        if (listaEntrevistas != null && position >= 0 && position < listaEntrevistas.size()) {
            Entrevista entrevista = listaEntrevistas.get(position);

            if (entrevista != null && entrevista.getAudioBase64() != null && !entrevista.getAudioBase64().isEmpty()) {
                String audioBase64 = entrevista.getAudioBase64();
                byte[] audioBytes = Base64.decode(audioBase64, Base64.DEFAULT);

                try {
                    // Guarda el audio en un archivo temporal
                    File tempAudioFile = File.createTempFile("temp_audio", ".mp3", getCacheDir());
                    FileOutputStream fos = new FileOutputStream(tempAudioFile);
                    fos.write(audioBytes);
                    fos.close();

                    // Reproduce el audio usando MediaPlayer
                    MediaPlayer mediaPlayer = new MediaPlayer();
                    mediaPlayer.setDataSource(tempAudioFile.getAbsolutePath());
                    mediaPlayer.prepare();
                    mediaPlayer.start();

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error al reproducir el audio", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "El audio no está disponible", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
