package com.dsm.registro.biometrico.clases.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dsm.registro.biometrico.R
import com.dsm.registro.biometrico.clases.LugarTrabajo

class LugarTrabajoAdapter (private val context:Context, private val dataSet: List<LugarTrabajo>,private val listener:LugarTrabajoListener) :
    RecyclerView.Adapter<LugarTrabajoAdapter.ViewHolder>(){

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtNombre: TextView
        val txtLugar: TextView
        val txtHoraInicio: TextView
        val txtHoraFin: TextView
        val txtMasInfo: TextView
        val txtEstado: TextView

        init {
            txtNombre = view.findViewById(R.id.txtNombre)
            txtLugar = view.findViewById(R.id.txtLugar)
            txtHoraInicio = view.findViewById(R.id.txtHoraInicio)
            txtHoraFin = view.findViewById(R.id.txtHoraFin)
            txtMasInfo = view.findViewById(R.id.txtMasInfo)
            txtEstado = view.findViewById(R.id.txtEstado)
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
        holder.txtHoraInicio.setText(lugarTrabajo.entrada)
        holder.txtHoraFin.setText(lugarTrabajo.salida)
        holder.txtEstado.setText(lugarTrabajo.estado)

        holder.txtMasInfo.setOnClickListener{
            listener.onItemClick(dataSet.get(position))
        }
    }

    interface LugarTrabajoListener {
        fun onItemClick(lugarTrabajo: LugarTrabajo)
    }

    override fun getItemCount() = dataSet.size

}