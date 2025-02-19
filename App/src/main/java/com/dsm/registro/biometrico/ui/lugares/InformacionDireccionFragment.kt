package com.dsm.registro.biometrico.ui.lugares

import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.dsm.registro.biometrico.R
import com.dsm.registro.biometrico.clases.DataPost
import com.dsm.registro.biometrico.clases.LugarTrabajo
import com.dsm.registro.biometrico.databinding.FragmentInformacionDireccionBinding
import com.dsm.registro.biometrico.service.MailerApiService
import com.dsm.registro.biometrico.utils.Utils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceContour
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark
import com.squareup.picasso.Picasso
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.roundToInt


class InformacionDireccionFragment : Fragment(R.layout.fragment_informacion_direccion) {

    companion object {
        fun newInstance() = InformacionDireccionFragment()
        private const val TAG = "CameraXApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ).toTypedArray()
        private const val BASE_URL =
            "https://us-west1-registrohoraslaborales.cloudfunctions.net/api-mailer/api/"
    }

    private lateinit var viewModel: InformacionDireccionViewModel
    private var _binding: FragmentInformacionDireccionBinding? = null
    private lateinit var database: DatabaseReference
    private val binding get() = _binding!!
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    var firebaseAuth: FirebaseAuth? = null

    var BtnTomarBiometrico: Button? = null
    var txtHoraFinReal: TextView? = null
    var txtHoraFin: TextView? = null
    var txtHoraInicioReal: TextView? = null
    var txtHoraInicio: TextView? = null
    var txtNombreLugar: TextView? = null
    var txtUbicacion: TextView? = null
    var txtEstado: TextView? = null

    val infoLugar = LugarTrabajo()
    var urlFotoGuardada = ""

    var tomandoFoto:Boolean = false

    var ubicacionActual:Location? = null
    var ubicacionGuardada:Location? = null
    var uriFoto:Uri? = null

    private var imageCapture: ImageCapture? = null

    private lateinit var cameraExecutor: ExecutorService

    val highAccuracyOpts = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .build()

    val detector = FaceDetection.getClient(highAccuracyOpts)

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

            infoLugar.uid = it.child("uid").value.toString()

            BtnTomarBiometrico = binding.btnTomarBiometria

            txtHoraFinReal = binding.txtHoraFinReal
            txtHoraFin = binding.txtHoraFin
            txtHoraInicioReal = binding.txtHoraEntradaReal
            txtHoraInicio = binding.txtHoraEntrada
            txtNombreLugar = binding.txtNombreLugar
            txtUbicacion = binding.txtUbicacion
            txtEstado = binding.txtEstado

            txtHoraFinReal!!.text = infoLugar.salida_real
            txtHoraFin!!.text = infoLugar.salida
            txtHoraInicioReal!!.text = infoLugar.entrada_real
            txtHoraInicio!!.text = infoLugar.entrada
            txtNombreLugar!!.text = infoLugar.nombre
            txtUbicacion!!.text = infoLugar.lugar
            txtEstado!!.text = infoLugar.estado

            Picasso.get().load(infoLugar.imagen).into(binding.imagenReferencia);

            val df = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val lugarDate = LocalDate.parse(infoLugar.dia, df)
            val nowDate = LocalDate.parse(Utils.getNowDate(), df)

            if(lugarDate.isEqual(nowDate)){
                if(infoLugar.estado =="pendiente" || infoLugar.estado =="proceso"){
                    BtnTomarBiometrico!!.visibility = View.VISIBLE
                    binding.viewFinder.visibility = View.VISIBLE
                    BtnTomarBiometrico!!.setOnClickListener {
                        binding.viewFinder.visibility = View.VISIBLE
                        if(tomandoFoto){
                            takePhoto()
                        }else{
                            Toast.makeText(context, "Seleccione o tome una foto de acuerdo a la subida de referencia en su perfil", Toast.LENGTH_SHORT).show()
                            startCamera()
                        }

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
        tomandoFoto = true
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

            imageCapture = ImageCapture.Builder()
                .build()

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture)

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return
        Toast.makeText(context, "Capturando foto", Toast.LENGTH_SHORT).show()
        // Create time stamped name and MediaStore entry.
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions
                .Builder(requireContext().contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues)
            .build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
            imageCapture!!.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Fallo al tomar foto: ${exc.message}", exc)
                    tomandoFoto = false
                    cameraExecutor.shutdown()
                    binding.viewFinder.removeAllViews()
                }

                override fun
                        onImageSaved(output: ImageCapture.OutputFileResults){
                    val msg = "Foto tomada con exito: ${output.savedUri}"
                    uriFoto = output.savedUri
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)
                    tomandoFoto = false
                    cameraExecutor.shutdown()
                    binding.viewFinder.removeAllViews()
                    ObtenerUbicacionActual()
                }
            }
        )
    }

    private fun ValidarEntradaSalida(){
        ubicacionGuardada = Location("guardada")
        ubicacionGuardada!!.latitude = infoLugar.latitud.toDouble()
        ubicacionGuardada!!.longitude = infoLugar.longitud.toDouble()

        var distancia = ubicacionActual!!.distanceTo(ubicacionGuardada!!).roundToInt()
        Log.i("Distancia calculada",distancia.toString())
        Log.i("Ubicacion actual",ubicacionActual.toString())
        Log.i("Operacion",(distancia.compareTo(20)).toString())

        val calendar = Calendar.getInstance()
        val hour: Int = calendar.get(Calendar.HOUR_OF_DAY)
        val minutes: Int = calendar.get(Calendar.MINUTE)

        Log.i("Info ID",infoLugar.uid)

        if(distancia.compareTo(20) <= 0){
            val image: InputImage
            var conteoCaras = 0
            try {
                image = InputImage.fromFilePath(requireContext(), uriFoto!!)
                val result = detector.process(image)
                    .addOnSuccessListener { faces ->
                        if(faces.isEmpty()){
                            Toast.makeText(
                                context,
                                "No se detecto ningun rostro",
                                Toast.LENGTH_SHORT
                            ).show()
                        }else{
                            for (face in faces) {
                                conteoCaras += 1

                                val bounds = face.boundingBox
                                val rotY = face.headEulerAngleY // Head is rotated to the right rotY degrees
                                val rotZ = face.headEulerAngleZ // Head is tilted sideways rotZ degrees

                                // If landmark detection was enabled (mouth, ears, eyes, cheeks, and
                                // nose available):
                                val leftEar = face.getLandmark(FaceLandmark.LEFT_EAR)
                                leftEar?.let {
                                    val leftEarPos = leftEar.position
                                    Log.i("Objeto Face","leftEarPos -> $leftEarPos")
                                }

                                // If contour detection was enabled:
                                val leftEyeContour = face.getContour(FaceContour.LEFT_EYE)?.points
                                val upperLipBottomContour = face.getContour(FaceContour.UPPER_LIP_BOTTOM)?.points

                                // If classification was enabled:
                                if (face.smilingProbability != null) {
                                    val smileProb = face.smilingProbability
                                }
                                if (face.rightEyeOpenProbability != null) {
                                    val rightEyeOpenProb = face.rightEyeOpenProbability
                                }

                                // If face tracking was enabled:
                                if (face.trackingId != null) {
                                    val id = face.trackingId
                                }
                                Log.i("Objeto Face","bounds -> $bounds")
                                Log.i("Objeto Face","rotY -> $rotY")
                                Log.i("Objeto Face","rotZ -> $rotZ")
                                Log.i("Info Caras","Total caras detectado $conteoCaras")
                            }

                            if(infoLugar.estado == "pendiente"){
                                infoLugar.estado = "proceso"
                                infoLugar.entrada_real = String.format("%02d:%02d", hour, minutes)
                            }else if(infoLugar.estado == "proceso"){
                                infoLugar.estado = "finalizado"
                                infoLugar.salida_real = String.format("%02d:%02d", hour, minutes)

                                var retrofit = Retrofit.Builder()
                                    .baseUrl(BASE_URL)
                                    .addConverterFactory(GsonConverterFactory.create())
                                    .build()

                                var retrofitAPI: MailerApiService = retrofit.create(MailerApiService::class.java)
                                val dataPost = DataPost()
                                dataPost.sendto = firebaseAuth!!.currentUser!!.email!!
                                dataPost.subject = "Notificación de salida de lugar"
                                dataPost.id = infoLugar.uid
                                dataPost.horaEntrada = infoLugar.entrada
                                dataPost.horaEntradaReal = infoLugar.entrada_real
                                dataPost.horaSalida = infoLugar.salida
                                dataPost.horaSalidaReal = infoLugar.salida_real

                                retrofitAPI.callMailer(dataPost).enqueue(object : Callback<ResponseBody> {
                                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                                        Log.i("Call Mailer","Correo enviado")
                                    }

                                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                        Log.i("Call Mailer","Error al enviar correo")
                                    }
                                })

                            }

                            Log.i("Actualizando","Estado del lugar de trabajo")
                            var userUID = firebaseAuth!!.currentUser?.uid.toString()
                            val databaseReference = FirebaseDatabase.getInstance().getReference("Direcciones")
                            databaseReference.child(userUID!!)
                                .child(infoLugar.uid)
                                .setValue(infoLugar)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Dirección de trabajo actualizada con éxito", Toast.LENGTH_SHORT)
                                        .show()
                                    Log.i("Actualizado","Estado actualizado del lugar de trabajo")
                                    findNavController().navigate(R.id.navigation_home)
                                }.addOnFailureListener { e ->
                                    Toast.makeText(
                                        context,
                                        "Ocurrio un error al actualizar dirección de trabajo. " + e.message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    Log.i("Error Actualizando","Estado del lugar de trabajo")
                                }
                        }

                    }
                    .addOnFailureListener { e ->
                        Log.e("Face Detection",e.message.toString())
                    }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }else{
            Toast.makeText(
                context,
                "Se encuentra demasiado lejos del punto de marcado, la distancia maxima es 5 metros",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun ObtenerUbicacionActual(){
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
                        Log.d("Location InformacionDireccionFragment", "Ubicacion actual ${location.latitude} , ${location.longitude}")

                        Toast.makeText(context,
                            "Ubicacion actual obtenida exitosamente",
                            Toast.LENGTH_LONG).show()
                        ubicacionActual = location
                        ValidarEntradaSalida()
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

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

}