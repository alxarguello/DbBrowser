package com.browser.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import com.browser.app.dao.BrowserDao;
import com.browser.app.exceptions.BrowserException;
import com.browser.app.util.Util;
import com.browser.engine.db.BConnection;

import android.app.ExpandableListActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class Explorer extends ExpandableListActivity {

	private SimpleExpandableListAdapter expListAdapter = null;
	private BConnection mBConnection;
	private BrowserDao mDao;
	private String selectedTableOrView;
	private String selectedObject;

	private static final int CM_PROPERTIES = 1;
	private static final int CM_DATA = 2;

	private static final ArrayList<String> groupTypes = new ArrayList<String>();

	static {
		groupTypes.add("Tables");
		groupTypes.add("Views");
		groupTypes.add("Indexes");
		groupTypes.add("Triggers");
	}

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sc_explorer);
		mBConnection = BConnection.getInstance(this);
		mDao = new BrowserDao(this);
		String status = BConnection.connectToActive(mDao);
		setStatusText(status);

		if (status == null || status.equals("")) {
			Toast t = Toast.makeText(this, getResourceString(R.string.NOACTIVECONN), Toast.LENGTH_LONG);
			t.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
			t.show();
		} else {
			fillData();
			registerForContextMenu(getExpandableListView());
		}
	}

	private void fillData() {
		try {
			if (mBConnection.checkDbFile()) {
				selectedTableOrView = selectedObject = null;
				expListAdapter = new SimpleExpandableListAdapter(this, createGroupList(), R.layout.sc_object_row, new String[] { "object.name" }, new int[] { R.id.exp_object_name }, createChildList(), R.layout.sc_subobject, new String[] { "sub.object.name" }, new int[] { R.id.exp_subobject_name });
				setListAdapter(expListAdapter);
			}
		} catch (BrowserException e) {
			Toast t = Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG);
			t.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
			t.show();
		}

	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
		selectedObject = selectedTableOrView = null;
		LinearLayout ll = (LinearLayout) v;
		if (groupPosition == 0 || groupPosition == 1) {
			selectedObject = selectedTableOrView = ((TextView) ll.getChildAt(0)).getText().toString();
		} else {
			selectedObject = ((TextView) ll.getChildAt(0)).getText().toString();
		}
		openContextMenu(getExpandableListView());
		return true;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

		menu.add(Menu.NONE, CM_PROPERTIES, Menu.NONE, getResourceString(R.string.EXPL_SEEPROP));
		if (selectedTableOrView != null) {
			menu.add(Menu.NONE, CM_DATA, Menu.NONE, getResourceString(R.string.EXPL_SEEDATA));
		}

	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {

		/*
		 * what item was selected is ListView
		 */

		// AdapterView.AdapterContextMenuInfo info =
		// (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

		switch (item.getItemId()) {
		case CM_PROPERTIES:
			gotoPropertiesScreen(selectedObject, selectedTableOrView != null);
			break;
		case CM_DATA:
			gotoQueryScreen(selectedTableOrView);
			break;
		}
		return true;
	}

	private List<HashMap<String, String>> createGroupList() {
		ArrayList<HashMap<String, String>> result = new ArrayList<HashMap<String, String>>();
		for (String gt : groupTypes) {
			HashMap<String, String> m = new HashMap<String, String>();
			m.put("object.name", gt);
			result.add(m);
		}
		return result;
	}

	private List<ArrayList<HashMap<String, String>>> createChildList() {

		ArrayList<ArrayList<HashMap<String, String>>> result = new ArrayList<ArrayList<HashMap<String, String>>>();
		List<TreeMap<String, Integer>> objects = mBConnection.getDBObjects();

		ArrayList<HashMap<String, String>> tables = new ArrayList<HashMap<String, String>>();
		ArrayList<HashMap<String, String>> views = new ArrayList<HashMap<String, String>>();
		ArrayList<HashMap<String, String>> indexes = new ArrayList<HashMap<String, String>>();
		ArrayList<HashMap<String, String>> triggers = new ArrayList<HashMap<String, String>>();

		for (TreeMap<String, Integer> o : objects) {
			Iterator<String> ii = o.keySet().iterator();
			while (ii.hasNext()) {
				String name = ii.next();
				HashMap<String, String> m = new HashMap<String, String>();
				switch (o.get(name)) {

				case BConnection.TYPE_TABLE:
					m.put("sub.object.name", name);
					tables.add(m);
					break;
				case BConnection.TYPE_VIEW:
					m.put("sub.object.name", name);
					views.add(m);
					break;
				case BConnection.TYPE_INDEX:
					m.put("sub.object.name", name);
					indexes.add(m);
					break;
				case BConnection.TYPE_TRIGGER:
					m.put("sub.object.name", name);
					triggers.add(m);
					break;
				}
			}
		}

		/**
		 * Deben ser indexados en el exacto orden :
		 * {tables,views,indexes,triggers}
		 */
		result.add(tables);
		result.add(views);
		result.add(indexes);
		result.add(triggers);

		return result;
	}

	public void setStatusText(String text) {
		Object ob = findViewById(R.id.statusText);
		if (ob != null && ob instanceof TextView) {
			((TextView) ob).setText(text);
		}
	}

	private void gotoPropertiesScreen(String extrasValue, boolean isTableOrView) {
		Intent itt = new Intent(this, ObjectProperties.class);
		itt.putExtra("object", extrasValue);
		itt.putExtra("is.table.view", isTableOrView);
		startActivityForResult(itt, 0);

	}

	private void gotoQueryScreen(String extrasValue) {
		Intent itt = new Intent(this, Query.class);
		itt.putExtra("table", extrasValue);
		startActivityForResult(itt, 0);
	}

	private String getResourceString(int stringId) {
		return Util.getResourceString(stringId, this);
	}
}
