package com.dsm.registro.biometrico.ui.cuenta

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.dsm.registro.biometrico.R
import com.dsm.registro.biometrico.databinding.FragmentPerfilUsuarioBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class PerfilUsuarioFragment : Fragment(R.layout.fragment_perfil_usuario) {

    companion object {
        fun newInstance() = PerfilUsuarioFragment()
    }

    private lateinit var viewModel: PerfilUsuarioViewModel
    private var _binding: FragmentPerfilUsuarioBinding? = null
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

        BtnCerrarSesion!!.setOnClickListener {
            firebaseAuth!!.signOut()
            Toast.makeText(context, "Sesión cerrada con éxito", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_navigation_perfil_usuario_to_navigation_notifications)
        }

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(PerfilUsuarioViewModel::class.java)
        // TODO: Use the ViewModel
    }

}