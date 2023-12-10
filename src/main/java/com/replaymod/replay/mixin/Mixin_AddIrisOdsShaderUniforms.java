package com.replaymod.replay.mixin;

import java.util.Objects;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.replaymod.render.capturer.IrisODSFrameCapturer;

import net.coderbot.iris.gl.uniform.UniformHolder;
import net.coderbot.iris.gl.uniform.UniformUpdateFrequency;
import net.coderbot.iris.uniforms.CommonUniforms;

@Pseudo
@Mixin(value = { CommonUniforms.class }, remap = false)
public class Mixin_AddIrisOdsShaderUniforms {
	@ModifyVariable(method = { "generalCommonUniforms" }, at = @At("HEAD"), argsOnly = true)
	private static UniformHolder addReplayModOdsUniforms(UniformHolder uniforms) {
		IrisODSFrameCapturer ods = IrisODSFrameCapturer.INSTANCE;
		if (ods != null) {
			UniformUpdateFrequency var10001 = UniformUpdateFrequency.PER_FRAME;
			Objects.requireNonNull(ods);
			uniforms.uniform1b(var10001, "leftEye", ods::isLeftEye);
			var10001 = UniformUpdateFrequency.PER_FRAME;
			Objects.requireNonNull(ods);
			uniforms.uniform1i(var10001, "direction", ods::getDirection);
		}

		return uniforms;
	}
}
