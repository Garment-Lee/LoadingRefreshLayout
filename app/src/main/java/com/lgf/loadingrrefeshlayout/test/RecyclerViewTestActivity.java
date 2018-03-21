package com.lgf.loadingrrefeshlayout.test;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lgf.loadingrrefeshlayout.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ligf on 2018/3/7.
 */

public class RecyclerViewTestActivity extends Activity {

    private static final String TAG = "RecyclerViewActivity";

    private RecyclerView mRecyclerView;

    private RecyclerViewAdapter mAdapter;

    private List<DataBean> mDataList = new ArrayList<>();

    private int mDataSize = 40;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recyclerview_test_activity);
        initData();
        initViews();
    }

    private void initViews(){
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_recyclerview_test_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new RecyclerViewAdapter(mDataList, this);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void initData(){
        mDataList.clear();
        for (int i = 0; i < mDataSize; i++){
            DataBean dataBean = new DataBean();
            dataBean.content = String.valueOf(i);
            dataBean.DataType = i < mDataSize / 2 ? 0 : 1;
            mDataList.add(dataBean);
        }
        mAdapter.notifyItemChanged(1);
    }

    private class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        List<DataBean> dataBeanList;
        LayoutInflater layoutInflater;

        private int count = 0;

        public RecyclerViewAdapter(List<DataBean> list, Context context){
            this.dataBeanList = list;
            layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            Log.i(TAG, "## onCreateViewHolder count:" + (++count) + ";view type:" + viewType);
            View layout = layoutInflater.inflate(R.layout.test_data_item_layout, parent, false);
            MyViewHolder myViewHolder = new MyViewHolder(layout);
            return myViewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            MyViewHolder myViewHolder = (MyViewHolder) holder;
            myViewHolder.contentTV.setText(dataBeanList.get(position).content);
            myViewHolder.viewTypeTV.setText(dataBeanList.get(position).DataType + "");
        }

        @Override
        public int getItemCount() {
            return dataBeanList == null ? 0 : dataBeanList.size();
        }

        @Override
        public int getItemViewType(int position) {
            return dataBeanList.get(position).DataType;
        }

        class MyViewHolder extends RecyclerView.ViewHolder{

            TextView contentTV;
            TextView viewTypeTV;

            public MyViewHolder(View itemView) {
                super(itemView);
                contentTV = (TextView) itemView.findViewById(R.id.tv_test_data_item_content);
                viewTypeTV = (TextView) itemView.findViewById(R.id.tv_test_data_item_itemtype);
            }
        }
    }
}
