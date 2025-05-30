package com.example.psipro.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.psipro.data.AppDatabase
import com.example.psipro.data.entities.AuditLog
import com.example.psipro.databinding.ActivityAuditLogBinding
import com.example.psipro.ui.AuditLogViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import java.text.SimpleDateFormat
import java.util.*
import android.widget.Toast
import com.example.psipro.data.repository.AuditLogRepository
import com.example.psipro.R

class AuditLogActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AuditLogAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        recyclerView = RecyclerView(this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        setContentView(recyclerView)
        adapter = AuditLogAdapter()
        recyclerView.adapter = adapter

        loadLogs()
    }

    private fun loadLogs() {
        CoroutineScope(Dispatchers.IO).launch {
            val logs = AppDatabase.getInstance(this@AuditLogActivity).auditLogDao().getAllLogs()
            withContext(Dispatchers.Main) {
                adapter.submitList(logs)
                if (logs.isEmpty()) {
                    Toast.makeText(this@AuditLogActivity, "Nenhum log de auditoria encontrado.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}

class AuditLogAdapter : ListAdapter<AuditLog, AuditLogAdapter.LogViewHolder>(DIFF) {
    companion object {
        val DIFF = object : DiffUtil.ItemCallback<AuditLog>() {
            override fun areItemsTheSame(oldItem: AuditLog, newItem: AuditLog) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: AuditLog, newItem: AuditLog) = oldItem == newItem
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val tv = TextView(parent.context)
        tv.setPadding(24, 24, 24, 24)
        return LogViewHolder(tv)
    }
    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    class LogViewHolder(private val tv: TextView) : RecyclerView.ViewHolder(tv) {
        fun bind(log: AuditLog) {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
            tv.text = "Ação: ${log.action}\nAlvo: ${log.target}\nData: ${sdf.format(Date(log.timestamp))}"
        }
    }
} 