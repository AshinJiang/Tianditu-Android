package com.shaunsheep.tianditu;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.shaunsheep.tianditu.measure.DistanceAzimuthOverlay;
import com.tianditu.android.maps.GeoPoint;
import com.tianditu.android.maps.MapView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private MapView mapView;
    private DistanceAzimuthOverlay distanceAzimuthOverlay;
    private TextView tv_measurebox_home_text;//测距显示文本
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mapView=(MapView)findViewById(R.id.mapview);

        tv_measurebox_home_text = (TextView) findViewById(R.id.tv_measurebox_home_text);

        distanceAzimuthOverlay = null;
        distanceAzimuthOverlay = new DistanceAzimuthOverlay(mapView, tv_measurebox_home_text);
        mapView.addOverlay(distanceAzimuthOverlay);
        mapView.postInvalidate();
    }
    /**
     * 清除测距的覆盖物并关闭测距工具
     *
     * @param v
     */
    public void onClearMeasureClick(View v) {
        stopMeasure();
    }

    private void stopMeasure() {
        distanceAzimuthOverlay.clearGeoPoints();
        tv_measurebox_home_text.setText("点击地图进行测距");
        mapView.removeOverlay(distanceAzimuthOverlay);

        //重新测距
        distanceAzimuthOverlay = new DistanceAzimuthOverlay(mapView, tv_measurebox_home_text);
        mapView.addOverlay(distanceAzimuthOverlay);
        mapView.postInvalidate();
    }

    /**
     * 清除测距的覆盖物
     */
    public void onRemoveMeasureClick(View v) {
        List<GeoPoint> getPoints = distanceAzimuthOverlay.getGeoPoints();
        if (getPoints == null || getPoints.isEmpty()) {
            return;
        }
        distanceAzimuthOverlay.clearGeoPoints();
        tv_measurebox_home_text.setText("点击地图进行测距");
        mapView.postInvalidate();
    }

    /**
     * 清除上一步绘制的测距
     *
     * @param v
     */
    public void onRedoMeasureClick(View v) {
        List<GeoPoint> getPoints = distanceAzimuthOverlay.getGeoPoints();
        if (getPoints == null || getPoints.isEmpty()) {
            return;
        }
        distanceAzimuthOverlay.removeLastGeoPoint();
        mapView.postInvalidate();
    }
}
