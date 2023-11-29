/*
 *  2023.
 * Alexey Rasskazov
 */

package com.hzbhd.alexross.subarulan2.controls.seekBar;


import android.content.Context;
import androidx.databinding.InverseBindingListener;
import androidx.databinding.InverseBindingMethod;
import androidx.databinding.InverseBindingMethods;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;

import com.hzbhd.alexross.subarulan2.R;

/**
 * Created by Alexey Rasskazov.
 */
@InverseBindingMethods({@InverseBindingMethod(type = VerticalSeekBar.class, attribute = "progress", event = "progressAttrChanged")})
public class VerticalSeekBar extends  androidx.appcompat.widget.AppCompatSeekBar{
    private int progress = 0;
    private InverseBindingListener mInverseBindingListener;

    private Rect rect;
    private Paint paint;
    private int seekbar_height;

    public int barColor;

    public VerticalSeekBar(Context context) {
        super(context);
    }

    public VerticalSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public VerticalSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        rect = new Rect();
        paint = new Paint();
        seekbar_height = this.getHeight();
        barColor=Color.GRAY;

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

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(h, w, oldh, oldw);
    }

    @Override
    public synchronized void setProgress(int progress)  // it is necessary for calling setProgress on click of a button
    {
        super.setProgress(progress);
        onSizeChanged(getWidth(), getHeight(), 0, 0);
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec);
        setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
    }


    protected void onDraw(Canvas c) {
        int numberOfIntervals = getMax();
        int barWidth=4;
        c.rotate(-90);
        c.translate(-getHeight(), 0);
        rect.set(0 + getPaddingLeft(),
                getWidth() / 2 - barWidth/2,
                getHeight() - getPaddingRight(),
                (getWidth() / 2 + barWidth/2));

        paint.setColor(barColor);

        c.drawRect(rect, paint);

        if (this.getProgress() > numberOfIntervals/2) {
            int height = getHeight() - getPaddingLeft() - getPaddingRight();
            rect.set(getHeight() / 2,
                    (getWidth() / 2) - barWidth/2,
                    (int) (getHeight() / 2 + (getHeight() / numberOfIntervals) * (getProgress() - numberOfIntervals/2)*(height*1f/getHeight())),
                    getWidth() / 2 + barWidth/2);

            paint.setColor(ContextCompat.getColor(this.getContext(),R.color.background_primary));
            c.drawRect(rect, paint);

        }

        if (this.getProgress() < numberOfIntervals/2) {
            int height = getHeight() - getPaddingLeft() - getPaddingRight();
            rect.set((int) ((getHeight()) / 2 - ((getHeight() / numberOfIntervals) * (numberOfIntervals/2 - getProgress())*(height*1f/getHeight()))),
                    (getWidth() / 2) - barWidth/2,
                    getHeight() / 2,
                    getWidth() / 2 + barWidth/2);

            paint.setColor(ContextCompat.getColor(this.getContext(),R.color.background_primary));
            c.drawRect(rect, paint);
        }


        Drawable thumb = getThumb();

        if (thumb != null) {
            final int saveCount = c.save();
            // Translate the padding. For the x, we need to allow the thumb to
            // draw in its extra space
            c.translate(getPaddingLeft() - getThumbOffset(), getPaddingTop());
            thumb.draw(c);
            c.restoreToCount(saveCount);
        }
    }

    @Override
    public synchronized boolean onTouchEvent(MotionEvent event) {
        return false;
    }


/*
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
                setProgress(getMax() - (int) (getMax() * event.getY() / getHeight()));
                onSizeChanged(getWidth(), getHeight(), 0, 0);
                break;

            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return true;
    }*/
}
