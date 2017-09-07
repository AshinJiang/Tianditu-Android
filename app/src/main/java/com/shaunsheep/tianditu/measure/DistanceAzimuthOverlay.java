package com.shaunsheep.tianditu.measure;

import android.graphics.Point;
import android.location.Location;
import android.widget.TextView;

import com.tianditu.android.maps.GeoPoint;
import com.tianditu.android.maps.MapView;
import com.tianditu.android.maps.MapViewRender;
import com.tianditu.android.maps.Overlay;
import com.tianditu.android.maps.renderoption.CircleOption;
import com.tianditu.android.maps.renderoption.FontOption;
import com.tianditu.android.maps.renderoption.LineOption;
import com.tianditu.android.maps.renderoption.PlaneOption;
import com.tianditu.maps.GeoPointEx;

import java.math.BigDecimal;
import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by ashinjiang on 2017/3/24
 */

public class DistanceAzimuthOverlay extends Overlay {
    private MapView mMapView;
    private ArrayList<GeoPoint> mGeoPoints = new ArrayList<>();
    ArrayList<Double> mDoubleDistances = new ArrayList<>();
    ArrayList<Double> mDoubleAzimuths = new ArrayList<>();
    private boolean mEditStatus; // 可编辑状态
    private TextView mTxtViewMeasure;//显示测距信息

    private CircleOption mCircleOption;
    private LineOption mLineOption;
    public PlaneOption mPlaneOption;
    private FontOption mFontOption;

    private int mColor1ARGB = 0xFF000000;
    private int mColor2ARGB = 0x7F696969;
    private int mColor3ARGB = 0xFF5F9EA0;
    private int mCircleRadius = 12; // pixels
    private int mStrokeWidth = 5; // 宽度
    private boolean mIsDottedLine = false;
    private int mFontOffsetX = 0;
    private int mFontOffsetY = -30;

    private String mFillChars = "              #";

    public DistanceAzimuthOverlay(MapView mapView, TextView textView) {
        mMapView = mapView;
        mTxtViewMeasure=textView;
        mEditStatus = true;

        mCircleOption = new CircleOption();
        mCircleOption.setFillColor(mColor1ARGB);

        mLineOption = new LineOption();
        mLineOption.setStrokeWidth(mStrokeWidth);
        mLineOption.setStrokeColor(mColor3ARGB);
        mLineOption.setDottedLine(mIsDottedLine);

        mPlaneOption = new PlaneOption();
        mPlaneOption.setStrokeWidth(mStrokeWidth);
        mPlaneOption.setStrokeColor(mColor3ARGB);
        mPlaneOption.setDottedLine(mIsDottedLine);
        mPlaneOption.setFillColor(mColor2ARGB);

        mFontOption = new FontOption();
        mFontOption.setOffset(mFontOffsetX, mFontOffsetY);
    }

    public boolean setGeoPoints(ArrayList<GeoPoint> geoPoints) {
        boolean bReturn = true;

        clearGeoPoints();

        for(int i = 0; i < geoPoints.size(); i ++) {
            bReturn = bReturn & addGeoPoint(geoPoints.get(i));
        }

        return bReturn;
    }

    public ArrayList<GeoPoint> getGeoPoints() {
        return mGeoPoints;
    }

    public boolean addGeoPoint(GeoPoint geoPoint) {
        boolean bReturn = mGeoPoints.add(geoPoint);

        if(mGeoPoints.size() < 1) {
            bReturn = bReturn & true;
        } else if(mGeoPoints.size() == 1) {
            bReturn = bReturn & mDoubleDistances.add(0.0);
            bReturn = bReturn & mDoubleAzimuths.add(0.0);
        } else {
            GeoPoint p1 = mGeoPoints.get(mGeoPoints.size() - 2);
            GeoPoint p2 = mGeoPoints.get(mGeoPoints.size() - 1);

            // 计算距离
            float[] results = new float[1];
            Location.distanceBetween(GeoPointEx.getdY(p1), GeoPointEx.getdX(p1), GeoPointEx.getdY(p2), GeoPointEx.getdX(p2), results);
            double dDistance = mDoubleDistances.get(mDoubleDistances.size() - 1) + results[0];
            bReturn = bReturn & mDoubleDistances.add(dDistance);

            // 计算方位角
            double dAzimuth = calculateAzimuth(GeoPointEx.getdY(p1), GeoPointEx.getdX(p1), GeoPointEx.getdY(p2), GeoPointEx.getdX(p2));
            bReturn = bReturn & mDoubleAzimuths.add(dAzimuth);
        }

        return bReturn;
    }

    public boolean addGeoPoints(ArrayList<GeoPoint> geoPoints) {
        boolean bReturn = true;

        for(int i = 0; i < geoPoints.size(); i ++) {
            bReturn = bReturn & addGeoPoint(geoPoints.get(i));
        }

        return bReturn;
    }

    public boolean removeLastGeoPoint() {
        int size = mGeoPoints.size();
        if(size == 0) {
            return false;
        }

        int index = size - 1;

        boolean isremove = mGeoPoints.remove(mGeoPoints.get(index)) &
                mDoubleDistances.remove(mDoubleDistances.get(index)) &
                mDoubleAzimuths.remove(mDoubleAzimuths.get(index));
        if(mTxtViewMeasure!=null){
            if(mDoubleDistances.size()>0) {
                mTxtViewMeasure.setText(getDistanceLabel(mDoubleDistances.get(mDoubleDistances.size() - 1)));
            }
        }
        return isremove;
    }

    public void clearGeoPoints() {
        mGeoPoints.clear();
        mDoubleDistances.clear();
        mDoubleAzimuths.clear();
    }

    // 生成距离标签带单位(四舍五入3位小数)
    String getDistanceLabel(double dDistance) {
        final int ONE_THOUSAND = 1000;
        String strDistance = String.valueOf(dDistance);
        String strDistanceLab = "";

        if ((int) dDistance < ONE_THOUSAND) {
            BigDecimal distanceLab = new BigDecimal(strDistance);
            strDistanceLab = distanceLab.setScale(3, BigDecimal.ROUND_HALF_UP).toString() + "米";
        } else {
            BigDecimal distanceLab = new BigDecimal(String.valueOf(dDistance / ONE_THOUSAND));
            strDistanceLab = distanceLab.setScale(3, BigDecimal.ROUND_HALF_UP).toString() + "公里";
        }

        return strDistanceLab;
    }

    // 生成方位角标签带单位(四舍五入3位小数)
    String getAzimuthLabel(double dAzimuth) {
        String strAzimuth = String.valueOf(dAzimuth);
        String strAzimuthLab = "";

        // 生成方位角标签
        BigDecimal azimuthLab = new BigDecimal(strAzimuth);
        strAzimuthLab = azimuthLab.setScale(3, BigDecimal.ROUND_HALF_UP).toString() + "°";

        return strAzimuthLab;
    }

    public void setEditStatus(boolean status) {
        mEditStatus = status;
    }

    public boolean isEditStatus() {
        return mEditStatus;
    }

    // 计算方位角
    private double calculateAzimuth(double lat_a, double lng_a, double lat_b, double lng_b) {
        double d = 0;
        lat_a = lat_a * Math.PI / 180;
        lng_a = lng_a * Math.PI / 180;
        lat_b = lat_b * Math.PI / 180;
        lng_b = lng_b * Math.PI / 180;
        d = Math.sin(lat_a) * Math.sin(lat_b) + Math.cos(lat_a) * Math.cos(lat_b) * Math.cos(lng_b - lng_a);
        d = Math.sqrt(1 - d * d);
        d = Math.cos(lat_b) * Math.sin(lng_b - lng_a) / d;
        d = Math.asin(d) * 180 / Math.PI;
        //d = Math.round(d * 10000);

        double degree = 0;

        double dx = lng_b - lng_a;
        double dy = lat_b - lat_a;

        if(dx > 0) {
            if(dy > 0) { // 第一象限
                degree = d;
            } else if(dy == 0) { // X轴正方向
                degree = 90;
            } else { // 第二象限
                degree = 180 - Math.abs(d);
            }
        } else if(dx == 0) {
            if(dy > 0) { // Y轴正方向
                degree = 360;
            } else if(dy == 0) { // 原点O
                degree = 0;
            } else { // Y轴负方向
                degree = 180;
            }
        } else {
            if(dy > 0) { // 第四象限
                degree = 360 - Math.abs(d);
            } else if(dy == 0) { // X轴负方向
                degree = 270;
            } else { // 第三象限
                degree = 180 + Math.abs(d);
            }
        }

        return degree;
    }

    // 单击事件
    @Override
    public boolean onTap(GeoPoint geoPoint, MapView mapView) {
        if (isEditStatus()) {
            addGeoPoint(geoPoint);
            if(mTxtViewMeasure!=null){
                mTxtViewMeasure.setText(getDistanceLabel(mDoubleDistances.get(mDoubleDistances.size()-1)));
            }
        }

        return true;
    }

    // 动画叠加绘制调用
    @Override
    public void draw(GL10 gl10, MapView mapView, boolean shadow) {
        if (shadow) {
            return;
        }

        if (mGeoPoints == null || mGeoPoints.size() == 0) {
            return;
        }


        if(mDoubleDistances.size() != mGeoPoints.size()) {
            return;
        }

        MapViewRender render = mapView.getMapViewRender();

        // 折线
        render.drawPolyLine(gl10, mLineOption, mGeoPoints);

        for (int i = 0; i < mGeoPoints.size(); i++) {
            GeoPoint geoPoint = mGeoPoints.get(i);
            Point point = mMapView.getProjection().toPixels(geoPoint, null);

            double dDistance = mDoubleDistances.get(i);
            double mAzimuth = mDoubleAzimuths.get(i);

            String strDistance = getDistanceLabel(dDistance);
            String strAzimuth = getAzimuthLabel(mAzimuth);

            // 拐点
            render.drawRound(gl10, mCircleOption, point,
                    mCircleRadius);

            if(i == 0) { // 起点标注
                String strLab = "起点"+ mFillChars;
                render.drawText(gl10, mFontOption, strLab, geoPoint);
            } else{ // 拐点标注
                String strLab = strDistance + "，" + strAzimuth + mFillChars;
                render.drawText(gl10, mFontOption, strLab, geoPoint);
            }
        }
    }
}
