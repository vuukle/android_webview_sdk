package com.vuukle.webview.helper

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.core.view.marginTop


object AnimationHelper {

    fun moveToY(view: View, mDuration: Long, finalYCoordinate: Float, onFinish: (() -> Unit)? = null) {

        ObjectAnimator.ofFloat(view, "translationY", finalYCoordinate).apply {
            duration = mDuration
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(p0: Animator?) {
                }

                override fun onAnimationEnd(p0: Animator?) {
                    view.clearAnimation()
                    onFinish?.invoke()
                }

                override fun onAnimationCancel(p0: Animator?) {
                    view.clearAnimation()
                    onFinish?.invoke()
                }

                override fun onAnimationRepeat(p0: Animator?) {
                }
            })
            start()
        }
    }

    fun changeMarginTop(view: View, mDuration: Long, finalMargin: Int, onFinish: (() -> Unit)? = null) {

        val lp = view.layoutParams as ViewGroup.MarginLayoutParams

        val va = ValueAnimator.ofInt(lp.topMargin, finalMargin)
        va.duration = mDuration

        va.addUpdateListener {
            val params = view.layoutParams as RelativeLayout.LayoutParams
            val value = va.animatedValue.toString().toInt()
            params.topMargin = value
            view.layoutParams = params
            if(value == finalMargin) view.clearAnimation()
        }
        va.start()
    }
}