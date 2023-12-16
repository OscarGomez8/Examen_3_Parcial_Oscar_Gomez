package com.example.examen_3_parcial_oscar_gomez;

import static android.app.PendingIntent.getActivity;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import android.Manifest;

import org.apache.commons.io.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST = 100;
    private static final int IMAGE_CAPTURE_REQUEST = 101;
    private static final int WRITE_EXTERNAL_STORAGE_PERMISSION_CODE = 1;

    private EditText txtId, txtDescripcion, txtPeriodista, txtFecha;
        private Button btnArchivar;
    private ImageButton imgBtnTomarFotografia;
    private ImageView imagenCapturada;
    private FirebaseFirestore db;
    private Button btnStartRecording, btnVerLista;
    private Button btnStopRecording;
    private MediaRecorder mediaRecorder;
    private String audioFilePath;
    private static final int RECORD_AUDIO_PERMISSION_CODE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicialización de los EditText, Button y FirebaseFirestore
        txtId = findViewById(R.id.txtId);
        txtDescripcion = findViewById(R.id.txtDescripcion);
        txtPeriodista = findViewById(R.id.txtPeriodista);
        txtFecha = findViewById(R.id.txtFecha);
        btnArchivar = findViewById(R.id.btnArchivar);
        imgBtnTomarFotografia = findViewById(R.id.btnTomarFoto);
        imagenCapturada = findViewById(R.id.imagen);
        db = FirebaseFirestore.getInstance();
        btnStartRecording = findViewById(R.id.btnInicioGrabacion);
        btnStopRecording = findViewById(R.id.btnDetenerGrabacion);
        btnVerLista = findViewById(R.id.btnVerLista);

        btnVerLista.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ListaEntrevistasActivity.class);
                startActivity(intent); // Inicia la nueva actividad
            }
        });


        btnStartRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!hasRecordPermission()) {
                    requestRecordPermission();
                } else {
                    startRecording();
                    btnStartRecording.setVisibility(View.GONE);
                    btnStopRecording.setVisibility(View.VISIBLE);
                }
            }
        });

        btnStopRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecording();
                btnStopRecording.setVisibility(View.GONE);
                btnStartRecording.setVisibility(View.VISIBLE);
            }
        });


        btnArchivar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtener los valores de los EditText
                String id = txtId.getText().toString().trim();
                String descripcion = txtDescripcion.getText().toString().trim();
                String periodista = txtPeriodista.getText().toString().trim();
                String fecha = txtFecha.getText().toString().trim();
                String audioBase64 = convertAudioToBase64(audioFilePath);

                // Validación simple de los campos (puedes agregar más validaciones según tu lógica)
                if (id.isEmpty() || descripcion.isEmpty() || periodista.isEmpty() || fecha.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                } else {
                    // Crear un mapa con los datos a guardar en Firestore
                    Map<String, Object> data = new HashMap<>();
                    data.put("id", id);
                    data.put("descripcion", descripcion);
                    data.put("periodista", periodista);
                    data.put("fecha", fecha);
                    data.put("audioBase64", audioBase64);

                    // Obtener la imagen capturada como Base64
                    Bitmap bitmap = ((BitmapDrawable) imagenCapturada.getDrawable()).getBitmap();
                    String imagenBase64 = convertBitmapToBase64(bitmap);

                    // Agregar la imagen en formato Base64 al mapa de datos
                    data.put("imagenBase64", imagenBase64);

                    // Guardar los datos en Firestore
                    db.collection("entrevistas")
                            .add(data)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Toast.makeText(MainActivity.this, "Datos ingresados correctamente", Toast.LENGTH_SHORT).show();
                                    // Aquí puedes realizar otras acciones después de ingresar los datos exitosamente

                                    // Limpiar los campos después de ingresar la información
                                    txtId.setText("");
                                    txtDescripcion.setText("");
                                    txtPeriodista.setText("");
                                    txtFecha.setText("");
                                    imagenCapturada.setImageResource(android.R.color.transparent); // Limpiar la imagen
                                    audioFilePath = null; // Limpiar la ruta del archivo de audio
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(MainActivity.this, "Error al ingresar datos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });


        // Configuración del OnClickListener para el botón de la imagen
        imgBtnTomarFotografia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Solicitar permisos para la cámara
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA},
                            CAMERA_PERMISSION_REQUEST);
                } else {
                    abrirCamara();
                }
            }
        });
    }

    // Método para abrir la cámara
    private void abrirCamara() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, IMAGE_CAPTURE_REQUEST);
        }
    }

    // Manejo de la respuesta a la solicitud de permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                abrirCamara();
            } else {
                Toast.makeText(this, "Permiso denegado para la cámara", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == RECORD_AUDIO_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido
                btnStartRecording.setVisibility(View.GONE);
                btnStopRecording.setVisibility(View.VISIBLE);
                Toast.makeText(this, "Permiso de grabación de audio concedido", Toast.LENGTH_SHORT).show();
                startRecording(); // Inicia la grabación después de conceder el permiso
            } else {
                // Permiso denegado
                Toast.makeText(this, "Permiso de grabación de audio denegado", Toast.LENGTH_SHORT).show();
                // Aquí puedes manejar el caso cuando el usuario rechaza el permiso
            }
        }
    }

    // Manejo del resultado de la captura de la imagen
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_CAPTURE_REQUEST && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imagenCapturada.setImageBitmap(imageBitmap);
        }
    }

    // Método para convertir un Bitmap a cadena Base64
    private String convertBitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos); // Ajusta el formato y calidad según tu imagen
        byte[] byteArrayImage = baos.toByteArray();
        return Base64.encodeToString(byteArrayImage, Base64.DEFAULT);
    }

    private void startRecording() {
        
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        // Generar un nombre de archivo único utilizando la marca de tiempo actual
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        audioFilePath = getExternalCacheDir().getAbsolutePath() + "/audio_" + timestamp + ".3gp";
        Log.d("AudioRecording", "Archivo de audio: " + audioFilePath);
        mediaRecorder.setOutputFile(audioFilePath);

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            Toast.makeText(this, "Grabando audio...", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Verificar permisos de almacenamiento
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_EXTERNAL_STORAGE_PERMISSION_CODE);
        }

// Verificar permisos de grabación de audio
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    RECORD_AUDIO_PERMISSION_CODE);
        }

    }


    private void stopRecording() {
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;
        Toast.makeText(this, "Grabación finalizada", Toast.LENGTH_SHORT).show();
    }

    private boolean hasRecordPermission() {
        return checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestRecordPermission() {
        requestPermissions(new String[]{android.Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_PERMISSION_CODE);
    }

    private String convertAudioToBase64(String audioFilePath) {
        String base64Audio = "";
        try {
            File file = new File(audioFilePath);
            byte[] audioBytes = FileUtils.readFileToByteArray(file);
            base64Audio = Base64.encodeToString(audioBytes, Base64.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al convertir audio a Base64: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return base64Audio;
    }


}
