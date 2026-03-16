package com.psipro.app.ui

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
import com.psipro.app.R
import com.psipro.app.adapter.AppointmentAdapter
import com.psipro.app.data.entities.Appointment
import com.psipro.app.data.entities.AppointmentStatus
import com.psipro.app.data.entities.Patient
import com.psipro.app.data.entities.RecurrenceType
import com.psipro.app.databinding.ActivityAppointmentScheduleBinding
import com.psipro.app.databinding.DialogAppointmentBinding
import com.psipro.app.viewmodel.AppointmentViewModel
import com.psipro.app.ui.viewmodels.PatientViewModel
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
import com.psipro.app.utils.MessageTemplateManager
import com.psipro.app.notification.AgendamentoAlarmManager
import com.psipro.app.notification.AgendamentoNotificationService
import com.psipro.app.ui.viewmodels.NotificationViewModel
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
    @Inject lateinit var agendamentoAlarmManager: AgendamentoAlarmManager
    @Inject lateinit var agendamentoNotificationService: AgendamentoNotificationService
    private lateinit var notificationViewModel: NotificationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppointmentScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Inicializar o NotificationViewModel usando ViewModelProvider
        notificationViewModel = ViewModelProvider(this)[NotificationViewModel::class.java]
        
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
        
        // Verificar se um paciente foi pré-selecionado
        checkPreselectedPatient()
    }
    
    private fun checkPreselectedPatient() {
        val patientId = intent.getLongExtra("PATIENT_ID", -1)
        val patientName = intent.getStringExtra("PATIENT_NAME")
        val isPreselected = intent.getBooleanExtra("PRESELECTED_PATIENT", false)
        val tipoConsulta = intent.getStringExtra("TIPO_CONSULTA")
        
        if (isPreselected && patientId != -1L) {
            // Carregar dados do paciente
            patientViewModel.loadPatient(patientId)
            lifecycleScope.launch {
                patientViewModel.currentPatient.collectLatest { patient ->
                    patient?.let {
                        selectedPatient = it
                        // Abrir automaticamente o diálogo de agendamento
                        showAppointmentDialog(tipoConsulta = tipoConsulta)
                    }
                }
            }
        }
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

    private fun showAppointmentDialog(appointment: Appointment? = null, tipoConsulta: String? = null) {
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

        // Se um paciente foi pré-selecionado, preencher automaticamente
        if (selectedPatient != null) {
            dialogBinding.patientNameInput.setText(selectedPatient!!.name)
            dialogBinding.patientPhoneInput.setText(selectedPatient!!.phone)
            // Desabilitar a seleção de paciente para evitar mudança acidental
            dialogBinding.patientNameLayout.setEndIconOnClickListener(null)
            dialogBinding.patientNameInput.isEnabled = false
            
            // Se for uma reconsulta, preencher o título automaticamente
            if (tipoConsulta == "Reconsulta") {
                dialogBinding.titleInput.setText("Reconsulta - ${selectedPatient!!.name}")
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

        // Botão de teste de notificação
        dialogBinding.testNotificationButton.setOnClickListener {
            testarNotificacao()
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

                // Opções de notificação - NOVAS OPÇÕES VISÍVEIS
                val notification15min = dialogBinding.notification15min.isChecked
                val notification30min = dialogBinding.notification30min.isChecked
                val notification45min = dialogBinding.notification45min.isChecked
                
                // Habilitar notificação se pelo menos uma opção estiver marcada
                val hasNotifications = notification15min || notification30min || notification45min

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
                            
                            // Agendar notificações se habilitado
                            if (hasNotifications) {
                                agendarNotificacoes(newAppointment, notification15min, notification30min, notification45min)
                            }
                            
                            // NOTA: Não criar cobrança automática ao criar agendamento.
                            // Cobrança será criada apenas quando:
                            // - Agendamento for marcado como REALIZADO → cria CobrancaSessao
                            // - Agendamento for marcado como FALTOU/CANCELOU → cria CobrancaAgendamento
                            // - Anotação de sessão for registrada → cria CobrancaSessao
                            
                            // --- ENVIAR MENSAGEM PARA O PACIENTE ---
                            showSendMessageDialog(newAppointment)
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

    private fun showSendMessageDialog(appointment: Appointment) {
        val patientName = appointment.patientName ?: "Paciente"
        val data = dateFormat.format(appointment.date)
        val hora = appointment.startTime
        val tipoConsulta = if (appointment.title.contains("Reconsulta", ignoreCase = true)) "reconsulta" else "consulta"
        
        // Obter nome do psicólogo das SharedPreferences
        val sharedPrefs = getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
        val psicologoNome = sharedPrefs.getString("profile_name", "Profissional") ?: "Profissional"
        
        val templates = listOf(
            "Olá $patientName! Aqui é $psicologoNome. Confirmei sua $tipoConsulta para $data às $hora. Aguardo você!",
            "Oi $patientName! Sua $tipoConsulta foi agendada para $data às $hora. Até lá!",
            "Olá $patientName! Confirmando sua $tipoConsulta: $data às $hora. Qualquer dúvida, me avise!",
            "Oi $patientName! Sua $tipoConsulta está marcada para $data às $hora. Nos vemos lá!",
            "Olá $patientName! Aqui é $psicologoNome. Sua $tipoConsulta foi agendada para $data às $hora. Até breve!"
        )
        
        val items = templates.toTypedArray()
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Enviar mensagem para o paciente?")
            .setMessage("Deseja enviar uma mensagem para $patientName confirmando a $tipoConsulta?")
            .setPositiveButton("Sim, enviar") { _, _ ->
                showMessageTemplateDialog(appointment, templates)
            }
            .setNegativeButton("Não, obrigado") { _, _ ->
                Toast.makeText(this, "Agendamento salvo com sucesso!", Toast.LENGTH_SHORT).show()
            }
            .setNeutralButton("Enviar depois") { _, _ ->
                // Salvar para enviar depois
                saveMessageForLater(appointment, templates[0])
                Toast.makeText(this, "Mensagem salva para enviar depois", Toast.LENGTH_SHORT).show()
            }
            .show()
    }
    
    private fun showMessageTemplateDialog(appointment: Appointment, templates: List<String>) {
        val items = templates.toTypedArray()
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Escolha a mensagem")
            .setItems(items) { _, which ->
                val selectedMessage = templates[which]
                sendWhatsAppMessage(appointment, selectedMessage)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun sendWhatsAppMessage(appointment: Appointment, message: String) {
        val phone = appointment.patientPhone
        if (phone.isNullOrBlank()) {
            Toast.makeText(this, "Telefone do paciente não informado", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Formatar telefone (remover caracteres especiais e adicionar código do país se necessário)
        val formattedPhone = formatPhoneNumber(phone)
        
        // Codificar a mensagem para URL
        val encodedMessage = java.net.URLEncoder.encode(message, "UTF-8")
        
        // Criar URL do WhatsApp
        val whatsappUrl = "https://wa.me/$formattedPhone?text=$encodedMessage"
        
        try {
            val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(whatsappUrl))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            Toast.makeText(this, "Abrindo WhatsApp...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao abrir WhatsApp: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun formatPhoneNumber(phone: String): String {
        // Remover caracteres especiais e espaços
        var formatted = phone.replace(Regex("[^0-9]"), "")
        
        // Se não tiver código do país, adicionar +55 (Brasil)
        if (!formatted.startsWith("55") && formatted.length <= 11) {
            formatted = "55$formatted"
        }
        
        return formatted
    }
    
    private fun saveMessageForLater(appointment: Appointment, message: String) {
        // Salvar mensagem para enviar depois (implementar conforme necessário)
        val sharedPrefs = getSharedPreferences("pending_messages", Context.MODE_PRIVATE)
        val key = "message_${appointment.id}_${System.currentTimeMillis()}"
        sharedPrefs.edit().putString(key, message).apply()
    }

    private fun agendarNotificacoes(appointment: Appointment, notification15min: Boolean, notification30min: Boolean, notification45min: Boolean) {
        val alarmManager = agendamentoAlarmManager
        val notificationService = agendamentoNotificationService

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = appointment.date.time
        calendar.set(Calendar.HOUR_OF_DAY, appointment.startTime.substring(0, 2).toInt())
        calendar.set(Calendar.MINUTE, appointment.startTime.substring(3, 5).toInt())
        calendar.set(Calendar.SECOND, 0)

        val appointmentTime = calendar.timeInMillis
        var notificationsAgendadas = 0

        // Agendar notificação de 15 minutos
        if (notification15min) {
            val reminderTime15 = appointmentTime - (15 * 60 * 1000)
            if (reminderTime15 > System.currentTimeMillis()) {
                agendamentoAlarmManager.agendarLembrete(
                    titulo = appointment.title,
                    paciente = appointment.patientName ?: "Paciente",
                    dataHora = appointment.date,
                    tipoEvento = "Consulta",
                    minutosAntes = 15
                )
                
                // Criar notificação no banco
                notificationViewModel.createAppointmentReminder(
                    title = "Lembrete de Consulta - 15 min",
                    message = "Consulta com ${appointment.patientName ?: "Paciente"} em 15 minutos",
                    appointmentId = appointment.id,
                    patientId = appointment.patientId,
                    scheduledFor = Date(reminderTime15)
                )
                
                notificationsAgendadas++
            }
        }

        // Agendar notificação de 30 minutos
        if (notification30min) {
            val reminderTime30 = appointmentTime - (30 * 60 * 1000)
            if (reminderTime30 > System.currentTimeMillis()) {
                agendamentoAlarmManager.agendarLembrete(
                    titulo = appointment.title,
                    paciente = appointment.patientName ?: "Paciente",
                    dataHora = appointment.date,
                    tipoEvento = "Consulta",
                    minutosAntes = 30
                )
                
                // Criar notificação no banco
                notificationViewModel.createAppointmentReminder(
                    title = "Lembrete de Consulta - 30 min",
                    message = "Consulta com ${appointment.patientName ?: "Paciente"} em 30 minutos",
                    appointmentId = appointment.id,
                    patientId = appointment.patientId,
                    scheduledFor = Date(reminderTime30)
                )
                
                notificationsAgendadas++
            }
        }

        // Agendar notificação de 45 minutos
        if (notification45min) {
            val reminderTime45 = appointmentTime - (45 * 60 * 1000)
            if (reminderTime45 > System.currentTimeMillis()) {
                agendamentoAlarmManager.agendarLembrete(
                    titulo = appointment.title,
                    paciente = appointment.patientName ?: "Paciente",
                    dataHora = appointment.date,
                    tipoEvento = "Consulta",
                    minutosAntes = 45
                )
                
                // Criar notificação no banco
                notificationViewModel.createAppointmentReminder(
                    title = "Lembrete de Consulta - 45 min",
                    message = "Consulta com ${appointment.patientName ?: "Paciente"} em 45 minutos",
                    appointmentId = appointment.id,
                    patientId = appointment.patientId,
                    scheduledFor = Date(reminderTime45)
                )
                
                notificationsAgendadas++
            }
        }

        if (notificationsAgendadas > 0) {
            Toast.makeText(this, "$notificationsAgendadas notificação(ões) agendada(s) para ${dateFormat.format(appointment.date)} às ${appointment.startTime}", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Não foi possível agendar notificações para este horário", Toast.LENGTH_SHORT).show()
        }
    }

    private fun testarNotificacao() {
        // Mostrar notificação imediata para teste
        agendamentoNotificationService.mostrarNotificacaoAgendamento(
            titulo = "Consulta de Teste",
            paciente = selectedPatient?.name ?: "Paciente de Teste",
            dataHora = Date(),
            tipoEvento = "Consulta",
            minutosAntes = 15
        )
        
        // Criar notificação de teste no banco
        notificationViewModel.createAppointmentReminder(
            title = "Notificação de Teste",
            message = "Esta é uma notificação de teste para verificar o sistema",
            patientId = selectedPatient?.id
        )

        Toast.makeText(this, "Notificação de teste enviada! Verifique o painel de notificações.", Toast.LENGTH_LONG).show()
    }
} 



