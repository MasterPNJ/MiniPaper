package com.example.minipaper

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

class ArcProgressBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var progress: Int = 0
        set(value) {
            field = value.coerceIn(0, max)
            invalidate()
        }
    var max: Int = 100

    private var arcColor: Int = Color.RED
    private val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = arcColor
        style = Paint.Style.STROKE
        strokeWidth = 20f
    }
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.LTGRAY
        style = Paint.Style.STROKE
        strokeWidth = 20f
    }

    /**
     * Permet de changer la couleur de l'arc.
     */
    fun setArcColor(color: Int) {
        arcColor = color
        arcPaint.color = arcColor
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Calculer le rayon et le centre de la vue
        val widthF = width.toFloat()
        val heightF = height.toFloat()
        val radius = min(widthF, heightF) / 2 - arcPaint.strokeWidth
        val cx = widthF / 2
        val cy = heightF / 2
        val oval = RectF(cx - radius, cy - radius, cx + radius, cy + radius)

        // Dessiner l'arc de fond (de 135° à 405°, soit 270°)
        canvas.drawArc(oval, 135f, 270f, false, backgroundPaint)
        // Dessiner l'arc correspondant à la progression
        val sweepAngle = 270f * (progress.toFloat() / max)
        canvas.drawArc(oval, 135f, sweepAngle, false, arcPaint)
    }
}
