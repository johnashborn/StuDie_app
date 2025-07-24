package com.example.studie.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class CameraOverlayView extends View {
    private Paint darkPaint;
    private Paint clearPaint;
    private RectF cutOutRect;

    private boolean isDragging = false;
    private boolean isResizing = false;
    private float lastTouchX, lastTouchY;
    private final float touchTolerance = 60; // pixels, to detect resize corners


    public CameraOverlayView(Context context) {
        super(context);
        init();
    }

    public CameraOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        darkPaint = new Paint();
        darkPaint.setColor(Color.parseColor("#A6000000")); // Semi-transparent black
        darkPaint.setStyle(Paint.Style.FILL);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);


        clearPaint = new Paint();
        clearPaint.setColor(Color.TRANSPARENT);
        clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR)); // Punch out
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Create a new layer so we can cut from it
        int saved = canvas.saveLayer(null, null);

        // Draw the full dark overlay
        canvas.drawRect(0, 0, getWidth(), getHeight(), darkPaint);

        // Calculate cutout rectangle if not already set
        if (cutOutRect == null) {
            int width = getWidth();
            int height = getHeight();
            int rectWidth = (int) (width * 0.8); // 80% of screen width
            int rectHeight = (int) (height * 0.2); // 20% of screen height

            int left = (width - rectWidth) / 2;
            int top = (height - rectHeight) / 2;
            int right = left + rectWidth;
            int bottom = top + rectHeight;

            cutOutRect = new RectF(left, top, right, bottom);
        }

        // Clear the rectangle area (make it transparent)
        canvas.drawRoundRect(cutOutRect, 20, 20, clearPaint);

        canvas.restoreToCount(saved);
        Log.d("Overlay", "onDraw called");

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastTouchX = x;
                lastTouchY = y;

                if (isInResizeHandle(x, y)) {
                    isResizing = true;
                    return true;
                } else if (cutOutRect.contains(x, y)) {
                    isDragging = true;
                    return true;
                }
                break;


            case MotionEvent.ACTION_MOVE:
                float dx = x - lastTouchX;
                float dy = y - lastTouchY;

                if (isDragging) {
                    cutOutRect.offset(dx, dy);
                    constrainRectToBounds();
                    invalidate();
                } else if (isResizing) {
                    cutOutRect.right += dx;
                    cutOutRect.bottom += dy;

                    // Minimum size to avoid disappearing
                    float minWidth = 200;
                    float minHeight = 100;

                    if (cutOutRect.width() < minWidth)
                        cutOutRect.right = cutOutRect.left + minWidth;
                    if (cutOutRect.height() < minHeight)
                        cutOutRect.bottom = cutOutRect.top + minHeight;

                    // Optional: make sure resizing stays inside view
                    if (cutOutRect.right > getWidth())
                        cutOutRect.right = getWidth();
                    if (cutOutRect.bottom > getHeight())
                        cutOutRect.bottom = getHeight();

                    invalidate();
                }

                lastTouchX = x;
                lastTouchY = y;
                return true;


            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isDragging = false;
                isResizing = false;
                break;

        }

        return super.onTouchEvent(event);
    }

    // for the resize logic
    private boolean isInResizeHandle(float x, float y) {
        float handleX = cutOutRect.right;
        float handleY = cutOutRect.bottom;

        float dx = x - handleX;
        float dy = y - handleY;

        return Math.hypot(dx, dy) < touchTolerance; // within radius
    }


    private void constrainRectToBounds() {
        float viewWidth = getWidth();
        float viewHeight = getHeight();

        float dx = 0, dy = 0;

        if (cutOutRect.left < 0) dx = -cutOutRect.left;
        else if (cutOutRect.right > viewWidth) dx = viewWidth - cutOutRect.right;

        if (cutOutRect.top < 0) dy = -cutOutRect.top;
        else if (cutOutRect.bottom > viewHeight) dy = viewHeight - cutOutRect.bottom;

        cutOutRect.offset(dx, dy);
    }

    public RectF getCutOutRect(){
        return cutOutRect;
    }


}
