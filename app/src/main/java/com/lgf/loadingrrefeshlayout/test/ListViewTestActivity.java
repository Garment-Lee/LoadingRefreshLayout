package com.lgf.loadingrrefeshlayout.test;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.lgf.loadingrrefeshlayout.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ligf on 2018/3/7.
 */

public class ListViewTestActivity extends Activity {

    public static final String TAG = "ListViewTestActivity";

    private ListView mDataListView;
    private ListAdapter mDataAdapter;
    private List<DataBean> mDataList = new ArrayList<>();

    private int dataSize = 20;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listview_test_activity_layout);
        initData();
        initViews();
    }

    private void initViews(){
        mDataListView = (ListView) findViewById(R.id.lv_listview_test_data);
        mDataAdapter = new DataTestAdapter(mDataList, this);
        mDataListView.setAdapter(mDataAdapter);
    }

    private void initData(){
        mDataList.clear();
        for (int i = 0; i < dataSize; i ++){
            DataBean dataBean = new DataBean();
            dataBean.content = String.valueOf(i);
            dataBean.DataType = i < dataSize/2 ? 0 : 1;
            mDataList.add(dataBean);
        }
    }

    private class DataTestAdapter extends BaseAdapter{

        private List<DataBean> dataBeanList;
        private LayoutInflater layoutInflater;

        public DataTestAdapter(List<DataBean> list, Context context){
            this.dataBeanList = list;
            layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return dataBeanList == null ? 0 : dataBeanList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public int getItemViewType(int position) {
            return dataBeanList.get(position).DataType;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ListViewViewHolderFirst listViewViewHolder;
            ListViewViewHolderSecond listViewViewHolderSecond;
            DataBean dataBean = dataBeanList.get(position);
            int viewType = getItemViewType(position);
            // create viewholder
            if (convertView == null){
                Log.i(TAG, "## getView create viewholder...position:" + position);
                switch (viewType){
                    case 0:
                        listViewViewHolder = new ListViewViewHolderFirst();
                        convertView = layoutInflater.inflate(R.layout.test_data_item_layout, null);
                        listViewViewHolder.contentTV = (TextView)convertView.findViewById(R.id.tv_test_data_item_content);
                        listViewViewHolder.itemTypeTV = (TextView)convertView.findViewById(R.id.tv_test_data_item_itemtype);
                        listViewViewHolder.contentTV.setText("content:" + dataBean.content);
                        listViewViewHolder.itemTypeTV.setText("viewtype:" + String.valueOf(dataBean.DataType));
                        convertView.setTag(listViewViewHolder);
                        break;
                    case 1:
                        listViewViewHolderSecond = new ListViewViewHolderSecond();
                        convertView = layoutInflater.inflate(R.layout.test_data_item_second_layout, null);
                        listViewViewHolderSecond.contentTV = (TextView) convertView.findViewById(R.id.tv_test_data_item_senond_content);
                        listViewViewHolderSecond.itemTypeTV = (TextView) convertView.findViewById(R.id.tv_test_data_item_senond_viewtype);
                        listViewViewHolderSecond.contentTV.setText("content:" + dataBean.content);
                        listViewViewHolderSecond.itemTypeTV.setText("view type:" + String.valueOf(dataBean.DataType));
                        convertView.setTag(listViewViewHolderSecond);
                        break;
                }
            } else {
                switch (viewType){
                    case 0:
                        listViewViewHolder = (ListViewViewHolderFirst) convertView.getTag();
                        listViewViewHolder.contentTV.setText("content:" + dataBean.content);
                        listViewViewHolder.itemTypeTV.setText("viewtype:" + String.valueOf(dataBean.DataType));
                        break;
                    case 1:
                        listViewViewHolderSecond = (ListViewViewHolderSecond) convertView.getTag();
                        listViewViewHolderSecond.contentTV.setText("content:" + dataBean.content);
                        listViewViewHolderSecond.itemTypeTV.setText("view type:" + String.valueOf(dataBean.DataType));
                        break;
                }
            }
            return convertView;
        }

        private class ListViewViewHolderFirst{
            public TextView contentTV;
            public TextView itemTypeTV;
        }

        private class ListViewViewHolderSecond{
            public TextView contentTV;
            public TextView itemTypeTV;
        }
    }
}
