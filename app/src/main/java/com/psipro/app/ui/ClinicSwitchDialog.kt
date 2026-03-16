package com.psipro.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.psipro.app.R
import com.psipro.app.databinding.DialogClinicSwitchBinding
import com.psipro.app.sync.BackendAuthManager
import com.psipro.app.sync.api.RemoteClinic
import com.psipro.app.sync.di.SyncEntryPoint
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class ClinicSwitchDialog : DialogFragment() {

    private var _binding: DialogClinicSwitchBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogClinicSwitchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setTitle(getString(R.string.switch_clinic))
        loadClinics()
        binding.cancelButton.setOnClickListener { dismiss() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadClinics() {
        val appContext = requireContext().applicationContext
        val entryPoint = EntryPointAccessors.fromApplication(appContext, SyncEntryPoint::class.java)
        val api = entryPoint.backendApiService()
        val auth = entryPoint.backendAuthManager()
        val currentClinicId = entryPoint.sessionStore().getClinicId()

        binding.progressBar.visibility = View.VISIBLE
        binding.clinicList.visibility = View.GONE
        binding.errorText.visibility = View.GONE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val resp = withContext(Dispatchers.IO) { api.getClinics() }
                binding.progressBar.visibility = View.GONE
                if (resp.isSuccessful && resp.body() != null) {
                    val clinics = resp.body()!!
                    if (clinics.isEmpty()) {
                        binding.errorText.text = getString(R.string.no_clinics_available)
                        binding.errorText.visibility = View.VISIBLE
                    } else {
                        showClinicList(clinics, currentClinicId) { clinic ->
                            switchClinic(clinic, auth, entryPoint)
                        }
                        binding.clinicList.visibility = View.VISIBLE
                    }
                } else {
                    binding.errorText.text = getString(R.string.error_loading_clinics)
                    binding.errorText.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.errorText.text = getString(R.string.error_loading_clinics) + ": " + e.message
                binding.errorText.visibility = View.VISIBLE
            }
        }
    }

    private fun showClinicList(
        clinics: List<RemoteClinic>,
        currentClinicId: String?,
        onSelect: (RemoteClinic) -> Unit
    ) {
        binding.clinicList.removeAllViews()
        for (clinic in clinics) {
            val item = layoutInflater.inflate(android.R.layout.simple_list_item_1, binding.clinicList, false)
            val text = item.findViewById<android.widget.TextView>(android.R.id.text1)
            val isCurrent = clinic.id == currentClinicId
            text.text = if (isCurrent) "${clinic.name} ✓" else clinic.name
            text.setPadding(48, 32, 48, 32)
            item.setOnClickListener {
                if (!isCurrent) onSelect(clinic)
            }
            binding.clinicList.addView(item)
        }
    }

    private fun switchClinic(clinic: RemoteClinic, auth: BackendAuthManager, entryPoint: SyncEntryPoint) {
        binding.progressBar.visibility = View.VISIBLE
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val ok = withContext(Dispatchers.IO) { auth.switchClinic(clinic.id) }
                binding.progressBar.visibility = View.GONE
                if (ok) {
                    withContext(Dispatchers.IO) {
                        entryPoint.syncPatientsManager().sync("clinic_switch")
                        entryPoint.syncAppointmentsManager().sync("clinic_switch")
                        entryPoint.syncSessionsManager().sync("clinic_switch")
                        entryPoint.syncPaymentsManager().sync("clinic_switch")
                    }
                    Toast.makeText(requireContext(), getString(R.string.clinic_switched, clinic.name), Toast.LENGTH_SHORT).show()
                    (activity as? com.psipro.app.DashboardActivity)?.updateDrawerHeader()
                    dismiss()
                } else {
                    Toast.makeText(requireContext(), getString(R.string.error_switching_clinic), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), getString(R.string.error_switching_clinic) + ": " + e.message, Toast.LENGTH_LONG).show()
            }
        }
    }
}
