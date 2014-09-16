package com.augmate.beacontest.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import com.augmate.sdk.beacons.BeaconInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegionVisualization extends View {

    private Paint textPaint;
    private Paint beaconPaint;
    private Paint bgPaint;
    private Paint regionPaint;
    private Paint samplePaint;
    private Paint sampleOutline;
    private Paint beaconAvgPaint;
    private Paint beaconBestPaint;

    public RegionVisualization(Context context) {
        super(context);
        init();
    }

    public RegionVisualization(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RegionVisualization(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setWillNotDraw(false);
        setWillNotCacheDrawing(false);
        if (!isInEditMode()) {
            setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }
        preparePaints();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    private void preparePaints() {
        textPaint = new Paint();
        textPaint.setColor(0xCCFFFFFF);
        textPaint.setTextSize(18);

        beaconPaint = new Paint();
        beaconPaint.setColor(0xCCAADDFF);
        beaconPaint.setTextSize(18);

        beaconAvgPaint = new Paint();
        beaconAvgPaint.setColor(0xCC55AAFF);

        beaconBestPaint = new Paint();
        beaconBestPaint.setStyle(Paint.Style.STROKE);
        beaconBestPaint.setStrokeWidth(5);
        beaconBestPaint.setColor(0x7755AAFF);

        regionPaint = new Paint();
        regionPaint.setColor(0xCCFFDDAA);

        samplePaint = new Paint();
        samplePaint.setColor(0xCCAAFFAA);

        sampleOutline = new Paint();
        sampleOutline.setStyle(Paint.Style.STROKE);
        sampleOutline.setStrokeWidth(3);
        sampleOutline.setColor(0xCC88CC88);

        bgPaint = new Paint();
        bgPaint.setColor(0x77000000);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int parentHeight = MeasureSpec.getSize(heightMeasureSpec);

        this.setMeasuredDimension(parentWidth, parentHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //canvas.drawPaint(bgPaint);

        float barWidth = 10;
        float barSpacing = 12;
        float barHeight = 100;
        float beaconWidth = barSpacing * 10;
        float beaconPadding = 20;
        float beaconSpacing = beaconWidth + beaconPadding;
        float regionPadding = 30;
        float regionWidth = beaconSpacing * 2 + regionPadding * 2;
        float regionSpacing = regionWidth + 50;

        float dpiScale = getResources().getDisplayMetrics().density / 2;
        canvas.scale(dpiScale, dpiScale);

        canvas.save();
        canvas.translate(50, 300);
        for (BeaconRegionChart beaconRegionChart : beaconRegionCharts.values()) {
            // foreach region
            canvas.save();

            // region underline
            canvas.drawRect(0, 0, regionWidth, 5, regionPaint);
            canvas.drawText("Region " + beaconRegionChart.regionId, 0, 20, beaconPaint);

            canvas.translate(regionPadding, -regionPadding);

            canvas.save();
            // 2 beacons per region * (100px + 30px) = 260px
            for (BeaconChart beaconChart : beaconRegionChart.beacons.values()) {

                // beacon underline
                canvas.drawRect(0, 0, beaconWidth, 5, regionPaint);
                canvas.drawText(String.format("Beacon %d:%.2f", beaconChart.beaconId, beaconChart.weightedAverage), 0, 20, beaconPaint);

                // draw beacon's samples
                canvas.save();
                for (BeaconHistoryChart beacon : beaconChart.beacons) {
                    float height = beacon.value * barHeight;
                    samplePaint.setAlpha((int) (255.0f * beacon.life));
                    sampleOutline.setAlpha((int) (255.0f * beacon.life));
                    canvas.drawRect(0.0f, -height, barWidth, 0, samplePaint);
                    canvas.drawRect(0.0f, -height, barWidth, 0, sampleOutline);
                    canvas.translate(barSpacing, 0);
                }
                canvas.restore();

                // shift right for the next beacon
                canvas.translate(beaconSpacing, 0);
            }
            canvas.restore();

            int numBeacons = beaconRegionChart.beacons.values().size();
            int beaconNum = 0;
            float bestAverage = 0;
            for (BeaconChart beaconChart : beaconRegionChart.beacons.values()) {
                float weightedLinePosition = beaconChart.weightedAverage * -barHeight;
                bestAverage = Math.max(bestAverage, beaconChart.weightedAverage);
                canvas.drawRect(beaconNum * beaconSpacing, weightedLinePosition - 2, numBeacons * beaconSpacing - beaconPadding, weightedLinePosition + 2, beaconAvgPaint);
                beaconNum++;
            }

            canvas.drawRect(0, bestAverage * -barHeight, numBeacons * beaconSpacing - beaconPadding, 0, beaconBestPaint);

            canvas.restore();

            // shift right for the next region
            canvas.translate(regionSpacing, 0);
        }
        canvas.restore();
    }

    class BeaconRegionChart {
        public int regionId;
        public Map<Integer, BeaconChart> beacons = new HashMap<>();

        public BeaconRegionChart(int regionId) {
            this.regionId = regionId;
        }

        public BeaconChart getChartForBeacon(int id) {
            BeaconChart chart = beacons.get(id);
            if (chart == null) {
                chart = new BeaconChart(id);
                beacons.put(id, chart);
            }
            return chart;
        }
    }

    class BeaconChart {
        public int beaconId;
        public BeaconHistoryChart[] beacons = new BeaconHistoryChart[10];
        public float weightedAverage = 1;

        public BeaconChart(int beaconId) {
            this.beaconId = beaconId;
            for (int i = 0; i < beacons.length; i++) {
                beacons[i] = new BeaconHistoryChart();
            }
        }
    }

    class BeaconHistoryChart {
        public float value = 0; // [0,1]
        public float life = 0;
        public static final float MaxDistance = 2;
    }

    Map<Integer, BeaconRegionChart> beaconRegionCharts = new HashMap<>();

    public BeaconRegionChart getChartForRegion(int regionId) {
        if (!beaconRegionCharts.containsKey(regionId))
            beaconRegionCharts.put(regionId, new BeaconRegionChart(regionId));

        return beaconRegionCharts.get(regionId);
    }


    /**
     * updates beacon charts to match latest data
     *
     * @param beacons latest beacon data
     */
    public void update(List<BeaconInfo> beacons) {
        for (BeaconInfo beacon : beacons) {

            // find region that contains this beacon
            BeaconRegionChart region = getChartForRegion(beacon.regionId);
            BeaconChart chart = region.getChartForBeacon(beacon.minor);

            for (int i = 0; i < beacon.history.length; i++) {
                if (beacon.history[i] == null) {
                    chart.beacons[i].value = 1;
                    chart.beacons[i].life = 0;
                } else {
                    float normalizedClampedDistance = Math.max(0, (BeaconHistoryChart.MaxDistance - (float) beacon.history[i].distance) / BeaconHistoryChart.MaxDistance);
                    float exponential = (float) (Math.pow(10.0, normalizedClampedDistance) - 1) / 9.0f;

                    chart.beacons[i].value = exponential;
                    chart.beacons[i].life = (float) beacon.history[i].life;
                }
            }

            chart.weightedAverage = (float) Math.max(0, (BeaconHistoryChart.MaxDistance - beacon.weightedAvgDistance) / BeaconHistoryChart.MaxDistance);
            chart.weightedAverage = getExponential(chart.weightedAverage);

            postInvalidate();
        }
    }

    // takes a linear [0,1] and returns a [0,1] on a curve
    private float getExponential(float normalizedValue) {
        return (float) (Math.pow(10.0, normalizedValue) - 1) / 9.0f;
    }
}
