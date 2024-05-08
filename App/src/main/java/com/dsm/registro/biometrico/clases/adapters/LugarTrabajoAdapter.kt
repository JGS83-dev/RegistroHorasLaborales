package com.dsm.registro.biometrico.clases.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.dsm.registro.biometrico.R
import com.dsm.registro.biometrico.clases.LugarTrabajo

class LugarTrabajoAdapter (private val context:Context, private val dataSet: List<LugarTrabajo>) :
    RecyclerView.Adapter<LugarTrabajoAdapter.ViewHolder>(){

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtNombre: TextView
        val txtLugar: TextView
        val txtHoraInicio: TextView
        val txtHoraFin: TextView
        val txtMasInfo: TextView

        init {
            txtNombre = view.findViewById(R.id.txtNombre)
            txtLugar = view.findViewById(R.id.txtLugar)
            txtHoraInicio = view.findViewById(R.id.txtHoraInicio)
            txtHoraFin = view.findViewById(R.id.txtHoraFin)
            txtMasInfo = view.findViewById(R.id.txtMasInfo)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.lugar_trabajo_layout, viewGroup, false)
        return ViewHolder(view)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val lugarTrabajo: LugarTrabajo = dataSet.get(position)
        holder.txtNombre.setText(lugarTrabajo.nombre)
        holder.txtLugar.setText(lugarTrabajo.lugar)
        holder.txtHoraInicio.setText(lugarTrabajo.horaInicio)
        holder.txtHoraFin.setText(lugarTrabajo.horaFin)

        holder.txtMasInfo.setOnClickListener{
            Toast.makeText(context,"Enviando a vista de lugar...",Toast.LENGTH_LONG).show()
        }
    }

    override fun getItemCount() = dataSet.size

}