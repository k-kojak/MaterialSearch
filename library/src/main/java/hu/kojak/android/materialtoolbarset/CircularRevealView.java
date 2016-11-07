package hu.kojak.android.materialtoolbarset;

import android.animation.Animator;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

/**
 * Created by Tamas Kojedzinszky on 06/11/2016.
 */

public class CircularRevealView extends RelativeLayout {

    private static final String STATE_IS_VIEWING = CircularRevealView.class.getName() + ".state_is_viewing";

    private final int[] mAnimationCenter = new int[2];
    private int mAnimSpeed;
    private Toolbar mToolbar;
    private RevealListener mListener = null;

    public CircularRevealView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public CircularRevealView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public CircularRevealView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        if (attrs == null) return;

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircularRevealView, defStyleAttr, 0);
        mAnimSpeed = a.getInteger(
                R.styleable.CircularRevealView_revealSpeed,
                context.getResources().getInteger(android.R.integer.config_mediumAnimTime));
        a.recycle();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        setClickable(true);
    }

    public void setListener(RevealListener listener) {
        mListener = listener;
    }

    public void setToolbar(Toolbar toolbar) {
        mToolbar = toolbar;
        moveViewToRoot();
    }

    public void retainState(Bundle outState) {
        if (outState == null) return;

        outState.putBoolean(STATE_IS_VIEWING, isViewing());
    }

    public void restoreState(Bundle savedState) {
        if (savedState == null) return;

        boolean isViewing = savedState.getBoolean(STATE_IS_VIEWING, false);
        if (isViewing) {
            setVisibility(View.VISIBLE);
        }
    }

    public void reveal(MenuItem menuItem) {
        View menuView = mToolbar.findViewById(menuItem.getItemId());
        menuView.getLocationOnScreen(mAnimationCenter);

        int statusBarHeight = getStatusBarHeight();
        mAnimationCenter[0] = mAnimationCenter[0] + menuView.getWidth() / 2;
        mAnimationCenter[1] = mAnimationCenter[1] + menuView.getHeight() / 2 - statusBarHeight;

        Animator anim = ViewAnimationUtils.createCircularReveal(
                this,
                mAnimationCenter[0],
                mAnimationCenter[1],
                0,
                mAnimationCenter[0]);

        setVisibility(View.VISIBLE);

        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animator) {
                if (mListener != null) {
                    mListener.onReveal();
                }
            }

            @Override
            public void onAnimationStart(Animator animator) {}

            @Override
            public void onAnimationCancel(Animator animator) {}

            @Override
            public void onAnimationRepeat(Animator animator) {}
        });
        anim.setDuration(mAnimSpeed);
        anim.start();

    }

    public void hide() {
        Animator anim = ViewAnimationUtils.createCircularReveal(
                this,
                mAnimationCenter[0],
                mAnimationCenter[1],
                mAnimationCenter[0],
                0);

        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animator) {
                setVisibility(View.GONE);
            }

            @Override
            public void onAnimationStart(Animator animator) {}
            @Override
            public void onAnimationCancel(Animator animator) {}
            @Override
            public void onAnimationRepeat(Animator animator) {}
        });
        anim.setDuration(mAnimSpeed);
        anim.start();

        if (mListener != null) {
            mListener.onHide();
        }
    }

    public boolean isViewing() {
        return getVisibility() == View.VISIBLE;
    }

    private void moveViewToRoot() {
        setVisibility(View.GONE);

        ViewGroup directParent = (ViewGroup) getParent();
        ViewGroup mainParent = (ViewGroup) directParent.getParent();
        directParent.removeView(this);
        mainParent.addView(this);
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }


}
