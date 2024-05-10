package com.dsm.registro.biometrico.ui.lugares

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.dsm.registro.biometrico.R
import com.dsm.registro.biometrico.clases.LugarTrabajo
import com.dsm.registro.biometrico.databinding.FragmentInformacionDireccionBinding
import com.dsm.registro.biometrico.utils.Utils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.squareup.picasso.Picasso
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class InformacionDireccionFragment : Fragment(R.layout.fragment_informacion_direccion) {

    companion object {
        fun newInstance() = InformacionDireccionFragment()
        private const val TAG = "CameraXApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }

    private lateinit var viewModel: InformacionDireccionViewModel
    private var _binding: FragmentInformacionDireccionBinding? = null
    private lateinit var database: DatabaseReference
    private val binding get() = _binding!!
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    var firebaseAuth: FirebaseAuth? = null
    var storage: FirebaseStorage? = null

    var BtnTomarBiometrico: Button? = null
    var txtHoraFinReal: TextView? = null
    var txtHoraFin: TextView? = null
    var txtHoraInicioReal: TextView? = null
    var txtHoraInicio: TextView? = null
    var txtNombreLugar: TextView? = null
    var txtUbicacion: TextView? = null

    val infoLugar = LugarTrabajo()

    var fotoTomada: InputImage? = null
    var fotoGuardada: InputImage? = null
    var urlFotoGuardada = ""

    private var imageCapture: ImageCapture? = null

    private lateinit var cameraExecutor: ExecutorService

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInformacionDireccionBinding.inflate(inflater, container, false)
        val root: View = binding.root

        firebaseAuth = FirebaseAuth.getInstance()
        database = Firebase.database.reference

        database.child("InformacionLugar").child(firebaseAuth!!.currentUser?.uid.toString()).get().addOnSuccessListener {
            Log.i("Informacion del lugar visualizado -> LLave",it.key.toString())
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

            BtnTomarBiometrico = binding.btnTomarBiometria

            txtHoraFinReal = binding.txtHoraFinReal
            txtHoraFin = binding.txtHoraFin
            txtHoraInicioReal = binding.txtHoraEntradaReal
            txtHoraInicio = binding.txtHoraEntrada
            txtNombreLugar = binding.txtNombreLugar
            txtUbicacion = binding.txtUbicacion

            txtHoraFinReal!!.text = infoLugar.salida_real
            txtHoraFin!!.text = infoLugar.salida
            txtHoraInicioReal!!.text = infoLugar.entrada_real
            txtHoraInicio!!.text = infoLugar.entrada
            txtNombreLugar!!.text = infoLugar.nombre
            txtUbicacion!!.text = infoLugar.lugar

            Picasso.get().load(infoLugar.imagen).into(binding.imagenReferencia);

            val df = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val lugarDate = LocalDate.parse(infoLugar.dia, df)
            val nowDate = LocalDate.parse(Utils.getNowDate(), df)

            if(lugarDate.isEqual(nowDate)){
                if(infoLugar.estado =="pendiente"){
                    BtnTomarBiometrico!!.visibility = View.VISIBLE
                    binding.viewFinder.visibility = View.VISIBLE
                    BtnTomarBiometrico!!.setOnClickListener {
                        Toast.makeText(context, "Seleccione o tome una foto de acuerdo a la subida de referencia en su perfil", Toast.LENGTH_LONG).show()
                        startCamera()
                    }
                }
            }

        }.addOnFailureListener{
            Log.e("firebase", "Error getting user data", it)
        }

        database.child("Usuarios").child(firebaseAuth!!.currentUser?.uid.toString()).get().addOnSuccessListener {
            urlFotoGuardada = it.child("biometria").value.toString()
        }.addOnFailureListener{
            Log.e("firebase", "Error getting user data", it)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(InformacionDireccionViewModel::class.java)
        // TODO: Use the ViewModel
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview)

            } catch(exc: Exception) {
                Log.e("Camera", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val callback = object : OnBackPressedCallback(
            true // default to enabled
        ) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.navigation_home)
                database.child("InformacionLugar").removeValue()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            this, // LifecycleOwner
            callback
        )
    }

    val highAccuracyOpts = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .build()

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

}