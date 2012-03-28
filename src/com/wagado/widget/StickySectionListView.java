package com.wagado.widget;

import ru.camino.parts.adapter.SectionListAdapter;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

public class StickySectionListView extends ListView {
	protected static final String TAG = "StickySectionListView";

	private final OnScrollListener mOnScrollListener = new OnScrollListener() {
		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			if (getAdapter() != null) {
				final View sticker = ((StickySectionListAdapter) getAdapter()).getStickerSection(firstVisibleItem, mStickerSection);
				if (sticker != null) {
					mParent.removeView(mStickerSection);
					mStickerSection = sticker;
					mParent.addView(mStickerSection, mLayoutParams);
				}
			}
		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {

		}
	};

	private FrameLayout mParent;
	private View mStickerSection;

	private final FrameLayout.LayoutParams mLayoutParams;

	public StickySectionListView(Context context) {
		this(context, null);
	}

	public StickySectionListView(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.attr.listViewStyle);
	}

	public StickySectionListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		mLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);

		setOnScrollListener(mOnScrollListener);
	}

	@Override
	public void setSelectionFromTop(int position, int y) {
		super.setSelectionFromTop(position, y);
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		if (!(adapter instanceof SectionListAdapter)) {
			throw new IllegalStateException(TAG + ": For sticky section your adapter must extends StickySectionListAdapter.");
		}

		super.setAdapter(adapter);

		mParent = (FrameLayout) getParent();
	}
}