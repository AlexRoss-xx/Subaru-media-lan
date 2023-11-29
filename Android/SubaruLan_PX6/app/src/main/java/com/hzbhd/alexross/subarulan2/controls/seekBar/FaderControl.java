/*
 *  2023.
 * Alexey Rasskazov
 */

package com.hzbhd.alexross.subarulan2.controls.seekBar;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.hzbhd.alexross.subarulan2.R;


public class FaderControl extends RelativeLayout {
    private ImageView imageView = null;

    private int fadeValue=0;
    private int balanceValue=0;

    private int WidthMeasureSpec = 0;
    private int HeightMeasureSpec = 0;
    private boolean isAlignmentResetOnLayoutChange;

    private Rect rect;
    private Paint paint;

    public int barColor;

    public FaderControl(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.fader_layout, this);

        rect = new Rect();
        paint = new Paint();

        barColor= Color.GRAY;
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
               /* int padding_w=  Math.round(this.getWidth()/18);
                int padding_h=  Math.round(this.getHeight()/18);


                int paddingW=  Math.round(this.getWidth()/2);
                int paddingH=  Math.round(this.getHeight()/2);

                int imageW=  Math.round( getImage().getWidth()/2);
                int imageH=  Math.round( getImage().getHeight()/2);*/
            }
        }
    }


    public void balanceValue(int value) {
        balanceValue=value;
        setImage();
    }


    public void fadeValue(int value) {
        fadeValue=value;
        setImage();
    }

    private void setImage() {
        int imageW=  Math.round( getImage().getWidth()- getImage().getPaddingLeft());
        int imageH=  Math.round( getImage().getHeight()-getImage().getPaddingTop());

        int padding_w=  Math.round((this.getWidth()-imageW)/18);
        int padding_h=  Math.round((this.getHeight()-imageH)/18);

        LayoutParams params = new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
        );
        params.setMargins(padding_w*(9+balanceValue), (this.getHeight()-imageH)-padding_h*(9+fadeValue), 0, 0);
        getImage().setLayoutParams(params);
    }


    private ImageView getImage() {
        if (imageView == null) {
            imageView = (ImageView) findViewById(R.id.imageViewCenter);
        }

        return imageView;
    }
}















