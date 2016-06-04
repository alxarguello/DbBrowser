package com.browser.app;

import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class BrowserActivity extends Activity {

	private TextView mnuConnections;
	private TextView mnuExploreDb;
	private TextView mnuQuerySQL;
	private TextView mnuSearchDb;

	protected void initSinisterMenu() {
		try {

			mnuConnections = (TextView) findViewById(R.id.mnuConnections);
			mnuExploreDb = (TextView) findViewById(R.id.mnuExploreDb);
			mnuQuerySQL = (TextView) findViewById(R.id.mnuQuerySQL);
			mnuSearchDb = (TextView) findViewById(R.id.mnuSearchDb);

			mnuConnections.setOnClickListener(connectionsListener);
			mnuExploreDb.setOnClickListener(exploreListener);
			mnuQuerySQL.setOnClickListener(queryListener);
			mnuSearchDb.setOnClickListener(searchListener);

		} catch (Exception e) {
			Log.e("AXAActivity", e.getMessage());
		}

	}

	@Override
	public void onBackPressed() {

	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		//showHideMenu();
		return true;
	}

	private OnClickListener connectionsListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

		}
	};

	private OnClickListener exploreListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

		}
	};

	private OnClickListener queryListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

		}
	};
	private OnClickListener searchListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

		}
	};

}
