package com.example.psipro.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.psipro.databinding.ItemFaqBinding
import com.example.psipro.ui.models.FaqItem

class FaqAdapter(
    private val items: List<FaqItem>
) : RecyclerView.Adapter<FaqAdapter.FaqViewHolder>() {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FaqViewHolder {
        val binding = ItemFaqBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FaqViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: FaqViewHolder, position: Int) {
        holder.bind(items[position])
    }
    
    override fun getItemCount(): Int = items.size
    
    class FaqViewHolder(
        private val binding: ItemFaqBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: FaqItem) {
            binding.questionTextView.text = item.question
            binding.answerTextView.text = item.answer
            binding.answerTextView.visibility = if (item.isExpanded) {
                ViewGroup.VISIBLE
            } else {
                ViewGroup.GONE
            }
            
            binding.root.setOnClickListener {
                item.isExpanded = !item.isExpanded
                binding.answerTextView.visibility = if (item.isExpanded) {
                    ViewGroup.VISIBLE
                } else {
                    ViewGroup.GONE
                }
            }
        }
    }
} 