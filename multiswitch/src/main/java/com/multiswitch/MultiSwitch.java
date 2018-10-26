package com.multiswitch;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class MultiSwitch extends View {

    private static final String TAG = "SwitchMultiButton";
    /*default value*/
    private String[] mTabTexts = {"L", "R"};
    private int mTabNum = mTabTexts.length;
    private static final float STROKE_RADIUS = 0;
    private static final float STROKE_WIDTH = 2;
    private static final float TEXT_SIZE = 14;
    private static final int SELECTED_COLOR = 0xffeb7b00;
    private static final int SELECTED_TAB = 0;
    private static final boolean ALLOW_MULTI_TAB = false;
    private static final boolean FIRE_NON_MANUAL_EVENT = false;

    /*other*/
    private Paint mStrokePaint;
    private Paint mFillPaint;
    private int mWidth;
    private int mHeight;
    private TextPaint mSelectedTextPaint;
    private TextPaint mUnselectedTextPaint;
    private OnSwitchListener onSwitchListener;
    private float mStrokeRadius;
    private float mStrokeWidth;
    private int mSelectedColor;
    private float mTextSize;
    private Set<Integer> mSelectedTabs;
    private float perWidth;
    private float mTextHeightOffset;
    private Paint.FontMetrics mFontMetrics;
    private Typeface mTypeface;
    private boolean mAllowMultiTab;
    private boolean mFireNonManualEvent;

    public MultiSwitch(Context context) {
        this(context, null);
    }

    public MultiSwitch(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MultiSwitch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
        initPaint();
    }

    /**
     * get the values of attributes
     *
     * @param context
     * @param attrs
     */
    private void initAttrs(Context context, AttributeSet attrs) {
        mSelectedTabs = new HashSet<>();
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MultiSwitch);
        mStrokeRadius = typedArray.getDimension(R.styleable.MultiSwitch_strokeRadius, STROKE_RADIUS);
        mStrokeWidth = typedArray.getDimension(R.styleable.MultiSwitch_strokeWidth, STROKE_WIDTH);
        mTextSize = typedArray.getDimension(R.styleable.MultiSwitch_textSize, TEXT_SIZE);
        mSelectedColor = typedArray.getColor(R.styleable.MultiSwitch_selectedColor, SELECTED_COLOR);
        mSelectedTabs.add(typedArray.getInteger(R.styleable.MultiSwitch_selectedTab, SELECTED_TAB));
        mAllowMultiTab = typedArray.getBoolean(R.styleable.MultiSwitch_allowMultiTab, ALLOW_MULTI_TAB);
        mFireNonManualEvent = typedArray.getBoolean(R.styleable.MultiSwitch_fireNonManualEvent, FIRE_NON_MANUAL_EVENT);
        int mSwitchTabsResId = typedArray.getResourceId(R.styleable.MultiSwitch_switchTabs, 0);
        if (mSwitchTabsResId != 0) {
            mTabTexts = getResources().getStringArray(mSwitchTabsResId);
            mTabNum = mTabTexts.length;
        }
        String typeface = typedArray.getString(R.styleable.MultiSwitch_typeface);
        if (!TextUtils.isEmpty(typeface)) {
            mTypeface = Typeface.createFromAsset(context.getAssets(), typeface);
        }
        typedArray.recycle();
    }

    /**
     * define paints
     */
    private void initPaint() {
        // round rectangle paint
        mStrokePaint = new Paint();
        mStrokePaint.setColor(mSelectedColor);
        mStrokePaint.setStyle(Paint.Style.STROKE);
        mStrokePaint.setAntiAlias(true);
        mStrokePaint.setStrokeWidth(mStrokeWidth);

        // selected paint
        mFillPaint = new Paint();
        mFillPaint.setColor(mSelectedColor);
        mFillPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mFillPaint.setAntiAlias(true);

        // selected text paint
        mSelectedTextPaint = new TextPaint();
        mSelectedTextPaint.setTextSize(mTextSize);
        mSelectedTextPaint.setColor(0xffffffff);
        mSelectedTextPaint.setAntiAlias(true);

        // unselected text paint
        mUnselectedTextPaint = new TextPaint();
        mUnselectedTextPaint.setTextSize(mTextSize);
        mUnselectedTextPaint.setColor(mSelectedColor);
        mUnselectedTextPaint.setAntiAlias(true);

        mTextHeightOffset = -(mSelectedTextPaint.ascent() + mSelectedTextPaint.descent()) * 0.5f;
        mFontMetrics = mSelectedTextPaint.getFontMetrics();
        if (mTypeface != null) {
            mSelectedTextPaint.setTypeface(mTypeface);
            mUnselectedTextPaint.setTypeface(mTypeface);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int defaultWidth = getDefaultWidth();
        int defaultHeight = getDefaultHeight();
        setMeasuredDimension(getExpectSize(defaultWidth, widthMeasureSpec), getExpectSize(defaultHeight,
                heightMeasureSpec));
    }

    /**
     * get default height when android:layout_height="wrap_content"
     */
    private int getDefaultHeight() {
        return (int) (mFontMetrics.bottom - mFontMetrics.top) + getPaddingTop() + getPaddingBottom();
    }

    /**
     * get default width when android:layout_width="wrap_content"
     */
    private int getDefaultWidth() {
        float tabTextWidth = 0f;
        int tabs = mTabTexts.length;
        for (String mTabText : mTabTexts) {
            tabTextWidth = Math.max(tabTextWidth, mSelectedTextPaint.measureText(mTabText));
        }
        float totalTextWidth = tabTextWidth * tabs;
        float totalStrokeWidth = (mStrokeWidth * tabs);
        int totalPadding = (getPaddingRight() + getPaddingLeft()) * tabs;
        return (int) (totalTextWidth + totalStrokeWidth + totalPadding);
    }


    /**
     * get expect size
     *
     * @param size
     * @param measureSpec
     * @return
     */
    private int getExpectSize(int size, int measureSpec) {
        int result = size;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (specMode) {
            case MeasureSpec.EXACTLY:
                result = specSize;
                break;
            case MeasureSpec.UNSPECIFIED:
                result = size;
                break;
            case MeasureSpec.AT_MOST:
                result = Math.min(size, specSize);
                break;
            default:
                break;
        }
        return result;
    }

    private RectF rectF = new RectF();

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float left = mStrokeWidth * 0.5f;
        float top = mStrokeWidth * 0.5f;
        float right = mWidth - mStrokeWidth * 0.5f;
        float bottom = mHeight - mStrokeWidth * 0.5f;

        //draw rounded rectangle
        rectF.set(left, top, right, bottom);
        canvas.drawRoundRect(rectF, mStrokeRadius, mStrokeRadius, mStrokePaint);

        //draw line
        for (int i = 0; i < mTabNum - 1; i++) {
            canvas.drawLine(perWidth * (i + 1), top, perWidth * (i + 1), bottom, mStrokePaint);
        }
        //draw tab and line
        for (int i = 0; i < mTabNum; i++) {
            String tabText = mTabTexts[i];
            float tabTextWidth = mSelectedTextPaint.measureText(tabText);
            if (mSelectedTabs.contains(i)) {
                //draw selected tab
                if (i == 0) {
                    drawLeftPath(canvas, left, top, bottom);
                } else if (i == mTabNum - 1) {
                    drawRightPath(canvas, top, right, bottom);
                } else {
                    rectF.set(perWidth * i, top, perWidth * (i + 1), bottom);
                    canvas.drawRect(rectF, mFillPaint);
                }
                // draw selected text
                canvas.drawText(tabText, 0.5f * perWidth * (2 * i + 1) - 0.5f * tabTextWidth, mHeight * 0.5f +
                        mTextHeightOffset, mSelectedTextPaint);

            } else {
                //draw unselected text
                canvas.drawText(tabText, 0.5f * perWidth * (2 * i + 1) - 0.5f * tabTextWidth, mHeight * 0.5f +
                        mTextHeightOffset, mUnselectedTextPaint);
            }
        }
    }

    /**
     * draw right path
     *
     * @param canvas
     * @param left
     * @param top
     * @param bottom
     */
    private void drawLeftPath(Canvas canvas, float left, float top, float bottom) {
        Path leftPath = new Path();
        leftPath.moveTo(left + mStrokeRadius, top);
        leftPath.lineTo(perWidth, top);
        leftPath.lineTo(perWidth, bottom);
        leftPath.lineTo(left + mStrokeRadius, bottom);
        rectF.set(left, bottom - 2 * mStrokeRadius, left + 2 * mStrokeRadius, bottom);
        leftPath.arcTo(rectF, 90, 90);
        leftPath.lineTo(left, top + mStrokeRadius);
        rectF.set(left, top, left + 2 * mStrokeRadius, top + 2 * mStrokeRadius);
        leftPath.arcTo(rectF, 180, 90);
        canvas.drawPath(leftPath, mFillPaint);
    }

    /**
     * draw left path
     *
     * @param canvas
     * @param top
     * @param right
     * @param bottom
     */
    private void drawRightPath(Canvas canvas, float top, float right, float bottom) {
        Path rightPath = new Path();
        rightPath.moveTo(right - mStrokeRadius, top);
        rightPath.lineTo(right - perWidth, top);
        rightPath.lineTo(right - perWidth, bottom);
        rightPath.lineTo(right - mStrokeRadius, bottom);
        rectF.set(right - 2 * mStrokeRadius, bottom - 2 * mStrokeRadius, right, bottom);
        rightPath.arcTo(rectF, 90, -90);
        rightPath.lineTo(right, top + mStrokeRadius);
        rectF.set(right - 2 * mStrokeRadius, top, right, top + 2 * mStrokeRadius);
        rightPath.arcTo(rectF, 0, -90);
        canvas.drawPath(rightPath, mFillPaint);
    }


    /**
     * called after onMeasure
     *
     * @param w
     * @param h
     * @param oldw
     * @param oldh
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
        perWidth = mWidth / mTabNum;
        checkAttrs();
    }

    /**
     * check attribute where suitable
     */
    private void checkAttrs() {
        if (mStrokeRadius > 0.5f * mHeight) {
            mStrokeRadius = 0.5f * mHeight;
        }
    }

    /**
     * receive the event when touched
     *
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            float x = event.getX();
            for (int i = 0; i < mTabNum; i++) {
                if (x > perWidth * i && x < perWidth * (i + 1)) {
                    if (mSelectedTabs.contains(i)) {
                        unselectTab(i, true);
                    } else {
                        selectTab(i, true);
                    }
                }
            }
            invalidate();
        }
        return isEnabled();
    }

    private void unselectTab(int tab, boolean fireEvent) {
        mSelectedTabs.remove(tab);
        if (!mAllowMultiTab)
            selectTab(tab, false);
        else {
            if (onSwitchListener != null && fireEvent) {
                onSwitchListener.onSwitch(tab, mTabTexts[tab], false);
            }
        }
    }

    public MultiSwitch setOnSwitchListener(@NonNull OnSwitchListener onSwitchListener) {
        this.onSwitchListener = onSwitchListener;
        return this;
    }

    /**
     * get position of selected tab
     */
    public Integer[] getSelectedTabs() {
        return mSelectedTabs.toArray(new Integer[mSelectedTabs.size()]);
    }

    /**
     * Set a tab to selected
     *
     * @param selectedTab integer with the position of the tab
     */
    public MultiSwitch setTabSelected(int selectedTab) {
        selectTab(selectedTab, mFireNonManualEvent);
        invalidate();
        return this;
    }

    private void selectTab(int tab, boolean fireEvent) {
        if (!mAllowMultiTab)
            mSelectedTabs.clear();
        mSelectedTabs.add(tab);
        if (onSwitchListener != null && fireEvent) {
            onSwitchListener.onSwitch(tab, mTabTexts[tab], true);
        }
    }

    /**
     * Unselect a tab
     *
     * @param selectedTab integer with the position of the tab
     */
    public void setTabUnselected(int selectedTab) {
        unselectTab(selectedTab, mFireNonManualEvent);
        invalidate();
    }

    public void clearSelected() {
        HashSet<Integer> cp = new HashSet(mSelectedTabs);

        mSelectedTabs.clear();
        invalidate();

        Iterator<Integer> it = cp.iterator();

        while (it.hasNext()) {
            Integer i = it.next();
            unselectTab(i, mFireNonManualEvent);
            it.remove();
        }
    }

    /**
     * set data for the switchbutton
     *
     * @param tagTexts
     * @return
     */
    public MultiSwitch setText(String... tagTexts) {
        if (tagTexts.length > 1) {
            mTabTexts = tagTexts;
            mTabNum = tagTexts.length;
            requestLayout();
            return this;
        } else {
            throw new IllegalArgumentException("the size of tagTexts should greater then 1");
        }
    }

    /**
     * set Typeface for buttons from java code
     *
     * @param typeface
     */
    public MultiSwitch setTypeface(@NonNull Typeface typeface) {
        mTypeface = typeface;
        mSelectedTextPaint.setTypeface(typeface);
        mUnselectedTextPaint.setTypeface(typeface);
        invalidate();
        return this;
    }
    /*======================================save and restore======================================*/

    /**
     * Return true if the button is checked, or false otherwise
     *
     * @param tabPosition a int value
     * @return state a boolean value with the button state
     */
    public boolean getState(int tabPosition) {
        return mSelectedTabs.contains(tabPosition);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("View", super.onSaveInstanceState());
        bundle.putFloat("StrokeRadius", mStrokeRadius);
        bundle.putFloat("StrokeWidth", mStrokeWidth);
        bundle.putFloat("TextSize", mTextSize);
        bundle.putInt("SelectedColor", mSelectedColor);
        bundle.putSerializable("SelectedTab", (HashSet<Integer>) mSelectedTabs);
        bundle.putBoolean("AllowMultiTab", mAllowMultiTab);
        return bundle;
    }
    /*======================================save and restore======================================*/

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            mStrokeRadius = bundle.getFloat("StrokeRadius");
            mStrokeWidth = bundle.getFloat("StrokeWidth");
            mTextSize = bundle.getFloat("TextSize");
            mSelectedColor = bundle.getInt("SelectedColor");
            mSelectedTabs = (HashSet<Integer>) bundle.getSerializable("SelectedTab");
            mAllowMultiTab = bundle.getBoolean("AllowMultiTab");
            super.onRestoreInstanceState(bundle.getParcelable("View"));
        } else {
            super.onRestoreInstanceState(state);
        }
    }

    /**
     * called when a tab is switched
     */
    public interface OnSwitchListener {
        void onSwitch(int position, String tabText, boolean isSelected);
    }
}
