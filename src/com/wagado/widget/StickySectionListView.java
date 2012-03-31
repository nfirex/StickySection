package com.wagado.widget;

import java.util.Iterator;

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
	public static final int NOT_VALUE = -1;

	protected static final String TAG = "StickySectionListView";

	private View mStickerSection;

	private FrameLayout mParent;
	private FrameLayout.LayoutParams mLayoutParams;

	private Bitmap mStickerBitmap;

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

			if (mStickerBitmap != null) {
				mStickerBitmap.recycle();
				mStickerBitmap = null;
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
		final boolean isNextSection = ((StickySectionListAdapter) getAdapter()).isHeader(position);
		if (isNextSection) {
			mNextSectionChild = 1;
		} else {
			mNextSectionChild = NOT_VALUE;
		}
	}

	/**
	 * Инициализация секции, которая будет смещать "Стикер" по переданной позиции.
	 * @param position - позиция секции в списке
	 */
	protected void calculateStickerMargin () {
		if (mNextSectionChild != NOT_VALUE) {
			final int top = getChildAt(mNextSectionChild).getTop();
			final int height = mStickerSection.getMeasuredHeight();

			if (top < 0 || top > height) {
				mStickerMargin = 0;
			} else {
				mStickerMargin = top - height;
			}
		} else {
			mStickerMargin = 0;
		}

//		mStickerSection.scrollTo(0, mStickerMargin);
	}

	/**
	 * Рисование "стикера" на переданном Canvas. Рисование идет после детей, перед эффектами и Scrollbar.
	 * @param canvas - Canvas на котором рисуются все элементы ListView
	 */
	protected void drawSticker(Canvas canvas) {
		if (mStickerBitmap == null) {
			mStickerBitmap = Bitmap.createBitmap(mStickerSection.getMeasuredWidth(), mStickerSection.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
			final Canvas canvas2 = new Canvas(mStickerBitmap);
			mStickerSection.draw(canvas2);
		}

		canvas.drawBitmap(mStickerBitmap, 0, mStickerMargin, null);
//		mStickerSection.draw(canvas);
	}




	/**
	 * OnScrollListener для отслеживания смены элементов списка. 
	 * Определяет какие элементы учавствуют в отображении и расположения "Стикера".
	 */
	private class StickyScrollListener implements OnScrollListener {
		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			createSticker(firstVisibleItem);
			catchNextSection(firstVisibleItem + 1);
		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			
		}
	};
}