package com.github.reygnn.nyx_launcher.home.drawer

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.reygnn.nyx_launcher.R
import com.github.reygnn.nyx_launcher.data.icon.IconLoader
import com.github.reygnn.nyx_launcher.home.model.LauncherApp
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * The app drawer: a scrollable list of all launchable apps. Tapping launches;
 * dragging one onto the home (→ PlaceItemUseCase) is a later step.
 */
@AndroidEntryPoint
class AppDrawerActivity : AppCompatActivity() {

    private val viewModel: AppDrawerViewModel by viewModels()

    @Inject
    lateinit var iconLoader: IconLoader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_drawer)

        val list = findViewById<RecyclerView>(R.id.app_list)
        val iconSizePx = (40 * resources.displayMetrics.density).toInt()
        val adapter = AppDrawerAdapter(iconLoader, lifecycleScope, iconSizePx, ::launchApp, ::addToHome)
        list.layoutManager = LinearLayoutManager(this)
        list.adapter = adapter

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.apps.collect(adapter::submit)
            }
        }
    }

    private fun addToHome(app: LauncherApp) {
        viewModel.addToHome(app.key)
        finish() // back to home, which re-renders with the placed app
    }

    private fun launchApp(app: LauncherApp) {
        val intent = Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_LAUNCHER)
            .setComponent(ComponentName(app.key.packageName, app.key.className))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { startActivity(intent) }
            .onFailure { if (it !is ActivityNotFoundException) throw it }
    }
}
