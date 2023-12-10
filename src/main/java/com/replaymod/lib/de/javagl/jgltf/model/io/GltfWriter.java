package com.replaymod.lib.de.javagl.jgltf.model.io;

import java.io.IOException;
import java.io.OutputStream;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public final class GltfWriter {
	private boolean indenting = true;

	public void setIndenting(boolean indenting) {
		this.indenting = indenting;
	}

	public boolean isIndenting() {
		return this.indenting;
	}

	public void write(Object gltf, OutputStream outputStream) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		if (this.indenting) {
			objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		}

		objectMapper.writeValue(outputStream, gltf);
	}
}
