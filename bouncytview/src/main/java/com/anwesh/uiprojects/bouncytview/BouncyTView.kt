package com.anwesh.uiprojects.bouncytview

/**
 * Created by anweshmishra on 05/11/19.
 */

import android.view.View
import android.view.MotionEvent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import android.app.Activity
import android.content.Context

val nodes : Int = 5
val scGap : Float = 0.01f
val strokeFactor : Int = 90
val sizeFactor : Float = 2.3f
val hFactor : Float = 0.4f
val foreColor : Int = Color.parseColor("#4CAF50")
val backColor : Int = Color.parseColor("#BDBDBD")

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(Math.PI * this).toFloat()

fun Canvas.drawBouncyT(size : Float, h : Float, scale : Float, paint : Paint) {
    val y : Float = h * 0.4f
    val sf : Float = scale.sinify()
    drawLine(0f, 0f, 0f, -h / 2, paint)
    save()
    translate(0f, h / 2)
    drawLine(0f, 0f, 0f, -y * sf, paint)
    val sc1 : Float = sf.divideScale(0, 2)
    val sc2 : Float = sf.divideScale(1, 2)
    save()
    translate(0f, -y * sc2)
    drawLine(-size * sc1, 0f, size * sc1, 0f, paint)
    restore()
    restore()
}

fun Canvas.drawBTNode(i : Int, scale : Float, paint : Paint) {
    val w: Float = width.toFloat()
    val h: Float = height.toFloat()
    val gap: Float = w / (nodes + 1)
    val size: Float = gap / sizeFactor
    paint.color = foreColor
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    save()
    translate(gap * (i + 1), 0f)
    drawBouncyT(size, h, scale, paint)
    restore()
}

