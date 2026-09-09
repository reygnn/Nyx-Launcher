package com.github.reygnn.nyx_launcher.home.drawer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.reygnn.nyx_launcher.home.model.ComponentKey
import com.github.reygnn.nyx_launcher.home.model.DropTarget
import com.github.reygnn.nyx_launcher.home.model.LauncherApp
import com.github.reygnn.nyx_launcher.home.model.firstFreeCell
import com.github.reygnn.nyx_launcher.home.usecase.GetDrawerAppsUseCase
import com.github.reygnn.nyx_launcher.home.usecase.ObserveHomeLayoutUseCase
import com.github.reygnn.nyx_launcher.home.usecase.PlaceItemUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Loads the sorted drawer list and places apps onto the home (first free cell). */
@HiltViewModel
class AppDrawerViewModel @Inject constructor(
    private val getDrawerApps: GetDrawerAppsUseCase,
    private val observeHomeLayout: ObserveHomeLayoutUseCase,
    private val placeItem: PlaceItemUseCase,
) : ViewModel() {

    private val _apps = MutableStateFlow<List<LauncherApp>>(emptyList())
    val apps: StateFlow<List<LauncherApp>> = _apps.asStateFlow()

    init {
        viewModelScope.launch { _apps.value = getDrawerApps() }
    }

    fun addToHome(app: ComponentKey) {
        viewModelScope.launch {
            val layout = observeHomeLayout().first()
            placeItem(app, DropTarget.Cell(layout.firstFreeCell()))
        }
    }
}
