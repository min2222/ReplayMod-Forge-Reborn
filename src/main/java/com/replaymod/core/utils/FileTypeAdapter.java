package com.replaymod.core.utils;

import java.io.File;
import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class FileTypeAdapter extends TypeAdapter<File> {
	public void write(JsonWriter out, File value) throws IOException {
		out.value(value.getPath());
	}

	public File read(JsonReader in) throws IOException {
		String path;
		if (in.peek() == JsonToken.BEGIN_OBJECT) {
			in.beginObject();
			in.nextName();
			path = in.nextString();
			in.endObject();
		} else {
			path = in.nextString();
		}

		return new File(path);
	}
}
