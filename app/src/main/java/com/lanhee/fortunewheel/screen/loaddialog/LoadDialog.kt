package com.lanhee.fortunewheel.screen.loaddialog

import android.app.Dialog
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.lanhee.fortunewheel.R
import com.lanhee.fortunewheel.data.SaveData
import com.lanhee.fortunewheel.databinding.DlgLoadBinding
import com.lanhee.fortunewheel.utils.AppDatabase
import com.lanhee.fortunewheel.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoadDialog(context: Context) : Dialog(context) {
    val binding by lazy { DlgLoadBinding.inflate(layoutInflater) }
    val recyclerView by lazy { binding.rcvLoad }
    var listener: OnLoadSelect? = null

    interface OnLoadSelect {
        fun onSelect(items: Array<String>)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        window?.let {
            it.attributes.width = (Utils.getScreenWidth(context) * 0.8f).toInt()
            it.attributes.height = (Utils.getScreenHeight(context) * 0.6f).toInt()
        }

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.addItemDecoration(object : ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                super.getItemOffsets(outRect, view, parent, state)
                outRect.top = 10
            }
        })

        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getInstance()
            db?.let { db ->
                val list = db.saveDao().getAll()
                recyclerView.adapter = LoadAdapter(list.toMutableList())
                checkEmpty()
            }
        }
    }

    private fun checkEmpty() {
        if(recyclerView.adapter!!.itemCount == 0) {
            binding.tvEmpty.visibility = View.VISIBLE
        }else{
            binding.tvEmpty.visibility = View.GONE
        }
    }

    inner class LoadAdapter(private val list: MutableList<SaveData>): RecyclerView.Adapter<LoadHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoadHolder {
            val view = layoutInflater.inflate(R.layout.item_load, null)
            view.layoutParams = RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT)
            val holder = LoadHolder(view)
            holder.itemView.setOnClickListener {
                listener?.onSelect(list[holder.layoutPosition].items)
                dismiss()
            }
            holder.itemView.findViewById<ImageButton>(R.id.btn_delete).setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    val db = AppDatabase.getInstance()
                    db?.let { db ->
                        val position = holder.layoutPosition
                        db.saveDao().delete(list[position])
                        list.removeAt(position)
                        CoroutineScope(Dispatchers.Main).launch {
                            notifyItemRemoved(position)
                            checkEmpty()
                        }
                    }
                }
            }
            return holder
        }

        override fun getItemCount(): Int {
            return list.size
        }
        override fun onBindViewHolder(holder: LoadHolder, position: Int) {
            holder.bind(list[position])
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