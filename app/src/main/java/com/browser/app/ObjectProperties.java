package com.browser.app;

import java.util.HashMap;
import java.util.Iterator;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.browser.app.dao.BrowserDao;
import com.browser.engine.db.BConnection;
import com.browser.engine.ui.CActivity;
import com.browser.engine.ui.CTextView;

public class ObjectProperties extends CActivity {

	
	private BConnection mBConnection;
	private BrowserDao mDao;

	private String object;
	private boolean isTableOrView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sc_properties);

		object = this.getIntent().getExtras().getString("object");
		isTableOrView = this.getIntent().getExtras().getBoolean("is.table.view");

		mBConnection = BConnection.getInstance(this);
		mDao = new BrowserDao(this);
		String status = BConnection.connectToActive(mDao);
		setStatusText(status);

		View ok = findViewById(R.id.b_okProps);
		ok.setOnClickListener(mClickOk);

		setProperties();

	}

	@Override
	public void onBackPressed() {
		doneView();
		super.onBackPressed();
	}

	private void doneView() {
		setResult(Query.RESULT_CANCELED);
		finish();
	}

	private void setProperties() {

		TableRow.LayoutParams lp = new TableRow.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		TableRow.LayoutParams ledp = new TableRow.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		lp.setMargins(1, 1, 1, 1);
		TableLayout tl = (TableLayout) findViewById(R.id.tblColumns);
		tl.removeAllViews();

		ledp.height = 40;
		ledp.rightMargin = 10;

		if (isTableOrView) {
			findViewById(R.id.ly_columns).setVisibility(View.VISIBLE);
			findViewById(R.id.ly_count).setVisibility(View.VISIBLE);
		}

		HashMap<String, String> oData = mBConnection.getObjectProperties(object, isTableOrView);
		Iterator<String> keys = oData.keySet().iterator();

		while (keys.hasNext()) {
			String k = keys.next();

			if (k.equalsIgnoreCase("object.browser.name")) {
				((TextView) findViewById(R.id.property_obj_name)).setText(oData.get(k));

			} else if (k.equalsIgnoreCase("object.browser.sql")) {
				((EditText) findViewById(R.id.property_obj_sql)).setText(oData.get(k));

			} else if (k.equalsIgnoreCase("object.browser.count")) {
				((TextView) findViewById(R.id.property_obj_count)).setText(oData.get(k));

			} else if (k.startsWith("object.browser.cname")) {
				TableRow row = new TableRow(this);
				CTextView cName = new CTextView(this);
				cName.setText(oData.get(k));
				cName.setTextColor(ColorStateList.valueOf(Color.WHITE));
				cName.setGravity(android.view.Gravity.LEFT);
				cName.setLayoutParams(lp);
				row.addView(cName);
				tl.addView(row, ledp);
			}
		}

	}

	public OnClickListener mClickOk = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			onBackPressed();
		}
	};

}
