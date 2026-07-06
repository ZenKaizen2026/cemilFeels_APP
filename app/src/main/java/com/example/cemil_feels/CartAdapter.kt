package com.example.cemil_feels

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.cemil_feels.databinding.ItemCartBinding
import java.text.NumberFormat
import java.util.Locale

/**
 * Representasi item belanja di dalam keranjang belanja.
 */
data class CartItem(
    val snack: Snack,
    val qty: Int
)

/**
 * Adapter untuk RecyclerView yang menampilkan daftar item belanja di keranjang.
 */
class CartAdapter(
    private val onPlusClicked: (CartItem) -> Unit,
    private val onMinusClicked: (CartItem) -> Unit,
    private val onRemoveClicked: (CartItem) -> Unit
) : ListAdapter<CartItem, CartAdapter.CartViewHolder>(CartDiffCallback) {

    class CartViewHolder(
        private val binding: ItemCartBinding,
        private val onPlusClicked: (CartItem) -> Unit,
        private val onMinusClicked: (CartItem) -> Unit,
        private val onRemoveClicked: (CartItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(cartItem: CartItem) {
            val snack = cartItem.snack
            binding.tvCartName.text = snack.name
            binding.tvCartDesc.text = snack.description
            binding.ivCartImage.setImageResource(snack.imageResId)
            binding.tvCartQty.text = cartItem.qty.toString()

            // Format harga
            val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))
            val itemTotalPrice = snack.price * cartItem.qty
            binding.tvCartPrice.text = "Rp. " + formatter.format(itemTotalPrice.toInt())

            // Bind click listeners
            binding.btnCartPlus.setOnClickListener {
                onPlusClicked(cartItem)
            }

            binding.btnCartMinus.setOnClickListener {
                if (cartItem.qty > 1) {
                    onMinusClicked(cartItem)
                }
            }

            binding.btnCartRemove.setOnClickListener {
                onRemoveClicked(cartItem)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CartViewHolder(binding, onPlusClicked, onMinusClicked, onRemoveClicked)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object CartDiffCallback : DiffUtil.ItemCallback<CartItem>() {
        override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem.snack.name == newItem.snack.name
        }

        override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem == newItem
        }
    }
}
