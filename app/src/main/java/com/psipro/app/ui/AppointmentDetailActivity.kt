package com.psipro.app.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import com.psipro.app.ui.compose.PsiproTheme
import com.psipro.app.data.entities.Appointment
import com.psipro.app.data.entities.AppointmentStatus
import com.psipro.app.databinding.ActivityAppointmentDetailBinding
import com.psipro.app.ui.viewmodels.AppointmentViewModel
import com.psipro.app.ui.compose.BillingDialog
import com.psipro.app.ui.compose.BillingNotificationDialog
import dagger.hilt.android.AndroidEntryPoint
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AppointmentDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAppointmentDetailBinding
    private val viewModel: AppointmentViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppointmentDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Observar estados de cobrança
        lifecycleScope.launch {
            viewModel.showBillingDialog.collect { showDialog ->
                if (showDialog) {
                    // Coletar valores diretamente
                    val message = viewModel.billingMessage.value
                    val appointment = viewModel.billingAppointment.value
                    
                    if (appointment != null) {
                        when (appointment.status) {
                            AppointmentStatus.FALTOU, AppointmentStatus.CANCELOU -> {
                                showBillingConfirmationDialog(message) { generateBilling ->
                                    viewModel.confirmBilling(generateBilling)
                                }
                            }
                            else -> {
                                showBillingNotificationDialog(message) {
                                    viewModel.dismissBillingDialog()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    private fun showBillingConfirmationDialog(message: String, onConfirm: (Boolean) -> Unit) {
        val dialogView = ComposeView(this)
        dialogView.setContent {
            PsiproTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BillingDialog(
                        message = message,
                        onConfirm = { onConfirm(true) },
                        onDismiss = { onConfirm(false) },
                        showConfirmButton = true
                    )
                }
            }
        }
        
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()
        
        dialog.show()
    }
    
    private fun showBillingNotificationDialog(message: String, onDismiss: () -> Unit) {
        val dialogView = ComposeView(this)
        dialogView.setContent {
            PsiproTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BillingNotificationDialog(
                        message = message,
                        onDismiss = onDismiss
                    )
                }
            }
        }
        
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()
        
        dialog.show()
    }
} 



