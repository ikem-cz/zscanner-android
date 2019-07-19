package cz.ikem.dci.zscanner

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout

@CoordinatorLayout.DefaultBehavior(SnackbarDodgeBehavior::class)
class SnackbarDodgeLinearLayout : LinearLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr)
}