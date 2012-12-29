package com.yiluhao.panoplayer;

import android.app.Activity;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;

public class MapTestActivity extends Activity {
    private ImageView imageView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pano_map);

        imageView = (ImageView) this.findViewById(R.id.pano_map);
        imageView.setOnTouchListener(new TouchListener());
    }

    private final class TouchListener implements OnTouchListener {
        private PointF startPoint = new PointF();
        private Matrix matrix = new Matrix();
        private Matrix currentMatrix = new Matrix();
        private int mode = 0;
        private static final int DRAG = 1;
        private static final int ZOOM = 2;
        private float startDis;// 开始距离
        private PointF midPoint;// 中间点

        private float left;
        private float top;
        private float right;
        private float bottom;
        Rect rect;

        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:// 手指压下屏幕
                mode = DRAG;
                currentMatrix.set(imageView.getImageMatrix());// 记录ImageView当前的移动位置
                startPoint.set(event.getX(), event.getY());

                float[] values = new float[9];
                currentMatrix.getValues(values);
                
                rect = ((ImageView)v).getDrawable().getBounds();

                left = values[Matrix.MTRANS_X];
                top = values[Matrix.MTRANS_Y];
                right = left + rect.width() * values[Matrix.MSCALE_X];
                bottom = top + rect.height() * values[Matrix.MSCALE_Y];
                
                break;

            case MotionEvent.ACTION_MOVE:// 手指在屏幕移动，该 事件会不断地触发
                if (mode == DRAG) {
                    float dx = event.getX() - startPoint.x;// 得到在x轴的移动距离
                    float dy = event.getY() - startPoint.y;// 得到在y轴的移动距离
                    matrix.set(currentMatrix);// 在没有进行移动之前的位置基础上进行移动
                    
                    if(right - left < v.getWidth()) {
                        dx = 0;
                    } else if (left + dx > 0 && dx > 0)
                        dx = -left;
                    else if (right + dx < v.getRight() && dx < 0)
                        dx =  v.getRight() - right; 
                    if(bottom - top < v.getHeight()) {
                        dy =  0;
                    }else if (top + dy > 0 && dy > 0)
                        dy = -top;
                    else if (bottom + dy < v.getBottom() && dy < 0)
                        dy = v.getBottom() - bottom;
                    Log.v("position:", dx+"-"+dy);
                    matrix.postTranslate(dx, dy);

                } else if (mode == ZOOM) {// 缩放
                    float endDis = distance(event);// 结束距离
                    if (endDis > 10f) {
                        float scale = endDis / startDis;// 得到缩放倍数
                        matrix.set(currentMatrix);
                        matrix.postScale(scale, scale, midPoint.x, midPoint.y);
                    }
                }
                break;

            case MotionEvent.ACTION_UP:// 手指离开屏
            case MotionEvent.ACTION_POINTER_UP:// 有手指离开屏幕,但屏幕还有触点（手指）
                mode = 0;
                break;

            case MotionEvent.ACTION_POINTER_DOWN:// 当屏幕上已经有触点（手指），再有一个手指压下屏幕
                mode = ZOOM;
                startDis = distance(event);
                if (startDis > 10f) {
                    midPoint = mid(event);
                    currentMatrix.set(imageView.getImageMatrix());// 记录ImageView当前的缩放倍数
                }
                break;
            }
            imageView.setImageMatrix(matrix);
            return true;
        }

    }

    /**
     * 计算两点之间的距离
     * 
     * @param event
     * @return
     */
    public static float distance(MotionEvent event) {
        float dx = event.getX(1) - event.getX(0);
        float dy = event.getY(1) - event.getY(0);
        return FloatMath.sqrt(dx * dx + dy * dy);
    }

    /**
     * 计算两点之间的中间点‰
     * 
     * @param event
     * @return
     */
    public static PointF mid(MotionEvent event) {
        float midX = (event.getX(1) + event.getX(0)) / 2;
        float midY = (event.getY(1) + event.getY(0)) / 2;
        return new PointF(midX, midY);
    }
}
