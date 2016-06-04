package com.browser.engine.ui;

import java.io.Serializable;
import java.util.List;

import com.browser.app.R;
import com.browser.app.util.Util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class CActivity extends Activity {

	protected void gotoScreen(Class<? extends Activity> screen) {
		Intent itt = new Intent(this, screen);
		startActivity(itt);
	}

	protected void gotoScreenForResult(Class<? extends Activity> screen) {
		gotoScreenForResult(screen, null, null);
	}

	protected void gotoScreenForResult(Class<? extends Activity> screen, String extrasName, Serializable extras) {
		Intent itt = new Intent(this, screen);
		itt.putExtra(extrasName, extras);
		gotoScreenForResult(itt);
	}

	protected void gotoScreenForResult(Class<? extends Activity> screen, List<? extends Serializable> extras) {
		Intent itt = new Intent(this, screen);
		int e = 1;
		for (Serializable s : extras) {
			itt.putExtra("ex" + (e++), s);
		}
		gotoScreenForResult(itt);
	}

	protected void gotoScreenForResult(Class<? extends Activity> screen, Bundle extras) {
		gotoScreenForResult(screen, extras, 0);
	}

	protected void gotoScreenForResult(Class<? extends Activity> screen, Bundle extras, int code) {
		Intent itt = new Intent(this, screen);
		itt.putExtra("extras", extras);
		gotoScreenForResult(itt, code);
	}

	private void gotoScreenForResult(Intent itt) {
		gotoScreenForResult(itt, 0);
	}

	private void gotoScreenForResult(Intent itt, int code) {
		startActivityForResult(itt, code);
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
		builder.setMessage(message).setPositiveButton("Yes", yesNoListener).setNegativeButton("No", yesNoListener).show();
	}

	protected void clearErrorMessage() {
		View e = findViewById(R.id.btnError);
		e.setClickable(false);
		e.setVisibility(View.INVISIBLE);
	}

	protected void setErrorMessage(final String message) {

		View e = findViewById(R.id.btnError);
		e.setClickable(true);
		e.setVisibility(View.VISIBLE);
		e.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				showMessage(message);

			}
		});

	}
	
	protected String getResourceString(int stringId) {
		return Util.getResourceString(stringId, this);
	}
	
	public void setStatusText(String text) {
		((TextView) findViewById(R.id.statusText)).setText(text);

	}
	
	public void toastMessage(String message){
		Toast t  = Toast.makeText(this, message, 10);
		t.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
		t.show();
	}

}
