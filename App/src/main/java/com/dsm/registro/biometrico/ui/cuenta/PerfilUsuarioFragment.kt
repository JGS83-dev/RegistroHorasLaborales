package com.dsm.registro.biometrico.ui.cuenta

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.dsm.registro.biometrico.R
import com.dsm.registro.biometrico.databinding.FragmentPerfilUsuarioBinding
import com.dsm.registro.biometrico.utils.Utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import java.io.File
import java.io.FileInputStream

class PerfilUsuarioFragment : Fragment(R.layout.fragment_perfil_usuario) {

    companion object {
        fun newInstance() = PerfilUsuarioFragment()
    }

    private lateinit var viewModel: PerfilUsuarioViewModel
    private var _binding: FragmentPerfilUsuarioBinding? = null
    private lateinit var database: DatabaseReference
    private val binding get() = _binding!!

    var BtnCerrarSesion: Button? = null
    var BtnAgregarImagenBiometrico: Button? = null
    var firebaseAuth: FirebaseAuth? = null
    var uriImagen: String? = null
    val Datos = HashMap<String, String?>()

    val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            uriImagen = context?.let { Utils.getRealPathFromURI(it,uri) }
            Log.d("PhotoPicker", "Selected URI: $uri")

            val uid = firebaseAuth!!.currentUser?.uid.toString()
            val storageRef = Firebase.storage.reference
            val imagenBiometricaRef = storageRef.child("/imagenes/usuarios/$uid/perfil/biometrica/$uid")

            val stream = FileInputStream(uriImagen?.let { File(it) })
            var uploadTask = imagenBiometricaRef.putStream(stream)

            uploadTask.addOnFailureListener {
                Log.e("Error al subir imagen", it.message.toString())
            }.addOnSuccessListener { taskSnapshot ->

                imagenBiometricaRef.downloadUrl.addOnCompleteListener {
                    Log.i("Exito al subir imagen biometrica", it.result.toString())

                    Datos["uid"] = uid
                    Datos["biometria"] = it.result.toString()
                    val databaseReference = FirebaseDatabase.getInstance().getReference("Usuarios")
                    databaseReference.child(uid)
                        .setValue(Datos)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Foto biometrica cargada con exito", Toast.LENGTH_LONG)
                                .show()
                        }.addOnFailureListener { e ->
                            Toast.makeText(
                                context,
                                "Ocurrio un error al cargar Foto biometrica. " + e.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }
        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPerfilUsuarioBinding.inflate(inflater, container, false)
        val root: View = binding.root

        BtnCerrarSesion = binding.btnCerrarSesion
        firebaseAuth = FirebaseAuth.getInstance()
        database = Firebase.database.reference

        database.child("Usuarios").child(firebaseAuth!!.currentUser?.uid.toString()).get().addOnSuccessListener {
            Log.i("Informacion del usuario -> LLave",it.key.toString())
            Datos["correo"] = it.child("correo").value.toString()
            Datos["nombres"] = it.child("nombres").value.toString()
            Datos["apellidos"] = it.child("apellidos").value.toString()
            Datos["password"] = it.child("password").value.toString()
            Datos["biometria"] = ""
            Datos["imagen"] = it.child("imagen").value.toString()

            Picasso.get().load(Datos["imagen"]).into(binding.imagenPerfil);
            binding.nombre.text = Datos["nombres"]
            binding.contrasenia.text = Datos["password"]
            binding.apellido.text = Datos["apellidos"]
            binding.correo.text = Datos["correo"]

            BtnCerrarSesion!!.setOnClickListener {
                firebaseAuth!!.signOut()
                Toast.makeText(context, "Sesión cerrada con éxito", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_navigation_perfil_usuario_to_navigation_notifications)
            }

            BtnAgregarImagenBiometrico = binding.btnTomarDatosBiometricos

            BtnAgregarImagenBiometrico!!.setOnClickListener {
                Toast.makeText(context, "Agregue su imagen de perfil biometrico", Toast.LENGTH_SHORT).show()
                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        }.addOnFailureListener{
            Log.e("firebase", "Error getting user data", it)
        }

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(PerfilUsuarioViewModel::class.java)
        // TODO: Use the ViewModel
    }

}