package com.replaymod.lib.de.javagl.jgltf.model.io;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

public interface GltfAsset {
	Object getGltf();

	ByteBuffer getBinaryData();

	List<GltfReference> getReferences();

	ByteBuffer getReferenceData(String string);

	Map<String, ByteBuffer> getReferenceDatas();
}
