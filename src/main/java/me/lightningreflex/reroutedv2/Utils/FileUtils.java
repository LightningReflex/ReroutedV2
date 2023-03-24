package me.lightningreflex.reroutedv2.Utils;

import java.io.File;

public class FileUtils {
	public static void delete(String ...item) {
		for (String s : item) {
			File file = new File(s);
			if (file.isDirectory() && file.list() != null) {
				for (String child : file.list()) {
					delete(new File(file, child).getPath());
				}
			}
			file.delete();
		}
	}

	public static void makeFolder(String ...item) {
		for (String s : item) {
			File file = new File(s);
			if (!file.exists()) {
				file.mkdir();
			}
		}
	}

	public static boolean filesInFolder(String folder) {
		File file = new File(folder);
		if (file.isDirectory() && file.list() != null) {
			return file.list().length > 0;
		}
		return false;
	}

	public static boolean fileExists(String file) {
		return new File(file).exists();
	}

	public static void write(String file, String string) {
		try {
			java.io.FileWriter fw = new java.io.FileWriter(file);
			fw.write(string);
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
