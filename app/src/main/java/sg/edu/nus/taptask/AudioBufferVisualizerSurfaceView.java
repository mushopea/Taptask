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

public class AudioBufferVisualizerSurfaceView extends SurfaceView implements SurfaceHolder.Callback  {
    private Context drawContext;
    public  DrawThread drawThread;
    private SurfaceHolder drawSurfaceHolder;
    private Boolean threadExists = false;
    public static volatile Boolean drawFlag = false;
    public boolean draw = true;

    private short[] audioBuffer = null;

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

            canvasHeight = canvas.getHeight();
            canvasWidth = canvas.getWidth();

            int xMax = buffer.length;
            float xScale =(float)canvasWidth/(float)xMax;

            // Clear screen
            Paint paint = new Paint();
            paint.setColor(Color.WHITE);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawPaint(paint);

            // Draw raw buffer
            Paint redPaint = new Paint();
            redPaint.setColor(Color.RED);
            redPaint.setStrokeWidth(3);
            float bufferYOffset = canvasHeight/5.0f;
            float bufferYScale = (canvasHeight/5.0f)/18000.0f;
            for (int x=0 ; x<canvasWidth-1 ; x++) {
                float x0 = x;
                float y0 = buffer[(int)(x0/xScale)]*bufferYScale + bufferYOffset;
                float x1 = x+1;
                float y1 = buffer[(int)(x1/xScale)]*bufferYScale + bufferYOffset;

                canvas.drawLine(x0, y0, x1, y1, redPaint);
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
                        try {
                            this.sleep(0);
                        } catch (InterruptedException e) {
                        }
                    }
                }
            }
        }

    }


}
