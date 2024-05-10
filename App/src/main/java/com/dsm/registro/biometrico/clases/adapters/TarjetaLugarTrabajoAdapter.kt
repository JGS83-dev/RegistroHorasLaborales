package com.dsm.registro.biometrico.clases.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dsm.registro.biometrico.R
import com.dsm.registro.biometrico.clases.LugarTrabajo
import com.squareup.picasso.Picasso

class TarjetaLugarTrabajoAdapter (private val context: Context, private val dataSet: List<LugarTrabajo>,
                                  private val listener: LugarTrabajoListener) :
    RecyclerView.Adapter<TarjetaLugarTrabajoAdapter.ViewHolder>(){

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtTitulo: TextView
        val btnImagen: ImageView

        init {
            txtTitulo = view.findViewById(R.id.lblNombreLugar)
            btnImagen = view.findViewById(R.id.imagenReferenciaLugar)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int) : ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.lugar_layout_card, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val lugarTrabajo: LugarTrabajo = dataSet.get(position)
        holder.txtTitulo.setText(lugarTrabajo.nombre)

        Picasso.get().load(lugarTrabajo.imagen).into(holder.btnImagen);

        holder.btnImagen.setOnClickListener{
            listener.onItemClick(dataSet.get(position))
        }
    }

    interface LugarTrabajoListener {
        fun onItemClick(lugarTrabajo: LugarTrabajo)
    }

    override fun getItemCount() = dataSet.size
}