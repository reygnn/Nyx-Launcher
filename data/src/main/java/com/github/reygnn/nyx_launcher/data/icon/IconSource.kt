package com.github.reygnn.nyx_launcher.data.icon

import android.graphics.Bitmap
import com.github.reygnn.nyx_launcher.home.model.IconRef

/**
 * Resolves + rasterizes an icon to a [Bitmap] — the Android/system half of the
 * loader (LauncherApps + [IconRasterizer]). Split out from [IconLoaderImpl] so
 * the caching layer (memory LRU, coalescing, disk, evict) is testable with a
 * fake source, no device required. Called on an IO dispatcher.
 */
interface IconSource {
    suspend fun load(ref: IconRef, sizePx: Int): Bitmap
}
