package me.lightningreflex.reroutedv2.Utils;

public class STDUtils {
	public static void in(String string) {
		// If "/home/minecraft/signal/fifo/stdin" exists, write to it.
		if (FileUtils.fileExists("/home/minecraft/signal/fifo/stdin")) {
			FileUtils.write("/home/minecraft/signal/fifo/stdin", string);
		}
	}

	public static void out(String string) {
		// If "/home/minecraft/signal/fifo/stdout" exists, write to it.
		if (FileUtils.fileExists("/home/minecraft/signal/fifo/stdout")) {
			FileUtils.write("/home/minecraft/signal/fifo/stdout", string);
		}
	}

	public static void err(String string) {
		// If "/home/minecraft/signal/fifo/stderr" exists, write to it.
		if (FileUtils.fileExists("/home/minecraft/signal/fifo/stderr")) {
			FileUtils.write("/home/minecraft/signal/fifo/stderr", string);
		}
	}
}
