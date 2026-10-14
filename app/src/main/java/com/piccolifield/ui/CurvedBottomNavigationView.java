package com.piccolifield.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.piccolifield.R;

public class CurvedBottomNavigationView extends FrameLayout {

    public interface OnTabSelectedListener {
        void onTabSelected(int position);
    }

    private static final int TAB_COUNT = 4;
    private final int[] TAB_ICONS = new int[]{
            R.drawable.ic_home,
            R.drawable.ic_favorite,
            R.drawable.ic_search,
            R.drawable.ic_settings
    };

    private Paint mPaint;
    private Path mPath;

    private float mCurveWidth;
    private float mCurveDepth;
    private float mBarTopY;

    private float mCurveCenterX = -1;
    private int mSelectedIndex = 0;

    private final ImageView[] mTabImageViews = new ImageView[TAB_COUNT];
    private FrameLayout mActiveCircleView;
    private ImageView mActiveIconView;

    private ValueAnimator mAnimator;
    private OnTabSelectedListener mListener;

    public CurvedBottomNavigationView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public CurvedBottomNavigationView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CurvedBottomNavigationView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setWillNotDraw(false);

        mCurveWidth = dpToPx(110);
        mCurveDepth = dpToPx(28);
        mBarTopY = dpToPx(18);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(ContextCompat.getColor(context, R.color.cafe_profundo));

        mPath = new Path();

        // 1. Contenedor para los iconos no seleccionados sobre la barra
        LinearLayout tabContainer = new LinearLayout(context);
        tabContainer.setOrientation(LinearLayout.HORIZONTAL);
        tabContainer.setWeightSum(TAB_COUNT);

        FrameLayout.LayoutParams containerParams = new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                (int) dpToPx(56)
        );
        containerParams.gravity = Gravity.BOTTOM;
        tabContainer.setLayoutParams(containerParams);

        for (int i = 0; i < TAB_COUNT; i++) {
            final int index = i;
            ImageView iconView = new ImageView(context);
            iconView.setImageResource(TAB_ICONS[i]);
            iconView.setColorFilter(ContextCompat.getColor(context, R.color.beige_licor));
            iconView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            iconView.setPadding((int) dpToPx(12), (int) dpToPx(12), (int) dpToPx(12), (int) dpToPx(12));

            LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                    0,
                    LayoutParams.MATCH_PARENT,
                    1f
            );
            iconView.setLayoutParams(itemParams);

            iconView.setOnClickListener(v -> selectTab(index, true));

            mTabImageViews[i] = iconView;
            tabContainer.addView(iconView);
        }

        addView(tabContainer);

        // 2. Círculo activo flotante
        mActiveCircleView = new FrameLayout(context);
        int circleSize = (int) dpToPx(52);
        FrameLayout.LayoutParams circleParams = new FrameLayout.LayoutParams(circleSize, circleSize);
        circleParams.gravity = Gravity.TOP | Gravity.START;
        mActiveCircleView.setLayoutParams(circleParams);
        mActiveCircleView.setBackgroundResource(R.drawable.bg_curved_active_circle);
        mActiveCircleView.setElevation(dpToPx(6));

        mActiveIconView = new ImageView(context);
        int iconSize = (int) dpToPx(26);
        FrameLayout.LayoutParams iconParams = new FrameLayout.LayoutParams(iconSize, iconSize);
        iconParams.gravity = Gravity.CENTER;
        mActiveIconView.setLayoutParams(iconParams);
        mActiveIconView.setImageResource(TAB_ICONS[mSelectedIndex]);
        mActiveIconView.setColorFilter(ContextCompat.getColor(context, R.color.cafe_profundo));

        mActiveCircleView.addView(mActiveIconView);
        addView(mActiveCircleView);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (getWidth() > 0) {
            if (mCurveCenterX < 0) {
                mCurveCenterX = getTabCenterX(mSelectedIndex);
            }
            updateActiveCirclePosition();
            updateTabIconsVisibility();
        }
    }

    private float getTabCenterX(int index) {
        float widthPerTab = (float) getWidth() / TAB_COUNT;
        return widthPerTab * index + widthPerTab / 2f;
    }

    private void updateActiveCirclePosition() {
        if (mActiveCircleView != null) {
            float circleRadius = mActiveCircleView.getWidth() / 2f;
            if (circleRadius == 0) circleRadius = dpToPx(26);

            float targetX = mCurveCenterX - circleRadius;
            float targetY = mBarTopY + (mCurveDepth / 2f) - circleRadius + dpToPx(1);

            mActiveCircleView.setX(targetX);
            mActiveCircleView.setY(targetY);
        }
    }

    private void updateTabIconsVisibility() {
        for (int i = 0; i < TAB_COUNT; i++) {
            if (i == mSelectedIndex) {
                mTabImageViews[i].setAlpha(0f);
            } else {
                mTabImageViews[i].setAlpha(1f);
            }
        }
    }

    public void selectTab(int index, boolean animate) {
        if (index < 0 || index >= TAB_COUNT) return;

        int previousIndex = mSelectedIndex;
        mSelectedIndex = index;

        float targetCenterX = getTabCenterX(index);

        if (mListener != null && previousIndex != index) {
            mListener.onTabSelected(index);
        }

        if (animate && getWidth() > 0) {
            if (mAnimator != null && mAnimator.isRunning()) {
                mAnimator.cancel();
            }

            float startX = mCurveCenterX;

            mAnimator = ValueAnimator.ofFloat(startX, targetCenterX);
            mAnimator.setDuration(320);
            mAnimator.setInterpolator(new DecelerateInterpolator());
            mAnimator.addUpdateListener(animation -> {
                mCurveCenterX = (float) animation.getAnimatedValue();
                updateActiveCirclePosition();
                invalidate();
            });
            mAnimator.start();

            // Animación del icono dentro del círculo
            mActiveIconView.animate()
                    .alpha(0f)
                    .setDuration(120)
                    .withEndAction(() -> {
                        mActiveIconView.setImageResource(TAB_ICONS[index]);
                        mActiveIconView.animate().alpha(1f).setDuration(120).start();
                    })
                    .start();

            updateTabIconsVisibility();
        } else {
            mCurveCenterX = targetCenterX;
            mActiveIconView.setImageResource(TAB_ICONS[index]);
            updateActiveCirclePosition();
            updateTabIconsVisibility();
            invalidate();
        }
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        if (width == 0 || height == 0) return;

        if (mCurveCenterX < 0) {
            mCurveCenterX = getTabCenterX(mSelectedIndex);
        }

        float firstCurveStart = mCurveCenterX - (mCurveWidth / 2f);
        float secondCurveEnd = mCurveCenterX + (mCurveWidth / 2f);

        mPath.reset();
        mPath.moveTo(0, mBarTopY);
        mPath.lineTo(firstCurveStart, mBarTopY);

        // Curva cóncava suave
        float c1x = firstCurveStart + (mCurveWidth / 4f);
        float c1y = mBarTopY;
        float c2x = mCurveCenterX - (mCurveWidth / 4f);
        float c2y = mBarTopY + mCurveDepth;
        mPath.cubicTo(c1x, c1y, c2x, c2y, mCurveCenterX, mBarTopY + mCurveDepth);

        float c3x = mCurveCenterX + (mCurveWidth / 4f);
        float c3y = mBarTopY + mCurveDepth;
        float c4x = secondCurveEnd - (mCurveWidth / 4f);
        float c4y = mBarTopY;
        mPath.cubicTo(c3x, c3y, c4x, c4y, secondCurveEnd, mBarTopY);

        mPath.lineTo(width, mBarTopY);
        mPath.lineTo(width, height);
        mPath.lineTo(0, height);
        mPath.close();

        canvas.drawPath(mPath, mPaint);
    }

    public void setOnTabSelectedListener(OnTabSelectedListener listener) {
        this.mListener = listener;
    }

    public int getSelectedIndex() {
        return mSelectedIndex;
    }

    private float dpToPx(float dp) {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        );
    }
}