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
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;


/**
 * Created by Tamas Kojedzinszky on 06/11/2016.
 */

public class CircularRevealView extends RelativeLayout {

    private static final String EXCEPTION_TARGETVIEW_IS_NULL = "TargetView is null.";
    private static final String EXCEPTION_ALREADY_HAS_DIFFERENT_BEHAVIOR = "This class already has a different behavior. Once you selected one, you cannot use a different one.";

    private static final String STATE_IS_VIEWING = CircularRevealView.class.getName() + ".state_is_viewing";
    private static final float TOOLBAR_ANIM_MULTIPLIER = 0.75f;

    private int mRevealSpeed;
    private Behavior mBehavior;
    private Position mPosition;
    private int mHeight;
    private int mOriginalTopMargin;

    private ViewGroup mCancelLayer;
    private ViewGroup mAndroidContent;
    private Animator mCircularAnimator;
    private Animation mAlphaAnimation;
    private int mAnimationRadius;

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
        init(context, null);
    }

    public CircularRevealView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CircularRevealView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        if (attrs == null) return;

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircularRevealView);
        mRevealSpeed = a.getInteger(R.styleable.CircularRevealView_revealSpeed,
                context.getResources().getInteger(android.R.integer.config_mediumAnimTime));
        a.recycle();

        // getting height
        setVisibility(View.VISIBLE);
        getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        mHeight = getHeight();
                        mOriginalTopMargin = ((MarginLayoutParams) getLayoutParams()).topMargin;
                        getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        setVisibility(View.GONE);
                    }
                });
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
        Behavior newBehavior = Behavior.TOOLBAR;
        checkBehavior(newBehavior);

        mTargetView = targetView;
        mBehavior = newBehavior;
        mPosition = Position.ON;
        reveal(sourceView);
    }

    public void revealFloating(@NonNull Position position, @NonNull View targetView, @Nullable View sourceView) {
        Behavior newBehavior = Behavior.FLOATING;
        checkBehavior(newBehavior);

        mTargetView = targetView;
        mBehavior = newBehavior;
        mPosition = position;
        reveal(sourceView);
    }

    private void checkBehavior(Behavior newBehavior) {
        if (mBehavior != null && !mBehavior.equals(newBehavior)) {
            throw new IllegalArgumentException(EXCEPTION_ALREADY_HAS_DIFFERENT_BEHAVIOR);
        }
    }

    /**
     * Not supporting it, yet.
     */
//    public void revealInPlace(@Nullable View sourceView) {
//        Behavior newBehavior = Behavior.INPLACE;
//        checkBehavior(newBehavior);
//
//        mBehavior = newBehavior;
//        mPosition = null;
//        reveal(sourceView);
//    }
    public boolean isAnimationRunning() {
        return (mCircularAnimator != null && mCircularAnimator.isRunning())
                || (mAlphaAnimation != null && mAlphaAnimation.hasStarted() && !mAlphaAnimation.hasEnded());
    }

    private void reveal(final View sourceView) {
        if (isAnimationRunning()) return;

        if (!mIsInitialized) {
            if (mBehavior == Behavior.TOOLBAR || mBehavior == Behavior.FLOATING) {
                moveViewToRoot();
            }
            mIsInitialized = true;
        }

        if (mBehavior == Behavior.FLOATING) {
            setMarginTopBasedOnTargetView();
            mCancelLayer.setVisibility(View.VISIBLE);
        }
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
        int halfSourceHeight = sourceOfTrigger.getHeight() / 2;
        mAnimationCenter[0] = mAnimationCenter[0] + sourceOfTrigger.getWidth() / 2;
        mAnimationCenter[1] = mAnimationCenter[1] + halfSourceHeight - toolbarHeight - ((MarginLayoutParams) getLayoutParams()).topMargin;
        mAnimationRadius = (int) Math.hypot(mAnimationCenter[0], mHeight + halfSourceHeight);

        mCircularAnimator = ViewAnimationUtils.createCircularReveal(
                this,
                mAnimationCenter[0],
                mAnimationCenter[1],
                0,
                mAnimationRadius);

        mCircularAnimator.addListener(new SimpleAnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (mBehavior == Behavior.FLOATING) {
                    mCancelLayer.setVisibility(View.VISIBLE);
                }
                setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (mListener != null) {
                    mListener.onReveal();
                }
            }
        });
        mCircularAnimator.setDuration(mRevealSpeed);
        mCircularAnimator.start();

    }

    private void preLollipopReveal() {
        setVisibility(View.VISIBLE);

        mAlphaAnimation = new AlphaAnimation(0, 1);
        mAlphaAnimation.setDuration(mRevealSpeed);
        mAlphaAnimation.setFillAfter(false);
        mAlphaAnimation.setAnimationListener(new SimpleAnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                if (mListener != null) {
                    mListener.onReveal();
                }
            }
        });
        startAnimation(mAlphaAnimation);
    }

    public void hide() {
        if (isAnimationRunning()) return;
        if (!isViewing()) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            postLollipopHide();
        } else {
            preLollipopHide();
        }
        revealTargetIfNeeded();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void postLollipopHide() {
        mCircularAnimator = ViewAnimationUtils.createCircularReveal(
                this,
                mAnimationCenter[0],
                mAnimationCenter[1],
                mAnimationRadius,
                0);

        mCircularAnimator.addListener(new SimpleAnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animator) {
                if (mBehavior == Behavior.FLOATING) {
                    mCancelLayer.setVisibility(View.GONE);
                }
                setVisibility(View.GONE);

                if (mListener != null) {
                    mListener.onHide();
                }
            }
        });
        mCircularAnimator.setDuration(mRevealSpeed);
        mCircularAnimator.start();

    }

    private void preLollipopHide() {
        mAlphaAnimation = new AlphaAnimation(1, 0);
        mAlphaAnimation.setDuration(mRevealSpeed);
        mAlphaAnimation.setFillAfter(false);
        mAlphaAnimation.setAnimationListener(new SimpleAnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                if (mBehavior == Behavior.FLOATING) {
                    mCancelLayer.setVisibility(View.GONE);
                }
                setVisibility(View.GONE);

                if (mListener != null) {
                    mListener.onHide();
                }

            }
        });
        startAnimation(mAlphaAnimation);
    }

    /**
     * This should be called only when in Toolbar mode, when we automatically reveal target.
     */
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

    public boolean isViewing() {
        return getVisibility() != View.GONE;
    }

    private void setMarginTopBasedOnTargetView() {
        if (mTargetView == null) {
            throw new RuntimeException(EXCEPTION_TARGETVIEW_IS_NULL);
        }

        int[] targetViewLocation = new int[2];
        mTargetView.getLocationInWindow(targetViewLocation);
        int topMargin = targetViewLocation[1];

        if (mPosition == Position.BELOW) {
            topMargin += mTargetView.getHeight();
        }

        topMargin -= getStatusBarHeight();

        positionOnScreenVertically(topMargin);
    }

    private void positionOnScreenVertically(int topMargin) {
        MarginLayoutParams params = (MarginLayoutParams) getLayoutParams();
        params.topMargin = topMargin + mOriginalTopMargin;
        setLayoutParams(params);
    }

    private void moveViewToRoot() {
        searchAndroidContentView();

        // saving margin params
        MarginLayoutParams marginParams = (MarginLayoutParams) getLayoutParams();

        // finding direct parent
        final ViewGroup directParent = (ViewGroup) getParent();
        directParent.removeView(this);
        if (mBehavior == Behavior.FLOATING) {
            initCancelLayer();
            mCancelLayer.addView(this);
            mAndroidContent.addView(mCancelLayer);
        } else {
            mAndroidContent.addView(this);
        }

        // applying margin params
        reapplyLostLayoutParams(marginParams);
    }

    private void reapplyLostLayoutParams(MarginLayoutParams savedMarginParams) {
        MarginLayoutParams params = ((MarginLayoutParams)getLayoutParams());
        params.leftMargin = savedMarginParams.leftMargin;
        params.topMargin= savedMarginParams.topMargin;
        params.rightMargin = savedMarginParams.rightMargin;
        params.bottomMargin = savedMarginParams.bottomMargin;
    }

    /**
     * Searching android.R.id.content view.
     */
    private void searchAndroidContentView() {
        if (mAndroidContent == null) {
            mAndroidContent = (ViewGroup) getParent();
            while (mAndroidContent != null && mAndroidContent.getId() != android.R.id.content) {
                mAndroidContent = (ViewGroup) mAndroidContent.getParent();
            }
            if (mAndroidContent == null) {
                throw new RuntimeException("Cannot find android.R.id.content in parents, that's a problem!");
            }
        }
    }

    private void initCancelLayer() {
        if (mCancelLayer == null) {
            mCancelLayer = new LinearLayout(getContext());
            mCancelLayer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            mCancelLayer.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    hide();
                }
            });
            mCancelLayer.setVisibility(View.GONE);
        }
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
