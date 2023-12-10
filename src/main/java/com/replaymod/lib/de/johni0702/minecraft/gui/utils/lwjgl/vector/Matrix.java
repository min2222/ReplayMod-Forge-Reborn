package com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.vector;

import java.io.Serializable;
import java.nio.FloatBuffer;

public abstract class Matrix implements Serializable {
	protected Matrix() {
	}

	public abstract Matrix setIdentity();

	public abstract Matrix invert();

	public abstract Matrix load(FloatBuffer floatBuffer);

	public abstract Matrix loadTranspose(FloatBuffer floatBuffer);

	public abstract Matrix negate();

	public abstract Matrix store(FloatBuffer floatBuffer);

	public abstract Matrix storeTranspose(FloatBuffer floatBuffer);

	public abstract Matrix transpose();

	public abstract Matrix setZero();

	public abstract float determinant();
}
