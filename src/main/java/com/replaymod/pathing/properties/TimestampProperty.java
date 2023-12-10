package com.replaymod.pathing.properties;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.replaymod.replay.ReplayHandler;
import com.replaymod.replay.ReplaySender;
import com.replaymod.replaystudio.pathing.property.AbstractProperty;
import com.replaymod.replaystudio.pathing.property.PropertyGroup;
import com.replaymod.replaystudio.pathing.property.PropertyPart;
import com.replaymod.replaystudio.pathing.property.PropertyParts;

public class TimestampProperty extends AbstractProperty<Integer> {
	public static final TimestampProperty PROPERTY = new TimestampProperty();
	public final PropertyPart<Integer> TIME = new PropertyParts.ForInteger(this, true);

	private TimestampProperty() {
		super("timestamp", "replaymod.gui.editkeyframe.timestamp", (PropertyGroup) null, 0);
	}

	public Collection<PropertyPart<Integer>> getParts() {
		return Collections.singletonList(this.TIME);
	}

	public void applyToGame(Integer value, @NonNull Object replayHandler) {
		ReplaySender replaySender = ((ReplayHandler) replayHandler).getReplaySender();
		if (replaySender.isAsyncMode()) {
			replaySender.jumpToTime(value);
		} else {
			replaySender.sendPacketsTill(value);
		}

	}

	public void toJson(JsonWriter writer, Integer value) throws IOException {
		writer.value(value);
	}

	public Integer fromJson(JsonReader reader) throws IOException {
		return reader.nextInt();
	}
}
