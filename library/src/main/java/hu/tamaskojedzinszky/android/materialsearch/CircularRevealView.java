package hu.tamaskojedzinszky.android.materialsearch;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.RelativeLayout;


/**
 * Created by Tamas Kojedzinszky on 06/11/2016.
 */

public class CircularRevealView extends RelativeLayout {

    private static final String STATE_IS_VIEWING = CircularRevealView.class.getName() + ".state_is_viewing";
    private static final float TOOLBAR_ANIM_MULTIPLIER = 0.75f;

    private final int[] mAnimationCenter = new int[2];

    private int mAnimSpeed;
    private int mHeight;
    private boolean mIsInitialized = false;

    @Nullable
    private View mTargetView;

    private RevealListener mListener = null;

    public CircularRevealView(Context context) {
        super(context);
        initAttributes(context, null);
    }

    public CircularRevealView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttributes(context, attrs);
    }

    public CircularRevealView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttributes(context, attrs);
    }

    private void initAttributes(Context context, @Nullable AttributeSet attrs) {
        if (attrs == null) return;

        int[] attrsArray = new int[] {
                R.attr.revealSpeed, // 0
                android.R.attr.layout_height, // 1
        };

        TypedArray a = context.obtainStyledAttributes(attrs, attrsArray);

        mAnimSpeed = a.getInteger(
                0,
                context.getResources().getInteger(android.R.integer.config_mediumAnimTime));

        mHeight = a.getDimensionPixelSize(1, 0);

        a.recycle();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        if (!mIsInitialized) {
            setClickable(true);
            moveViewToRoot();
            mIsInitialized = true;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    /**
     * Setting a targetView, which will be a reference for properly displaying the
     * CircularReveal view. TargetView can be the Toolbar, AppBarLayout or any view
     * (but ideally some view in the top section) which you want to be hidden
     * when the searchview becomes visible.
     */
    public void setTargetView(@NonNull View targetView) {
        mTargetView = targetView;
    }

    public void setListener(RevealListener listener) {
        mListener = listener;
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

    public void reveal(View sourceView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            postLollipopReveal(sourceView);
        } else {
            preLollipopReveal();
        }
        hideTargetIfNeeded();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void postLollipopReveal(View sourceOfTrigger) {
        sourceOfTrigger.getLocationOnScreen(mAnimationCenter);

        int toolbarHeight = getStatusBarHeight();
        mAnimationCenter[0] = mAnimationCenter[0] + sourceOfTrigger.getWidth() / 2;
        mAnimationCenter[1] = mAnimationCenter[1] + sourceOfTrigger.getHeight() / 2 - toolbarHeight;

        Animator anim = ViewAnimationUtils.createCircularReveal(
                this,
                mAnimationCenter[0],
                mAnimationCenter[1],
                0,
                mAnimationCenter[0]);

        setVisibility(View.VISIBLE);

        anim.addListener(new SimpleAnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animator) {
                if (mListener != null) {
                    mListener.onReveal();
                }
            }
        });
        anim.setDuration(mAnimSpeed);
        anim.start();

    }

    private void preLollipopReveal() {
        setTopMargin(this, -mHeight);
        setVisibility(View.VISIBLE);
        animateTopMargin(this, 0, mAnimSpeed, new SimpleAnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                if (mListener != null) {
                    mListener.onReveal();
                }
            }
        });
    }

    private void hideTargetIfNeeded() {
        if (mTargetView == null) return;

        int targetHeight = mTargetView.getHeight();
        if (mHeight < targetHeight) {
            animateTopMargin(
                    mTargetView,
                    -(targetHeight - mHeight),
                    (int) (mAnimSpeed * TOOLBAR_ANIM_MULTIPLIER),
                    null);
        }
    }

    public void hide() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            postLollipopHide();
        } else {
            preLollipopHide();
        }
        revealTargetIfNeeded();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void postLollipopHide() {
        Animator anim = ViewAnimationUtils.createCircularReveal(
                this,
                mAnimationCenter[0],
                mAnimationCenter[1],
                mAnimationCenter[0],
                0);

        anim.addListener(new SimpleAnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animator) {
                setVisibility(View.GONE);
                if (mListener != null) {
                    mListener.onHide();
                }
            }
        });
        anim.setDuration(mAnimSpeed);
        anim.start();

    }

    private void preLollipopHide() {
        animateTopMargin(this, -mHeight, mAnimSpeed, new SimpleAnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                setVisibility(View.GONE);
                if (mListener != null) {
                    mListener.onHide();
                }
            }
        });
    }

    private void revealTargetIfNeeded() {
        if (mTargetView == null) return;

        int targetHeight = mTargetView.getHeight();
        if (mHeight < targetHeight) {
            animateTopMargin(
                    mTargetView,
                    0,
                    (int) (mAnimSpeed * TOOLBAR_ANIM_MULTIPLIER),
                    null);
        }
    }

    public boolean isViewing() {
        return getVisibility() != View.GONE;
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

    private static void animateTopMargin(final View view, int to, int duration,
                                         @Nullable SimpleAnimationListener animListener) {
        final MarginLayoutParams params = (MarginLayoutParams) view.getLayoutParams();
        final int original = params.topMargin;
        final int end = to - original;
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                params.topMargin = (int) (original + end * interpolatedTime);
                view.setLayoutParams(params);
            }
        };
        a.setDuration(duration);
        if (animListener != null) {
            a.setAnimationListener(animListener);
        }
        view.startAnimation(a);
    }

    private static void setTopMargin(final View view, int topMargin) {
        final MarginLayoutParams params = (MarginLayoutParams) view.getLayoutParams();
        params.topMargin = topMargin;
        view.setLayoutParams(params);
    }

    private static class SimpleAnimationListener implements Animation.AnimationListener {
        @Override
        public void onAnimationStart(Animation animation) {}
        @Override
        public void onAnimationEnd(Animation animation) {}
        @Override
        public void onAnimationRepeat(Animation animation) {}
    }

    private static class SimpleAnimatorListener implements Animator.AnimatorListener {
        @Override
        public void onAnimationStart(Animator animation) {}
        @Override
        public void onAnimationEnd(Animator animation) {}
        @Override
        public void onAnimationCancel(Animator animation) {}
        @Override
        public void onAnimationRepeat(Animator animation) {}
    }

}
