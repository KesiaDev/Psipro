package com.example.psipro.ui.fragments


import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.psipro.R
import com.example.psipro.databinding.FragmentConfiguracoesBinding
import com.example.psipro.ui.EditProfileDialog
import android.util.Log
import com.example.psipro.ui.EditProfileActivity

class ConfiguracoesFragment : Fragment() {
    private var _binding: FragmentConfiguracoesBinding? = null
    private val binding get() = _binding!!

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.forEach { (perm, granted) ->
            Log.d("PermissaoDebug", "Permissão: $perm, concedida: $granted")
        }
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            EditProfileDialog().show(childFragmentManager, "editProfile")
        } else {
            Toast.makeText(
                requireContext(),
                "Permissões necessárias não concedidas",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConfiguracoesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)

        // Alternância de tema (claro/escuro)
        val isDarkMode = prefs.getString("app_theme", "light") == "dark"
        binding.darkModeSwitch.isChecked = isDarkMode
        binding.darkModeSwitch.text = if (isDarkMode) "Modo Escuro" else "Modo Claro"
        binding.darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            val mode = if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            val themePref = if (isChecked) "dark" else "light"
            prefs.edit().putString("app_theme", themePref).apply()
            AppCompatDelegate.setDefaultNightMode(mode)
            binding.darkModeSwitch.text = if (isChecked) "Modo Escuro" else "Modo Claro"
            val msg = if (isChecked) "Modo escuro ativado" else "Modo claro ativado"
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        }

        // Notificações
        val notificationsEnabled = prefs.getBoolean("notifications_enabled", true)
        binding.notificationsSwitch.isChecked = notificationsEnabled
        binding.notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("notifications_enabled", isChecked).apply()
            val msg = if (isChecked) "Notificações ativadas" else "Notificações desativadas"
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        }

        // Listeners dos botões
        binding.editProfileButton.setOnClickListener {
            // Abrir a tela de edição de perfil como Activity e aguardar resultado
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            startActivityForResult(intent, EDIT_PROFILE_REQUEST_CODE)
        }
        binding.privacyPolicyButton.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Política de Privacidade")
                .setMessage("Seus dados são coletados apenas para fins de funcionamento do aplicativo e não são compartilhados com terceiros. Para mais informações, entre em contato com o suporte.")
                .setPositiveButton("OK", null)
                .show()
        }
        binding.termsButton.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Termos de Uso")
                .setMessage("Ao utilizar este aplicativo, você concorda com os termos e condições estabelecidos para o uso responsável e seguro da plataforma.")
                .setPositiveButton("OK", null)
                .show()
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf(Manifest.permission.CAMERA)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        } else {
            permissions.add("android.permission.READ_MEDIA_IMAGES")
        }

        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionsToRequest.isEmpty()) {
            EditProfileDialog().show(childFragmentManager, "editProfile")
        } else {
            requestPermissionLauncher.launch(permissionsToRequest)
        }
    }

    companion object {
        private const val EDIT_PROFILE_REQUEST_CODE = 1002
    }
} 