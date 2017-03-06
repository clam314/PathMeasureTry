package com.clam314.pathmeasuretry;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.FloatEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * Created by clam314 on 2017/3/3
 */

public class StickyCircleView extends View{
    private static final String TAG = StickyCircleView.class.getSimpleName();
    private final static float defaultRadius = 50f;
    private final static float defaultPadding = 15f;

    private static final long LOADING_DURATION = 2000;
    private static final long STICKY_DURATION = 300;

    private float MaxMoveDistance = 1000f;
    private int viewWidth;
    private int viewHeight;
    private static final int circleColor = Color.parseColor("#00ffad");

    private Circle circleStart, circleEnd;
    private PointF pStartA,pStartB,pEndA,pEndB,pControlO, pControlP;
    private PointF downPoint,movePoint;
    private float mCircleDistance;
    private float mMoveDistance;
    private float loadCircleRadius;
    private Paint mPaint, mLoadPaint;
    private Path mPath;
    private Path mLoadPath;
    private PathMeasure pathMeasure;

    private ValueAnimator stickyAnimator;
    private ValueAnimator loadAnimator;
    private FloatEvaluator evaluator;

    private float mLoadAnimatorValue;
    private float mScale;

    private boolean loading = false;
    private boolean needStickeyAdmin = true;
    private OnReloadListener mReloadListener;

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
        initPath();
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

        mLoadPaint = new Paint();
        mLoadPaint.setAntiAlias(true);
        mLoadPaint.setStrokeWidth(5);
        mLoadPaint.setStyle(Paint.Style.STROKE);
        mLoadPaint.setStrokeCap(Paint.Cap.ROUND);
        mLoadPaint.setColor(Color.WHITE);
    }

    private void initPath(){
        mPath = new Path();

        pathMeasure = new PathMeasure();
        mLoadPath = new Path();
        loadCircleRadius = defaultRadius - defaultPadding;
        RectF circle = new RectF(-loadCircleRadius, -loadCircleRadius, loadCircleRadius, loadCircleRadius);
        mLoadPath.addArc(circle, 0, 359.9f);
    }

    private void initAnimation(){
        evaluator = new FloatEvaluator();
        stickyAnimator = new ValueAnimator();
        stickyAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        stickyAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float newDistance = (float) animation.getAnimatedValue();
                float distance = getDistanceBetweenTwoPoints(downPoint.x,downPoint.y,movePoint.x,movePoint.y);
                float cos = (movePoint.x - downPoint.x)/distance;
                float sin = (movePoint.y - downPoint.y)/distance;
                movePoint.x = downPoint.x + newDistance * cos;
                movePoint.y = downPoint.y + newDistance * sin;
                invalidate();
            }
        });
        stickyAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if(loading){
                    loadAnimator.start();
                    if(mReloadListener != null) mReloadListener.onReload();
                }
            }
        });

        loadAnimator = ValueAnimator.ofFloat(0,1).setDuration(LOADING_DURATION);
        loadAnimator.setRepeatCount(5);
        loadAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mLoadAnimatorValue = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
    }

    public void setOnReloadListener(OnReloadListener listener){
        this.mReloadListener = listener;
    }

    public boolean isLoading(){
        return loading;
    }

    public void stopReload(){
        if(loadAnimator.isRunning()){
            loadAnimator.cancel();
        }
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
        setLayerType(View.LAYER_TYPE_SOFTWARE,null);
        calculateCircleByMove();
        canvas.drawCircle(circleStart.centerPoint.x, circleStart.centerPoint.y, circleStart.radius,mPaint);
        canvas.drawCircle(circleEnd.centerPoint.x, circleEnd.centerPoint.y, circleEnd.radius,mPaint);
        if(calculateCurves(circleStart,circleEnd)){
            drawCurves(canvas);
        }
//        drawPoint(downPoint,canvas);
//        drawPoint(movePoint,canvas);
        if(loadAnimator.isRunning()){
            drawLoading(canvas);
        }else {
            drawLoadingNormal(canvas);
        }

    }

    private void drawPoint(PointF point,Canvas canvas){
        Paint pointPaint = new Paint();
        pointPaint.setAntiAlias(true);
        pointPaint.setColor(Color.RED);
        pointPaint.setStrokeWidth(10f);
        pointPaint.setStyle(Paint.Style.FILL);

        canvas.drawPoint(point.x,point.y,pointPaint);
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

    private void drawLoadingNormal(Canvas canvas){
        canvas.save();
        canvas.translate(circleStart.centerPoint.x,circleStart.centerPoint.y);
        canvas.scale(1 - mScale,1 - mScale);
        canvas.rotate(360 * mScale);
        pathMeasure.setPath(mLoadPath,false);

        float[] pos = new float[2];
        float[] tan = new float[2];
        float stop = pathMeasure.getLength() * 0.75f;
        float start = 0;
        pathMeasure.getPosTan(stop,pos,tan);
        float degrees =(float)(Math.atan2(tan[1],tan[0])*180/Math.PI);

        Matrix matrix = new Matrix();
        Path triangle = new Path();
        triangle.moveTo(pos[0] - 5, pos[1] + 5);
        triangle.lineTo(pos[0],pos[1]);
        triangle.lineTo(pos[0] + 5, pos[1] + 5);
        triangle.close();
        matrix.setRotate(degrees+90, pos[0],pos[1]);

        Path showPath = new Path();
        showPath.addPath(triangle,matrix);
        pathMeasure.getSegment(start,stop,showPath,true);

        canvas.drawPath(showPath, mLoadPaint);
        canvas.restore();
    }

    private void drawLoading(Canvas canvas){
        canvas.save();
        canvas.translate(circleStart.centerPoint.x, circleStart.centerPoint.y);
        canvas.scale(1 - mScale,1 - mScale);
        pathMeasure.setPath(mLoadPath,false);
        Path newPath = new Path();
        float stop = pathMeasure.getLength() * mLoadAnimatorValue;
        float start = (float)(stop - (0.5 - Math.abs(mLoadAnimatorValue - 0.5)) * 200f);
        pathMeasure.getSegment(start,stop,newPath,true);
        canvas.drawPath(newPath, mLoadPaint);
        canvas.restore();
    }

    private void calculateCircleByMove(){
        mMoveDistance = getDistanceBetweenTwoPoints(downPoint.x,downPoint.y,movePoint.x,movePoint.y);
        if(mMoveDistance <= 0) return;
        mScale = mMoveDistance/MaxMoveDistance;
        circleStart.radius = defaultRadius * (1- mScale);
        circleEnd.radius = defaultRadius * mScale;

        circleEnd.centerPoint.x = circleStart.centerPoint.x + movePoint.x - downPoint.x;
        circleEnd.centerPoint.y = circleStart.centerPoint.y + movePoint.y - downPoint.y;
    }

    private boolean calculateCurves(Circle circleStart,Circle circleEnd){
        float startRadius = circleStart.radius;
        float endRadius = circleEnd.radius;
        float startX = circleStart.centerPoint.x;
        float startY = circleStart.centerPoint.y;
        float endX= circleEnd.centerPoint.x;
        float endY = circleEnd.centerPoint.y;

        mCircleDistance = getDistanceBetweenTwoPoints(startX,startY,endX,endY);
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

    private static float getDistanceBetweenTwoPoints(float p1x, float ply, float p2x, float p2y){
        return (float) Math.sqrt(Math.pow(p1x - p2x, 2) + Math.pow(ply - p2y, 2));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                if(!stickyAnimator.isRunning() && !loadAnimator.isRunning()){
                    downPoint.x = x;
                    downPoint.y = y;
                    movePoint.set(downPoint);
                    resetLoadAnimator();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if(!stickyAnimator.isRunning() && !loadAnimator.isRunning()){
                    movePoint.x = x;
                    movePoint.y = y;
                    float distanceMove = getDistanceBetweenTwoPoints(downPoint.x,downPoint.y,movePoint.x,movePoint.y);
                    if(inLoadArea(distanceMove)){
                        loading = true;
                        executeAnimator(distanceMove);
                    }
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if(!stickyAnimator.isRunning() && !loadAnimator.isRunning()){
                    movePoint.x = x;
                    movePoint.y = y;
                    float distanceUp = getDistanceBetweenTwoPoints(downPoint.x,downPoint.y,movePoint.x,movePoint.y);
                    if(inLoadArea(distanceUp)){
                       loading = true;
                    }
                    executeAnimator(distanceUp);
                }
                break;
        }
        return true;
    }

    private void resetLoadAnimator(){
        loading = false;
    }

    private boolean inLoadArea(float distance){
        return distance <= MaxMoveDistance*0.75 && distance >= MaxMoveDistance * 0.33;
    }

    private void executeAnimator(float distance){
        if(distance == 0) return;
        stickyAnimator.setObjectValues(distance,0);
        stickyAnimator.setEvaluator(evaluator);
        stickyAnimator.setDuration(STICKY_DURATION);
        stickyAnimator.start();
    }

    static class Circle{
        PointF centerPoint;
        float radius;
        Circle(float centerX,float centerY,float radius){
            centerPoint = new PointF(centerX,centerY);
            this.radius = radius;
        }
    }

    public interface OnReloadListener{
        void onReload();
    }
}
