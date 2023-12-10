package com.replaymod.compat.optifine;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import net.minecraft.client.Options;

public class OptifineReflection {
	public static Field gameSettings_ofFastRender;

	public static void reloadLang() {
		try {
			Class langClass;
			try {
				langClass = Class.forName("Lang");
			} catch (ClassNotFoundException var2) {
				langClass = Class.forName("net.optifine.Lang");
			}

			langClass.getDeclaredMethod("resourcesReloaded").invoke((Object) null);
		} catch (ClassNotFoundException var3) {
		} catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException var4) {
			var4.printStackTrace();
		}

	}

	static {
		try {
			Class.forName("Config");
			gameSettings_ofFastRender = Options.class.getDeclaredField("ofFastRender");
			gameSettings_ofFastRender.setAccessible(true);
		} catch (ClassNotFoundException var1) {
		} catch (NoSuchFieldException var2) {
			var2.printStackTrace();
		}

	}
}
