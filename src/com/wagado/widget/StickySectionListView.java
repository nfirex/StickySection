package com.wagado.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
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
			final View sticker = ((StickySectionListAdapter) getAdapter()).getStickerSection(firstVisibleItem, mStickerSection);
			if (sticker != null) {
				mParent.removeView(mStickerSection);

				mStickerSection = sticker;
				mStickerSection.setVisibility(View.INVISIBLE);

				mParent.addView(mStickerSection, mLayoutParams);
			}

			final boolean isNextSection = ((StickySectionListAdapter) getAdapter()).isHeader(firstVisibleItem + 1);
			if (isNextSection) {
				mNextSection = getChildAt(1);
			} else {
				mNextSection = null;
			}
		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			
		}
	};

	private View mStickerSection;
	private View mNextSection;
	private FrameLayout mParent;
	private int mStickerMargin;
	private FrameLayout.LayoutParams mLayoutParams;

	public StickySectionListView(Context context) {
		this(context, null);
	}

	public StickySectionListView(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.attr.listViewStyle);
	}

	public StickySectionListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		if (!(adapter instanceof StickySectionListAdapter)) {
			throw new IllegalStateException(TAG + ": For sticky section your adapter must extends StickySectionListAdapter.");
		}

		super.setAdapter(adapter);

		mParent = (FrameLayout) getParent();
		mLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);

		setOnScrollListener(mOnScrollListener);
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);

		if (mStickerSection != null) {
			drawSticker(canvas);
		}
	}




	protected void scrollStickerView() {
		if (mNextSection != null) {
			final int top = mNextSection.getTop();
			final int height = mStickerSection.getMeasuredHeight();

			if (top < 0 || top > height) {
				mStickerMargin = 0;
			} else {
				mStickerMargin = top - height;
			}
		} else {
			mStickerMargin = 0;
		}
	}

	protected void drawSticker(Canvas canvas) {
		final Bitmap bitmap = Bitmap.createBitmap(mStickerSection.getMeasuredWidth(), mStickerSection.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
		final Canvas canvas2 = new Canvas(bitmap);

		mStickerSection.draw(canvas2);

		scrollStickerView();

		canvas.drawBitmap(bitmap, 0, mStickerMargin, null);
	}
}