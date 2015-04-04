package sg.edu.nus.taptask;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import sg.edu.nus.taptask.AccelerometerSampler;

public class AudioBufferVisualizerSurfaceView extends SurfaceView implements SurfaceHolder.Callback  {
    private Context drawContext;
    public  DrawThread drawThread;
    private SurfaceHolder drawSurfaceHolder;
    private Boolean threadExists = false;
    public static volatile Boolean drawFlag = false;
    public boolean draw = true;

    private short[] audioBuffer = null;

    private AccelerometerSampler accelerometerSampler = null;

    private static final Handler handler = new Handler(){
        public void handleMessage(Message paramMessage)
        {
        }
    };

    public AudioBufferVisualizerSurfaceView(Context ctx, AttributeSet attributeSet)
    {
        super(ctx, attributeSet);
        drawContext = ctx;
        setDrawSurfaceHolder();
    }

    public void setAudioBuffer(short[] buffer) {
        audioBuffer = buffer;
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
        private int time = 0;
        private boolean[] tapArray = null;

        private double maxFFTMagnitudeSum = Double.MIN_VALUE;
        private double minFFTMagnitudeSobelSum = Double.MAX_VALUE;


        public DrawThread(SurfaceHolder paramContext, Context paramHandler, Handler arg4)
        {
            surfaceHolder = paramContext;
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        }

        public void doDraw(Canvas canvas)
        {
            // Copy audioBuffer
            short[] buffer = new short[audioBuffer.length];
            synchronized (audioBuffer) {
                System.arraycopy(audioBuffer, 0, buffer, 0, audioBuffer.length);
            }

            // Copy absAccelerationBuffer
            double[] absAccelerationBufferCopy = accelerometerSampler.getAbsAccelerationBuffer();

            double[] doubleBuffer = FFTHelper.shortToDouble(buffer);
            // Taper window
            doubleBuffer = FFTHelper.realMultiply(doubleBuffer, FFTHelper.planckTaperWindow(doubleBuffer.length, 0.4f));

            canvasHeight = canvas.getHeight();
            canvasWidth = canvas.getWidth();


            // Clear screen
            Paint paint = new Paint();
            paint.setColor(Color.WHITE);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawPaint(paint);

            // Draw raw buffer
            Paint redPaint = new Paint();
            redPaint.setColor(Color.RED);
            redPaint.setStrokeWidth(3);
            int bufferXMax = buffer.length;
            float bufferXScale =(float)canvasWidth/(float)bufferXMax;
            float bufferYOffset = canvasHeight/5.0f;
            float bufferYScale = -(canvasHeight/5.0f)/18000.0f;
            for (int x=0 ; x<canvasWidth-1 ; x++) {
                float x0 = x;
                float y0 = (float)doubleBuffer[(int)(x0/bufferXScale)]*bufferYScale + bufferYOffset;
                float x1 = x+1;
                float y1 = (float)doubleBuffer[(int)(x1/bufferXScale)]*bufferYScale + bufferYOffset;

                canvas.drawLine(x0, y0, x1, y1, redPaint);
            }

            // Perform FFT
            double[] FFTResult = FFTHelper.FFTReal(doubleBuffer);
            double[] FFTResultShifted = FFTHelper.FFTShift(FFTResult);
            double[] FFTMagnitude = FFTHelper.complexMagnitude(FFTResultShifted);

            // Draw FFTMagnitude
            Paint bluePaint = new Paint();
            bluePaint.setColor(Color.BLUE);
            bluePaint.setStrokeWidth(3);
            int FFTMagnitudeXMax = FFTMagnitude.length;
            float FFTMagnitudeXScale =(float)canvasWidth/(float)FFTMagnitudeXMax;
            float FFTMagnitudeYOffset = canvasHeight/5.0f * 2.5f;
            float FFTMagnitudeYScale = -(canvasHeight/5.0f)/10.0f;
            for (int x=0 ; x<canvasWidth-1 ; x++) {
                float x0 = x;
                float y0 = (float) (FFTMagnitude[(int)(x0/FFTMagnitudeXScale)]*FFTMagnitudeYScale + FFTMagnitudeYOffset);
                float x1 = x+1;
                float y1 = (float) (FFTMagnitude[(int)(x1/FFTMagnitudeXScale)]*FFTMagnitudeYScale + FFTMagnitudeYOffset);

                canvas.drawLine(x0, y0, x1, y1, bluePaint);
            }

            // Convolve sobel kernel with FFTMagnitude
            double[] FFTMagnitudeSobel = FFTHelper.FFTConvolution(FFTMagnitude, FFTHelper.sobelKernel(FFTMagnitude.length,1));


            // Draw FFTMagnitudeSobel
            Paint cyanPaint = new Paint();
            cyanPaint.setColor(Color.CYAN);
            cyanPaint.setStrokeWidth(3);
            int FFTMagnitudeSobelXMax = FFTMagnitudeSobel.length;
            float FFTMagnitudeSobelXScale =(float)canvasWidth/(float)FFTMagnitudeSobelXMax;
            float FFTMagnitudeSobelYOffset = canvasHeight/5.0f * 2.50f;
            float FFTMagnitudeSobelYScale = -(canvasHeight/5.0f)/10.0f;
            for (int x=0 ; x<canvasWidth-1 ; x++) {
                float x0 = x;
                float y0 = (float) (FFTMagnitudeSobel[(int)(x0/FFTMagnitudeSobelXScale)]*FFTMagnitudeSobelYScale + FFTMagnitudeSobelYOffset);
                float x1 = x+1;
                float y1 = (float) (FFTMagnitudeSobel[(int)(x1/FFTMagnitudeSobelXScale)]*FFTMagnitudeSobelYScale + FFTMagnitudeSobelYOffset);

                canvas.drawLine(x0, y0, x1, y1, cyanPaint);
            }

            // Normalize and sum FFTMagnitude
            double[] FFTMagnitudeNormalized = FFTHelper.normalizeMax(FFTMagnitude, 1);
            double FFTMagnitudeSum = FFTHelper.sum(FFTMagnitudeNormalized);
            if (FFTMagnitudeSum > maxFFTMagnitudeSum) {
                maxFFTMagnitudeSum = FFTMagnitudeSum;
            }

            // Sum FFTMagnitudeSobel
            double FFTMagnitudeSobelSum = FFTHelper.absSum(FFTMagnitudeSobel);
            if (FFTMagnitudeSobelSum < minFFTMagnitudeSobelSum) {
                minFFTMagnitudeSobelSum = FFTMagnitudeSobelSum;
            }


            // Draw Acceleration
            Paint purplePaint = new Paint();
            purplePaint.setColor(Color.MAGENTA);
            purplePaint.setStrokeWidth(1);
            int accelerationXMax = absAccelerationBufferCopy.length;
            float accelerationXScale =(float)canvasWidth/(float)accelerationXMax;
            float accelerationYOffset = canvasHeight/5.0f * 3.8f;
            float accelerationYScale = -(canvasHeight/5.0f)/10.0f;
            for (int x=0 ; x<canvasWidth-1 ; x++) {
                float x0 = x;
                float y0 = (float) (absAccelerationBufferCopy[(int)(x0/accelerationXScale)%absAccelerationBufferCopy.length]*accelerationYScale + accelerationYOffset);
                float x1 = x+1;
                float y1 = (float) (absAccelerationBufferCopy[(int)(x1/accelerationXScale)%absAccelerationBufferCopy.length]*accelerationYScale + accelerationYOffset);

                canvas.drawLine(x0, y0, x1, y1, purplePaint);
            }

            // Draw Jounce
            double[] jounceBuffer = FFTHelper.FFTConvolution(absAccelerationBufferCopy, FFTHelper.sobelKernel(absAccelerationBufferCopy.length,1)); // Snap/Jerk
            jounceBuffer = FFTHelper.FFTConvolution(jounceBuffer, FFTHelper.sobelKernel(jounceBuffer.length,1)); // Jounce

            // TODO: Something might be wrong with FFTConvolution....... Sometimes the result seems to be shifted
            //jounceBuffer = FFTHelper.FFTConvolution(jounceBuffer, FFTHelper.sobelKernel(jounceBuffer.length,1)); // Crackle
            //jounceBuffer = FFTHelper.FFTConvolution(jounceBuffer, FFTHelper.sobelKernel(jounceBuffer.length,1)); // Pop

            Paint greenPaint = new Paint();
            greenPaint.setColor(Color.GREEN);
            greenPaint.setStrokeWidth(1);
            int jerkXMax = jounceBuffer.length;
            float jerkXScale =(float)canvasWidth/(float)jerkXMax;
            float jerkYOffset = canvasHeight/5.0f * 4.2f;
            float jerkYScale = -(canvasHeight/5.0f)/10.0f;
            for (int x=0 ; x<canvasWidth-1 ; x++) {
                float x0 = x;
                float y0 = (float) (jounceBuffer[(int)(x0/jerkXScale)%absAccelerationBufferCopy.length]*jerkYScale + jerkYOffset);
                float x1 = x+1;
                float y1 = (float) (jounceBuffer[(int)(x1/jerkXScale)%absAccelerationBufferCopy.length]*jerkYScale + jerkYOffset);

                canvas.drawLine(x0, y0, x1, y1, greenPaint);
            }


            // Write FFTMagnitudeSum
            Paint blackPaint = new Paint();
            blackPaint.setColor(Color.BLACK);
            blackPaint.setTextSize(40);
            canvas.drawText("FFTMagnitudeSum: " + (int)(FFTMagnitudeSum), 40, 40, blackPaint);
            // Write maxFFTMagnitudeSum
            canvas.drawText("maxFFTMagnitudeSum: " + (int)(maxFFTMagnitudeSum), 40, 90, blackPaint);
            // Write FFTMagnitudeSobelSum
            canvas.drawText("FFTMagnitudeSobelSum: " + (int)(FFTMagnitudeSobelSum), 40, 140, blackPaint);
            // Write minFFTMagnitudeSobelSum
            canvas.drawText("minFFTMagnitudeSobelSum: " + (int)(minFFTMagnitudeSobelSum), 40, 190, blackPaint);


            // Detect tap
            //boolean tap = FFTHelper.absSum(doubleBuffer) < 50;//FFTMagnitudeNormalized[FFTMagnitudeNormalized.length-1] > 0.6;
            boolean tap = FFTMagnitudeSobelSum < 500;
            if (tap) {
                tapArray[time] = true;
            } else {
                tapArray[time] = false;
            }

            // Draw line at time index
            float x0 = time;
            float y0 = canvasHeight/5.0f * 4.5f;
            float x1 = time;
            float y1 = canvasHeight/5.0f * 4.5f + 100;
            canvas.drawLine(x0, y0, x1, y1, bluePaint);

            // Draw taps
            for (int i=0 ; i<time && i<tapArray.length ; i++) {
                if (tapArray[i]) {
                    canvas.drawLine(i, y0, i, y1, redPaint);
                }
            }


            // Increment time index
            time ++;
            time %= canvasWidth;
        }



        public void setSurfaceSize(int canvasWidth, int canvasHeight)
        {
            synchronized (surfaceHolder)
            {
                time = 0;
                bitmap = Bitmap.createScaledBitmap(bitmap, canvasWidth, canvasHeight, true);
                tapArray = new boolean[canvasWidth];
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
                        //try {
                        //    this.sleep(1);
                        //} catch (InterruptedException e) {
                        //}
                    }
                }
            }
        }

    }


}
