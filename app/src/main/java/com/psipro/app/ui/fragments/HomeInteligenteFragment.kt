package com.psipro.app.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.psipro.app.R
import com.psipro.app.ui.compose.PsiproTheme
import com.psipro.app.ui.screens.FinanceiroDashboardActivity
import com.psipro.app.ui.screens.NovaSessaoActivity
import com.psipro.app.ui.screens.home.HomeScreen
import com.psipro.app.ui.AppointmentDetailActivity
import android.net.Uri
import androidx.fragment.app.viewModels
import com.psipro.app.ui.viewmodels.AppointmentViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeInteligenteFragment : Fragment() {
    
    private val appointmentViewModel: AppointmentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                PsiproTheme {
                    HomeScreen(
                        onNavigateToAppointment = { appointmentId ->
                            // Navegar para detalhes do agendamento
                            val intent = Intent(requireContext(), AppointmentDetailActivity::class.java).apply {
                                putExtra("APPOINTMENT_ID", appointmentId)
                            }
                            startActivity(intent)
                        },
                        onNavigateToSessionNote = { appointmentId ->
                            // Navegar para anotação de sessão
                            val intent = Intent(requireContext(), NovaSessaoActivity::class.java).apply {
                                putExtra("APPOINTMENT_ID", appointmentId)
                            }
                            startActivity(intent)
                        },
                        onNavigateToWhatsApp = { phoneNumber ->
                            // Abrir app de telefone para contato
                            val tel = phoneNumber.replace(Regex("[^0-9+]"), "")
                            if (tel.isNotEmpty()) {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$tel"))
                                startActivity(intent)
                            }
                        },
                        onNavigateToFinancial = {
                            // Navegar para financeiro
                            val intent = Intent(requireContext(), FinanceiroDashboardActivity::class.java)
                            startActivity(intent)
                        },
                        onNavigateToSchedule = {
                            // Navegar para agenda
                            findNavController().navigate(R.id.navigation_schedule)
                        },
                        onConfirmAppointmentRealized = { appointmentId ->
                            // UX: Ação rápida - confirmar sessão como realizada diretamente da Home
                            // Isso gera a cobrança automaticamente e pergunta se foi paga
                            appointmentViewModel.updateAppointmentStatus(
                                appointmentId = appointmentId,
                                status = com.psipro.app.data.entities.AppointmentStatus.REALIZADO,
                                onSuccess = {
                                    // Sucesso - o diálogo de pagamento será mostrado automaticamente
                                    android.util.Log.d("HomeFragment", "Sessão confirmada como realizada")
                                },
                                onError = { exception ->
                                    android.util.Log.e("HomeFragment", "Erro ao confirmar sessão: ${exception.message}")
                                }
                            )
                        }
                    )
                }
            }
        }
    }
}

