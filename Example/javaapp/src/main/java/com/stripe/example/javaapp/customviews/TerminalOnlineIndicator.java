package com.stripe.example.javaapp.customviews;

import android.animation.Animator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.stripe.example.javaapp.R;
import com.stripe.stripeterminal.external.models.NetworkStatus;

/**
 * Circular indicator view used to tell when a terminal is online/offline.
 */
public class TerminalOnlineIndicator extends View implements ValueAnimator.AnimatorUpdateListener, Animator.AnimatorListener {
    private static final String PULSE_RADIUS_SCALE = "PulseScale";
    private static final String PULSE_COLOR_ALPHA = "PulseAlpha";
    private static final long PULSE_ANIM_DURATION = 2_000L;
    private static final int MAX_ALPHA = 255;
    private static final int MIN_ALPHA = 0;
    private static final float MAX_PULSE_SCALE = 4f;
    private static final float MIN_PULSE_SCALE = 1f;
    private static final float ZERO = 0f;

    private float pulseScale = ZERO;
    private int pulseAlpha = MIN_ALPHA;
    private float radius = ZERO;
    private Paint paint = new Paint();

    @ColorInt int onlineColor;
    @ColorInt int offlineColor;
    @ColorInt int unknownColor;

    private float pulseStrokeWidth;
    private boolean isAnimating = false;

    private final PropertyValuesHolder alphaPropertyHolder = PropertyValuesHolder.ofInt(PULSE_COLOR_ALPHA, MAX_ALPHA, MIN_ALPHA);
    private final PropertyValuesHolder scalePropertyHolder = PropertyValuesHolder.ofFloat(PULSE_RADIUS_SCALE, MIN_PULSE_SCALE, MAX_PULSE_SCALE);
    private final ValueAnimator pulsingAnimator = ValueAnimator.ofPropertyValuesHolder(alphaPropertyHolder, scalePropertyHolder);

    NetworkStatus networkStatus = NetworkStatus.UNKNOWN;

    public TerminalOnlineIndicator(Context context, @Nullable AttributeSet attrs) {
        super(context,attrs);
        init(context, attrs, 0, 0);
    }

    public TerminalOnlineIndicator(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray styledAttrs = null;
        try { // Get User defined styling
            styledAttrs = context.getTheme().obtainStyledAttributes(attrs, R.styleable.TerminalOnlineIndicator, defStyleAttr, defStyleRes);
            onlineColor = styledAttrs.getColor(R.styleable.TerminalOnlineIndicator_onlineColor, Color.GREEN);
            offlineColor = styledAttrs.getColor(R.styleable.TerminalOnlineIndicator_offlineColor, Color.RED);
            unknownColor = styledAttrs.getColor(R.styleable.TerminalOnlineIndicator_unknownColor, Color.GRAY);
            pulseStrokeWidth = styledAttrs.getDimension(
                    R.styleable.TerminalOnlineIndicator_pulseWidth,
                    styledAttrs.getResources().getDimension(R.dimen.terminal_offline_indicator_pulse_width)
            );
        } finally {
            if (styledAttrs != null) {
                styledAttrs.recycle();
            }
        }

        pulsingAnimator.setDuration(PULSE_ANIM_DURATION);
        pulsingAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        pulsingAnimator.setRepeatCount(Animation.INFINITE);
        pulsingAnimator.setRepeatMode(ValueAnimator.RESTART);
    }

    public void setNetworkStatus(NetworkStatus networkStatus) {
        this.networkStatus = networkStatus;
        // Set new content description for accessibility.
        @StringRes int resource;
        switch(networkStatus) {
            case UNKNOWN:
                resource = R.string.a11y_terminal_unknown;
                break;
            case OFFLINE:
                resource = R.string.a11y_terminal_offline;
                break;
            case ONLINE:
            default:
                resource = R.string.a11y_terminal_online;
                break;
        }
        setContentDescription(getResources().getString(resource));
        postInvalidate(); // so the view is redrawn on the next layout
        // Use post so animation start/cancel happens on the Ui thread.
        this.post(() -> {
            if (networkStatus == NetworkStatus.ONLINE && !pulsingAnimator.isStarted()) {
                pulsingAnimator.start(); // start pulsing if we aren't already.
            } else if (networkStatus != NetworkStatus.ONLINE) {
                pulsingAnimator.cancel(); // Stop pulsing
            }
        });
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            // Calculate the co-ordinates of our circle indicator
            int height = bottom - top;
            int length = right - left;
            radius = Math.min(length, height) / MAX_PULSE_SCALE;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        @ColorInt int colorInt;
        switch (networkStatus) {
            case UNKNOWN:
                colorInt = unknownColor;
                break;
            case OFFLINE:
                colorInt = offlineColor;
                break;
            case ONLINE:
            default:
                colorInt = onlineColor;
                break;
        }
        paint.setColor(colorInt);
        paint.setStyle(Paint.Style.FILL);
        paint.setAlpha(MAX_ALPHA);
        canvas.translate(getHeight() / 2f, getWidth() / 2f);
        canvas.drawCircle(ZERO, ZERO, radius, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(pulseStrokeWidth);
        paint.setAlpha(pulseAlpha);
        canvas.drawCircle(ZERO, ZERO, radius * pulseScale, paint);
        canvas.save();
        canvas.restore();
    }

    @Override
    public void onAnimationStart(@NonNull Animator animator) {
        isAnimating = true;
        // Init the pulse values
        pulseScale = MIN_PULSE_SCALE;
        pulseAlpha = MAX_ALPHA;
    }

    @Override
    public void onAnimationEnd(@NonNull Animator animator) {
        isAnimating = false;
        // reset the pulse values
        pulseScale = ZERO;
        pulseAlpha = MIN_ALPHA;
    }

    @Override
    public void onAnimationUpdate(@NonNull ValueAnimator valueAnimator) {
        pulseAlpha = (int) valueAnimator.getAnimatedValue(PULSE_COLOR_ALPHA);
        pulseScale = (float) valueAnimator.getAnimatedValue(PULSE_RADIUS_SCALE);
        postInvalidate(); // so we can redraw the pulse
    }

    @Override
    public void onAnimationCancel(@NonNull Animator animator) {
        isAnimating = false;
        // reset the pulse values
        pulseScale = ZERO;
        pulseAlpha = MIN_ALPHA;
    }

    @Override
    public void onAnimationRepeat(@NonNull Animator animator) {

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        pulsingAnimator.pause();
        pulsingAnimator.removeAllUpdateListeners();
        pulsingAnimator.removeAllListeners();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        pulsingAnimator.addUpdateListener(this);
        pulsingAnimator.addListener(this);
        if (pulsingAnimator.isPaused()) {
            pulsingAnimator.resume();
        }
    }
}
