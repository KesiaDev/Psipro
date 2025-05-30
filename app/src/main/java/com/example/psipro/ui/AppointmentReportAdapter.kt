package com.example.psipro.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.psipro.data.entities.AppointmentReport
import com.example.psipro.databinding.ItemAppointmentReportBinding

class AppointmentReportAdapter : ListAdapter<AppointmentReport, AppointmentReportAdapter.ViewHolder>(DiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAppointmentReportBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemAppointmentReportBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(report: AppointmentReport) {
            binding.textTitle.text = report.title
            binding.textContent.text = report.content
            binding.textCreatedAt.text = report.createdAt.toString()
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<AppointmentReport>() {
        override fun areItemsTheSame(oldItem: AppointmentReport, newItem: AppointmentReport): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: AppointmentReport, newItem: AppointmentReport): Boolean = oldItem == newItem
    }
} 