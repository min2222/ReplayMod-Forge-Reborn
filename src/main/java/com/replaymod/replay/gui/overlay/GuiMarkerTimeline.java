package com.replaymod.replay.gui.overlay;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.replaymod.core.ReplayMod;
import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.RenderInfo;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.advanced.AbstractGuiTimeline;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Draggable;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Typeable;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Utils;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Point;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;
import com.replaymod.replay.ReplayHandler;
import com.replaymod.replay.ReplayModReplay;
import com.replaymod.replay.camera.CameraEntity;
import com.replaymod.replaystudio.data.Marker;
import com.replaymod.replaystudio.util.Location;

import net.minecraft.client.resources.language.I18n;

public class GuiMarkerTimeline extends AbstractGuiTimeline<GuiMarkerTimeline> implements Draggable, Typeable {
	protected static final int TEXTURE_MARKER_X = 109;
	protected static final int TEXTURE_MARKER_Y = 20;
	protected static final int TEXTURE_MARKER_SELECTED_X = 114;
	protected static final int TEXTURE_MARKER_SELECTED_Y = 20;
	protected static final int MARKER_SIZE = 5;
	@Nullable
	private final ReplayHandler replayHandler;
	private final Consumer<Set<Marker>> saveMarkers;
	protected Set<Marker> markers;
	private ReadableDimension lastSize;
	private Marker selectedMarker;
	private int draggingStartX;
	private int draggingTimeDelta;
	private boolean dragging;
	private long lastClickTime;

	public GuiMarkerTimeline(@Nonnull ReplayHandler replayHandler) {
		this.replayHandler = replayHandler;

		try {
			this.markers = (Set) replayHandler.getReplayFile().getMarkers().or(HashSet::new);
		} catch (IOException var3) {
			ReplayModReplay.LOGGER.error("Failed to get markers from replay", var3);
			this.markers = new HashSet();
		}

		this.saveMarkers = (markers) -> {
			try {
				replayHandler.getReplayFile().writeMarkers(markers);
			} catch (IOException var3) {
				ReplayModReplay.LOGGER.error("Failed to save markers to replay", var3);
			}

		};
	}

	public GuiMarkerTimeline(Set<Marker> markers, Consumer<Set<Marker>> saveMarkers) {
		this.replayHandler = null;
		this.markers = markers;
		this.saveMarkers = saveMarkers;
	}

	protected GuiMarkerTimeline getThis() {
		return this;
	}

	public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
		this.lastSize = size;
		super.draw(renderer, size, renderInfo);
		this.drawMarkers(renderer, size);
	}

	protected void drawMarkers(GuiRenderer renderer, ReadableDimension size) {
		renderer.bindTexture(ReplayMod.TEXTURE);
		Iterator var3 = this.markers.iterator();

		while (var3.hasNext()) {
			Marker marker = (Marker) var3.next();
			this.drawMarker(renderer, size, marker);
		}

	}

	protected void drawMarker(GuiRenderer renderer, ReadableDimension size, Marker marker) {
		int visibleLength = (int) ((double) this.getLength() * this.getZoom());
		int markerPos = Utils.clamp(marker.getTime(), this.getOffset(), this.getOffset() + visibleLength);
		double positionInVisible = (double) (markerPos - this.getOffset());
		double fractionOfVisible = positionInVisible / (double) visibleLength;
		int markerX = (int) (4.0D + fractionOfVisible * (double) (size.getWidth() - 4 - 4));
		this.drawMarker(renderer, size, marker, markerX);
	}

	protected void drawMarker(GuiRenderer renderer, ReadableDimension size, Marker marker, int markerX) {
		byte textureX;
		byte textureY;
		if (marker.equals(this.selectedMarker)) {
			textureX = 114;
			textureY = 20;
		} else {
			textureX = 109;
			textureY = 20;
		}

		renderer.drawTexturedRect(markerX - 2, size.getHeight() - 3 - 5, textureX, textureY, 5, 5);
	}

	protected Marker getMarkerAt(int mouseX, int mouseY) {
		if (this.lastSize == null) {
			return null;
		} else {
			Point mouse = new Point(mouseX, mouseY);
			this.getContainer().convertFor(this, mouse);
			mouseX = mouse.getX();
			mouseY = mouse.getY();
			if (mouseX >= 0 && mouseY >= this.lastSize.getHeight() - 3 - 5 && mouseX <= this.lastSize.getWidth()
					&& mouseY <= this.lastSize.getHeight() - 3) {
				int visibleLength = (int) ((double) this.getLength() * this.getZoom());
				int contentWidth = this.lastSize.getWidth() - 4 - 4;
				Iterator var6 = this.markers.iterator();

				Marker marker;
				int markerX;
				do {
					if (!var6.hasNext()) {
						return null;
					}

					marker = (Marker) var6.next();
					int markerPos = Utils.clamp(marker.getTime(), this.getOffset(), this.getOffset() + visibleLength);
					double positionInVisible = (double) (markerPos - this.getOffset());
					double fractionOfVisible = positionInVisible / (double) visibleLength;
					markerX = (int) (4.0D + fractionOfVisible * (double) contentWidth);
				} while (Math.abs(markerX - mouseX) >= 3);

				return marker;
			} else {
				return null;
			}
		}
	}

	public boolean mouseClick(ReadablePoint position, int button) {
		Marker marker = this.getMarkerAt(position.getX(), position.getY());
		if (marker != null) {
			if (button == 0) {
				long now = System.currentTimeMillis();
				this.selectedMarker = marker;
				if (Math.abs(this.lastClickTime - now) > 500L) {
					this.draggingStartX = position.getX();
					this.draggingTimeDelta = marker.getTime() - this.getTimeAt(position.getX(), position.getY());
				} else {
					(new GuiEditMarkerPopup(this.getContainer(), marker, (updatedMarker) -> {
						this.markers.remove(marker);
						this.markers.add(updatedMarker);
						this.saveMarkers.accept(this.markers);
					})).open();
				}

				this.lastClickTime = now;
			} else if (button == 1) {
				this.selectedMarker = null;
				if (this.replayHandler != null) {
					CameraEntity cameraEntity = this.replayHandler.getCameraEntity();
					if (cameraEntity != null) {
						cameraEntity.setCameraPosRot(new Location(marker.getX(), marker.getY(), marker.getZ(),
								marker.getYaw(), marker.getPitch()));
					}

					this.replayHandler.doJump(marker.getTime(), true);
				} else {
					this.setCursorPosition(marker.getTime());
				}
			}

			return true;
		} else {
			this.selectedMarker = null;
			return super.mouseClick(position, button);
		}
	}

	public boolean mouseDrag(ReadablePoint position, int button, long timeSinceLastCall) {
		if (this.selectedMarker != null) {
			int diff = position.getX() - this.draggingStartX;
			if (Math.abs(diff) > 5) {
				this.dragging = true;
			}

			if (this.dragging) {
				int timeAt = this.getTimeAt(position.getX(), position.getY());
				if (timeAt != -1) {
					this.selectedMarker.setTime(this.draggingTimeDelta + timeAt);
				}

				return true;
			}
		}

		return false;
	}

	public boolean mouseRelease(ReadablePoint position, int button) {
		if (this.selectedMarker != null) {
			this.mouseDrag(position, button, 0L);
			if (this.dragging) {
				this.dragging = false;
				this.saveMarkers.accept(this.markers);
				return true;
			}
		}

		return false;
	}

	protected String getTooltipText(RenderInfo renderInfo) {
		Marker marker = this.getMarkerAt(renderInfo.mouseX, renderInfo.mouseY);
		if (marker != null) {
			return marker.getName() != null ? marker.getName()
					: I18n.get("replaymod.gui.ingame.unnamedmarker", new Object[0]);
		} else {
			return super.getTooltipText(renderInfo);
		}
	}

	public void setSelectedMarker(Marker selectedMarker) {
		this.selectedMarker = selectedMarker;
	}

	public Marker getSelectedMarker() {
		return this.selectedMarker;
	}

	public boolean typeKey(ReadablePoint mousePosition, int keyCode, char keyChar, boolean ctrlDown,
			boolean shiftDown) {
		if (keyCode == 261 && this.selectedMarker != null) {
			this.markers.remove(this.selectedMarker);
			this.saveMarkers.accept(this.markers);
			return true;
		} else {
			return false;
		}
	}

	public void addMarker(Marker marker) {
		this.markers.add(marker);
		this.saveMarkers.accept(this.markers);
	}

	public ReadableDimension getLastSize() {
		return super.getLastSize();
	}
}
