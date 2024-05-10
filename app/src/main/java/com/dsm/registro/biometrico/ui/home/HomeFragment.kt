package com.dsm.registro.biometrico.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dsm.registro.biometrico.R
import com.dsm.registro.biometrico.clases.LugarTrabajo
import com.dsm.registro.biometrico.clases.adapters.LugarTrabajoAdapter
import com.dsm.registro.biometrico.clases.adapters.LugarTrabajoRecientesAdapter
import com.dsm.registro.biometrico.databinding.FragmentHomeBinding
import com.dsm.registro.biometrico.utils.Utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    lateinit var database: DatabaseReference
    lateinit var firebaseAuth: FirebaseAuth
    var uid = ""

    var BtnIniciarSesion: Button? = null
    var BtnCrearCuenta: Button? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        val root: View = binding.root
        firebaseAuth = FirebaseAuth.getInstance()

        if (allPermissionsGranted()) {
            Log.i("Permisos","Se han concedido los permisos")
        } else {
            ActivityCompat.requestPermissions(requireActivity(), REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        BtnIniciarSesion = binding.btnSesion
        BtnCrearCuenta = binding.btnCrearCuenta

        database = Firebase.database.reference
        var lugares = ArrayList<LugarTrabajo>()
        var recientes = ArrayList<LugarTrabajo>()
        uid = firebaseAuth!!.currentUser?.uid.toString()


        if(firebaseAuth!!.currentUser == null){
            BtnIniciarSesion!!.visibility = View.VISIBLE

            BtnIniciarSesion!!.setOnClickListener{
                findNavController().navigate(R.id.navigation_notifications)
            }
            BtnCrearCuenta!!.visibility = View.VISIBLE

            BtnCrearCuenta!!.setOnClickListener{
                findNavController().navigate(R.id.navigation_registrarse)
            }
        }

        //Actividades para hoy
        database.child("Direcciones").child(uid).get().addOnSuccessListener {
            it.children.forEach{

                val df = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                val lugarDate = LocalDate.parse(it.child("dia").value.toString(), df)
                val nowDate = LocalDate.parse(Utils.getNowDate(), df)

                if(nowDate.equals(lugarDate)){
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
            }
            val recyclerViewLugares: RecyclerView = binding.recyclerHoy
            recyclerViewLugares.layoutManager = LinearLayoutManager(context)

            val lugaresAdapter = LugarTrabajoAdapter(requireContext(),lugares,object : LugarTrabajoAdapter.LugarTrabajoListener {
                override fun onItemClick(lugar: LugarTrabajo) {
                    navegarAInformacionLugar(lugar)
                }
            })

            recyclerViewLugares.adapter = lugaresAdapter
        }.addOnFailureListener{
            Log.e("firebase", "Error getting data", it)
        }

        //Actividades Recientes
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

                recientes.add(infoLugar)
            }
            val recyclerViewLugaresRecientes: RecyclerView = binding.recyclerRecientes
            recyclerViewLugaresRecientes.layoutManager = LinearLayoutManager(context)

            val lugaresAdapterRecientes = LugarTrabajoRecientesAdapter(requireContext(),recientes,object : LugarTrabajoRecientesAdapter.LugarTrabajoListener {
                override fun onItemClick(lugar: LugarTrabajo) {
                    navegarAInformacionLugar(lugar)
                }
            })

            recyclerViewLugaresRecientes.adapter = lugaresAdapterRecientes
        }.addOnFailureListener{
            Log.e("firebase", "Error getting data", it)
        }

        return root
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
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

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                Toast.makeText(context,
                    "Permisos concedidos por el usuario.",
                    Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context,
                    "Permisos denegados por el usuario.",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}