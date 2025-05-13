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
import com.example.apppisc.data.repository.FinancialRecordRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.example.apppisc.ui.viewmodels.PatientViewModel
import androidx.fragment.app.viewModels
import com.example.apppisc.ui.fragments.PatientSelectDialogFragment
import java.text.NumberFormat
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.text.Editable
import android.text.TextWatcher

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
        _binding = DialogAppointmentBinding.inflate(LayoutInflater.from(context))
        val builder = MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)

        setupColorSpinner()

        // Recupera o ID do agendamento dos argumentos (pode ser nulo para novo)
        val selectedDateArg = arguments?.getLong("selected_date", -1L) ?: -1L
        if (selectedDateArg > 0) {
            val date = Date(selectedDateArg)
            binding.dateInput.setText(dateFormat.format(date))
        } else if (binding.dateInput.text.isNullOrBlank()) {
            // Se não veio argumento, preenche com hoje
            binding.dateInput.setText(dateFormat.format(Date()))
        }
        // Listener para buscar pacientes cadastrados ao clicar na lupa
        binding.patientNameLayout.setEndIconOnClickListener {
            lifecycleScope.launch {
                patientViewModel.checkPatientLimit() // Garante que está atualizado
                patientViewModel.patients.collect { patients ->
                    if (patients.isEmpty()) {
                        Toast.makeText(requireContext(), "Nenhum paciente cadastrado.", Toast.LENGTH_SHORT).show()
                        return@collect
                    }
                    val dialog = PatientSelectDialogFragment(patients) { paciente ->
                        binding.patientNameInput.setText(paciente.name)
                        binding.patientPhoneInput.setText(paciente.phone)
                        selectedPatientId = paciente.id
                    }
                    dialog.show(parentFragmentManager, "patientSelectDialog")
                    return@collect
                }
            }
        }
        // Garantir que os campos de horário abram o time picker
        binding.startTimeInput.setOnClickListener {
            showTimePicker { time -> binding.startTimeInput.setText(time) }
        }
        binding.endTimeInput.setOnClickListener {
            showTimePicker { time -> binding.endTimeInput.setText(time) }
        }
        // Data de término já permite picker normalmente
        binding.recurrenceEndDateInput.setOnClickListener {
            Toast.makeText(requireContext(), "Clique detectado!", Toast.LENGTH_SHORT).show()
            showDatePicker { date -> binding.recurrenceEndDateInput.setText(dateFormat.format(date)) }
        }
        binding.recurrenceEndDateLayout.setOnClickListener {
            Toast.makeText(requireContext(), "Clique detectado!", Toast.LENGTH_SHORT).show()
            showDatePicker { date -> binding.recurrenceEndDateInput.setText(dateFormat.format(date)) }
        }

        // Formatação automática do valor da sessão
        val nf = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
        nf.maximumFractionDigits = 2
        nf.minimumFractionDigits = 2
        nf.currency = java.util.Currency.getInstance("BRL")
        var isEditing = false
        binding.sessionValueInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (!isEditing) {
                    isEditing = true
                    val cleanString = s.toString().replace("R$", "").replace("[.,]".toRegex(), "").trim()
                    if (cleanString.isNotEmpty()) {
                        val parsed = cleanString.toDoubleOrNull() ?: 0.0
                        val formatted = nf.format(parsed / 100).replace("R$", "").trim()
                        binding.sessionValueInput.setText(formatted)
                        binding.sessionValueInput.setSelection(formatted.length)
                    }
                    isEditing = false
                }
            }
        })

        // Clique do botão Salvar (funciona para novo e edição)
        binding.salvarAgendamentoButton.setOnClickListener {
            val title = binding.titleInput.text.toString().trim()
            val description = binding.descriptionInput.text.toString().trim()
            val patientName = binding.patientNameInput.text.toString().trim()
            val patientPhone = binding.patientPhoneInput.text.toString().trim()
            val dateStr = binding.dateInput.text.toString().trim()
            val startTime = binding.startTimeInput.text.toString().trim()
            val endTime = binding.endTimeInput.text.toString().trim()
            val sessionValueStr = binding.sessionValueInput.text.toString()
                .replace("R$", "")
                .replace(".", "")
                .replace(',', '.')
                .trim()
            val sessionValue = sessionValueStr.toDoubleOrNull() ?: 0.0

            // Validação dos campos obrigatórios
            if (title.isEmpty() || patientName.isEmpty() || dateStr.isEmpty() || startTime.isEmpty() || endTime.isEmpty()) {
                Toast.makeText(requireContext(), "Por favor, preencha todos os campos obrigatórios.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Validação do formato da data
            val date: Date = try {
                dateFormat.parse(dateStr) ?: throw IllegalArgumentException()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Data inválida. Use o formato dd/MM/yyyy.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val reminderEnabled = binding.reminderSwitch.isChecked
            val reminderMinutes = binding.reminderMinutesInput.text.toString().toIntOrNull() ?: 30
            val recurrenceTypeIdx = binding.recurrenceTypeSpinner.selectedItemPosition
            val recurrenceType = if (recurrenceTypeIdx in RecurrenceType.values().indices) {
                RecurrenceType.values()[recurrenceTypeIdx]
            } else {
                RecurrenceType.NONE
            }
            val recurrenceInterval = binding.recurrenceIntervalInput.text.toString().toIntOrNull()
            val recurrenceEndDate = binding.recurrenceEndDateInput.text.toString().takeIf { it.isNotBlank() }?.let { dateFormat.parse(it) }
            val recurrenceCount = binding.recurrenceCountInput.text.toString().toIntOrNull()
            val patientId = selectedPatientId ?: loadedAppointment?.patientId

            val appointmentId = arguments?.getLong("appointment_id", -1L) ?: -1L
            android.util.Log.d("AppointmentDialog", "SALVAR: appointment_id=$appointmentId, loadedAppointment=$loadedAppointment")

            lifecycleScope.launch {
                try {
                    if (appointmentId == -1L || loadedAppointment == null) {
                        // Novo agendamento
                        android.util.Log.d("AppointmentDialog", "SALVAR: Novo agendamento")
                        val patientIdValue = patientId ?: 0L
                        val appointment = Appointment(
                            title = title,
                            description = description,
                            patientId = patientIdValue,
                            patientName = patientName,
                            patientPhone = patientPhone,
                            date = date,
                            startTime = startTime,
                            endTime = endTime,
                            reminderEnabled = reminderEnabled,
                            reminderMinutes = reminderMinutes,
                            recurrenceType = recurrenceType,
                            recurrenceInterval = recurrenceInterval,
                            recurrenceEndDate = recurrenceEndDate,
                            recurrenceCount = recurrenceCount,
                            sessionValue = sessionValue,
                            colorHex = selectedColorHex
                        )
                        appointmentViewModel.addAppointment(
                            appointment = appointment,
                            onConflict = {
                                Toast.makeText(requireContext(), "Já existe uma consulta agendada neste horário", Toast.LENGTH_SHORT).show()
                            },
                            onSuccess = { id ->
                                Toast.makeText(requireContext(), "Consulta salva com sucesso!", Toast.LENGTH_SHORT).show()
                                dismiss()
                            },
                            onError = { e ->
                                Toast.makeText(requireContext(), "Erro ao salvar consulta: ${e.message}", Toast.LENGTH_SHORT).show()
                                android.util.Log.e("AppointmentDialog", "Erro ao salvar consulta", e)
                            }
                        )
                    } else {
                        // Edição: atualizar apenas os campos editados
                        android.util.Log.d("AppointmentDialog", "SALVAR: Editando agendamento")
                        val updated = loadedAppointment?.copy(
                            title = title,
                            description = description,
                            patientId = patientId ?: 0L,
                            patientName = patientName,
                            patientPhone = patientPhone,
                            date = date,
                            startTime = startTime,
                            endTime = endTime,
                            reminderEnabled = reminderEnabled,
                            reminderMinutes = reminderMinutes,
                            recurrenceType = recurrenceType,
                            recurrenceInterval = recurrenceInterval,
                            recurrenceEndDate = recurrenceEndDate,
                            recurrenceCount = recurrenceCount,
                            sessionValue = sessionValue,
                            colorHex = selectedColorHex
                        )
                        if (updated != null) {
                            appointmentViewModel.updateAppointment(updated)
                            android.util.Log.d("AppointmentDialog", "updateAppointment chamado")
                        } else {
                            android.util.Log.d("AppointmentDialog", "updateAppointment NÃO chamado: updated == null")
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Erro ao salvar consulta: ${e.message}", Toast.LENGTH_SHORT).show()
                    android.util.Log.e("AppointmentDialog", "Erro ao salvar consulta", e)
                }
            }
        }
        binding.confirmarWhatsappButton.visibility = View.GONE
        // Novo agendamento: esconder botões de confirmação
        binding.confirmarPresencaButton.visibility = View.GONE
        binding.faltouButton.visibility = View.GONE

        val selectedHourArg = arguments?.getString("selected_hour")
        if (!selectedHourArg.isNullOrBlank()) {
            binding.startTimeInput.setText(selectedHourArg)
        }
        val selectedEndHourArg = arguments?.getString("selected_end_hour")
        if (!selectedEndHourArg.isNullOrBlank()) {
            binding.endTimeInput.setText(selectedEndHourArg)
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
} 