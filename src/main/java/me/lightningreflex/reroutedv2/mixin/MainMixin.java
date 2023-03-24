package me.lightningreflex.reroutedv2.mixin;

import me.lightningreflex.reroutedv2.Config;
import me.lightningreflex.reroutedv2.Utils.FileUtils;
import me.lightningreflex.reroutedv2.Utils.PrintUtils;
import me.lightningreflex.reroutedv2.Utils.STDUtils;
import net.minecraft.server.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Mixin(Main.class)
public class MainMixin {

	@Inject(
		method = "main", at = @At("HEAD"),
		cancellable = true)
	private static void injected(String[] args, CallbackInfo ci) {
		ci.cancel();
		PrintUtils.print("Successfully hijacked Minecraft server startup.");
		PrintUtils.print("Made by LightningReflex");
		STDUtils.err(" ");
//		STDUtils.err("[ReroutedV2] Successfully hijacked Minecraft server startup.");
//		STDUtils.err("[ReroutedV2] Made by LightningReflex");
		try {
			Files.readAllLines(new File("/home/minecraft/signal/start_cmd").toPath()).forEach(System.out::println);
		} catch (IOException e) {
			e.printStackTrace();
		}


		HashMap<String, Object> config = Config.getConfig();

		RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
		List<String> defaultStartupArguments = runtimeMxBean.getInputArguments();
		String joinedDefaultStartupArguments = String.join(" ", defaultStartupArguments);

//		Process finalServerJarProcess = serverJarProcess;
//		new Thread(() -> {
//			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
//			try {
//				String string;
//				while ((string = bufferedReader.readLine()) != null) {
//					PrintUtils.print("Ran command: " + string);
//					if (finalServerJarProcess != null) {
//						for (int i = 0; i < 2; i++) {
//							finalServerJarProcess.getOutputStream().write((string).getBytes());
//							finalServerJarProcess.getOutputStream().flush();
//						}
//					}
//					if (string.equals("stopnow")) {
//						PrintUtils.print("Stopping server...");
//						if (finalServerJarProcess != null) {
//							finalServerJarProcess.destroy();
//						}
//						Runtime.getRuntime().exit(0);
//					}
//				}
//			}
//			catch (IOException iOException) {
//				PrintUtils.print("Exception handling console input");
//				iOException.printStackTrace();
//			}
//		}).start();

		// Delete unnecessary files
//		FileUtils.delete(
//			".fabric",
//			"config",
//			"logs",
//			"plugins",
//			"eula.txt",
//			"fabric-server-launcher.properties",
//			"server.properties"
//		);

		if ((boolean) config.get("deleteUnnecessaryFiles")) {
			PrintUtils.print("Deleting unnecessary files...");
			STDUtils.err(" ");
			String[] whitelistedItems = {
				"mods", // Keep now, filter later
				"server.jar", // Required for fast startup
				"minecraft-server.jar", // Required for fast startup
				".fabric", // Required for fast startup
				"rerouted", // Main server jar
				"rerouted-config.yml" // Config file
			};
			for (String child : new File(".").list()) {
				if (Arrays.stream(whitelistedItems).toList().contains(child)) {
					continue;
				}
				FileUtils.delete(child);
			}

			// Delete all mods besides the rerouted jar
			File modsFolder = new File("mods");
			if (modsFolder.isDirectory() && modsFolder.list() != null) {
				for (String child : modsFolder.list()) {
					if (!child.endsWith(".jar")) {
						// This is not a jar, delete it
						FileUtils.delete("mods/" + child);
						continue;
					}
					// Check if the jar is the rerouted jar by opening the fabric.mod.json and checking the id key in the jar
					File inputFile = new File("mods/" + child);
					try (ZipInputStream jarInputStream = new ZipInputStream(Files.newInputStream(inputFile.toPath()))) {
						ZipEntry zipEntry;
						outerWhile:
						while ((zipEntry = jarInputStream.getNextEntry()) != null) {
							if (!zipEntry.getName().equals("fabric.mod.json")) {
								continue;
							}
							// Read the fabric.mod.json
							BufferedReader reader = new BufferedReader(new InputStreamReader(jarInputStream));
							String line;
							while ((line = reader.readLine()) != null) {
								if (line.contains("\"id\": \"rerouted-v2\"")) {
									// This is the rerouted jar, don't delete it
									break outerWhile;
								}
							}
							// This is not the rerouted jar, delete it
							FileUtils.delete("mods/" + child);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		} else {
			PrintUtils.print("Not deleting unnecessary files...");
			STDUtils.err(" ");
		}

		// Create folder called "rerouted"
		FileUtils.makeFolder("rerouted");

		// Check if there are any files in the rerouted folder
		if (!FileUtils.fileExists("rerouted/server.jar")) { // server.jar inside rerouted folder does not exist
			// Let user know to put any server.jar in rerouted folder
			PrintUtils.print("Please put a server.jar in the rerouted folder.");
			PrintUtils.print("Then start the server again.");
			PrintUtils.print("Stopping server...");
			Runtime.getRuntime().exit(0);
		}

		String startupArgumentsToUse;
		if ((boolean) config.get("useCustomParameters")) {
			// Use custom parameters
			PrintUtils.print("Using custom startup parameters: " + config.get("customParameters"));
			STDUtils.err(" ");
			startupArgumentsToUse = (String) config.get("customParameters");
		} else {
			// Use default parameters
			PrintUtils.print("Using default startup parameters: " + joinedDefaultStartupArguments);
			STDUtils.err(" ");
			startupArgumentsToUse = joinedDefaultStartupArguments;
		}

		PrintUtils.print("Starting server...");
		// Attempt to start server using server.jar in rerouted folder
		try {
			final String jex = ProcessHandle.current().info().command().orElse("/opt/java/17/bin/java");
			ProcessBuilder serverJarProcessBuilder = new ProcessBuilder(
				"/bin/bash", "-c",
				"cd rerouted && chmod a+x server.jar && " + jex + " " + startupArgumentsToUse + " -jar server.jar"
			).inheritIO();
			Process serverJarProcess = serverJarProcessBuilder.start();
			serverJarProcess.waitFor();
			serverJarProcess.destroy();
			Runtime.getRuntime().exit(0);
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
}
