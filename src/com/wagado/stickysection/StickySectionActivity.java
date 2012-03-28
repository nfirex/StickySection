package com.wagado.stickysection;

import java.util.ArrayList;
import java.util.List;

import com.wagado.widget.StickySectionListAdapter;
import com.wagado.widget.StickySectionListView;

import ru.camino.parts.adapter.SectionListAdapter.SectionDetector;
import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;

public class StickySectionActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		final int count = 30000;
		final List<String> list = new ArrayList<String>();
		for (int i = 1; i <= count; i ++) {
			list.add("element ¹ " + Integer.toString(i));
		}
		final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_1, android.R.id.text1, list);

		final StickySectionListView listView = (StickySectionListView) findViewById(R.id.sticky_section_list);
		listView.setAdapter(createAdapter(arrayAdapter));
		listView.setFastScrollEnabled(true);
	}

	private StickySectionListAdapter createAdapter(BaseAdapter adapter) {
		final SectionDetector sectionDetector = new SectionDetector() {
			final private int portion = 43;

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

		return new StickySectionListAdapter(getBaseContext(), adapter, sectionDetector, android.R.layout.preference_category, android.R.id.title);
	}
}