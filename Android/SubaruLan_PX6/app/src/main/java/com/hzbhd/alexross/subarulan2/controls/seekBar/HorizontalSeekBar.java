/*
 *  2023.
 * Alexey Rasskazov
 */

package com.hzbhd.alexross.subarulan2.controls.seekBar;

import android.content.Context;
import androidx.databinding.InverseBindingListener;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;

/**
 * Created by Alexey Rasskazov.
 */

public class HorizontalSeekBar extends androidx.appcompat.widget.AppCompatSeekBar{
    private int progress = 0;
    private InverseBindingListener mInverseBindingListener;

    private Rect rect;
    private Paint paint;
    private int seekbar_width;


    public int barColor;


    public HorizontalSeekBar(Context context) {
        super(context);
    }

    public HorizontalSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public HorizontalSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        rect = new Rect();
        paint = new Paint();
        seekbar_width = this.getWidth();
        barColor = Color.DKGRAY;

        this.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                progress = i;
                setProgress(i);
                if (mInverseBindingListener != null) {
                    mInverseBindingListener.onChange();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public void setProgressAttrChanged(InverseBindingListener inverseBindingListener) {
        if (inverseBindingListener != null) {
            mInverseBindingListener = inverseBindingListener;
        }
    }


    @Override
    public synchronized void setProgress(int progress)  // it is necessary for calling setProgress on click of a button
    {
        super.setProgress(progress);
        onSizeChanged(getWidth(), getHeight(), 0, 0);
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public synchronized boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    protected void onDraw(Canvas c) {
        int barHeight = 4;

        rect.set(0 + getPaddingLeft(),
                getHeight() / 2 - getPaddingTop() - barHeight / 2,
                getWidth() - getPaddingRight(),
                (getHeight() / 2 + barHeight / 2));

        paint.setColor(barColor);
        int numberOfIntervals = getMax();
        c.drawRect(rect, paint);

        int width = getWidth() - getPaddingLeft() - getPaddingRight();

        if (this.getProgress() > numberOfIntervals/2) {

            rect.set(getWidth() / 2,
                    (getHeight() / 2) - barHeight / 2,
                    (int) (getWidth() / 2 + (getWidth() / numberOfIntervals) * (getProgress() - numberOfIntervals/2) * (width * 1f / getWidth())),
                    getHeight() / 2 + barHeight / 2);

            paint.setColor(Color.BLUE);
            c.drawRect(rect, paint);
        }

        if (this.getProgress() < numberOfIntervals/2) {

            rect.set((int) ((getWidth()) / 2 - ((getWidth() / numberOfIntervals) * (numberOfIntervals/2 - getProgress()) * (width * 1f / getWidth()))),
                    (getHeight() / 2) - barHeight / 2,
                    getWidth() / 2,
                    getHeight() / 2 + barHeight / 2);

            paint.setColor(Color.BLUE);
            c.drawRect(rect, paint);
        }


        Drawable thumb = getThumb();

        if (thumb != null) {
            final int saveCount = c.save();
            c.translate(getPaddingLeft() - getThumbOffset(), getPaddingTop());
            thumb.draw(c);
            c.restoreToCount(saveCount);
        }
    }
}
