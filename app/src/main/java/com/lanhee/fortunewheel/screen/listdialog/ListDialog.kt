package com.lanhee.fortunewheel.screen.listdialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.chip.Chip
import com.lanhee.fortunewheel.databinding.DlgListBinding
import com.lanhee.fortunewheel.utils.Utils

class ListDialog : DialogFragment() {
    var defaultList: Array<String>? = null
    var listener: OnListApply? = null

    private val binding by lazy { DlgListBinding.inflate(layoutInflater) }
    private lateinit var viewModel: ListDialogViewModel

    interface OnListApply {
        fun onListApply(items: Array<String>)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this, ListDialogViewModel.Factory())[ListDialogViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.items.observe(this) {
            binding.chgList.removeAllViews()
            it.forEach { item ->
                binding.chgList.addView(createChip(item))
            }
            binding.btnApply.isEnabled = it.size > 1
        }

        viewModel.inputText.observe(this) {
            binding.btnListAdd.isEnabled = !it.isNullOrEmpty()
        }

        binding.btnListAdd.setOnClickListener {
            viewModel.addItem(binding.etListInput.text.toString())
            binding.etListInput.text = null
        }

        binding.btnApply.setOnClickListener {
            val result = viewModel.items.value!!
            listener?.onListApply(result)
            dismiss()
        }

        binding.etListInput.addTextChangedListener {
            viewModel.setInputText(it.toString())
        }

        defaultList?.let { defaultList ->
            viewModel.setItems(defaultList)
        }
    }

    private fun createChip(text: String): Chip {
        val chip = Chip(context)
        chip.isCloseIconVisible = true
        chip.setOnCloseIconClickListener {
            viewModel.deleteItem((it as Chip).text.toString())
        }
        chip.text = text
        return chip
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let {
            val width = (Utils.getScreenWidth(requireContext()) * 0.8f).toInt()
            val height = (Utils.getScreenHeight(requireContext()) * 0.4f).toInt()
            it.setLayout(width, height)
        }
    }

}