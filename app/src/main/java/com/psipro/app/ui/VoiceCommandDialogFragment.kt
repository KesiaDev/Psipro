package com.psipro.app.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.psipro.app.ui.compose.PsiproTheme
import com.psipro.app.ui.components.VoiceCommandDialog
import com.psipro.app.ui.viewmodels.VoiceCommandViewModel
import com.psipro.app.utils.VoiceAction
import dagger.hilt.android.AndroidEntryPoint

/**
 * Listener para comandos de voz reconhecidos.
 */
interface VoiceCommandListener {
    fun onVoiceCommand(action: VoiceAction)
}

/**
 * Diálogo de comando de voz. Mostra interface para o usuário dar comandos por voz
 * e executa a ação correspondente.
 */
@AndroidEntryPoint
class VoiceCommandDialogFragment : DialogFragment() {

    private val viewModel: VoiceCommandViewModel by activityViewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.startListening()
        } else {
            viewModel.setError("Permissão de microfone necessária para comandos de voz")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return androidx.compose.ui.platform.ComposeView(requireContext()).apply {
            setContent {
                val state by viewModel.state.collectAsState()
                PsiproTheme {
                    VoiceCommandDialog(
                        state = state,
                        onStartListening = {
                            if (ContextCompat.checkSelfPermission(
                                    requireContext(),
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                viewModel.startListening()
                            } else {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        onStopListening = { viewModel.stopListening() },
                        onDismiss = { dismiss() }
                    )
                }
            }
        }
    }

}
