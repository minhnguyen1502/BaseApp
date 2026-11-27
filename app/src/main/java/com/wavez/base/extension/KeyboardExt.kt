package com.wavez.base.extension

import android.app.Activity
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

fun hideKeyboardFrom(context: Context, view: View) {
    try {
        val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
}

fun showKeyboardFrom(context: Context, view: View?) {
    try {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
}

/**
 * Shrinks this view from the bottom by the keyboard height whenever it's shown, so its
 * bottom edge sticks to the top of the keyboard instead of getting covered by it.
 * Scoped to this view only (no side effects on siblings/ancestors like a toolbar).
 * Must be called on a view that still receives IME window insets (see BaseActivity).
 */
fun View.pushUpWithKeyboard() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        val imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
        val lp = v.layoutParams as? ViewGroup.MarginLayoutParams
        if (lp != null && lp.bottomMargin != imeBottom) {
            lp.bottomMargin = imeBottom
            v.layoutParams = lp
        }
        insets
    }
    ViewCompat.requestApplyInsets(this)
}
