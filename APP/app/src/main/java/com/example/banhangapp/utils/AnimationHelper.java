package com.example.banhangapp.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import androidx.recyclerview.widget.RecyclerView;

public class AnimationHelper {
    
    /**
     * Fade in animation for views
     */
    public static void fadeIn(View view, long duration) {
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate()
            .alpha(1f)
            .setDuration(duration)
            .setInterpolator(new DecelerateInterpolator())
            .setListener(null);
    }
    
    /**
     * Fade out animation for views
     */
    public static void fadeOut(View view, long duration) {
        view.animate()
            .alpha(0f)
            .setDuration(duration)
            .setInterpolator(new AccelerateDecelerateInterpolator())
            .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    view.setVisibility(View.GONE);
                }
            });
    }
    
    /**
     * Slide up animation
     */
    public static void slideUp(View view, long duration) {
        view.setTranslationY(view.getHeight());
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate()
            .translationY(0)
            .alpha(1f)
            .setDuration(duration)
            .setInterpolator(new DecelerateInterpolator())
            .setListener(null);
    }
    
    /**
     * Slide down animation
     */
    public static void slideDown(View view, long duration) {
        view.setTranslationY(-view.getHeight());
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate()
            .translationY(0)
            .alpha(1f)
            .setDuration(duration)
            .setInterpolator(new DecelerateInterpolator())
            .setListener(null);
    }
    
    /**
     * Scale animation with bounce effect
     */
    public static void scaleIn(View view, long duration) {
        view.setScaleX(0f);
        view.setScaleY(0f);
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(duration)
            .setInterpolator(new OvershootInterpolator())
            .setListener(null);
    }
    
    /**
     * Pulse animation for buttons
     */
    public static void pulse(View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.05f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.05f, 1f);
        scaleX.setDuration(200);
        scaleY.setDuration(200);
        scaleX.start();
        scaleY.start();
    }
    
    /**
     * Shake animation for error states
     */
    public static void shake(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationX", 0, 25, -25, 25, -25, 15, -15, 6, -6, 0);
        animator.setDuration(500);
        animator.start();
    }
    
    /**
     * Animate RecyclerView items with stagger
     */
    public static void animateRecyclerViewItems(RecyclerView recyclerView, android.content.Context context) {
        if (recyclerView.getAdapter() != null) {
            for (int i = 0; i < recyclerView.getChildCount(); i++) {
                View child = recyclerView.getChildAt(i);
                int position = recyclerView.getChildAdapterPosition(child);
                if (position != RecyclerView.NO_POSITION) {
                    Animation animation = AnimationUtils.loadAnimation(context, 
                        context.getResources().getIdentifier("item_animation", "anim", context.getPackageName()));
                    if (animation != null) {
                        animation.setStartOffset(position * 100);
                        child.startAnimation(animation);
                    }
                }
            }
        }
    }
    
    /**
     * Slide in from right for RecyclerView items
     */
    public static void slideInFromRight(View view, int position) {
        view.setTranslationX(view.getWidth());
        view.setAlpha(0f);
        view.animate()
            .translationX(0)
            .alpha(1f)
            .setDuration(400)
            .setStartDelay(position * 100)
            .setInterpolator(new DecelerateInterpolator())
            .setListener(null);
    }
}

