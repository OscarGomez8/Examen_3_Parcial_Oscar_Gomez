package com.example.examen_3_parcial_oscar_gomez;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.examen_3_parcial_oscar_gomez.configuracion.Entrevista;
import com.example.examen_3_parcial_oscar_gomez.configuracion.Utilidades;

import java.util.List;

public class EntrevistaAdapter extends RecyclerView.Adapter<EntrevistaAdapter.EntrevistaViewHolder> {
    private Context context;
    private List<Entrevista> listaEntrevistas;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onEliminarClick(int position);
        void onReproducirClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public EntrevistaAdapter(Context context, List<Entrevista> listaEntrevistas) {
        this.context = context;
        this.listaEntrevistas = listaEntrevistas;
    }

    @NonNull
    @Override
    public EntrevistaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_entrevista, parent, false);
        return new EntrevistaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EntrevistaViewHolder holder, int position) {
        Entrevista entrevista = listaEntrevistas.get(position);

        holder.txtId.setText(entrevista.getId());
        holder.txtDescripcion.setText(entrevista.getDescripcion());
        Bitmap imagenBitmap = Utilidades.base64ToBitmap(entrevista.getImagenBase64());
        holder.imageViewEntrevista.setImageBitmap(imagenBitmap);

        // Botón de eliminar
        holder.btnEliminar.setOnClickListener(view -> {
            if (mListener != null) {
                mListener.onEliminarClick(position);
            }
        });

        // Botón de reproducir
        holder.btnReproducir.setOnClickListener(view -> {
            if (mListener != null) {
                mListener.onReproducirClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaEntrevistas.size();
    }

    public static class EntrevistaViewHolder extends RecyclerView.ViewHolder {
        TextView txtId, txtDescripcion;
        ImageView imageViewEntrevista;
        ImageButton btnEliminar, btnReproducir;

        public EntrevistaViewHolder(@NonNull View itemView) {
            super(itemView);
            txtId = itemView.findViewById(R.id.text_id);
            txtDescripcion = itemView.findViewById(R.id.textViewDescripcion1);
            imageViewEntrevista = itemView.findViewById(R.id.imageViewEntrevista);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
            btnReproducir = itemView.findViewById(R.id.btnReproducir);
        }
    }
}
