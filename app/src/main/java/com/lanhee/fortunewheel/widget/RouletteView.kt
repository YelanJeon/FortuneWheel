package com.lanhee.fortunewheel.widget

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.annotation.FloatRange
import com.lanhee.fortunewheel.R
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

class RouletteView : View {
    private val _colors =
        arrayOf(R.color.selector01, R.color.selector02, R.color.selector03)    //룰렛 구역에 들어갈 색상

    private lateinit var items: Array<String>   //룰렛에 들어갈 항목들
    private lateinit var paints: Array<Paint>   //원호 그릴 때 쓰이는 paint들
    private val textPaint by lazy {
        val p = Paint()
        p.textAlign = Paint.Align.CENTER
        p.textSize = 48f
        p.color = resources.getColor(R.color.primary, null)
        p.isAntiAlias = true
        p
    }
    private var rectF: RectF? = null    //원호를 그릴 때 쓰이는 RectF 객체를 미리 만들어두었다

    private lateinit var timer: Timer   //애니메이션 반복시킬 timer

    var listener: OnRouletteListener? = null    //룰렛 리스너

    var isRolling = false  //지금 돌아가는지 여부


    interface OnRouletteListener {
        fun onRouletteClick(position: Int)
        fun onRouletteStop(position: Int)
    }


    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    )

    fun getItems() : Array<String> {
        return items
    }

    fun setItem(text: String, index: Int) {
        items[index] = text
        applyItems()
    }

    fun setItems(items: Array<String>) {
        this.items = items
        applyItems()
    }

    private fun applyItems() {
        val firstColor = _colors[0]
        val lastColor = _colors[items.size%_colors.size]

        this.paints = Array(items.size) { index ->
            val paint = Paint()
            paint.isAntiAlias = true

            var cc = _colors[index%_colors.size]
            if(index == items.size-1 && index > _colors.size-1) {
                if(cc == firstColor) {
                    cc = _colors[1]
                }else if(cc==lastColor){
                    cc = _colors[0]
                }
            }
            paint.color = resources.getColor(cc, null)
            paint
        }
        invalidate()
    }

    fun startRolling() {
        rotation = 0f

        timer = kotlin.concurrent.timer("roll", false, 0L, 1L) {
            isRolling = true
            android.os.Handler(Looper.getMainLooper()).post {
                val animator = ObjectAnimator.ofFloat(this@RouletteView, View.ROTATION.name, rotation + 100)
                animator.duration = 1L
                animator.interpolator = LinearInterpolator()
                animator.start()
            }
        }
    }

    fun stopRolling() {
        timer.cancel()
        val animator = ObjectAnimator.ofFloat(this@RouletteView, View.ROTATION.name, rotation + ((2_000 .. 3_000).random()))
        animator.duration = 2500L
        animator.interpolator = DecelerateInterpolator(1.5f)
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(anim: Animator) { }

            override fun onAnimationEnd(anim: Animator) {
                isRolling = false
                val degree = rotation%360
                val sweep = 360 / paints.size
                val position = paints.size-1 - (degree/sweep).toInt();
                listener?.onRouletteStop(position)
            }

            override fun onAnimationCancel(anim: Animator) { }

            override fun onAnimationRepeat(p0: Animator) { }

        })
        animator.start()

    }

    fun reset() {
        rotation = 0f
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.let {
            val centerX by lazy { (rectF!!.left + rectF!!.right) / 2f}
            val centerY by lazy { (rectF!!.top + rectF!!.bottom) / 2f}
            val radius by lazy { (rectF!!.left + rectF!!.right) / 4f }
            if (rectF == null) {
                rectF = RectF(0f, 0f, width.toFloat(), height.toFloat())    //캔버스를 꽉 채우는 rectF 정의
            }
            repeat(items.size) { index ->
                val sweepAngle = 360f / paints.size    //한번에 회전시킬 각도 = 한바퀴/갯수
                val start = -90f + (index * sweepAngle)    //12시 각도에서 시작하기 위해 -90에 돌릴 각도를 추가하였다
                it.drawArc(rectF!!, start, sweepAngle, true, paints[index])    //캔버스에 원호를 그림

                // draw roulette text
                val medianAngle = (start + sweepAngle / 2f) * Math.PI / 180f
                val x = (centerX + (radius * cos(medianAngle))).toFloat()
                val y = (centerY + (radius * sin(medianAngle))).toFloat()

                var text = items[index]
                if(text.length > 5) {
                    text = text.substring(0, 5) + "..."
                }
                it.drawText(text, x, y, textPaint)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        listener?.let { listener ->
            event?.let { event ->
                if(event.action == MotionEvent.ACTION_DOWN) {
                    val x = event.x - rectF!!.centerX()
                    val y = event.y - rectF!!.centerY()
                    var touchedDegree = kotlin.math.atan2(x, y) * 180 / kotlin.math.PI
                    touchedDegree -= 180f
                    if(touchedDegree < 0) { touchedDegree += 360f }

                    val ranges = Array(items.size) { index ->
                        val to: Int = 360/items.size * (items.size-index)
                        val from: Int = to - 360/items.size
                        val range = IntRange(from, to)
                        range
                    }
                    for(i in ranges.indices) {
                        val range = ranges[i]
                        if(touchedDegree.toInt() in range) {
                            listener.onRouletteClick(i)
                        }
                    }
                }
            }
        }

        return true
    }

    private operator fun FloatRange.contains(num: Double) = num in from..to

}

