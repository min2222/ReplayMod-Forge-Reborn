package com.replaymod.render.utils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.ProtectionDomain;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.commons.io.IOUtils;

import com.replaymod.core.ReplayMod;
import com.replaymod.render.rendering.Frame;
import com.replaymod.render.rendering.FrameConsumer;

public class Lwjgl3Loader extends URLClassLoader {
	private static Path tempJarFile;
	private static Lwjgl3Loader instance;
	private final Set<String> implClasses = new CopyOnWriteArraySet();

	private Lwjgl3Loader(Path jarFile) throws IOException, ReflectiveOperationException {
		super(new URL[] { jarFile.toUri().toURL() }, Lwjgl3Loader.class.getClassLoader());
		Path nativesDir = ReplayMod.instance.folders.getCacheFolder().resolve("lwjgl-natives");
		Class<?> configClass = Class.forName("org.lwjgl.system.Configuration", true, this);
		Object extractDirField = configClass.getField("SHARED_LIBRARY_EXTRACT_DIRECTORY").get((Object) null);
		Method setMethod = configClass.getMethod("set", Object.class);
		setMethod.invoke(extractDirField, nativesDir.toAbsolutePath().toString());
	}

	private boolean canBeSharedWithMc(String name) {
		if (name.startsWith("org.lwjgl.")) {
			return false;
		} else {
			Iterator var2 = this.implClasses.iterator();

			String implClass;
			do {
				if (!var2.hasNext()) {
					return true;
				}

				implClass = (String) var2.next();
			} while (!name.startsWith(implClass));

			return false;
		}
	}

	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		if (!this.canBeSharedWithMc(name)) {
			synchronized (this.getClassLoadingLock(name)) {
				Class<?> cls = this.findLoadedClass(name);
				if (cls == null) {
					cls = this.findClass(name);
				}

				if (resolve) {
					this.resolveClass(cls);
				}

				return cls;
			}
		} else {
			return super.loadClass(name, resolve);
		}
	}

	protected Class<?> findClass(String name) throws ClassNotFoundException {
		try {
			return super.findClass(name);
		} catch (ClassNotFoundException var7) {
			String path = name.replace('.', '/').concat(".class");
			URL url = this.getParent().getResource(path);
			if (url == null) {
				throw var7;
			} else {
				try {
					byte[] bytes = IOUtils.toByteArray(url);
					return this.defineClass(name, bytes, 0, bytes.length, (ProtectionDomain) null);
				} catch (IOException var6) {
					throw new ClassNotFoundException(name, var6);
				}
			}
		}
	}

	private static synchronized Path getJarFile() throws IOException {
		if (tempJarFile == null) {
			Path jarFile = Files.createTempFile("replaymod-lwjgl", ".jar");
			jarFile.toFile().deleteOnExit();
			InputStream in = Lwjgl3Loader.class.getResourceAsStream("lwjgl.jar");

			try {
				if (in == null) {
					throw new IOException("Failed to find embedded lwjgl.jar file.");
				}

				Files.copy(in, jarFile, new CopyOption[] { StandardCopyOption.REPLACE_EXISTING });
			} catch (Throwable var5) {
				if (in != null) {
					try {
						in.close();
					} catch (Throwable var4) {
						var5.addSuppressed(var4);
					}
				}

				throw var5;
			}

			if (in != null) {
				in.close();
			}

			tempJarFile = jarFile;
		}

		return tempJarFile;
	}

	public static synchronized Lwjgl3Loader instance() {
		if (instance == null) {
			try {
				instance = new Lwjgl3Loader(getJarFile());
			} catch (ReflectiveOperationException | IOException var1) {
				throw new RuntimeException(var1);
			}
		}

		return instance;
	}

	public static <P extends Frame> FrameConsumer<P> createFrameConsumer(Class<? extends FrameConsumer<P>> implClass,
			Class<?>[] parameterTypes, Object[] args) {
		try {
			Lwjgl3Loader loader = instance();
			loader.implClasses.add(implClass.getName());
			Class<?> realClass = Class.forName(implClass.getName(), true, loader);
			Constructor<?> constructor = realClass.getConstructor(parameterTypes);
			return (FrameConsumer) constructor.newInstance(args);
		} catch (ReflectiveOperationException var6) {
			throw new RuntimeException(var6);
		}
	}

	static {
		registerAsParallelCapable();
	}
}
