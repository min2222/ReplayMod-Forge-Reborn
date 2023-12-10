package com.replaymod.render.blend;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.apache.commons.io.output.NullOutputStream;
import org.blender.utils.BlenderFactory;
import org.cakelab.blender.io.BlenderFile;

import com.replaymod.render.ReplayModRender;
import com.replaymod.render.blend.data.DScene;
import com.replaymod.render.blend.data.Serializer;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;

public class BlendState implements Exporter {
	private static BlendState currentState;
	private final List<Exporter> exporters = new ArrayList();
	private final BlenderFile blenderFile;
	private final BlenderFactory factory;
	private final DScene scene = new DScene();
	private final BlendMaterials materials = new BlendMaterials();

	public static void setState(BlendState state) {
		currentState = state;
	}

	public static BlendState getState() {
		return currentState;
	}

	public BlendState(File file) throws IOException {
		this.blenderFile = BlenderFactory.newBlenderFile(file);
		this.factory = new BlenderFactory(this.blenderFile);
	}

	public void register(Exporter exporter) {
		this.exporters.add(exporter);
	}

	public <T extends Exporter> T get(Class<T> clazz) {
		Iterator var2 = this.exporters.iterator();

		Exporter exporter;
		do {
			if (!var2.hasNext()) {
				throw new NoSuchElementException("No exporter of type " + clazz);
			}

			exporter = (Exporter) var2.next();
		} while (!clazz.isInstance(exporter));

		return clazz.cast(exporter);
	}

	public void setup() {
		Iterator var1 = this.exporters.iterator();

		while (var1.hasNext()) {
			Exporter exporter = (Exporter) var1.next();

			try {
				exporter.setup();
			} catch (IOException var6) {
				CrashReport report = CrashReport.forThrowable(var6, "Setup of blend exporter");
				CrashReportCategory category = report.addCategory("Exporter");
				Objects.requireNonNull(exporter);
				category.setDetail("Exporter", exporter::toString);
				throw new ReportedException(report);
			}
		}

	}

	public void tearDown() {
		Iterator var1 = this.exporters.iterator();

		while (var1.hasNext()) {
			Exporter exporter = (Exporter) var1.next();

			try {
				exporter.tearDown();
			} catch (IOException var25) {
				CrashReport report = CrashReport.forThrowable(var25, "Tear down of blend exporter");
				CrashReportCategory category = report.addCategory("Exporter");
				Objects.requireNonNull(exporter);
				category.setDetail("Exporter", exporter::toString);
				throw new ReportedException(report);
			}
		}

		try {
			Serializer serializer = new Serializer();
			this.scene.serialize(serializer);
			PrintStream sysout = System.out;

			try {
				System.setOut(new PrintStream(new NullOutputStream()));
				this.blenderFile.write();
			} finally {
				System.setOut(sysout);
			}
		} catch (IOException var23) {
			ReplayModRender.LOGGER.error("Exception writing blend file: ", var23);
		} finally {
			try {
				this.blenderFile.close();
			} catch (IOException var21) {
				ReplayModRender.LOGGER.error("Exception closing blend file: ", var21);
			}

		}

	}

	public void preFrame(int frame) {
		Iterator var2 = this.exporters.iterator();

		while (var2.hasNext()) {
			Exporter exporter = (Exporter) var2.next();

			try {
				exporter.preFrame(frame);
			} catch (IOException var7) {
				CrashReport report = CrashReport.forThrowable(var7, "Pre frame of blend exporter");
				CrashReportCategory category = report.addCategory("Exporter");
				Objects.requireNonNull(exporter);
				category.setDetail("Exporter", exporter::toString);
				category.setDetail("Frame", () -> {
					return String.valueOf(frame);
				});
				throw new ReportedException(report);
			}
		}

	}

	public void postFrame(int frame) {
		Iterator var2 = this.exporters.iterator();

		while (var2.hasNext()) {
			Exporter exporter = (Exporter) var2.next();

			try {
				exporter.postFrame(frame);
			} catch (IOException var7) {
				CrashReport report = CrashReport.forThrowable(var7, "Post frame of blend exporter");
				CrashReportCategory category = report.addCategory("Exporter");
				Objects.requireNonNull(exporter);
				category.setDetail("Exporter", exporter::toString);
				category.setDetail("Frame", () -> {
					return String.valueOf(frame);
				});
				throw new ReportedException(report);
			}
		}

		this.scene.endFrame = frame;
	}

	public BlenderFile getBlenderFile() {
		return this.blenderFile;
	}

	public BlenderFactory getFactory() {
		return this.factory;
	}

	public DScene getScene() {
		return this.scene;
	}

	public BlendMaterials getMaterials() {
		return this.materials;
	}
}
