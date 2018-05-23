# LoadingRefreshLayout
    实现一个下拉加载的效果。

## 特点

	1. 继承于LinearLayout，不用自己实现ViewGroup的测量、布局、绘制。
  
	2. 可以兼容多种类型的控件，RecyclerView、ListView、ScrollerLayout等。
  
	3. 解决了事件滑动的冲突。
  
	4. 使用MarginLayoutParams的topMargin属性，实现View的移动。


## 实现思路


	1. 实现View的移动方法有很多种，因为移动的View位于布局的最顶部，可以通过MarginLayoutParams的topMargin属性，
	实现顶部View的移动。下移时，增大该值，上移时减少该值。获取headView的topMargin属性，该属性表示的是该headView
	距离父布局的顶部的距离，可以设置为headView的高度的负值，这样headView就会隐藏在父布局的上面，这个状态作为初始状态。
    
	2. LoadingRefreshLayout与嵌套的View可能会存在滑动的冲突，在需要下拉或者上移的时候，拦截滑动事件，
    重写onInterceptOnTouchEvent()方法，在判断需要拦截时，返回true，在onTouchEvent()方法中实现顶部HeadView的移动操作。
    
	3. 松手时，需要回到加载状态或者原始状态，这时涉及动画移动，使用ValueAnimator属性动画实现。
  
	4. 松手后，进入到正在加载状态，需要执行加载的动作，这时调用回调监听器；调用端加载完成后，需要调用加载完成的接口，
    LoadingRefreshLayout进入到加载完成的状态。


	5. 状态的切换过程。初始状态我们先让Head位于屏幕的上方（不可见的状态），RecyclerView充满屏幕。当手指触碰屏幕到
    手指的移动时，如果RecyclerView处于显示最顶端Item的状态，且是下滑动作，这是就拦截事件。Head随手指进行上下移动，
    使用的是Head的MarginLayoutParams的topMargin进行Head位置的变化，这时加载状态有如下：

	* 下拉加载状态：从开始拖动到topMargin的大小从-HeadHeight到0。
  
	*  松开加载状态：从topMargin的大小等于0，直至松手前。
  
	*  正在加载状态：松手之后，topMargin的大小等于0，直至加载完毕。
  
	*  加载完成：topMargin的大小变为-HeadHeight。
	
![](https://github.com/Garment-Lee/LoadingRefreshLayout/raw/master/img/loadrefreshLayout.png)  



## 效果图
![](https://github.com/Garment-Lee/LoadingRefreshLayout/raw/master/img/LoadRefreshLayout-refresh.gif)  


## 用法

#### xml使用
``` java

<?xml version="1.0" encoding="utf-8"?>
<android.support.constraint.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android";
    xmlns:app="http://schemas.android.com/apk/res-auto";
    xmlns:tools="http://schemas.android.com/tools";
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context="com.lgf.loadingrrefeshlayout.MainActivity">
    <com.lgf.loadingrrefeshlayout.LoadingRefreshLayout
        android:id="@+id/rl_data_list_layout"
        android:layout_width="match_parent"
        android:layout_height="match_parent">
	
	<include
            layout="@layout/loading_head_layout">
        </include>
	
        <android.support.v7.widget.RecyclerView
            android:id="@+id/rv_data_list"
            android:layout_width="match_parent"
            android:layout_height="wrap_content">
        </android.support.v7.widget.RecyclerView>
    </com.lgf.loadingrrefeshlayout.LoadingRefreshLayout>
</android.support.constraint.ConstraintLayout>


```
#### activity中使用

	实现拦截接口，判断拦截的条件
``` java

public interface IInterceptChecker {

    boolean isAllowToIntercept();

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

```

	设置事件拦截条件判断器
``` java
  mLoadingRefreshLayout.setInterceptChecker(this);

```

	实现刷新的回调接口，执行刷新动作
``` java

   public interface OnRefreshListener {
        void onRefresh();
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

```

	设置回调监听器
``` java
mLoadingRefreshLayout.setOnRefreshListener(this);

```
<br>
<br>
<br>
*************************************************分隔线******************************************************

# LoadMoreAdapterWrapper
RecyclerView上拉到底自动加载更多的效果

## 特点

	1. 使用装饰者模式，装饰RecyclerView.Adapter，在使用时，不需要修改原来的代码，无缝接入，便于维护和扩展。
  
	2. 可以扩展更多的自定义状态，比如加载失败状态。
  
  
## 实现思路

	1. 实现上拉到底自动加载，需要监听RecyclerView滑动到底部的事件，可继承RecyclerView.OnScrollListener，
    重写onScrollStateChanged()方法，根据当前的滑动状态和RecyclerView的可见Item的位置，可以判断RecyclerView
    是否滑动到底部。
    
	2. 要显示正在加载的提示或者没有更多的提示，可以通过重写RecyclerView.Adapter的getItemViewType()、
    getItemCount()、onCreateViewHolder()、onBindViewHolder()方法，不同的状态设置不同的ItemViewType；
    根据不同的ItemViewType，生成不同的ViewHolder，加载不同的布局。
    
	3. 暴露出状态设置的接口，供调用端使用，实现不同显示状态的切换。


## 效果图
![](https://github.com/Garment-Lee/LoadingRefreshLayout/raw/master/img/LoadRefreshLayout-loadmore5.gif)  


## 用法

### Activity中使用

    使用装饰类LoadMoreAdapterWrapper，进行相关的初始化。
    
 ``` java
   mDataRecyclerView = (RecyclerView) findViewById(R.id.rv_data_list);
    mDataRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    mRecyclerViewAdapter = new MyRecyclerViewAdapter();
    loadMoreAdapterWrapper = new LoadMoreAdapterWrapper(mRecyclerViewAdapter);
    loadMoreAdapterWrapper.setOnLoadMoreListener(this);
    mDataRecyclerView.setAdapter(loadMoreAdapterWrapper);

 ```
 
    实现加载更多的逻辑
    
 ``` java
    @Override
    public void onLoadMore() {
        loadMoreAdapterWrapper.showLoadMoreView();
        mDataRecyclerView.scrollToPosition(loadMoreAdapterWrapper.getItemCount() -1);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loadMoreAdapterWrapper.showLoadFailedView();
            }
        }, 5000);
    }
 ```
 
 
    重新加载实现逻辑
    
 ``` java
     @Override
    public void onRetry() {
        loadMoreAdapterWrapper.showLoadMoreView();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loadMoreAdapterWrapper.showNoMoreView();
            }
        }, 5000);
    }

 
 ```
 








