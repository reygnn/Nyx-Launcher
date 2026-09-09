package com.github.reygnn.nyx_launcher.home

import com.github.reygnn.nyx_launcher.home.model.ComponentKey
import com.github.reygnn.nyx_launcher.home.model.HomeItem
import com.github.reygnn.nyx_launcher.home.model.HomeLayout
import com.github.reygnn.nyx_launcher.home.model.IconRef

/**
 * One rendering slot: empty, or an icon carrying its item id, the icon ref, and
 * a [launch] key (the app to start on tap — `null` for a folder, whose tap opens
 * the folder later).
 */
sealed interface HomeCell {
    data object Empty : HomeCell
    data class Icon(val id: ItemIdRef, val ref: IconRef, val launch: ComponentKey?) : HomeCell
}

// Alias to avoid importing ItemId in every UI file signature.
typealias ItemIdRef = com.github.reygnn.nyx_launcher.home.model.ItemId

private fun HomeItem.toIconCell(): HomeCell.Icon? = when (this) {
    is HomeItem.App -> HomeCell.Icon(id, IconRef.System(key), launch = key)
    is HomeItem.Folder ->
        members.firstOrNull()?.let { HomeCell.Icon(id, IconRef.System(it), launch = null) }
}

/** Dense row-major cells for one page (empties for gaps); index = y*columns + x. */
fun HomeLayout.pageCells(page: Int): List<HomeCell> {
    val cols = grid.columns
    val byIndex = items.filter { it.pos.page == page }.associateBy { it.pos.y * cols + it.pos.x }
    return (0 until cols * grid.rows).map { index ->
        byIndex[index]?.item?.toIconCell() ?: HomeCell.Empty
    }
}

/** Flat icon list for the dock (no empties). */
fun HomeLayout.dockCells(): List<HomeCell.Icon> = dock.mapNotNull { it.toIconCell() }
