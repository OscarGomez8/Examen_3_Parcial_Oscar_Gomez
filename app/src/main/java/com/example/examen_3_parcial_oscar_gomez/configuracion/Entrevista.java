package com.example.examen_3_parcial_oscar_gomez.configuracion;

public class Entrevista {
    private String id;
    private String descripcion;
    private String periodista;
    private String fecha;
    private String imagenBase64;
    private String audioBase64; // Agregado el campo para el audio

    public Entrevista() {
        // Constructor vacío requerido por Firestore
    }

    public Entrevista(String id, String descripcion, String periodista, String fecha, String imagenBase64, String audioBase64) {
        this.id = id;
        this.descripcion = descripcion;
        this.periodista = periodista;
        this.fecha = fecha;
        this.imagenBase64 = imagenBase64;
        this.audioBase64 = audioBase64;
    }

    // Getters y setters para los campos

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getPeriodista() {
        return periodista;
    }

    public void setPeriodista(String periodista) {
        this.periodista = periodista;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getImagenBase64() {
        return imagenBase64;
    }

    public void setImagenBase64(String imagenBase64) {
        this.imagenBase64 = imagenBase64;
    }

    public String getAudioBase64() {
        return audioBase64;
    }

    public void setAudioBase64(String audioBase64) {
        this.audioBase64 = audioBase64;
    }
}
