package com.example.cemil_feels

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.cemil_feels.databinding.ItemSnackBinding
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
        
        fun bind(snack: Snack) {
            binding.tvSnackName.text = snack.name
            binding.tvSnackRating.text = "⭐ ${snack.rating}"
            binding.ivSnackImage.setImageResource(snack.imageResId)

            val context = binding.root.context
            val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))
            binding.tvSnackPrice.text = "Rp. " + formatter.format(snack.price.toInt())

            // Atur status seleksi visual card (Border & Background Tint)
            val isSelected = adapter.selectedSnackNames.contains(snack.name)
            if (isSelected) {
                binding.root.strokeColor = ContextCompat.getColor(context, R.color.colorPrimary)
                binding.root.strokeWidth = 6 // Tebal garis border saat dipilih (~3dp)
                binding.root.setCardBackgroundColor(ContextCompat.getColor(context, R.color.colorSelected))
            } else {
                binding.root.strokeColor = ContextCompat.getColor(context, R.color.colorGrayLight)
                binding.root.strokeWidth = 2 // Tebal garis border normal (~1dp)
                binding.root.setCardBackgroundColor(ContextCompat.getColor(context, R.color.white))
            }

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
                    adapter.notifyItemChanged(bindingAdapterPosition)
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

    companion object DiffCallback : DiffUtil.ItemCallback<Snack>() {
        override fun areItemsTheSame(oldItem: Snack, newItem: Snack): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: Snack, newItem: Snack): Boolean {
            return oldItem == newItem
        }
    }
}
