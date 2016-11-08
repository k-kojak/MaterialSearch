package hu.kojak.android.materialtoolbarset;

import android.animation.Animator;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;
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

    private AppBarLayout mTopView;

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
        setClickable(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    public void init(@NonNull AppBarLayout actionBar) {
        mTopView = actionBar;
        moveViewToRoot();
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

    public void reveal(MenuItem menuItem) {
        View menuView = mTopView.findViewById(menuItem.getItemId());
        menuView.getLocationOnScreen(mAnimationCenter);

        int toolbarHeight = getStatusBarHeight();
        mAnimationCenter[0] = mAnimationCenter[0] + menuView.getWidth() / 2;
        mAnimationCenter[1] = mAnimationCenter[1] + menuView.getHeight() / 2 - toolbarHeight;

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
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });
        anim.setDuration(mAnimSpeed);
        anim.start();

        int actionbarHeight = mTopView.getHeight();
        if (mHeight < actionbarHeight) {
            animateTopMargin(mTopView, - (actionbarHeight - mHeight), (int) (mAnimSpeed * TOOLBAR_ANIM_MULTIPLIER));
        }

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
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });
        anim.setDuration(mAnimSpeed);
        anim.start();

        if (mListener != null) {
            mListener.onHide();
        }

        int actionbarHeight = mTopView.getHeight();
        if (mHeight < actionbarHeight) {
            animateTopMargin(mTopView, 0, (int) (mAnimSpeed * TOOLBAR_ANIM_MULTIPLIER));
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

    private static void animateTopMargin(final View view, int to, int duration) {
        final LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
        final int original = params.topMargin;
        final int end = to - original;
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                params.topMargin = (int) (original + end * interpolatedTime);
                view.setLayoutParams(params);
            }
        };
        a.setDuration(duration); // in ms
        view.startAnimation(a);
    }

}
