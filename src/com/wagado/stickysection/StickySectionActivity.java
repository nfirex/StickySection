package com.wagado.stickysection;

import java.util.ArrayList;
import java.util.List;

import com.wagado.widget.StickySectionListView;

import ru.camino.parts.adapter.SectionListAdapter;
import ru.camino.parts.adapter.SectionListAdapter.SectionDetector;
import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class StickySectionActivity extends ListActivity {

	private StickySectionListView mListView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mListView = (StickySectionListView) getListView();

		SectionListAdapter adapter = (SectionListAdapter) getLastNonConfigurationInstance();
		if (getLastNonConfigurationInstance() == null) {
			final int count = 10000;
			final List<String> list = new ArrayList<String>();
			for (int i = 1; i <= count; i ++) {
				list.add("element #" + Integer.toString(i));
			}
			adapter = createAdapter(new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_1, android.R.id.text1, list));
		}

		mListView.setAdapter(adapter);
		mListView.setFastScrollEnabled(true);
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		return mListView.getAdapter();
	}

	private SectionListAdapter createAdapter(BaseAdapter adapter) {
		final SectionDetector sectionDetector = new SectionDetector() {
			final private int portion = 19;

			private int count = 0;

			@Override
			public Object detectSection(Object arg0, Object arg1) {
				String title = null;

				if (count % portion == 0) {
					title = "section for " + Integer.toString(count + 1) + "-" + Integer.toString(count +portion);
				}

				count ++;

				return title;
			}
		};

		return new SectionListAdapter(adapter, sectionDetector) {
			protected final int mHeaderLayoutId = android.R.layout.preference_category;
			protected final int mTitleTextViewId = android.R.id.title;

			@Override
			protected View getSectionView(Object header, View convertView, ViewGroup parent) {
				View v;
				if (convertView != null) {
					v = convertView;
				} else {
					v = View.inflate(getBaseContext(), mHeaderLayoutId, null);
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
		};
	}
}