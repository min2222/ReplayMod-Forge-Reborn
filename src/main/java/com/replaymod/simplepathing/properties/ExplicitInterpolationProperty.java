package com.replaymod.simplepathing.properties;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang3.ObjectUtils;
import org.checkerframework.checker.nullness.qual.NonNull;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.replaymod.replaystudio.pathing.property.AbstractProperty;
import com.replaymod.replaystudio.pathing.property.PropertyGroup;
import com.replaymod.replaystudio.pathing.property.PropertyPart;

public class ExplicitInterpolationProperty extends AbstractProperty<Object> {
	public static final ExplicitInterpolationProperty PROPERTY = new ExplicitInterpolationProperty();

	private ExplicitInterpolationProperty() {
		super("interpolationFixed", "<internal>", (PropertyGroup) null, new Object());
	}

	public Collection<PropertyPart<Object>> getParts() {
		return Collections.emptyList();
	}

	public void applyToGame(Object value, @NonNull Object replayHandler) {
	}

	public void toJson(JsonWriter writer, Object value) throws IOException {
		writer.nullValue();
	}

	public Object fromJson(JsonReader reader) throws IOException {
		reader.nextNull();
		return ObjectUtils.NULL;
	}
}
