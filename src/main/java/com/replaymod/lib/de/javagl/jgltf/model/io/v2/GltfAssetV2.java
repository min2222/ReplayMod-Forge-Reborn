package com.replaymod.lib.de.javagl.jgltf.model.io.v2;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import com.replaymod.lib.de.javagl.jgltf.impl.v2.Buffer;
import com.replaymod.lib.de.javagl.jgltf.impl.v2.GlTF;
import com.replaymod.lib.de.javagl.jgltf.impl.v2.Image;
import com.replaymod.lib.de.javagl.jgltf.model.Optionals;
import com.replaymod.lib.de.javagl.jgltf.model.io.Buffers;
import com.replaymod.lib.de.javagl.jgltf.model.io.GltfAsset;
import com.replaymod.lib.de.javagl.jgltf.model.io.GltfReference;
import com.replaymod.lib.de.javagl.jgltf.model.io.IO;

public final class GltfAssetV2 implements GltfAsset {
	private final GlTF gltf;
	private final ByteBuffer binaryData;
	private final Map<String, ByteBuffer> referenceDatas;

	public GltfAssetV2(GlTF gltf, ByteBuffer binaryData) {
		this.gltf = (GlTF) Objects.requireNonNull(gltf, "The gltf may not be null");
		this.binaryData = binaryData;
		this.referenceDatas = new ConcurrentHashMap();
	}

	void putReferenceData(String uriString, ByteBuffer byteBuffer) {
		if (byteBuffer == null) {
			this.referenceDatas.remove(uriString);
		} else {
			this.referenceDatas.put(uriString, byteBuffer);
		}

	}

	public GlTF getGltf() {
		return this.gltf;
	}

	public ByteBuffer getBinaryData() {
		return Buffers.createSlice(this.binaryData);
	}

	public List<GltfReference> getReferences() {
		List<GltfReference> references = new ArrayList();
		references.addAll(this.getBufferReferences());
		references.addAll(this.getImageReferences());
		return references;
	}

	public List<GltfReference> getBufferReferences() {
		List<GltfReference> references = new ArrayList();
		List<Buffer> buffers = Optionals.of(this.gltf.getBuffers());

		for (int i = 0; i < buffers.size(); ++i) {
			Buffer buffer = (Buffer) buffers.get(i);
			if (buffer.getUri() != null) {
				String uri = buffer.getUri();
				if (!IO.isDataUriString(uri)) {
					Consumer<ByteBuffer> target = (byteBuffer) -> {
						this.putReferenceData(uri, byteBuffer);
					};
					GltfReference reference = new GltfReference("buffer " + i, uri, target);
					references.add(reference);
				}
			}
		}

		return references;
	}

	public List<GltfReference> getImageReferences() {
		List<GltfReference> references = new ArrayList();
		List<Image> images = Optionals.of(this.gltf.getImages());

		for (int i = 0; i < images.size(); ++i) {
			Image image = (Image) images.get(i);
			if (image.getBufferView() == null) {
				String uri = image.getUri();
				if (!IO.isDataUriString(uri)) {
					Consumer<ByteBuffer> target = (byteBuffer) -> {
						this.putReferenceData(uri, byteBuffer);
					};
					GltfReference reference = new GltfReference("image " + i, uri, target);
					references.add(reference);
				}
			}
		}

		return references;
	}

	public ByteBuffer getReferenceData(String uriString) {
		return Buffers.createSlice((ByteBuffer) this.referenceDatas.get(uriString));
	}

	public Map<String, ByteBuffer> getReferenceDatas() {
		return Collections.unmodifiableMap(this.referenceDatas);
	}
}
