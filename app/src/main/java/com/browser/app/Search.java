package com.browser.app;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.browser.app.dao.BrowserDao;
import com.browser.app.exceptions.BrowserException;
import com.browser.app.util.BPermmits;
import com.browser.engine.search.BSearcher;
import com.browser.engine.search.BSearcher.SearchEngine;
import com.browser.engine.ui.CListActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.SQLException;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class Search extends CListActivity {

	private static final Logger logger = Logger.getLogger(Search.class.getName());
	private ProgressDialog mPd = null;
	private BSearcher mSearch = null;
	private TextView mSearchText;
	private View mOptionText;
	private BrowserDao mDao;

	private final int CONTEXT_MENU_CONNECTION = 1;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sc_search);
		findViewById(R.id.b_find).setOnClickListener(mFind);

		mSearchText = (TextView) findViewById(R.id.filtertext);
		fillSearchList();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		menu.add(Menu.NONE, CONTEXT_MENU_CONNECTION, Menu.NONE, getResourceString(R.string.CONNECT));
	}

	private void lockScreenRotation() {

		switch (this.getResources().getConfiguration().orientation) {
		case Configuration.ORIENTATION_PORTRAIT:
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			break;
		case Configuration.ORIENTATION_LANDSCAPE:
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			break;
		}
	}

	private void unlockScreenRotation() {
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
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
		boolean selected = false;
		switch (item.getItemId()) {
		case CONTEXT_MENU_CONNECTION:
			selected = saveConnection(((TextView) mOptionText.findViewById(R.id.search_text_row)).getText().toString(), ((TextView) mOptionText.findViewById(R.id.search_path_row)).getText().toString());
			if (selected) {
				gotoScreen(Connections.class);
			}
			break;
		}
		return selected;
	}

	private void fillSearchList() {
		mOptionText = null;
		String[] sourceName = new String[] { BSearcher.DB_NAME, BSearcher.PATH };
		int[] target = new int[] { R.id.search_text_row, R.id.search_path_row };

		SimpleAdapter connections = new SimpleAdapter(this, BSearcher.getDbs(), R.layout.sc_row, sourceName, target);
		setListAdapter(connections);

		ListView lv = getListView();
		lv.setTextFilterEnabled(false);
		lv.setOnItemClickListener(mListClicked);
		registerForContextMenu(lv);
	}

	private OnItemClickListener mListClicked = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			mOptionText = view;
			Search.this.openContextMenu(getListView());
		}
	};

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (mSearch.isStoped() == false) {
				mPd.dismiss();
				fillSearchList();
				unlockScreenRotation();
			}

		}
	};

	private OnCancelListener mCancel = new OnCancelListener() {

		@Override
		public void onCancel(DialogInterface arg0) {
			mHandler.sendEmptyMessage(0);
			mSearch.stopThread();
		}
	};

	private OnClickListener mFind = new OnClickListener() {

		@Override
		public void onClick(View arg0) {

			((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(Search.this.getCurrentFocus().getWindowToken(), 0);
			BSearcher.getDbs().clear();

			RadioGroup rg = (RadioGroup) findViewById(R.id.radioGroup);
			int ck = rg.getCheckedRadioButtonId();
			switch (ck) {
			case R.id.radioApps:

				startSearch(SearchEngine.Apps);
				break;
			case R.id.radioStorage:
				if (mSearchText.getText().toString() == null || mSearchText.getText().toString().equals("")) {
					mSearchText.setText(".db");
				}
				startSearch(SearchEngine.Storage);

				break;
			default:
				break;
			}

		}
	};

	private void startSearch(SearchEngine engine) {
		lockScreenRotation();
		mPd = ProgressDialog.show(Search.this, null, getResourceString(R.string.SRC_SEARCHING), true, true, mCancel);
		String filter = null;
		if (mSearchText != null) {
			filter = mSearchText.getText().toString();
		}
		mSearch = new BSearcher(mHandler, filter, engine);
		mSearch.start();
	}

	private boolean saveConnection(String cName, String path) {
		boolean saved = true;
		try {
			if (mDao == null) {
				mDao = new BrowserDao(this);
			}
			BPermmits p = BPermmits.getInstance();
			p.rootAccess();
			p.makeReadable(path);
			mDao.saveConnection(cName, path);
		} catch (SQLException e) {
			saved = false;
			logger.log(Level.INFO, "ERROR Search - saveConnection: \n" + e);
		} catch (BrowserException e) {
			saved = false;
			showMessage(e.getMessage());
		}
		return saved;
	}

}
