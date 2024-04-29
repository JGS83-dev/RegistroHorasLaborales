package com.dsm.registro.biometrico.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.dsm.registro.biometrico.databinding.FragmentNotificationsBinding
import com.google.firebase.auth.FirebaseAuth

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val notificationsViewModel =
            ViewModelProvider(this).get(NotificationsViewModel::class.java)

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        var CorreoLogin = binding.CorreoLogin
        var PassLogin = binding.PassLogin
        var Btn_Logeo = binding.BtnLogeo
        var BtnRegistrarse = binding.BtnRegistrarse
        var firebaseAuth = FirebaseAuth.getInstance()
        var correo = ""
        var password = ""

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}