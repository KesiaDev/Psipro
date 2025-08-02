package com.psipro.app.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.psipro.app.R
import com.psipro.app.ui.screens.FinanceiroDashboardActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FinanceiroFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Layout simples apenas para mostrar loading
        return inflater.inflate(R.layout.fragment_financeiro_loading, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Navegar para o novo dashboard financeiro com pequeno delay
        view.postDelayed({
            try {
                val intent = Intent(requireContext(), FinanceiroDashboardActivity::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                // Fallback se houver erro
            }
        }, 100) // 100ms delay
    }
} 



