package com.duke.clockview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.Calendar;
import java.util.Date;

public class ClockPointer extends View {

    private Thread refreshThread;//刷新时间线程

    private float mWidth = 1000;//当宽为wrap_content时，默认的宽度
    private float mHeight = 1000;//当高为wrap_content时，默认的高度

    private float refresh_time = 16;//秒针刷新的时间
    private float width_hour = 20;//时针宽度
    private float width_minutes = 10;//分针刻度宽度
    private float width_second = 5;//秒针刻度宽度
    private float width_circle = 5;//表盘最外圆圈的宽度

    private double millSecond, second, minute, hour;//获取当前的时间参数（毫秒，秒，分钟，小时）



    private float density_second = 0.85f;//秒针长度比例
    private float density_minute = 0.70f;//分针长度比例
    private float density_hour = 0.45f;//时针长度比例

    private Paint paintSecond;

    private Paint paintMinute;

    private Paint paintHour;


    public ClockPointer(Context context) {
        this(context, null, 0);
    }

    public ClockPointer(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClockPointer(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaints();
    }

    private void initPaints() {
        paintSecond = new Paint();
        paintSecond.setAntiAlias(true);
        paintSecond.setStrokeWidth(width_second);
        paintSecond.setColor(Color.RED);

        paintMinute = new Paint();
        paintMinute.setAntiAlias(true);
        paintMinute.setStrokeWidth(width_minutes);

        paintHour = new Paint();
        paintHour.setAntiAlias(true);
        paintHour.setStrokeWidth(width_hour);

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
//        super.onDraw(canvas);
        // 获取宽高参数
        this.mWidth = Math.min(getWidth(), getHeight());
        this.mHeight = Math.max(getWidth(), getHeight());
        //获取时间
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(System.currentTimeMillis()));
        millSecond = calendar.get(Calendar.MILLISECOND);
        second = calendar.get(Calendar.SECOND);
        minute = calendar.get(Calendar.MINUTE);
        hour = calendar.get(Calendar.HOUR);

        //绘制秒针
        drawSecond(canvas, paintSecond);

        //绘制分针
        drawMinute(canvas, paintMinute);

        //绘制时针
        drawHour(canvas, paintHour);

    }

    //绘制秒针
    private void drawSecond(Canvas canvas, Paint paint) {
        /*
         * 这里对秒针的角度进行了细微处理
         * 如果刷新时间小于1秒，则我们的角度计算添加了毫秒
         * 如果刷新时间大于1秒，则去除了毫秒进行角度计算
         */
        float degree = refresh_time >= 1000 ? (int) (second * 360 / 60) : (float) (second * 360 / 60 + millSecond / 1000 * 360 / 60);
        Log.i("View", degree + ":" + second);
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
