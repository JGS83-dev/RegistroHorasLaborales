package com.dsm.registro.biometrico.ui.lugares

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.dsm.registro.biometrico.R
import com.dsm.registro.biometrico.databinding.FragmentRegistrarDireccionBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.firebase.database.DatabaseReference

class RegistrarDireccion : Fragment(R.layout.fragment_registrar_direccion) {

    companion object {
        fun newInstance() = RegistrarDireccion()
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ).toTypedArray()
    }

    private lateinit var viewModel: RegistrarDireccionViewModel
    private var _binding: FragmentRegistrarDireccionBinding? = null
    var BtnGuardar: Button? = null
    var BtnUbicacion: Button? = null
    private lateinit var database: DatabaseReference
    private val binding get() = _binding!!
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegistrarDireccionBinding.inflate(inflater, container, false)
        val root: View = binding.root

        BtnGuardar = binding.btnGuardar
        BtnUbicacion = binding.btnDireccion

        BtnUbicacion!!.setOnClickListener {
            Log.d("Location", "Solicitando ubicacion actual")
            if (allPermissionsGranted()) {
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
                val cancellationTokenSource = CancellationTokenSource()
                if (ActivityCompat.checkSelfPermission(
                        requireContext(),
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        requireContext(),
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    fusedLocationClient.getCurrentLocation(100, cancellationTokenSource.token)
                        .addOnSuccessListener { location ->
                            Log.d("Location", "Ubicacion actual ${location.altitude} , ${location.longitude}")
                            Toast.makeText(context,
                                "Ubicacion actual obtenida exitosamente",
                                Toast.LENGTH_LONG).show()
                        }
                        .addOnFailureListener { exception ->
                            Log.d("Location", "Oops location failed with exception: $exception")
                        }
                }
            } else {
                Log.d("Permissions", "Permisos no concedidos")
                ActivityCompat.requestPermissions(requireActivity(),
                    REQUIRED_PERMISSIONS,
                    REQUEST_CODE_PERMISSIONS
                )
            }
        }

        return root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(RegistrarDireccionViewModel::class.java)
        // TODO: Use the ViewModel
    }
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
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

}