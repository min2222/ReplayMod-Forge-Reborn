package com.replaymod.render.blend;

import java.io.IOException;
import java.nio.FloatBuffer;

import org.blender.dna.Link;
import org.blender.dna.ListBase;
import org.blender.utils.BlenderFactory;
import org.cakelab.blender.nio.CPointer;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlUtil;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.vector.Matrix3f;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.vector.Matrix4f;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.vector.Quaternion;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.vector.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public class Util {
	private static FloatBuffer floatBuffer = GlUtil.allocateMemory(64).asFloatBuffer();

	public static BlenderFactory factory() {
		return BlendState.getState().getFactory();
	}

	public static int align4(int size) {
		return align(size, 4);
	}

	public static int align(int size, int alignment) {
		int misalignment = size % alignment;
		return misalignment > 0 ? size + (alignment - misalignment) : size;
	}

	public static Matrix4f getGlMatrix(int matrix) {
		floatBuffer.clear();
		GL11.glGetFloatv(matrix, floatBuffer);
		floatBuffer.rewind();
		Matrix4f mat = new Matrix4f();
		mat.load(floatBuffer);
		return mat;
	}

	public static Matrix4f getGlModelViewMatrix() {
		return getGlMatrix(2982);
	}

	public static Matrix4f getGlTextureMatrix() {
		return getGlMatrix(2984);
	}

	public static boolean isGlTextureMatrixIdentity() {
		Matrix4f mat = getGlTextureMatrix();
		return mat.m00 == 1.0F && mat.m01 == 0.0F && mat.m02 == 0.0F && mat.m03 == 0.0F && mat.m10 == 0.0F
				&& mat.m11 == 1.0F && mat.m12 == 0.0F && mat.m13 == 0.0F && mat.m20 == 0.0F && mat.m21 == 0.0F
				&& mat.m22 == 1.0F && mat.m23 == 0.0F && mat.m30 == 0.0F && mat.m31 == 0.0F && mat.m32 == 0.0F
				&& mat.m33 == 1.0F;
	}

	public static Vector3f scaleFromMat(Matrix4f mat, Vector3f scale) {
		if (scale == null) {
			scale = new Vector3f();
		}

		scale.set((new Vector3f(mat.m00, mat.m01, mat.m02)).length(),
				(new Vector3f(mat.m10, mat.m11, mat.m12)).length(), (new Vector3f(mat.m20, mat.m21, mat.m22)).length());
		Matrix3f m3 = new Matrix3f();
		m3.m00 = mat.m00;
		m3.m01 = mat.m01;
		m3.m02 = mat.m02;
		m3.m10 = mat.m10;
		m3.m11 = mat.m11;
		m3.m12 = mat.m12;
		m3.m20 = mat.m20;
		m3.m21 = mat.m21;
		m3.m22 = mat.m22;
		if (m3.determinant() < 0.0F) {
			scale.x = -scale.x;
		}

		return scale;
	}

	public static void scaleMat3(Matrix4f mat, Vector3f scale) {
		mat.m00 /= scale.x;
		mat.m01 /= scale.x;
		mat.m02 /= scale.x;
		mat.m10 /= scale.y;
		mat.m11 /= scale.y;
		mat.m12 /= scale.y;
		mat.m20 /= scale.z;
		mat.m21 /= scale.z;
		mat.m22 /= scale.z;
	}

	public static Vector3f posFromMat(Matrix4f mat, Vector3f pos) {
		if (pos == null) {
			pos = new Vector3f();
		}

		pos.set(mat.m30, mat.m31, mat.m32);
		return pos;
	}

	public static Quaternion rotFromMat(Matrix4f mat, Quaternion rot) {
		Quaternion result = Quaternion.setFromMatrix(mat, rot);
		result.normalise();
		return result;
	}

	public static void glToBlend(Vector3f vec) {
		float tmp = vec.y;
		vec.y = -vec.z;
		vec.z = tmp;
	}

	public static void glScaleToBlend(Vector3f vec) {
		float tmp = vec.y;
		vec.y = vec.z;
		vec.z = tmp;
	}

	public static void glToBlend(Quaternion q) {
		float tmp = q.y;
		q.y = -q.z;
		q.z = tmp;
		q.w = -q.w;
	}

	public static Vector3f getCameraPos() {
		Minecraft mc = Minecraft.getInstance();
		Vec3 pos = mc.getEntityRenderDispatcher().camera.getPosition();
		return new Vector3f((float) pos.x, (float) pos.y, (float) pos.z);
	}

	public static Vector3f rotate(Quaternion rot, Vector3f vec, Vector3f dest) {
		if (dest == null) {
			dest = new Vector3f();
		}

		Quaternion vecQ = new Quaternion(vec.x, vec.y, vec.z, 0.0F);
		Quaternion.mul(rot, vecQ, vecQ);
		Quaternion.mulInverse(vecQ, rot, vecQ);
		dest.set(vecQ);
		return dest;
	}

	public static <T> CPointer<T> plus(CPointer<T> lhs, int rhs) throws IOException {
		while (rhs > 0) {
			lhs = lhs.plus(1);
			--rhs;
		}

		return lhs;
	}

	public static void insert(ListBase list, CPointer<Link> element) throws IOException {
		CPointer<Link> oldFirst = list.getFirst().cast(Link.class);
		if (oldFirst.isValid()) {
			((Link) oldFirst.get()).setPrev(element);
		}

		((Link) element.get()).setNext(oldFirst);
		list.setFirst(element.cast(Object.class));
		if (list.getLast().isNull()) {
			list.setLast(element.cast(Object.class));
		}

	}

	public static String getTileEntityId(BlockEntity tileEntity) {
		CompoundTag nbt = tileEntity.serializeNBT();
		return nbt.getString("id");
	}

	public interface IOFunction<T, R> {
		R apply(T object) throws IOException;
	}

	public interface IOBiConsumer<T, U> {
		void accept(T object, U object2) throws IOException;
	}

	public interface IOConsumer<T> {
		void accept(T object) throws IOException;
	}

	public interface IOCallable<R> {
		R call() throws IOException;
	}
}
