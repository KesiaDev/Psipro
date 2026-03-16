package com.psipro.app.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.psipro.app.databinding.ActivityHelpBinding
import com.psipro.app.ui.adapters.FaqAdapter
import com.psipro.app.ui.models.FaqItem
import com.psipro.app.R

class HelpActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityHelpBinding
    private lateinit var adapter: FaqAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHelpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupRecyclerView()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.help)
    }
    
    private fun setupRecyclerView() {
        adapter = FaqAdapter(getFaqItems())
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@HelpActivity)
            adapter = this@HelpActivity.adapter
        }
    }
    
    private fun getFaqItems(): List<FaqItem> {
        return listOf(
            FaqItem(
                getString(R.string.faq_question_1),
                getString(R.string.faq_answer_1)
            ),
            FaqItem(
                getString(R.string.faq_question_2),
                getString(R.string.faq_answer_2)
            ),
            FaqItem(
                getString(R.string.faq_question_3),
                getString(R.string.faq_answer_3)
            ),
            FaqItem(
                getString(R.string.faq_question_4),
                getString(R.string.faq_answer_4)
            ),
            FaqItem(
                getString(R.string.faq_question_5),
                getString(R.string.faq_answer_5)
            )
        )
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
} 



