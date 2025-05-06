package com.example.apppisc.ui.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.apppisc.data.entities.Appointment
import com.example.apppisc.data.entities.RecurrenceType
import com.example.apppisc.databinding.DialogAppointmentBinding
import com.example.apppisc.viewmodel.AppointmentViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.Color
import androidx.core.content.ContextCompat
import com.example.apppisc.R
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.lifecycle.lifecycleScope

class AppointmentDialogFragment : DialogFragment() {
    private var _binding: DialogAppointmentBinding? = null
    private val binding get() = _binding!!
    private val appointmentViewModel: AppointmentViewModel by activityViewModels()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
    private val timeFormat = SimpleDateFormat("HH:mm", Locale("pt", "BR"))
    private var selectedPatientId: Long? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogAppointmentBinding.inflate(LayoutInflater.from(context))
        val builder = MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)

        // Recupera o ID do agendamento dos argumentos
        val appointmentId = arguments?.getLong("appointment_id") ?: -1L
        if (appointmentId == -1L) {
            Toast.makeText(requireContext(), "Erro ao carregar consulta.", Toast.LENGTH_SHORT).show()
            dismiss()
            return builder.create()
        }

        // Carrega o agendamento pelo ViewModel
        var loadedAppointment: Appointment? = null
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
        val timeFormat = SimpleDateFormat("HH:mm", Locale("pt", "BR"))

        // Observa o agendamento e atualiza a UI
        viewLifecycleOwner.lifecycleScope.launch {
            val appointment = withContext(Dispatchers.IO) {
                appointmentViewModel.getAppointmentById(appointmentId)
            }
            loadedAppointment = appointment
            if (appointment != null) {
                updateConfirmationUI(appointment)
            } else {
                Toast.makeText(requireContext(), "Consulta não encontrada.", Toast.LENGTH_SHORT).show()
                dismiss()
            }
        }

        // Botão de confirmar presença
        binding.confirmarPresencaButton.setOnClickListener {
            loadedAppointment?.let { appointment ->
                appointmentViewModel.updateConfirmation(
                    appointment.id,
                    true,
                    Date(),
                    null,
                    onSuccess = {
                        Toast.makeText(requireContext(), "Presença confirmada!", Toast.LENGTH_SHORT).show()
                        updateConfirmationUI(appointment.copy(isConfirmed = true, confirmationDate = Date(), absenceReason = null))
                    },
                    onError = {
                        Toast.makeText(requireContext(), "Erro ao confirmar presença.", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        // Botão de faltou
        binding.faltouButton.setOnClickListener {
            binding.motivoAusenciaLayout.visibility = View.VISIBLE
            binding.salvarMotivoButton.visibility = View.VISIBLE
        }

        // Botão de salvar motivo da ausência
        binding.salvarMotivoButton.setOnClickListener {
            val reason = binding.motivoAusenciaInput.text.toString().trim()
            if (reason.isEmpty()) {
                Toast.makeText(requireContext(), "Informe o motivo da ausência.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            loadedAppointment?.let { appointment ->
                appointmentViewModel.updateConfirmation(
                    appointment.id,
                    false,
                    Date(),
                    reason,
                    onSuccess = {
                        Toast.makeText(requireContext(), "Motivo salvo!", Toast.LENGTH_SHORT).show()
                        updateConfirmationUI(appointment.copy(isConfirmed = false, confirmationDate = Date(), absenceReason = reason))
                    },
                    onError = {
                        Toast.makeText(requireContext(), "Erro ao salvar motivo.", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        return builder.create()
    }

    private fun updateConfirmationUI(appointment: Appointment) {
        val statusText = when (appointment.isConfirmed) {
            true -> "Status: Presença confirmada"
            false -> "Status: Faltou"
            null -> "Status: Não respondido"
        }
        val color = when (appointment.isConfirmed) {
            true -> ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark)
            false -> ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark)
            null -> ContextCompat.getColor(requireContext(), R.color.bronze_gold)
        }
        binding.statusPresencaText.text = statusText
        binding.statusPresencaText.setTextColor(color)
        binding.dataConfirmacaoText.text = appointment.confirmationDate?.let {
            "Data/hora da confirmação: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR")).format(it)}"
        } ?: ""
        if (appointment.isConfirmed == false) {
            binding.motivoAusenciaLayout.visibility = View.VISIBLE
            binding.salvarMotivoButton.visibility = View.GONE
            binding.motivoAusenciaInput.setText(appointment.absenceReason ?: "")
            binding.motivoAusenciaInput.isEnabled = false
        } else {
            binding.motivoAusenciaLayout.visibility = View.GONE
            binding.salvarMotivoButton.visibility = View.GONE
        }
        // Desabilita botões após confirmação
        val confirmed = appointment.isConfirmed != null
        binding.confirmarPresencaButton.isEnabled = !confirmed
        binding.faltouButton.isEnabled = !confirmed
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 