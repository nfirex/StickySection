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
			mAdapter = (SectionListAdapter) adapter;
			mParent = (ViewGroup) getParent();
			mLayoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			mCurrentSection = INVALID_POSITION;

			createSticker(0);
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

		createSticker(position);
		catchNextSection(position);
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
	 * Recreate sticker (or null it)
	 * @param position - position of item at ListView
	 */
	protected void createSticker (int position) {
		final int section = getSectionByPosition(position);
		if (mCurrentSection != section) {
			mCurrentSection = section;

			if (mCurrentSection == INVALID_POSITION) {
				mStickerSection = null;
			} else {
				mStickerSection = getAdapter().getView(mCurrentSection, mStickerSection, null);
			}

			if (mStickerSection != null) {
				mParent.removeView(mStickerSection);

				mStickerSection.setVisibility(View.INVISIBLE);

				mParent.addView(mStickerSection, mLayoutParams);
			}
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
			final int height = mStickerSection.getHeight();

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
		canvas.translate(0, mStickerMargin);
		mStickerSection.draw(canvas);
		canvas.translate(0, - mStickerMargin);
	}




	/**
	 * OnScrollListener for tracking changes of children's positions
	 */
	private class StickyScrollListener implements OnScrollListener {
		private OnScrollListener internalOnScrollListener;

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			if (isStickyScroll) {
				createSticker(firstVisibleItem);
				catchNextSection(firstVisibleItem);
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
