package com.lanhee.fortunewheel.screen.loaddialog

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.lanhee.fortunewheel.R
import com.lanhee.fortunewheel.data.SaveData
import com.lanhee.fortunewheel.databinding.DlgLoadBinding
import com.lanhee.fortunewheel.utils.Utils
import kotlinx.coroutines.Dispatchers

class LoadDialog() : DialogFragment() {
    private val binding by lazy { DlgLoadBinding.inflate(layoutInflater) }
    private lateinit var viewModel: LoadDialogViewModel

    private val recyclerView by lazy { binding.rcvLoad }
    var listener: OnLoadSelect? = null

    interface OnLoadSelect {
        fun onSelect(items: Array<String>)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this, LoadDialogViewModel.Factory())[LoadDialogViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.list.observe(viewLifecycleOwner) {
            (recyclerView.adapter as LoadAdapter).submitList(it.toMutableList())
            binding.tvEmpty.visibility = if(it.isEmpty()) View.VISIBLE else View.GONE
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.addItemDecoration(object : ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                super.getItemOffsets(outRect, view, parent, state)
                outRect.top = 10
            }
        })
        recyclerView.adapter = LoadAdapter()

        viewModel.loadData()

    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let {
            val width = (Utils.getScreenWidth(requireContext()) * 0.8f).toInt()
            val height = (Utils.getScreenHeight(requireContext()) * 0.6f).toInt()
            it.setLayout(width, height)
        }
    }

    inner class LoadAdapter: ListAdapter<SaveData, LoadHolder>(object : DiffUtil.ItemCallback<SaveData>() {
        override fun areItemsTheSame(oldItem: SaveData, newItem: SaveData): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SaveData, newItem: SaveData): Boolean {
            return oldItem.id == newItem.id
        }
    }) {

        override fun submitList(list: MutableList<SaveData>?) {
            super.submitList(list)
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoadHolder {
            val view = layoutInflater.inflate(R.layout.item_load, null)
            val holder = LoadHolder(view)
            holder.itemView.layoutParams = RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT)
            holder.itemView.setOnClickListener {
                listener?.onSelect(getItem(holder.layoutPosition).items)
                dismiss()
            }
            holder.itemView.findViewById<ImageButton>(R.id.btn_delete).setOnClickListener {
                val position = holder.layoutPosition
                viewModel.deleteData(getItem(position))
            }
            return holder
        }

        override fun onBindViewHolder(holder: LoadHolder, position: Int) {
            holder.bind(getItem(position))
        }
    }

    inner class LoadHolder(view: View): RecyclerView.ViewHolder(view) {
        fun bind(saveData: SaveData) {
            itemView.findViewById<TextView>(R.id.tv_title).text = saveData.title
            val chipGroup = itemView.findViewById<ChipGroup>(R.id.chg)
            chipGroup.removeAllViews()
            saveData.items.forEach {
                val chip = createChip(it)
                chipGroup.addView(chip)
            }
        }
        private fun createChip(text: String): Chip {
            val chip = Chip(context)
            chip.isClickable = false
            chip.text = text
            return chip
        }
    }
}