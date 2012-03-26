package com.wagado.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.AbsListView.OnScrollListener;

public class StickySectionListView extends ListView implements OnScrollListener {
	protected static final String TAG = "StickySectionListView";

	private FrameLayout mParent;

	public StickySectionListView(Context context) {
		this(context, null);
	}

	public StickySectionListView(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.attr.listViewStyle);
	}

	public StickySectionListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setOnScrollListener(this);
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		if (!(adapter instanceof StickySectionListAdapter)) {
			throw new IllegalStateException(TAG + ": For sticky section your adapter must extends StickySectionListAdapter.");
		}

		super.setAdapter(adapter);

		mParent = (FrameLayout) getParent();
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		if (getAdapter() != null) {
			((StickySectionListAdapter)getAdapter()).setSection(firstVisibleItem, mParent);
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		
	}
}
