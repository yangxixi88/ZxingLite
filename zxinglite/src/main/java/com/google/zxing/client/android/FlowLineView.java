package com.google.zxing.client.android;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.camera.CameraManager;

import java.util.ArrayList;
import java.util.List;

import static android.hardware.camera2.params.TonemapCurve.POINT_SIZE;

/**
 * Created by Lan Long on 2017/10/12.
 * email: 5789492@qq.com
 */

public class FlowLineView extends View implements AnimeViewCallback{

    private static final String TAG = "FlowLineView";
    private Paint mMaskPaint;
    private final int mMaskColor = Color.argb(32,0,0,0);
    private final int mTextColor = Color.parseColor("#CCCCCC");
    private static final int CURRENT_POINT_OPACITY = 0xA0;
    private CameraManager mCameraManager;
    private Path mLeftShadePath = new Path();
    private Path mRightShadePath = new Path();
    private int mBitmapCenter;
    private Paint mTextPaint;
    private Paint mPointPaint;
    private final int textMarinTop = dp2px(70);

    private List<ResultPoint> possibleResultPoints;
    private List<ResultPoint> lastPossibleResultPoints;

    public FlowLineView(Context context) {
        super(context);
        init();
    }

    public FlowLineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FlowLineView(Context context,AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private Path mPath = new Path();
    private Path mInnerPath = new Path();
    private Bitmap mBitmap;
    private Canvas mBitmapCanvas;
    private Paint mPaint = new Paint();
    private Paint mBitmapPaint = new Paint();
    private ValueAnimator mAnimator;

    private void init() {

        CornerPathEffect cornerPathEffect = new CornerPathEffect(10);
        mMaskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mMaskPaint.setColor(mMaskColor);

        mPointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPointPaint.setColor(Color.WHITE);
        mPointPaint.setStrokeWidth(3);
//        mMaskPaint.setPathEffect(cornerPathEffect);


        mPaint.setStrokeWidth(6);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.WHITE);
        mPaint.setPathEffect(cornerPathEffect);

        mBitmapPaint.setStrokeWidth(4);
        mBitmapPaint.setAntiAlias(true);
        mBitmapPaint.setStyle(Paint.Style.STROKE);
        mBitmapPaint.setPathEffect(cornerPathEffect);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextSize(dp2px(14));

        possibleResultPoints = new ArrayList<>(5);
        lastPossibleResultPoints = null;


    }



    private float startLinePoint = 0.75f;
    private float endLinePoint = 0.5f;
    private float offsetLinePoint = 0f;

    int[] colorArray;
    float[] pathArray;
boolean isInit = false;
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(mCameraManager == null){
            return;
        }
        Rect frame = mCameraManager.getFramingRect();
        Rect previewFrame = mCameraManager.getFramingRectInPreview();
        if (frame == null || previewFrame == null) {

            return;
        }else if(!isInit){
            isInit = !isInit;
            makeShadeShop(new RectF(frame),mLeftShadePath,true,canvas);
            makeShadeShop(new RectF(frame),mRightShadePath,false,canvas);
//            makePolygon(new RectF(5, 5, 445, 445), mPath);
//            makePolygon(new RectF(frame.left+20, frame.top+20, frame.right-20, frame.bottom-20), mInnerPath);
            makePolygon(new RectF(0, 0, frame.right-frame.left+40, frame.right-frame.left+40), mPath);
            makePolygon(new RectF(frame), mInnerPath);
            mBitmap = Bitmap.createBitmap((frame.right-frame.left)+80, (frame.bottom-frame.top)+80, Bitmap.Config.ARGB_8888);
            mBitmapCanvas = new Canvas(mBitmap);
            mBitmapCenter = (frame.right-frame.left)/2;
            start();
            Log.e(TAG, "onDraw: frame——h"+ frame.width()+"frame——h"+frame.height());
            Log.e(TAG, "onDraw: previewFrame——h"+ previewFrame.width()+"previewFrame——h"+previewFrame.height());
            return;
        }
        canvas.drawPath(mInnerPath, mPaint);
        final int width = canvas.getWidth();
//        final int height = canvas.getHeight();
        // 除了中间的矩形识别区域，其他区域都将蒙上一层半透明的图层
//        canvas.drawRect(0, 0, width, frame.top, maskPaint);
//        canvas.drawRect(0, frame.top, frame.left, frame.bottom, maskPaint);
//        canvas.drawRect(frame.right, frame.top, width, frame.bottom, maskPaint);
//        canvas.drawRect(0, frame.bottom, width, height, maskPaint);

        canvas.drawPath(mLeftShadePath,mMaskPaint);
        canvas.drawPath(mRightShadePath,mMaskPaint);

        if (mBitmap != null) {
            canvas.drawBitmap(mBitmap, frame.left-20, frame.top-20, null);
        }
        String text = "将AR标识放入框内，即可自动扫描";
        canvas.drawText(text, (width - mTextPaint.measureText(text)) / 2, frame.bottom + textMarinTop, mTextPaint);

        List<ResultPoint> currentPossible = possibleResultPoints;
        List<ResultPoint> currentLast = lastPossibleResultPoints;
        int frameLeft = frame.left;
        int frameTop = frame.top;

        if (currentPossible.isEmpty()) {
            lastPossibleResultPoints = null;
        } else {
            possibleResultPoints = new ArrayList<>(5);
            lastPossibleResultPoints = currentPossible;
            mPointPaint.setAlpha(CURRENT_POINT_OPACITY);
            synchronized (currentPossible) {
                for (ResultPoint point : currentPossible) {
                    mPointPaint.setStyle(Paint.Style.FILL);
                    canvas.drawCircle(frameLeft + (int) (point.getX() * 1),
                            frameTop + (int) (point.getY() * 1),
                            6, mPointPaint);

                    mPointPaint.setStyle(Paint.Style.STROKE);
                    canvas.drawCircle(frameLeft + (int) (point.getX() * 1),
                            frameTop + (int) (point.getY() * 1),
                            18, mPointPaint);
                }
            }
        }
        if (currentLast != null) {
            mPointPaint.setAlpha(CURRENT_POINT_OPACITY / 2);
            synchronized (currentLast) {
                float radius = POINT_SIZE / 2.0f;
                for (ResultPoint point : currentLast) {
                    canvas.drawCircle(frameLeft + (int) (point.getX() * 1),
                            frameTop + (int) (point.getY() * 1),
                            18, mPointPaint);
                }
            }
        }

    }

    @Override
    public void addPossibleResultPoint(ResultPoint point) {
        Log.e(TAG, "addPossibleResultPoint: "+point.toString() );
        List<ResultPoint> points = possibleResultPoints;
        synchronized (points) {
            points.add(point);
            int size = points.size();
            if (size > 20) {
                // trim it
                points.subList(0, size - 20 / 2).clear();
            }
        }
    }

    public void setCameraManager(CameraManager cameraManager) {
        this.mCameraManager = cameraManager;
//        start();
    }

    @Override
    public void drawViewfinder() {
        Log.e(TAG, "drawViewfinder: " );
    }


    /**
     * 绘制多边形
     * @param rect
     * @param path
     */
    private void makePolygon(RectF rect, Path path) {
        float r = (rect.right - rect.left) / 2;
        float mX = (rect.right + rect.left) / 2;
        float mY = (rect.top + rect.bottom) / 2;
        Log.e(TAG, "makePolygon: left:"+rect.left+"top:"+rect.top+"right:"+rect.right+"bottom:"+rect.bottom );
        path.moveTo(mX,mY-r);//1
        path.lineTo(mX+r*sin(60),mY-r/2);//3
        path.lineTo(mX+r*sin(60),mY+r/2);//5
        path.lineTo(mX,mY+r);//6
        path.lineTo(mX-r*sin(60),mY+r/2);//4
        path.lineTo(mX-r*sin(60),mY-r/2);//2
        path.close();

    }

    /**
     * 绘制阴影区域
     * @param rect  识别区域矩形
     * @param path  设置的路径
     * @param isLeft    是否是左边
     * @param canvas    画布（获取宽高使用）
     */
    private void makeShadeShop(RectF rect,Path path,boolean isLeft,Canvas canvas){
        float r = (rect.right - rect.left) / 2;
        float mX = (rect.right + rect.left) / 2;
        float mY = (rect.top + rect.bottom) / 2;
        final int width = canvas.getWidth();
        final int height = canvas.getHeight();
        if(isLeft){
            path.moveTo(0,0);//左上
            path.lineTo(width/2,0);//顶部中心点
            path.lineTo(mX,mY-r);//1
            path.lineTo(mX-r*sin(60),mY-r/2);//2
            path.lineTo(mX-r*sin(60),mY+r/2);//4
            path.lineTo(mX,mY+r);//6
            path.lineTo(width/2,height);//底部中心点
            path.lineTo(0,height);//左下
            path.close();
        }else{
            path.moveTo(width,0);//右上
            path.lineTo(width/2,0);//顶部中心点
            path.lineTo(mX,mY-r);//1
            path.lineTo(mX+r*sin(60),mY-r/2);//3
            path.lineTo(mX+r*sin(60),mY+r/2);//5
            path.lineTo(mX,mY+r);//6
            path.lineTo(width/2,height);//底部中心点
            path.lineTo(width,height);//右下
            path.close();
        }


    }
    /**
     * Math.sin的参数为弧度，使用起来不方便，重新封装一个根据角度求sin的方法
     * @param num 角度
     * @return
     */
    float sin(int num){
        return (float) Math.sin(num* Math.PI/180);
    }

    /**
     * 与sin同理
     */
    float cos(int num){
        return (float) Math.cos(num* Math.PI/180);
    }
    public void start() {
        mAnimator = ValueAnimator.ofInt(360, 0);
        mAnimator.setDuration(5 * 360);
        mAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mAnimator.setInterpolator(new TimeInterpolator() {

            @Override
            public float getInterpolation(float input) {
                return input;
            }
        });
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Integer value = (Integer) animation.getAnimatedValue();
                startLinePoint = value / 360f;
                if(startLinePoint>=0.25f){
                    startLinePoint = startLinePoint-0.25f;
                }else{
                    startLinePoint = startLinePoint+0.76f;
                }//1——0
                endLinePoint = startLinePoint + 0.5f;
                if (startLinePoint > 0.5f) {
                    offsetLinePoint = startLinePoint - 0.5f;
                    int splitColor = Color.argb((int) (255 * (offsetLinePoint / 0.5f)), 255, 255, 255);
                    colorArray =
                            new int[]{splitColor, 0x00FFFFFF, 0, 0, 0xFFFFFFFF, splitColor};
                    pathArray =
                            new float[]{0f, offsetLinePoint, offsetLinePoint, startLinePoint, startLinePoint, 1f};
                } else {
                    colorArray =
                            new int[]{0, 0, 0xFFFFFFFF, 0x00FFFFFF, 0, 0};
                    pathArray =
                            new float[]{0f, startLinePoint, startLinePoint, endLinePoint, endLinePoint, 1f};
                }

                SweepGradient mShader = new SweepGradient(mBitmapCenter, mBitmapCenter, colorArray, pathArray);
                mBitmapPaint.setShader(mShader);
                mBitmapCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                mBitmapCanvas.drawPath(mPath, mBitmapPaint);
                postInvalidate();
            }
        });

        mAnimator.start();
    }

    private int dp2px(int dp) {
        float density = getContext().getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }

    public void Pause(){

        if(mAnimator.isRunning()){
            mAnimator.pause();
        }else{
            mAnimator.start();
        }
    }

}