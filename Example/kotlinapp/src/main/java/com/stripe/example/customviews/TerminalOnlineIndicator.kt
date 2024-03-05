package com.stripe.example.customviews

import android.animation.Animator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import androidx.annotation.ColorInt
import com.stripe.example.R
import com.stripe.stripeterminal.external.models.NetworkStatus
import kotlin.math.min

/**
 * Circular indicator view used to tell when a terminal is online/offline.
 */
class TerminalOnlineIndicator @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
        defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes), ValueAnimator.AnimatorUpdateListener, Animator.AnimatorListener {
    private var pulseScale = ZERO
    private var pulseAlpha = MIN_ALPHA
    private var radius: Float = ZERO
    private val paint = Paint()

    @ColorInt
    var onlineColor: Int

    @ColorInt
    var offlineColor: Int

    @ColorInt
    var unknownColor: Int
    private val pulseStrokeWidth: Float
    private var isAnimating = false

    private val alphaPropertyHolder = PropertyValuesHolder.ofInt(PULSE_COLOR_ALPHA, MAX_ALPHA, MIN_ALPHA)
    private val scalePropertyHolder = PropertyValuesHolder.ofFloat(PULSE_RADIUS_SCALE, MIN_PULSE_SCALE, MAX_PULSE_SCALE)
    private val pulsingAnimator = ValueAnimator.ofPropertyValuesHolder(alphaPropertyHolder, scalePropertyHolder)
            .apply {
                duration = PULSE_ANIM_DURATION
                interpolator = AccelerateDecelerateInterpolator()
                repeatCount = Animation.INFINITE
                repeatMode = ValueAnimator.RESTART
            }

    var networkStatus: NetworkStatus = NetworkStatus.UNKNOWN
        set(value) {
            field = value
            // Set new content description for accessibility.
            contentDescription =
                    resources.getString(
                            when (value) {
                                NetworkStatus.UNKNOWN -> R.string.a11y_terminal_unknown
                                NetworkStatus.OFFLINE -> R.string.a11y_terminal_offline
                                NetworkStatus.ONLINE -> R.string.a11y_terminal_online
                            }
                    )
            postInvalidate() // so the view is redrawn on the next layout
            // Use post so animation start/cancel happens on the Ui thread.
            post {
                if (value == NetworkStatus.ONLINE && !pulsingAnimator.isStarted) {
                    pulsingAnimator.start() // start pulsing if we aren't already.
                } else if (value != NetworkStatus.ONLINE) {
                    pulsingAnimator.cancel() // Stop pulsing
                }
            }
        }

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.TerminalOnlineIndicator, defStyleAttr, defStyleRes).apply {
            try {
                // Get User defined styling
                onlineColor = getColor(R.styleable.TerminalOnlineIndicator_onlineColor, Color.GREEN)
                offlineColor = getColor(R.styleable.TerminalOnlineIndicator_offlineColor, Color.RED)
                unknownColor = getColor(R.styleable.TerminalOnlineIndicator_unknownColor, Color.GRAY)
                pulseStrokeWidth = getDimension(
                        R.styleable.TerminalOnlineIndicator_pulseWidth,
                        resources.getDimension(R.dimen.terminal_offline_indicator_pulse_width)
                )
            } finally {
                recycle()
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        paint.color = when (networkStatus) {
            NetworkStatus.UNKNOWN -> unknownColor
            NetworkStatus.OFFLINE -> offlineColor
            NetworkStatus.ONLINE -> onlineColor
        }
        paint.style = Paint.Style.FILL
        paint.alpha = MAX_ALPHA

        canvas.apply {
            translate(height / 2f, width / 2f)
            drawCircle(ZERO, ZERO, radius, paint)

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = pulseStrokeWidth
            paint.alpha = pulseAlpha
            drawCircle(ZERO, ZERO, radius * pulseScale, paint)
            save()
            restore()
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) {
            // Calculate the co-ordinates of our circle indicator
            val height = bottom - top
            val length = right - left
            radius = min(length, height) / MAX_PULSE_SCALE
        }
    }

    override fun onAnimationStart(animation: Animator) {
        isAnimating = true
        // Init the pulse values
        pulseScale = MIN_PULSE_SCALE
        pulseAlpha = MAX_ALPHA
    }

    override fun onAnimationEnd(animation: Animator) {
        isAnimating = false
        // reset the pulse values
        pulseScale = ZERO
        pulseAlpha = MIN_ALPHA
    }

    override fun onAnimationUpdate(animation: ValueAnimator) {
        pulseAlpha = animation.getAnimatedValue(PULSE_COLOR_ALPHA) as Int
        pulseScale = animation.getAnimatedValue(PULSE_RADIUS_SCALE) as Float
        postInvalidate() // so we can redraw the pulse
    }

    override fun onAnimationCancel(animation: Animator) {
        isAnimating = false
        // reset the pulse values
        pulseScale = ZERO
        pulseAlpha = MIN_ALPHA
    }

    override fun onAnimationRepeat(animation: Animator) = Unit

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        pulsingAnimator.run {
            pause()
            removeAllUpdateListeners()
            removeAllListeners()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        pulsingAnimator.run {
            addUpdateListener(this@TerminalOnlineIndicator)
            addListener(this@TerminalOnlineIndicator)
            if (isPaused) resume()
        }
    }

    companion object {
        private const val PULSE_RADIUS_SCALE = "PulseScale"
        private const val PULSE_COLOR_ALPHA = "PulseAlpha"
        private const val PULSE_ANIM_DURATION = 2_000L
        private const val MAX_ALPHA = 255
        private const val MIN_ALPHA = 0
        private const val MAX_PULSE_SCALE = 4f
        private const val MIN_PULSE_SCALE = 1f
        private const val ZERO = 0f
    }
}
