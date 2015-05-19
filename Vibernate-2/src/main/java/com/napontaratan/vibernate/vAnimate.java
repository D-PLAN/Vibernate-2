package com.napontaratan.vibernate;

import android.animation.ObjectAnimator;
import android.support.v7.widget.RecyclerView;

/**
 * Created by Aor-Nawattranakul on 2015-05-18.
 */
public class vAnimate {


    public static void animate(RecyclerView.ViewHolder holder) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(holder.itemView, "translationY", 0, 100);
        animator.setDuration(1000);
        animator.start();

    }


}
