package com.udacity

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import java.util.concurrent.TimeUnit
import kotlin.math.min
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {


    private var widthSize = 0
    private var heightSize = 0
    private var loadingDefaultText = ""
    private var btnDefaultColor = 0
    private var btnLoadingColor = context.getColor(R.color.colorPrimaryDark)
    private var textColor = ContextCompat.getColor(context, R.color.white)
    private var loadingText: String = "We Are Loading"
    private lateinit var buttonTextBounds: Rect
    private var currentProgressCircleAnimationValue = 0f
    private var buttonText = ""
    private var currentButtonBackgroundAnimationValue = 0f
    private lateinit var buttonBackgroundAnimator: ValueAnimator
    private val progressCircleRect = RectF()
    private var progressCircleSize = 0f
    private var angle = 360f
    var oldAngle = 0

    private val buttonTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 55f
        typeface = Typeface.DEFAULT
    }

    private val circlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = context.getColor(R.color.colorAccent)
    }

    private var progressCircleAnimator = ValueAnimator.ofFloat(0f, 360f).apply {
        repeatMode = ValueAnimator.RESTART
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener {
            currentProgressCircleAnimationValue = it.animatedValue as Float
            invalidate()
        }
    }
    private val animatorSet: AnimatorSet = AnimatorSet().apply {
        duration = TimeUnit.SECONDS.toMillis(3)
        disableView(this@LoadingButton)
    }

    private fun AnimatorSet.disableView(loadingButton: LoadingButton) {
        loadingButton.isEnabled = !this.isStarted

    }

    private fun createButtonBackgroundAnimator() {
        ValueAnimator.ofFloat(0f, widthSize.toFloat()).apply {
            repeatMode = ValueAnimator.RESTART
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener {
                currentButtonBackgroundAnimationValue = it.animatedValue as Float
                invalidate()
            }
        }.also {
            buttonBackgroundAnimator = it
            animatorSet.playTogether(progressCircleAnimator, buttonBackgroundAnimator)
        }
    }


    private val buttonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, old, new ->
        when (new) {
            ButtonState.Loading -> {
                // LoadingButton is now Loading and we need to set the correct text
                buttonText = loadingText

                // We only calculate ButtonText bounds and ProgressCircle rect once,
                // Only when buttonText is first initialized with loadingText
                if (!::buttonTextBounds.isInitialized) {
                    retrieveButtonTextBounds()
                    computeCircle()
                }

                // ProgressCircle and Button background animations must start now
                animatorSet.start()
            }
            else -> {
                angle = 360f
                oldAngle = 0
                // LoadingButton is not doing any Loading so we need to reset to default text
                buttonText = loadingDefaultText

                // ProgressCircle animation must stop now
                new.takeIf { it == ButtonState.Completed }?.run { animatorSet.cancel() }
            }
        }
    }

    override fun performClick(): Boolean {
        super.performClick()
        if (buttonState == ButtonState.Completed) {
            buttonState = ButtonState.Clicked
            invalidate()
        }
        return true
    }

    init {
        isClickable = true
        buttonText = loadingDefaultText
        if (!::buttonTextBounds.isInitialized) {
            retrieveButtonTextBounds()
            computeCircle()
        }
        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {

            loadingDefaultText =
                getText(R.styleable.LoadingButton_loadingText).toString()
            btnDefaultColor =
                getColor(R.styleable.LoadingButton_loadingBackgroundColor, 0)
            buttonText = loadingDefaultText
        }

    }

    private fun retrieveButtonTextBounds() {
        buttonTextBounds = Rect()
        buttonTextPaint.getTextBounds(buttonText, 0, buttonText.length, buttonTextBounds)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let { buttonCanvas ->
            buttonCanvas.apply {
                drawBackgroundColor()
                drawButtonText()
                drawProgressCircleIfLoading()
            }
        }
    }


    private fun Canvas.drawProgressCircleIfLoading() {
        val bounds = Rect()
        var text_width = 0
        var text_height = 0
        buttonTextPaint.getTextBounds(buttonText, 0, buttonText.length, bounds)
        text_width = bounds.width()
        text_height = bounds.height()
        if (buttonState == ButtonState.Loading) {
            val rectf = RectF(
                (width / 2f) + (text_width / 2f) + 16,
                (height / 2f) - (text_height / 2f),
                (width / 2f) + (text_width / 2f) + 16 + 50f,
                (height / 2f) - (text_height / 2f) + 50f
            )

            drawArc(rectf, 0f, angle, true, circlePaint)


        }
    }

    private fun computeCircle() {
        val horizontalCenter =
            (buttonTextBounds.right + buttonTextBounds.width() + 12f)
        val verticalCenter = (heightSize / 2f)

        progressCircleRect.set(
            horizontalCenter - progressCircleSize,
            verticalCenter - progressCircleSize,
            horizontalCenter + progressCircleSize,
            verticalCenter + progressCircleSize
        )
    }


    private fun Canvas.drawButtonText() {
        buttonTextPaint.color = textColor
        drawText(
            buttonText,
            (widthSize / 2f),
            (heightSize / 2f) + 20f,
            buttonTextPaint
        )
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        progressCircleSize = (min(w, h) / 2f) * 0.2f
        createButtonBackgroundAnimator()
    }


    fun changeState(state: ButtonState) {
        if (state != buttonState) {
            buttonState = state
            invalidate()
        }
    }

    private fun Canvas.drawBackgroundColor() {

        when (buttonState) {
            ButtonState.Loading -> {
                drawLoadingBackgroundColor()
                drawDefaultBackgroundColor()
                animateCircle(100)
            }
            else -> drawColor(btnDefaultColor)
        }
    }

    private fun Canvas.drawLoadingBackgroundColor() = buttonPaint.apply {
        color = btnLoadingColor
    }.run {
        drawRect(
            0f,
            0f,
            currentButtonBackgroundAnimationValue,
            heightSize.toFloat(),
            buttonPaint
        )
    }

    private fun Canvas.drawDefaultBackgroundColor() = buttonPaint.apply {
        color = btnDefaultColor
    }.run {
        drawRect(
            currentButtonBackgroundAnimationValue,
            0f,
            widthSize.toFloat(),
            heightSize.toFloat(),
            buttonPaint
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

    private fun animateCircle(progress: Int) {
        val newAngle = (360 * (progress / 100f)).toInt()
        if (newAngle != oldAngle) {
            progressCircleAnimator = ValueAnimator.ofInt(oldAngle, newAngle)
            progressCircleAnimator.duration = 500
            progressCircleAnimator.interpolator = LinearInterpolator()
            progressCircleAnimator.addUpdateListener {
                angle = (it.animatedValue as Int).toFloat()
                invalidate()
            }
            progressCircleAnimator.start()
            oldAngle = newAngle
        }
    }

}