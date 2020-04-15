/*
 * Chipper Toolbox - a somewhat opinionated collection of assorted utilities for Java
 * Copyright (c) 2019 - 2020 Una Thompson (unascribed), Isaac Ellingson (Falkreon)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.playsawdust.chipper.toolbox.io;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermissions;
import org.checkerframework.checker.nullness.qual.EnsuresNonNull;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.PolyNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.Platform;
import com.sun.jna.platform.win32.KnownFolders;
import com.sun.jna.platform.win32.Shell32Util;

import com.google.common.base.Ascii;

/**
 * An abstraction for platform-specific directories using wording from the XDG Base Directories
 * specification. By default, JNA will be used to detect the running platform and choose an
 * appropriate place to put the directories. For example, with an {@link #setAppName app name} of
 * "my-app" / "My App", the following directories will be used on the given platforms for the
 * {@link #getConfigHome config home}:
 * <ul>
 * <li><strong>Windows</strong>: %APPDATA%\Roaming\My App\Config
 * <li><strong>macOS</strong>: ~/Library/Preferences/My App
 * <li><strong>Linux</strong>: ~/.config/my-app
 * <li><strong>Other</strong>: ~/.my-app
 * </ul>
 */
public class Directories {
	private static final Logger log = LoggerFactory.getLogger(Directories.class);

	private static String appLowerName = "unknown-toolbox-app";
	private static String appName = "Unknown Toolbox App";
	
	private static String addonsLowerName = "addons";
	private static String addonsName = "Addons";

	private static PlatformDirectoryProvider impl;

	/**
	 * Override the PlatformDirectoryProvider with the given one, instead of choosing one
	 * automatically based on the running platform.
	 * <p>
	 * Calling this method during initialization instead of {@link #setAppName} with an implementation
	 * other than {@link WindowsDirectoryProvider} allows Directories to be used without JNA.
	 */
	public static void setProvider(PlatformDirectoryProvider provider) {
		synchronized (Directories.class) {
			if (impl != null) throw new IllegalStateException("Provider already initialized");
			impl = provider;
		}
	}

	/**
	 * Explicitly set the PlatformDirectoryProvider to a PortableDirectoryProvider initialized with
	 * the app name. This will make the directory {@code ./My App/Config} be used as the
	 * {@link #getConfigHome config home}, for example.
	 */
	public static void setPortableProvider() {
		synchronized (Directories.class) {
			setProvider(new PortableDirectoryProvider(appName, true));
		}
	}

	/**
	 * Set the app name used by autodetection. Different platforms have different conventions for
	 * capitalization, so you must provide a lowerName to use where appropriate and a proper-case
	 * name to use where that's appropriate.
	 * <p>
	 * Both strings must be valid file names on Windows, macOS, and Linux. This validation is not
	 * performed by Toolbox.
	 * @param lowerName the lowercase hyphenated name of this app (e.g. "sawdust", "my-app")
	 * @param name the proper-case name of this app (e.g. "Sawdust", "My App")
	 */
	public static void setAppName(String lowerName, String name) {
		synchronized (Directories.class) {
			if (impl != null) throw new IllegalStateException("Provider already initialized");
			appLowerName = lowerName;
			appName = name;
		}
	}
	
	/**
	 * Set this app's preferred name for the addons directory. Used for {@link #getAddonHome} on
	 * platforms where the directory for that home is shared with another home and differentiation
	 * is required.
	 * <p>
	 * Defaults to "addons", "Addons".
	 * @param lowerName the lowercase hyphenated name for addons (e.g. "plugins", "addons")
	 * @param name the proper-case name for addons (e.g. "Plugins", "Addons")
	 */
	public static void setAddonsDirectoryName(String lowerName, String name) {
		synchronized (Directories.class) {
			if (impl != null) throw new IllegalStateException("Provider already initialized");
			addonsLowerName = lowerName;
			addonsName = name;
		}
	}

	private static void initializeProvider() {
		if (impl != null) return;
		synchronized (Directories.class) {
			if (impl != null) return;
			try {
				if (Platform.isMac()) {
					impl = new MacDirectoryProvider(appName);
				} else if (Platform.isWindows()) {
					impl = new WindowsDirectoryProvider(appName);
				} else if (Platform.isLinux()) {
					// TODO how does the BSD community feel about the XDG basedir
					// spec? I feel like they'd probably prefer bare dirs, hence
					// this check being for Linux, but, who knows.
					impl = new XDGDirectoryProvider(appLowerName);
				} else {
					impl = new BareDirectoryProvider(appLowerName, false);
				}
			} catch (NoClassDefFoundError e) {
				throw new Error("Got a NoClassDefFoundError while attempting to perform platform autodetection. Is JNA on the classpath? It's required for autodetect as well as Windows support. Check the Toolbox README for more details.", e);
			}
		}
	}

	/**
	 * Some platforms use "Title Case" names, while others use "snake-case" names. Windows, macOS,
	 * and portable distributions use Title Case. Linux and bare directories use snake-case.
	 * <p>
	 * If adding your own directories under the predefined directories, you should check
	 * this to ensure your names fit in.
	 *
	 * @return {@code true} if "Title Case" names should be used for subdirectories, {@code false}
	 * 		if "snake-case" should be used
	 */
	public static boolean shouldUseTitleNames() {
		initializeProvider();
		return impl.shouldUseTitleNames();
	}

	/**
	 * Corrects "Title Case" names to "snake-case" if {@code #shouldUseTitleNames} is {@code false}.
	 * @param str the name to possibly correct, in "Title Case"
	 * @return the input unchanged if {@link #shouldUseTitleNames} is true, otherwise the result
	 * 		of {@code Ascii.toLowerCase(str).replace(' ', '-')}
	 */
	public static String correctName(String str) {
		if (shouldUseTitleNames()) {
			return str;
		} else {
			return Ascii.toLowerCase(str).replace(' ', '-');
		}
	}

	/**
	 * @return The OS-specific single base directory relative to which
	 * 		user-specific data files should be written.
	 */
	@NonNull
	public static File getDataHome() {
		initializeProvider();
		return impl.getDataHome();
	}

	/**
	 * Returns a subdirectory of {@link #getDataHome the data home}, named the given name
	 * if {@link #shouldUseTitleNames} is true; otherwise, it is forced lower-case and spaces
	 * are replaced with hyphens. Creates the directory if it does not exist.
	 * @param subdir the name of the subdirectory to create under the data home, in "Title Case"
	 */
	@NonNull
	public static File getDataHome(String subdir) {
		return PlatformDirectoryProvider.ensureCreated(new File(getDataHome(), correctName(subdir)));
	}

	/**
	 * @return The OS-specific single base directory relative to which
	 * 		user-specific configuration files should be written.
	 */
	@NonNull
	public static File getConfigHome() {
		initializeProvider();
		return impl.getConfigHome();
	}

	/**
	 * Returns a subdirectory of {@link #getConfigHome the config home}, named the given name
	 * if {@link #shouldUseTitleNames} is true; otherwise, it is forced lower-case and spaces
	 * are replaced with hyphens.
	 * @param subdir the name of the subdirectory to create under the config home, in "Title Case"
	 */
	@NonNull
	public static File getConfigHome(String subdir) {
		return PlatformDirectoryProvider.ensureCreated(new File(getConfigHome(), correctName(subdir)));
	}

	/**
	 * @return The OS-specific single base directory relative to which
	 * 		user-specific non-essential (cached) data should be written.
	 */
	@NonNull
	public static File getCacheHome() {
		initializeProvider();
		return impl.getCacheHome();
	}

	/**
	 * Returns a subdirectory of {@link #getCacheHome the cache home}, named the given name
	 * if {@link #shouldUseTitleNames} is true; otherwise, it is forced lower-case and spaces
	 * are replaced with hyphens.
	 * @param subdir the name of the subdirectory to create under the cache home, in "Title Case"
	 */
	@NonNull
	public static File getCacheHome(String subdir) {
		return PlatformDirectoryProvider.ensureCreated(new File(getCacheHome(), correctName(subdir)));
	}

	/**
	 * @return The OS-specific single base directory relative to which
	 * 		addons will be found.
	 */
	@NonNull
	public static File getAddonHome() {
		initializeProvider();
		return impl.getAddonHome();
	}

	/**
	 * @return The OS-specific single base directory relative to which
	 * 		user-specific non-essential runtime files and other file objects
	 * 		(such as sockets, named pipes, ...) should be stored. This directory
	 * 		will be deleted when the process exits.
	 */
	@NonNull
	public static File getRuntimeDir() {
		initializeProvider();
		return impl.getRuntimeDir();
	}

	public static abstract class PlatformDirectoryProvider {
		public abstract boolean shouldUseTitleNames();
		public abstract @NonNull File getDataHome();
		public abstract @NonNull File getConfigHome();
		public abstract @NonNull File getCacheHome();
		public abstract @NonNull File getAddonHome();
		public abstract @NonNull File getRuntimeDir();

		protected static void deleteOnExit(File dir) {
			if (dir == null) return;
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				try {
					Files.walkFileTree(dir.toPath(), new SimpleFileVisitor<Path>() {
						@Override
						public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
							Files.delete(file);
							return FileVisitResult.CONTINUE;
						}

						@Override
						public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
							Files.delete(dir);
							return FileVisitResult.CONTINUE;
						}
					});
				} catch (IOException e) {
					LoggerFactory.getLogger("Directories").warn("Failed to delete {}", dir, e);
				}
			}, "Runtime directory cleanup thread"));
		}

		protected static @NonNull File ensureCreated(@NonNull File dir) {
			if (dir.exists() && !dir.isDirectory()) {
				throw new IllegalStateException(dir+" exists and is not a directory");
			}
			if (!dir.mkdirs() && !dir.isDirectory()) {
				throw new RuntimeException("Failed to create directory "+dir);
			}
			return dir;
		}
	}

	public static class BareDirectoryProvider extends PlatformDirectoryProvider {

		protected final String appName;
		private boolean hasUsedRuntimeDir = false;
		private boolean properCase = false;

		public BareDirectoryProvider(String appName, boolean properCase) {
			this.appName = appName;
			this.properCase = properCase;
		}

		@Override
		public boolean shouldUseTitleNames() {
			return properCase;
		}

		@Override
		@NonNull
		public File getDataHome() {
			return ensureCreated(new File(getHome(), properCase ? "Data" : "data"));
		}

		@Override
		@NonNull
		public File getConfigHome() {
			return ensureCreated(new File(getHome(), properCase ? "Config" : "config"));
		}

		@Override
		@NonNull
		public File getCacheHome() {
			return ensureCreated(new File(getHome(), properCase ? "Cache" : "cache"));
		}

		@Override
		@NonNull
		public File getAddonHome() {
			return ensureCreated(new File(getHome(), properCase ? addonsName : addonsLowerName));
		}

		@Override
		@NonNull
		public File getRuntimeDir() {
			File f = ensureCreated(new File(getHome(), properCase ? "Runtime" : "runtime"));
			if (!hasUsedRuntimeDir) {
				hasUsedRuntimeDir = true;
				deleteOnExit(f);
			}
			return f;
		}

		protected File getHome() {
			return new File(System.getProperty("user.home"), "."+appName);
		}

	}

	public static class PortableDirectoryProvider extends BareDirectoryProvider {

		public PortableDirectoryProvider(String appName, boolean properCase) {
			super(appName, properCase);
		}

		@Override
		protected File getHome() {
			return new File("./"+appName);
		}

	}

	/**
	 * <a href="https://developer.apple.com/library/content/qa/qa1170/_index.html">https://developer.apple.com/library/content/qa/qa1170/_index.html</a>
	 */
	public static class MacDirectoryProvider extends PlatformDirectoryProvider {

		private final String appName;
		@MonotonicNonNull
		private File runtimeDir;

		public MacDirectoryProvider(String appName) {
			this.appName = appName;
		}

		@Override
		public boolean shouldUseTitleNames() {
			return true;
		}

		@Override
		@NonNull
		public File getDataHome() {
			return ensureCreated(getLibrary(appName));
		}

		@Override
		@NonNull
		public File getConfigHome() {
			return ensureCreated(new File(getLibrary("Preferences"), appName));
		}

		@Override
		@NonNull
		public File getCacheHome() {
			return ensureCreated(new File(getLibrary("Caches"), appName));
		}

		@Override
		@NonNull
		public File getAddonHome() {
			return ensureCreated(new File(getLibrary("Application Support"), appName+"/"+addonsName));
		}

		private File getLibrary(String base) {
			return new File(System.getProperty("user.home"), "Library/"+base);
		}

		@Override
		@NonNull
		@EnsuresNonNull("this.runtimeDir")
		public File getRuntimeDir() {
			if (runtimeDir == null) {
				runtimeDir = com.google.common.io.Files.createTempDir();
				deleteOnExit(runtimeDir);
			}
			return runtimeDir;
		}

	}

	public static class WindowsDirectoryProvider extends PlatformDirectoryProvider {
		private static final String WINDOWS_JNA_ERROR =
				"Got a NoClassDefFoundError while attempting to retrieve a Windows directory. "
				+ "Is JNA on the classpath? It's required to invoke native Windows functions to find "
				+ "the correct directories.";

		private final String appName;
		private boolean hasUsedRuntimeDir = false;

		public WindowsDirectoryProvider(String appName) {
			this.appName = appName;
		}

		@Override
		public boolean shouldUseTitleNames() {
			return true;
		}

		@Override
		@NonNull
		public File getDataHome() {
			return ensureCreated(new File(getRoaming(), "Data"));
		}

		@Override
		@NonNull
		public File getConfigHome() {
			return ensureCreated(new File(getRoaming(), "Config"));
		}

		@Override
		@NonNull
		public File getCacheHome() {
			return ensureCreated(new File(getLocal(), "Cache"));
		}

		@Override
		@NonNull
		public File getAddonHome() {
			return ensureCreated(new File(getRoaming(), addonsName));
		}

		@Override
		@NonNull
		public File getRuntimeDir() {
			File f = ensureCreated(new File(getLocal(), "Runtime"));
			if (!hasUsedRuntimeDir) {
				hasUsedRuntimeDir = true;
				deleteOnExit(f);
			}
			return f;
		}

		private File getRoaming() {
			try {
				return new File(Shell32Util.getKnownFolderPath(KnownFolders.FOLDERID_RoamingAppData), appName);
			} catch (NoClassDefFoundError e) {
				throw new Error(WINDOWS_JNA_ERROR, e);
			}
		}

		private File getLocal() {
			try {
				return new File(Shell32Util.getKnownFolderPath(KnownFolders.FOLDERID_LocalAppData), appName);
			} catch (NoClassDefFoundError e) {
				throw new Error(WINDOWS_JNA_ERROR, e);
			}
		}

	}

	/**
1	 * Basic implementation of the <a href="https://specifications.freedesktop.org/basedir-spec/basedir-spec-latest.html">XDG Base Directory Specification</a>
	 */
	public static class XDGDirectoryProvider extends PlatformDirectoryProvider {
		private final String appName;
		@MonotonicNonNull
		private File runtimeDir;

		public XDGDirectoryProvider(String appName) {
			this.appName = appName;
		}

		@Override
		public boolean shouldUseTitleNames() {
			return false;
		}

		/**
		 * @return The single base directory relative to which user-specific data
		 * 		files should be written. This directory is defined by the
		 * 		environment variable {@code $XDG_DATA_HOME}.
		 */
		@Override
		@NonNull
		public File getDataHome() {
			return getBaseDir("XDG_DATA_HOME", "/.local/share");
		}

		@Override
		@NonNull
		public File getAddonHome() {
			return ensureCreated(new File(getDataHome(), addonsLowerName));
		}

		/**
		 * @return The single base directory relative to which user-specific
		 * 		configuration files should be written. This directory is defined by
		 * 		the environment variable {@code $XDG_CONFIG_HOME}.
		 */
		@Override
		@NonNull
		public File getConfigHome() {
			return getBaseDir("XDG_CONFIG_HOME", "/.config");
		}

		/**
		 * @return The single base directory relative to which user-specific
		 * 		non-essential (cached) data should be written. This directory is defined
		 * 		by the environment variable {@code $XDG_CACHE_HOME}.
		 */
		@Override
		@NonNull
		public File getCacheHome() {
			return getBaseDir("XDG_CACHE_HOME", "/.cache");
		}

		/**
		 * @return The single base directory relative to which user-specific
		 * 		non-essential runtime files and other file objects (such as sockets,
		 * 		named pipes, ...) should be stored.
		 */
		@Override
		@NonNull
		@EnsuresNonNull("this.runtimeDir")
		public File getRuntimeDir() {
			if (runtimeDir != null) return runtimeDir;
			File dir = getBaseDir("XDG_RUNTIME_DIR", null);
			if (dir == null) {
				log.warn("Synthesizing runtime directory, as $XDG_RUNTIME_DIR is unset");
				dir = new File(System.getProperty("java.io.tmpdir"));
				dir = ensureCreated(new File(dir, appLowerName+"_"+System.getProperty("user.name")));
			}
			try {
				Files.setPosixFilePermissions(dir.toPath(), PosixFilePermissions.fromString("rwx------"));
			} catch (IOException | UnsupportedOperationException e) {
				log.warn("Failed to set directory permissions on {} to owner-only", dir, e);
			}
			runtimeDir = dir;
			deleteOnExit(dir);
			return dir;
		}

		@PolyNull
		private File getBaseDir(@NonNull String env, @PolyNull String def) {
			String home = System.getenv("HOME");
			if (home == null || home.trim().isEmpty()) {
				home = System.getProperty("user.home");
			}
			String dir = System.getenv(env);
			if (dir == null || dir.trim().isEmpty()) {
				if (def == null) return null;
				dir = home+def;
			}
			return ensureCreated(new File(dir, appName));
		}

	}
}