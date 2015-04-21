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

    private TapPattern firstPattern = null;
    private TapPattern secondPattern = null;

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

    public void setFirstPattern(TapPattern firstPattern) {
        this.firstPattern = firstPattern;
    }

    public void setSecondPattern(TapPattern secondPattern) {
        this.secondPattern = secondPattern;
    }

    public void setDrawSurfaceHolder()
    {
        drawSurfaceHolder = getHolder();
        drawSurfaceHolder.addCallback(this);
    }


    public synchronized void init()
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

        // Paints
        private Paint transparentPaint = new Paint();
        private Paint redPaint = new Paint();
        private Paint redPaint2 = new Paint();
        private Paint grayPaint = new Paint();


        // Pattern being tapped
        TapPattern tapPattern = null;
        int timeIndex = 0;

        public DrawThread(SurfaceHolder paramContext, Context paramHandler, Handler arg4)
        {
            surfaceHolder = paramContext;
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);


            // Transparent paint
            transparentPaint.setColor(Color.TRANSPARENT);
            transparentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

            // Red paint
            redPaint.setColor(Color.RED);
            redPaint.setAlpha(80);
            redPaint.setStrokeWidth(3);

            // Red paint 2
            redPaint2.setColor(Color.RED);
            redPaint2.setAlpha(20);

            // Gray paint
            grayPaint.setColor(Color.rgb(230,230,230));

        }

        public void doDraw(Canvas canvas)
        {
            canvasHeight = canvas.getHeight();
            canvasWidth = canvas.getWidth();

            // Clear screen
            canvas.drawPaint(transparentPaint);

            // Offsets
            float circleYOffset = canvasHeight/2.0f;
            float circleRadius = canvasWidth*0.03f;
            float backgroundHeight = circleRadius * 9;

            // Draw background rectangle
            canvas.drawRect(0, circleYOffset-backgroundHeight, canvasWidth, circleYOffset+backgroundHeight, grayPaint);

            // Draw horizontal line
            canvas.drawLine(0, circleYOffset, canvasWidth, circleYOffset, redPaint);

            //Draw first pattern
            if (firstPattern != null) {
                ArrayList<Double> circlePositions = firstPattern.tapPositions;
                int circleXMax = firstPattern.pattern.length;
                float circleXScale = (float)canvasWidth/(float)circleXMax;
                for (int i=0 ; i<circlePositions.size() ; i++) {
                    float x = (float)(circlePositions.get(i) * circleXScale);
                    float y = circleYOffset;

                    canvas.drawCircle(x, y, circleRadius, redPaint);
                }
            }

            //Draw second pattern
            if (secondPattern != null) {
                ArrayList<Double> circlePositions = secondPattern.tapPositions;
                int circleXMax = secondPattern.pattern.length;
                float circleXScale = (float)canvasWidth/(float)circleXMax;
                for (int i=0 ; i<circlePositions.size() ; i++) {
                    float x = (float)(circlePositions.get(i) * circleXScale);
                    float y = circleYOffset;

                    canvas.drawCircle(x, y, circleRadius, redPaint);
                }
            }

            if (accelerometerSampler != null && accelerometerSampler.isSampling) {
                // Copy absAccelerationBuffer
                double[] absAccelerationBufferCopy = accelerometerSampler.getAbsAccelerationBufferSafe();

                if (absAccelerationBufferCopy.length < 1) {
                    return;
                }

                synchronized (accelerometerSampler) {
                    tapPattern = TapPattern.createPattern(accelerometerSampler.getAbsAccelerationBuffer(), accelerometerSampler.samplingFrequency, 5, tapPattern);
                    timeIndex = ((AccelerometerRecorder) accelerometerSampler).timeIndexRecorded();
                }
                double[] patternBuffer = tapPattern.pattern;

                /*
                // Draw absAccelerationBufferCopy
                double[] jounce = TapPattern.getJounce(absAccelerationBufferCopy);
                int patternBufferXMax = absAccelerationBufferCopy.length;
                float patternBufferXScale = (float)canvasWidth/(float)patternBufferXMax;
                float patternBufferYOffset = canvasHeight/5.0f * 1.0f;
                float patternBufferYScale = -(canvasHeight/5.0f)/10.0f;
                for (int x=0 ; x<canvasWidth-1 ; x++) {
                    float x0 = x;
                    float y0 = (float) (patternBuffer[(int)(x0/patternBufferXScale)%patternBufferXMax]*patternBufferYScale + patternBufferYOffset);
                    float x1 = x+1;
                    float y1 = (float) (patternBuffer[(int)(x1/patternBufferXScale)%patternBufferXMax]*patternBufferYScale + patternBufferYOffset);

                    canvas.drawLine(x0, y0, x1, y1, redPaint);
                }
                */

                // Draw circles of ongoing taps
                ArrayList<Double> circlePositions = tapPattern.tapPositions;
                int circleXMax = tapPattern.pattern.length;
                float circleXScale = (float)canvasWidth/(float)circleXMax;
                for (int i=0 ; i<circlePositions.size() ; i++) {
                    float x = (float)(circlePositions.get(i) * circleXScale - (circleXMax - timeIndex)*circleXScale);
                    float y = circleYOffset;

                    canvas.drawCircle(x, y, circleRadius, redPaint);
                }
                // Draw vertical time line
                canvas.drawRect(0, circleYOffset-backgroundHeight, timeIndex*circleXScale, circleYOffset+backgroundHeight, redPaint2);
            }

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

                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                    }

                }
            }
            threadExists = false;
        }

    }


}
