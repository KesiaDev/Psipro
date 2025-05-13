import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.apppisc.R
import com.example.apppisc.ui.fragments.FinanceiroRegistro

class FinanceiroAdapter(
    private val registros: List<FinanceiroRegistro>,
    private val onItemClick: ((FinanceiroRegistro) -> Unit)? = null,
    private val onItemLongClick: ((FinanceiroRegistro) -> Unit)? = null
) : RecyclerView.Adapter<FinanceiroAdapter.FinanceiroViewHolder>() {

    class FinanceiroViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val descricao: TextView = view.findViewById(R.id.itemDescricao)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FinanceiroViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_financeiro, parent, false)
        return FinanceiroViewHolder(view)
    }

    override fun onBindViewHolder(holder: FinanceiroViewHolder, position: Int) {
        val registro = registros[position]
        holder.descricao.text = registro.descricao
        holder.itemView.setOnClickListener { onItemClick?.invoke(registro) }
        holder.itemView.setOnLongClickListener {
            onItemLongClick?.invoke(registro)
            true
        }
    }

    override fun getItemCount() = registros.size
}

 