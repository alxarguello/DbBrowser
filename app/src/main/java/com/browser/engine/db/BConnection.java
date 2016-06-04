package com.browser.engine.db;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.browser.app.dao.BrowserDao;
import com.browser.app.Query;
import com.browser.app.R;
import com.browser.app.exceptions.BrowserException;
import com.browser.app.util.Util;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;

import android.util.Log;

@SuppressLint("DefaultLocale")
public class BConnection {

	private final Context mContext;
	private static String mDBName;
	private static String mPath;
	private SQLiteDatabase androidConnection;
	private static BConnection instance;

	private BCursor result = null;
	private BrowserException errors;

	public static final int TYPE_TABLE = 3;
	public static final int TYPE_VIEW = 4;
	public static final int TYPE_TRIGGER = 5;
	public static final int TYPE_INDEX = 6;

	private static HashMap<String, Integer> types = new HashMap<String, Integer>();

	private static final Logger logger = Logger.getLogger(BConnection.class.getName());

	static {
		types.put("TABLE", TYPE_TABLE);
		types.put("VIEW", TYPE_VIEW);
		types.put("TRIGGER", TYPE_TRIGGER);
		types.put("INDEX", TYPE_INDEX);
	}

	public static BConnection getInstance(Context c) {
		if (instance == null) {
			instance = new BConnection(c);
		}
		return instance;
	}

	private BConnection(Context c) {
		this.mContext = c;
	}

	private BConnection(Context c, String dbName, String path) {
		this.mContext = c;
		BConnection.mDBName = dbName;
		BConnection.mPath = path;
	}

	public static void connectTo(String dbName, String path) {
		BConnection.mDBName = dbName;
		BConnection.mPath = path;
	}

	public static String connectToActive(BrowserDao dao) {
		if (mPath == null || mPath.equals("") == false) {
			String[] data = dao.retrieveActiveConnection();
			if (data != null) {
				mDBName = data[0];
				mPath = data[1];
			}
		}
		return mDBName;
	}

	/**
	 * Open the notes database. If it cannot be opened, try to create a new
	 * instance of the database. If it cannot be created, throw an exception to
	 * signal the failure
	 * 
	 * @return this (self reference, allowing this to be chained in an
	 *         initialization call)
	 * @throws SQLException
	 *             if the database could be neither opened or created
	 */
	public void open() throws SQLException, BrowserException {
		try {
			if (mPath != null && mPath.equalsIgnoreCase("") == false) {
				if (checkDbFile()) {

					androidConnection = SQLiteDatabase.openDatabase(mPath, null, SQLiteDatabase.OPEN_READWRITE
							| SQLiteDatabase.NO_LOCALIZED_COLLATORS);
					if (androidConnection.isOpen() == false) {
						throw new BrowserException(getResourceString(R.string.BCN_NOTPSS));
					}
				}
			} else {
				throw new BrowserException(getResourceString(R.string.NOACTIVECONN));
			}

		} catch (SQLException e) {
			throw new SQLException("Error on SSDb - open: \n" + e);
		}

	}

	public boolean checkDbFile() throws BrowserException {
		boolean v = false;
		if (mPath != null && mPath.equalsIgnoreCase("") == false) {
			if (new File(mPath).isFile()) {
				v = true;
			} else {
				throw new BrowserException(getResourceString(R.string.BCN_NOTEXS));
			}
		}
		return v;
	}

	public boolean isOpen() {
		boolean op = false;
		if (androidConnection != null && androidConnection.isOpen()) {
			op = true;
		}
		return op;
	}

	public void close() {

		if (androidConnection != null) {
			try {
				androidConnection.close();
			} catch (Exception e) {
			}
			try {
				androidConnection.releaseReference();
			} catch (Exception e) {
			}
			SQLiteDatabase.releaseMemory();
		}
	}

	public BCursor execQuery(String sql) {
		result = null;
		errors = null;
		try {
			open();

			if (sql != null && sql.equals("") == false) {
				if (sql.toLowerCase().startsWith("select")) {
					result = new BCursor(androidConnection.rawQuery(sql, null));
				} else {
					try {
						androidConnection.beginTransaction();
						androidConnection.execSQL(sql);
						androidConnection.setTransactionSuccessful();
					} catch (SQLException e) {
						logger.log(Level.INFO, "ERROR on query execution:\n"
								+ e);
					}
				}
			}
		} catch (SQLException e) {
			errors = new BrowserException(e.getMessage());
		} catch (BrowserException e) {
			errors = new BrowserException(e.getMessage());
		} catch (Exception e) {
			logger.log(Level.INFO, "ERROR on query execution:\n" + e);
		} finally {
			if (androidConnection != null && androidConnection.inTransaction()) {
				androidConnection.endTransaction();
			}

			close();
		}

		return result;
	}

	public BCursor getQueryResult() {
		return result;
	}

	public String getStringQueryResult() {
		String r = null;
		try {
			r = BExport.exportToString(result);
		} catch (Exception ex) {
			logger.log(Level.SEVERE, "ERROR BConnection - getStringQueryResult:\n"
					+ ex.getMessage());
		}
		if (r == null) {
			r = "";
		}
		return r;
	}

	public BrowserException getErrors() {
		return errors;
	}

	public long saveRow(Bundle b, int code, String table) throws BrowserException {
		long result = 0;
		try {
			open();
			// androidConnection.beginTransaction();
			ContentValues cnv = new ContentValues();
			Iterator<String> it = b.keySet().iterator();

			if (code == Query.ROW_EDITION) {
				StringBuilder where = new StringBuilder();
				String[] optArgs = new String[b.size()];

				int ix = 0;
				while (it.hasNext()) {
					Object c = b.get(it.next());
					if (c instanceof BColumn) {
						BColumn bc = (BColumn) c;

						cnv.put(bc.getName(), bc.getValue());
						if (bc.getPrevValue() != null) {
							optArgs[ix++] = bc.getPrevValue();
							where.append((ix > 1 ? "AND " : "") + bc.getName()
									+ "=? ");
						}
					}
				}

				int k = 0;
				for (String v : optArgs) {
					if (v != null) {
						k++;
					}
				}
				String[] whereArgs = new String[k];
				k = 0;
				for (String v : optArgs) {
					if (v != null) {
						whereArgs[k++] = v;
					}
				}

				result = androidConnection.update(table, cnv, where.toString(), whereArgs);
			} else if (code == Query.ROW_NEW) {
				while (it.hasNext()) {
					Object c = b.get(it.next());
					if (c instanceof BColumn) {
						BColumn bc = (BColumn) c;
						cnv.put(bc.getName(), bc.getValue());
					}
				}
				if (cnv.size() > 0) {
					// androidConnection.execSQL("INSERT INTO suggestions (display1,display2,date,query) values ('firework katy perry','Resultados de \"firework katy perry\"','1298312604392','firework katy perry')");
					result = androidConnection.insertOrThrow(table, null, cnv);
				}

			}

			// androidConnection.setTransactionSuccessful();

		} catch (BrowserException e) {
			throw new BrowserException(e.getMessage());
		} catch (SQLException e) {
			e.printStackTrace();
			logger.log(Level.INFO, "ERROR on query execution:\n" + e);
		} catch (Exception e) {
			logger.log(Level.INFO, "ERROR on query execution:\n" + e);
		} finally {
			if (androidConnection.inTransaction()) {
				androidConnection.endTransaction();
			}

			close();
		}
		return result;
	}

	public int deleteRow(String table, ArrayList<BColumn> cols) throws BrowserException {
		int res = 0;
		try {
			open();

			StringBuilder where = new StringBuilder();
			String[] tmpArgs = new String[cols.size()];
			int ix = 0;
			for (BColumn column : cols) {
				if (column.getValue() != null) {
					tmpArgs[ix++] = column.getValue();
					where.append((ix > 1 ? "AND " : "") + column.getName()
							+ "=? ");
				}

			}

			int k = 0;
			for (String v : tmpArgs) {
				if (v != null) {
					k++;
				}
			}
			String[] whereArgs = new String[k];
			k = 0;
			for (String v : tmpArgs) {
				if (v != null) {
					whereArgs[k++] = v;
				}
			}

			androidConnection.beginTransaction();
			res = androidConnection.delete(table, where.toString(), whereArgs);
			androidConnection.setTransactionSuccessful();
		} catch (BrowserException e) {
			throw new BrowserException(e.getMessage());
		} finally {
			androidConnection.endTransaction();
			close();
		}
		return res;
	}

	public List<TreeMap<String, Integer>> getDBObjects() {
		TreeMap<String, Integer> data = new TreeMap<String, Integer>();

		try {
			open();
			Cursor c = androidConnection.rawQuery(getResourceString(R.string.select_master), null);
			if (c != null && c.getCount() > 0) {
				while (c.moveToNext()) {
					int type = -1;
					type = getType(c.getString(0));
					data.put(c.getString(1), type);
				}
			}
		} catch (Exception e) {
			logger.log(Level.INFO, "ERROR on query execution:\n" + e);
		} finally {
			close();
		}

		ArrayList<TreeMap<String, Integer>> l = new ArrayList<TreeMap<String, Integer>>();
		l.add(data);
		return l;

	}

	public HashMap<String, String> getObjectProperties(String object, boolean isTableOrView) {
		HashMap<String, String> map = new HashMap<String, String>();
		try {
			open();
			Cursor c = null;
			c = androidConnection.rawQuery(getResourceString(R.string.select_master_object), new String[] { object });
			if (c != null && c.getCount() > 0) {
				if (c.moveToNext()) {
					map.put("object.browser.name", c.getString(1));
					map.put("object.browser.sql", c.getString(4));

				}
			}

			if (isTableOrView) {
				c = androidConnection.rawQuery("SELECT * FROM " + object
						+ " LIMIT 1", null);
				if (c != null) {
					String[] cl = c.getColumnNames();
					for (String cn : cl) {
						map.put("object.browser.cname." + cn, cn);
					}
				}
				c = androidConnection.rawQuery("SELECT count(*) FROM " + object, null);
				if (c != null && c.getCount() > 0) {
					if (c.moveToNext()) {
						map.put("object.browser.count", c.getString(0));
					}
				}
			}
		} catch (Exception e) {
			logger.log(Level.INFO, "ERROR BConnection getObjectProperties:\n"
					+ e);
		} finally {
			close();
		}

		return map;

	}

	private int getType(String data) {
		int t = -1;
		Iterator<String> i = types.keySet().iterator();
		while (i.hasNext()) {
			if (i.next().equalsIgnoreCase(data)) {
				t = types.get(data.toUpperCase());
				break;
			}
		}
		return t;
	}

	public String getActiveConnectionName() {
		return mDBName;
	}

	private String getResourceString(int stringId) {
		return BHelper.getResourceString(stringId, mContext);
	}

	private static class BHelper extends SQLiteOpenHelper {

		public BHelper(Context context) {
			super(context, mPath, null, 3);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// Do nothing, all the db/schema should come from the server
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(BHelper.class.getName(), "Upgrading database from version "
					+ oldVersion + " to " + newVersion
					+ ", which will destroy all old data");
			onCreate(db);
		}

		private static String getResourceString(int stringId, Context cc) {
			return Util.getResourceString(stringId, cc);
		}
	}

}
