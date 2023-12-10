package com.replaymod.core.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Throwables;
import com.google.common.net.PercentEscaper;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.replaymod.core.ReplayMod;
import com.replaymod.core.versions.MCVer;
import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.RenderInfo;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.AbstractGuiScrollable;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiPanel;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiScrollable;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiButton;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiLabel;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.HorizontalLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.LayoutData;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.VerticalLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.popup.GuiInfoPopup;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Colors;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.Image;
import com.replaymod.replaystudio.lib.viaversion.api.protocol.version.ProtocolVersion;

import net.minecraft.CrashReport;
import net.minecraft.client.gui.screens.Screen;

public class Utils {
	private static Logger LOGGER = LogManager.getLogger();
	public static final Image DEFAULT_THUMBNAIL;
	public static final SSLContext SSL_CONTEXT;
	public static final SSLSocketFactory SSL_SOCKET_FACTORY;
	private static final PercentEscaper REPLAY_NAME_ENCODER;

	private static InputStream getResourceAsStream(String path) {
		return Utils.class.getResourceAsStream(path);
	}

	public static String convertSecondsToShortString(int seconds) {
		int hours = seconds / 3600;
		int min = seconds / 60 - hours * 60;
		int sec = seconds - (min * 60 + hours * 60 * 60);
		StringBuilder builder = new StringBuilder();
		if (hours > 0) {
			builder.append(String.format("%02d", hours)).append(":");
		}

		builder.append(String.format("%02d", min)).append(":");
		builder.append(String.format("%02d", sec));
		return builder.toString();
	}

	public static Dimension fitIntoBounds(ReadableDimension toFit, ReadableDimension bounds) {
		int width = toFit.getWidth();
		int height = toFit.getHeight();
		float w = (float) width / (float) bounds.getWidth();
		float h = (float) height / (float) bounds.getHeight();
		if (w > h) {
			height = (int) ((float) height / w);
			width = (int) ((float) width / w);
		} else {
			height = (int) ((float) height / h);
			width = (int) ((float) width / h);
		}

		return new Dimension(width, height);
	}

	public static boolean isValidEmailAddress(String mail) {
		return mail.matches(
				"^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$");
	}

	public static Path replayNameToPath(Path folder, String replayName) {
		return isUsable(folder, replayName + ".mcpr") ? folder.resolve(replayName + ".mcpr")
				: folder.resolve(REPLAY_NAME_ENCODER.escape(replayName) + ".mcpr");
	}

	private static boolean isUsable(Path folder, String fileName) {
		if (fileName.contains(folder.getFileSystem().getSeparator())) {
			return false;
		} else {
			Path path;
			try {
				path = folder.resolve(fileName);
			} catch (InvalidPathException var7) {
				return false;
			}

			if (Files.exists(path, new LinkOption[0])) {
				return true;
			} else {
				try {
					OutputStream outputStream = Files.newOutputStream(path, StandardOpenOption.CREATE_NEW);

					try {
						outputStream.flush();
					} catch (Throwable var8) {
						if (outputStream != null) {
							try {
								outputStream.close();
							} catch (Throwable var6) {
								var8.addSuppressed(var6);
							}
						}

						throw var8;
					}

					if (outputStream != null) {
						outputStream.close();
					}
				} catch (IOException var10) {
					return false;
				}

				int var11 = 0;

				while (true) {
					try {
						Files.delete(path);
						return true;
					} catch (IOException var9) {
						if (var11++ > 100) {
							LOGGER.warn("Repeatedly failed to clean up temporary test file at " + path + ": ", var9);
							return false;
						}
					}
				}
			}
		}
	}

	public static String fileNameToReplayName(String fileName) {
		String baseName = FilenameUtils.getBaseName(fileName);

		try {
			return URLDecoder.decode(baseName, Charsets.UTF_8.name());
		} catch (IllegalArgumentException var3) {
			return baseName;
		} catch (UnsupportedEncodingException var4) {
			throw Throwables.propagate(var4);
		}
	}

	public static boolean isCtrlDown() {
		return Screen.hasControlDown();
	}

	public static <T> void addCallback(ListenableFuture<T> future, Consumer<T> onSuccess,
			Consumer<Throwable> onFailure) {
		Futures.addCallback(future, new FutureCallback<T>() {
			public void onSuccess(@Nullable T result) {
				onSuccess.accept(result);
			}

			public void onFailure(@Nonnull Throwable t) {
				onFailure.accept(t);
			}
		}, Runnable::run);
	}

	public static GuiInfoPopup error(Logger logger, GuiContainer container, CrashReport crashReport, Runnable onClose) {
		String crashReportStr = crashReport.getFriendlyReport();
		logger.error(crashReportStr);
		if (crashReport.getSaveFile() == null) {
			try {
				File folder = new File(MCVer.getMinecraft().gameDirectory, "crash-reports");
				SimpleDateFormat var10003 = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
				Date var10004 = new Date();
				File file = new File(folder, "crash-" + var10003.format(var10004) + "-client.txt");
				logger.debug("Saving crash report to file: {}", file);
				crashReport.saveToFile(file);
			} catch (Throwable var7) {
				logger.error("Saving crash report file:", var7);
			}
		} else {
			logger.debug("Not saving crash report as file already exists: {}", crashReport.getSaveFile());
		}

		logger.trace("Opening crash report popup GUI");
		Utils.GuiCrashReportPopup popup = new Utils.GuiCrashReportPopup(container, crashReportStr);
		popup.onClosed(() -> {
			logger.trace("Crash report popup closed");
			if (onClose != null) {
				onClose.run();
			}

		});
		return popup;
	}

	public static <T extends Throwable> void throwIfInstanceOf(Throwable t, Class<T> cls) throws T {
		if (cls.isInstance(t)) {
			throw cls.cast(t);
		}
	}

	public static void throwIfUnchecked(Throwable t) {
		if (t instanceof RuntimeException) {
			throw (RuntimeException) t;
		} else if (t instanceof Error) {
			throw (Error) t;
		}
	}

	public static void denyIfMinimalMode(GuiContainer container, Runnable onPopupClosed, Runnable orElseRun) {
		if (isNotMinimalModeElsePopup(container, onPopupClosed)) {
			orElseRun.run();
		}

	}

	public static boolean ifMinimalModeDoPopup(GuiContainer container, Runnable onPopupClosed) {
		return !isNotMinimalModeElsePopup(container, onPopupClosed);
	}

	public static boolean isNotMinimalModeElsePopup(GuiContainer container, Runnable onPopupClosed) {
		if (!ReplayMod.isMinimalMode()) {
			LOGGER.trace("Minimal mode not active, continuing");
			return true;
		} else {
			LOGGER.trace("Minimal mode active, denying action, opening popup");
			Utils.MinimalModeUnsupportedPopup popup = new Utils.MinimalModeUnsupportedPopup(container);
			popup.onClosed(() -> {
				LOGGER.trace("Minimal mode popup closed");
				if (onPopupClosed != null) {
					onPopupClosed.run();
				}

			});
			return false;
		}
	}

	public static <T> T configure(T instance, Consumer<T> configure) {
		configure.accept(instance);
		return instance;
	}

	public static Path ensureDirectoryExists(Path path) throws IOException {
		return Files.createDirectories(Files.exists(path, new LinkOption[0]) ? path.toRealPath() : path);
	}

	static {
		Image thumbnail;
		try {
			thumbnail = Image.read(getResourceAsStream("/default_thumb.jpg"));
		} catch (Exception var6) {
			thumbnail = new Image(1, 1);
			var6.printStackTrace();
		}

		DEFAULT_THUMBNAIL = thumbnail;

		try {
			InputStream in = getResourceAsStream("/dst_root_ca_x3.pem");

			try {
				Certificate certificate = CertificateFactory.getInstance("X.509").generateCertificate(in);
				KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
				keyStore.load((InputStream) null, (char[]) null);
				keyStore.setCertificateEntry("1", certificate);
				TrustManagerFactory trustManagerFactory = TrustManagerFactory
						.getInstance(TrustManagerFactory.getDefaultAlgorithm());
				trustManagerFactory.init(keyStore);
				SSLContext ctx = SSLContext.getInstance("TLS");
				ctx.init((KeyManager[]) null, trustManagerFactory.getTrustManagers(), (SecureRandom) null);
				SSL_CONTEXT = ctx;
				SSL_SOCKET_FACTORY = ctx.getSocketFactory();
			} catch (Throwable var7) {
				if (in != null) {
					try {
						in.close();
					} catch (Throwable var5) {
						var7.addSuppressed(var5);
					}
				}

				throw var7;
			}

			if (in != null) {
				in.close();
			}
		} catch (CertificateException | KeyStoreException | NoSuchAlgorithmException | KeyManagementException
				| IOException var8) {
			throw new RuntimeException(var8);
		}

		REPLAY_NAME_ENCODER = new PercentEscaper(".-_ ", false);
	}

	private static class GuiCrashReportPopup extends GuiInfoPopup {
		private final GuiScrollable scrollable;

		public GuiCrashReportPopup(GuiContainer container, String crashReport) {
			super(container);
			this.setBackgroundColor(Colors.DARK_TRANSPARENT);
			this.getInfo().addElements(new VerticalLayout.Data(0.5D),
					new GuiElement[] {
							((GuiLabel) (new GuiLabel()).setColor(Colors.BLACK))
									.setI18nText("replaymod.gui.unknownerror", new Object[0]),
							this.scrollable = (GuiScrollable) ((GuiScrollable) ((GuiScrollable) (new GuiScrollable())
									.setScrollDirection(AbstractGuiScrollable.Direction.VERTICAL))
									.setLayout((new VerticalLayout()).setSpacing(2)))
									.addElements((LayoutData) null, (GuiElement[]) Arrays
											.stream(crashReport.replace("\t", "    ").split("\n")).map((l) -> {
												return (GuiLabel) ((GuiLabel) (new GuiLabel()).setText(l))
														.setColor(Colors.BLACK);
											}).toArray((x$0) -> {
												return new GuiElement[x$0];
											})) });
			GuiButton copyToClipboardButton = (GuiButton) ((GuiButton) ((GuiButton) (new GuiButton())
					.setI18nLabel("chat.copy", new Object[0])).onClick(() -> {
						com.replaymod.lib.de.johni0702.minecraft.gui.versions.MCVer.setClipboardString(crashReport);
					})).setSize(150, 20);
			GuiButton closeButton = this.getCloseButton();
			this.popup.removeElement(closeButton);
			this.popup.addElements(new VerticalLayout.Data(1.0D),
					new GuiElement[] {
							((GuiPanel) ((GuiPanel) (new GuiPanel()).setLayout((new HorizontalLayout()).setSpacing(5)))
									.setSize(305, 20)).addElements((LayoutData) null,
											new GuiElement[] { copyToClipboardButton, closeButton }) });
			this.open();
		}

		public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
			this.scrollable.setSize(size.getWidth() * 3 / 4, size.getHeight() * 3 / 4);
			super.draw(renderer, size, renderInfo);
		}
	}

	private static class MinimalModeUnsupportedPopup extends GuiInfoPopup {
		private MinimalModeUnsupportedPopup(GuiContainer container) {
			super(container);
			this.setBackgroundColor(Colors.DARK_TRANSPARENT);
			ProtocolVersion latestVersion = (ProtocolVersion) ProtocolVersion.getProtocols().stream()
					.max(Comparator.comparing(ProtocolVersion::getVersion)).orElseThrow(RuntimeException::new);
			this.getInfo().addElements(new VerticalLayout.Data(0.5D),
					new GuiElement[] {
							((GuiLabel) (new GuiLabel()).setColor(Colors.BLACK))
									.setI18nText("replaymod.gui.minimalmode.unsupported", new Object[0]),
							((GuiLabel) (new GuiLabel()).setColor(Colors.BLACK)).setI18nText(
									"replaymod.gui.minimalmode.supportedversion",
									new Object[] { "1.7.10 - " + latestVersion.getName() }) });
			this.open();
		}
	}
}
