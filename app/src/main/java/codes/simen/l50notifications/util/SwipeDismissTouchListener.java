/*
 * This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * https://github.com/romannurik/Android-SwipeToDismiss/blob/master/src/com/example/android/swipedismiss/SwipeDismissTouchListener.java
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package codes.simen.l50notifications.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.os.Build;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

/**
 * A {@link android.view.View.OnTouchListener} that makes any {@link android.view.View} dismissable when the
 * user swipes (drags her finger) horizontally across the view.
 *
 *
 * <p>Example usage:</p>
 *
 * <pre>
 * view.setOnTouchListener(new SwipeDismissTouchListener(
 *         view,
 *         null, // Optional token/cookie object
 *         new SwipeDismissTouchListener.OnDismissCallback() {
 *             public void onDismiss(View view, Object token) {
 *                 parent.removeView(view);
 *             }
 *         }));
 * </pre>
 *
 * <p>This class Requires API level 12 or later due to use of {@link
 * android.view.ViewPropertyAnimator}.</p>
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
public class SwipeDismissTouchListener implements View.OnTouchListener {
    public static final int DIRECTION_LEFT  = 0;
    public static final int DIRECTION_RIGHT = 1;
    public static final int DIRECTION_UP    = 2;
    public static final int DIRECTION_DOWN  = 3;

    // Cached ViewConfiguration and system-wide constant values
    private final int mSlop;
    private final int mMinFlingVelocity;
    private final int mMaxFlingVelocity;
    private final long mAnimationTime;

    // Fixed properties
    private final View mView;
    private final DismissCallbacks mCallbacks;
    private int mViewWidth = 1; // 1 and not 0 to prevent dividing by zero
    private int mViewHeight = 1;

    // Transient properties
    private float mDownX;
    private float mDownY;
    private boolean mSwiping;
    private boolean mWasSwiping;
    private boolean mSwipingVertical;
    private boolean mWasSwipingVertical;
    private int mSwipingSlop;
    private final Object mToken;
    private VelocityTracker mVelocityTracker;
    private float mTranslationX;
    private float mTranslationY;
    private boolean mIsTop;

    /**
     * The callback interface used by {@link SwipeDismissTouchListener} to inform its client
     * about a successful dismissal of the view for which it was created.
     */
    public interface DismissCallbacks {
        /**
         * Called to determine whether the view can be dismissed.
         */
        boolean canDismiss();
        /**
         * Called to determine whether the view can be expanded.
         */
        boolean canExpand();

        /**
         * Called when the user has indicated they she would like to dismiss the view.
         *
         * @param view      The originating {@link android.view.View} to be dismissed.
         * @param token     The optional token passed to this object's constructor.
         * @param direction The direction the view was swiped
         */
        void onDismiss(View view, Object token, int direction);

        void outside();
    }

    /**
     * Constructs a new swipe-to-dismiss touch listener for the given view.
     * @param view     The view to make dismissable.
     * @param isTop     Whether the view is in the top position.
     *                  If it is, we'll use a dirty hack to remove the "invisible line".
     * @param callbacks The callback to trigger when the user has indicated that she would like to
     */
    public SwipeDismissTouchListener(View view, boolean isTop, DismissCallbacks callbacks) {
        ViewConfiguration vc = ViewConfiguration.get(view.getContext());
        mSlop = vc.getScaledTouchSlop();
        mMinFlingVelocity = vc.getScaledMinimumFlingVelocity() * 4;
        mMaxFlingVelocity = vc.getScaledMaximumFlingVelocity();
        mAnimationTime = view.getContext().getResources().getInteger(
                android.R.integer.config_shortAnimTime);
        mView = view;
        mIsTop = isTop;
        mToken = null;
        mCallbacks = callbacks;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        // offset because the view is translated during swipe
        motionEvent.offsetLocation(mTranslationX, mTranslationY);

        if (mViewWidth < 2) {
            mViewWidth = mView.getWidth();
            if (mViewWidth > 491)
                mViewWidth = 491;
        }
        if (mViewHeight < 2) {
            mViewHeight = mView.getHeight();
        }

        switch (motionEvent.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                mDownX = motionEvent.getRawX();
                mDownY = motionEvent.getRawY();
                if (mCallbacks.canDismiss()) {
                    mVelocityTracker = VelocityTracker.obtain();
                    mVelocityTracker.addMovement(motionEvent);

                    if (mIsTop) {
                        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mView.getLayoutParams();
                        layoutParams.setMargins(0, 0, 0, 200);
                        mView.setLayoutParams(layoutParams);
                    }
                }
                return false;
            }

            case MotionEvent.ACTION_UP: {
                if (mVelocityTracker == null) {
                    break;
                }

                float deltaX = motionEvent.getRawX() - mDownX;
                float deltaY = motionEvent.getRawY() - mDownY;
                mVelocityTracker.addMovement(motionEvent);
                mVelocityTracker.computeCurrentVelocity(1000);
                float velocityX = mVelocityTracker.getXVelocity();
                float velocityY = mVelocityTracker.getYVelocity();
                float absVelocityX = Math.abs(velocityX);
                float absVelocityY = Math.abs(velocityY);
                boolean dismissLeft = false;
                boolean dismissRight = false;
                boolean dismissUp = false;
                boolean dismissDown = false;

                if (Math.abs(deltaX) > mViewWidth / 2 && mSwiping) {
                    dismissLeft = true;
                    dismissRight = deltaX > 0;
                } else if (mMinFlingVelocity <= absVelocityX && absVelocityX <= mMaxFlingVelocity
                        && absVelocityY < absVelocityX
                        && absVelocityY < absVelocityX && mSwiping) {
                    // dismiss only if flinging in the same direction as dragging
                    dismissLeft = (velocityX < 0) == (deltaX < 0);
                    dismissRight = mVelocityTracker.getXVelocity() > 0;
                }

                if (Math.abs(deltaY) > mViewHeight / 2 && mSwipingVertical) {
                    dismissUp = true;
                    dismissDown = deltaY > 0;
                } else if (mMinFlingVelocity <= absVelocityY && absVelocityY <= mMaxFlingVelocity
                        && absVelocityX < absVelocityY
                        && absVelocityX < absVelocityY && mSwipingVertical) {
                    // dismiss only if flinging in the same direction as dragging
                    dismissUp = (velocityY < 0) == (deltaY < 0);
                    dismissDown = mVelocityTracker.getYVelocity() > 0;
                }

                if (dismissLeft) {
                    // dismiss
                    final boolean finalDismissRight = dismissRight;
                    mView.animate()
                            .translationX(dismissRight ? mViewWidth*2 : -mViewWidth*2)
                            .alpha(0)
                            .setDuration(mAnimationTime)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    if (finalDismissRight) performDismiss(DIRECTION_RIGHT);
                                    else                   performDismiss(DIRECTION_LEFT);
                                }
                            });
                } else if (dismissUp) {
                    // dismiss
                    final boolean finalDismissDown = dismissDown;
                    mView.animate()
                            .translationY(dismissDown ? 0 : -mViewHeight * 2)
                            .alpha(dismissDown ? 1 : 0)
                            .setDuration(mAnimationTime)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mView.getLayoutParams();
                                    layoutParams.setMargins(0,0,0,0);
                                    mView.setLayoutParams(layoutParams);

                                    if (finalDismissDown)     performDismiss(DIRECTION_DOWN);
                                    else                      performDismiss(DIRECTION_UP);
                                }
                            });
                } else if (mSwiping || mSwipingVertical) {
                    // cancel
                    mView.animate()
                            .translationX(0)
                            .translationY(0)
                            .alpha(1)
                            .setDuration(mAnimationTime)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mView.getLayoutParams();
                                    layoutParams.setMargins(0,0,0,0);
                                    mView.setLayoutParams(layoutParams);
                                }
                            });
                }
                mVelocityTracker.recycle();
                mVelocityTracker = null;
                mTranslationX = 0;
                mTranslationY = 0;
                mDownX = 0;
                mDownY = 0;
                mSwiping = false;
                mSwipingVertical = false;

                break;
            }

            case MotionEvent.ACTION_CANCEL: {
                if (mVelocityTracker == null) {
                    break;
                }

                mView.animate()
                        .translationX(0)
                        .translationY(0)
                        .alpha(1)
                        .setDuration(mAnimationTime)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mView.getLayoutParams();
                                layoutParams.setMargins(0,0,0,0);
                                mView.setLayoutParams(layoutParams);
                            }
                        });

                mVelocityTracker.recycle();
                mVelocityTracker = null;
                mTranslationX = 0;
                mTranslationY = 0;
                mDownX = 0;
                mDownY = 0;
                mSwiping = false;
                mSwipingVertical = false;
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                if (mVelocityTracker == null) {
                    break;
                }
                mWasSwiping = mSwiping;
                mWasSwipingVertical = mSwipingVertical;

                mVelocityTracker.addMovement(motionEvent);
                float deltaX = motionEvent.getRawX() - mDownX;
                float deltaY = motionEvent.getRawY() - mDownY;
                if (Math.abs(deltaX) > mSlop && Math.abs(deltaY) < Math.abs(deltaX) / 2 && !mSwipingVertical) {
                    mSwiping = true;
                    mSwipingSlop = (deltaX > 0 ? mSlop : -mSlop);
                    mView.getParent().requestDisallowInterceptTouchEvent(true);

                    // Cancel listview's touch
                    MotionEvent cancelEvent = MotionEvent.obtain(motionEvent);
                    cancelEvent.setAction(MotionEvent.ACTION_CANCEL |
                            (motionEvent.getActionIndex() <<
                                    MotionEvent.ACTION_POINTER_INDEX_SHIFT));
                    mView.onTouchEvent(cancelEvent);
                    cancelEvent.recycle();

                    if (!mWasSwiping) {
                        //mView.setPadding(mViewWidth, 0, mViewWidth, 0);
                    }
                }

                if (Math.abs(deltaY) > mSlop && Math.abs(deltaX) < Math.abs(deltaY) / 2 && !mSwiping) {
                    mSwipingVertical = true;
                    mSwipingSlop = (deltaY > 0 ? mSlop : -mSlop);
                    mView.getParent().requestDisallowInterceptTouchEvent(true);

                    // Cancel listview's touch
                    MotionEvent cancelEvent = MotionEvent.obtain(motionEvent);
                    cancelEvent.setAction(MotionEvent.ACTION_CANCEL |
                            (motionEvent.getActionIndex() <<
                                    MotionEvent.ACTION_POINTER_INDEX_SHIFT));
                    mView.onTouchEvent(cancelEvent);
                    cancelEvent.recycle();

                    if (!mWasSwipingVertical) {
                        //mView.setPadding(0, mViewHeight, 0, mViewHeight);
                    }
                }

                if (mSwiping) {
                    mTranslationX = deltaX;
                    mView.setTranslationX(deltaX - mSwipingSlop);

                    mView.setAlpha(Math.max(0f, Math.min(1f,
                            1f - 1f * Math.abs(deltaX) / (mViewWidth * 2))));
                    return true;
                }

                if (mSwipingVertical) {
                    if (deltaY > 0) {
                        if (deltaY > 50 && mCallbacks.canExpand()) performDismiss(DIRECTION_DOWN);
                        deltaY = (float) (deltaY * 0.05 + mSwipingSlop);
                    }
                    mTranslationY = deltaY;
                    mView.setTranslationY(deltaY - mSwipingSlop);

                    return true;
                }
                break;
            }
            case MotionEvent.ACTION_OUTSIDE: {
                // We won't get X and Y for outside touches
                mCallbacks.outside();
                return true;
            }
        }
        return false;
    }

    private void performDismiss(final int direction) {

        if (DIRECTION_DOWN == direction) {
            mCallbacks.onDismiss(mView, mToken, direction);
            // Reset view presentation
            mView.setAlpha(1f);
            mView.setTranslationX(0);
            return;
        }

        // Animate the dismissed view to zero-height and then fire the dismiss callback.
        // This triggers layout on each animation frame; in the future we may want to do something
        // smarter and more performant.

        final FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mView.getLayoutParams();
        final int originalHeight = mView.getHeight();

        ValueAnimator animator = ValueAnimator.ofInt(originalHeight, 1).setDuration(mAnimationTime);

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCallbacks.onDismiss(mView, mToken, direction);
                // Reset view presentation
                mView.setAlpha(1f);
                mView.setTranslationX(0);
                lp.height = originalHeight;
                lp.setMargins(0, 0, 0, 0);
                mView.setLayoutParams(lp);
            }
        });

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                lp.height = (Integer) valueAnimator.getAnimatedValue();
                mView.setLayoutParams(lp);
            }
        });

        animator.start();
    }
}