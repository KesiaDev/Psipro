package com.psipro.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.psipro.app.ui.screens.AutoavaliacaoScreen
import com.psipro.app.ui.viewmodels.AutoavaliacaoViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AutoavaliacaoFragment : Fragment() {
    
    private val viewModel: AutoavaliacaoViewModel by viewModels()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                AutoavaliacaoScreen(
                    onBack = {
                        // Navegação será tratada pelo sistema
                    },
                    viewModel = viewModel
                )
            }
        }
    }
} 



