package com.replaymod.lib.de.johni0702.minecraft.gui;

import java.util.Objects;

import com.replaymod.lib.de.johni0702.minecraft.gui.versions.MCVer;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;

public class RenderInfo {
	public final float partialTick;
	public final int mouseX;
	public final int mouseY;
	public final int layer;

	public RenderInfo(float partialTick, int mouseX, int mouseY, int layer) {
		this.partialTick = partialTick;
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		this.layer = layer;
	}

	public RenderInfo offsetMouse(int minusX, int minusY) {
		return new RenderInfo(this.partialTick, this.mouseX - minusX, this.mouseY - minusY, this.layer);
	}

	public RenderInfo layer(int layer) {
		return this.layer == layer ? this : new RenderInfo(this.partialTick, this.mouseX, this.mouseY, layer);
	}

	public void addTo(CrashReport crashReport) {
		CrashReportCategory category = crashReport.addCategory("Render info details");
		MCVer.addDetail(category, "Partial Tick", () -> "" + partialTick);
		MCVer.addDetail(category, "Mouse X", () -> "" + mouseX);
		MCVer.addDetail(category, "Mouse Y", () -> "" + mouseY);
		MCVer.addDetail(category, "Layer", () -> "" + layer);
	}

	public float getPartialTick() {
		return this.partialTick;
	}

	public int getMouseX() {
		return this.mouseX;
	}

	public int getMouseY() {
		return this.mouseY;
	}

	public int getLayer() {
		return this.layer;
	}

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if (o != null && this.getClass() == o.getClass()) {
			RenderInfo that = (RenderInfo) o;
			return Float.compare(that.partialTick, this.partialTick) == 0 && this.mouseX == that.mouseX
					&& this.mouseY == that.mouseY && this.layer == that.layer;
		} else {
			return false;
		}
	}

	public int hashCode() {
		return Objects.hash(new Object[] { this.partialTick, this.mouseX, this.mouseY, this.layer });
	}

	public String toString() {
		return "RenderInfo{partialTick=" + this.partialTick + ", mouseX=" + this.mouseX + ", mouseY=" + this.mouseY
				+ ", layer=" + this.layer + "}";
	}
}
