package com.dsm.registro.biometrico.ui.cuenta

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieAnimationView
import com.dsm.registro.biometrico.R
import com.dsm.registro.biometrico.databinding.FragmentRegistrarseBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.io.File
import java.io.FileInputStream


class RegistrarseFragment : Fragment(R.layout.fragment_registrarse) {

    companion object {
        fun newInstance() = RegistrarseFragment()
    }

    private lateinit var viewModel: RegistrarseViewModel
    private var _binding: FragmentRegistrarseBinding? = null
    private val binding get() = _binding!!

    var NombreEt: EditText? = null
    var ApellidoEt: EditText? = null
    var CorreoEt:EditText? = null
    var ContaseñaEt:EditText? = null
    var ConfirmarContraseñaEt:EditText? = null
    var BtnRegistrarUsuario: Button? = null
    var BtnTengounacuenta: Button? = null
    var BtnAgregarImagen: LottieAnimationView? = null

    var firebaseAuth: FirebaseAuth? = null
    var storage: FirebaseStorage? = null

    var nombre = " "
    var apellido = " "
    var correo = " "
    var password = ""
    var confirmarpassword = ""
    var uriImagen: String? = null

    val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            uriImagen = context?.let { getRealPathFromURI(it,uri) }
            Log.d("PhotoPicker", "Selected URI: $uri")
        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }

    private fun getRealPathFromURI(context: Context, contentUri: Uri): String? {
        var cursor: Cursor? = null
        return try {
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null)
            val column_index: Int = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
//            Log.i("Real Path Imagen ->",cursor.getString(column_index))
            cursor.getString(column_index)
        } catch (e: Exception) {
            Log.e("Fallo al obtener URI: ", "getRealPathFromURI Exception : $e")
            ""
        } finally {
            cursor?.close()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegistrarseBinding.inflate(inflater, container, false)
        val root: View = binding.root

        NombreEt = binding.NombreEt
        ApellidoEt = binding.ApellidoEt
        CorreoEt = binding.CorreoEt
        ContaseñaEt = binding.ContaseAEt
        ConfirmarContraseñaEt = binding.ConfirmarContraseAEt
        BtnRegistrarUsuario = binding.RegistrarUsuario
        BtnTengounacuenta = binding.TengounacuentaBtn
        BtnAgregarImagen = binding.agregarImagenBtn

        firebaseAuth = FirebaseAuth.getInstance()
        storage = Firebase.storage

        BtnRegistrarUsuario!!.setOnClickListener { ValidarDatos() }

        BtnTengounacuenta!!.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_registrarse_to_navigation_notifications)
        }

        BtnAgregarImagen!!.setOnClickListener {
            Toast.makeText(context, "Agregue su imagen de perfil", Toast.LENGTH_SHORT).show()
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        return root
    }

    private fun ValidarDatos() {
        nombre = NombreEt!!.text.toString()
        apellido = ApellidoEt!!.text.toString()
        correo = CorreoEt!!.text.toString()
        password = ContaseñaEt!!.text.toString()
        confirmarpassword = ConfirmarContraseñaEt!!.text.toString()
        if (TextUtils.isEmpty(nombre)) {
            Toast.makeText(context, "Ingrese nombre", Toast.LENGTH_SHORT).show()
        } else if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            Toast.makeText(context, "Ingrese correo", Toast.LENGTH_SHORT).show()
        } else if (TextUtils.isEmpty(password)) {
            Toast.makeText(context, "Ingrese contraseña", Toast.LENGTH_SHORT).show()
        } else if (TextUtils.isEmpty(confirmarpassword)) {
            Toast.makeText(context, "Confirme contraseña", Toast.LENGTH_SHORT).show()
        } else if (password != confirmarpassword) {
            Toast.makeText(context, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
        } else {
            CrearCuenta()
        }
    }

    private fun CrearCuenta() {
        firebaseAuth!!.createUserWithEmailAndPassword(correo, password)
            .addOnSuccessListener { GuardarInformacion() }.addOnFailureListener { e ->
                Toast.makeText(
                    context,
                    "Ocurrio un error al crear cuenta. " + e.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun GuardarInformacion() {
        val uid = firebaseAuth!!.uid
        val Datos = HashMap<String, String?>()
        Datos["uid"] = uid
        Datos["correo"] = correo
        Datos["nombres"] = nombre
        Datos["apellidos"] = apellido
        Datos["password"] = password

        val storageRef = storage!!.reference
        val imagenUsuarioRef = storageRef.child("/imagenes/usuarios/$uid/perfil")
        val stream = FileInputStream(uriImagen?.let { File(it) })
        var uploadTask = imagenUsuarioRef.putStream(stream)

        uploadTask.addOnFailureListener {
            Log.e("Error al subir imagen", it.message.toString())
        }.addOnSuccessListener { taskSnapshot ->
            Log.e("Exito al subir imagen", taskSnapshot.toString())
            Datos["imagen"] = taskSnapshot.metadata?.path
        }

        val databaseReference = FirebaseDatabase.getInstance().getReference("Usuarios")
        databaseReference.child(uid!!)
            .setValue(Datos)
            .addOnSuccessListener {
                Toast.makeText(context, "Cuenta creada con éxito", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_navigation_registrarse_to_navigation_home)
            }.addOnFailureListener { e ->
                Toast.makeText(context, "Ocurrio un error al crear cuenta. " + e.message, Toast.LENGTH_SHORT).show()
            }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(RegistrarseViewModel::class.java)
        // TODO: Use the ViewModel
    }

}