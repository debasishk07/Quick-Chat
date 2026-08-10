package com.quickchat.app

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

object NavTransitions {

    // A smooth custom Ease-Out Quart easing curve matching the sketchy-minimal calm feel
    val EaseOutQuart = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)

    // Standard durations
    const val PUSH_DURATION = 280
    const val MODAL_DURATION = 200
    const val TAB_DURATION = 150
    const val ACCESSIBILITY_DURATION = 100

    /**
     * Standard Forward Push (Right-to-Left slide-in, Left-to-Right pop-exit, with parallax)
     */
    fun pushEnter(isReducedMotion: Boolean): EnterTransition {
        if (isReducedMotion) return fadeIn(tween(ACCESSIBILITY_DURATION))
        return slideInHorizontally(
            animationSpec = tween(PUSH_DURATION, easing = EaseOutQuart),
            initialOffsetX = { it }
        ) + fadeIn(animationSpec = tween(PUSH_DURATION, easing = EaseOutQuart))
    }

    fun pushExit(isReducedMotion: Boolean): ExitTransition {
        if (isReducedMotion) return fadeOut(tween(ACCESSIBILITY_DURATION))
        return slideOutHorizontally(
            animationSpec = tween(PUSH_DURATION, easing = EaseOutQuart),
            targetOffsetX = { -it / 3 } // subtle parallax
        ) + fadeOut(animationSpec = tween(PUSH_DURATION, easing = EaseOutQuart))
    }

    fun pushPopEnter(isReducedMotion: Boolean): EnterTransition {
        if (isReducedMotion) return fadeIn(tween(ACCESSIBILITY_DURATION))
        return slideInHorizontally(
            animationSpec = tween(PUSH_DURATION, easing = EaseOutQuart),
            initialOffsetX = { -it / 3 } // subtle parallax
        ) + fadeIn(animationSpec = tween(PUSH_DURATION, easing = EaseOutQuart))
    }

    fun pushPopExit(isReducedMotion: Boolean): ExitTransition {
        if (isReducedMotion) return fadeOut(tween(ACCESSIBILITY_DURATION))
        return slideOutHorizontally(
            animationSpec = tween(PUSH_DURATION, easing = EaseOutQuart),
            targetOffsetX = { it }
        ) + fadeOut(animationSpec = tween(PUSH_DURATION, easing = EaseOutQuart))
    }

    /**
     * Modal Presentation (Scale-up + fade-in layer style)
     */
    fun modalEnter(isReducedMotion: Boolean): EnterTransition {
        if (isReducedMotion) return fadeIn(tween(ACCESSIBILITY_DURATION))
        return scaleIn(
            animationSpec = tween(MODAL_DURATION, easing = EaseOutQuart),
            initialScale = 0.93f
        ) + fadeIn(animationSpec = tween(MODAL_DURATION, easing = EaseOutQuart))
    }

    fun modalExit(isReducedMotion: Boolean): ExitTransition {
        if (isReducedMotion) return fadeOut(tween(ACCESSIBILITY_DURATION))
        return scaleOut(
            animationSpec = tween(MODAL_DURATION, easing = EaseOutQuart),
            targetScale = 0.93f
        ) + fadeOut(animationSpec = tween(MODAL_DURATION, easing = EaseOutQuart))
    }

    /**
     * Tab Cross-fade (Quick snap switch)
     */
    fun tabEnter(): EnterTransition {
        return fadeIn(animationSpec = tween(TAB_DURATION, easing = EaseOutQuart))
    }

    fun tabExit(): ExitTransition {
        return fadeOut(animationSpec = tween(TAB_DURATION, easing = EaseOutQuart))
    }
}
