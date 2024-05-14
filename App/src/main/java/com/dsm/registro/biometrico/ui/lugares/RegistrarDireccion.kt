package com.dsm.registro.biometrico.ui.lugares

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieAnimationView
import com.dsm.registro.biometrico.R
import com.dsm.registro.biometrico.databinding.FragmentRegistrarDireccionBinding
import com.dsm.registro.biometrico.utils.Utils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.io.File
import java.io.FileInputStream
import java.util.Calendar


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
    var BtnImagenReferencia: LottieAnimationView? = null
    var DiaAsistenciaEt: EditText? = null
    var HoraEntradaEt: EditText? = null
    var HoraSalidaEt: EditText? = null
    var NombreDireccionEt: EditText? = null

    var dia = " "
    var entrada = " "
    var salida = " "
    var nombre = ""
    var uriImagen: String? = ""

    var firebaseAuth: FirebaseAuth? = null
    var storage: FirebaseStorage? = null
    var timePicker: TimePickerDialog? = null
    var datePicker: DatePickerDialog? = null
    val Datos = HashMap<String, String?>()

    private val binding get() = _binding!!
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            uriImagen = context?.let { Utils.getRealPathFromURI(it,uri) }
            Log.d("PhotoPicker", "Selected URI: $uri")
        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegistrarDireccionBinding.inflate(inflater, container, false)
        val root: View = binding.root

        BtnGuardar = binding.btnGuardar
        BtnUbicacion = binding.btnDireccion
        BtnImagenReferencia = binding.agregarImagenBtn

        firebaseAuth = FirebaseAuth.getInstance()
        storage = Firebase.storage

        DiaAsistenciaEt = binding.txtDiaActividad
        HoraEntradaEt = binding.txtHoraEntrada
        HoraSalidaEt = binding.txtHoraSalida
        NombreDireccionEt = binding.txtNombreLugar

        HoraEntradaEt!!.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour: Int = calendar.get(Calendar.HOUR_OF_DAY)
            val minutes: Int = calendar.get(Calendar.MINUTE)

            timePicker = TimePickerDialog(
                requireContext(),
                { tp, sHour, sMinute -> HoraEntradaEt!!.setText(String.format("%02d:%02d", sHour, sMinute)) },
                hour,
                minutes,
                true
            )
            timePicker!!.show()
        }

        DiaAsistenciaEt!!.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            datePicker = DatePickerDialog(
                requireContext(),

                DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    val mes = monthOfYear +1
                    DiaAsistenciaEt!!.setText(String.format("%02d/%02d/%04d", dayOfMonth, mes,year))
                },
                year,
                day,
                month
            )
            datePicker!!.show()
        }

        HoraSalidaEt!!.setOnClickListener{
            val calendar = Calendar.getInstance()
            val hour: Int = calendar.get(Calendar.HOUR_OF_DAY)
            val minutes: Int = calendar.get(Calendar.MINUTE)

            timePicker = TimePickerDialog(requireContext(),
                { tp, sHour, sMinute -> HoraSalidaEt!!.setText(String.format("%02d:%02d", sHour, sMinute)) }, hour, minutes, true
            )
            timePicker!!.show()
        }

        BtnImagenReferencia!!.setOnClickListener {
            Toast.makeText(context, "Seleccione imagen de referencia del lugar", Toast.LENGTH_SHORT).show()
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

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
                            Log.d("Location", "Ubicacion actual ${location.latitude} , ${location.longitude}")
                            Datos["latitud"] = location.latitude.toString()
                            Datos["longitud"] = location.longitude.toString()
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

        BtnGuardar!!.setOnClickListener {
            ValidarDatos()
        }

        return root
    }
    private fun ValidarDatos() {
        dia = DiaAsistenciaEt!!.text.toString()
        entrada = HoraEntradaEt!!.text.toString()
        salida = HoraSalidaEt!!.text.toString()
        nombre = NombreDireccionEt!!.text.toString()

        if (TextUtils.isEmpty(dia)) {
            Toast.makeText(context, "Ingrese día de asistencia", Toast.LENGTH_SHORT).show()
        } else if (TextUtils.isEmpty(entrada)) {
            Toast.makeText(context, "Ingrese hora de entrada", Toast.LENGTH_SHORT).show()
        } else if (TextUtils.isEmpty(salida)) {
            Toast.makeText(context, "Ingrese hora de salida", Toast.LENGTH_SHORT).show()
        } else if (TextUtils.isEmpty(nombre)) {
            Toast.makeText(context, "Ingrese nombre de la dirección", Toast.LENGTH_SHORT).show()
        }else {
            CrearRegistro()
        }
    }

    private fun CrearRegistro() {
        val uid = firebaseAuth!!.currentUser?.uid.toString()
        Datos["uid"] = uid
        Datos["dia"] = dia
        Datos["entrada"] = entrada
        Datos["entrada_real"] = ""
        Datos["salida_real"] = ""
        Datos["salida"] = salida
        Datos["nombre"] = nombre
        Datos["estado"] = "pendiente"

        val storageRef = storage!!.reference
        val imagenUsuarioRef = storageRef.child("/imagenes/usuarios/$uid/lugar")
        val stream = FileInputStream(uriImagen?.let { File(it) })
        var uploadTask = imagenUsuarioRef.putStream(stream)

        uploadTask.addOnFailureListener {
            Log.e("Error al subir imagen", it.message.toString())
        }.addOnSuccessListener { taskSnapshot ->

            imagenUsuarioRef.downloadUrl.addOnCompleteListener {
                Log.i("Exito al subir imagen", it.result.toString())
                Datos["imagen"] = it.result.toString()
                val databaseReference = FirebaseDatabase.getInstance().getReference("Direcciones")
                databaseReference.child(uid!!)
                    .push()
                    .setValue(Datos)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Dirección de trabajo registrada con éxito", Toast.LENGTH_SHORT)
                            .show()
                        findNavController().navigate(R.id.action_navigation_registrar_direccion_to_navigation_home)
                    }.addOnFailureListener { e ->
                        Toast.makeText(
                            context,
                            "Ocurrio un error al registrar dirección de trabajo. " + e.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }
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