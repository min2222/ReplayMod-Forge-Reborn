package com.replaymod.render.metadata;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.FreeBox;
import com.coremedia.iso.boxes.MovieBox;
import com.coremedia.iso.boxes.TrackBox;
import com.coremedia.iso.boxes.UserBox;
import com.google.common.primitives.Bytes;
import com.googlecode.mp4parser.BasicContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.render.RenderSettings;
import com.replaymod.render.ReplayModRender;

public class MetadataInjector {
	private static final String STITCHING_SOFTWARE = "Minecraft ReplayMod";
	private static final String SPHERICAL_XML_HEADER = "<?xml version=\"1.0\"?> <rdf:SphericalVideo xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:GSpherical=\"http://ns.google.com/videos/1.0/spherical/\"> ";
	private static final String SPHERICAL_XML_CONTENTS = "<GSpherical:Spherical>true</GSpherical:Spherical> <GSpherical:Stitched>true</GSpherical:Stitched> <GSpherical:StitchingSoftware>Minecraft ReplayMod</GSpherical:StitchingSoftware> <GSpherical:ProjectionType>equirectangular</GSpherical:ProjectionType> ";
	private static final String SPHERICAL_CROP_XML = "<GSpherical:FullPanoWidthPixels>%d</GSpherical:FullPanoWidthPixels> <GSpherical:FullPanoHeightPixels>%d</GSpherical:FullPanoHeightPixels> <GSpherical:CroppedAreaImageWidthPixels>%d</GSpherical:CroppedAreaImageWidthPixels> <GSpherical:CroppedAreaImageHeightPixels>%d</GSpherical:CroppedAreaImageHeightPixels> <GSpherical:CroppedAreaLeftPixels>%d</GSpherical:CroppedAreaLeftPixels> <GSpherical:CroppedAreaTopPixels>%d</GSpherical:CroppedAreaTopPixels> ";
	private static final String STEREO_XML_CONTENTS = "<GSpherical:StereoMode>top-bottom</GSpherical:StereoMode>";
	private static final String SPHERICAL_XML_FOOTER = "</rdf:SphericalVideo>";
	private static final String XML_MONO_METADATA = "<?xml version=\"1.0\"?> <rdf:SphericalVideo xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:GSpherical=\"http://ns.google.com/videos/1.0/spherical/\"> <GSpherical:Spherical>true</GSpherical:Spherical> <GSpherical:Stitched>true</GSpherical:Stitched> <GSpherical:StitchingSoftware>Minecraft ReplayMod</GSpherical:StitchingSoftware> <GSpherical:ProjectionType>equirectangular</GSpherical:ProjectionType> <GSpherical:FullPanoWidthPixels>%d</GSpherical:FullPanoWidthPixels> <GSpherical:FullPanoHeightPixels>%d</GSpherical:FullPanoHeightPixels> <GSpherical:CroppedAreaImageWidthPixels>%d</GSpherical:CroppedAreaImageWidthPixels> <GSpherical:CroppedAreaImageHeightPixels>%d</GSpherical:CroppedAreaImageHeightPixels> <GSpherical:CroppedAreaLeftPixels>%d</GSpherical:CroppedAreaLeftPixels> <GSpherical:CroppedAreaTopPixels>%d</GSpherical:CroppedAreaTopPixels> </rdf:SphericalVideo>";
	private static final String XML_STEREO_METADATA = "<?xml version=\"1.0\"?> <rdf:SphericalVideo xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:GSpherical=\"http://ns.google.com/videos/1.0/spherical/\"> <GSpherical:Spherical>true</GSpherical:Spherical> <GSpherical:Stitched>true</GSpherical:Stitched> <GSpherical:StitchingSoftware>Minecraft ReplayMod</GSpherical:StitchingSoftware> <GSpherical:ProjectionType>equirectangular</GSpherical:ProjectionType> <GSpherical:FullPanoWidthPixels>%d</GSpherical:FullPanoWidthPixels> <GSpherical:FullPanoHeightPixels>%d</GSpherical:FullPanoHeightPixels> <GSpherical:CroppedAreaImageWidthPixels>%d</GSpherical:CroppedAreaImageWidthPixels> <GSpherical:CroppedAreaImageHeightPixels>%d</GSpherical:CroppedAreaImageHeightPixels> <GSpherical:CroppedAreaLeftPixels>%d</GSpherical:CroppedAreaLeftPixels> <GSpherical:CroppedAreaTopPixels>%d</GSpherical:CroppedAreaTopPixels> <GSpherical:StereoMode>top-bottom</GSpherical:StereoMode></rdf:SphericalVideo>";
	private static final byte[] UUID_BYTES = new byte[] { -1, -52, -126, 99, -8, 85, 74, -109, -120, 20, 88, 122, 2, 82,
			31, -35 };

	public static void injectMetadata(RenderSettings.RenderMethod renderMethod, File videoFile, int videoWidth,
			int videoHeight, int sphericalFovX, int sphericalFovY) {
		String xmlString;
		switch (renderMethod) {
		case EQUIRECTANGULAR:
			xmlString = "<?xml version=\"1.0\"?> <rdf:SphericalVideo xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:GSpherical=\"http://ns.google.com/videos/1.0/spherical/\"> <GSpherical:Spherical>true</GSpherical:Spherical> <GSpherical:Stitched>true</GSpherical:Stitched> <GSpherical:StitchingSoftware>Minecraft ReplayMod</GSpherical:StitchingSoftware> <GSpherical:ProjectionType>equirectangular</GSpherical:ProjectionType> <GSpherical:FullPanoWidthPixels>%d</GSpherical:FullPanoWidthPixels> <GSpherical:FullPanoHeightPixels>%d</GSpherical:FullPanoHeightPixels> <GSpherical:CroppedAreaImageWidthPixels>%d</GSpherical:CroppedAreaImageWidthPixels> <GSpherical:CroppedAreaImageHeightPixels>%d</GSpherical:CroppedAreaImageHeightPixels> <GSpherical:CroppedAreaLeftPixels>%d</GSpherical:CroppedAreaLeftPixels> <GSpherical:CroppedAreaTopPixels>%d</GSpherical:CroppedAreaTopPixels> </rdf:SphericalVideo>";
			break;
		case ODS:
			xmlString = "<?xml version=\"1.0\"?> <rdf:SphericalVideo xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:GSpherical=\"http://ns.google.com/videos/1.0/spherical/\"> <GSpherical:Spherical>true</GSpherical:Spherical> <GSpherical:Stitched>true</GSpherical:Stitched> <GSpherical:StitchingSoftware>Minecraft ReplayMod</GSpherical:StitchingSoftware> <GSpherical:ProjectionType>equirectangular</GSpherical:ProjectionType> <GSpherical:FullPanoWidthPixels>%d</GSpherical:FullPanoWidthPixels> <GSpherical:FullPanoHeightPixels>%d</GSpherical:FullPanoHeightPixels> <GSpherical:CroppedAreaImageWidthPixels>%d</GSpherical:CroppedAreaImageWidthPixels> <GSpherical:CroppedAreaImageHeightPixels>%d</GSpherical:CroppedAreaImageHeightPixels> <GSpherical:CroppedAreaLeftPixels>%d</GSpherical:CroppedAreaLeftPixels> <GSpherical:CroppedAreaTopPixels>%d</GSpherical:CroppedAreaTopPixels> <GSpherical:StereoMode>top-bottom</GSpherical:StereoMode></rdf:SphericalVideo>";
			break;
		default:
			throw new IllegalArgumentException("Invalid render method");
		}

		Dimension original = getOriginalDimensions(videoWidth, videoHeight, sphericalFovX, sphericalFovY);
		writeMetadata(videoFile, String.format(xmlString, original.getWidth(), original.getHeight(), videoWidth,
				videoHeight, (original.getWidth() - videoWidth) / 2, (original.getHeight() - videoHeight) / 2));
	}

	private static Dimension getOriginalDimensions(int videoWidth, int videoHeight, int sphericalFovX,
			int sphericalFovY) {
		if (sphericalFovX < 360) {
			videoWidth = Math.round((float) (videoWidth * 360) / (float) sphericalFovX);
		}

		if (sphericalFovY < 180) {
			videoHeight = Math.round((float) (videoHeight * 180) / (float) sphericalFovY);
		}

		return new Dimension(videoWidth, videoHeight);
	}

	private static void writeMetadata(File videoFile, String metadata) {
		byte[] bytes = Bytes.concat(new byte[][] { UUID_BYTES, metadata.getBytes() });
		File tempFile = null;
		FileOutputStream videoFileOutputStream = null;
		IsoFile tempIsoFile = null;

		try {
			tempFile = File.createTempFile("videoCopy", "mp4");
			FileUtils.copyFile(videoFile, tempFile);
			tempIsoFile = new IsoFile(tempFile.getAbsolutePath());
			MovieBox moovBox = (MovieBox) getBoxByName(tempIsoFile, "moov");
			if (moovBox == null) {
				throw new IOException("Could not find moov box inside IsoFile");
			}

			TrackBox trackBox = (TrackBox) getBoxByName(moovBox, "trak");
			if (trackBox == null) {
				throw new IOException("Could not find trak box inside moov box");
			}

			UserBox metadataBox = new UserBox(new byte[0]);
			metadataBox.setData(bytes);
			trackBox.addBox(metadataBox);
			FreeBox freeBox = (FreeBox) getBoxByName(tempIsoFile, "free");
			if (freeBox == null) {
				throw new IOException("Could not find free box inside IsoFile");
			}

			int freeSize = Math.max(0, freeBox.getData().capacity() - (int) metadataBox.getSize());
			freeBox.setData(ByteBuffer.allocate(freeSize));
			videoFileOutputStream = new FileOutputStream(videoFile);
			tempIsoFile.getBox(videoFileOutputStream.getChannel());
		} catch (Exception var14) {
			ReplayModRender.LOGGER.error("Spherical Metadata couldn't be injected", var14);
		} finally {
			IOUtils.closeQuietly(tempIsoFile);
			IOUtils.closeQuietly(videoFileOutputStream);
			FileUtils.deleteQuietly(tempFile);
		}

	}

	private static Box getBoxByName(BasicContainer container, String boxName) {
		for (Box box : container.getBoxes()) {
			if (box.getType().equals(boxName))
				return box;
		}
		return null;
	}
}
