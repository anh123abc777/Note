package com.example.keep

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.StyleableRes
import com.google.android.material.checkbox.MaterialCheckBox

class TriStateMaterialCheckBox : MaterialCheckBox {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, com.google.android.material.R.attr.checkboxStyle)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        val sets = intArrayOf(R.attr.state)

        val typedArray = context.obtainStyledAttributes(attrs, sets)

        try {
            state = typedArray.getInt(ATTR_STATE, STATE_UNCHECKED)
        } finally {
            typedArray.recycle()
        }

        initComponent()
    }

    companion object {
        // @formatter:off
        @StyleableRes private const val ATTR_STATE = 0
        // @formatter:on

        const val STATE_UNCHECKED: Int = 0
        const val STATE_INDETERMINATE: Int = 1
        const val STATE_CHECKED: Int = 2

        private val UNCHECKED = intArrayOf(R.attr.state_unchecked)
        private val INDETERMINATE = intArrayOf(R.attr.state_indeterminate)
        private val CHECKED = intArrayOf(R.attr.state_checked)
    }

    private var isChangingState = false

    var state: Int
        @Throws(IllegalStateException::class)
        set(value) {
            if (isChangingState) return
            if (field == value) return
            isChangingState = true

            field = value
            isChecked = when (value) {
                STATE_UNCHECKED -> false
                STATE_INDETERMINATE -> true
                STATE_CHECKED -> true
                else -> throw IllegalStateException("$value is not a valid state for ${this.javaClass.name}")
            }
            refreshDrawableState()

            isChangingState = false

            onStateChanged?.let { it(this@TriStateMaterialCheckBox, value) }
        }

    var onStateChanged: ((TriStateMaterialCheckBox, Int) -> Unit)? = null

    private fun initComponent() {
        setButtonDrawable(R.drawable.tri_state_button)
        setOnCheckedChangeListener { _, _ ->
            state = when (state) {
                STATE_UNCHECKED -> STATE_CHECKED
                STATE_INDETERMINATE -> STATE_CHECKED
                STATE_CHECKED -> STATE_UNCHECKED
                else -> -1
            }
        }
    }

    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val drawableState = super.onCreateDrawableState(extraSpace + 1)

        mergeDrawableStates(
            drawableState, when (state) {
                STATE_UNCHECKED -> UNCHECKED
                STATE_INDETERMINATE -> INDETERMINATE
                STATE_CHECKED -> CHECKED
                else -> throw IllegalStateException("$state is not a valid state for ${this.javaClass.name}")
            }
        )

        return drawableState
    }
}