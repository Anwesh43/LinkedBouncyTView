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
val delay : Long = 20

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(Math.PI * this).toFloat()
fun Float.cosify() : Float = 1 - Math.sin(Math.PI / 2 + (Math.PI / 2) * this).toFloat()

fun Canvas.drawBouncyT(size : Float, h : Float, scale : Float, paint : Paint) {
    val y : Float = h * hFactor
    val sf : Float = scale.sinify()
    drawLine(0f, h, 0f, h / 2, paint)
    save()
    translate(0f, h / 2)
    drawLine(0f, 0f, 0f, -y * sf, paint)
    val sc1 : Float = scale.divideScale(0, 2)
    val sc2 : Float = scale.divideScale(1, 2).cosify()
    save()
    translate(0f, -y * (1 - sc2))
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

class BouncyTView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scGap * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class BTNode(var i : Int, val state : State = State()) {

        private var next : BTNode? = null
        private var prev : BTNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = BTNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawBTNode(i, state.scale, paint)
            next?.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : BTNode {
            var curr : BTNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class BouncyT(var i : Int) {

        private val root : BTNode = BTNode(0)
        private var curr : BTNode = root
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            root.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : BouncyTView) {

        private val bouncyT : BouncyT = BouncyT(0)
        private val animator : Animator = Animator(view)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(backColor)
            bouncyT.draw(canvas, paint)
            animator.animate {
                bouncyT.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            bouncyT.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : BouncyTView {
            val view : BouncyTView = BouncyTView(activity)
            activity.setContentView(view)
            return view
        }
    }
}