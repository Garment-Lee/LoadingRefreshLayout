package com.lgf.loadingrrefeshlayout;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by garment on 2018/2/24.
 */

public class LoadingRefreshLayout extends LinearLayout {

    private static final String TAG = "LoadingRefreshLayout";

    /**
     * 下拉刷新状态
     */
    private final int STATUS_PULL_TO_REFRESH = 0;

    /**
     * 松开刷新状态
     */
    private final int STATUS_RELEASE_TO_REFRESH = 1;

    /**
     * 正在刷新状态
     */
    private final int STATUS_DOING_REFRESH = 2;

    /**
     * 刷新完成状态
     */
    private final int STATUS_FINISH_TO_REFRESH = 3;

    /**
     * 下拉头
     */
    private View head;

    /**
     * 下拉头中状态提示
     */
    private TextView statusTV;

    /**
     * 当前的下拉状态
     */
    private int currentStatus;

    /**
     * 是否已经加载过head的标志
     */
    private boolean hasLoaded = false;

    /**
     * head的高度
     */
    private int headHeight;

    /**
     * head的布局参数
     */
    private MarginLayoutParams headMarginLayoutParams;

    /**
     * 下拉刷新回调
     */
    private OnRefreshListener onRefreshListener;

    private IInterceptChecker interceptChecker;

    /**
     *
     */
    private float interceptY;



    public LoadingRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        head = LayoutInflater.from(context).inflate(R.layout.loading_head_layout, null, true);
        statusTV = (TextView) head.findViewById(R.id.tv_loading_head);
        setOrientation(VERTICAL);
        addView(head, 0);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed && !hasLoaded) {
            headHeight = head.getHeight();
            headMarginLayoutParams = (MarginLayoutParams) head.getLayoutParams();
            headMarginLayoutParams.topMargin = -headHeight;
            hasLoaded = true;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        switch (ev.getAction()) {

            case MotionEvent.ACTION_DOWN:
                interceptY = ev.getRawY();
                break;

            case MotionEvent.ACTION_MOVE:
                float delY = ev.getRawY() - interceptY;
                if (delY > 0 && isAllowToIntercept()) {
                    //拦截之后，把当前的位置点的Y坐标的值记录下来，供onTouchEvent()中使用
                    interceptY = ev.getRawY();
                    return true;
                }
                break;

            case MotionEvent.ACTION_UP:

                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                float moveY = event.getRawY();
                //使用拦截之后的位置点的Y坐标
                int delY = (int) (moveY - interceptY);

                if (headMarginLayoutParams.topMargin >= -headHeight && currentStatus != STATUS_DOING_REFRESH && isAllowToIntercept()) {
                    if (headMarginLayoutParams.topMargin >= 0) {
                        //切换到松开刷新状态
                        currentStatus = STATUS_RELEASE_TO_REFRESH;
                        statusTV.setText(R.string.release_to_refresh);
                    } else {
                        //依然是下来刷新状态
                        currentStatus = STATUS_PULL_TO_REFRESH;
                        statusTV.setText(R.string.pull_to_refresh);
                    }
                    //增加的大小为手指移动位移的一半
                    headMarginLayoutParams.topMargin = headMarginLayoutParams.topMargin + delY / 2;
                    Log.i(TAG, "onTouchEvent delY/2:" + delY / 2);
                    //对超越边界值进行修正
                    if (headMarginLayoutParams.topMargin < -headHeight) {
                        headMarginLayoutParams.topMargin = -headHeight;
                    }

                    head.setLayoutParams(headMarginLayoutParams);
                }

                interceptY = moveY;

                break;
            case MotionEvent.ACTION_UP:
            default:
                //松开手之后
                if (currentStatus == STATUS_PULL_TO_REFRESH) {
                    //跳转到隐藏head状态
                    int topMargin = headMarginLayoutParams.topMargin;
                    autoMoveAnimation(topMargin, -headHeight - topMargin);

                } else if (currentStatus == STATUS_RELEASE_TO_REFRESH) {
                    //跳转到正在加载状态
                    int topMargin = headMarginLayoutParams.topMargin;
                    autoMoveAnimation(topMargin, -topMargin);
                    currentStatus = STATUS_DOING_REFRESH;
                    statusTV.setText(R.string.doing_refresh);
                    if (onRefreshListener != null) {
                        onRefreshListener.onRefresh();
                    }
                }
        }
        return super.onTouchEvent(event);
    }

    private void autoMoveAnimation(final int initPosition, int offset) {
        ValueAnimator valueAnimator = ValueAnimator.ofInt(0, offset);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int delOffset = (int) animation.getAnimatedValue();
                headMarginLayoutParams.topMargin = initPosition + delOffset;
                head.setLayoutParams(headMarginLayoutParams);
            }
        });
        valueAnimator.setDuration(500);
        valueAnimator.start();
    }

    /**
     * 设置是否允许拦截事件
     */
    private boolean isAllowToIntercept() {
        boolean allowToPull = false;
        if (interceptChecker != null){
            return interceptChecker.isAllowToIntercept();
        }
        return allowToPull;
    }

    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        this.onRefreshListener = onRefreshListener;
    }

    public void finishLoading() {
        if (currentStatus == STATUS_DOING_REFRESH) {
            int topMargin = headMarginLayoutParams.topMargin;
            autoMoveAnimation(topMargin, -headHeight - topMargin);
            currentStatus = STATUS_FINISH_TO_REFRESH;
        }
    }

    public void setInterceptChecker(IInterceptChecker interceptChecker){
        this.interceptChecker = interceptChecker;
    }

    public interface OnRefreshListener {
        void onRefresh();
    }
}
