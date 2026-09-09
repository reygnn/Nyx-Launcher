package com.github.reygnn.nyx_launcher.data.icon

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import javax.inject.Inject

/**
 * Turns a resolved [Drawable] into a square [Bitmap] of the requested size.
 *
 * `AdaptiveIconDrawable.draw()` composites its foreground + background layers
 * onto the canvas, so this one path handles adaptive AND legacy icons — no
 * special-casing. Robolectric covers it (ICON_LOADER_SPEC §9.2). Never recycles
 * (ICL-INV-8).
 */
class IconRasterizer @Inject constructor() {

    fun rasterize(drawable: Drawable, sizePx: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, sizePx, sizePx)
        drawable.draw(canvas)
        return bitmap
    }
}
