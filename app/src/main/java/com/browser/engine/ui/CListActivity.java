package com.browser.engine.ui;

import com.browser.app.R;
import com.browser.app.util.Util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.TextView;

public class CListActivity extends ListActivity {

	protected void gotoScreen(Class<? extends Activity> screen) {
		gotoScreen(screen, null, null);
	}

	protected void gotoScreen(Class<? extends Activity> screen, String bundleName, String bundleValue) {
		Intent itt = new Intent(this, screen);
		if (bundleName != null && bundleName.equals("") == false && bundleValue != null) {
			itt.putExtra(bundleName, bundleValue);
		}
		startActivity(itt);
	}

	protected void showMessage(String message) {
		showMessage(null, message);
	}

	protected void showMessage(String title, String message) {
		final AlertDialog d = new AlertDialog.Builder(this).create();
		if (title != null && title.equals("") == false) {
			d.setTitle(message);
		}
		d.setMessage(message);
		d.setButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				d.dismiss();
			}
		});
		d.show();
	}

	protected void showYesNoDialog(String message, DialogInterface.OnClickListener yesNoListener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message).setPositiveButton(getResourceString(R.string.AFFIRMATIVE), yesNoListener).setNegativeButton("No", yesNoListener).show();
	}

	protected String getResourceString(int stringId) {
		return Util.getResourceString(stringId, this);
	}

	public void setStatusText(String text) {
		try {
			((TextView) findViewById(R.id.statusText)).setText(text);
		} catch (Exception e) {

		}
	}
}
