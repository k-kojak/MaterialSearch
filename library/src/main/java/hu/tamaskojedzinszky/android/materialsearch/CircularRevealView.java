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
import android.util.Log;
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

    private static final String EXCEPTION_TARGETVIEW_IS_NULL = "TargetView is null.";

    private static final String STATE_IS_VIEWING = CircularRevealView.class.getName() + ".state_is_viewing";
    private static final float TOOLBAR_ANIM_MULTIPLIER = 0.75f;

    private int mRevealSpeed;
    private Behavior mBehavior;
    private Position mPosition;
    private int mHeight;

    private boolean mIsInitialized = false;
    private final int[] mAnimationCenter = new int[2];

    @Nullable
    private View mTargetView;

    private RevealListener mListener = null;

    private enum Behavior {
        TOOLBAR,
        FLOATING,
        INPLACE;
    }

    public enum Position {
        ON,
        BELOW
    }

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

        int[] attrsArray = new int[]{
                R.attr.revealSpeed, // 0
                android.R.attr.layout_height, // 1

        };

        TypedArray a = context.obtainStyledAttributes(attrs, attrsArray);

        mRevealSpeed = a.getInteger(0,
                context.getResources().getInteger(android.R.integer.config_mediumAnimTime));

        mHeight = a.getDimensionPixelSize(1, 0);

        a.recycle();

        setVisibility(View.GONE);

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

    public void revealOnToolbar(@NonNull View targetView, @Nullable View sourceView) {
        mTargetView = targetView;
        mBehavior = Behavior.TOOLBAR;
        mPosition = Position.ON;
        reveal(sourceView);
    }

    public void revealFloating(@NonNull Position position, @NonNull View targetView, @Nullable View sourceView) {
        mTargetView = targetView;
        mBehavior = Behavior.FLOATING;
        mPosition = position;
        reveal(sourceView);
    }

    public void revealInPlace(@Nullable View sourceView) {
        mBehavior = Behavior.INPLACE;
        mPosition = null;
        reveal(sourceView);
    }

    private void reveal(View sourceView) {
        if (!mIsInitialized) {
            if (mBehavior == Behavior.TOOLBAR || mBehavior == Behavior.FLOATING) {
                moveViewToRoot();
            }
            mIsInitialized = true;
        }

        if (mBehavior == Behavior.FLOATING) {
            setMarginTopBasedOnTargetView();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            postLollipopFloatingReveal(sourceView);
        } else {
            preLollipopFloatingReveal();
        }
        hideTargetIfNeeded();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void postLollipopFloatingReveal(View sourceOfTrigger) {
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

        anim.addListener(new SimpleAnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                setVisibility(View.VISIBLE);
                Log.d("asd", "Settting visiblity to VISIBLE");
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (mListener != null) {
                    mListener.onReveal();
                }
            }
        });
        anim.setDuration(mRevealSpeed);
        anim.start();

    }

    private void preLollipopFloatingReveal() {
        setTopMargin(this, -mHeight);
        setVisibility(View.VISIBLE);
        animateTopMargin(this, 0, mRevealSpeed, new SimpleAnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                if (mListener != null) {
                    mListener.onReveal();
                }
            }
        });
    }

    /**
     * This should be called only when in Toolbar mode, when we automatically hide the view.
     */
    private void hideTargetIfNeeded() {
        if (mBehavior != Behavior.TOOLBAR) return;
        if (mTargetView == null) {
            throw new RuntimeException(EXCEPTION_TARGETVIEW_IS_NULL);
        }

        int targetHeight = mTargetView.getHeight();
        if (mHeight < targetHeight) {
            animateTopMargin(
                    mTargetView,
                    -(targetHeight - mHeight),
                    (int) (mRevealSpeed * TOOLBAR_ANIM_MULTIPLIER),
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
        anim.setDuration(mRevealSpeed);
        anim.start();

    }

    private void preLollipopHide() {
        animateTopMargin(this, -mHeight, mRevealSpeed, new SimpleAnimationListener() {
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
        if (mBehavior != Behavior.TOOLBAR) return;
        if (mTargetView == null) {
            throw new RuntimeException(EXCEPTION_TARGETVIEW_IS_NULL);
        }

        int targetHeight = mTargetView.getHeight();
        if (mHeight < targetHeight) {
            animateTopMargin(
                    mTargetView,
                    0,
                    (int) (mRevealSpeed * TOOLBAR_ANIM_MULTIPLIER),
                    null);
        }
    }

    public boolean isViewing() {
        return getVisibility() != View.GONE;
    }

    private void setMarginTopBasedOnTargetView() {
        if (mTargetView == null) {
            throw new RuntimeException(EXCEPTION_TARGETVIEW_IS_NULL);
        }

        int[] location = new int[2];
        mTargetView.getLocationInWindow(location);
        int topMargin = location[1];

        if (mPosition == Position.BELOW) {
            topMargin += mTargetView.getHeight();
        }

        topMargin -= getStatusBarHeight();

        setTopMargin(this, topMargin);
    }

    private void moveViewToRoot() {
        Log.d("asd", "moveViewToRoot()");
        // searching android.R.id.content
        ViewGroup androidContent = (ViewGroup) getParent();
        while (androidContent != null && androidContent.getId() != android.R.id.content) {
            androidContent = (ViewGroup) androidContent.getParent();
        }
        if (androidContent == null) {
            throw new RuntimeException("Cannot find android.R.id.content in parents, that's a problem!");
        }

        // moving view to android.R.id.content
        final ViewGroup directParent = (ViewGroup) getParent();
        final ViewGroup finalAndroidContent = androidContent;
        directParent.removeView(CircularRevealView.this);
        finalAndroidContent.addView(CircularRevealView.this);
        Log.d("asd", "ACTUALLY MOVING VIEW HERE");

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
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }
    }

    private static class SimpleAnimatorListener implements Animator.AnimatorListener {
        @Override
        public void onAnimationStart(Animator animation) {
        }

        @Override
        public void onAnimationEnd(Animator animation) {
        }

        @Override
        public void onAnimationCancel(Animator animation) {
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
        }
    }

}
