package com.lgf.loadingrrefeshlayout;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by garment on 2018/2/26.
 */

public class LoadMoreAdapterWrapper extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final String TAG = "LoadMoreAdapterWrapper";

    private final int ITEM_TYPE_LOAD_MORE_VIEW = Integer.MAX_VALUE - 1;
    private final int ITEM_TYPE_NO_MORE_VIEW = ITEM_TYPE_LOAD_MORE_VIEW - 1;
    private final int ITEM_TYPE_LOAD_FAILED_VIEW = ITEM_TYPE_NO_MORE_VIEW - 1;
    private final int ITEM_TYPE_NO_VIEW = ITEM_TYPE_LOAD_FAILED_VIEW - 1;

    /**"加载更多" 显示的View*/
    private View loadMoreView;

    /**"没有更多" 显示的View*/
    private View noMoreView;

    /**"加载失败" 显示的View*/
    private View loadFailedView;

    /**保存当前的ViewType*/
    private int currentViewType;

    private RecyclerView.Adapter innerAdapter;

    /**是否需要显示提示item的标志*/
    private boolean hasStatusView = false;

    /**加载更多的回调监听器*/
    private OnLoadMoreListener onLoadMoreListener;

    public LoadMoreAdapterWrapper(RecyclerView.Adapter adapter){
        this.innerAdapter = adapter;
    }

    /**
     * 展示"正在加载"item提示
     */
    public void showLoadMoreView(){
        currentViewType = ITEM_TYPE_LOAD_MORE_VIEW;
        hasStatusView = true;
        notifyItemChanged(getItemCount());
    }

    /**
     * 展示"没有更多"item提示
     */
    public void showNoMoreView(){
        currentViewType = ITEM_TYPE_NO_MORE_VIEW;
        hasStatusView = true;
        notifyItemChanged(getItemCount());
    }

    /**
     * 展示"加载失败"item提示
     */
    public void showLoadFailedView(){
        currentViewType = ITEM_TYPE_LOAD_FAILED_VIEW;
        hasStatusView = true;
        notifyItemChanged(getItemCount());
    }

    /**
     * 不展示item提示
     */
    public void disableLoadMoreView(){
        currentViewType = ITEM_TYPE_NO_VIEW;
        hasStatusView = false;
        notifyDataSetChanged();
    }

    private RecyclerView.ViewHolder createLoadMoreViewHolder(ViewGroup parent){
        loadMoreView = LayoutInflater.from(parent.getContext()).inflate(R.layout.load_more_view_layout, parent, false);
        return new LoadMoreViewHolder(loadMoreView);
    }

    private RecyclerView.ViewHolder createNoMoreViewHolder(ViewGroup parent){
        noMoreView = LayoutInflater.from(parent.getContext()).inflate(R.layout.no_more_view_layout, parent, false);
        return new LoadMoreViewHolder(noMoreView);
    }

    private RecyclerView.ViewHolder createLoadFailedViewHolder(ViewGroup parent){
        loadFailedView = LayoutInflater.from(parent.getContext()).inflate(R.layout.load_failed_view_layout, parent, false);
        return new LoadMoreViewHolder(loadFailedView);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.i(TAG, "## onCreateViewHolder...viewType:" + viewType);

        if (viewType == ITEM_TYPE_LOAD_MORE_VIEW) {
            return createLoadMoreViewHolder(parent);
        } else if (viewType == ITEM_TYPE_NO_MORE_VIEW){
            return createNoMoreViewHolder(parent);
        } else if (viewType == ITEM_TYPE_LOAD_FAILED_VIEW){
            return createLoadFailedViewHolder(parent);
        }
        return innerAdapter.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Log.i(TAG, "## onBindViewHolder...position:" + position);

        if (holder.getItemViewType() == ITEM_TYPE_LOAD_FAILED_VIEW){
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showLoadMoreView();
                    if (onLoadMoreListener != null){
                        onLoadMoreListener.onRetry();
                    }
                }
            });
            return;
        } else {
            if (hasStatusView && position == (getItemCount()-1))
                return;
            innerAdapter.onBindViewHolder(holder, position);
        }
    }

    @Override
    public int getItemCount() {
        return innerAdapter.getItemCount() + (hasStatusView ? 1: 0);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == getItemCount() - 1 && hasStatusView){
            return currentViewType;
        }
        return innerAdapter.getItemViewType(position);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        recyclerView.addOnScrollListener(new LoadMoreOnScrollListener());
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener){
        this.onLoadMoreListener = onLoadMoreListener;
    }

    public interface OnLoadMoreListener{
        void onLoadMore();
        void onRetry();
    }

    public class LoadMoreOnScrollListener extends RecyclerView.OnScrollListener{

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            int lastVisibleItemPos = linearLayoutManager.findLastVisibleItemPosition();
            int allItemCount = recyclerView.getAdapter().getItemCount();
            int visibleItemCount = recyclerView.getChildCount();
            //每次下拉到底都会回调onLoadMore()方法，调用端注意，可以添加判断条件
            if (newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItemPos == (allItemCount -1) && visibleItemCount > 0){
                if (onLoadMoreListener != null){
                    onLoadMoreListener.onLoadMore();
                }
            }
        }
    }
}
