package com.lgf.loadingrrefeshlayout;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements LoadMoreAdapterWrapper.OnLoadMoreListener, IInterceptChecker, LoadingRefreshLayout.OnRefreshListener{

    private final String TAG = "MainActivity";

    private LoadingRefreshLayout mLoadingRefreshLayout;
    private RecyclerView mDataRecyclerView = null;
    private String[] dataStringArr;
    private LoadMoreAdapterWrapper loadMoreAdapterWrapper;
    private MyRecyclerViewAdapter mRecyclerViewAdapter;
    private int count = 0;

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
        initViews();
    }

    private void initViews(){
        mLoadingRefreshLayout =(LoadingRefreshLayout) findViewById(R.id.rl_data_list_layout);
        mLoadingRefreshLayout.setOnRefreshListener(this);
        mLoadingRefreshLayout.setInterceptChecker(this);
        mDataRecyclerView = (RecyclerView) findViewById(R.id.rv_data_list);
        mDataRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerViewAdapter = new MyRecyclerViewAdapter();
        loadMoreAdapterWrapper = new LoadMoreAdapterWrapper(mRecyclerViewAdapter);
        loadMoreAdapterWrapper.setOnLoadMoreListener(this);
        mDataRecyclerView.setAdapter(loadMoreAdapterWrapper);
    }

    private void initData(){
        dataStringArr = new String[30];
        for (int i = 0; i < dataStringArr.length; i++){
            dataStringArr[i] = i + "";
            Log.i(TAG, "## onBindViewHolder dataStringArr:" + dataStringArr[i]);

        }
    }

    private void refreshData(){
        for (int i = 0; i < dataStringArr.length; i++){
            dataStringArr[i] = i + count + "";
        }
        count = count + 5;
    }

    @Override
    public void onLoadMore() {
        loadMoreAdapterWrapper.showLoadMoreView();
        mDataRecyclerView.scrollToPosition(loadMoreAdapterWrapper.getItemCount() -1);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loadMoreAdapterWrapper.showLoadFailedView();
            }
        }, 3000);
    }

    @Override
    public void onRetry() {
        loadMoreAdapterWrapper.showLoadMoreView();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loadMoreAdapterWrapper.showNoMoreView();
            }
        }, 3000);
    }

    @Override
    public boolean isAllowToIntercept() {
        boolean allowToPull = false;
        View firstChild = mDataRecyclerView.getChildAt(0);
        if (firstChild != null) {
            RecyclerView.LayoutManager layoutManager = mDataRecyclerView.getLayoutManager();
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
            int firstVisiblePosition = linearLayoutManager.findFirstVisibleItemPosition();
            if (firstVisiblePosition == 0 && firstChild.getTop() == 0) {
                allowToPull = true;
            } else {
                allowToPull = false;
            }
        } else {
            allowToPull = true;
        }
        return allowToPull;
    }

    @Override
    public void onRefresh() {
        refreshData();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mLoadingRefreshLayout.finishLoading();
                loadMoreAdapterWrapper.notifyDataSetChanged();
            }
        }, 2000);
    }

    private class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.MyViewHolder>{

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Log.i(TAG, "## onCreateViewHolder...");
            View layout = LayoutInflater.from(parent.getContext()).inflate(R.layout.data_item_layout, parent, false);
            MyViewHolder myViewHolder = new MyViewHolder(layout);
            return myViewHolder;
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            Log.i(TAG, "## onBindViewHolder position:" + position);
            holder.textView.setText(dataStringArr[position]);
        }

        @Override
        public int getItemCount() {
            Log.i(TAG, "## getItemCount...");
            return dataStringArr == null ? 0 : dataStringArr.length;
        }

        class MyViewHolder extends RecyclerView.ViewHolder{

            private TextView textView;
            public MyViewHolder(View view){
                super(view);
                this.textView = (TextView) view.findViewById(R.id.tv_data_item);
            }
        }
    }
}
