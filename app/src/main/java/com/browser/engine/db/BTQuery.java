package com.browser.engine.db;

import com.browser.app.dao.BrowserDao;

import android.content.Context;
import android.os.Handler;

public class BTQuery extends Thread {

	private BConnection connection;
	private BrowserDao mDao;
	private String sql;
	private Handler hd;
	// private boolean stop=false;
	private Object sync;
	private boolean saveState;

	public BTQuery(BConnection connection, String sql, Handler hd, Object sync,
			boolean saveState, Context context) {
		this.connection = connection;
		this.sql = sql;
		this.hd = hd;
		this.sync = sync;
		this.saveState = saveState;

		mDao = new BrowserDao(context);

	}

	public void run() {
		synchronized (sync) {
			connection.execQuery(sql);
			if (saveState && sql!=null && sql.equals("")==false) {
				mDao.saveQuery(sql);
			}
			if(hd!=null){
				hd.sendEmptyMessage(0);
			}
		}
	}

	public void stopThread() {
		try {
			this.interrupt();
		} catch (Exception e) {
		}
	}
}
