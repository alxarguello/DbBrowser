package com.browser.app;

import com.browser.app.dao.BrowserDao;
import com.browser.engine.db.BConnection;
import com.browser.engine.ui.CListActivity;

import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class Connections extends CListActivity {

	private BrowserDao mDao;
	private View mOptionText;

	private static final int CM_CONNECT_THIS = 1;
	private static final int CM_EXPLORE_THIS = 2;
	private static final int CM_DELETE_THIS = 3;
	private static final int CM_QUERY_THIS = 4;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.browser_connections_layout);
		mDao = new BrowserDao(this);
		mDao.open();
		fillConnections();
	}

	private void fillConnections() {
		getListView().invalidate();
		Cursor connectionCursor = mDao.retrieveConnections();
		startManagingCursor(connectionCursor);
		String[] sourceName = new String[] { BrowserDao.C_CONNECTION_NAME,
				BrowserDao.C_PATH };
		int[] target = new int[] { R.id.connections_text_row,
				R.id.connections_path_row };

		SimpleCursorAdapter connections = new SimpleCursorAdapter(this, R.layout.browser_connections_row_layout, connectionCursor, sourceName, target);
		setListAdapter(connections);

		ListView lv = getListView();
		lv.setTextFilterEnabled(false);
		lv.setOnItemClickListener(mListClicked);
		registerForContextMenu(lv);

	}

	private OnItemClickListener mListClicked = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			mOptionText = view;
			Connections.this.openContextMenu(getListView());
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.connections_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {

		case R.id.mDelConnections:
			showYesNoDialog(getResourceString(R.string.CONN_EREASEALL), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case DialogInterface.BUTTON_POSITIVE:
						deleteConnections();
						break;
					}
				}
			});

		case R.id.mDisconnectAll:
			clearConnections();
			return true;
		case R.id.mNewConnection:
			gotoScreen(Search.class);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

		if (mOptionText != null) {
			String name = ((TextView) mOptionText.findViewById(R.id.connections_text_row)).getText().toString();
			if (name.endsWith(BrowserDao.C_ACTIVE)) {
				menu.add(Menu.NONE, CM_QUERY_THIS, Menu.NONE, getResourceString(R.string.CONN_EXECUTEQUERY));
				menu.add(Menu.NONE, CM_EXPLORE_THIS, Menu.NONE, getResourceString(R.string.CONN_EXPLORE));
			} else {
				menu.add(Menu.NONE, CM_CONNECT_THIS, Menu.NONE, getResourceString(R.string.CONNECT));

			}
		}
		menu.add(Menu.NONE, CM_DELETE_THIS, Menu.NONE, getResourceString(R.string.CONN_DELETE));

	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {

		/*
		 * what item was selected is ListView
		 */

		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		if (mOptionText == null) {
			mOptionText = info.targetView;
		}
		final String name = ((TextView) mOptionText.findViewById(R.id.connections_text_row)).getText().toString();
		final String path = ((TextView) mOptionText.findViewById(R.id.connections_path_row)).getText().toString();

		switch (item.getItemId()) {
		case CM_CONNECT_THIS:
			showYesNoDialog(getResourceString(R.string.CONN_CONNECTTHIS), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case DialogInterface.BUTTON_POSITIVE:
						connectThis(name, path);
						break;
					}
				}
			});
			break;
		case CM_EXPLORE_THIS:
			gotoScreen(Explorer.class);
			break;
		case CM_DELETE_THIS:
			showYesNoDialog(getResourceString(R.string.CONN_DELETETHIS), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case DialogInterface.BUTTON_POSITIVE:
						deleteConnection(path);
						break;
					}
				}
			});
			break;
		case CM_QUERY_THIS:
			gotoScreen(Query.class);
			break;
		}
		return true;
	}

	private void deleteConnections() {
		mDao.deleteAllConnections();
		fillConnections();
	}

	private void clearConnections() {
		mDao.clearActiveConnections();
		fillConnections();
	}

	private void deleteConnection(String path) {
		mDao.deleteConnection(path);
		fillConnections();
	}

	private void connectThis(String name, String path) {
		mDao.setActiveConnection(path);
		BConnection.connectTo(name, path);
		fillConnections();
	}

}
