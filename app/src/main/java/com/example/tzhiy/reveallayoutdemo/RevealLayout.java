package com.example.tzhiy.reveallayoutdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;

/**
 * Created by tzhiy on 2017/6/26.
 */

public class RevealLayout extends LinearLayout implements Runnable {

    private Paint paint;

    private View mTouchTarget;

    private int centerX;
    private int centerY;

    private int revealRaidus;

    private int targetHeight;
    private int targetWidth;

    private int minBetweenWidthAndHeight;
    private int maxBetweenWidthAndHeight;

    private boolean shouldDoAnimation;
    private boolean isPressed;

    private int mRevealRadiusGap;

    private int[] mLocationInScreen = new int[2];

    private int maxRevealRadius;
    private long INVALIDATE_DURATION=50;

    private DispatchUpTouchEventRunable dispatchUpTouchEventRunable = new DispatchUpTouchEventRunable();

    private boolean isDispatched;


    public RevealLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RevealLayout(Context context) {
        this(context, null);
    }

    public RevealLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(getResources().getColor(R.color.cirleColor));
        this.getLocationOnScreen(mLocationInScreen);
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int x = (int) ev.getRawX();
        int y = (int) ev.getRawY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                View touchTarget = getTouchTarget(this, x, y);
                if (touchTarget != null && touchTarget.isClickable() && touchTarget.isEnabled()) {
                    mTouchTarget = touchTarget;
                    initParametersForChild(ev, touchTarget);
                    postInvalidateDelayed(INVALIDATE_DURATION);
                }

                isDispatched=super.dispatchTouchEvent(ev);
                break;

            case MotionEvent.ACTION_UP:
                isPressed=false;
                postInvalidateDelayed(INVALIDATE_DURATION);
                dispatchUpTouchEventRunable.event=ev;
                postDelayed(dispatchUpTouchEventRunable,40);
                isDispatched=true;
                break;
            case MotionEvent.ACTION_CANCEL :
                    isPressed = false;
                    postInvalidateDelayed(INVALIDATE_DURATION);
                    isDispatched=super.dispatchTouchEvent(ev);

                break;

        }


        return isDispatched;
    }

    private void initParametersForChild(MotionEvent ev, View touchTarget) {
        centerX = (int) ev.getX();
        centerY = (int) ev.getY();
        targetHeight = touchTarget.getMeasuredHeight();
        targetWidth = touchTarget.getMeasuredWidth();

        minBetweenWidthAndHeight = Math.min(targetHeight, targetWidth);
        maxBetweenWidthAndHeight = Math.max(targetHeight,targetWidth);

        revealRaidus =0;
        shouldDoAnimation=true;
        isPressed = true;
        mRevealRadiusGap = minBetweenWidthAndHeight / 8;

        int[] location = new int[2];
        touchTarget.getLocationOnScreen(location);
        int left = location[0] - mLocationInScreen[0];
        int transformedCenterX = centerX - left;
        int maxWidthRadius = Math.max(transformedCenterX, targetWidth - transformedCenterX);

        int top = location[0]-mLocationInScreen[0];
        int transformedCenterY = centerY-top;
        int maxHeightRadius = Math.max(transformedCenterY,targetHeight-transformedCenterY);

        maxRevealRadius = Math.max(maxHeightRadius,maxWidthRadius);


    }

    private View getTouchTarget(View view, int x, int y) {
        View target = null;
        ArrayList<View> touchables = view.getTouchables();
        for (View touchable : touchables) {
            if (isTouchPointInView(touchable, x, y)) {
                target = touchable;
            }
        }

        return target;
    }

    private boolean isTouchPointInView(View touchable, int x, int y) {
        int[] locations = new int[2];
        touchable.getLocationOnScreen(locations);
        int left = locations[0];
        int top = locations[1];
        int right = left + touchable.getMeasuredWidth();
        int bottom = top + touchable.getMeasuredHeight();

        if (touchable.isClickable() && x >= left && x <= right && y >= top && y <= bottom) {
            return true;
        }
        return false;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if(!shouldDoAnimation||targetWidth<=0||targetHeight<=0||mTouchTarget==null){
            return;
        }

        if(revealRaidus>minBetweenWidthAndHeight/2){
            revealRaidus+=mRevealRadiusGap*4;
        }else{
            revealRaidus+=mRevealRadiusGap;
        }
        this.getLocationOnScreen(mLocationInScreen);
        int [] locations = new int[2];
        mTouchTarget.getLocationOnScreen(locations);
        int left = locations[0]-mLocationInScreen[0];
        int top = locations[1]-mLocationInScreen[1];
        int right = left+mTouchTarget.getMeasuredWidth();
        int bottom = top+mTouchTarget.getMeasuredHeight();

        canvas.save();
        canvas.clipRect(left,top,right,bottom);
        canvas.drawCircle(centerX,centerY,revealRaidus,paint);
        canvas.restore();

        if(revealRaidus<=maxRevealRadius){
            postInvalidateDelayed(INVALIDATE_DURATION,left,top,right,bottom);
        }else if(!isPressed){
            shouldDoAnimation=false;
            postInvalidateDelayed(INVALIDATE_DURATION,left,top,right,bottom);
        }

    }

    @Override
    public boolean performClick() {
        postDelayed(this,40);
        return true;
    }

    @Override
    public void run() {
        super.performClick();
    }

    private class DispatchUpTouchEventRunable implements Runnable{
        public MotionEvent event;

        @Override
        public void run() {
            if (mTouchTarget == null || !mTouchTarget.isEnabled()) {
                return;
            }

            if (isTouchPointInView(mTouchTarget, (int)event.getRawX(), (int)event.getRawY())) {
                mTouchTarget.performClick();
            }
        }
    }


}
