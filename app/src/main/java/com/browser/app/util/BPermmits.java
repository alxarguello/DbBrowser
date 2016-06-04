package com.browser.app.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BPermmits {

	private static BPermmits instance;
	private static Logger logger = Logger.getLogger(BPermmits.class.getName());
	private static Process p;
	private DataOutputStream os;
	private DataInputStream is;

	private BPermmits() {

	}

	public static BPermmits getInstance() {
		if (instance == null) {
			instance = new BPermmits();
		}
		return instance;
	}

	public void rootAccess() {
		try {
			if (p == null) {
				p = Runtime.getRuntime().exec("su");
				os = new DataOutputStream(p.getOutputStream());
				is = new DataInputStream(p.getInputStream());

			}

		} catch (Exception e) {

		}

	}

	public DataOutputStream getOutputStream() {
		return os;
	}

	public DataInputStream getInputStream() {
		return is;
	}

	public void makeReadable(String path) {
		try {

			File f = new File(path);
			String dir = f.getPath().substring(0, f.getPath().length() - f.getName().length());

			makeReadable(dir, path);

		} catch (Exception e) {
			logger.log(Level.SEVERE, "ERROR on BPermmits - makeReadable :\n" + e);
		}
	}

	public void makeReadable(String dir, String file) {
		try {

			@SuppressWarnings("rawtypes")
			Class fileUtils = Class.forName("android.os.FileUtils");
			Method setPermissions = fileUtils.getMethod("setPermissions", String.class, int.class, int.class, int.class);
			setPermissions.invoke(null, dir, 777, -1, -1);
			setPermissions.invoke(null, file, 777, -1, -1);

			if (os != null) {

				os.writeBytes("chmod 777 " + dir + " \n");
				os.writeBytes("chmod 777 " + file + " \n");
				os.flush();
			}
			Thread.sleep(100);

		} catch (Exception e) {
			logger.log(Level.SEVERE, "ERROR on BPermmits - makeReadable :\n" + e);
		}
	}

	public void waitFor() {
		try {
			p.waitFor();
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE, "ERROR on BPermmits - waitFor :\n" + e);
		}
	}

}
