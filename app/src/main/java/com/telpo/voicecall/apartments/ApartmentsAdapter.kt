package com.telpo.voicecall.apartments

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.telpo.voicecall.databinding.ApartmentsViewBinding

class ApartmentsAdapter(
    private val onItemClick: (Apartment) -> Unit
) : ListAdapter<Apartment, ApartmentsAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(private var binding: ApartmentsViewBinding) : RecyclerView.ViewHolder(binding.root) {
        val apartmentView = binding.apartmentView

        fun bind(apartment: Apartment) {
            binding.apartment = apartment
            binding.executePendingBindings()
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Apartment>() {
        override fun areItemsTheSame(oldItem: Apartment, newItem: Apartment): Boolean {
            return oldItem.apartment == newItem.apartment
        }

        override fun areContentsTheSame(oldItem: Apartment, newItem: Apartment): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ApartmentsViewBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val apartment = getItem(position)

        holder.bind(apartment)

        holder.apartmentView.setOnClickListener {
            onItemClick(apartment)
        }
    }
}
