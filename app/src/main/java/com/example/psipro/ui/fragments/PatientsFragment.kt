package com.example.psipro.ui.fragments

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
import com.example.psipro.CadastroPacienteActivity
import com.example.psipro.DetalhePacienteActivity
import com.example.psipro.databinding.FragmentPatientsBinding
import com.example.psipro.adapter.PatientAdapter
import com.example.psipro.ui.viewmodels.PatientViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import dagger.hilt.android.AndroidEntryPoint
import com.google.android.material.snackbar.Snackbar
import androidx.navigation.fragment.findNavController
import com.example.psipro.R
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

@AndroidEntryPoint
class PatientsFragment : Fragment() {
    private var _binding: FragmentPatientsBinding? = null
    private val binding get() = _binding!!
    private val patientViewModel: PatientViewModel by viewModels()
    private lateinit var patientAdapter: PatientAdapter
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
                        val dateFormat = SimpleDateFormat("dd/MM/yyyy")
                        var importedCount = 0
                        var duplicatedCount = 0
                        var invalidCount = 0
                        val ignoredRows = mutableListOf<Int>()
                        for (rowIndex in 1..sheet.lastRowNum) { // Pula o header
                            val row = sheet.getRow(rowIndex)
                            if (row != null) {
                                val nome = row.getCell(0)?.toString()?.trim() ?: ""
                                val telefone = row.getCell(1)?.toString()?.trim() ?: ""
                                val email = row.getCell(2)?.toString()?.trim() ?: ""
                                val nascimentoStr = row.getCell(3)?.toString()?.trim() ?: ""
                                val cpf = row.getCell(4)?.toString()?.trim() ?: ""
                                val observacoes = row.getCell(5)?.toString()?.trim() ?: ""
                                if (nome.isBlank() || nascimentoStr.isBlank()) {
                                    invalidCount++
                                    ignoredRows.add(rowIndex+1)
                                    continue
                                }
                                val birthDate: Date? = try { dateFormat.parse(nascimentoStr) } catch (e: Exception) { null }
                                if (birthDate == null) {
                                    invalidCount++
                                    ignoredRows.add(rowIndex+1)
                                    continue
                                }
                                // Evitar duplicidade por CPF (se informado)
                                val exists = withContext(Dispatchers.IO) {
                                    if (cpf.isNotBlank()) patientViewModel.getPatientByCpf(cpf) else null
                                }
                                if (exists != null) {
                                    duplicatedCount++
                                    ignoredRows.add(rowIndex+1)
                                    continue
                                }
                                val patient = com.example.psipro.data.entities.Patient(
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