package com.psipro.app.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.psipro.app.CadastroPacienteActivity
import com.psipro.app.DetalhePacienteActivity
import com.psipro.app.databinding.FragmentPatientsBinding
import com.psipro.app.adapter.PatientAdapter
import com.psipro.app.ui.viewmodels.PatientViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import dagger.hilt.android.AndroidEntryPoint
import com.google.android.material.snackbar.Snackbar
import androidx.navigation.fragment.findNavController
import com.psipro.app.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import android.widget.Toast
import android.app.Activity
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.text.SimpleDateFormat
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.Normalizer

@AndroidEntryPoint
class PatientsFragment : Fragment() {
    private var _binding: FragmentPatientsBinding? = null
    private val binding get() = _binding!!
    private val patientViewModel: PatientViewModel by viewModels()
    private lateinit var patientAdapter: PatientAdapter

    // Função para normalizar headers
    fun normalizeHeader(header: String): String {
        return Normalizer.normalize(header.lowercase(), Normalizer.Form.NFD)
            .replace(Regex("""\p{InCombiningDiacriticalMarks}+"""), "")
            .replace(Regex("""[^a-z0-9 ]"""), "")
            .replace(Regex("""\s+"""), " ")
            .trim()
    }

    private val importExcelLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val uri: Uri? = data?.data
            if (uri != null) {
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        val inputStream = requireContext().contentResolver.openInputStream(uri)
                        val workbook = WorkbookFactory.create(inputStream)
                        val sheet = workbook.getSheetAt(0)
                        val dateFormats = listOf("dd/MM/yyyy", "yyyy-MM-dd", "MM/dd/yyyy")
                        var importedCount = 0
                        var duplicatedCount = 0
                        var invalidCount = 0
                        val ignoredRows = mutableListOf<Int>()
                        val headerRow = sheet.getRow(0)
                        val colMap = mutableMapOf<String, Int>()
                        for (cellIdx in 0 until headerRow.lastCellNum) {
                            val header = headerRow.getCell(cellIdx)?.toString()?.trim() ?: ""
                            val normalized = normalizeHeader(header)
                            colMap[normalized] = cellIdx
                        }
                        val nomeKeys = listOf("nome", "nome completo")
                        val telefoneKeys = listOf("telefone", "celular")
                        val emailKeys = listOf("email", "e-mail")
                        val nascimentoKeys = listOf("data de nascimento", "nascimento")
                        val cpfKeys = listOf("cpf")
                        val obsKeys = listOf("observacoes", "observacao", "obs")
                        fun getCell(row: org.apache.poi.ss.usermodel.Row, keys: List<String>): String {
                            for (key in keys) {
                                val idx = colMap[key]
                                if (idx != null) return row.getCell(idx)?.toString()?.trim() ?: ""
                            }
                            return ""
                        }
                        for (rowIndex in 1..sheet.lastRowNum) { // Pula o header
                            val row = sheet.getRow(rowIndex)
                            if (row != null) {
                                val nome = getCell(row, nomeKeys)
                                val telefone = getCell(row, telefoneKeys)
                                val email = getCell(row, emailKeys)
                                val nascimentoStr = getCell(row, nascimentoKeys)
                                val cpf = getCell(row, cpfKeys)
                                val observacoes = getCell(row, obsKeys)
                                if (nome.isBlank() || nascimentoStr.isBlank()) {
                                    invalidCount++
                                    ignoredRows.add(rowIndex+1)
                                    continue
                                }
                                // Tenta converter a data
                                val birthDate: Date? = try {
                                    val idx = nascimentoKeys.mapNotNull { colMap[it] }.firstOrNull() ?: -1
                                    val cell = if (idx >= 0) row.getCell(idx) else null
                                    when {
                                        cell == null -> null
                                        cell.cellType == org.apache.poi.ss.usermodel.CellType.NUMERIC -> cell.dateCellValue
                                        else -> {
                                            var parsed: Date? = null
                                            for (fmt in dateFormats) {
                                                try {
                                                    parsed = SimpleDateFormat(fmt).parse(nascimentoStr)
                                                    if (parsed != null) break
                                                } catch (_: Exception) {}
                                            }
                                            parsed
                                        }
                                    }
                                } catch (e: Exception) { null }
                                if (birthDate == null) {
                                    invalidCount++
                                    ignoredRows.add(rowIndex+1)
                                    continue
                                }
                                val patient = com.psipro.app.data.entities.Patient(
                                    name = nome,
                                    phone = telefone,
                                    email = email,
                                    birthDate = birthDate,
                                    cpf = cpf,
                                    notes = observacoes
                                )
                                patientViewModel.savePatient(patient)
                                importedCount++
                            }
                        }
                        inputStream?.close()
                        val resumo = "Importados: $importedCount\nIgnorados (inválidos ou duplicados): ${invalidCount + duplicatedCount}\nLinhas ignoradas: ${ignoredRows.joinToString()}"
                        Toast.makeText(requireContext(), resumo, Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Erro ao importar Excel: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Nenhum arquivo selecionado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPatientsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        binding.addPatientFab.setOnClickListener {
            startActivity(Intent(requireContext(), CadastroPacienteActivity::class.java))
        }

        patientAdapter = PatientAdapter(
            onItemClick = { patient ->
                val intent = Intent(requireContext(), DetalhePacienteActivity::class.java)
                intent.putExtra("PATIENT_ID", patient.id)
                startActivity(intent)
            },
            onScheduleClick = { patient ->
                // Navegar para a Agenda (ScheduleFragment)
                parentFragment?.parentFragmentManager?.let { fm ->
                    // Tenta obter o NavController do fragmento pai
                    val navController = findNavController()
                    navController.navigate(R.id.navigation_schedule)
                } ?: run {
                    // Fallback: tenta obter o NavController da Activity
                    requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)?.selectedItemId = R.id.navigation_schedule
                }
            },
            onDeleteClick = { patient ->
                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Excluir paciente")
                    .setMessage("Tem certeza que deseja excluir este paciente?")
                    .setPositiveButton("Excluir") { _, _ ->
                        patientViewModel.deletePatient(patient)
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        )
        binding.patientsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.patientsRecyclerView.adapter = patientAdapter

        // Observa a lista de pacientes
        viewLifecycleOwner.lifecycleScope.launch {
            patientViewModel.patients.collectLatest { patients ->
                patientAdapter.submitList(patients)
            }
        }

        // Busca
        binding.searchEditText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                patientViewModel.setSearchQuery(s?.toString() ?: "")
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_patient_list, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_import_patients -> {
                // Abrir seletor de arquivos para Excel
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                importExcelLauncher.launch(intent)
                true
            }
            R.id.action_test -> {
                // Download do arquivo de exemplo Excel
                try {
                    val inputStream: InputStream = resources.openRawResource(R.raw.modelo_importacao_pacientes)
                    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    val outFile = File(downloadsDir, "modelo_importacao_pacientes.xlsx")
                    val outputStream = FileOutputStream(outFile)
                    inputStream.copyTo(outputStream)
                    inputStream.close()
                    outputStream.close()
                    Toast.makeText(requireContext(), "Arquivo salvo em Downloads/modelo_importacao_pacientes.xlsx", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Erro ao salvar arquivo: ${e.message}", Toast.LENGTH_LONG).show()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
} 



