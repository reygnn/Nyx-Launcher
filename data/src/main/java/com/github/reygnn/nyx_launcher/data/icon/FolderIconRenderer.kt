package com.github.reygnn.nyx_launcher.data.icon

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.github.reygnn.nyx_launcher.home.model.ComponentKey
import com.github.reygnn.nyx_launcher.home.model.IconRef
import javax.inject.Inject

/**
 * Renders a folder's derived 2×2 preview from up to four member icons on a faint
 * rounded background (IHM-INV-2: never stored as an IconRef). Member bitmaps come
 * from the shared [IconLoader] cache, so this composite is cheap; changing the
 * membership changes the members list and thus the preview automatically.
 */
class FolderIconRenderer @Inject constructor(
    private val iconLoader: IconLoader,
) {
    suspend fun render(members: List<ComponentKey>, sizePx: Int): Bitmap {
        val out = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)

        val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x33FFFFFF } // ~20% white
        val radius = sizePx * 0.18f
        canvas.drawRoundRect(RectF(0f, 0f, sizePx.toFloat(), sizePx.toFloat()), radius, radius, bg)

        val pad = (sizePx * 0.10f).toInt()
        val cell = (sizePx - pad * 3) / 2 // 2 cells + 3 paddings span the size
        members.take(4).forEachIndexed { index, key ->
            val bitmap = runCatching { iconLoader.bitmap(IconRef.System(key), cell) }.getOrNull()
                ?: return@forEachIndexed
            val col = index % 2
            val row = index / 2
            val left = (pad + col * (cell + pad)).toFloat()
            val top = (pad + row * (cell + pad)).toFloat()
            canvas.drawBitmap(bitmap, left, top, null)
        }
        return out
    }
}
