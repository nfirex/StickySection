package com.wagado.widget;

import java.util.Iterator;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;
import ru.camino.parts.adapter.SectionListAdapter;

public class StickySectionListAdapter extends SectionListAdapter implements SectionIndexer {
	protected static final String TAG = "StickySectionListAdapter";

	protected final Context mContext;
	protected final int mHeaderLayoutId;
	protected final int mTitleTextViewId;

	private int mCurrentSection = -1;

	public StickySectionListAdapter(Context context, BaseAdapter adapter, SectionListAdapter.SectionDetector detector) {
		this(context, adapter, detector, android.R.layout.preference_category, android.R.id.title);
	}

	public StickySectionListAdapter(Context context, BaseAdapter adapter, SectionListAdapter.SectionDetector detector, int headerLayoutId, int titleTextViewId) {
		super(adapter, detector);
		mContext = context;
		mHeaderLayoutId = headerLayoutId;
		mTitleTextViewId = titleTextViewId;
	}

	@Override
	protected View getSectionView(Object header, View convertView, ViewGroup parent) {
		View v;
		if (convertView != null) {
			v = convertView;
		} else {
			v = View.inflate(mContext, mHeaderLayoutId, null);
		}
		((TextView) v.findViewById(mTitleTextViewId)).setText(header.toString());

		return v;
	}

	@Override
	protected Object getSectionHeader(Object firstItem, Object secondItem) {
		if (getSectionDetector() != null) {
			return getSectionDetector().detectSection(firstItem, secondItem);
		} else {
			return null;
		}
	}

	public View getStickerSection(int position, View stickerSection) {
		final int section = getSectionByPosition(position);

		if (mCurrentSection != section) {
			mCurrentSection = section;
			return getView(mCurrentSection, stickerSection, null);
		}

		return null;
	}

	public int getSectionByPosition(int position) {
		final Iterator<Integer> iterator = getHeaders().keySet().iterator();

		int value = iterator.next();
		while (iterator.hasNext()) {
			final int value2 = iterator.next();

			if (position < value2) {
				return value;
			}

			value = value2;
		}

		return value;
	}


	@Override
	public int getPositionForSection(int section) {
		final Iterator<Integer> iterator = getHeaders().keySet().iterator();

		int count = 0;
		int value = iterator.next();
		while (iterator.hasNext()) {
			if (section == count) {
				break;
			}

			count ++;
			value = iterator.next();
		}

		return value;
	}

	@Override
	public int getSectionForPosition(int position) {
		final Iterator<Integer> iterator = getHeaders().keySet().iterator();

		int count = 0;
		while (iterator.hasNext()) {
			int value = iterator.next();
			if (value == position) {
				break;
			}

			count ++;
		}

		return count;
	}

	@Override
	public Object[] getSections() {
		return getHeaders().values().toArray();
	}
}