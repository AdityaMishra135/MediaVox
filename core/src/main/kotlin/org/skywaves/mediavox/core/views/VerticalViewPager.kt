package org.skywaves.mediavox.core.views

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.viewpager.widget.ViewPager

class VerticalViewPager(context: Context, attrs: AttributeSet? = null) : ViewPager(context, attrs) {
    init {
        // The majority of the magic happens here
        setPageTransformer(true, VerticalPageTransformer())
        // The easiest way to get rid of the overscroll drawing that happens on the left and right
        overScrollMode = View.OVER_SCROLL_NEVER
    }
    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean =
            // return touch coordinates to original reference frame for any child views
            super.onInterceptTouchEvent(swapXY(ev)).apply { swapXY(ev) }

    override fun onTouchEvent(ev: MotionEvent): Boolean = super.onTouchEvent(swapXY(ev))
}

class VerticalPageTransformer : ViewPager.PageTransformer {
    override fun transformPage(view: View, position: Float) {
        view.translationX = view.height * -position

        transform(view, position)
    }
}

private fun transform(view: View, position: Float) {
    when {
        position < -1 -> view.apply {
            // [-Infinity,-1)
            // This page is way off-screen to the left.
            alpha = 0f
        }
        position <= 1 -> view.apply {
            // [-1,1]
            alpha = 1f
            // Counteract the default slide transition
            translationX = width * -position
            //set Y position to swipe in from top
            val yPosition = position * height
            translationY = yPosition
        }
        else -> view.apply {
            // (1,+Infinity]
            // This page is way off-screen to the right.
            alpha = 0f
        }
    }
}

private fun VerticalViewPager.swapXY(ev: MotionEvent): MotionEvent = ev.apply {
    val width = width.toFloat()
    val height = height.toFloat()
    val newX = this.y / height * width
    val newY = this.x / width * height
    this.setLocation(newX, newY)
}