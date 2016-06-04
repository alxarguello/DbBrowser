package com.browser.app;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.browser.app.dao.BrowserDao;
import com.browser.app.exceptions.BrowserException;
import com.browser.app.util.Util;
import com.browser.engine.db.BColumn;
import com.browser.engine.db.BConnection;
import com.browser.engine.db.BCursor;
import com.browser.engine.db.BExport;
import com.browser.engine.db.BMetadata;
import com.browser.engine.db.BTQuery;
import com.browser.engine.ui.CActivity;
import com.browser.engine.ui.CBTextView;
import com.browser.engine.ui.ParallelScrollView;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.SeekBar;

public class Query extends CActivity {

	private BConnection mBConnection;
	private BrowserDao mDao;
	private TableRow mRowHeader;
	private TableRow mSelectedRow;
	private CBTextView mSelectedColumn;
	private String tableEdition = null;
	private int lastTextSize = 0;
	private TableLayout mHeader;
	private TableLayout mGridTable;
	private String status;
	private String filter;
	private int selectedColumnIndex = -1;
	private int selectedColumnFilter = -1;

	private Dialog dialog;
	private ProgressDialog mPd = null;
	private BTQuery query;

	public static final Object sync = new Object();

	private static ArrayList<BMetadata> meta = new ArrayList<BMetadata>();
	private static final Logger logger = Logger.getLogger(Query.class.getName());
	private static final SimpleDateFormat sdf = new SimpleDateFormat("-ddMMyyyy.HH.mm");

	private static final int CONTEXT_MENU_MODIFY_ROW = 1;
	private static final int CONTEXT_MENU_DELETE_ROW = 2;
	private static final int MENU_SET_FILTER = 3;
	private static final int MENU_EDIT_FILTER = 4;
	private static final int MENU_DELETE_FILTER = 5;
	private static final int MENU_DELETE_ROW = 6;
	private static final int MENU_NEW_ROW = 7;
	private static final int MENU_EDIT_ROW = 8;
	private static final int MENU_CLONE_ROW = 9;
	private static final int MENU_VIEW_ROW = 10;
	private static final int MENU_TEXT_SIZE = 11;
	private static final int MENU_EXPORT = 12;

	public static final int RESULT_OK = 13;
	public static final int ROW_EDITION = 14;
	public static final int ROW_NEW = 15;
	public static final int SEARCH_DATA_FROM = 16;

	private static final int FIXED_C_SIZE = 280;
	private static final int FIXED_RATE = 16;

	private static final int FIXED_TOP_ROWS = 5000;

	/** Called when the activity is first created. */

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sc_query);

		((ParallelScrollView) findViewById(R.id.headerScroll)).linkViews(findViewById(R.id.horizontalTableScroll));
		findViewById(R.id.btnExecute).setOnClickListener(mClickExecute);
		findViewById(R.id.btnClearGrid).setOnClickListener(mClickClearGrid);
		mHeader = (TableLayout) findViewById(R.id.header);
		mGridTable = (TableLayout) findViewById(R.id.gridTable);
		View ssql = findViewById(R.id.btnSSql);
		if (ssql != null) {
			ssql.setOnClickListener(mClickShowSQL);
		}

		Object config = getLastNonConfigurationInstance();
		if (config != null && config instanceof HashMap<?, ?>) {
			@SuppressWarnings("unchecked")
			HashMap<String, Object> m = (HashMap<String, Object>) config;
			setConfigData(m);
		} else {
			mBConnection = BConnection.getInstance(this);
			mDao = new BrowserDao(this);
			status = BConnection.connectToActive(mDao);
			// init from DB
			setQueryForTable();
		}

		if (status == null || status.equals("")) {
			toastMessage(getResourceString(R.string.NOACTIVECONN));
		}
		setStatusText(status);
		registerForContextMenu(mGridTable);
		lastTextSize = setTextSize(mDao.getSettingsTextSize(), false);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		int ix = Menu.FIRST;

		menu.add(Menu.NONE, MENU_TEXT_SIZE, Menu.NONE, getResourceString(R.string.QRY_TXSIZE));

		if (mSelectedRow != null && mSelectedColumn != null) {
			if (filter != null) {
				menu.add(Menu.NONE, MENU_EDIT_FILTER, ix, getResourceString(R.string.QRY_EDFILTER)).setIcon(R.drawable.ic_menu_filter);
				menu.add(Menu.NONE, MENU_DELETE_FILTER, ix++, getResourceString(R.string.QRY_DLFILTER)).setIcon(R.drawable.ic_menu_filter);
			} else {
				menu.add(Menu.NONE, MENU_SET_FILTER, ix++, getResourceString(R.string.QRY_STFILTER)).setIcon(R.drawable.ic_menu_filter);
			}
			if (tableEdition != null && tableEdition.equals("") == false) {
				menu.add(Menu.NONE, MENU_DELETE_ROW, ix++, getResourceString(R.string.QRY_DLREGIST)).setIcon(R.drawable.ic_menu_delete);
				menu.add(Menu.NONE, MENU_EDIT_ROW, ix++, getResourceString(R.string.QRY_EDREGIST)).setIcon(R.drawable.ic_menu_edit);
				menu.add(Menu.NONE, MENU_CLONE_ROW, ix++, getResourceString(R.string.QRY_CLREGIST)).setIcon(R.drawable.ic_menu_duplicate);
			} else {
				menu.add(Menu.NONE, MENU_VIEW_ROW, ix++, getResourceString(R.string.QRY_VWREGIST)).setIcon(R.drawable.ic_menu_view);
			}
		}
		menu.add(Menu.NONE, MENU_NEW_ROW, ix++, getResourceString(R.string.QRY_NWREGIST)).setIcon(R.drawable.ic_menu_new);
		// menu.add(Menu.NONE, MENU_TEXT_SIZE, Menu.NONE,
		// getResourceString(R.string.QRY_EXPORT));
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case MENU_SET_FILTER:
			showFilterDialog(false);
			return true;
		case MENU_EDIT_FILTER:
			showFilterDialog(true);
			return true;
		case MENU_DELETE_FILTER:
			deleteFilter();
			return true;
		case MENU_DELETE_ROW:
			deleteRow();
			return true;
		case MENU_EDIT_ROW:
			rowEdition(false);
			return true;
		case MENU_NEW_ROW:
			rowEdition(true);
			return true;
		case MENU_CLONE_ROW:
			cloneRow();
			return true;
		case MENU_VIEW_ROW:
			rowEdition(false);
			return true;
		case MENU_TEXT_SIZE:
			showSettingsDialog();
			return true;
		case MENU_EXPORT:
			doExport();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		menu.add(Menu.NONE, CONTEXT_MENU_MODIFY_ROW, Menu.NONE, getResourceString(R.string.QRY_EDREGIST));
		menu.add(Menu.NONE, CONTEXT_MENU_DELETE_ROW, Menu.NONE, getResourceString(R.string.QRY_DLREGIST));

	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {

		/*
		 * what item was selected is ListView
		 */

		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		try {
			if (mSelectedRow == null) {
				if (info != null && info.targetView != null) {

					mSelectedRow = (TableRow) info.targetView.getParent();

				}
			}
		} catch (NullPointerException e) {
		}
		if (mSelectedRow != null) {
			switch (item.getItemId()) {
			case CONTEXT_MENU_MODIFY_ROW:
				rowEdition(false);
				break;
			case CONTEXT_MENU_DELETE_ROW:
				deleteRow();
				break;
			}
		}
		return true;
	}

	protected void onActivityResult(int code, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (saveRow(data, code)) {
				doQuery();
			}
		}
	}

	private void executeQuery(Handler handler) {
		String sql = ((EditText) findViewById(R.id.txtQuery)).getText().toString();
		try {
			// createGrid(mBConnection.execQuery(sql));
			findViewById(R.id.btnExecute).setEnabled(false);
			// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
			mPd = ProgressDialog.show(Query.this, null, getResourceString(R.string.QRY_EXQUERY), true, true, mCancelQuery);
			query = new BTQuery(mBConnection, sql, handler, sync, !isTableEdition(), this.getApplicationContext());
			query.start();
		} catch (Exception e) {
			setErrorMessage(e.getMessage());
		} finally {
			// mBConnection.closeDb();
		}

	}

	private void doQuery() {
		clearErrorMessage();
		clearGrid();
		executeQuery(mHandlerQuery);
	}

	private void doExport() {

		clearErrorMessage();
		clearGrid();
		executeQuery(mHandlerExport);
	}

	private void clearGrid() {
		mHeader.removeAllViews();
		mGridTable.removeAllViews();
		mSelectedRow = null;
		mSelectedColumn = null;
	}

	private void createGrid(BCursor cursor) {
		try {
			int[] hSize = null;
			mRowHeader = null;

			clearGrid();

			TableLayout.LayoutParams tl = new TableLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
			if (cursor != null && cursor.getColumnCount() > 0) {
				mRowHeader = new TableRow(this);
				hSize = new int[cursor.getColumnCount()];
				meta.clear();
				for (int c = 0; c < cursor.getColumnCount(); c++) {

					CBTextView cHeader = new CBTextView(this, true);

					cHeader.setTextColor(ColorStateList.valueOf(Color.BLACK));
					cHeader.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
					cHeader.setBackgroundColor(Color.WHITE);

					int cs = 0;
					String data = cursor.getColumnName(c);
					if (data.length() * FIXED_RATE >= FIXED_C_SIZE) {
						cs = FIXED_C_SIZE;
						cHeader.setExtendedData(data);
						int sb = data.length() > FIXED_C_SIZE - 3 ? FIXED_C_SIZE - 3
								: data.length();
						cHeader.setText(data.substring(0, sb) + "...");
					} else {
						cs = (data.length() * FIXED_RATE);
						cHeader.setText(data);
					}

					cHeader.getColumnMetadata().setColumnName(cursor.getColumnName(c));
					meta.add(cHeader.getColumnMetadata());

					TableRow.LayoutParams lp = new TableRow.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
					lp.setMargins(1, 1, 1, 1);
					lp.weight = 1;

					hSize[c] = cs;
					lp.width = hSize[c];

					cHeader.setLayoutParams(lp);
					mRowHeader.addView(cHeader);
				}

				mHeader.addView(mRowHeader, tl);
			}
			boolean resizeHeader = false;
			int cc = 0;
			if (cursor != null && cursor.getCount() > 0) {
				boolean colored = false;
				while (cursor.moveToNext()) {
					cc++;
					if (cc <= FIXED_TOP_ROWS) {

						TableRow row = new TableRow(this);
						for (int c = 0; c < cursor.getColumnCount(); c++) {

							CBTextView column = new CBTextView(this);

							column.setTextColor(ColorStateList.valueOf(Color.BLACK));
							column.setGravity(android.view.Gravity.CENTER_HORIZONTAL);

							String data = cursor.getString(c);
							int cs = 0;
							boolean ext = false;
							column.setExtendedData(data);
							if (data != null) {
								if ((data.length() * FIXED_RATE) >= FIXED_C_SIZE) {
									cs = FIXED_C_SIZE;
									ext = true;
									if (data.length() >= FIXED_C_SIZE - 3) {
										column.setText(data.substring(0, FIXED_C_SIZE - 3) + "...");
									} else {
										column.setText(data.substring(0, data.length() - 3) + "...");
									}
								} else {
									cs = (data.length() * FIXED_RATE);
									column.setText(data);
								}
							} else {
								cs = FIXED_RATE;
								column.setText("");
							}

							TableRow.LayoutParams lp = new TableRow.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
							if (cursor.isLast()) {
								lp.setMargins(1, 1, 1, 7);
							} else {
								lp.setMargins(1, 1, 1, 1);
							}
							lp.weight = 1;

							if (cs > hSize[c]) {
								resizeHeader = true;
								hSize[c] = cs;
							}
							lp.width = hSize[c];

							column.setLayoutParams(lp);
							if (colored) {
								column.setBackgroundResource(R.color.darkScale);
								column.setTag(new BColumn(cursor.getColumnName(c), c, R.color.darkScale, column.getText().toString(), column.getExtendedData(), ext));
							} else {
								column.setBackgroundResource(R.color.grayScale);
								column.setTag(new BColumn(cursor.getColumnName(c), c, R.color.grayScale, column.getText().toString(), column.getExtendedData(), ext));
							}
							column.setTextColor(Color.WHITE);
							column.setOnClickListener(mClickRow);
							column.setClickable(true);
							column.setFocusable(true);
							row.addView(column);
						}

						TableLayout.LayoutParams layoutResult = new TableLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
						if (colored) {
							colored = false;
						} else {
							colored = true;
						}

						mGridTable.addView(row, layoutResult);

					}
				}

			}

			if (resizeHeader) {
				for (int r = 0; r < hSize.length; r++) {
					mRowHeader.getChildAt(r).getLayoutParams().width = hSize[r];
				}
			}

			if (cc-1 > FIXED_TOP_ROWS) {
				showMessage(getResourceString(R.string.QRY_EXCEED));
			}
		} catch (Exception e) {
			logger.log(Level.INFO, "ERROR Query - createGrid:\n" + e.getMessage());
		}
	}

	private void deleteRow() {
		showYesNoDialog(getResourceString(R.string.QRY_RDLREGIST), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:

					ArrayList<BColumn> cols = new ArrayList<BColumn>();
					for (int i = 0; i < mSelectedRow.getChildCount(); i++) {
						Object c = mSelectedRow.getChildAt(i).getTag();
						if (c instanceof BColumn) {
							cols.add((BColumn) c);
						}
					}
					try {
						int af = mBConnection.deleteRow(tableEdition, cols);
						toastMessage(getResourceString(R.string.QRY_RWAFFEC) + af);
						doQuery();
					} catch (BrowserException e) {
						setErrorMessage(e.getMessage());
					}
					break;
				}
			}
		});
	}

	private void cloneRow() {
		showYesNoDialog(getResourceString(R.string.QRY_DPLREGIST), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:

					Bundle cols = new Bundle();
					for (int i = 0; i < mSelectedRow.getChildCount(); i++) {
						Object c = mSelectedRow.getChildAt(i).getTag();
						if (c instanceof BColumn) {
							BColumn col = (BColumn) c;
							if (col.getName().equalsIgnoreCase("_id") == false) {
								cols.putSerializable(col.getName(), col);
							}
						}
					}
					try {
						long af = mBConnection.saveRow(cols, ROW_NEW, tableEdition);
						toastMessage(getResourceString(R.string.QRY_NEWROW) + af);
						doQuery();
					} catch (BrowserException e) {
						setErrorMessage(e.getMessage());
					}
					break;
				}
			}
		});
	}

	private int setTextSize(int size, boolean isDialog) {
		if (isDialog) {
			((TextView) dialog.findViewById(R.id.r_sett_text)).setText(size + 10 + "px");
		}
		((TextView) findViewById(R.id.txtQuery)).setTextSize(TypedValue.COMPLEX_UNIT_SP, size + 9.6F);
		return size;
	}

	private void rowEdition(boolean newRow) {

		int editType = -1;
		Bundle b = new Bundle();
		if (newRow) {
			editType = ROW_NEW;
			b.putBoolean("table.new.row", true);
		} else {
			editType = ROW_EDITION;
			b.putBoolean("table.new.row", false);
		}

		if (tableEdition != null) {
			b.putString("table.edition.from", tableEdition);
		}
		if (newRow == false) {
			for (int i = 0; i < mSelectedRow.getChildCount(); i++) {
				Object c = mSelectedRow.getChildAt(i).getTag();
				if (c instanceof BColumn) {
					BColumn cc = (BColumn) c;
					b.putSerializable(cc.getName(), cc);
				}
			}
		}
		gotoScreenForResult(Row.class, b, editType);
	}

	private boolean saveRow(Intent data, int code) {
		clearErrorMessage();
		clearGrid();
		boolean result = true;
		try {
			Bundle b = (Bundle) data.getExtras().get("edition");
			long af = mBConnection.saveRow(b, code, tableEdition);
			switch (code) {
			case ROW_EDITION:
				toastMessage(getResourceString(R.string.QRY_RWAFFEC) + af);
				break;
			case ROW_NEW:
				if (af > 0) {
					toastMessage(getResourceString(R.string.QRY_NEWROW) + af);
				} else {
					toastMessage(getResourceString(R.string.QRY_NOTPOSS));
				}
				break;
			default:
				break;
			}
		} catch (BrowserException e) {
			setErrorMessage(e.getMessage());
			result = false;
		}
		return result;
	}

	private void exportResults(BCursor cursor) {
		try {

			File path = BExport.saveExportFile("EXP." + sdf.format(Calendar.getInstance().getTime()), cursor);
			if (path.exists()) {
				showMessage(getResourceString(R.string.QRY_EXQUERY) + " : " + path.getPath());
			}
		} catch (Exception e) {
			logger.log(Level.INFO, "ERROR Query - exportResults:\n" + e);
		}
	}

	private void showSettingsDialog() {
		dialog = new Dialog(this);
		dialog.setContentView(R.layout.r_sett_textsize);
		dialog.setTitle(getResourceString(R.string.QRY_TXSIZE));

		// dialog.setOnCancelListener(mCancelQuery);
		dialog.findViewById(R.id.r_sett_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {

				dialog.dismiss();
			}
		});
		SeekBar bar = ((SeekBar) dialog.findViewById(R.id.r_sett_seek));
		bar.setProgress(lastTextSize);
		setTextSize(lastTextSize, true);
		bar.setOnSeekBarChangeListener(mSeekBarChange);
		dialog.show();
	}

	private void setQueryForTable() {

		Bundle b = this.getIntent().getExtras();
		String tb = null;
		String qr = null;
		if (b != null) {
			tb = b.getString("table");
			qr = b.getString("customQuery");
		}
		if (tb != null || (tableEdition != null && tableEdition.equals("") == false)) {
			tableEdition = tb;
			if (tableEdition != null && tableEdition.equals("") == false) {
				findViewById(R.id.btnClearSql).setVisibility(View.GONE);
				TextView v = (TextView) findViewById(R.id.txtQuery);
				v.setEnabled(false);
				v.setFocusable(false);
				v.setText("select * from " + tableEdition);

				doQuery();
			}
		} else {
			if (qr != null) {
				((TextView) findViewById(R.id.txtQuery)).setText(qr);
			}
			findViewById(R.id.btnClearSql).setOnClickListener(mClickClearSql);
		}
	}

	private boolean isTableEdition() {
		boolean ed = false;
		if (tableEdition != null && tableEdition.equals("") == false) {
			ed = true;
		}
		return ed;
	}

	@Override
	public Object onRetainNonConfigurationInstance() {

		HashMap<String, Object> data = new HashMap<String, Object>();

		data.put("mBConnection", mBConnection);
		data.put("mDao", mDao);
		data.put("mRowHeader", mRowHeader);
		data.put("mSelectedRow", mSelectedRow);
		data.put("mSelectedColumn", mSelectedColumn);
		data.put("tableEdition", tableEdition);
		data.put("lastTextSize", lastTextSize);
		data.put("mHeader", mHeader);
		data.put("mGridTable", mGridTable);
		data.put("status", status);

		return data;
	}

	public void setConfigData(HashMap<String, Object> data) {
		try {
			mBConnection = (BConnection) data.get("mBConnection");
			mDao = (BrowserDao) data.get("mDao");
			mRowHeader = (TableRow) data.get("mRowHeader");
			mSelectedRow = (TableRow) data.get("mSelectedRow");
			mSelectedColumn = (CBTextView) data.get("mSelectedColumn");
			tableEdition = (String) data.get("tableEdition");
			lastTextSize = (Integer) data.get("lastTextSize");

			TableLayout header = (TableLayout) data.get("mHeader");
			for (int i = 0; i < header.getChildCount(); i++) {
				View ch = header.getChildAt(i);
				header.removeViewAt(i);
				mHeader.addView(ch);
			}
			TableLayout grid = (TableLayout) data.get("mGridTable");
			int i = 0;
			while (grid.getChildCount() > 0) {
				View ch = grid.getChildAt(0);
				grid.removeViewAt(0);
				mGridTable.addView(ch, i);
				i++;
			}
			status = (String) data.get("status");
			setQueryForTable();
		} catch (Exception e) {
			logger.log(Level.INFO, "ERROR Query - setConfigData:\n" + e);
		}
	}

	public static ArrayList<BMetadata> getMetaData() {
		return meta;
	}

	private void showFilterDialog(boolean editFilter) {
		dialog = new Dialog(this);
		dialog.setContentView(R.layout.r_filter);
		dialog.setTitle(getResourceString(R.string.QRY_FLINCLM));
		View tm = null;
		if (editFilter) {
			tm = mRowHeader.getChildAt(selectedColumnFilter);
		} else {
			tm = mRowHeader.getChildAt(selectedColumnIndex);
		}

		CBTextView headerWithFilter = (CBTextView) tm;

		((TextView) dialog.findViewById(R.id.r_filter_cname)).setText(headerWithFilter.getText());
		if (editFilter) {
			((TextView) dialog.findViewById(R.id.r_filter_data)).setText(filter);
		}
		dialog.findViewById(R.id.r_filter_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				TextView t = (TextView) dialog.findViewById(R.id.r_filter_data);
				filter = t.getText().toString();
				filter(false);
				dialog.dismiss();
			}
		});

		dialog.show();
	}

	private void deleteFilter() {
		showYesNoDialog(getResourceString(R.string.QRY_DLTFIL), mClickClearFilter);
	}

	/**
	 * 
	 * 
	 * @param clearFilter
	 *            (false, si se quiere establecer filtro, true, si se quiere
	 *            eliminar el filtro)
	 */
	private void filter(boolean clearFilter) {

		if (mGridTable != null) {

			if (clearFilter == false && filter != null && filter.equals("") == false) {
				selectedColumnFilter = selectedColumnIndex;
			} else if (filter == null || filter.equals("")) {
				clearFilter = true;
			}

			View tm = mRowHeader.getChildAt(selectedColumnFilter);
			CBTextView headerWithFilter = (CBTextView) tm;
			if (headerWithFilter != null) {
				if (clearFilter) {
					filter = null;
					headerWithFilter.getColumnMetadata().setFilter(null);
					headerWithFilter.setBackgroundResource(R.color.white);
				} else {
					headerWithFilter.getColumnMetadata().setFilter(filter);
					headerWithFilter.setBackgroundResource(R.color.blueColumn);
				}
			}

			/**
			 * Busqueda dentro de la columna definida dentro de cada fila, las
			 * filas que no contengan una columna que cumpla con el filtro se
			 * establecen como no visibles
			 */
			if ((filter != null && filter.equals("") == false && clearFilter == false) || clearFilter && selectedColumnFilter >= 0) {
				for (int r = 0; r < mGridTable.getChildCount(); r++) {
					View rr = mGridTable.getChildAt(r);
					if (rr instanceof TableRow) {

						TableRow row = (TableRow) rr;
						row.setVisibility(View.VISIBLE);
						if (clearFilter == false) {
							View cc = row.getChildAt(selectedColumnFilter);
							if (cc instanceof TextView) {
								TextView col = (TextView) cc;
								BColumn bcol = (BColumn) col.getTag();
								if (bcol.getValue().contains(filter) == false) {
									row.setVisibility(View.GONE);
								}
							}
						}
					}
				}
			}
			if (clearFilter) {
				selectedColumnFilter = -1;
			}
		}
	}

	@Override
	public void finish() {
		mBConnection.close();
		super.finish();
	}

	private DialogInterface.OnClickListener mClickClearFilter = new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface arg0, int arg1) {
			filter(true);
		}

	};

	private OnClickListener mClickExecute = new OnClickListener() {
		@Override
		public void onClick(View v) {
			doQuery();
		}
	};

	private OnClickListener mClickClearSql = new OnClickListener() {
		@Override
		public void onClick(View v) {
			((EditText) findViewById(R.id.txtQuery)).setText("");
		}
	};

	private OnClickListener mClickClearGrid = new OnClickListener() {
		@Override
		public void onClick(View v) {
			clearGrid();
		}
	};

	private OnClickListener mClickRow = new OnClickListener() {
		@Override
		public void onClick(View v) {

			TableRow tr = (TableRow) v.getParent();
			if (mSelectedRow != null) {
				for (int c = 0; c < mSelectedRow.getChildCount(); c++) {
					View tc = mSelectedRow.getChildAt(c);
					int color = ((BColumn) tc.getTag()).getColor();
					tc.setBackgroundResource(Integer.parseInt(String.valueOf(color)));
				}
			}
			mSelectedColumn = (CBTextView) v;
			mSelectedRow = tr;
			for (int c = 0; c < tr.getChildCount(); c++) {
				View ch = tr.getChildAt(c);
				if (ch.equals(mSelectedColumn) && filter == null) {
					selectedColumnIndex = c;
				}
				if (filter != null && c == selectedColumnFilter) {
					ch.setBackgroundResource(R.color.blueColumn);
				} else {
					ch.setBackgroundResource(R.color.blueRow);
				}
			}
			if (filter == null) {
				mSelectedColumn.setBackgroundResource(R.color.blueColumn);
			}

		}
	};

	private OnSeekBarChangeListener mSeekBarChange = new OnSeekBarChangeListener() {

		@Override
		public void onProgressChanged(SeekBar skb, int progress, boolean fromUser) {
			setTextSize(progress, true);
			lastTextSize = progress;
		}

		@Override
		public void onStartTrackingTouch(SeekBar sb) {
			// do nothing
		}

		@Override
		public void onStopTrackingTouch(SeekBar arg0) {
			mDao.setTextSetting(Util.integerToString(lastTextSize));
		}

	};

	private Handler mHandlerQuery = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			synchronized (Query.sync) {

				findViewById(R.id.btnExecute).setEnabled(true);

				// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
				if (mBConnection.getErrors() == null) {
					createGrid(mBConnection.getQueryResult());

				} else {
					setErrorMessage(mBConnection.getErrors().getMessage());

				}

				try {
					if (mPd.isShowing()) {
						mPd.dismiss();
					}
				} catch (IllegalArgumentException e) {
					logger.log(Level.INFO, "ERROR Query - mHandlerQuery:handleMessage:\n" + e);
				}

			}
		}
	};

	private Handler mHandlerExport = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			synchronized (Query.sync) {

				findViewById(R.id.btnExecute).setEnabled(true);

				// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
				if (mBConnection.getErrors() == null) {
					exportResults(mBConnection.getQueryResult());

				} else {
					setErrorMessage(mBConnection.getErrors().getMessage());
				}
				try {
					if (mPd.isShowing()) {
						mPd.dismiss();
					}
				} catch (IllegalArgumentException e) {
					logger.log(Level.INFO, "ERROR Query - mHandlerQuery:handleMessage:\n" + e);
				}

			}
		}
	};

	private OnCancelListener mCancelQuery = new OnCancelListener() {

		@Override
		public void onCancel(DialogInterface arg0) {
			mHandlerQuery.sendEmptyMessage(0);
			findViewById(R.id.btnExecute).setEnabled(false);
			query.stopThread();

		}
	};

	private OnClickListener mClickShowSQL = new OnClickListener() {

		@Override
		public void onClick(View v) {
			View q = findViewById(R.id.txtQuery);
			if (q.getVisibility() == View.GONE) {
				q.setVisibility(View.VISIBLE);
			} else {
				q.setVisibility(View.GONE);
			}

		}
	};

}
