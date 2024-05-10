package com.dsm.registro.biometrico.ui.dashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dsm.registro.biometrico.R
import com.dsm.registro.biometrico.clases.LugarTrabajo
import com.dsm.registro.biometrico.clases.adapters.TarjetaLugarTrabajoAdapter
import com.dsm.registro.biometrico.databinding.FragmentDashboardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    var BtnAgregar: Button? = null
    var BtnBuscar: Button? = null
    var firebaseAuth: FirebaseAuth? = null
    lateinit var database: DatabaseReference
    var uid = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        BtnAgregar = binding.btnAgregar
        BtnBuscar = binding.btnBuscar
        database = Firebase.database.reference
        firebaseAuth = FirebaseAuth.getInstance()

        uid = firebaseAuth!!.currentUser?.uid.toString()
        var lugares = ArrayList<LugarTrabajo>()

        database.child("Direcciones").child(uid).get().addOnSuccessListener {
            it.children.forEach{
                val infoLugar = LugarTrabajo()
                infoLugar.dia = it.child("dia").value.toString()
                infoLugar.nombre = it.child("nombre").value.toString()
                infoLugar.latitud = it.child("latitud").value.toString()
                infoLugar.longitud = it.child("longitud").value.toString()
                infoLugar.imagen = it.child("imagen").value.toString()
                infoLugar.estado = it.child("estado").value.toString()

                infoLugar.entrada = it.child("entrada").value.toString()
                infoLugar.salida = it.child("salida").value.toString()
                infoLugar.entrada_real = it.child("entrada_real").value.toString()
                infoLugar.salida_real = it.child("salida_real").value.toString()

                infoLugar.lugar = "${it.child("latitud").value.toString()} , ${it.child("longitud").value.toString()}"

                infoLugar.uid = it.key.toString()

                lugares.add(infoLugar)
            }
            val recyclerViewLugaresRecientes: RecyclerView = binding.recyclerLugares
            recyclerViewLugaresRecientes.layoutManager = LinearLayoutManager(context)

            val lugaresAdapterRecientes = TarjetaLugarTrabajoAdapter(requireContext(),lugares,object : TarjetaLugarTrabajoAdapter.LugarTrabajoListener {
                override fun onItemClick(lugar: LugarTrabajo) {
                    navegarAInformacionLugar(lugar)
                }
            })

            recyclerViewLugaresRecientes.adapter = lugaresAdapterRecientes
        }.addOnFailureListener{
            Log.e("firebase", "Error getting data", it)
        }

        BtnAgregar!!.setOnClickListener {
            if(firebaseAuth!!.currentUser != null){
                findNavController().navigate(R.id.action_navigation_dashboard_to_navigation_registrar_direccion)
            }else{
                Toast.makeText(context,"Debe iniciar sesión para realizar esta acción",Toast.LENGTH_LONG).show()
            }
        }

        return root
    }

    private fun navegarAInformacionLugar(lugar: LugarTrabajo) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("InformacionLugar")
        databaseReference.child(uid)
            .setValue(lugar)
            .addOnSuccessListener {
                findNavController().navigate(R.id.informacionDireccionFragment)
                Toast.makeText(context,"Enviando a vista de lugar...",Toast.LENGTH_LONG).show()
            }.addOnFailureListener { e ->
                Toast.makeText(context,"Ocurrió un error al cargar la información",Toast.LENGTH_LONG).show()
            }

    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}