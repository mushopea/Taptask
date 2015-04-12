package sg.edu.nus.taptask;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;

import sg.edu.nus.taptask.model.TapPattern;

public class AccelerometerRecordSurfaceView extends SurfaceView implements SurfaceHolder.Callback  {
    private Context drawContext;
    public  DrawThread drawThread;
    private SurfaceHolder drawSurfaceHolder;
    private Boolean threadExists = false;
    public static volatile Boolean drawFlag = false;
    public boolean draw = true;

    private AccelerometerSampler accelerometerSampler = null;

    private static final Handler handler = new Handler(){
        public void handleMessage(Message paramMessage)
        {
        }
    };

    public AccelerometerRecordSurfaceView(Context ctx, AttributeSet attributeSet)
    {
        super(ctx, attributeSet);
        drawContext = ctx;
        this.setBackgroundColor(Color.TRANSPARENT);
        this.setZOrderOnTop(true);
        setDrawSurfaceHolder();
    }

    public void setAccelerationSampler(AccelerometerSampler accelerometerSampler) {
        this.accelerometerSampler = accelerometerSampler;
    }

    public void setDrawSurfaceHolder()
    {
        drawSurfaceHolder = getHolder();
        drawSurfaceHolder.addCallback(this);
    }


    public void init()
    {
        if (!threadExists) {

            drawSurfaceHolder = getHolder();
            drawSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);
            drawSurfaceHolder.addCallback(this);

            drawThread = new DrawThread(drawSurfaceHolder, drawContext, handler);

            drawThread.setName("" +System.currentTimeMillis());
            drawThread.start();
        }
        threadExists = true;
        drawFlag = true;
    }


    public void surfaceChanged(SurfaceHolder paramSurfaceHolder, int paramInt1, int paramInt2, int paramInt3)
    {
        drawThread.setSurfaceSize(paramInt2, paramInt3);
    }


    public void surfaceCreated(SurfaceHolder paramSurfaceHolder)
    {
        init();
    }


    public void surfaceDestroyed(SurfaceHolder paramSurfaceHolder)
    {
        while (true)
        {
            if (!drawFlag)
                return;
            try {
                drawFlag = false;
                drawThread.join();
            } catch (InterruptedException localInterruptedException){

            }
        }
    }

    class DrawThread extends Thread
    {
        private Bitmap bitmap;
        private int canvasHeight = 0;
        private int canvasWidth = 0;
        private SurfaceHolder surfaceHolder;

        public DrawThread(SurfaceHolder paramContext, Context paramHandler, Handler arg4)
        {
            surfaceHolder = paramContext;
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        }

        public void doDraw(Canvas canvas)
        {
            // Copy absAccelerationBuffer
            double[] absAccelerationBufferCopy = accelerometerSampler.getAbsAccelerationBufferSafe();

            canvasHeight = canvas.getHeight();
            canvasWidth = canvas.getWidth();

            // Clear screen
            Paint transparentPaint = new Paint();
            transparentPaint.setColor(Color.TRANSPARENT);
            transparentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            canvas.drawPaint(transparentPaint);


            TapPattern tapPattern = TapPattern.createPattern(accelerometerSampler.getAbsAccelerationBufferSafe(), accelerometerSampler.samplingFrequency, 5);
            double[] patternBuffer = tapPattern.pattern;

            Paint redPaint = new Paint();
            redPaint.setColor(Color.RED);
            redPaint.setAlpha(80);
            redPaint.setStrokeWidth(1);
            int patternBufferXMax = patternBuffer.length;
            float patternBufferXScale = (float)canvasWidth/(float)patternBufferXMax;
            float patternBufferYOffset = canvasHeight/5.0f * 1.0f;
            float patternBufferYScale = -(canvasHeight/5.0f)/10.0f;
            for (int x=0 ; x<canvasWidth-1 ; x++) {
                float x0 = x;
                float y0 = (float) (patternBuffer[(int)(x0/patternBufferXScale)%patternBuffer.length]*patternBufferYScale + patternBufferYOffset);
                float x1 = x+1;
                float y1 = (float) (patternBuffer[(int)(x1/patternBufferXScale)%patternBuffer.length]*patternBufferYScale + patternBufferYOffset);

                canvas.drawLine(x0, y0, x1, y1, redPaint);
            }


            ArrayList<Integer> circlePositions = tapPattern.getCirclePositions();
            int circleXMax = patternBuffer.length;
            float circleXScale = (float)canvasWidth/(float)circleXMax;
            float circleYOffset = canvasHeight/5.0f * 2.0f;
            for (int i=0 ; i<circlePositions.size() ; i++) {
                float x = circlePositions.get(i) / circleXScale;
                float y = circleYOffset;

                canvas.drawCircle(x, y, (float)(canvasWidth*0.03), redPaint);
            }
            canvas.drawLine(0, circleYOffset, canvasWidth, circleYOffset, redPaint);

        }



        public void setSurfaceSize(int canvasWidth, int canvasHeight)
        {
            synchronized (surfaceHolder)
            {
                bitmap = Bitmap.createScaledBitmap(bitmap, canvasWidth, canvasHeight, true);
            }
        }


        public void run()
        {
            while (drawFlag)
            {
                if (draw) {
                    Canvas localCanvas = null;
                    try {
                        localCanvas = surfaceHolder.lockCanvas(null);
                        synchronized (surfaceHolder) {
                            if (localCanvas != null) {
                                doDraw(localCanvas);
                            }
                        }
                    } finally {
                        if (localCanvas != null) {
                            surfaceHolder.unlockCanvasAndPost(localCanvas);
                        }
                    }
                }
            }
        }

    }


}
