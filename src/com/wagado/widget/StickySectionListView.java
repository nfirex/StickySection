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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SectionIndexer;

public class StickySectionListView extends ListView {
	protected static final String TAG = "StickySectionListView";

	private int mNextSectionChild;
	private boolean isStickyScroll;
	private SectionIndexer mAdapter;

	private final ListSticker mSticker;
	private final StickyScrollListener mStickyScrollListener;

	public StickySectionListView(Context context) {
		this(context, null);
	}

	public StickySectionListView(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.attr.listViewStyle);
	}

	public StickySectionListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		mSticker = new ListSticker(context, this);
		mStickyScrollListener = new StickyScrollListener();
		super.setOnScrollListener(mStickyScrollListener);
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		super.setAdapter(adapter);

		if (adapter instanceof SectionIndexer) {
			isStickyScroll = true;
			mAdapter = (SectionIndexer) adapter;
		} else {
			isStickyScroll = false;
			mAdapter = null;
			mSticker.clear();
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

		mSticker.onDraw(canvas);
	}

	@Override
	protected float getTopFadingEdgeStrength() {
		return getFirstVisiblePosition() < mSticker.firstSectionPosition ? super.getTopFadingEdgeStrength() : 0;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		mSticker.onSizeChanged(w, h, oldw, oldh);
	}

	@Override
	public void requestLayout() {
		if (isStickyScroll) {
			mSticker.refresh();
		}

		super.requestLayout();
	}




	/**
	 * Get section position for element 
	 * @param position - position of element
	 */
	protected int getSectionByPosition(int position) {
		if (position < mSticker.firstSectionPosition) {
			return INVALID_POSITION;
		} else {
			int result = mSticker.firstSectionPosition;

			for (int i = 1; i < mSticker.count; i ++) {
				final int section = mAdapter.getPositionForSection(i);

				if (position < section) {
					break;
				}

				result = section;
			}

			return result;
		}
	}

	/**
	 * Check current sticker position and recreate sticker (or null it) if it needed
	 * @param position - position of item at ListView
	 */
	protected void checkAndCreateSticker (int position) {
		final int section = getSectionByPosition(position);

		if (mSticker.position != section) {
			mSticker.createSticker(section);
		}

		catchNextSection();
	}

	/**
	 * Catch child index of view that can shift the sticker.
	 */
	protected void catchNextSection () {
		mNextSectionChild = INVALID_POSITION;

		if (getChildCount() == 0 || mSticker.count == 0) {
			return;
		}

		int index = 0;
		while (getChildAt(index).getTop() < mSticker.getSectionHeight()) {
			index ++;
			final int position = getFirstVisiblePosition() + index;
			final int section = mAdapter.getSectionForPosition(position);

			if (mAdapter.getPositionForSection(section) == position) {
				mNextSectionChild = index;
				break;
			}
		}
	}




	/**
	 * Sticky Section
	 */
	private class ListSticker extends ViewGroup {
		public int count;
		public int position;
		public int firstSectionPosition;

		private int mTop;
		private int mWidth;
		private int mHeight;

		private Bitmap mBitmap;
		private View mView;

		private final AbsListView mParent;

		public ListSticker(Context context, AbsListView parent) {
			super(context);

			position = INVALID_POSITION;
			firstSectionPosition = INVALID_POSITION;
			mParent = parent;
		}

		@Override
		protected void onLayout(boolean changed, int l, int t, int r, int b) {
			// Nothing to do
		}

		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
			mWidth = w;
			createSticker(position);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			if (mBitmap != null) {
				calculateStickerMargin();
				canvas.drawBitmap(mBitmap, 0, mTop, null);
			}
		}




		/**
		 * Update sticker and data
		 */
		public void refresh() {
			clear();

			final Object[] sections = mAdapter.getSections();
			count = sections == null ? 0 : mAdapter.getSections().length;
			firstSectionPosition = count == 0 ? INVALID_POSITION : mAdapter.getPositionForSection(0);

			createSticker(position);
		}

		/**
		 * Get current section height 
		 */
		public int getSectionHeight() {
			return mHeight;
		}

		/**
		 * Recreate sticker (or null it)
		 * @param position - position of item at ListView
		 */
		public void createSticker (int position) {
			this.position = position;

			if (count > 0) {
				if (mBitmap != null) {
					mBitmap.recycle();
					mBitmap = null;
					mHeight = 0;
				}

				if (position != INVALID_POSITION && mWidth > 0) {
					if (mView == null) {
						mView = mParent.getAdapter().getView(position, null, null);
						mView.setDrawingCacheEnabled(true);
						addView(mView);
					} else {
						mParent.getAdapter().getView(position, mView, this);
					}

					mBitmap = getBitmap(mView);
					mHeight = mBitmap.getHeight();
				}
			}
		}

		/**
		 * Destroy Bitmap and View of sticker
		 */
		public void clear() {
			if (mView != null) {
				mView.setDrawingCacheEnabled(false);
				removeView(mView);
				mView = null;
			}

			if (mBitmap != null) {
				mBitmap.recycle();
				mBitmap = null;
			}
		}




		/**
		 * Get Bitmap from hidden View
		 * @param view - View from which the Bitmap will create
		 */
		private Bitmap getBitmap(View view) {
			view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
			view.layout(0, 0, mWidth, view.getMeasuredHeight());

			return view.getDrawingCache(true).copy(Config.ARGB_8888, false);
		}

		/**
		 * Calculate sticker margin from top if it can shift by section
		 */
		private void calculateStickerMargin () {
			if (mNextSectionChild != INVALID_POSITION) {
				final int top = mParent.getChildAt(mNextSectionChild).getTop();

				if (top < 0 || top > getSectionHeight()) {
					mTop = 0;
				} else {
					mTop = top - getSectionHeight();
				}
			} else {
				mTop = 0;
			}
		}
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

		/**
		 * Set internal OnScrollListener
		 */
		public void setInternalScrollListener (OnScrollListener listener) {
			internalOnScrollListener = listener;
		}
	};

	/**
	 * Class for saving data about sticker if Activity change the state (rotate a screen for example or crash)
	 */
	static class SavedState extends BaseSavedState {
		int currentStickerSection;

		SavedState(Parcelable superState, int currentSticker) {
			super(superState);
			this.currentStickerSection = currentSticker;
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

		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
			public SavedState createFromParcel(Parcel in) {
				return new SavedState(in);
			}

			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};
	}

	@Override
	public void onRestoreInstanceState(Parcelable state) {
		final SavedState savedState = (SavedState) state;
		super.onRestoreInstanceState(savedState.getSuperState());

		mSticker.createSticker(savedState.currentStickerSection);
	}

	@Override
	public Parcelable onSaveInstanceState() {
		return new SavedState(super.onSaveInstanceState(), mSticker.position);
	}
}