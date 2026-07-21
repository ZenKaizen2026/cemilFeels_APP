package com.example.cemil_feels

import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.cemil_feels.databinding.ItemSnackBinding
import com.example.cemil_feels.data.model.Snack
import java.text.NumberFormat
import java.util.Locale

/**
 * Adapter untuk RecyclerView yang menampilkan daftar camilan.
 * Mendukung visual selection state pada material card.
 */
class SnackAdapter(
    private val onAddClicked: (Snack) -> Unit,
    private val onCardClicked: (Snack, Boolean) -> Unit
) : ListAdapter<Snack, SnackAdapter.SnackViewHolder>(DiffCallback) {

    // Set penyimpan nama-nama camilan yang sedang terpilih secara visual
    val selectedSnackNames = mutableSetOf<String>()

    class SnackViewHolder(
        val binding: ItemSnackBinding,
        private val onAddClicked: (Snack) -> Unit,
        private val onCardClicked: (Snack, Boolean) -> Unit,
        private val adapter: SnackAdapter
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun updateSelectionState(snack: Snack) {
            val isSelected = adapter.selectedSnackNames.contains(snack.name)
            if (isSelected) {
                binding.root.strokeColor = Color.parseColor("#4CAF50") // Green Success Color
                binding.root.strokeWidth = 6
                binding.root.setCardBackgroundColor(Color.parseColor("#E8F5E9")) // Light Green Tint
                binding.btnAddSnack.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#4CAF50"))
                binding.btnAddSnack.setIconResource(R.drawable.ic_check_circle)
            } else {
                binding.root.strokeColor = Color.parseColor("#E0E0E0")
                binding.root.strokeWidth = 2
                binding.root.setCardBackgroundColor(Color.WHITE)
                binding.btnAddSnack.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#FF7A1A"))
                binding.btnAddSnack.setIconResource(android.R.drawable.ic_input_add)
            }
        }

        fun bind(snack: Snack) {
            binding.tvSnackName.text = snack.name
            binding.tvSnackRating.text = "⭐ ${snack.rating}"
            binding.ivSnackImage.setImageResource(snack.imageResId)

            val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))
            binding.tvSnackPrice.text = "Rp. " + formatter.format(snack.price.toInt())

            // Atur status seleksi visual card
            updateSelectionState(snack)

            // FT-03: Sinkronisasi Stok & Efek Grayscale jika stok 0
            if (snack.stock <= 0) {
                val colorMatrix = ColorMatrix().apply { setSaturation(0f) }
                binding.ivSnackImage.colorFilter = ColorMatrixColorFilter(colorMatrix)
                binding.root.alpha = 0.5f
                binding.btnAddSnack.isEnabled = false
                binding.tvSnackDescription.text = "Stok Habis!"
                binding.root.setOnClickListener(null)
            } else {
                binding.ivSnackImage.colorFilter = null
                binding.root.alpha = 1.0f
                binding.btnAddSnack.isEnabled = true
                binding.tvSnackDescription.text = snack.description
                
                // Click listener untuk tombol tambah (+)
                binding.btnAddSnack.setOnClickListener {
                    val isSelected = adapter.selectedSnackNames.contains(snack.name)
                    if (!isSelected) {
                        adapter.selectedSnackNames.add(snack.name)
                        adapter.notifyItemChanged(bindingAdapterPosition, "SELECTION_CHANGED")
                    }
                    onAddClicked(snack)
                }

                // Click listener untuk card utama sendiri
                binding.root.setOnClickListener {
                    val currentlySelected = adapter.selectedSnackNames.contains(snack.name)
                    if (currentlySelected) {
                        adapter.selectedSnackNames.remove(snack.name)
                    } else {
                        adapter.selectedSnackNames.add(snack.name)
                    }
                    onCardClicked(snack, !currentlySelected)
                    adapter.notifyItemChanged(bindingAdapterPosition, "SELECTION_CHANGED")
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SnackViewHolder {
        val binding = ItemSnackBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SnackViewHolder(binding, onAddClicked, onCardClicked, this)
    }

    override fun onBindViewHolder(holder: SnackViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onBindViewHolder(holder: SnackViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.contains("SELECTION_CHANGED")) {
            holder.updateSelectionState(getItem(position))
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Snack>() {
        override fun areItemsTheSame(oldItem: Snack, newItem: Snack): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: Snack, newItem: Snack): Boolean {
            return oldItem == newItem
        }
    }
}
