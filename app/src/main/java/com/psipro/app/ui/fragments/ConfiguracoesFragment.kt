package com.psipro.app.ui.fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.psipro.app.R
import com.psipro.app.databinding.FragmentConfiguracoesBinding
import com.psipro.app.ui.EditProfileDialog
import android.util.Log
import com.psipro.app.utils.AccessibilityPreferences
import com.psipro.app.ui.EditProfileActivity
import com.psipro.app.ui.ClinicSwitchDialog
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import com.psipro.app.sync.BackendAuthManager
import com.psipro.app.sync.di.SyncEntryPoint
import com.psipro.app.sync.work.SyncScheduler

@AndroidEntryPoint
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
            context?.let { Toast.makeText(it, "Permissões necessárias não concedidas", Toast.LENGTH_SHORT).show() }
        }
    }

    // Novo launcher para EditProfileActivity
    private val editProfileLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            (activity as? com.psipro.app.DashboardActivity)?.updateDrawerHeader()
            context?.let { Toast.makeText(it, "Perfil atualizado com sucesso!", Toast.LENGTH_SHORT).show() }
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
        if (_binding == null) return

        val ctx = context ?: return
        val prefs = ctx.getSharedPreferences("settings", Context.MODE_PRIVATE)

        // Tema: Modo Escuro (ON) / Modo Claro (OFF)
        val themeMode = prefs.getString("theme_mode", "system")
        val isDarkBySystem = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        binding.darkModeSwitch.isChecked = when (themeMode) {
            "dark" -> true
            "light" -> false
            else -> isDarkBySystem
        }
        binding.darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            val mode = if (isChecked) "dark" else "light"
            prefs.edit().putString("theme_mode", mode).apply()
            applyTheme(mode)
            val msg = if (isChecked) "Modo escuro ativado" else "Modo claro ativado"
            Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show()
        }

        // Notificações
        val notificationsEnabled = prefs.getBoolean("notifications_enabled", true)
        binding.notificationsSwitch.isChecked = notificationsEnabled
        binding.notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("notifications_enabled", isChecked).apply()
            val msg = if (isChecked) "Notificações ativadas" else "Notificações desativadas"
            Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show()
        }

        // Trocar clínica (só para usuários logados no backend)
        val entryPoint = EntryPointAccessors.fromApplication(ctx.applicationContext, SyncEntryPoint::class.java)
        binding.clinicCard.visibility = if (entryPoint.backendAuthManager().isBackendAuthenticated()) View.VISIBLE else View.GONE
        binding.switchClinicButton.setOnClickListener {
            ClinicSwitchDialog().show(childFragmentManager, "clinicSwitch")
        }

        // Sincronizar agora (limpa watermarks e força sync completo)
        binding.syncCard.visibility = if (entryPoint.backendAuthManager().isBackendAuthenticated()) View.VISIBLE else View.GONE
        binding.syncNowButton.setOnClickListener {
            entryPoint.sessionStore().clearSyncWatermarks()
            com.psipro.app.sync.work.SyncScheduler.enqueueBoth(ctx, "manual_sync")
            Toast.makeText(ctx, "Sincronização iniciada. A agenda será atualizada em instantes.", Toast.LENGTH_LONG).show()
        }

        // Listeners dos botões
        binding.editProfileButton.setOnClickListener {
            val intent = Intent(ctx, EditProfileActivity::class.java)
            editProfileLauncher.launch(intent)
        }
        binding.privacyPolicyButton.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(ctx)
                .setTitle("Política de Privacidade")
                .setMessage("Seus dados são coletados apenas para fins de funcionamento do aplicativo e não são compartilhados com terceiros. Para mais informações, entre em contato com o suporte.")
                .setPositiveButton("OK", null)
                .show()
        }
        binding.termsButton.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(ctx)
                .setTitle("Termos de Uso")
                .setMessage("Ao utilizar este aplicativo, você concorda com os termos e condições estabelecidos para o uso responsável e seguro da plataforma.")
                .setPositiveButton("OK", null)
                .show()
        }

        // Acessibilidade
        binding.accessibilityLargeFontSwitch.isChecked = AccessibilityPreferences.getLargeFont(ctx)
        binding.accessibilityLargeFontSwitch.setOnCheckedChangeListener { _, isChecked ->
            AccessibilityPreferences.setLargeFont(ctx, isChecked)
            Toast.makeText(ctx, if (isChecked) "Fonte ampliada ativada. Reiniciando app…" else "Fonte ampliada desativada. Reiniciando app…", Toast.LENGTH_SHORT).show()
            restartAppForFontScale(ctx)
        }
        binding.accessibilityHighContrastSwitch.isChecked = AccessibilityPreferences.getHighContrast(ctx)
        binding.accessibilityHighContrastSwitch.setOnCheckedChangeListener { _, isChecked ->
            AccessibilityPreferences.setHighContrast(ctx, isChecked)
            Toast.makeText(ctx, if (isChecked) "Alto contraste ativado. Reinicie o app para aplicar." else "Alto contraste desativado.", Toast.LENGTH_SHORT).show()
            activity?.recreate()
        }
        binding.accessibilityLargerButtonsSwitch.isChecked = AccessibilityPreferences.getLargerButtons(ctx)
        binding.accessibilityLargerButtonsSwitch.setOnCheckedChangeListener { _, isChecked ->
            AccessibilityPreferences.setLargerButtons(ctx, isChecked)
            Toast.makeText(ctx, if (isChecked) "Botões maiores ativados." else "Botões maiores desativados.", Toast.LENGTH_SHORT).show()
            activity?.recreate()
        }
        binding.accessibilitySpacingSwitch.isChecked = AccessibilityPreferences.getIncreasedSpacing(ctx)
        binding.accessibilitySpacingSwitch.setOnCheckedChangeListener { _, isChecked ->
            AccessibilityPreferences.setIncreasedSpacing(ctx, isChecked)
            Toast.makeText(ctx, if (isChecked) "Espaçamento aumentado ativado." else "Espaçamento aumentado desativado.", Toast.LENGTH_SHORT).show()
            activity?.recreate()
        }
    }

    private fun restartAppForFontScale(context: Context) {
        // Fonte ampliada usa attachBaseContext; só aplica ao reiniciar o processo
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)
        android.os.Process.killProcess(android.os.Process.myPid())
    }

    private fun applyTheme(mode: String) {
        val nightMode = when (mode) {
            "dark" -> AppCompatDelegate.MODE_NIGHT_YES
            "light" -> AppCompatDelegate.MODE_NIGHT_NO
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
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



