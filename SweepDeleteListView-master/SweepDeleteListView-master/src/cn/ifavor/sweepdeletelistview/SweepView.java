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

/* ����ɾ������ */
public class SweepView extends ViewGroup{

	/* ����View */
	private View mContentView;
	/* ɾ��View */
	private View mDeleteView;
	
	// ע�⣬������ؽ���߱�������ͳһ������ӼӼ���һ�࣬���׻���
	/* ɾ����� */
	private int mWidthDelete;
	/* ���ݿ�� */
	private int mWidthContent;
	/* ͳһ�߶� */
	private int mHeight;
	
	/* View���ֶ��� */
	private ViewDragHelper mHelper;
	/*��ǰ״̬*/
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

	/* ����xml����ʱ���ã�һ����getChildAt���߳�ʼ����Ա */
	@Override
	protected void onFinishInflate() {
		mHelper = ViewDragHelper.create(this, new SweepCallback());
		
		mContentView = getChildAt(0);
		mDeleteView = getChildAt(1);
		
		LayoutParams layoutParams = mDeleteView.getLayoutParams();
		// ֱ�ӻ�ȡ�� View ��LayoutParams,��þ�ȷ�Ŀ��
		mWidthDelete = layoutParams.width;
	}
	
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		// ������������
		mContentView.measure(widthMeasureSpec, heightMeasureSpec);
		
		// ����ɾ������
		int deleteWidthMeasureSpec = MeasureSpec.makeMeasureSpec(mWidthDelete, MeasureSpec.EXACTLY);
		mDeleteView.measure(deleteWidthMeasureSpec, heightMeasureSpec);
		
		// ȷ���Լ��ĳߴ�
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
		// ע��㲥
		IntentFilter filter = new IntentFilter(CLOSE_EXPAND_ACTION);
		mReceiver = new CloseExpandReceiver();
		getContext().registerReceiver(mReceiver, filter);
		
		mHeight = mContentView.getMeasuredHeight();
		mWidthContent = mContentView.getMeasuredWidth();
		
	}
	
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// ������������
		mContentView.layout(0, 0, mWidthContent, mHeight);
		
		// ����ɾ������
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
		 * ��Down��ʱ�����
		 * �Ƿ����touch
		 * 
		 * ���� child ����ȥ��view
		 * 
		 * ����ֵ
		 * return false: ��ȥ�������κ�Ч��û��
		 * return true: ��������ʼ����MOVE UP �¼�
		 */
		@Override
		public boolean tryCaptureView(View child, int pointerId) {
			closeAll(getContext());
			
			System.out.println(mContentView == child);
			// ��ǰҪ������View�ǲ���mContentView������Ǿͷ������������κβ���
			return mContentView == child || mDeleteView == child;
		}
		
		/**
		 * ��move��ʱ��ص�
		 * 
		 * ������
		 * child: tryCaptureView�з�����view
		 * left: ���߾ࣨ����ֵ��
		 * dx: ���������ƶ�����
		 * 
		 * ����ֵ��ȷ��Ҫ�ƶ����٣����غ���ʽ��ʼ�ƶ�
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
		 * ��view��λ�øı�ʱ���ã�Ҳ����move�ļ��������������ƶ���ֻ��move�¼��Ķ���ļ���
		 * ���ã�������һ��view���ƶ�����ʵҲ������clampViewPositionHorizontal����
		 */
		@Override
		public void onViewPositionChanged(View changedView, int left, int top,
				int dx, int dy) {
			// ��viewλ�øı䣬ˢ�½���
			invalidate();
			
			// ��� changedView ��mContentView�������mDeleteView
			if (changedView == mContentView){
				// �ı�mDeleteView��λ��
				mDeleteView.layout(mWidthContent + left, 0, mWidthContent+left+mWidthDelete, mHeight);
			} else if (changedView == mDeleteView){
				mContentView.layout(left-mWidthContent, 0, left, mHeight);
			}
			
//			System.out.println("getScrollX: " + mDeleteView.getScrollX());
		}
		
		/**
		 * �ɿ��ֵĻص�
		 * ������
		 * releasedChild�����ɿ����ĸ�view
		 * xvel���ɿ�ʱx���������
		 * yvel���ɿ�ʱy���������
		 */
		@Override
		public void onViewReleased(View releasedChild, float xvel, float yvel) {
			ScrollAnimation animation = null;
			int left = releasedChild.getLeft();
			/*
			if (releasedChild == mContentView){
				if (left > -mWidthDelete / 2){
					// ���²���
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
					// �ڲ������� Scroller ��startScroll����������ģ�⣬���ϵ��õ��� computeScroll
					mHelper.smoothSlideViewTo(releasedChild, 0, 0);
					// �ر�
					isOpened = false;
				} else {
					mHelper.smoothSlideViewTo(releasedChild, -mWidthDelete, 0);
					// ��
					isOpened = true;
				}
			} else if (releasedChild == mDeleteView){
				if (left >= mWidthContent - mWidthDelete / 2){
					// �ر�
					mHelper.smoothSlideViewTo(releasedChild, mWidthContent, 0);
					isOpened = false;
				} else {
					mHelper.smoothSlideViewTo(releasedChild, mWidthContent - mWidthDelete, 0);
					// ��
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
					// ���ﲻ��ʹ��scrollTo,��Ϊlayot�Ѿ��ı���scrollTo������ϵ
//					mContentView.scrollTo(currentScroll, 0);
					mDeleteView.layout(currentScroll+mWidthContent, 0, currentScroll+mWidthContent+mWidthDelete, mHeight);
					// ���ﲻ��ʹ��scrollTo,��Ϊlayot�Ѿ��ı���scrollTo������ϵ
//					mDeleteView.scrollTo(currentScroll + mWidthContent, 0);
				} else if (mView == mDeleteView){
					mContentView.layout(currentScroll - mWidthContent, 0, currentScroll, mHeight);
					// ���ﲻ��ʹ��scrollTo,��Ϊlayot�Ѿ��ı���scrollTo������ϵ
//					mContentView.scrollTo(currentScroll, 0);
					mDeleteView.layout(currentScroll, 0, currentScroll+mWidthDelete, mHeight);
					// ���ﲻ��ʹ��scrollTo,��Ϊlayot�Ѿ��ı���scrollTo������ϵ
//					mDeleteView.scrollTo(currentScroll + mWidthContent, 0);
				}
		
			}
			
		}
	}
	
	@Override
	public void computeScroll() {
		// mHelper �Ѿ���continueSettling() �а����������ƶ����ػ���view��λ�ã����ָı�λ�õķ���layout�����Ǽ��ݵģ�
		if (mHelper.continueSettling(true)){
			invalidate();
			System.out.println(mContentView.getLeft());
		}
	}
	
	private class CloseExpandReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			System.out.println("�ҽ��յ��㲥��");
			closeInternel();
		}
	} 
	
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if (mReceiver != null){
			getContext().unregisterReceiver(mReceiver);
			System.out.println("ȡ��ע��㲥");
		}
	}
	
	public static void closeAll(Context context){
		Intent intent = new Intent();
		intent.setAction(SweepView.CLOSE_EXPAND_ACTION);
		context.sendBroadcast(intent);
	}
	
	private void closeInternel(){
		// �����״̬, ���²��֣����� onlayout �ĳ�ʼ���֣�
//		if (isOpened){
		// ɾ���˶�����жϣ��޸���ʱ���ܹرյ�bug
		if (true){
			// ������������
			mContentView.layout(0, 0, mWidthContent, mHeight);
			
			// ����ɾ������
			mDeleteView.layout(mWidthContent, 0, mWidthContent + mWidthDelete, mHeight);
			isOpened = false;
			System.out.println("���Ѿ��ر�");
		}
	}
}	
