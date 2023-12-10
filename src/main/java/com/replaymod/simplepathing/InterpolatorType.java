package com.replaymod.simplepathing;

import java.util.function.Supplier;

import com.replaymod.replaystudio.pathing.interpolation.CatmullRomSplineInterpolator;
import com.replaymod.replaystudio.pathing.interpolation.CubicSplineInterpolator;
import com.replaymod.replaystudio.pathing.interpolation.Interpolator;
import com.replaymod.replaystudio.pathing.interpolation.LinearInterpolator;

public enum InterpolatorType {
	DEFAULT("default", (Class) null, (Supplier) null),
	CATMULL_ROM("catmullrom", CatmullRomSplineInterpolator.class, () -> {
		return new CatmullRomSplineInterpolator(0.5D);
	}), CUBIC("cubic", CubicSplineInterpolator.class, CubicSplineInterpolator::new),
	LINEAR("linear", LinearInterpolator.class, LinearInterpolator::new);

	private String localizationKey;
	private Class<? extends Interpolator> interpolatorClass;
	private Supplier<Interpolator> interpolatorConstructor;

	private InterpolatorType(String localizationKey, Class<? extends Interpolator> interpolatorClass,
			Supplier<Interpolator> interpolatorConstructor) {
		this.localizationKey = localizationKey;
		this.interpolatorClass = interpolatorClass;
		this.interpolatorConstructor = interpolatorConstructor;
	}

	public String getLocalizationKey() {
		return this.localizationKey;
	}

	public String getI18nName() {
		return String.format("replaymod.gui.editkeyframe.interpolator.%1$s.name", this.localizationKey);
	}

	public String getI18nDescription() {
		return String.format("replaymod.gui.editkeyframe.interpolator.%1$s.desc", this.localizationKey);
	}

	public Class<? extends Interpolator> getInterpolatorClass() {
		return this.interpolatorClass;
	}

	public static InterpolatorType fromString(String string) {
		InterpolatorType[] var1 = values();
		int var2 = var1.length;

		for (int var3 = 0; var3 < var2; ++var3) {
			InterpolatorType t = var1[var3];
			if (t.getI18nName().equals(string)) {
				return t;
			}
		}

		return CATMULL_ROM;
	}

	public static InterpolatorType fromClass(Class<? extends Interpolator> cls) {
		InterpolatorType[] var1 = values();
		int var2 = var1.length;

		for (int var3 = 0; var3 < var2; ++var3) {
			InterpolatorType type = var1[var3];
			if (cls.equals(type.getInterpolatorClass())) {
				return type;
			}
		}

		return DEFAULT;
	}

	public Interpolator newInstance() {
		return (Interpolator) this.interpolatorConstructor.get();
	}

	// $FF: synthetic method
	private static InterpolatorType[] $values() {
		return new InterpolatorType[] { DEFAULT, CATMULL_ROM, CUBIC, LINEAR };
	}
}
