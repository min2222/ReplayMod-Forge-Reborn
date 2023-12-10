package com.replaymod.render;

import static com.replaymod.core.utils.Utils.configure;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;

import org.apache.commons.io.FilenameUtils;
import org.lwjgl.opengl.GL11;

import com.replaymod.core.ReplayMod;
import com.replaymod.core.versions.MCVer;
import com.replaymod.lib.de.javagl.jgltf.impl.v2.Accessor;
import com.replaymod.lib.de.javagl.jgltf.impl.v2.Animation;
import com.replaymod.lib.de.javagl.jgltf.impl.v2.AnimationChannel;
import com.replaymod.lib.de.javagl.jgltf.impl.v2.AnimationChannelTarget;
import com.replaymod.lib.de.javagl.jgltf.impl.v2.AnimationSampler;
import com.replaymod.lib.de.javagl.jgltf.impl.v2.Asset;
import com.replaymod.lib.de.javagl.jgltf.impl.v2.Buffer;
import com.replaymod.lib.de.javagl.jgltf.impl.v2.BufferView;
import com.replaymod.lib.de.javagl.jgltf.impl.v2.Camera;
import com.replaymod.lib.de.javagl.jgltf.impl.v2.CameraPerspective;
import com.replaymod.lib.de.javagl.jgltf.impl.v2.GlTF;
import com.replaymod.lib.de.javagl.jgltf.impl.v2.Node;
import com.replaymod.lib.de.javagl.jgltf.model.io.v2.GltfAssetV2;
import com.replaymod.lib.de.javagl.jgltf.model.io.v2.GltfAssetWriterV2;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.vector.Quaternion;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.vector.Vector4f;
import com.replaymod.replay.camera.CameraEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class CameraPathExporter {
	private final Minecraft mc = MCVer.getMinecraft();
	private final RenderSettings settings;
	private int framesDone;
	private ByteBuffer timeBuffer;
	private ByteBuffer cameraTranslationBuffer;
	private ByteBuffer cameraRotationBuffer;

	public CameraPathExporter(RenderSettings settings) {
		this.settings = settings;
	}

	public void setup(int totalFrames) {
		this.timeBuffer = ByteBuffer.allocate(4 * totalFrames).order(ByteOrder.LITTLE_ENDIAN);
		this.cameraTranslationBuffer = ByteBuffer.allocate(4 * totalFrames * 3).order(ByteOrder.LITTLE_ENDIAN);
		this.cameraRotationBuffer = ByteBuffer.allocate(4 * totalFrames * 4).order(ByteOrder.LITTLE_ENDIAN);
	}

	public void recordFrame(float tickDelta) {
		Entity entity = this.mc.getCameraEntity() == null ? this.mc.player : this.mc.getCameraEntity();
		net.minecraft.client.Camera camera = this.mc.gameRenderer.getMainCamera();
		Vec3 vec = camera.getPosition();
		float x = (float) vec.x();
		float y = (float) vec.y();
		float z = (float) vec.z();
		float yaw = camera.getYRot() + 180.0F;
		float pitch = camera.getXRot();
		float roll = entity instanceof CameraEntity ? ((CameraEntity) entity).roll : 0.0F;
		Quaternion quatYaw = new Quaternion();
		Quaternion quatPitch = new Quaternion();
		Quaternion quatRoll = new Quaternion();
		quatYaw.setFromAxisAngle(new Vector4f(0.0F, -1.0F, 0.0F, (float) Math.toRadians((double) yaw)));
		quatPitch.setFromAxisAngle(new Vector4f(-1.0F, 0.0F, 0.0F, (float) Math.toRadians((double) pitch)));
		quatRoll.setFromAxisAngle(new Vector4f(0.0F, 0.0F, -1.0F, (float) Math.toRadians((double) roll)));
		Quaternion quaternion = new Quaternion(0.0F, 0.0F, 0.0F, 1.0F);
		Quaternion.mul(quaternion, quatYaw, quaternion);
		Quaternion.mul(quaternion, quatPitch, quaternion);
		Quaternion.mul(quaternion, quatRoll, quaternion);
		quaternion.normalise(quaternion);
		float[] translation = new float[] { x, y, z };
		float[] rotation = new float[] { quaternion.getX(), quaternion.getY(), quaternion.getZ(), quaternion.getW() };
		this.timeBuffer.putFloat((float) this.framesDone / (float) this.settings.getFramesPerSecond());
		float[] var17 = translation;
		int var18 = translation.length;

		int var19;
		float f;
		for (var19 = 0; var19 < var18; ++var19) {
			f = var17[var19];
			this.cameraTranslationBuffer.putFloat(f);
		}

		var17 = rotation;
		var18 = rotation.length;

		for (var19 = 0; var19 < var18; ++var19) {
			f = var17[var19];
			this.cameraRotationBuffer.putFloat(f);
		}

		++this.framesDone;
	}

	public void finish() throws IOException {
		int timeBufferSize = timeBuffer.rewind().remaining();
		int cameraTranslationBufferSize = cameraTranslationBuffer.rewind().remaining();
		int cameraRotationBufferSize = cameraRotationBuffer.rewind().remaining();

		int binaryDataSize = 0;
		binaryDataSize += timeBufferSize;
		binaryDataSize += cameraTranslationBufferSize;
		binaryDataSize += cameraRotationBufferSize;

		ByteBuffer binaryData = ByteBuffer.allocate(binaryDataSize);
		int timeBufferOffset = binaryData.position();
		binaryData.put(timeBuffer);
		int cameraTranslationBufferOffset = binaryData.position();
		binaryData.put(cameraTranslationBuffer);
		int cameraRotationBufferOffset = binaryData.position();
		binaryData.put(cameraRotationBuffer);
		binaryData.rewind();

		GlTF glTF = new GlTF();
		glTF.setAsset(configure(new Asset(), asset -> {
			asset.setVersion("2.0");
			asset.setGenerator("ReplayMod v" + ReplayMod.instance.getVersion());
		}));
		glTF.addAnimations(configure(new Animation(), animation -> {
			animation.addChannels(configure(new AnimationChannel(), channel -> {
				channel.setTarget(configure(new AnimationChannelTarget(), target -> {
					target.setNode(0);
					target.setPath("translation");
				}));
				channel.setSampler(0);
			}));
			animation.addChannels(configure(new AnimationChannel(), channel -> {
				channel.setTarget(configure(new AnimationChannelTarget(), target -> {
					target.setNode(0);
					target.setPath("rotation");
				}));
				channel.setSampler(1);
			}));
			animation.addSamplers(configure(new AnimationSampler(), sampler -> {
				sampler.setInput(0);
				sampler.setOutput(1);
			}));
			animation.addSamplers(configure(new AnimationSampler(), sampler -> {
				sampler.setInput(0);
				sampler.setOutput(2);
			}));
		}));
		glTF.addCameras(configure(new Camera(), camera -> {
			camera.setType("perspective");
			camera.setPerspective(configure(new CameraPerspective(), perspective -> {
				float aspectRatio = (float) settings.getVideoWidth() / (float) settings.getVideoHeight();
				perspective.setAspectRatio(aspectRatio);
				perspective.setYfov((float) Math.toRadians(mc.options.fov().get()));
				perspective.setZnear(0.05f);
				perspective.setZfar((float) mc.options.renderDistance().get() * 16 * 4);
			}));
		}));
		glTF.addNodes(configure(new Node(), node -> node.setCamera(0)));
		glTF.addBuffers(configure(new Buffer(), buffer -> buffer.setByteLength(binaryData.limit())));
		// Time
		glTF.addBufferViews(configure(new BufferView(), bufferView -> {
			bufferView.setBuffer(0);
			bufferView.setByteOffset(timeBufferOffset);
			bufferView.setByteLength(timeBufferSize);
		}));
		glTF.addAccessors(configure(new Accessor(), accessor -> {
			accessor.setBufferView(0);
			accessor.setType("SCALAR");
			accessor.setComponentType(GL11.GL_FLOAT);
			accessor.setCount(framesDone);
		}));
		// Camera translation
		glTF.addBufferViews(configure(new BufferView(), bufferView -> {
			bufferView.setBuffer(0);
			bufferView.setByteOffset(cameraTranslationBufferOffset);
			bufferView.setByteLength(cameraTranslationBufferSize);
		}));
		glTF.addAccessors(configure(new Accessor(), accessor -> {
			accessor.setBufferView(1);
			accessor.setType("VEC3");
			accessor.setComponentType(GL11.GL_FLOAT);
			accessor.setCount(framesDone);
		}));
		// Camera rotation
		glTF.addBufferViews(configure(new BufferView(), bufferView -> {
			bufferView.setBuffer(0);
			bufferView.setByteOffset(cameraRotationBufferOffset);
			bufferView.setByteLength(cameraRotationBufferSize);
		}));
		glTF.addAccessors(configure(new Accessor(), accessor -> {
			accessor.setBufferView(2);
			accessor.setType("VEC4");
			accessor.setComponentType(GL11.GL_FLOAT);
			accessor.setCount(framesDone);
		}));

		java.nio.file.Path videoPath = settings.getOutputFile().toPath();
		java.nio.file.Path glbBasePath = Files.isDirectory(videoPath) ? videoPath.resolve("camera.glb")
				: videoPath.resolveSibling(FilenameUtils.getBaseName(videoPath.getFileName().toString()) + ".glb");
		java.nio.file.Path glbPath = glbBasePath;
		for (int i = 0; Files.exists(glbPath); i++) {
			String baseName = FilenameUtils.getBaseName(glbBasePath.getFileName().toString());
			glbPath = glbBasePath.resolveSibling(baseName + "." + i + ".glb");
		}
		try (OutputStream out = Files.newOutputStream(glbPath)) {
			GltfAssetV2 asset = new GltfAssetV2(glTF, binaryData);
			new GltfAssetWriterV2().writeBinary(asset, out);
		}
	}
}
