package com.replaymod.lib.de.johni0702.minecraft.gui.element.advanced;

import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.RenderInfo;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.AbstractGuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Colors;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.MCVer;

public abstract class AbstractGuiTimelineTime<T extends AbstractGuiTimelineTime<T, U>, U extends AbstractGuiTimeline<U>>
		extends AbstractGuiElement<T> implements IGuiTimelineTime<T, U> {
	private U timeline;

	public AbstractGuiTimelineTime() {
	}

	public AbstractGuiTimelineTime(GuiContainer container) {
		super(container);
	}

	public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
		super.draw(renderer, size, renderInfo);
		if (this.timeline != null && this.timeline.getLastSize() != null) {
			int offset = (size.getWidth() - this.timeline.getLastSize().getWidth()) / 2;
			int visibleLength = (int) ((double) this.timeline.getLength() * this.timeline.getZoom());
			int markerInterval = this.timeline.getMarkerInterval();

			for (int time = this.timeline.getOffset() / markerInterval * markerInterval; time <= this.timeline
					.getOffset() + visibleLength; time += markerInterval) {
				if (time >= this.timeline.getOffset()) {
					this.drawTime(renderer, size, time, offset);
				}
			}

		}
	}

	protected void drawTime(GuiRenderer renderer, ReadableDimension size, int time, int offset) {
		int visibleLength = (int) ((double) this.timeline.getLength() * this.timeline.getZoom());
		double positionInVisible = (double) (time - this.timeline.getOffset());
		double fractionOfVisible = positionInVisible / (double) visibleLength;
		int positionX = (int) (4.0D + fractionOfVisible * (double) (size.getWidth() - 4 - 4)) + offset;
		String str;
		if (this.timeline.getLength() > 3600000) {
			str = String.format("%02d:%02d:%02d", time / 1000 / 60 / 60, time / 1000 / 60 % 60, time / 1000 % 60);
		} else {
			str = String.format("%02d:%02d", time / 1000 / 60, time / 1000 % 60);
		}

		int stringWidth = MCVer.getFontRenderer().width(str);
		positionX = Math.max(stringWidth / 2, Math.min(size.getWidth() - stringWidth / 2, positionX));
		renderer.drawCenteredString(positionX, 0, Colors.WHITE, str, true);
	}

	public ReadableDimension calcMinSize() {
		return new Dimension(0, 0);
	}

	public T setTimeline(U timeline) {
		this.timeline = timeline;
		return this.getThis();
	}

	public U getTimeline() {
		return this.timeline;
	}
}
