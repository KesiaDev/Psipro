package com.example.psipro

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.psipro.databinding.ActivityProntuarioEditBinding

class ProntuarioEditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProntuarioEditBinding
    private lateinit var perguntasAdapter: PerguntaAdapter
    private val perguntasIniciais = mutableListOf(
        Pergunta("Queixa Inicial?", ""),
        Pergunta("Evolução", ""),
        Pergunta("Observações", "")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProntuarioEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        perguntasAdapter = PerguntaAdapter(
            onPerguntaEdit = { pos, novaPergunta ->
                perguntasIniciais[pos].titulo = novaPergunta.titulo
                perguntasAdapter.submitList(perguntasIniciais.toList())
                Toast.makeText(this, "Pergunta editada!", Toast.LENGTH_SHORT).show()
            },
            onPerguntaDelete = { pos ->
                perguntasIniciais.removeAt(pos)
                perguntasAdapter.submitList(perguntasIniciais.toList())
            }
        )

        binding.recyclerPerguntas.layoutManager = LinearLayoutManager(this)
        binding.recyclerPerguntas.adapter = perguntasAdapter
        perguntasAdapter.submitList(perguntasIniciais.toList())

        binding.btnAdicionarPergunta.setOnClickListener {
            perguntasIniciais.add(Pergunta("Nova Pergunta", ""))
            perguntasAdapter.submitList(perguntasIniciais.toList())
            binding.recyclerPerguntas.scrollToPosition(perguntasIniciais.size - 1)
        }

        binding.btnSalvarProntuario.setOnClickListener {
            Toast.makeText(this, "Prontuário salvo!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
} 