package com.example.apppisc

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apppisc.databinding.ActivityProntuarioListBinding

class ProntuarioListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProntuarioListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProntuarioListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar RecyclerView (adapte para seu adapter real)
        binding.recyclerProntuarios.layoutManager = LinearLayoutManager(this)
        // binding.recyclerProntuarios.adapter = ProntuarioAdapter(...)

        binding.btnNovoProntuario.setOnClickListener {
            val intent = Intent(this, ProntuarioEditActivity::class.java)
            startActivity(intent)
        }
    }
} 