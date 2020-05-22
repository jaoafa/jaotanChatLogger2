package com.jaoafa.jaotanChatLogger2.Lib;

import java.io.File;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Library {
	public static String getCurrentpath() {
		String cp = System.getProperty("java.class.path");
		String fs = System.getProperty("file.separator");
		String acp = (new File(cp)).getAbsolutePath();
		int p, q;
		for (p = 0; (q = acp.indexOf(fs, p)) >= 0; p = q + 1)
			;
		return acp.substring(0, p);
	}

	/**
	 * ホスト名を返す
	 * @return ホスト名。取得できなければnullを返却
	 */
	public static String getHostName() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String sdfFormat(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		return sdf.format(date);
	}

	public static boolean isInt(String s) {
		try {
			Integer.valueOf(s);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	public static boolean isLong(String s) {
		try {
			Long.valueOf(s);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
}
