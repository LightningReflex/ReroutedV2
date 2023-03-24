package me.lightningreflex.reroutedv2;

import me.lightningreflex.reroutedv2.Utils.FileUtils;
import org.apache.logging.log4j.core.config.yaml.YamlConfiguration;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.HashMap;

public class Config {
	private static HashMap<String, Object> config = null;

	public static HashMap<String, Object> getConfig() {
		if (config == null) {
			try {
				if (!FileUtils.fileExists("rerouted-config.yml")) {
					// Move rerouted-config.yml from jar resources to current directory
					ExportResource("/rerouted-config.yml", "../");
				}

				// Load config from current directory
				InputStream fileInputStream = new FileInputStream("rerouted-config.yml");

				// Load config into HashMap
				Yaml yaml = new Yaml();
				config = yaml.load(fileInputStream);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return config;
	}

	static public boolean ExportResource(String resourceName, String relativeExportLocation) throws Exception {
		InputStream stream = null;
		OutputStream resStreamOut = null;
		String jarFolder;
		boolean success = false;
		try {
			// Note that each / is a directory down in the "jar tree" been the jar the root of the tree
			stream = Rerouted.class.getResourceAsStream(resourceName);
			if (stream == null) {
				throw new Exception("Cannot get resource \"" + resourceName + "\" from Jar file.");
			}

			int readBytes;
			byte[] buffer = new byte[4096];
			jarFolder = new File(
				Rerouted.class
					.getProtectionDomain()
					.getCodeSource()
					.getLocation()
					.toURI()
					.getPath())
				.getParentFile()
				.getPath()
				.replace('\\', '/');
			if (!jarFolder.endsWith("/")) {
				jarFolder += "/";
			}
			if (!relativeExportLocation.endsWith("/")) {
				relativeExportLocation += "/";
			}
			resStreamOut = new FileOutputStream(jarFolder + relativeExportLocation + resourceName);
			while ((readBytes = stream.read(buffer)) > 0) {
				resStreamOut.write(buffer, 0, readBytes);
			}
			success = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			stream.close();
			resStreamOut.close();
		}

		return success;
	}
}
