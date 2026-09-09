package com.github.reygnn.nyx_launcher.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.github.reygnn.nyx_launcher.R
import com.github.reygnn.nyx_launcher.data.icon.IconLoader
import com.github.reygnn.nyx_launcher.home.model.ComponentKey
import com.github.reygnn.nyx_launcher.home.model.ItemId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Renders one page of [HomeCell]s. Tap an app launches it; long-press an icon
 * begins a drag (id as local state); empty cells stay non-long-clickable so the
 * gesture falls through to the page's long-press (opens the drawer). Async icons
 * are gated by a per-holder token (ICL-INV-9).
 */
class HomeGridAdapter(
    private val iconLoader: IconLoader,
    private val scope: CoroutineScope,
    private val iconSizePx: Int,
    private val onLaunch: (ComponentKey) -> Unit,
    private val onOpenFolder: (id: ItemId) -> Unit,
    private val onIconLongPress: (view: View, id: ItemId) -> Unit,
) : RecyclerView.Adapter<HomeGridAdapter.CellHolder>() {

    private var cells: List<HomeCell> = emptyList()

    fun submit(newCells: List<HomeCell>) {
        cells = newCells
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CellHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_home_cell, parent, false)
        return CellHolder(view)
    }

    override fun getItemCount(): Int = cells.size

    override fun onBindViewHolder(holder: CellHolder, position: Int) {
        val cell = cells[position]
        val token = ++holder.bindToken
        holder.icon.setImageDrawable(null)

        when (cell) {
            HomeCell.Empty -> {
                holder.itemView.setOnClickListener(null)
                holder.itemView.setOnLongClickListener(null)
                holder.itemView.isLongClickable = false
                holder.itemView.isClickable = false
            }
            is HomeCell.Icon -> {
                holder.itemView.setOnClickListener {
                    val launch = cell.launch
                    if (launch != null) onLaunch(launch) else onOpenFolder(cell.id)
                }
                holder.itemView.setOnLongClickListener {
                    onIconLongPress(holder.itemView, cell.id)
                    true
                }
                scope.launch {
                    val bitmap = runCatching { iconLoader.bitmap(cell.ref, iconSizePx) }.getOrNull()
                        ?: return@launch
                    if (holder.bindToken == token) holder.icon.setImageBitmap(bitmap)
                }
            }
        }
    }

    override fun onViewRecycled(holder: CellHolder) {
        holder.bindToken++
        holder.icon.setImageDrawable(null)
    }

    class CellHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.cell_icon)
        var bindToken: Int = 0
    }
}
