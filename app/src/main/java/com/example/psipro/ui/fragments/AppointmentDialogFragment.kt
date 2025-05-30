package com.example.psipro.ui.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.psipro.data.entities.Appointment
import com.example.psipro.data.entities.RecurrenceType
import com.example.psipro.databinding.DialogAppointmentBinding
import com.example.psipro.viewmodel.AppointmentViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.Color
import androidx.core.content.ContextCompat
import com.example.psipro.R
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.lifecycle.lifecycleScope
import com.example.psipro.data.repository.FinancialRecordRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.example.psipro.ui.viewmodels.PatientViewModel
import androidx.fragment.app.viewModels

import java.text.NumberFormat
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.text.Editable
import android.text.TextWatcher
import androidx.compose.ui.platform.ComposeView
import com.example.psipro.ui.compose.AppointmentForm

@AndroidEntryPoint
class AppointmentDialogFragment : DialogFragment() {
    private var _binding: DialogAppointmentBinding? = null
    private val binding get() = _binding!!
    private val appointmentViewModel: AppointmentViewModel by activityViewModels()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
    private val timeFormat = SimpleDateFormat("HH:mm", Locale("pt", "BR"))
    private var selectedPatientId: Long? = null
    @Inject lateinit var financialRecordRepository: FinancialRecordRepository
    private val patientViewModel: PatientViewModel by viewModels()
    private var loadedAppointment: Appointment? = null
    private var selectedColorHex: String = "#FFF9C4" // Cor padrão (amarelo)

    // Lista de cores disponíveis
    private val colorList = listOf(
        ColorItem("Amarelo", "#FFF9C4"),
        ColorItem("Azul bebê", "#B3E5FC"),
        ColorItem("Roxo", "#D1C4E9"),
        ColorItem("Verde", "#C8E6C9"),
        ColorItem("Rosa", "#F8BBD0"),
        ColorItem("Laranja", "#FFE0B2"),
        ColorItem("Cinza", "#CFD8DC")
    )

    data class ColorItem(val name: String, val hex: String)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val initialPatientName = arguments?.getString("patient_name") ?: ""
        val initialPatientPhone = arguments?.getString("patient_phone") ?: ""
        val initialDate = arguments?.getLong("selected_date")?.let {
            SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")).format(Date(it))
        } ?: ""
        val initialStartTime = arguments?.getString("selected_hour") ?: ""
        val initialEndTime = arguments?.getString("selected_end_hour") ?: ""
        android.util.Log.d("AppointmentDialog", "Dados recebidos no diálogo: Nome=$initialPatientName, Telefone=$initialPatientPhone")
        val composeView = ComposeView(requireContext()).apply {
            setContent {
                AppointmentForm(
                    initialPatientName = initialPatientName,
                    initialPatientPhone = initialPatientPhone,
                    initialDate = initialDate,
                    initialStartTime = initialStartTime,
                    initialEndTime = initialEndTime,
                    onSave = { notificacaoAtiva, tempoAntecedencia, tipoAntecedencia, titulo, descricao, paciente, telefone, valor, data, horaInicio, horaFim ->
                        // Converter tempo de antecedência para minutos
                        val minutos = if (tipoAntecedencia == "horas") (tempoAntecedencia.toIntOrNull() ?: 0) * 60 else tempoAntecedencia.toIntOrNull() ?: 0
                        // Converter valor para double
                        val valorDouble = valor.replace("[^\\d]".toRegex(), "").toDoubleOrNull()?.div(100) ?: 0.0
                        // Converter data para Date
                        val dateObj = dateFormat.parse(data) ?: Date()
                        // Criar objeto Appointment
                        val appointment = Appointment(
                            title = titulo,
                            description = descricao,
                            patientId = 0L, // Ajuste conforme necessário
                            patientName = paciente,
                            patientPhone = telefone,
                            date = dateObj,
                            startTime = horaInicio,
                            endTime = horaFim,
                            reminderEnabled = notificacaoAtiva,
                            reminderMinutes = minutos,
                            sessionValue = valorDouble
                        )
                        // Salvar usando ViewModel
                        appointmentViewModel.addAppointment(
                            appointment = appointment,
                            onConflict = {
                                Toast.makeText(requireContext(), "Já existe uma consulta agendada neste horário", Toast.LENGTH_SHORT).show()
                            },
                            onSuccess = {
                                Toast.makeText(requireContext(), "Consulta criada com sucesso", Toast.LENGTH_SHORT).show()
                                // --- INTEGRAR WHATSAPP ---
                                if (notificacaoAtiva) {
                                    val context = requireContext().applicationContext
                                    val alarmManager = context.getSystemService(android.content.Context.ALARM_SERVICE) as android.app.AlarmManager
                                    // Nome do profissional (usuário do app)
                                    val nomeProfissional = getUserNameFromPrefsOrDefault(context) // Função helper
                                    val mensagemWhatsapp = "Agendamento realizado! ✅ Você marcou um serviço de Reconsulta com a profissional $nomeProfissional no dia $data às $horaInicio. Caso precise desmarcar, por favor avise com antecedência ➡️ Não responda esta mensagem caso tenha dúvida contate diretamente a profissional.🤗"
                                    val intent = android.content.Intent(context, com.example.psipro.notification.WhatsAppReminderReceiver::class.java).apply {
                                        putExtra("phone", telefone)
                                        putExtra("message", mensagemWhatsapp)
                                        putExtra("patientId", 0L) // Ajuste se tiver o ID do paciente
                                    }
                                    val pendingIntent = android.app.PendingIntent.getBroadcast(
                                        context,
                                        System.currentTimeMillis().toInt(),
                                        intent,
                                        android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                                    )
                                    val cal = java.util.Calendar.getInstance().apply {
                                        time = dateObj
                                        val (h, m) = horaInicio.split(":").map { it.toIntOrNull() ?: 0 }
                                        set(java.util.Calendar.HOUR_OF_DAY, h)
                                        set(java.util.Calendar.MINUTE, m)
                                        set(java.util.Calendar.SECOND, 0)
                                        add(java.util.Calendar.MINUTE, -minutos)
                                    }
                                    alarmManager.setExact(
                                        android.app.AlarmManager.RTC_WAKEUP,
                                        cal.timeInMillis,
                                        pendingIntent
                                    )
                                }
                                dismiss()
                            },
                            onError = { exception ->
                                Toast.makeText(requireContext(), "Erro ao criar consulta: ${exception.message}", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                )
            }
        }
        return MaterialAlertDialogBuilder(requireContext())
            .setView(composeView)
            .create()
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

    // Adicionar funções auxiliares para picker
    private fun showTimePicker(onTimeSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val timePicker = com.google.android.material.timepicker.MaterialTimePicker.Builder()
            .setTimeFormat(com.google.android.material.timepicker.TimeFormat.CLOCK_24H)
            .setHour(calendar.get(Calendar.HOUR_OF_DAY))
            .setMinute(calendar.get(Calendar.MINUTE))
            .build()
        timePicker.addOnPositiveButtonClickListener {
            val hour = timePicker.hour.toString().padStart(2, '0')
            val minute = timePicker.minute.toString().padStart(2, '0')
            onTimeSelected("$hour:$minute")
        }
        timePicker.show(parentFragmentManager, "timePicker")
    }

    private fun showDatePicker(onDateSelected: (Date) -> Unit) {
        val datePicker = com.google.android.material.datepicker.MaterialDatePicker.Builder.datePicker()
            .setTitleText("Selecione a data")
            .build()
        datePicker.addOnPositiveButtonClickListener { selection ->
            onDateSelected(Date(selection))
        }
        datePicker.show(parentFragmentManager, "datePicker")
    }

    private fun setupColorSpinner() {
        val adapter = object : ArrayAdapter<ColorItem>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            colorList
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val inflater = LayoutInflater.from(context)
                val view = inflater.inflate(R.layout.spinner_item_color, parent, false)
                val colorItem = getItem(position)
                view.findViewById<View>(R.id.colorView)?.background?.setTint(
                    Color.parseColor(colorItem?.hex ?: "#FFF9C4")
                )
                view.findViewById<TextView>(R.id.colorName)?.text = colorItem?.name
                return view
            }
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val inflater = LayoutInflater.from(context)
                val view = inflater.inflate(R.layout.spinner_item_color, parent, false)
                val colorItem = getItem(position)
                view.findViewById<View>(R.id.colorView)?.background?.setTint(
                    Color.parseColor(colorItem?.hex ?: "#FFF9C4")
                )
                view.findViewById<TextView>(R.id.colorName)?.text = colorItem?.name
                return view
            }
        }
        binding.colorSpinner.adapter = adapter
        binding.colorSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedColorHex = colorList[position].hex
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedColorHex = "#FFF9C4" // Cor padrão
            }
        }
    }

    // Helper para obter o nome do profissional
    fun getUserNameFromPrefsOrDefault(context: android.content.Context): String {
        val prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getString("user_name", "Profissional") ?: "Profissional"
    }
} 