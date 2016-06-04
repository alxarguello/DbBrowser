package com.browser.app.dao;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.browser.app.R;
import com.browser.app.exceptions.BrowserException;
import com.browser.app.util.BPermmits;
import com.browser.app.util.Util;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class BrowserDao {

	private static final String DB_NAME = "browserDb";
	private static final int DATABASE_VERSION =4;
	private static final Logger logger = Logger.getLogger(BrowserDao.class.getName());
	private static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

	private DbHelper mDbHelper;
	private final Context mContext;
	private SQLiteDatabase connection;

	public static final String C_CONNECTION_NAME = "connection_name";
	public static final String C_PATH = "path";
	public static final String C_ACTIVE = "(active)";

	public static final String QR_EXECUTEDON = "executedon";
	public static final String QR_LASTEXC = "lastExecution";
	public static final String QR_QUERY = "query";

	public static final String S_NAME = "SETTING_NAME";
	public static final String S_VALUE = "SETTING_VALUE";

	/**
	 * Database creation sql statement
	 */

	public BrowserDao(Context context) {
		this.mContext = context;
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
	public BrowserDao open() throws SQLException {
		try {
			mDbHelper = new DbHelper(mContext);
			connection = mDbHelper.getWritableDatabase();
		} catch (SQLException e) {
			throw new SQLException("Error on SSDb - open: \n" + e);
		}
		return this;
	}

	public void close() {
		mDbHelper.close();
	}

	public boolean isOpen() {
		boolean op = false;
		if (connection != null && connection.isOpen()) {
			op = true;
		}
		return op;
	}

	public void saveQuery(String sql) {

		try {
			String[] ac = retrieveActiveConnection();
			openDb();
			connection.beginTransaction();
			if (ac != null && ac[1] != null) {
				connection.execSQL(getResourceString(R.string.insert_latest), new Object[] {
						sql, ac[1],
						sdf.format(Calendar.getInstance().getTime()) });
				connection.setTransactionSuccessful();
			}
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "ERROR BrowserDao on saveQuery: \n" + e.toString());
		} finally {
			try {
				connection.endTransaction();
				closeDb();
			} catch (Exception ex) {
				logger.log(Level.SEVERE, "ERROR BrowserDao on saveQuery-finally: \n" + ex.toString());
			}
		}
	}

	public Cursor retrieveLastestQuerys() {

		Cursor c = null;
		try {
			StringBuilder sql = new StringBuilder(getResourceString(R.string.select_latest));
			String[] params = null;
			String[] active = retrieveActiveConnection();
			openDb();
			if (active != null && active[1] != null && active[1].equals("") == false) {
				sql.append(" WHERE EXECUTEDON=? LIMIT 100");
				params = new String[] { active[1] };
			} else {
				sql.append(" LIMIT 100");
			}
			c = connection.rawQuery(sql.toString(), params);
			// c = connection.rawQuery("SELECT * FROM LATESTS",null);
		} catch (SQLException e) {
			logger.log(Level.INFO, "ERROR BrowserDao - retrieveConnections:\n" + e);
		} 
		return c;
	}

	public Cursor retrieveConnections() {
		openDb();
		Cursor c = null;
		try {
			c = connection.rawQuery(getResourceString(R.string.select_connections), null);
		} catch (SQLException e) {
			logger.log(Level.INFO, "ERROR BrowserDao - retrieveConnections:\n" + e);
		}
		return c;
	}

	public String[] retrieveActiveConnection() {
		openDb();
		String[] data = null;
		try {
			Cursor c = connection.rawQuery(getResourceString(R.string.select_active_connection), null);
			if (c.moveToNext()) {
				data = new String[2];
				data[0] = c.getString(c.getColumnIndex("connection_name"));
				data[1] = c.getString(c.getColumnIndex("path"));
				BPermmits p = BPermmits.getInstance();
				p.rootAccess();
				p.makeReadable(data[1]);
			}
		} catch (SQLException e) {
			logger.log(Level.INFO, "ERROR BrowserDao - retrieveConnections:\n" + e);
		} finally {
			closeDb();
		}
		return data;
	}

	public void saveConnection(String cName, String path) throws SQLException, BrowserException {
		openDb();
		connection.beginTransaction();
		try {
			Cursor c = retrieveConnection(connection, cName, path);
			if (c == null || c.getCount() <= 0) {
				clearActiveConnections(connection);
				saveConnection(connection, cName, path, 1);
				connection.setTransactionSuccessful();
			} else {
				throw new BrowserException(getResourceString(R.string.BDO_EXISTS));
			}
		} catch (SQLException e) {
			throw new SQLiteException("ERROR BrowserDao - saveConnection:\n" + e);
		} finally {
			connection.endTransaction();
			closeDb();
		}
	}

	public void saveConnection(SQLiteDatabase connection, String cName, String path, int active) throws SQLException {
		try {
			connection.execSQL(getResourceString(R.string.insert_connections), new Object[] {
					cName, path, active });
		} catch (SQLException e) {
			throw new SQLiteException("ERROR BrowserDao - saveConnection:\n" + e);
		}
	}

	public void clearActiveConnections() throws SQLException {
		openDb();
		connection.beginTransaction();
		try {
			clearActiveConnections(connection);
			connection.setTransactionSuccessful();
		} catch (SQLiteException e) {
			throw new SQLiteException("ERROR BrowserDao - clearActiveConnections:\n" + e);
		} finally {
			connection.endTransaction();
			closeDb();
		}
	}

	public void clearActiveConnections(SQLiteDatabase connection) throws SQLException {
		try {
			connection.execSQL(getResourceString(R.string.update_clear_connections));
		} catch (SQLiteException e) {
			throw new SQLiteException("ERROR BrowserDao - clearActiveConnections:\n" + e);
		}
	}

	public void setActiveConnection(String path) {
		open();
		connection.beginTransaction();
		try {
			clearActiveConnections(connection);
			connection.execSQL(getResourceString(R.string.update_setactive_connections), new Object[] { path });
			connection.setTransactionSuccessful();
		} catch (SQLException e) {
			throw new SQLException("ERROR BrowserDao - setActiveConnection:\n" + e);
		} finally {
			connection.endTransaction();
			closeDb();
		}
	}

	public void setActiveConnection(SQLiteDatabase connection, String path) {

	}

	public void deleteConnection(String path) throws SQLException {
		openDb();
		connection.beginTransaction();
		try {
			deleteConnection(connection, path);
			connection.setTransactionSuccessful();
		} catch (SQLException e) {
			throw new SQLException("ERROR BrowserDao - deleteConnection:\n" + e);
		} finally {
			connection.endTransaction();
			closeDb();
		}
	}

	public void deleteConnection(SQLiteDatabase connection, String path) throws SQLException {
		try {
			int d = connection.delete(getResourceString(R.string.table_connections), getResourceString(R.string.delete_connection), new String[] { path });
			System.out.println(">>" + d);
		} catch (SQLException e) {
			throw new SQLException("ERROR BrowserDao - deleteConnection:\n" + e);
		}
	}

	public void deleteAllConnections() throws SQLException {
		openDb();
		connection.beginTransaction();
		try {
			deleteAllConnections(connection);
			connection.setTransactionSuccessful();
		} catch (SQLException e) {
			throw new SQLException("ERROR BrowserDao - deleteAllConnections:\n" + e);
		} finally {
			connection.endTransaction();
			closeDb();
		}
	}

	private void deleteAllConnections(SQLiteDatabase connection) throws SQLException {
		try {
			connection.execSQL(getResourceString(R.string.delete_all_connections));
		} catch (SQLiteException e) {
			throw new SQLException("ERROR BrowserDao - deleteAllConnections:\n" + e);
		}

	}

	public Cursor retrieveConnection(String name, String path) {
		Cursor c = null;
		open();
		connection.beginTransaction();
		try {
			c = retrieveConnection(connection, name, path);
			connection.setTransactionSuccessful();
		} catch (SQLException e) {
			throw new SQLException("ERROR BrowserDao - retrieveConnection:\n" + e);
		} finally {
			connection.endTransaction();
			closeDb();
		}
		return c;
	}

	public Cursor retrieveConnection(SQLiteDatabase connection, String name, String path) {
		Cursor c = null;
		try {
			c = connection.rawQuery(getResourceString(R.string.select_single_connection), new String[] { path });
		} catch (SQLException e) {
			throw new SQLException("ERROR BrowserDao - retrieveConnection:\n" + e);
		}
		return c;
	}

	public void setTextSetting(String value) {
		openDb();
		connection.beginTransaction();
		try {

			setTextSetting(connection, value);
			connection.setTransactionSuccessful();

		} catch (SQLException e) {
			logger.log(Level.INFO, "ERROR BrowserDao - setTextSetting:\n" + e);
		} catch (BrowserException e) {
			logger.log(Level.INFO, "ERROR BrowserDao - setTextSetting:\n" + e);
		} catch (Exception e) {
			logger.log(Level.INFO, "ERROR BrowserDao - setTextSetting:\n" + e);
		} finally {
			connection.endTransaction();
			closeDb();
		}

	}

	private void setTextSetting(SQLiteDatabase connection, String value) throws BrowserException {
		try {

			int ls = getSettingsTextSize(this.connection);
			if (ls <= 0) {
				connection.execSQL(getResourceString(R.string.insert_settings), new Object[] {
						"text_size", value });
			} else {
				connection.execSQL(getResourceString(R.string.update_settings), new Object[] {
						value, "text_size" });
			}
		} catch (SQLException e) {
			throw new SQLiteException("ERROR BrowserDao - saveConnection:\n" + e);
		}
	}

	public int getSettingsTextSize() {
		int tz = 0;
		openDb();
		try {
			tz = getSettingsTextSize(this.connection);
		} catch (SQLException e) {
			throw new SQLException("ERROR BrowserDao - retrieveConnection:\n" + e);
		} finally {
			closeDb();
		}
		return tz;
	}

	private int getSettingsTextSize(SQLiteDatabase connection) {
		int tz = 0;
		Cursor c = connection.rawQuery(getResourceString(R.string.select_settings), new String[] { "text_size" });
		if (c != null && c.getCount() > 0 && c.moveToNext()) {
			// 2da columna corresponde al valor
			tz = Util.stringToInteger(c.getString(1));
		}
		return tz;

	}

	private void openDb() {
		if (connection == null || connection.isOpen() == false) {
			open();
		}
	}

	private void closeDb() {
		try {
			if (connection != null && connection.isOpen()) {
				connection.close();
				mDbHelper.close();
			}
		} catch (Exception e) {
			logger.log(Level.INFO, "ERROR BrowserDao - closeDb:\n" + e);
		}
	}

	private String getResourceString(int stringId) {
		return DbHelper.getResourceString(stringId, mContext);
	}

	private static class DbHelper extends SQLiteOpenHelper {
		Context mHContext = null;

		DbHelper(Context context) {
			super(context, DB_NAME, null, DATABASE_VERSION);
			this.mHContext = context;
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			try {
				db.execSQL(getResourceString(R.string.c_table_connections, mHContext));
			} catch (Exception e) {
				Log.e(BrowserDao.class.getName(), "Erro updating/creating Data Base Table", e);
			}
			try {
				db.execSQL(getResourceString(R.string.c_table_settings, mHContext));
			} catch (Exception e) {
				Log.e(BrowserDao.class.getName(), "Erro updating/creating Data Base Table", e);
			}
			try {
				db.execSQL(getResourceString(R.string.c_table_latests, mHContext));
			} catch (Exception e) {
				Log.e(BrowserDao.class.getName(), "Erro updating/creating Data Base Table", e);
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(BrowserDao.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
			
			
			onCreate(db);
		}

		private static String getResourceString(int stringId, Context cc) {
			return Util.getResourceString(stringId, cc);
		}
	}
}
