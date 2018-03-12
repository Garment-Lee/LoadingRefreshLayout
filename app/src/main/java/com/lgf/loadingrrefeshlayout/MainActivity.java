package com.lgf.loadingrrefeshlayout;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";

    private LoadingRefreshLayout mLoadingRefreshLayout;
    private RecyclerView mDataRecyclerView = null;
    private String[] dataStringArr;
    private LoadMoreAdapterWrapper loadMoreAdapterWrapper;
    private MyRecyclerViewAdapter mRecyclerViewAdapter;
    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
        initViews();
    }

    private void initViews(){
        mLoadingRefreshLayout =(LoadingRefreshLayout) findViewById(R.id.rl_data_list_layout);
        mLoadingRefreshLayout.setOnRefreshListener(new LoadingRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
                mLoadingRefreshLayout.finishLoading();
                loadMoreAdapterWrapper.notifyDataSetChanged();
            }

            @Override
            public void onLoadMore() {

            }
        });
        mDataRecyclerView = (RecyclerView) findViewById(R.id.rv_data_list);
        mDataRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerViewAdapter = new MyRecyclerViewAdapter();
        loadMoreAdapterWrapper = new LoadMoreAdapterWrapper(this, mRecyclerViewAdapter);
        loadMoreAdapterWrapper.setOnLoadMoreListener(onLoadMoreListener);
        mDataRecyclerView.setAdapter(loadMoreAdapterWrapper);
    }

    LoadMoreAdapterWrapper.OnLoadMoreListener onLoadMoreListener = new LoadMoreAdapterWrapper.OnLoadMoreListener() {
        @Override
        public void onLoadMore() {
            mDataRecyclerView.scrollToPosition(loadMoreAdapterWrapper.getItemCount() -1);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadMoreAdapterWrapper.disableLoadMoreView();
                }
            }, 5000);
        }

        @Override
        public void onRetry() {

        }
    };

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

    private class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.MyViewHolder>{

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Log.i(TAG, "## onCreateViewHolder...");
            View layout = LayoutInflater.from(parent.getContext()).inflate(R.layout.data_item_layout, null);
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
