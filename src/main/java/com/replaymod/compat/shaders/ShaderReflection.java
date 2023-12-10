package com.replaymod.compat.shaders;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;

public class ShaderReflection {
	public static Field shaders_frameTimeCounter;
	public static Field shaders_isShadowPass;
	public static Method shaders_beginRender;
	public static Field renderGlobal_chunksToUpdateForced;
	public static Method config_isShaders;

	static {
		try {
			Class shadersClass;
			try {
				shadersClass = Class.forName("shadersmod.client.Shaders");
			} catch (ClassNotFoundException var2) {
				shadersClass = Class.forName("net.optifine.shaders.Shaders");
			}

			shaders_frameTimeCounter = shadersClass.getDeclaredField("frameTimeCounter");
			shaders_frameTimeCounter.setAccessible(true);
			shaders_isShadowPass = shadersClass.getDeclaredField("isShadowPass");
			shaders_isShadowPass.setAccessible(true);
			shaders_beginRender = shadersClass.getDeclaredMethod("beginRender", Minecraft.class, Camera.class,
					Float.TYPE, Long.TYPE);
			shaders_beginRender.setAccessible(true);
			renderGlobal_chunksToUpdateForced = Class.forName("net.minecraft.client.renderer.RenderGlobal")
					.getDeclaredField("chunksToUpdateForced");
			renderGlobal_chunksToUpdateForced.setAccessible(true);
			config_isShaders = Class.forName("Config").getDeclaredMethod("isShaders");
			config_isShaders.setAccessible(true);
		} catch (ClassNotFoundException var3) {
		} catch (NoSuchFieldException | NoSuchMethodException var4) {
			var4.printStackTrace();
		}

	}
}
