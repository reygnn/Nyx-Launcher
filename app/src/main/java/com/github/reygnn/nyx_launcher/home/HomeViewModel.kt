package com.github.reygnn.nyx_launcher.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.reygnn.nyx_launcher.home.model.ComponentKey
import com.github.reygnn.nyx_launcher.home.model.DropTarget
import com.github.reygnn.nyx_launcher.home.model.HomeLayout
import com.github.reygnn.nyx_launcher.home.model.ItemId
import com.github.reygnn.nyx_launcher.home.usecase.MoveItemUseCase
import com.github.reygnn.nyx_launcher.home.usecase.ObserveHomeLayoutUseCase
import com.github.reygnn.nyx_launcher.home.usecase.RemoveFromFolderUseCase
import com.github.reygnn.nyx_launcher.home.usecase.RemoveItemUseCase
import com.github.reygnn.nyx_launcher.home.usecase.RenameFolderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Home-screen state holder. [layout] is a lifecycle-aware [StateFlow] (`null` =
 * not loaded). Mutations run through the pure transitions + save; the layout flow
 * re-emits on success, so the UI re-renders itself.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    observeHomeLayout: ObserveHomeLayoutUseCase,
    private val moveItem: MoveItemUseCase,
    private val removeFromFolder: RemoveFromFolderUseCase,
    private val removeItem: RemoveItemUseCase,
    private val renameFolderUseCase: RenameFolderUseCase,
) : ViewModel() {

    val layout: StateFlow<HomeLayout?> = observeHomeLayout()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )

    fun move(moving: ItemId, target: DropTarget) {
        viewModelScope.launch { moveItem(moving, target) }
    }

    fun extractFromFolder(folder: ItemId, member: ComponentKey, target: DropTarget) {
        viewModelScope.launch { removeFromFolder(folder, member, target) }
    }

    fun remove(id: ItemId) {
        viewModelScope.launch { removeItem(id) }
    }

    fun renameFolder(folder: ItemId, title: String) {
        viewModelScope.launch { renameFolderUseCase(folder, title) }
    }
}
