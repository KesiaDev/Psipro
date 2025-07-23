package com.example.psipro.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.psipro.R
import com.example.psipro.ui.screens.FinanceiroDashboardActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FinanceiroFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_financeiro, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Navegar para o novo dashboard financeiro
        val intent = Intent(requireContext(), FinanceiroDashboardActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }
} 