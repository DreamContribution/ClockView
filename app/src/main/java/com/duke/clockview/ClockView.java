package com.duke.clockview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Duke on 15/9/25.
 */
public class ClockView extends View {

    private Thread refreshThread;//刷新时间线程

    private float refresh_time = 1000;//秒针刷新的时间


    private float width_circle = 5;//表盘最外圆圈的宽度
    private float width_longer = 5;//整点刻度宽度
    private float width_shorter = 3;//非整点刻度宽度
    private float length_longer = 60;//整点刻度长度
    private float length_shorter = 30;//非整点刻度长度
    private float text_size = 60;//表盘中文字大小

    private float radius_center = 15;//表盘正中心的半径长度 radius_center

    private float width_hour = 20;//时针宽度
    private float width_minutes = 10;//分针刻度宽度
    private float width_second = 8;//秒针刻度宽度


    private float density_second = 0.85f;//秒针长度比例
    private float density_minute = 0.70f;//分针长度比例
    private float density_hour = 0.45f;//时针长度比例


    private float mWidth = 1000;//当宽为wrap_content时，默认的宽度
    private float mHeight = 1000;//当高为wrap_content时，默认的高度

    private double millSecond, second, minute, hour;//获取当前的时间参数（毫秒，秒，分钟，小时）

    public ClockView(Context context) {
        this(context, null, 0);
    }

    public ClockView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClockView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.clock);
        /**
         * 这里所有的R.styleable.OOXX,所有的OOXX中，前面的名字必须为：styleable中name属性_属性名称
         * 但是在xml中使用的时候，不能有styleable中的name属性
         */
        width_circle = ta.getDimension(R.styleable.clock_width_circle, 5);//表盘最外圆圈的宽度
        width_longer = ta.getDimension(R.styleable.clock_width_longer, 5);//整点刻度宽度
        width_shorter = ta.getDimension(R.styleable.clock_width_shorter, 3);//非整点刻度宽度
        length_longer = ta.getDimension(R.styleable.clock_length_longer, 60);//整点刻度长度
        length_shorter = ta.getDimension(R.styleable.clock_length_shorter, 30);//非整点刻度长度
        text_size = ta.getDimension(R.styleable.clock_text_size, 60);//表盘中文字大小
        radius_center = ta.getDimension(R.styleable.clock_radius_center, 15);//表盘正中心的半径长度 radius_center
        width_hour = ta.getDimension(R.styleable.clock_width_hour, 20);//时针宽度
        width_minutes = ta.getDimension(R.styleable.clock_density_minute, 10);//分针刻度宽度
        width_second = ta.getDimension(R.styleable.clock_density_second, 8);//秒针刻度宽度
        density_second = ta.getFloat(R.styleable.clock_density_second, 0.85f);//秒针长度比例
        density_minute = ta.getFloat(R.styleable.clock_density_minute, 0.70f);//分针长度比例
        density_hour = ta.getFloat(R.styleable.clock_density_hour, 0.45f);//时针长度比例
        refresh_time = ta.getFloat(R.styleable.clock_refresh_time, 1000);
        ta.recycle();
    }


    private void init() {
        refreshThread = new Thread();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        //添加了适应wrap_content的界面计算
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthSpecMode == MeasureSpec.AT_MOST && heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension((int) mWidth, (int) mHeight);
        } else if (widthSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension((int) mWidth, heightSpecSize);
        } else if (heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSpecSize, (int) mHeight);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 获取宽高参数
        this.mWidth = Math.min(getWidth(), getHeight());
//        this.mWidth = getWidth();
//        this.mHeight = getHeight();
        this.mHeight = Math.max(getWidth(), getHeight());
        //获取时间
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(System.currentTimeMillis()));
        millSecond = calendar.get(Calendar.MILLISECOND);
        second = calendar.get(Calendar.SECOND);
        minute = calendar.get(Calendar.MINUTE);
        hour = calendar.get(Calendar.HOUR);

        // 画外圆
        Paint paintCircle = new Paint();
        paintCircle.setStyle(Paint.Style.STROKE);
        paintCircle.setAntiAlias(true);
        paintCircle.setStrokeWidth(width_circle);
        canvas.drawCircle(mWidth / 2,
                mHeight / 2, mWidth / 2 - width_circle, paintCircle);


        // 画刻度
        Paint painDegree = new Paint();
        painDegree.setAntiAlias(true);
        float lineLength = 0;
        for (int i = 0; i < 60; i++) {
            if (i % 5 == 0) {
                painDegree.setStrokeWidth(width_longer);
                lineLength = length_longer;
            } else {
                painDegree.setStrokeWidth(width_shorter);
                lineLength = length_shorter;
            }
            canvas.drawLine(mWidth / 2, mHeight / 2 - mWidth / 2 + width_circle, mWidth / 2, mHeight / 2 - mWidth / 2 + lineLength, painDegree);
            canvas.rotate(360 / 60, mWidth / 2, mHeight / 2);
        }

        painDegree.setTextSize(text_size);
        String targetText[] = getContext().getResources().getStringArray(R.array.clock);

        //绘制时间文字
        float startX = mWidth / 2 - painDegree.measureText(targetText[1]) / 2;
        float startY = mHeight / 2 - mWidth / 2 + 120;
        float textR = (float) Math.sqrt(Math.pow(mWidth / 2 - startX, 2) + Math.pow(mHeight / 2 - startY, 2));

        for (int i = 0; i < 12; i++) {
            float x = (float) (startX + Math.sin(Math.PI / 6 * i) * textR);
            float y = (float) (startY + textR - Math.cos(Math.PI / 6 * i) * textR);
            if (i != 11 && i != 10 && i != 0) {
                y = y + painDegree.measureText(targetText[i]) / 2;
            } else {
                x = x - painDegree.measureText(targetText[i]) / 4;
                y = y + painDegree.measureText(targetText[i]) / 4;
            }
            canvas.drawText(targetText[i], x, y, painDegree);
        }


        //绘制秒针
        Paint paintSecond = new Paint();
        paintSecond.setAntiAlias(true);
        paintSecond.setStrokeWidth(width_second);
        paintSecond.setColor(Color.RED);
        drawSecond(canvas, paintSecond);

        //绘制分针
        Paint paintMinute = new Paint();
        paintMinute.setAntiAlias(true);
        paintMinute.setStrokeWidth(width_minutes);
        drawMinute(canvas, paintMinute);

        //绘制时针
        Paint paintHour = new Paint();
        paintHour.setAntiAlias(true);
        paintHour.setStrokeWidth(width_hour);
        drawHour(canvas, paintHour);

        // 画圆心
        Paint paintPointer = new Paint();
        paintPointer.setAntiAlias(true);
        paintPointer.setStyle(Paint.Style.FILL);
        canvas.drawCircle(mWidth / 2, mHeight / 2, radius_center, paintPointer);
    }

    //绘制秒针
    private void drawSecond(Canvas canvas, Paint paint) {
//        float degree = (float) (second * 360 / 60 + millSecond / 1000 * 360 / 60);
        /*
         * 这里对秒针的角度进行了细微处理
         * 如果刷新时间小于1秒，则我们的角度计算添加了毫秒
         * 如果刷新时间大于1秒，则去除了毫秒进行角度计算
         */
        float degree = refresh_time > 1000 ? (float) (second * 360 / 60) : (float) (second * 360 / 60 + millSecond / 1000 * 360 / 60);
        canvas.rotate(degree, mWidth / 2, mHeight / 2);
        canvas.drawLine(mWidth / 2, mHeight / 2, mWidth / 2, mHeight / 2 - (mWidth / 2 - width_circle) * density_second, paint);
        canvas.rotate(-degree, mWidth / 2, mHeight / 2);
    }

    //绘制分针
    private void drawMinute(Canvas canvas, Paint paint) {
        float degree = (float) (minute * 360 / 60);
        canvas.rotate(degree, mWidth / 2, mHeight / 2);
        canvas.drawLine(mWidth / 2, mHeight / 2, mWidth / 2, mHeight / 2 - (mWidth / 2 - width_circle) * density_minute, paint);
        canvas.rotate(-degree, mWidth / 2, mHeight / 2);
    }

    //绘制时针
    private void drawHour(Canvas canvas, Paint paint) {
        float degreeHour = (float) hour * 360 / 12;
        float degreeMinut = (float) minute / 60 * 360 / 12;
        float degree = degreeHour + degreeMinut;
        canvas.rotate(degree, mWidth / 2, mHeight / 2);
        canvas.drawLine(mWidth / 2, mHeight / 2, mWidth / 2, mHeight / 2 - (mWidth / 2 - width_circle) * density_hour, paint);
        canvas.rotate(-degree, mWidth / 2, mHeight / 2);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        //在添加到Activity的时候启动线程
        refreshThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    //设置更新界面的刷新时间
                   SystemClock.sleep((long) refresh_time);
                    postInvalidate();
                }
            }
        });
        refreshThread.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        //停止刷新线程
        refreshThread.interrupt();
    }
}