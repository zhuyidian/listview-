package cn.ifavor.sweepdeletelistview;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/* 滑动删除布局 */
public class SweepView extends ViewGroup{

	/* 内容View */
	private View mContentView;
	/* 删除View */
	private View mDeleteView;
	
	// 注意，这里务必将宽高变量进行统一，否则加加减减一多，容易混淆
	/* 删除宽度 */
	private int mWidthDelete;
	/* 内容宽度 */
	private int mWidthContent;
	/* 统一高度 */
	private int mHeight;
	
	/* View助手对象 */
	private ViewDragHelper mHelper;
	/*当前状态*/
	private boolean isOpened;

	public SweepView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public SweepView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SweepView(Context context) {
		super(context);
	}

	/* 当从xml加载时调用，一般是getChildAt或者初始化成员 */
	@Override
	protected void onFinishInflate() {
		mHelper = ViewDragHelper.create(this, new SweepCallback());
		
		mContentView = getChildAt(0);
		mDeleteView = getChildAt(1);
		
		LayoutParams layoutParams = mDeleteView.getLayoutParams();
		// 直接获取子 View 的LayoutParams,获得精确的宽高
		mWidthDelete = layoutParams.width;
	}
	
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		// 测量内容区域
		mContentView.measure(widthMeasureSpec, heightMeasureSpec);
		
		// 测量删除区域
		int deleteWidthMeasureSpec = MeasureSpec.makeMeasureSpec(mWidthDelete, MeasureSpec.EXACTLY);
		mDeleteView.measure(deleteWidthMeasureSpec, heightMeasureSpec);
		
		// 确定自己的尺寸
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec);
//		System.out.println("width:" +width+", height: " + height);
		setMeasuredDimension(width, height);
	}
	
	public static final String CLOSE_EXPAND_ACTION = "com.example.customeview.sweepdelete.SweepView.close_expand_action";
	private BroadcastReceiver mReceiver;
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		// 注册广播
		IntentFilter filter = new IntentFilter(CLOSE_EXPAND_ACTION);
		mReceiver = new CloseExpandReceiver();
		getContext().registerReceiver(mReceiver, filter);
		
		mHeight = mContentView.getMeasuredHeight();
		mWidthContent = mContentView.getMeasuredWidth();
		
	}
	
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// 布局内容区域
		mContentView.layout(0, 0, mWidthContent, mHeight);
		
		// 布局删除区域
		mDeleteView.layout(mWidthContent, 0, mWidthContent + mWidthDelete, mHeight);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		mHelper.processTouchEvent(event);
		
//		return super.onTouchEvent(event);
		return true;
	}
	
	class SweepCallback extends ViewDragHelper.Callback{

		/**
		 * 当Down的时候调用
		 * 是否分析touch
		 * 
		 * 参数 child 按下去的view
		 * 
		 * 返回值
		 * return false: 不去分析，任何效果没有
		 * return true: 分析，开始监听MOVE UP 事件
		 */
		@Override
		public boolean tryCaptureView(View child, int pointerId) {
			closeAll(getContext());
			
			System.out.println(mContentView == child);
			// 当前要分析的View是不是mContentView，如果是就分析，否则不做任何操作
			return mContentView == child || mDeleteView == child;
		}
		
		/**
		 * 当move的时候回调
		 * 
		 * 参数：
		 * child: tryCaptureView中分析的view
		 * left: 左侧边距（期望值）
		 * dx: 本次期望移动距离
		 * 
		 * 返回值：确定要移动多少，返回后正式开始移动
		 */
		@Override
		public int clampViewPositionHorizontal(View child, int left, int dx) {
//			System.out.println("left:" + left + ", dx:" + dx);
			int offset = 0;
			
			if (child == mContentView){
				if (left < -mWidthDelete){
					offset = -mWidthDelete;
				} else if (left > 0){
					offset = 0;
				} else {
					offset = left;
				}
			} else if (child == mDeleteView){
				if (left < mWidthContent - mWidthDelete){
					offset = mWidthContent - mWidthDelete;
				} else if (left > mWidthContent){
					offset = mWidthContent;
				} else {
					offset = left;
				}
			}
			
			return offset;
		}
		
		/**
		 * 当view的位置改变时调用，也属于move的监听，但不参与移动，只是move事件的额外的监听
		 * 作用：带动另一个view的移动，其实也可以在clampViewPositionHorizontal带动
		 */
		@Override
		public void onViewPositionChanged(View changedView, int left, int top,
				int dx, int dy) {
			// 当view位置改变，刷新界面
			invalidate();
			
			// 如果 changedView 是mContentView，则带动mDeleteView
			if (changedView == mContentView){
				// 改变mDeleteView的位置
				mDeleteView.layout(mWidthContent + left, 0, mWidthContent+left+mWidthDelete, mHeight);
			} else if (changedView == mDeleteView){
				mContentView.layout(left-mWidthContent, 0, left, mHeight);
			}
			
//			System.out.println("getScrollX: " + mDeleteView.getScrollX());
		}
		
		/**
		 * 松开手的回调
		 * 参数：
		 * releasedChild，你松开了哪个view
		 * xvel：松开时x方向的速率
		 * yvel：松开时y方向的速率
		 */
		@Override
		public void onViewReleased(View releasedChild, float xvel, float yvel) {
			ScrollAnimation animation = null;
			int left = releasedChild.getLeft();
			/*
			if (releasedChild == mContentView){
				if (left > -mWidthDelete / 2){
					// 重新布局
//					mContentView.layout(0, 0, mWidthContent, mHeight);
//					mDeleteView.layout(mWidthContent, 0, mWidthContent + mWidthDelete, mHeight);
					animation = new ScrollAnimation(mContentView, left, 0);
					mContentView.startAnimation(animation);
					invalidate();
				} else {
					animation = new ScrollAnimation(mContentView, left, -mWidthDelete);
					mContentView.startAnimation(animation);
					invalidate();
				}
				
			} else if (releasedChild == mDeleteView){
				if (left > mWidthContent - mWidthDelete / 2){
//					mContentView.layout(-mWidthDelete, 0, mWidthContent-mWidthDelete, mHeight);
//					mDeleteView.layout(mWidthContent - mWidthDelete, 0, mWidthContent, mHeight);
					animation = new ScrollAnimation(mDeleteView, left, mWidthContent);
					mDeleteView.startAnimation(animation);
					invalidate();
				} else {
					animation = new ScrollAnimation(mDeleteView, left, mWidthContent - mWidthDelete);
					mDeleteView.startAnimation(animation);
					invalidate();
				}

			}
			*/
			
			
			if (releasedChild == mContentView){
				if (left >= -mWidthDelete / 2){
					// 内部开启了 Scroller 的startScroll，进行数据模拟，不断调用的是 computeScroll
					mHelper.smoothSlideViewTo(releasedChild, 0, 0);
					// 关闭
					isOpened = false;
				} else {
					mHelper.smoothSlideViewTo(releasedChild, -mWidthDelete, 0);
					// 打开
					isOpened = true;
				}
			} else if (releasedChild == mDeleteView){
				if (left >= mWidthContent - mWidthDelete / 2){
					// 关闭
					mHelper.smoothSlideViewTo(releasedChild, mWidthContent, 0);
					isOpened = false;
				} else {
					mHelper.smoothSlideViewTo(releasedChild, mWidthContent - mWidthDelete, 0);
					// 打开
					isOpened = true;
				}
			}
			invalidate();
		}
		
		private class ScrollAnimation extends Animation{
			View mView;
			int startScroll;
			int targetScroll;
			public ScrollAnimation(View view, int startScroll, int targetScroll) {
				this.mView = view;
				this.startScroll = startScroll;
				this.targetScroll = targetScroll;
				int durationMillis = Math.abs(targetScroll -startScroll );
				setDuration(durationMillis);
			}
			@Override
			protected void applyTransformation(float interpolatedTime,
					Transformation t) {
				int currentScroll = (int) (startScroll + (targetScroll - startScroll) * interpolatedTime);
				if (mView == mContentView){
					
					mContentView.layout(currentScroll, 0, currentScroll+mWidthContent, mHeight);
					// 这里不能使用scrollTo,因为layot已经改变了scrollTo的坐标系
//					mContentView.scrollTo(currentScroll, 0);
					mDeleteView.layout(currentScroll+mWidthContent, 0, currentScroll+mWidthContent+mWidthDelete, mHeight);
					// 这里不能使用scrollTo,因为layot已经改变了scrollTo的坐标系
//					mDeleteView.scrollTo(currentScroll + mWidthContent, 0);
				} else if (mView == mDeleteView){
					mContentView.layout(currentScroll - mWidthContent, 0, currentScroll, mHeight);
					// 这里不能使用scrollTo,因为layot已经改变了scrollTo的坐标系
//					mContentView.scrollTo(currentScroll, 0);
					mDeleteView.layout(currentScroll, 0, currentScroll+mWidthDelete, mHeight);
					// 这里不能使用scrollTo,因为layot已经改变了scrollTo的坐标系
//					mDeleteView.scrollTo(currentScroll + mWidthContent, 0);
				}
		
			}
			
		}
	}
	
	@Override
	public void computeScroll() {
		// mHelper 已经在continueSettling() 中帮我们做了移动，重绘了view的位置（这种改变位置的方法layout方法是兼容的）
		if (mHelper.continueSettling(true)){
			invalidate();
			System.out.println(mContentView.getLeft());
		}
	}
	
	private class CloseExpandReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			System.out.println("我接收到广播了");
			closeInternel();
		}
	} 
	
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if (mReceiver != null){
			getContext().unregisterReceiver(mReceiver);
			System.out.println("取消注册广播");
		}
	}
	
	public static void closeAll(Context context){
		Intent intent = new Intent();
		intent.setAction(SweepView.CLOSE_EXPAND_ACTION);
		context.sendBroadcast(intent);
	}
	
	private void closeInternel(){
		// 如果打开状态, 重新布局（就是 onlayout 的初始布局）
//		if (isOpened){
		// 删掉了多余的判断，修复有时不能关闭的bug
		if (true){
			// 布局内容区域
			mContentView.layout(0, 0, mWidthContent, mHeight);
			
			// 布局删除区域
			mDeleteView.layout(mWidthContent, 0, mWidthContent + mWidthDelete, mHeight);
			isOpened = false;
			System.out.println("我已经关闭");
		}
	}
}	
