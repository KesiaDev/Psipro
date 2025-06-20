package com.example.psipro.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CalendarView
import android.widget.Toast
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.psipro.R
import com.example.psipro.adapter.AppointmentAdapter
import com.example.psipro.data.entities.Appointment
import com.example.psipro.data.entities.AppointmentStatus
import com.example.psipro.data.entities.Patient
import com.example.psipro.data.entities.RecurrenceType
import com.example.psipro.databinding.ActivityAppointmentScheduleBinding
import com.example.psipro.databinding.DialogAppointmentBinding
import com.example.psipro.viewmodel.AppointmentViewModel
import com.example.psipro.ui.viewmodels.PatientViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.*
import dagger.hilt.android.AndroidEntryPoint
import android.content.Context
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.example.psipro.utils.MessageTemplateManager
import com.example.psipro.data.repository.FinancialRecordRepository
import com.example.psipro.data.entities.FinancialRecord
import javax.inject.Inject

@AndroidEntryPoint
class AppointmentScheduleActivity : SecureActivity() {
    private lateinit var binding: ActivityAppointmentScheduleBinding
    private val appointmentViewModel: AppointmentViewModel by viewModels()
    private val patientViewModel: PatientViewModel by viewModels()
    private lateinit var adapter: AppointmentAdapter
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private var selectedDate = Calendar.getInstance()
    private var selectedPatient: Patient? = null
    @Inject lateinit var financialRecordRepository: FinancialRecordRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppointmentScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Sincronizar selectedDate com a data exibida no CalendarView (hoje)
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = binding.calendarView.date
        selectedDate = calendar

        setupRecyclerView()
        setupCalendarView()
        setupFab()
        observeAppointments()
        setupExportarButton()
    }

    private fun setupRecyclerView() {
        adapter = AppointmentAdapter(
            onItemClick = { appointment ->
                showAppointmentDialog(appointment)
            },
            onItemLongClick = { appointment ->
                showDeleteConfirmationDialog(appointment)
            },
            onRecurrenceClick = { appointment ->
                showRecurrenceSeriesDialog(appointment)
            }
        )
        binding.appointmentsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@AppointmentScheduleActivity)
            adapter = this@AppointmentScheduleActivity.adapter
        }
    }

    private fun setupCalendarView() {
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate.set(year, month, dayOfMonth)
            lifecycleScope.launch {
                appointmentViewModel.getAppointmentsByDate(selectedDate.time).collectLatest { appointments ->
                    adapter.submitList(appointments)
                    // Mostrar ou esconder mensagem de vazio
                    binding.noAppointmentsText.visibility = if (appointments.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    private fun setupFab() {
        binding.fabAddAppointment.setOnClickListener {
            Toast.makeText(this, "Cliquei no +", Toast.LENGTH_SHORT).show()
            showAppointmentDialog()
        }
    }

    private fun observeAppointments() {
        lifecycleScope.launch {
            appointmentViewModel.getAppointmentsByDate(selectedDate.time).collectLatest { appointments ->
                adapter.submitList(appointments)
                // Mostrar ou esconder mensagem de vazio
                binding.noAppointmentsText.visibility = if (appointments.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun showDeleteConfirmationDialog(appointment: Appointment) {
        if (appointment.recurrenceSeriesId != null) {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_appointment)
                .setMessage("Esta consulta faz parte de uma série recorrente. O que deseja fazer?")
                .setPositiveButton("Excluir toda a série") { _, _ ->
                    appointmentViewModel.deleteAppointmentsBySeriesId(
                        appointment.recurrenceSeriesId,
                        onSuccess = {
                            Toast.makeText(this, "Série de consultas excluída com sucesso", Toast.LENGTH_SHORT).show()
                        },
                        onError = { exception ->
                            Toast.makeText(this, "Erro ao excluir série: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                .setNegativeButton("Excluir só esta") { _, _ ->
                    appointmentViewModel.deleteAppointment(
                        appointment = appointment,
                        onSuccess = {
                            Toast.makeText(this, R.string.appointment_deleted, Toast.LENGTH_SHORT).show()
                        },
                        onError = { exception ->
                            Toast.makeText(this, R.string.error_deleting_appointment, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                .setNeutralButton(R.string.cancel, null)
                .show()
        } else {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_appointment)
                .setMessage(R.string.delete_appointment_confirmation)
                .setPositiveButton(R.string.delete) { _, _ ->
                    appointmentViewModel.deleteAppointment(
                        appointment = appointment,
                        onSuccess = {
                            Toast.makeText(this, R.string.appointment_deleted, Toast.LENGTH_SHORT).show()
                        },
                        onError = { exception ->
                            Toast.makeText(this, R.string.error_deleting_appointment, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }
    }

    private fun showAppointmentDialog(appointment: Appointment? = null) {
        val dialogBinding = DialogAppointmentBinding.inflate(layoutInflater)
        val isEdit = appointment != null

        // Preencher opções do Spinner de recorrência
        val recurrenceOptions = listOf(
            "Não repetir", "Diária", "Semanal", "Quinzenal", "Mensal", "Personalizada"
        )
        val recurrenceTypeValues = listOf(
            RecurrenceType.NONE, RecurrenceType.DAILY, RecurrenceType.WEEKLY, RecurrenceType.BIWEEKLY, RecurrenceType.MONTHLY, RecurrenceType.CUSTOM
        )
        val spinnerAdapter = android.widget.ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            recurrenceOptions
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.recurrenceTypeSpinner.adapter = spinnerAdapter

        // Se for edição, preencher os campos de recorrência
        if (isEdit) {
            val idx = recurrenceTypeValues.indexOf(appointment!!.recurrenceType)
            dialogBinding.recurrenceTypeSpinner.setSelection(if (idx >= 0) idx else 0)
            dialogBinding.recurrenceIntervalInput.setText(appointment.recurrenceInterval?.toString() ?: "")
            dialogBinding.recurrenceEndDateInput.setText(appointment.recurrenceEndDate?.let { dateFormat.format(it) } ?: "")
            dialogBinding.recurrenceCountInput.setText(appointment.recurrenceCount?.toString() ?: "")
        }

        // Date picker para data de término da recorrência
        dialogBinding.recurrenceEndDateInput.setOnClickListener {
            showDatePicker { date ->
                dialogBinding.recurrenceEndDateInput.setText(dateFormat.format(date))
            }
        }

        // Setup patient selection
        dialogBinding.patientNameLayout.setEndIconOnClickListener {
            showPatientSelectionDialog { patient ->
                selectedPatient = patient
                dialogBinding.patientNameInput.setText(patient.name)
                dialogBinding.patientPhoneInput.setText(patient.phone)
            }
        }

        if (isEdit) {
            // Fill in existing appointment data
            lifecycleScope.launch {
                appointment?.patientId?.let { patientId ->
                    val patient = patientViewModel.getPatientById(patientId)
                    selectedPatient = patient
                    dialogBinding.apply {
                        titleInput.setText(appointment.title)
                        descriptionInput.setText(appointment.description)
                        patientNameInput.setText(patient?.name ?: "")
                        patientPhoneInput.setText(patient?.phone ?: "")
                        dateInput.setText(dateFormat.format(appointment.date))
                        startTimeInput.setText(appointment.startTime)
                        endTimeInput.setText(appointment.endTime)
                        reminderSwitch.isChecked = appointment.reminderEnabled
                        reminderMinutesInput.setText(appointment.reminderMinutes.toString())
                    }
                }
            }
        }

        // Date picker
        dialogBinding.dateInput.setOnClickListener {
            showDatePicker { date ->
                dialogBinding.dateInput.setText(dateFormat.format(date))
            }
        }

        // Time pickers
        dialogBinding.startTimeInput.setOnClickListener {
            showTimePicker { time ->
                dialogBinding.startTimeInput.setText(time)
            }
        }

        dialogBinding.endTimeInput.setOnClickListener {
            showTimePicker { time ->
                dialogBinding.endTimeInput.setText(time)
            }
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(if (isEdit) R.string.edit_appointment else R.string.new_appointment)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.save) { _, _ ->
                val title = dialogBinding.titleInput.text.toString()
                val description = dialogBinding.descriptionInput.text.toString()
                val dateStr = dialogBinding.dateInput.text.toString()
                val startTime = dialogBinding.startTimeInput.text.toString()
                val endTime = dialogBinding.endTimeInput.text.toString()
                val reminderEnabled = dialogBinding.reminderSwitch.isChecked
                val reminderMinutes = dialogBinding.reminderMinutesInput.text.toString().toIntOrNull() ?: 30

                // Recorrência
                val recurrenceType = recurrenceTypeValues[dialogBinding.recurrenceTypeSpinner.selectedItemPosition]
                val recurrenceInterval = dialogBinding.recurrenceIntervalInput.text.toString().toIntOrNull()
                val recurrenceEndDate = dialogBinding.recurrenceEndDateInput.text.toString().takeIf { it.isNotBlank() }?.let { dateFormat.parse(it) }
                val recurrenceCount = dialogBinding.recurrenceCountInput.text.toString().toIntOrNull()

                if (title.isBlank() || dateStr.isBlank() || startTime.isBlank() || endTime.isBlank() || selectedPatient == null) {
                    Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val date = dateFormat.parse(dateStr) ?: Date()
                val newAppointment = if (isEdit) {
                    appointment!!.copy(
                        title = title,
                        description = description,
                        patientId = selectedPatient!!.id,
                        patientName = selectedPatient!!.name,
                        patientPhone = selectedPatient!!.phone,
                        date = date,
                        startTime = startTime,
                        endTime = endTime,
                        reminderEnabled = reminderEnabled,
                        reminderMinutes = reminderMinutes,
                        recurrenceType = recurrenceType,
                        recurrenceInterval = recurrenceInterval,
                        recurrenceEndDate = recurrenceEndDate,
                        recurrenceCount = recurrenceCount
                    )
                } else {
                    Appointment(
                        title = title,
                        description = description,
                        patientId = selectedPatient!!.id,
                        patientName = selectedPatient!!.name,
                        patientPhone = selectedPatient!!.phone,
                        date = date,
                        startTime = startTime,
                        endTime = endTime,
                        reminderEnabled = reminderEnabled,
                        reminderMinutes = reminderMinutes,
                        recurrenceType = recurrenceType,
                        recurrenceInterval = recurrenceInterval,
                        recurrenceEndDate = recurrenceEndDate,
                        recurrenceCount = recurrenceCount
                    )
                }

                if (isEdit && appointment!!.recurrenceSeriesId != null) {
                    MaterialAlertDialogBuilder(this)
                        .setTitle("Editar série de consultas")
                        .setMessage("Esta consulta faz parte de uma série recorrente. O que deseja fazer?")
                        .setPositiveButton("Editar toda a série") { _, _ ->
                            appointmentViewModel.updateAppointmentsBySeriesId(
                                appointment.recurrenceSeriesId!!,
                                appointment.date,
                                title,
                                description,
                                startTime,
                                endTime,
                                reminderEnabled,
                                reminderMinutes,
                                onSuccess = {
                                    Toast.makeText(this, "Série de consultas atualizada com sucesso", Toast.LENGTH_SHORT).show()
                                },
                                onError = { exception ->
                                    Toast.makeText(this, "Erro ao atualizar série: ${exception.message}", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                        .setNegativeButton("Editar só esta") { _, _ ->
                            appointmentViewModel.updateAppointment(
                                appointment = newAppointment,
                                onConflict = {
                                    Toast.makeText(this, R.string.appointment_conflict, Toast.LENGTH_SHORT).show()
                                },
                                onSuccess = {
                                    Toast.makeText(this, R.string.appointment_updated, Toast.LENGTH_SHORT).show()
                                },
                                onError = { exception ->
                                    Toast.makeText(this, R.string.error_updating_appointment, Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                        .setNeutralButton(R.string.cancel, null)
                        .show()
                } else if (isEdit) {
                    appointmentViewModel.updateAppointment(
                        appointment = newAppointment,
                        onConflict = {
                            Toast.makeText(this, R.string.appointment_conflict, Toast.LENGTH_SHORT).show()
                        },
                        onSuccess = {
                            Toast.makeText(this, R.string.appointment_updated, Toast.LENGTH_SHORT).show()
                        },
                        onError = { exception ->
                            Toast.makeText(this, R.string.error_updating_appointment, Toast.LENGTH_SHORT).show()
                        }
                    )
                } else {
                    appointmentViewModel.addAppointment(
                        appointment = newAppointment,
                        onConflict = {
                            Toast.makeText(this, R.string.appointment_conflict, Toast.LENGTH_SHORT).show()
                        },
                        onSuccess = { id ->
                            Toast.makeText(this, R.string.appointment_created, Toast.LENGTH_SHORT).show()
                            // --- INTEGRAR FINANCEIRO ---
                            lifecycleScope.launch {
                                val valorSessao = selectedPatient?.sessionValue ?: 0.0
                                val record = FinancialRecord(
                                    patientId = selectedPatient!!.id,
                                    description = newAppointment.title,
                                    value = valorSessao,
                                    type = "RECEITA",
                                    date = newAppointment.date
                                )
                                financialRecordRepository.insert(record)
                            }
                            // Seleção de template de mensagem para WhatsApp
                            val templates = MessageTemplateManager.getTemplates(this)
                            val patientName = newAppointment.patientName ?: "Paciente"
                            val data = dateFormat.format(newAppointment.date)
                            val hora = newAppointment.startTime
                            val variables = mapOf(
                                "nome" to patientName,
                                "data" to data,
                                "hora" to hora
                            )
                            val items = templates.map { MessageTemplateManager.fillTemplate(it, variables) }.toTypedArray()
                            androidx.appcompat.app.AlertDialog.Builder(this)
                                .setTitle("Selecionar modelo de mensagem para WhatsApp")
                                .setItems(items) { _, which ->
                                    val mensagem = items[which]
                                    val intent = android.content.Intent(this@AppointmentScheduleActivity, com.example.psipro.notification.WhatsAppReminderReceiver::class.java).apply {
                                        putExtra("phone", newAppointment.patientPhone)
                                        putExtra("message", mensagem)
                                        putExtra("patientId", newAppointment.patientId)
                                    }
                                    newAppointment.patientId?.let { patientId ->
                                        val pendingIntent = android.app.PendingIntent.getBroadcast(
                                            this@AppointmentScheduleActivity,
                                            patientId.toInt(),
                                            intent,
                                            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                                        )

                                        // Disparar 1 dia antes da consulta, às 8h da manhã
                                        val cal = java.util.Calendar.getInstance().apply {
                                            time = newAppointment.date
                                            set(java.util.Calendar.HOUR_OF_DAY, 8)
                                            set(java.util.Calendar.MINUTE, 0)
                                            set(java.util.Calendar.SECOND, 0)
                                            add(java.util.Calendar.DATE, -1)
                                        }

                                        val triggerAtMillis = cal.timeInMillis
                                        val alarmManager = this.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
                                        alarmManager.setExact(android.app.AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                                    }
                                    Toast.makeText(this@AppointmentScheduleActivity, "Consulta Salva", Toast.LENGTH_SHORT).show()
                                }
                                .setNegativeButton("Cancelar", null)
                                .show()
                        },
                        onError = { exception ->
                            Toast.makeText(this, R.string.error_creating_appointment, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showPatientSelectionDialog(onPatientSelected: (Patient) -> Unit) {
        lifecycleScope.launch {
            patientViewModel.patients.collectLatest { patients: List<Patient> ->
                if (patients.isEmpty()) {
                    Toast.makeText(this@AppointmentScheduleActivity, "Nenhum paciente cadastrado", Toast.LENGTH_SHORT).show()
                    return@collectLatest
                }

                val patientNames = patients.map { it.name }.toTypedArray()

                MaterialAlertDialogBuilder(this@AppointmentScheduleActivity)
                    .setTitle("Selecionar paciente")
                    .setItems(patientNames) { _, which ->
                        onPatientSelected(patients[which])
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        }
    }

    private fun showDatePicker(onDateSelected: (Date) -> Unit) {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(getString(R.string.appointment_date))
            .setSelection(selectedDate.timeInMillis)
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            onDateSelected(Date(selection))
        }

        datePicker.show(supportFragmentManager, "datePicker")
    }

    private fun showTimePicker(onTimeSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(calendar.get(Calendar.HOUR_OF_DAY))
            .setMinute(calendar.get(Calendar.MINUTE))
            .build()

        timePicker.addOnPositiveButtonClickListener {
            val hour = timePicker.hour.toString().padStart(2, '0')
            val minute = timePicker.minute.toString().padStart(2, '0')
            onTimeSelected("$hour:$minute")
        }

        timePicker.show(supportFragmentManager, "timePicker")
    }

    private fun showRecurrenceSeriesDialog(appointment: Appointment) {
        // Em breve: buscar e exibir todas as ocorrências da série
    }

    private fun setupExportarButton() {
        binding.exportarButton.setOnClickListener {
            val consultas = adapter.currentList
            if (consultas.isNullOrEmpty()) {
                Toast.makeText(this, "Nenhuma consulta para exportar.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val formatos = arrayOf("CSV", "PDF")
            AlertDialog.Builder(this)
                .setTitle("Exportar agenda")
                .setItems(formatos) { _, which ->
                    when (which) {
                        0 -> exportarAgendaCsv(consultas)
                        1 -> exportarAgendaPdf(consultas)
                    }
                }
                .show()
        }
    }

    private fun exportarAgendaCsv(consultas: List<Appointment>) {
        val csvHeader = "Paciente,Data,Horário,Descrição\n"
        val csvBody = consultas.joinToString(separator = "\n") { consulta ->
            "\"${consulta.patientName}\"," +
            "${dateFormat.format(consulta.date)}," +
            "${consulta.startTime} - ${consulta.endTime}," +
            "\"${consulta.description.orEmpty()}\""
        }
        val csv = csvHeader + csvBody
        try {
            val fileName = "agenda_${System.currentTimeMillis()}.csv"
            val file = java.io.File(cacheDir, fileName)
            file.writeText(csv)
            val uri = androidx.core.content.FileProvider.getUriForFile(
                this,
                "$packageName.provider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "Exportar agenda em CSV"))
        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao exportar CSV: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun exportarAgendaPdf(consultas: List<Appointment>) {
        try {
            val fileName = "agenda_${System.currentTimeMillis()}.pdf"
            val file = java.io.File(cacheDir, fileName)
            val pdfDocument = android.graphics.pdf.PdfDocument()
            val paint = android.graphics.Paint()
            val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            var y = 40
            paint.textSize = 18f
            paint.isFakeBoldText = true
            canvas.drawText("Agenda de Consultas", 40f, y.toFloat(), paint)
            y += 40
            paint.textSize = 12f
            paint.isFakeBoldText = false
            consultas.forEach { consulta ->
                val texto = "Paciente: ${consulta.patientName} | Data: ${dateFormat.format(consulta.date)} | Horário: ${consulta.startTime} - ${consulta.endTime}"
                canvas.drawText(texto, 40f, y.toFloat(), paint)
                y += 18
                if (!consulta.description.isNullOrBlank()) {
                    canvas.drawText("Descrição: ${consulta.description}", 60f, y.toFloat(), paint)
                    y += 18
                }
                y += 10
                if (y > 800) { /* Nova página se necessário */ }
            }
            pdfDocument.finishPage(page)
            file.outputStream().use { pdfDocument.writeTo(it) }
            pdfDocument.close()
            val uri = androidx.core.content.FileProvider.getUriForFile(
                this,
                "$packageName.provider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "Exportar agenda em PDF"))
        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao exportar PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_appointment_schedule, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
} 