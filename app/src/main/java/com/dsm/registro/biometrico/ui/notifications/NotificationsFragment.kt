package com.dsm.registro.biometrico.ui.notifications

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.dsm.registro.biometrico.R
import com.dsm.registro.biometrico.databinding.FragmentNotificationsBinding
import com.dsm.registro.biometrico.ui.home.HomeFragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class NotificationsFragment : Fragment(R.layout.fragment_notifications) {

    private var _binding: FragmentNotificationsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    var CorreoLogin: EditText? = null
    var PassLogin:EditText? = null
    var Btn_Logeo: Button? = null
    var BtnRegistrarse: Button? = null

    private lateinit var auth: FirebaseAuth

    var correo = ""
    var password = ""
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val notificationsViewModel =
            ViewModelProvider(this).get(NotificationsViewModel::class.java)

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        auth = FirebaseAuth.getInstance()

        val currentUser = auth.currentUser
        if (currentUser != null) {
            findNavController().navigate(R.id.action_navigation_notifications_to_navigation_home)
        }

        CorreoLogin = binding.CorreoLogin
        PassLogin = binding.PassLogin
        Btn_Logeo = binding.BtnLogeo
        BtnRegistrarse = binding.BtnRegistrarse
        correo = ""
        password = ""

        Btn_Logeo!!.setOnClickListener { ValidarDatos() }

        BtnRegistrarse!!.setOnClickListener(View.OnClickListener {
            Toast.makeText(context, "Ya tiene una sesión activa", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_navigation_notifications_to_navigation_dashboard)
        })

        return root
    }

    private fun ValidarDatos() {
        correo = CorreoLogin?.getText().toString()
        password = PassLogin?.getText().toString()
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            Toast.makeText(context, "Correo inválido", Toast.LENGTH_SHORT).show()
        } else if (TextUtils.isEmpty(password)) {
            Toast.makeText(context, "Ingrese contraseña", Toast.LENGTH_SHORT).show()
        } else {
            LoginDeUsuario()
        }
    }

    private fun LoginDeUsuario() {

        auth.signInWithEmailAndPassword(correo, password)
            .addOnCompleteListener() { task ->
                if (task.isSuccessful) {
                    val user: FirebaseUser? = auth?.getCurrentUser()
                    findNavController().navigate(R.id.action_navigation_notifications_to_navigation_home)
                    if (user != null) {
                        Toast.makeText(
                            activity,
                            "Bienvenido(a): " + user.email,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        context,
                        "Verifique si el correo y contraseña son los correctos",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
           .addOnFailureListener(OnFailureListener { e ->
                Toast.makeText(
                    context,
                    "" + e.message,
                    Toast.LENGTH_SHORT
                ).show()
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}