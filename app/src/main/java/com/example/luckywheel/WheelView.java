package com.example.luckywheel;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class WheelView extends View {

    private List<Option> items = new ArrayList<>();
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private RectF rectF = new RectF();
    private float centerX;
    private float centerY;
    private float radius;
    private Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final int[] colors = {
            Color.parseColor("#F44336"), // 紅
            Color.parseColor("#FF9800"), // 橘
            Color.parseColor("#FFEB3B"), // 黃
            Color.parseColor("#4CAF50"), // 綠
            Color.parseColor("#2196F3"), // 藍
            Color.parseColor("#9C27B0"), // 紫
            Color.parseColor("#E91E63"), // 粉
            Color.parseColor("#795548")  // 棕
    };

    public WheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(40);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);

        borderPaint.setColor(Color.WHITE);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(6);
    }

    public void setItems(List<Option> items) {
        this.items = items;
        invalidate();
    }

    // 計算總權重
    private float getTotalWeight() {
        float total = 0;
        for (Option option : items) {
            total += option.getWeight();
        }
        return total;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (items == null || items.isEmpty()) {
            return;
        }

        centerX = getWidth() / 2f;
        centerY = getHeight() / 2f;
        radius = Math.min(getWidth(), getHeight()) / 2f;

        rectF.set(
                centerX - radius,
                centerY - radius,
                centerX + radius,
                centerY + radius
        );

        drawWheel(canvas);
        drawTexts(canvas);
        drawCenter(canvas);
    }

    private void drawCenter(Canvas canvas) {
        Paint centerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerPaint.setColor(Color.WHITE);

        canvas.drawCircle(centerX, centerY, radius * 0.18f, centerPaint);

        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(36);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);

        canvas.drawText("Lucky", centerX, centerY - 10, textPaint);
        canvas.drawText("Wheel", centerX, centerY + 40, textPaint);
    }

    private void drawTexts(Canvas canvas) {
        float totalWeight = getTotalWeight();
        if (totalWeight == 0) return;

        float startAngle = 0;
        for (int i = 0; i < items.size(); i++) {
            // 依權重比例計算該選項的扇形大小 (Sweep Angle)
            float sweepAngle = (items.get(i).getWeight() / totalWeight) * 360f;
            float textAngle = startAngle + sweepAngle / 2;

            float textRadius = radius * 0.65f;
            double radians = Math.toRadians(textAngle);

            float x = (float) (centerX + textRadius * Math.cos(radians));
            float y = (float) (centerY + textRadius * Math.sin(radians));

            canvas.save();
            float fixAngle = textAngle;
            // 避免字體倒過來
            if (fixAngle > 90 && fixAngle < 270) {
                fixAngle += 180;
            }
            canvas.rotate(fixAngle, x, y);
            // 修正點：使用 items.get(i).getName() 畫出文字
            canvas.drawText(items.get(i).getName(), x, y, textPaint);
            canvas.restore();

            startAngle += sweepAngle;
        }
    }

    private void drawWheel(Canvas canvas) {
        float totalWeight = getTotalWeight();
        if (totalWeight == 0) return;

        float startAngle = 0;
        for (int i = 0; i < items.size(); i++) {
            // 依權重比例計算該選項的扇形大小
            float sweepAngle = (items.get(i).getWeight() / totalWeight) * 360f;

            paint.setColor(colors[i % colors.length]);

            canvas.drawArc(rectF, startAngle, sweepAngle, true, paint);
            canvas.drawArc(rectF, startAngle, sweepAngle, true, borderPaint);

            startAngle += sweepAngle;
        }
    }
}