/**
 * Copyright 2012 by Kleshchin Nikita (nfirex), Artemyev Vasiliy (vasyx)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wagado.widget;

import ru.camino.parts.adapter.SectionListAdapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListAdapter;
import android.widget.ListView;

public class StickySectionListView extends ListView {
	protected static final String TAG = "StickySectionListView";

	private View mStickerSection;

	private ViewGroup mParent;
	private ViewGroup.LayoutParams mLayoutParams;
	private SectionListAdapter mAdapter;
	private Bitmap mStickerBitmap;

	private boolean isStickyScroll;

	private int mStickerMargin;
	private int mCurrentSection;
	private int mNextSectionChild;

	private final StickyScrollListener mStickyScrollListener; 

	public StickySectionListView(Context context) {
		this(context, null);
	}

	public StickySectionListView(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.attr.listViewStyle);
	}

	public StickySectionListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		mStickyScrollListener = new StickyScrollListener();
		super.setOnScrollListener(mStickyScrollListener);
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		super.setAdapter(adapter);

		if (adapter instanceof SectionListAdapter) {
			isStickyScroll = true;
			mCurrentSection = INVALID_POSITION;
			mAdapter = (SectionListAdapter) adapter;
			mParent = (ViewGroup) getParent();
			mLayoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		} else {
			isStickyScroll = false;
			mAdapter = null;
			mParent = null;
			mLayoutParams = null;
			mStickerSection = null;
		}
	}

	@Override
	public void setSelectionFromTop(int position, int y) {
		super.setSelectionFromTop(position, y);

		checkAndCreateSticker(position);
	}

	@Override
	public void setOnScrollListener(OnScrollListener l) {
		mStickyScrollListener.setInternalScrollListener(l);
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);

		if (mStickerSection != null) {
			calculateStickerMargin();
			drawSticker(canvas);
		}
	}

	@Override
	public void onRestoreInstanceState(Parcelable state) {
		final SavedState savedState = (SavedState) state;

		super.onRestoreInstanceState(savedState.getSuperState());

		createSticker(savedState.currentStickerSection);
	}

	@Override
	public Parcelable onSaveInstanceState() {
		final SavedState savedState = new SavedState(super.onSaveInstanceState());
		savedState.currentStickerSection = mCurrentSection;

		return savedState;
	}




	/**
	 * Get section position for element 
	 * @param position - position of element
	 */
	protected int getSectionByPosition(int position) {
		int result = INVALID_POSITION;

		for (int i: (mAdapter.getHeaders().keySet())) {
			if (position < i) break;
			else result = i;
		}

		return result;
	}

	/**
	 * Check current sticker position and recreate sticker (or null it) if it needed
	 * @param position - position of item at ListView
	 */
	protected void checkAndCreateSticker (int position) {
		final int section = getSectionByPosition(position);
		if (mCurrentSection != section) {
			mCurrentSection = section;
			createSticker(mCurrentSection);
		}

		catchNextSection(position);
	}

	/**
	 * Recreate sticker (or null it)
	 * @param position - position of item at ListView
	 */
	protected void createSticker (int section) {
		if (mStickerBitmap != null) {
			mStickerBitmap.recycle();
			mStickerBitmap = null;
		}

		if (section == INVALID_POSITION) {
			mStickerSection = null;
		} else {
			mStickerSection = getAdapter().getView(section, mStickerSection, null);
			mStickerSection.setVisibility(View.INVISIBLE);
			mStickerBitmap = getBitmap(mStickerSection);
		}

		if (mStickerSection != null) {
			mParent.removeView(mStickerSection);
			mParent.addView(mStickerSection, mLayoutParams);
		}
	}

	/**
	 * Catch child index of view that can shift the sticker.
	 * @param position - position of first view child at ListView
	 */
	protected void catchNextSection (int position) {
		final int delta = 1;
		final boolean isNextSection = mAdapter.isHeader(position + delta);
		if (isNextSection) {
			mNextSectionChild = delta;
		} else {
			mNextSectionChild = INVALID_POSITION;
		}
	}

	/**
	 * Calculate sticker margin from top if it can shift by section
	 */
	protected void calculateStickerMargin () {
		if (mNextSectionChild != INVALID_POSITION) {
			final int top = getChildAt(mNextSectionChild).getTop();
			final int height = mStickerBitmap.getHeight();

			if (top < 0 || top > height) {
				mStickerMargin = 0;
			} else {
				mStickerMargin = top - height;
			}
		} else {
			mStickerMargin = 0;
		}
	}

	/**
	 * Draw sticker at ListView's Canvas after drawing a children of ListView
	 * @param canvas - Canvas for ListView, his child and blahblahblah
	 */
	protected void drawSticker(Canvas canvas) {
		if (mStickerSection.getHeight() == 0) {
			canvas.drawBitmap(mStickerBitmap, 0, mStickerMargin, null);
		} else {
			canvas.translate(0, mStickerMargin);
			mStickerSection.draw(canvas);
			canvas.translate(0, - mStickerMargin);
		}
	}

	/**
	 * Get Bitmap from hidden View
	 * @param view - View from which the Bitmap will create
	 */
	protected Bitmap getBitmap(View view) {
		view.setDrawingCacheEnabled(true);
		view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight()); 
		view.buildDrawingCache(true);

		final Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache(true));

		view.setDrawingCacheEnabled(false);

		return bitmap;
	}




	/**
	 * OnScrollListener for tracking changes of children's positions
	 */
	private class StickyScrollListener implements OnScrollListener {
		private OnScrollListener internalOnScrollListener;

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			if (isStickyScroll) {
				checkAndCreateSticker(firstVisibleItem);
			}

			if (internalOnScrollListener != null) {
				internalOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
			}
		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			if (internalOnScrollListener != null) {
				internalOnScrollListener.onScrollStateChanged(view, scrollState);
			}
		}

		public void setInternalScrollListener (OnScrollListener listener) {
			internalOnScrollListener = listener;
		}
	};

	/**
	 * Class for saving data about sticker if Activity change the state (rotate a screen for example)
	 */
	private class SavedState extends BaseSavedState {
		int currentStickerSection;

		SavedState(Parcelable superState) {
			super(superState);
		}

		private SavedState(Parcel in) {
			super(in);
			currentStickerSection = in.readInt();
		}

		@Override
		public void writeToParcel(Parcel out, int flags) {
			super.writeToParcel(out, flags);
			out.writeInt(currentStickerSection);
		}

		@Override
		public String toString() {
			return TAG + ".SavedState{currentStickerSection=" + Integer.toString(currentStickerSection) + "}";
		}
	}
}
