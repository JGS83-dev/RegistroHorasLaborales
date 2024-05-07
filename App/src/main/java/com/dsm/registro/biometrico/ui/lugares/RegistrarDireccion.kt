package com.dsm.registro.biometrico.ui.lugares

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dsm.registro.biometrico.R

class RegistrarDireccion : Fragment() {

    companion object {
        fun newInstance() = RegistrarDireccion()
    }

    private lateinit var viewModel: RegistrarDireccionViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_registrar_direccion, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(RegistrarDireccionViewModel::class.java)
        // TODO: Use the ViewModel
    }

}