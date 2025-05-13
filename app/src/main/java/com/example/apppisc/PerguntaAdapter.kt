package com.example.apppisc

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.apppisc.databinding.ItemPerguntaCardBinding

class PerguntaAdapter(
    private val onPerguntaEdit: (Int, Pergunta) -> Unit = { _, _ -> },
    private val onPerguntaDelete: (Int) -> Unit = {}
) : RecyclerView.Adapter<PerguntaAdapter.PerguntaViewHolder>() {
    private val perguntas = mutableListOf<Pergunta>()

    fun submitList(list: List<Pergunta>) {
        perguntas.clear()
        perguntas.addAll(list)
        notifyDataSetChanged()
    }

    fun addPergunta(pergunta: Pergunta) {
        perguntas.add(pergunta)
        notifyItemInserted(perguntas.size - 1)
    }

    fun removePergunta(position: Int) {
        perguntas.removeAt(position)
        notifyItemRemoved(position)
    }

    fun editPergunta(position: Int, novoTitulo: String) {
        perguntas[position].titulo = novoTitulo
        notifyItemChanged(position)
    }

    fun getPerguntas(): List<Pergunta> = perguntas

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PerguntaViewHolder {
        val binding = ItemPerguntaCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PerguntaViewHolder(binding, parent.context)
    }

    override fun onBindViewHolder(holder: PerguntaViewHolder, position: Int) {
        holder.bind(perguntas[position], position)
    }

    override fun getItemCount() = perguntas.size

    inner class PerguntaViewHolder(private val binding: ItemPerguntaCardBinding, private val context: Context) : RecyclerView.ViewHolder(binding.root) {
        fun bind(pergunta: Pergunta, position: Int) {
            binding.txtPerguntaTitulo.text = pergunta.titulo
            binding.edtResposta.setText(pergunta.resposta)

            binding.btnEditarPergunta.setOnClickListener {
                val editText = EditText(context)
                editText.setText(pergunta.titulo)
                AlertDialog.Builder(context)
                    .setTitle("Editar Pergunta")
                    .setView(editText)
                    .setPositiveButton("OK") { _, _ ->
                        val novoTitulo = editText.text.toString().trim()
                        if (novoTitulo.isNotEmpty()) {
                            onPerguntaEdit(position, pergunta.copy(titulo = novoTitulo))
                        }
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }

            binding.btnDeletarPergunta.setOnClickListener {
                AlertDialog.Builder(context)
                    .setTitle("Remover Pergunta")
                    .setMessage("Deseja remover esta pergunta?")
                    .setPositiveButton("Sim") { _, _ ->
                        onPerguntaDelete(position)
                        Toast.makeText(context, "Pergunta removida", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Não", null)
                    .show()
            }
        }
    }
}

data class Pergunta(var titulo: String, var resposta: String) 