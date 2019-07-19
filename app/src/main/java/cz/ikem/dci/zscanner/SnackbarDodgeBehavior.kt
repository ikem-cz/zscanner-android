package cz.ikem.dci.zscanner

import android.os.Build
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.snackbar.Snackbar

class SnackbarDodgeBehavior : CoordinatorLayout.Behavior<View>() {

    private val mEnabled = Build.VERSION.SDK_INT >= 11;

    override fun layoutDependsOn(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        return mEnabled && (dependency is Snackbar.SnackbarLayout)
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        val translationY = Math.min(0f, dependency.translationY - dependency.height)
        child.translationY = translationY
        return true
    }
}