package com.github.reygnn.nyx_launcher.home

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.DragEvent
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.github.reygnn.nyx_launcher.R
import com.github.reygnn.nyx_launcher.data.icon.IconLoader
import com.github.reygnn.nyx_launcher.home.drawer.AppDrawerActivity
import com.github.reygnn.nyx_launcher.home.model.CellPos
import com.github.reygnn.nyx_launcher.home.model.ComponentKey
import com.github.reygnn.nyx_launcher.home.model.DropTarget
import com.github.reygnn.nyx_launcher.home.model.firstFreeCell
import com.github.reygnn.nyx_launcher.home.model.HomeItem
import com.github.reygnn.nyx_launcher.home.model.HomeLayout
import com.github.reygnn.nyx_launcher.home.model.ItemId
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * The launcher home: a [ViewPager2] of grid pages + a persistent dock. Tap an
 * app launches; tap a folder opens a bottom sheet; long-press an icon drags;
 * dropping runs the tested transitions via [HomeViewModel]. Cross-page drag is a
 * later step.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: HomeViewModel by viewModels()

    @Inject
    lateinit var iconLoader: IconLoader

    private lateinit var pager: ViewPager2
    private lateinit var dock: RecyclerView
    private var pagerAdapter: HomePagerAdapter? = null
    private lateinit var dockAdapter: DockAdapter

    private var gridIconPx = 0
    private var dockSize = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pager = findViewById(R.id.home_pager)
        dock = findViewById(R.id.dock)
        gridIconPx = (48 * resources.displayMetrics.density).toInt()

        dockAdapter = DockAdapter(iconLoader, lifecycleScope, gridIconPx, ::launchApp, ::openFolder, ::startDrag)
        dock.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        dock.adapter = dockAdapter
        dock.setOnDragListener { _, event -> handleDockDrag(event) }

        // Remove bar: visible only during a drag; drop an icon here to remove it.
        val removeBar = findViewById<TextView>(R.id.remove_bar)
        findViewById<View>(R.id.home_root).setOnDragListener { _, event ->
            when (event.action) {
                DragEvent.ACTION_DRAG_STARTED -> { removeBar.isVisible = true; true }
                DragEvent.ACTION_DRAG_ENDED -> { removeBar.isVisible = false; true }
                else -> true
            }
        }
        removeBar.setOnDragListener { _, event ->
            if (event.action == DragEvent.ACTION_DROP) {
                (event.localState as? ItemId)?.let(viewModel::remove)
                true
            } else {
                true
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.layout.collect { layout ->
                    layout ?: return@collect
                    dockSize = layout.dock.size

                    if (pagerAdapter == null) {
                        pagerAdapter = HomePagerAdapter(
                            iconLoader = iconLoader,
                            scope = lifecycleScope,
                            iconSizePx = gridIconPx,
                            columns = layout.grid.columns,
                            onLaunch = ::launchApp,
                            onOpenFolder = ::openFolder,
                            onStartDrag = ::startDrag,
                            onOpenDrawer = ::openDrawer,
                            onDropOnPage = ::dropOnPage,
                        ).also { pager.adapter = it }
                    }
                    val currentPage = pager.currentItem
                    pagerAdapter?.submit((0 until layout.pages).map(layout::pageCells))
                    if (currentPage < layout.pages) pager.setCurrentItem(currentPage, false)
                    dockAdapter.submit(layout.dockCells())
                }
            }
        }
    }

    private fun startDrag(view: View, id: ItemId) {
        view.startDragAndDrop(null, View.DragShadowBuilder(view), id, 0)
    }

    private fun dropOnPage(page: Int, cellIndex: Int, id: ItemId) {
        val columns = currentColumns()
        viewModel.move(id, DropTarget.Cell(CellPos(page, cellIndex % columns, cellIndex / columns)))
    }

    private fun handleDockDrag(event: DragEvent): Boolean = when (event.action) {
        DragEvent.ACTION_DROP -> {
            val id = event.localState as? ItemId
            val child = dock.findChildViewUnder(event.x, event.y)
            val slot = child?.let(dock::getChildAdapterPosition)?.takeIf { it != RecyclerView.NO_POSITION }
                ?: dockSize
            if (id != null) viewModel.move(id, DropTarget.DockSlot(slot))
            true
        }
        else -> true
    }

    private fun openFolder(folderId: ItemId) {
        val layout = viewModel.layout.value ?: return
        val folder = layout.allHomeItems().firstOrNull { it.id == folderId } as? HomeItem.Folder ?: return

        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.folder_sheet, null)
        val titleField = view.findViewById<EditText>(R.id.folder_title)
        titleField.setText(folder.title)
        dialog.setOnDismissListener {
            val newTitle = titleField.text.toString()
            if (newTitle != folder.title) viewModel.renameFolder(folderId, newTitle)
        }
        val members = view.findViewById<RecyclerView>(R.id.folder_members)
        members.layoutManager = GridLayoutManager(this, currentColumns())
        members.adapter = FolderMemberAdapter(
            iconLoader = iconLoader,
            scope = lifecycleScope,
            iconSizePx = gridIconPx,
            onLaunch = { key -> launchApp(key); dialog.dismiss() },
            onExtract = { key ->
                viewModel.extractFromFolder(folderId, key, DropTarget.Cell(layout.firstFreeCell()))
                dialog.dismiss()
            },
        ).also { it.submit(folder.members) }
        dialog.setContentView(view)
        dialog.show()
    }


    private fun currentColumns(): Int = viewModel.layout.value?.grid?.columns ?: 1

    private fun openDrawer() {
        startActivity(Intent(this, AppDrawerActivity::class.java))
    }

    private fun launchApp(key: ComponentKey) {
        val intent = Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_LAUNCHER)
            .setComponent(ComponentName(key.packageName, key.className))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { startActivity(intent) }
            .onFailure { if (it !is ActivityNotFoundException) throw it }
    }
}

/** Every top-level item across the grid and the dock. */
private fun HomeLayout.allHomeItems(): List<HomeItem> = items.map { it.item } + dock
