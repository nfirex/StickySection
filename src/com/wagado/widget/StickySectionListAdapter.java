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