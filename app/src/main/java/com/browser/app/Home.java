package com.browser.app;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.browser.app.dao.BrowserDao;
import com.browser.engine.ui.CListActivity;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Home extends CListActivity {

	private BrowserDao mDao;
	private View mOptionText;
	private static final Logger logger = Logger.getLogger(Home.class.getName());

	private static final int QH_SEE_THIS = 1;
	private static final int QH_USE_THIS = 2;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sc_h);

		mDao = new BrowserDao(this);
		String[] st = mDao.retrieveActiveConnection();
		if (st != null) {
			setStatusText(st[0]);
		}
		mDao.open();
		/**
		 * Elementos del Home
		 */
		findViewById(R.id.connections).setOnClickListener(mConnectionsListener);
		findViewById(R.id.search).setOnClickListener(mSearchListener);
		findViewById(R.id.query).setOnClickListener(mQueryListener);
		findViewById(R.id.explorer).setOnClickListener(mExplorerListener);

		fillQuerys();


	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.browser_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.mContact:
			Intent eMail = new Intent(Intent.ACTION_SEND);
			eMail.putExtra(Intent.EXTRA_EMAIL, new String[] { "alx.arguello@gmail.com" });
			eMail.putExtra(Intent.EXTRA_SUBJECT, "Support DB Browser");
			eMail.setType("plain/text");
			startActivity(eMail);

			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void fillQuerys() {
		try {
			getListView().invalidate();
			Cursor querysCursor = mDao.retrieveLastestQuerys();
			if (querysCursor != null) {
				startManagingCursor(querysCursor);
				String[] sourceName = new String[] { BrowserDao.QR_LASTEXC,
						BrowserDao.QR_QUERY };
				int[] target = new int[] { R.id.search_text_row,
						R.id.search_path_row };

				SimpleCursorAdapter connections = new SimpleCursorAdapter(this, R.layout.sc_row, querysCursor, sourceName, target);
				setListAdapter(connections);

				ListView lv = getListView();
				lv.setTextFilterEnabled(false);
				lv.setOnItemClickListener(mListClicked);
				registerForContextMenu(lv);
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.toString());
		}
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
		final String date = ((TextView) mOptionText.findViewById(R.id.search_text_row)).getText().toString();
		final String query = ((TextView) mOptionText.findViewById(R.id.search_path_row)).getText().toString();

		switch (item.getItemId()) {
		case QH_SEE_THIS:
			showQuery(date, query);
			break;
		case QH_USE_THIS:
			gotoScreen(Query.class, "customQuery", query);
			break;
		}
		return true;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

		menu.add(Menu.NONE, QH_SEE_THIS, Menu.NONE, getResourceString(R.string.HOME_SEE_QR));
		menu.add(Menu.NONE, QH_USE_THIS, Menu.NONE, getResourceString(R.string.HOME_USE_QR));
	}

	public void showQuery(String dateExecuted, String value) {
		final Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.r_edit);
		dialog.setTitle(getString(R.string.HOME_EXCE_ON) + dateExecuted);
		((EditText) dialog.findViewById(R.id.edit)).setText(value);
		dialog.findViewById(R.id.b_edit).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});
		dialog.show();
	}

	private OnItemClickListener mListClicked = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			mOptionText = view;
			Home.this.openContextMenu(getListView());
		}
	};

	private OnClickListener mConnectionsListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			gotoScreen(Connections.class);
		}
	};

	private OnClickListener mSearchListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			gotoScreen(Search.class);
		}
	};

	private OnClickListener mQueryListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			gotoScreen(Query.class);
		}
	};

	private OnClickListener mExplorerListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			gotoScreen(Explorer.class);
		}
	};

}