package com.hbm.devices.scan.ui.android;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.hbm.devices.scan.filter.FamilytypeMatch;
import com.hbm.devices.scan.filter.Filter;
import com.hbm.devices.scan.filter.UUIDMatch;

public class FilterFragment extends Fragment implements OnItemClickListener {

	private ScanActivity activity;

	private FilterListAdapter adapter;

	public FilterFragment() {
		this.activity = (ScanActivity) ScanActivity.activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.filters, container, false);

		final EditText filterStringEdit = (EditText) view
				.findViewById(R.id.filter_string_edit);

		final ListView filtersListView = (ListView) view
				.findViewById(R.id.filters_list_view);
		adapter = new FilterListAdapter(activity);
		filtersListView.setAdapter(adapter);
		filtersListView.setOnItemClickListener(this);

		Button familityTypeBtn = (Button) view
				.findViewById(R.id.filter_add_family_type_btn);
		familityTypeBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String[] famType = new String[] { filterStringEdit.getText()
						.toString() };
				activity.filterList
						.add(new Filter(new FamilytypeMatch(famType)));
				filterStringEdit.setText("");
				adapter.notifyDataSetChanged();
			}
		});

		Button uuidBtn = (Button) view.findViewById(R.id.filter_add_uuid_btn);
		uuidBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String[] uuids = new String[] { filterStringEdit.getText()
						.toString() };
				activity.filterList.add(new Filter(new UUIDMatch(uuids)));
				filterStringEdit.setText("");
				adapter.notifyDataSetChanged();
			}
		});

		return view;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		activity.filterList.remove(position);
		adapter.notifyDataSetChanged();

	}

	private class FilterListAdapter extends BaseAdapter {

		private LayoutInflater layoutInflater;

		public FilterListAdapter(Activity activity) {
			layoutInflater = (LayoutInflater) activity
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return activity.filterList.size();
		}

		@Override
		public Filter getItem(int position) {
			return activity.filterList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolderItem viewHolder;

			if (convertView == null) {
				convertView = layoutInflater.inflate(R.layout.filter_item,
						parent, false);
				viewHolder = new ViewHolderItem();
				viewHolder.filterType = (TextView) convertView
						.findViewById(R.id.filter_type);
				viewHolder.filterString = (TextView) convertView
						.findViewById(R.id.filter_string);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolderItem) convertView.getTag();
			}

			Filter filter = getItem(position);
			if (filter != null) {
				viewHolder.filterType.setText(filter.getMatcher()
						.getMatcherName());
				StringBuffer buffer = new StringBuffer();
				for (String s : filter.getMatcher().getFilterStrings()) {
					buffer.append(s).append(", ");
				}
				// don't display the last ", "
				viewHolder.filterString.setText(buffer.substring(0,
						buffer.length() - 2));
			}

			return convertView;
		}

	}

	private static class ViewHolderItem {
		TextView filterType;
		TextView filterString;
	}

}
