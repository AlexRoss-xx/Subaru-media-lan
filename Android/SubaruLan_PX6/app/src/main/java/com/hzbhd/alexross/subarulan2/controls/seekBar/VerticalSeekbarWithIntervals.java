/*
 *  2023.
 * Alexey Rasskazov
 */

package com.hzbhd.alexross.subarulan2.controls.seekBar;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.hzbhd.alexross.subarulan2.R;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


public class VerticalSeekbarWithIntervals extends LinearLayout {
    private RelativeLayout RelativeLayout_Intrevals = null;
    private RelativeLayout RelativeLayout_Deviders = null;
    private VerticalSeekBar VerticalSeekBar = null;

    private int WidthMeasureSpec = 0;
    private int HeightMeasureSpec = 0;
    private boolean isAlignmentResetOnLayoutChange;


    public VerticalSeekbarWithIntervals(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.vert_seekbar_with_intervals, this);
    }

    public static float dpToPx(Context context, float valueInDp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    private Activity getActivity() {
        return (Activity) getContext();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        if (changed) {
            if (!isAlignmentResetOnLayoutChange) {
                alignIntervals();

                // We've changed the intervals layout, we need to refresh.
                RelativeLayout_Intrevals.measure(WidthMeasureSpec, HeightMeasureSpec);
                RelativeLayout_Intrevals.layout(RelativeLayout_Intrevals.getLeft(), RelativeLayout_Intrevals.getTop(), RelativeLayout_Intrevals.getRight(), RelativeLayout_Intrevals.getBottom());

                RelativeLayout_Deviders.measure(WidthMeasureSpec, HeightMeasureSpec);
                RelativeLayout_Deviders.layout(RelativeLayout_Deviders.getLeft(), RelativeLayout_Deviders.getTop(), RelativeLayout_Deviders.getRight(), RelativeLayout_Deviders.getBottom());

            }
        }
    }


    private void alignIntervals() {

        if (getSeekbar() != null && getRelativeLayout().getChildCount()>0) {
            int heightOfSeekbarThumb = getSeekbarThumbHeight();
            int thumbOffset = heightOfSeekbarThumb / 2;

            int heightOfSeekbar = getRelativeLayout().getHeight();
            int firstIntervalHeight = getRelativeLayout().getChildAt(0).getHeight();
            int remainingPaddableHeight = heightOfSeekbar - firstIntervalHeight;

            int numberOfIntervals = getSeekbar().getMax();
            int maximumHeightOfEachInterval = remainingPaddableHeight / numberOfIntervals;

            alignFirstInterval(6);
            alignIntervalsInBetween(maximumHeightOfEachInterval);
            //   alignLastInterval(0, maximumHeightOfEachInterval);
            isAlignmentResetOnLayoutChange = true;

            alignFirstDevider();
            alignDeviders();
        }
    }

    private int getSeekbarThumbHeight() {
        return 25;
    }

    private void alignFirstInterval(int offset) {
        TextView firstInterval = (TextView) getRelativeLayout().getChildAt(0);
        int padding=  Math.round((getSeekbar().getPaddingRight() - firstInterval.getHeight() / 2f));

        firstInterval.setPadding(0, padding, 0, 0);
    }

    private void alignIntervalsInBetween(int maximumHeightOfEachInterval) {
        int seekbarHeight = getSeekbar().getHeight() - getSeekbar().getPaddingRight() - getSeekbar().getPaddingLeft();

        for (int index = 0; index < (getRelativeLayout().getChildCount() - 1); index++) {
            TextView textViewInterval = (TextView) getRelativeLayout().getChildAt(index);
            int height = textViewInterval.getHeight();
            float intheight = (seekbarHeight + height - height * (getRelativeLayout().getChildCount())) / (1f * (getRelativeLayout().getChildCount() - 1));

            int padding;
            if ((index & 1) == 0) {
                padding = (int) intheight;
            } else {
                padding = Math.round(intheight);
            }

            textViewInterval.setPadding(0, textViewInterval.getPaddingTop(), 0, padding);
        }
    }


    private void alignLastInterval(int offset, int maximumWidthOfEachInterval) {
        int lastIndex = getRelativeLayout().getChildCount() - 1;

        TextView lastInterval = (TextView) getRelativeLayout().getChildAt(lastIndex);
        int height = lastInterval.getHeight();

        int padding = Math.round(maximumWidthOfEachInterval - height - offset);
        lastInterval.setPadding(0, 0, 0, padding);
    }


    private void alignFirstDevider() {
        ImageView imgView = (ImageView) getRelativeLayout_Deviders().getChildAt(0);
        int height = imgView.getHeight();
        int padding = Math.round(height);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) imgView.getLayoutParams();
        params.topMargin = Math.round((getSeekbar().getPaddingRight() - height / 2f));
        imgView.setLayoutParams(params);
    }

    private void alignDeviders() {

        int seekbarHeight = getSeekbar().getHeight() - getSeekbar().getPaddingRight() - getSeekbar().getPaddingLeft();
        // Don't align the first or last interval.
        for (int index = 0; index < (getRelativeLayout_Deviders().getChildCount() - 1); index++) {
            ImageView imgView = (ImageView) getRelativeLayout_Deviders().getChildAt(index);
            int height = imgView.getHeight();
            float intheight = (seekbarHeight + height - height * (getRelativeLayout_Deviders().getChildCount())) / (1f * (getRelativeLayout_Deviders().getChildCount() - 1));
            int padding;
            if ((index & 1) == 0) {
                padding = (int) intheight;
            } else {
                padding = Math.round(intheight);
            }

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) imgView.getLayoutParams();
            params.bottomMargin = padding;
            imgView.setLayoutParams(params);
        }
    }


    protected synchronized void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        WidthMeasureSpec = widthMeasureSpec;
        HeightMeasureSpec = heightMeasureSpec;

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public int getProgressValue() {
        return getSeekbar().getProgress();
    }

    public void setIntervals(List<String> intervals) {
        displayIntervals(intervals);
        getSeekbar().setMax(intervals.size() - 1);
    }


    public void setProgress(int progress) {
        getSeekbar().setProgress( getSeekbar().getMax()/2+progress);
    }

    public VerticalSeekBar GetSeekBar() {
        return getSeekbar();
    }

    public void setProgressValue(int progress) {
        setProgress(progress);
    }


    private void displayIntervals(List<String> intervals) {
        int idOfPreviousInterval = 0;
        int idOfPreviousDevider = 0;

        if (getRelativeLayout().getChildCount() == 0) {
            for (int i = intervals.size() - 1; i >= 0; i--) {
                TextView textViewInterval = createInterval(intervals.get(i));
                alignTextViewToRightOfPreviousInterval(textViewInterval, idOfPreviousInterval);

                idOfPreviousInterval = textViewInterval.getId();

                getRelativeLayout().addView(textViewInterval);
            }

            for (int i = 0; i < intervals.size(); i++) {
                ImageView imageViewDevider = createDevider();
                alignDeviderViewToRightOfPreviousInterval(imageViewDevider, idOfPreviousDevider);

                idOfPreviousDevider = imageViewDevider.getId();

                getRelativeLayout_Deviders().addView(imageViewDevider);
            }
        }
    }

    private TextView createInterval(String interval) {
        View textBoxView = (View) LayoutInflater.from(getContext())
                .inflate(R.layout.seekbar_with_intervals_labels, null);

        TextView textView = (TextView) textBoxView
                .findViewById(R.id.textViewInterval);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            textView.setId(View.generateViewId());
        else
            textBoxView.setId(generateViewId());

        textView.setText(interval);

        return textView;
    }

    private ImageView createDevider() {
        View boxView = (View) LayoutInflater.from(getContext())
                .inflate(R.layout.seekbar_deviders_v, null);

        ImageView imgView = (ImageView) boxView
                .findViewById(R.id.imageViewDevider);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            imgView.setId(View.generateViewId());
        else
            imgView.setId(generateViewId());

        return imgView;
    }


    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    /**
     * Generate a value suitable for use in {@link #setId(int)}.
     * This value will not collide with ID values generated at build time by aapt for R.id.
     *
     * @return a generated ID value
     */
    public static int generateViewId() {
        for (; ; ) {
            final int result = sNextGeneratedId.get();

            int newValue = result + 1;
            if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
            if (sNextGeneratedId.compareAndSet(result, newValue)) {
                return result;
            }
        }
    }

    public void setAlignmentResetOnLayoutChange() {
        alignIntervals();
        RelativeLayout_Intrevals.measure(WidthMeasureSpec, HeightMeasureSpec);
        RelativeLayout_Intrevals.layout(RelativeLayout_Intrevals.getLeft(), RelativeLayout_Intrevals.getTop(), RelativeLayout_Intrevals.getRight(), RelativeLayout_Intrevals.getBottom());

        RelativeLayout_Deviders.measure(WidthMeasureSpec, HeightMeasureSpec);
        RelativeLayout_Deviders.layout(RelativeLayout_Deviders.getLeft(), RelativeLayout_Deviders.getTop(), RelativeLayout_Deviders.getRight(), RelativeLayout_Deviders.getBottom());

    }

    private void alignTextViewToRightOfPreviousInterval(TextView textView, int idOfPreviousInterval) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

        if (idOfPreviousInterval > 0) {
            params.addRule(RelativeLayout.BELOW, idOfPreviousInterval);
        }
        textView.setGravity(Gravity.RIGHT);
        textView.setLayoutParams(params);

    }


    private void alignDeviderViewToRightOfPreviousInterval(ImageView imgView, int idOfPreviousInterval) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

        if (idOfPreviousInterval > 0) {
            params.addRule(RelativeLayout.BELOW, idOfPreviousInterval);
        }
        imgView.setLayoutParams(params);
    }


    public void setOnSeekBarChangeListener(final OnSeekBarChangeListener onSeekBarChangeListener) {

        getSeekbar().setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                for (int i = 0; i < getRelativeLayout().getChildCount(); i++) {
                    TextView tv = (TextView) getRelativeLayout().getChildAt(i);
                    if (i == seekBar.getProgress())
                        tv.setTextColor(getResources().getColor(R.color.colorPrimary));
                    else
                        tv.setTextColor(getResources().getColor(R.color.white_overlay));
                }
                onSeekBarChangeListener.onProgressChanged(seekBar, progress, fromUser);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                onSeekBarChangeListener.onStartTrackingTouch(seekBar);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                onSeekBarChangeListener.onStopTrackingTouch(seekBar);
            }
        });
    }

    private RelativeLayout getRelativeLayout() {
        if (RelativeLayout_Intrevals == null) {
            RelativeLayout_Intrevals = (RelativeLayout) findViewById(R.id.intervals);
        }

        return RelativeLayout_Intrevals;
    }

    private RelativeLayout getRelativeLayout_Deviders() {
        if (RelativeLayout_Deviders == null) {
            RelativeLayout_Deviders = (RelativeLayout) findViewById(R.id.intervals_d);
        }

        return RelativeLayout_Deviders;
    }


    private VerticalSeekBar getSeekbar() {
        if (VerticalSeekBar == null) {
            VerticalSeekBar = (VerticalSeekBar) findViewById(R.id.vertseekbar);
        }

        return VerticalSeekBar;
    }
}