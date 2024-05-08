package com.dsm.registro.biometrico.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dsm.registro.biometrico.R
import com.dsm.registro.biometrico.clases.LugarTrabajo
import com.dsm.registro.biometrico.clases.adapters.LugarTrabajoAdapter
import com.dsm.registro.biometrico.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    lateinit var database: DatabaseReference
    lateinit var firebaseAuth: FirebaseAuth

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

        database = Firebase.database.reference
        var lugares = ArrayList<LugarTrabajo>()
        val uid = firebaseAuth!!.currentUser?.uid.toString()
        database.child("Direcciones").child(uid).get().addOnSuccessListener {
            it.children.forEach{
                val lugar = "${it.child("latitud").value.toString()} , ${it.child("longitud").value.toString()}"
                lugares.add(LugarTrabajo(
                    it.key.toString(),
                    it.child("nombre").value.toString(),
                    lugar,
                    it.child("entrada").value.toString(),
                    it.child("salida").value.toString(),
                    it.child("estado").value.toString()))
            }

            val recyclerViewLugares: RecyclerView = binding.recyclerHoy
            recyclerViewLugares.layoutManager = LinearLayoutManager(context)

            val lugaresAdapter = LugarTrabajoAdapter(requireContext(),lugares);

            recyclerViewLugares.adapter = lugaresAdapter
        }.addOnFailureListener{
            Log.e("firebase", "Error getting data", it)
        }

        return root
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val TAG = "CameraXApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
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