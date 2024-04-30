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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dsm.registro.biometrico.R
import com.dsm.registro.biometrico.databinding.FragmentPerfilUsuarioBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class PerfilUsuarioFragment : Fragment(R.layout.fragment_perfil_usuario) {

    companion object {
        fun newInstance() = PerfilUsuarioFragment()
    }

    private lateinit var viewModel: PerfilUsuarioViewModel
    private var _binding: FragmentPerfilUsuarioBinding? = null
    private lateinit var database: DatabaseReference
    private val binding get() = _binding!!

    var BtnCerrarSesion: Button? = null
    var firebaseAuth: FirebaseAuth? = null
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

            Picasso.get().load(it.child("imagen").value.toString()).into(binding.imagenPerfil);
            binding.nombre.text = binding.nombre.text.toString() + " " + it.child("nombres").value.toString()
            binding.contrasenia.text = binding.contrasenia.text.toString() + " " + it.child("password").value.toString()
            binding.apellido.text = binding.apellido.text.toString() + " " + it.child("apellidos").value.toString()
            binding.correo.text = binding.correo.text.toString() + " " + it.child("correo").value.toString()
            binding.nombre.text = binding.nombre.text.toString() + " " + it.child("nombres").value.toString()

            BtnCerrarSesion!!.setOnClickListener {
                firebaseAuth!!.signOut()
                Toast.makeText(context, "Sesión cerrada con éxito", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_navigation_perfil_usuario_to_navigation_notifications)
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