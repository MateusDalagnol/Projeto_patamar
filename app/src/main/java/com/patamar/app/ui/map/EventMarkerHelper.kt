package com.patamar.app.ui.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.graphics.PathParser
import com.patamar.app.data.model.EventCategory

// Pin de mapa clássico: gota cinza-chumbo com furo redondo (o desenho é o do
// ícone "location_on", em coordenadas 24x24). Desenhado em Canvas e cacheado —
// todos os eventos dividem o mesmo visual, a categoria fica no detalhe.
object EventMarkerHelper {

    private const val PIN_PATH =
        "M12,2C8.13,2 5,5.13 5,9c0,5.25 7,13 7,13s7,-7.75 7,-13c0,-3.87 -3.13,-7 -7,-7z"

    private var cache: Bitmap? = null

    fun createMarkerDrawable(context: Context, @Suppress("UNUSED_PARAMETER") category: EventCategory): Drawable {
        val bitmap = cache ?: render(context).also { cache = it }
        return BitmapDrawable(context.resources, bitmap)
    }

    private fun render(context: Context): Bitmap {
        val density = context.resources.displayMetrics.density
        // O desenho ocupa x 5..19 e y 2..22 (14x20 unidades); a ponta fica no
        // centro da borda de baixo, que é onde o Marker ancora.
        val scale = 46f * density / 20f
        val widthPx = 14f * scale
        val heightPx = 20f * scale
        val bitmap = Bitmap.createBitmap(widthPx.toInt() + 1, heightPx.toInt() + 1, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val path = PathParser.createPathFromPathData(PIN_PATH)
        path.transform(Matrix().apply {
            postTranslate(-5f, -2f)
            postScale(scale, scale)
        })
        canvas.drawPath(path, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#2E2E33")
            style = Paint.Style.FILL
        })

        // furo redondo (centro em 12,9 e raio 2.5 no desenho original)
        canvas.drawCircle(
            (12f - 5f) * scale, (9f - 2f) * scale, 2.5f * scale,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        )
        return bitmap
    }
}
