package com.clam314.pathmeasuretry;

import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;

/**
 * Created by clam314 on 2017/3/3
 */

public class StickyCircleView extends View{
    private static final String TAG = StickyCircleView.class.getSimpleName();
    private float defaultRadius = 50f;
    private float MaxMoveDistance = 1000f;
    private int viewWidth;
    private int viewHeight;
    private static final int circleColor = Color.BLACK;

    private Circle circleStart, circleEnd;
    private PointF pStartA,pStartB,pEndA,pEndB,pControlO, pControlP;
    private PointF downPoint,movePoint;
    private float mCircleDistance;
    private float mMoveDistance;
    private Paint mPaint;
    private Path mPath;

    private ValueAnimator animator;


    public StickyCircleView(Context context) {
        super(context);
        initAll();
    }

    public StickyCircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAll();
    }

    public StickyCircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAll();
    }

    private void initAll(){
        initCircle();
        initPaint();
        initAnimation();
    }

    private void initCircle(){
        circleStart = new Circle(0,0,defaultRadius);
        circleEnd = new Circle(0,0,defaultRadius);

        pStartA = new PointF();
        pStartB = new PointF();
        pEndA = new PointF();
        pEndB = new PointF();
        pControlP = new PointF();
        pControlO = new PointF();
        downPoint = new PointF();
        movePoint = new PointF();
    }

    private void initPaint(){
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(1);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(circleColor);

        mPath = new Path();
    }

    private void initAnimation(){
        animator = new ValueAnimator();
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                movePoint = (PointF) animation.getAnimatedValue();
                invalidate();
            }
        });
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = w;
        viewHeight = h;
        circleEnd.centerPoint.x = circleStart.centerPoint.x = viewWidth/2;
        circleEnd.centerPoint.y = circleStart.centerPoint.y = 50f + defaultRadius;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        calculateCircleByMove();
        canvas.drawCircle(circleStart.centerPoint.x, circleStart.centerPoint.y, circleStart.radius,mPaint);
        canvas.drawCircle(circleEnd.centerPoint.x, circleEnd.centerPoint.y, circleEnd.radius,mPaint);
        if(calculateCurves(circleStart,circleEnd)){
            drawCurves(canvas);
        }
    }

    private void drawCurves(Canvas canvas){
        mPath.reset();
        mPath.moveTo(pStartA.x,pStartA.y);
        mPath.quadTo(pControlO.x, pControlO.y, pEndA.x, pEndA.y);
        mPath.lineTo(pEndB.x, pEndB.y);
        mPath.quadTo(pControlP.x, pControlP.y, pStartB.x, pStartB.y);
        mPath.close();
        canvas.drawPath(mPath,mPaint);
    }

    private void calculateCircleByMove(){
        mMoveDistance = (float) Math.sqrt(Math.pow(downPoint.x - movePoint.x, 2) + Math.pow(downPoint.y - movePoint.y, 2));
        if(mMoveDistance == 0) return;
        float scale = mMoveDistance/MaxMoveDistance;
        circleStart.radius = defaultRadius * (1- scale);
        circleEnd.radius = defaultRadius * scale;

        circleEnd.centerPoint.x = circleStart.centerPoint.x + movePoint.x - downPoint.x;
        circleEnd.centerPoint.y = circleStart.centerPoint.y + movePoint.y - downPoint.y;

        Log.d(TAG,"mMoveDistance: "+ mMoveDistance+ " EndPoint: "+circleEnd.centerPoint.toString());
    }

    private boolean calculateCurves(Circle circleStart,Circle circleEnd){
        float startRadius = circleStart.radius;
        float endRadius = circleEnd.radius;
        float startX = circleStart.centerPoint.x;
        float startY = circleStart.centerPoint.y;
        float endX= circleEnd.centerPoint.x;
        float endY = circleEnd.centerPoint.y;

        mCircleDistance = (float) Math.sqrt(Math.pow(startX - endX, 2) + Math.pow(startY - endY, 2));
        if(mCircleDistance == 0){
            return false;
        }

        float cos = (startX - endX)/mCircleDistance;
        float sin = (startY - endY)/mCircleDistance;

        float ax = startX - startRadius * sin;
        float ay = startY + startRadius * cos;
        pStartA.x = ax;
        pStartA.y = ay;

        float bx = startX + startRadius * sin;
        float by = startY - startRadius * cos;
        pStartB.x = bx;
        pStartB.y = by;

        float cx = endX - endRadius * sin;
        float cy = endY + endRadius * cos;
        pEndA.x = cx;
        pEndA.y = cy;

        float dx = endX + endRadius * sin;
        float dy = endY - endRadius * cos;
        pEndB.x = dx;
        pEndB.y = dy;

        float ox = cx + mCircleDistance /2 * cos;
        float oy = cy + mCircleDistance /2 * sin;
        pControlO.x = ox;
        pControlO.y = oy;

        float px = dx + mCircleDistance /2 * cos;
        float py = dy + mCircleDistance /2 * sin;
        pControlP.x = px;
        pControlP.y = py;

        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                downPoint.x = x;
                downPoint.y = y;
                movePoint.set(downPoint);
                break;
            case MotionEvent.ACTION_MOVE:
                movePoint.x = x;
                movePoint.y = y;
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                animator.setObjectValues(movePoint,downPoint);
                animator.setEvaluator(new PointEvaluator());
                animator.setDuration(3000);
                animator.start();
                break;
        }
        return true;
    }

    private class PointEvaluator implements TypeEvaluator<PointF>{
        @Override
        public PointF evaluate(float fraction, PointF startValue, PointF endValue) {
            float sX = endValue.x;
            float sY = endValue.y;
            float eX = startValue.x;
            float eY = startValue.y;

            float newEndX = eX - fraction * (eX - sX);
            float newEndY = (newEndX - sX) * (eX - sX)/(eY - sY) + sY;
            return new PointF(newEndX,newEndY);
        }
    }

    static class Circle{
        PointF centerPoint;
        float radius;
        Circle(float centerX,float centerY,float radius){
            centerPoint = new PointF(centerX,centerY);
            this.radius = radius;
        }
    }
}
