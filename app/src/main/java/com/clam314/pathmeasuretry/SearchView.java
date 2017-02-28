package com.clam314.pathmeasuretry;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by clam314 on 2017/2/28
 */

public class SearchView extends View {
    private Paint mPaint;
    private int mViewWidth;
    private int mViewHeight;

    private State mCurrentState = State.NONE;

    private Path pathSearch;
    private Path pathCircle;
    private PathMeasure mMeasure;

    private static final int defaultDuration = 2000;

    private ValueAnimator mStartingAnimator;
    private ValueAnimator mSearchingAnimator;
    private ValueAnimator mEndingAnimator;

    private float mAnimatorValue = 0;

    private ValueAnimator.AnimatorUpdateListener mUpdateListener;
    private Animator.AnimatorListener mAnimatorListener;

    private boolean isOver = false;
    private int count = 0;

    private Handler mAnimatorHandler;

    public SearchView(Context context) {
        this(context,null);
    }

    public SearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAll();
    }

    public SearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAll();
    }

    private void initAll(){
        initPaint();
        initPath();
        initListener();
        initHandler();
        initAnimator();

        mCurrentState = State.STARING;
        mStartingAnimator.start();
    }

    private void initPaint(){
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.WHITE);
        mPaint.setStrokeWidth(15);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setAntiAlias(true);
    }

    private void initPath(){
        pathSearch = new Path();
        pathCircle = new Path();
        mMeasure = new PathMeasure();

        RectF oval1 = new RectF(-50,-50,50,50);
        pathSearch.addArc(oval1,45,-359.9f);

        RectF oval2 = new RectF(-100,-100,100,100);
        pathCircle.addArc(oval2,45,-359.9f);

        float[] pos = new float[2];

        mMeasure.setPath(pathCircle,false);
        mMeasure.getPosTan(0,pos,null);

        pathSearch.lineTo(pos[0],pos[1]);
        Log.i("TAG", "pos=" + pos[0] + ":" + pos[1]);
    }

    private void initListener(){
        mUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mAnimatorValue = (float)animation.getAnimatedValue();
                invalidate();
            }
        };
        mAnimatorListener = new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mAnimatorHandler.sendEmptyMessage(0);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        };
    }

    private void initHandler(){
        mAnimatorHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (mCurrentState){
                    case STARING:
                        isOver = false;
                        mCurrentState = State.SEARCHING;
                        mStartingAnimator.removeAllListeners();
                        mSearchingAnimator.start();
                        Log.i("TAG", State.STARING.toString());
                        break;
                    case SEARCHING:
                        if(!isOver){
                            mSearchingAnimator.start();
                            count++;
                            if(count > 2){
                                isOver = true;
                            }
                        }else {
                            mCurrentState = State.ENDING;
                            mEndingAnimator.start();
                        }
                        Log.i("TAG", State.SEARCHING.toString());
                        break;
                    case ENDING:
                        mCurrentState = State.NONE;
                        Log.i("TAG", State.NONE.toString());
                        break;
                }
            }
        };
    }

    private void initAnimator(){
        mStartingAnimator = ValueAnimator.ofFloat(0,1).setDuration(defaultDuration);
        mSearchingAnimator = ValueAnimator.ofFloat(0,1).setDuration(defaultDuration);
        mEndingAnimator = ValueAnimator.ofFloat(0,1).setDuration(defaultDuration);

        mStartingAnimator.addUpdateListener(mUpdateListener);
        mSearchingAnimator.addUpdateListener(mUpdateListener);
        mEndingAnimator.addUpdateListener(mUpdateListener);

        mStartingAnimator.addListener(mAnimatorListener);
        mSearchingAnimator.addListener(mAnimatorListener);
        mEndingAnimator.addListener(mAnimatorListener);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mViewWidth = w;
        mViewHeight = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        setLayerType(View.LAYER_TYPE_SOFTWARE,null);
        drawSearch(canvas);
    }

    private void drawSearch(Canvas canvas){
        mPaint.setColor(Color.WHITE);
        canvas.translate(mViewWidth/2, mViewHeight/2);
        canvas.drawColor(Color.parseColor("#0082D7"));

        switch (mCurrentState){
            case NONE:
                canvas.drawPath(pathSearch,mPaint);
                break;
            case STARING:
                mMeasure.setPath(pathSearch,false);
                Path dst = new Path();
                mMeasure.getSegment(mMeasure.getLength()*mAnimatorValue,mMeasure.getLength(),dst,true);
                canvas.drawPath(dst,mPaint);
                break;
            case SEARCHING:
                mMeasure.setPath(pathCircle,false);
                Path dst2 = new Path();
                float stop = mMeasure.getLength()*mAnimatorValue;
                float start = (float)(stop - (0.5 - Math.abs(mAnimatorValue - 0.5)) * 200f);
                mMeasure.getSegment(start,stop,dst2,true);
                canvas.drawPath(dst2,mPaint);
                break;
            case ENDING:
                mMeasure.setPath(pathSearch,false);
                Path dst3 = new Path();
                mMeasure.getSegment(mMeasure.getLength()*mAnimatorValue,mMeasure.getLength(),dst3,true);
                canvas.drawPath(dst3,mPaint);
                break;
        }
    }

    public static enum State{
        NONE,STARING,SEARCHING,ENDING
    }
}
