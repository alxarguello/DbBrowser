package com.browser.app.ui.vw;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.OnItemClickListener;

import com.browser.app.BrowserActivity;
import com.browser.app.R;
import com.browser.app.dao.BrowserDao;

public class HomeView extends BrowserActivity {

	private BrowserDao mDao;
	private ListView list;
	private View mOptionText;
	private int menuViewResource, appViewResource;

	public HomeView() {
		super();
		menuViewResource = R.layout.browser_menu_layout;
		appViewResource = R.layout.browser_connections_layout;

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initSinisterMenu();
		mDao = new BrowserDao(this);
		mDao.open();
		fillConnections();
	}

	private void fillConnections() {
		try {
			list = (ListView) findViewById(R.id.connectionList);
			list.invalidate();
			Cursor connectionCursor = mDao.retrieveConnections();
			startManagingCursor(connectionCursor);
			String[] sourceName = new String[] { BrowserDao.C_CONNECTION_NAME,
					BrowserDao.C_PATH };
			int[] target = new int[] { R.id.connections_text_row,
					R.id.connections_path_row };

			SimpleCursorAdapter connections = new SimpleCursorAdapter(this, R.layout.browser_connections_row_layout, connectionCursor, sourceName, target);
			list.setAdapter(connections);

			list.setTextFilterEnabled(false);
			list.setOnItemClickListener(mListClicked);
			registerForContextMenu(list);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private OnItemClickListener mListClicked = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			mOptionText = view;
			HomeView.this.openContextMenu(list);
		}
	};
}
