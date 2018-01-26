package cn.ifavor.sweepdeletelistview;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity {

	ListView mList;
	List<String> mDatas;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 创建ListView
		mList = new ListView(this);
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		mList.setLayoutParams(params);
		setContentView(mList);

		// 模拟数据
		mDatas = new ArrayList<String>();
		for (int i = 0; i < 30; i++) {
			mDatas.add("我是内容" + i);
		}

		// 初始化组件
		init();
	}

	/*  初始化组件 */
	private void init() {
		mList.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == OnScrollListener.SCROLL_STATE_FLING) {
					SweepView.closeAll(MainActivity.this);
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
			}
		});

		mList.setAdapter(new MyAdapter());
	}

	private class MyAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mDatas.size();
		}

		@Override
		public Object getItem(int position) {
			return mDatas.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		ViewHolder holder = null;

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {

			if (convertView == null) {
				holder = new ViewHolder();
				convertView = View.inflate(MainActivity.this,
						R.layout.item_sweep_delete, null);
				holder.tvContent = (TextView) convertView
						.findViewById(R.id.tv_content);
				holder.tvDelete = (TextView) convertView
						.findViewById(R.id.tv_delete);

				// 给点击事件设置监听
				holder.tvDelete.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						mDatas.remove(position);
						notifyDataSetChanged();
						SweepView.closeAll(MainActivity.this);
					}
				});

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			// 设置数据
			holder.tvContent.setText(mDatas.get(position));
			return convertView;
		}
	}

	private static class ViewHolder {
		TextView tvContent;
		TextView tvDelete;
	}
}
