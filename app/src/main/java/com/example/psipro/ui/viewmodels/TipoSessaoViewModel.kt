package com.example.psipro.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.psipro.data.dao.TipoSessaoDao
import com.example.psipro.data.entities.TipoSessao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class TipoSessaoViewModel @Inject constructor(
    private val tipoSessaoDao: TipoSessaoDao
) : ViewModel() {
    val tiposSessao: StateFlow<List<TipoSessao>> = tipoSessaoDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
} 