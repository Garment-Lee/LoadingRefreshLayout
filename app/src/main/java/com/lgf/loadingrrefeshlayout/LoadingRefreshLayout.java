package com.lgf.loadingrrefeshlayout;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by garment on 2018/2/24.
 */

public class LoadingRefreshLayout extends LinearLayout implements View.OnTouchListener{

    /**下拉刷新状态*/
    private final int STATUS_PULL_TO_REFRESH = 0;
    /**松开刷新状态*/
    private final int STATUS_RELEASE_TO_REFRESH = 1;
    /**正在刷新状态*/
    private final int STATUS_DOING_REFRESH = 2;
    /**刷新完成状态*/
    private final int STATUS_FINISH_TO_REFRESH = 3;

    /**下拉头*/
    private View head;
    /**下拉头中状态提示*/
    private TextView statusTV;
    /**当前的下拉状态*/
    private int currentStatus;
    /**是否已经加载过head的标志*/
    private boolean hasLoaded = false;
    /**head的隐藏高度*/
    private int hideHeadHeight;
    /**head的布局参数*/
    private MarginLayoutParams headMarginLayoutParams;
    /**是否允许下拉*/
    private boolean allowToPull = false;
    /**手指接触屏幕时Y坐标的位置*/
    private float downY;
    private OnRefreshListener onRefreshListener;


    private RecyclerView recyclerView;

    public LoadingRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context){
        head = LayoutInflater.from(context).inflate(R.layout.loading_head_layout, null, true);
        statusTV = (TextView) head.findViewById(R.id.tv_loading_head);
        setOrientation(VERTICAL);
        addView(head, 0);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed && !hasLoaded){
            hideHeadHeight = -head.getHeight();
            headMarginLayoutParams = (MarginLayoutParams) head.getLayoutParams();
            headMarginLayoutParams.topMargin = hideHeadHeight;
            recyclerView = (RecyclerView) getChildAt(1);
            recyclerView.setOnTouchListener(this);
            hasLoaded = true;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        setIsAbleToPull();
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                downY = event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                float moveY = event.getRawY();
                int distance = (int)(moveY - downY);
                if (distance <= 0 && headMarginLayoutParams.topMargin <= hideHeadHeight){
                    return false;
                }
                if (currentStatus != STATUS_DOING_REFRESH){
                    if (headMarginLayoutParams.topMargin > 0){
                        currentStatus = STATUS_RELEASE_TO_REFRESH;
                        statusTV.setText(R.string.release_to_refresh);
                    } else {
                        currentStatus = STATUS_PULL_TO_REFRESH;
                        statusTV.setText(R.string.pull_to_refresh);
                    }
                    headMarginLayoutParams.topMargin = (distance/2 )+ hideHeadHeight;
                    head.setLayoutParams(headMarginLayoutParams);
                }

                break;
            case MotionEvent.ACTION_UP:
            default:
                if (currentStatus == STATUS_PULL_TO_REFRESH){
                    int topMargin = headMarginLayoutParams.topMargin;
                    while (true){
                        topMargin = topMargin - 20;
                        if (topMargin > hideHeadHeight){
                            headMarginLayoutParams.topMargin = topMargin;
                            head.setLayoutParams(headMarginLayoutParams);
                        } else {
                            topMargin = hideHeadHeight;
                            headMarginLayoutParams.topMargin = topMargin;
                            head.setLayoutParams(headMarginLayoutParams);
                            break;
                        }
                    }
                } else if (currentStatus == STATUS_RELEASE_TO_REFRESH){
                    int topMargin = headMarginLayoutParams.topMargin;
                    while (true){
                        topMargin = topMargin - 20;
                        if (topMargin > 0){
                            headMarginLayoutParams.topMargin = topMargin;
                            head.setLayoutParams(headMarginLayoutParams);
                        } else {
                            topMargin = 0;
                            headMarginLayoutParams.topMargin = topMargin;
                            head.setLayoutParams(headMarginLayoutParams);
                            break;
                        }
                    }
                    currentStatus = STATUS_DOING_REFRESH;
                    statusTV.setText(R.string.doing_refresh);
                    if (onRefreshListener != null){
                        onRefreshListener.onRefresh();
                    }
                }
        }
        return false;
    }

    /**
     * 设置是否允许下拉
     */
    private void setIsAbleToPull(){
        View firstChild = recyclerView.getChildAt(0);
        if (firstChild != null){
            RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
            int firstVisiblePosition = linearLayoutManager.findFirstVisibleItemPosition();
            if (firstVisiblePosition == 0 && firstChild.getTop() == 0){
                allowToPull = true;
            } else {
                allowToPull = false;
            }
        } else {
            allowToPull = true;
        }
    }

    public void setOnRefreshListener(OnRefreshListener onRefreshListener){
        this.onRefreshListener = onRefreshListener;
    }

    public void finishLoading(){
        if (currentStatus == STATUS_DOING_REFRESH){
            int topMargin = headMarginLayoutParams.topMargin;
            while (true){
                topMargin = topMargin - 20;
                if (topMargin > hideHeadHeight){
                    headMarginLayoutParams.topMargin = topMargin;
                    head.setLayoutParams(headMarginLayoutParams);
                } else {
                    topMargin = hideHeadHeight;
                    headMarginLayoutParams.topMargin = topMargin;
                    head.setLayoutParams(headMarginLayoutParams);
                    break;
                }
            }
            currentStatus = STATUS_FINISH_TO_REFRESH;
        }
    }

    public interface OnRefreshListener{
        void onRefresh();
    }
}
