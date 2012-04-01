/**
 * Copyright 2012 by Kleshchin Nikita (nfirex)
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

import java.util.Iterator;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

public class StickySectionListView extends ListView {
	public static final int NOT_VALUE = -1;

	protected static final String TAG = "StickySectionListView";

	private View mStickerSection;

	private FrameLayout mParent;
	private FrameLayout.LayoutParams mLayoutParams;

	private boolean isStickyScroll;

	private int mStickerMargin;
	private int mCurrentSection;
	private int mNextSectionChild;

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
		super.setAdapter(adapter);

		if (adapter instanceof StickySectionListAdapter) {
			mParent = (FrameLayout) getParent();
			mLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
			mCurrentSection = 0;

			createSticker(mCurrentSection);

			setOnScrollListener(new StickyScrollListener());
		} else {
			mParent = null;
			mLayoutParams = null;
			mStickerSection = null;

			if (isStickyScroll) {
				setOnScrollListener(null);
			}
		}
	}

	@Override
	public void setOnScrollListener(OnScrollListener l) {
		super.setOnScrollListener(l);

		isStickyScroll = l instanceof StickyScrollListener;
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
	 * Получить позицию секции, которой принадлежит элемент
	 * @param position - позиция элемента, для которого ищется позиция секции
	 */
	protected int getSectionByPosition(int position) {
		final Iterator<Integer> iterator = ((StickySectionListAdapter) getAdapter()).getHeaders().keySet().iterator();

		int value = iterator.next();

		if (position < value) {
			return NOT_VALUE;
		}

		while (iterator.hasNext()) {
			final int value2 = iterator.next();
			if (position < value2) {
				break;
			}

			value = value2;
		}

		return value;
	}

	/**
	 * Инициализация "Стикера" по переданной позиции.
	 * @param position - позиция секции в списке
	 */
	protected void createSticker (int position) {
		final int section = getSectionByPosition(position);
		if (mCurrentSection != section) {
			mCurrentSection = section;

			if (mCurrentSection == NOT_VALUE) {
				mStickerSection = null;
			} else {
				mStickerSection = getAdapter().getView(mCurrentSection, mStickerSection, null);
			}

			if (mStickerSection != null) {
				mParent.removeView(mStickerSection);

				mStickerSection.setDrawingCacheEnabled(true);
				mStickerSection.setVisibility(View.INVISIBLE);

				mParent.addView(mStickerSection, mLayoutParams);
			}
		}
	}

	/**
	 * Инициализация секции, которая будет смещать "Стикер" по переданной позиции.
	 * @param position - позиция секции в списке
	 */
	protected void catchNextSection (int position) {
		final int delta = 1;
		final boolean isNextSection = ((StickySectionListAdapter) getAdapter()).isHeader(position + delta);
		if (isNextSection) {
			mNextSectionChild = delta;
		} else {
			mNextSectionChild = NOT_VALUE;
		}
	}

	/**
	 * Расчет отсупа "Стикера" от верхней границы StickySectionListView
	 */
	protected void calculateStickerMargin () {
		if (mNextSectionChild != NOT_VALUE) {
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
	 * Рисование "стикера" на переданном Canvas. Рисование идет после детей, перед эффектами и Scrollbar.
	 * @param canvas - Canvas на котором рисуются все элементы ListView
	 */
	protected void drawSticker(Canvas canvas) {
		canvas.translate(0, mStickerMargin);
		mStickerSection.draw(canvas);
		canvas.translate(0, - mStickerMargin);
	}




	/**
	 * OnScrollListener для отслеживания смены элементов списка. 
	 * Определяет какие элементы учавствуют в отображении и расположения "Стикера".
	 */
	private class StickyScrollListener implements OnScrollListener {
		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			createSticker(firstVisibleItem);
			catchNextSection(firstVisibleItem);
		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			
		}
	};

	static class SavedState extends BaseSavedState {
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
			return TAG + ".SavedState{"
					+ " currentStickerSection=" + Integer.toString(currentStickerSection)
					+ "}";
		}
	}
}