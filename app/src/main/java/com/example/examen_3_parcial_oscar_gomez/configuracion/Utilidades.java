package com.example.examen_3_parcial_oscar_gomez.configuracion;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

public class Utilidades {
    public static Bitmap base64ToBitmap(String base64String) {
        if (base64String == null) {
            // Manejar el caso cuando la cadena es nula (puedes devolver un valor predeterminado o lanzar una excepción según tus necesidades)
            return null;
        }

        byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }
}
