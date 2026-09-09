package com.github.reygnn.nyx_launcher.home.transition

import com.github.reygnn.nyx_launcher.home.model.HomeItem
import com.github.reygnn.nyx_launcher.home.model.HomeLayout
import com.github.reygnn.nyx_launcher.home.model.ItemId
import com.github.reygnn.nyx_launcher.home.model.PlacedItem
import com.github.reygnn.nyx_launcher.home.model.ReconcileOutcome
import com.github.reygnn.nyx_launcher.home.model.ReconcileReport
import com.github.reygnn.nyx_launcher.home.model.ComponentKey

/**
 * Pure reconcile policy (RECONCILE_HOME_LAYOUT_SPEC §2). Android-free, total,
 * deterministic (ids via factory) — a JVM truth table. The fail-closed gate
 * lives in the use-case, so this only ever sees a genuine [installed] set.
 *
 * Order: prune dead → repair folders (dissolve at 1 / drop at 0) → trim the dock
 * to `columns` → trim trailing empty pages (keep >= 1). Idempotent (RHL-INV-2):
 * a second pass over a clean layout returns [ReconcileOutcome.Unchanged].
 *
 * NOT YET IMPLEMENTED: dedup by precedence (RHL-INV-4). Duplicates only arise
 * from backup/import (not built yet); normal add/remove keeps app-uniqueness via
 * the transitions. [ReconcileReport.dedupedApps] is therefore always 0 for now.
 */
object HomeLayoutReconciler {

    fun reconcile(
        layout: HomeLayout,
        installed: Set<ComponentKey>,
        newId: () -> ItemId,
    ): ReconcileOutcome {
        var prunedApps = 0
        var dissolvedFolders = 0
        var removedEmptyFolders = 0

        // A folder guaranteed >= 1 member: at exactly 1, dissolve to a plain app.
        fun repair(folder: HomeItem.Folder): HomeItem =
            if (folder.members.size == 1) {
                dissolvedFolders++
                HomeItem.App(newId(), folder.members.single())
            } else {
                folder
            }

        // --- dock: prune dead, repair folders ---
        val newDock = mutableListOf<HomeItem>()
        for (item in layout.dock) {
            when (item) {
                is HomeItem.App ->
                    if (item.key in installed) newDock += item else prunedApps++
                is HomeItem.Folder -> {
                    val kept = item.members.filter { it in installed }
                    prunedApps += item.members.size - kept.size
                    if (kept.isEmpty()) removedEmptyFolders++
                    else newDock += repair(item.copy(members = kept))
                }
            }
        }

        // --- dock capacity = columns (import safety) ---
        var dockTrimmed = 0
        val cappedDock = if (newDock.size > layout.grid.columns) {
            dockTrimmed = newDock.size - layout.grid.columns
            newDock.take(layout.grid.columns)
        } else {
            newDock
        }

        // --- grid: prune dead, repair folders (survivor keeps the folder's cell) ---
        val newItems = mutableListOf<PlacedItem>()
        for (placed in layout.items) {
            when (val item = placed.item) {
                is HomeItem.App ->
                    if (item.key in installed) newItems += placed else prunedApps++
                is HomeItem.Folder -> {
                    val kept = item.members.filter { it in installed }
                    prunedApps += item.members.size - kept.size
                    if (kept.isEmpty()) removedEmptyFolders++
                    else newItems += placed.copy(item = repair(item.copy(members = kept)))
                }
            }
        }

        // --- trim trailing empty pages (keep >= 1); interior blanks preserved ---
        val usedPages = newItems.maxOfOrNull { it.pos.page + 1 } ?: 0
        val newPages = minOf(layout.pages, maxOf(1, usedPages))
        val trimmedPages = layout.pages - newPages

        val changed = prunedApps > 0 || dissolvedFolders > 0 || removedEmptyFolders > 0 ||
            dockTrimmed > 0 || trimmedPages > 0
        if (!changed) return ReconcileOutcome.Unchanged

        return ReconcileOutcome.Changed(
            layout = layout.copy(pages = newPages, items = newItems, dock = cappedDock),
            report = ReconcileReport(
                prunedApps = prunedApps,
                dedupedApps = 0, // RHL-INV-4 deferred
                dissolvedFolders = dissolvedFolders,
                removedEmptyFolders = removedEmptyFolders,
                trimmedPages = trimmedPages,
                dockTrimmed = dockTrimmed,
            ),
        )
    }
}
