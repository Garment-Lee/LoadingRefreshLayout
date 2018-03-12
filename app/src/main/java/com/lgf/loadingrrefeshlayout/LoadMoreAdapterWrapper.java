package com.lgf.loadingrrefeshlayout;

import android.content.Context;
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

    private View loadMoreView;
    private View noMoreView;
    private View loadFailedView;

    private int currentViewType;


    private RecyclerView.Adapter innerAdapter;
    private Context context;
    private boolean hasStatusView = false;

    private OnLoadMoreListener onLoadMoreListener;

    public LoadMoreAdapterWrapper(Context context, RecyclerView.Adapter adapter){
        this.innerAdapter = adapter;
        this.context = context;
    }

    public void showLoadMoreView(){
        currentViewType = ITEM_TYPE_LOAD_MORE_VIEW;
        hasStatusView = true;
        notifyItemChanged(getItemCount());
    }

    public void showNoMoreView(){
        currentViewType = ITEM_TYPE_NO_MORE_VIEW;
        hasStatusView = true;
        notifyItemChanged(getItemCount());
    }

    public void showLoadFailedView(){
        currentViewType = ITEM_TYPE_LOAD_FAILED_VIEW;
        hasStatusView = true;
        notifyItemChanged(getItemCount());
    }

    public void disableLoadMoreView(){
        currentViewType = ITEM_TYPE_NO_VIEW;
        hasStatusView = false;
        notifyDataSetChanged();
    }

    private RecyclerView.ViewHolder createLoadMoreViewHolder(){
        loadMoreView = LayoutInflater.from(context).inflate(R.layout.load_more_view_layout, null);
        return new LoadMoreViewHolder(loadMoreView);
    }

    private RecyclerView.ViewHolder createNoMoreViewHolder(){
        noMoreView = LayoutInflater.from(context).inflate(R.layout.no_more_view_layout, null);
        return new LoadMoreViewHolder(noMoreView);
    }

    private RecyclerView.ViewHolder createLoadFailedViewHolder(){
        loadFailedView = LayoutInflater.from(context).inflate(R.layout.load_failed_view_layout, null);
        return new LoadMoreViewHolder(loadFailedView);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.i(TAG, "## onCreateViewHolder...viewType:" + viewType);

        if (viewType == ITEM_TYPE_LOAD_MORE_VIEW) {
            return createLoadMoreViewHolder();
        } else if (viewType == ITEM_TYPE_NO_MORE_VIEW){
            return createNoMoreViewHolder();
        } else if (viewType == ITEM_TYPE_LOAD_FAILED_VIEW){
            return createLoadFailedViewHolder();
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
            if (newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItemPos == (allItemCount -1) && visibleItemCount > 0){
                showLoadMoreView();
                if (onLoadMoreListener != null){
                    onLoadMoreListener.onLoadMore();
                }
            }
        }
    }


}
