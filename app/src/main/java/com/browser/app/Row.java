package com.browser.app;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableRow;

import com.browser.engine.db.BColumn;
import com.browser.engine.db.BMetadata;
import com.browser.engine.ui.CActivity;
import com.browser.engine.ui.CTextView;

public class Row extends CActivity {

	private Dialog dialog;
	private BColumn selectedColumn;
	private EditText selectedText;
	private Bundle b = null;
	private Bundle resultBundle;
	private String table = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sc_row_edition);
		b = this.getIntent().getExtras().getBundle("extras");
		table = b.getString("table.edition.from");
		// boolean newRow = b.getBoolean("table.new.row");

		View cancel = findViewById(R.id.b_cancelEdition);
		View done = findViewById(R.id.b_doneEdition);
		View ok = findViewById(R.id.b_okView);
		if (table != null && table.equals("") == false) {

			cancel.setOnClickListener(mClickCancel);
			done.setOnClickListener(mClickDone);

		} else {
			done.setVisibility(View.GONE);
			done.setEnabled(false);
			cancel.setVisibility(View.GONE);
			cancel.setEnabled(false);

			ok.setVisibility(View.VISIBLE);
			ok.setEnabled(true);

			ok.setOnClickListener(mClickOk);
		}

		TableRow.LayoutParams lp = new TableRow.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		TableRow.LayoutParams ledp = new TableRow.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		// ledp.height = 40;
		ledp.rightMargin = 10;
		LinearLayout v = (LinearLayout) findViewById(R.id.row_layout);

		/**
		 * es necesario traer la lista de columnas para mostrarlas en el orden
		 * adecaudo
		 */
		for (BMetadata m : Query.getMetaData()) {
			String cName = m.getColumnName();
			CTextView tv = new CTextView(this);
			tv.setText(cName);
			tv.setLayoutParams(lp);
			v.addView(tv);

			EditText etx = new EditText(this);
			if (cName.equals("_id")) {
				etx.setFocusable(false);

				etx.setText("(Auto)");
			}

			Object c = b.get(cName);
			if (c != null) {
				if (c instanceof BColumn) {
					BColumn bc = (BColumn) c;
					etx.setTag(bc);

					if (bc.isExtendedData()) {
						etx.setText(bc.getShowValue());
						etx.setOnClickListener(mClickColumn);
						etx.setFocusable(false);
					} else {
						etx.setText(bc.getShowValue());
					}

				}
			} else {
				etx.setTag(new BColumn(cName));
			}

			etx.setLayoutParams(ledp);
			v.addView(etx);
		}
	}

	@Override
	public void onBackPressed() {
		cancelEdition();
		super.onBackPressed();
	}

	public void showColumnEdition(String columName, String value) {
		dialog = new Dialog(this);
		dialog.setContentView(R.layout.r_edit);
		dialog.setTitle(columName);
		((EditText) dialog.findViewById(R.id.edit)).setText(value);
		dialog.findViewById(R.id.b_edit).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String newValue = ((EditText) dialog.findViewById(R.id.edit)).getText().toString();
				selectedColumn.renderColumnData(newValue);
				updateView();
				dialog.dismiss();
			}
		});

		dialog.show();
	}

	private void updateView() {
		selectedText.setText(selectedColumn.getShowValue());
	}

	private void cancelEdition() {
		setResult(Query.RESULT_CANCELED);
		finish();
	}

	private void doneEdition() {
		Intent intt = new Intent();
		intt.putExtra("edition", resultBundle);
		setResult(Query.RESULT_OK, intt);

		finish();
	}

	public void updateRowData() {

		resultBundle = new Bundle();
		LinearLayout v = (LinearLayout) findViewById(R.id.row_layout);
		for (int vs = 0; vs < v.getChildCount(); vs++) {
			View et = v.getChildAt(vs);
			if (et instanceof EditText) {
				EditText tx = ((EditText) et);
				BColumn fn = null;
				if (tx.isFocusable()) {
					BColumn edited = ((BColumn) tx.getTag());
					String x = tx.getText().toString();
					Object col = b.get(edited.getName());
					if (col != null) {
						fn = (BColumn) col;
					} else {
						fn = new BColumn(edited.getName());
					}
					fn.setNewData(x);
				} else {
					fn = ((BColumn) tx.getTag());
				}
				if (fn != null) {
					resultBundle.putSerializable(fn.getName(), fn);
				}

			}

		}

	}

	public OnClickListener mClickColumn = new OnClickListener() {

		@Override
		public void onClick(View view) {
			selectedText = null;
			if (view.getTag() != null) {
				selectedText = (EditText) view;
				selectedColumn = (BColumn) view.getTag();
				if (selectedColumn.getValue() != null) {
					showColumnEdition(selectedColumn.getName(), selectedColumn.getValue());
				}
			}
		}
	};

	public OnClickListener mClickDone = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			showYesNoDialog(getResourceString(R.string.ROW_SVCHAN), mClickSaveEdition);
		}
	};

	public DialogInterface.OnClickListener mClickSaveEdition = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface arg0, int arg1) {
			if (arg1 == -1) {
				updateRowData();
				doneEdition();
			}else{
				cancelEdition();
			}
		}
	};

	public OnClickListener mClickCancel = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			onBackPressed();
		}
	};

	public OnClickListener mClickOk = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			onBackPressed();
		}
	};
}
