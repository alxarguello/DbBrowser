package com.browser.engine.db;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.os.Environment;

public class BExport {

	private static final Logger logger = Logger.getLogger(BExport.class.getName());

	public static String exportToString(BCursor data) {
		String r = null;
		try {
			if (data != null
					&& (data.getCount() > 0 || data.getColumnCount() > 0)) {

				StringBuffer sb = new StringBuffer();

				if (data != null && data.getColumnCount() > 0) {
					boolean first = true;
					for (int c = 0; c < data.getColumnCount(); c++) {
						String col = data.getColumnName(c);
						sb.append(col);
						if (first) {
							first = false;
							sb.append("|");
						} else if ((c + 1) < data.getColumnCount()) {
							sb.append("|");
						} else {
							sb.append("\n");
						}
					}
				}

				if (data != null && data.getCount() > 0) {

					while (data.moveToNext()) {
						boolean first = true;
						for (int c = 0; c < data.getColumnCount(); c++) {
							String cdata = data.getString(c);
							sb.append(cdata);
							if (first) {
								first = false;
								sb.append("|");
							} else if ((c + 1) < data.getColumnCount()) {
								sb.append("|");
							} else {
								sb.append("\n");
							}
						}
					}
				}
				r=sb.toString();
			}
		} catch (Exception ex) {
			logger.log(Level.SEVERE, "ERROR BExport - exportToString:\n"
					+ ex.getMessage());
		}
		if (r == null) {
			r = "";
		}
		return r;
	}

	public static File saveExportFile(String fileName, BCursor data) {

		File dir = Environment.getRootDirectory();
		File exp = null;
		if (data != null && (data.getCount() > 0 || data.getColumnCount() > 0)) {
			exp = new File(dir, fileName + ".txt");
			if (exp.exists()) {
				exp.delete();
			}
			try {

				FileWriter fw = new FileWriter(exp);

				if (data != null && data.getColumnCount() > 0) {
					boolean first = true;
					for (int c = 0; c < data.getColumnCount(); c++) {
						String col = data.getColumnName(c);
						fw.write(col);
						if (first) {
							first = false;
						} else if ((c + 1) < data.getColumnCount()) {
							fw.write("|");
						} else {
							fw.write("\n");
						}
					}
				}
				fw.flush();
				if (data != null && data.getCount() > 0 && exp.createNewFile()) {
					while (data.moveToNext()) {
						boolean first = true;
						for (int c = 0; c < data.getColumnCount(); c++) {
							String cdata = data.getString(c);
							fw.write(cdata);
							if (first) {
								first = false;
							} else if ((c + 1) < data.getColumnCount()) {
								fw.write("|");
							} else {
								fw.write("\n");
							}
						}
					}
				}
				fw.flush();

			} catch (IOException e) {
				logger.log(Level.INFO, "ERROR BExport - saveExportFile:\n" + e);
			}
		}
		return exp;
	}

}
