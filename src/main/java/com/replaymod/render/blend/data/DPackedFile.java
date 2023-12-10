package com.replaymod.render.blend.data;

import java.io.IOException;

import org.blender.dna.PackedFile;
import org.cakelab.blender.nio.CPointer;

public class DPackedFile {
	public byte[] data;

	public DPackedFile() {
	}

	public DPackedFile(byte[] data) {
		this.data = data;
	}

	public CPointer<PackedFile> serialize(Serializer serializer) throws IOException {
		PackedFile packedFile = (PackedFile) serializer.writeData(PackedFile.class);
		packedFile.setSize(this.data.length);
		packedFile.setSeek(0);
		packedFile.setData(serializer.writeBytes(this.data).cast(Object.class));
		return packedFile.__io__addressof();
	}
}
