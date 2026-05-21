package com.example.varushopretailer.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.varushopretailer.databinding.ItemLanguageBinding
import com.example.varushopretailer.modal.Language

class LanguageAdapter(
    private val languages: List<Language>, private val onLanguageSelected: (Language) -> Unit
) : RecyclerView.Adapter<LanguageAdapter.ViewHolder>() {

    private var displayList = languages.toMutableList()
    private var selectedLanguage: Language? = languages.find { it.isSelected }

    inner class ViewHolder(val binding: ItemLanguageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLanguageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val lang = displayList[position]

        with(holder.binding) {
            tvLanguageName.text = "${lang.name} (${lang.nativeName})"
            tvFlag.text = lang.flagEmoji

            val isCurrentlySelected = lang.code == selectedLanguage?.code
            cardLanguage.isChecked = isCurrentlySelected
            radioSelect.isChecked = isCurrentlySelected

            root.setOnClickListener {
                handleSelection(lang)
            }

            radioSelect.isClickable = false
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun handleSelection(newlySelected: Language) {
        if (newlySelected.code == selectedLanguage?.code) return

        selectedLanguage?.isSelected = false

        newlySelected.isSelected = true
        selectedLanguage = newlySelected

        notifyDataSetChanged()
        onLanguageSelected(newlySelected)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun filter(query: String) {
        val lowercaseQuery = query.lowercase().trim()
        displayList = if (lowercaseQuery.isEmpty()) {
            languages.toMutableList()
        } else {
            languages.filter {
                it.name.lowercase().contains(lowercaseQuery) || it.nativeName.lowercase()
                    .contains(lowercaseQuery)
            }.toMutableList()
        }
        notifyDataSetChanged()
    }

    override fun getItemCount() = displayList.size
}