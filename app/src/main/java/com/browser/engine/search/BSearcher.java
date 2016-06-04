package com.browser.engine.search;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import com.browser.app.util.BPermmits;

import android.os.Handler;

public final class BSearcher extends Thread {

	public static final int SEARCH_STORAGE = 1;
	public static final int SEARCH_DEVICE = 2;

	private DataOutputStream os;
	private DataInputStream is;

	private SearchEngine engine;

	private static final List<HashMap<String, String>> dbs = new ArrayList<HashMap<String, String>>();
	private ArrayList<String> apps = new ArrayList<String>();

	public static final String DB_NAME = "dbName";
	public static final String PATH = "path";

	private static int deep = 0;
	private Handler hd;
	private String filter;
	private boolean stop;

	public BSearcher(Handler hd, String filter, SearchEngine engine) {
		this.hd = hd;
		this.filter = filter;
		this.engine = engine;
	}

	public void run() {
		stop = false;
		searchDataBases();
		hd.sendEmptyMessage(0);
	}

	public void stopThread() {
		stop = true;
		try {
			this.interrupt();
		} catch (Exception e) {
		}
	}

	public void searchDataBases() {
		dbs.clear();
		System.out.println(">>");
		switch (engine) {
		case Apps:
			searchApps();
			break;
		case Storage:
			searchStorage();
			break;
		}
		System.out.println("<<");
	}

	private void searchApps() {

		try {
			BPermmits p = BPermmits.getInstance();
			p.rootAccess();
			os = p.getOutputStream();
			is = p.getInputStream();

			os.writeBytes("ls -la /data/data/ \n");
			os.flush();

			Thread.sleep(50);

			String l = null;

			while (is.available() > 0) {
				l = is.readLine();
				apps.add(l);
			}

			HashMap<String, String> column;
			
			for (String d : apps) {
				String dir = "/data/data/" + d + "/databases/";

				os.writeBytes("ls -la " + dir + " \n");
				os.flush();
				Thread.sleep(50);
				String ll = null;

				while (is.available() > 0) {
					ll = is.readLine();
					if (filter != null && filter.equals("") == false) {
						if(!ll.contains(filter)){
							continue;
						}
					}
					
					column = new HashMap<String, String>();
					column.put(DB_NAME, ll);
					column.put(PATH, dir + ll);
					dbs.add(column);

				}

			}

		} catch (Exception e) {

		}
	}

	private void searchStorage() {
		search("/");
	}

	private void search(String directory) {
		File f = new File(directory);
		if (stop == false && f.isDirectory() && (f.getPath().startsWith("/proc") == false && f.getPath().startsWith("/sys") == false && deep < 8)) {
			System.out.println(".");
			deep++;
			File[] files = f.listFiles();
			if (files != null) {
				for (File file : files) {
					search(file.getPath());
				}
			}
			deep--;
		} else if (f.isFile()) {
			addDb(f);
		}

	}

	private void addDb(File file) {
		HashMap<String, String> columns;
		if (file != null && file.isFile() && (file.getName().contains(filter)) && stop == false) {
			System.out.println("..>> " + file.getAbsolutePath());
			columns = new HashMap<String, String>();
			columns.put(DB_NAME, file.getName());
			columns.put(PATH, file.getAbsolutePath());
			dbs.add(columns);
		}
	}

	/*
	 * public Handler handler = new Handler() {
	 * 
	 * @Override public void handleMessage(Message msg) { pd.dismiss(); } };
	 */

	public static List<HashMap<String, String>> getDbs() {
		return dbs;
	}

	public boolean isStoped() {
		return stop;
	}

	public enum SearchEngine {

		Apps(0), Storage(1);

		int type;

		SearchEngine(int type) {
			this.type = type;
		}

		public int getType() {
			return type;
		}

	}

	

}
