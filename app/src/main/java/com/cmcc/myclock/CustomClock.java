package com.cmcc.myclock;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.cmcc.myclock.util.SizeUtil;

public class CustomClock extends View {

    private float mRadius; //表盘外圆半径
    private float mPadding; //表盘与背景边缘间距
    private float mTextSize; //文字（1~12数字）大小
    private float mHourPointWidth; //时针宽度
    private float mMinutePointWidth; //分针宽度
    private float mSecondPointWidth; //秒针宽度
    private int mPointRadius; //指针圆角
    private float mPointEndLength; //指针末尾（越过圆心）长度

    private int mColorLong; //整点刻度颜色
    private int mColorShort; //非整点刻度颜色
    private int mHourPointColor; //时针颜色
    private int mMinutePointColor; //分针颜色
    private int mSecondPointColor; //秒针颜色

    private Paint mPaint; //画笔

    public CustomClock(Context context) {
        this(context, null);
    }

    public CustomClock(Context context, AttributeSet attrs) {
        super(context, attrs);
        obtainStyledAttrs(attrs); //获取自定义的属性
        init();
    }

    public void obtainStyledAttrs(AttributeSet attrs) {
        TypedArray array = null;
        try {
            array = getContext().obtainStyledAttributes(attrs, R.styleable.CustomClock);
            mPadding = array.getDimension(R.styleable.CustomClock_cc_padding, DptoPx(10));
            mTextSize = array.getDimension(R.styleable.CustomClock_cc_text_size, SptoPx(16));
            mHourPointWidth = array.getDimension(R.styleable.CustomClock_cc_hour_pointer_width, DptoPx(5));
            mMinutePointWidth = array.getDimension(R.styleable.CustomClock_cc_minute_pointer_width, DptoPx(3));
            mSecondPointWidth = array.getDimension(R.styleable.CustomClock_cc_second_pointer_width, DptoPx(2));
            mPointRadius = (int) array.getDimension(R.styleable.CustomClock_cc_pointer_corner_radius, DptoPx(10));
            mPointEndLength = array.getDimension(R.styleable.CustomClock_cc_pointer_end_length, DptoPx(10));

            mColorLong = array.getColor(R.styleable.CustomClock_cc_scale_long_color, Color.argb(225, 0, 0, 0));
            mColorShort = array.getColor(R.styleable.CustomClock_cc_scale_short_color, Color.argb(125, 0, 0, 0));
            mMinutePointColor = array.getColor(R.styleable.CustomClock_cc_minute_pointer_color, Color.BLACK);
            mSecondPointColor = array.getColor(R.styleable.CustomClock_cc_second_pointer_color, Color.RED);
        } catch (Exception e) {
            //一旦出现错误全部使用默认值
            mPadding = DptoPx(10);
            mTextSize = SptoPx(16);
            mHourPointWidth = DptoPx(5);
            mMinutePointWidth = DptoPx(3);
            mSecondPointWidth = DptoPx(2);
            mPointRadius = (int) DptoPx(10);
            mPointEndLength = DptoPx(10);

            mColorLong = Color.argb(225, 0, 0, 0);
            mColorShort = Color.argb(125, 0, 0, 0);
            mMinutePointColor = Color.BLACK;
            mSecondPointColor = Color.RED;
        } finally {
            if (array != null) {
                array.recycle();
            }
        }

    }

    //Dp转px
    private float DptoPx(float value) {

        return SizeUtil.Dp2Px(getContext(), value);
    }

    //sp转px
    private float SptoPx(float value) {
        return SizeUtil.Sp2Px(getContext(), value);
    }

    public void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        //设置一个默认大小，避免wrap_content或过大
        int mSize = 1000;  //默认大小

        if(getLayoutParams().width == ViewGroup.LayoutParams.WRAP_CONTENT &&
            getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            setMeasuredDimension(mSize, mSize);
        } else {
//            if(widthMode == MeasureSpec.EXACTLY) {
//                mSize = Math.min(mSize, widthSize);
//            }
//            if(heightMode == MeasureSpec.EXACTLY) {
//                mSize = Math.min(mSize, heightSize);
//            }
            mSize = Math.min(widthSize, heightSize);
        }
        setMeasuredDimension(mSize, mSize);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mRadius = Math.min(w, h)/2 - mPadding;
        mPointEndLength = mRadius/6;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //将坐标原点移动至中心
        canvas.save();
        canvas.translate(getWidth()/2, getHeight()/2);
        drawCircle(canvas); //画表盘外圆
        drawScale(canvas);

        canvas.restore();
    }

    //绘制表盘外圆
    public void drawCircle(Canvas canvas) {
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(0, 0, mRadius, mPaint);
    }

    //绘制刻度
    public void drawScale(Canvas canvas) {
        canvas.save();
        mPaint.setStrokeWidth(DptoPx(1));  //非整点刻度线宽
        int lineLength = 0; //刻度长度
        for(int i = 0; i < 60; i++) {
            if(i % 5 == 0) {
                mPaint.setStrokeWidth(DptoPx(1.5f));
                mPaint.setColor(mColorLong);
                lineLength = 40; //整点刻度长度

                //整点刻度文字1~12
                mPaint.setTextSize(mTextSize);
                String text = ((i/5) == 0 ? 12 : (i/5)) + "";
                Rect textBound = new Rect();
                mPaint.getTextBounds(text, 0, text.length(), textBound);
                mPaint.setColor(Color.BLACK);
                canvas.save();
                canvas.translate(0, -mRadius + mPadding + DptoPx(5) + lineLength + (textBound.bottom - textBound.top));
                canvas.rotate(-6*i);
                canvas.drawText(text, -(textBound.right - textBound.left)/2, (textBound.bottom - textBound.top)/2 , mPaint);
                canvas.restore();
            } else {
                mPaint.setStrokeWidth(DptoPx(1));
                mPaint.setColor(mColorShort);
                lineLength = 30;
            }
            canvas.drawLine(0, -mRadius+mPadding, 0, -mRadius+mPadding+lineLength, mPaint);
            canvas.rotate(6);
        }
        canvas.restore();
    }
}
