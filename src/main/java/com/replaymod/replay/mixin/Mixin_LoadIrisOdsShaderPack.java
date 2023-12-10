package com.replaymod.replay.mixin;

import java.nio.file.Path;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.replaymod.render.capturer.IrisODSFrameCapturer;

import net.coderbot.iris.Iris;
import net.minecraftforge.fml.ModList;

@Pseudo
@Mixin(value = { Iris.class }, remap = false)
public class Mixin_LoadIrisOdsShaderPack {
	@Redirect(method = {
			"loadExternalShaderpack" }, at = @At(value = "INVOKE", target = "Lnet/coderbot/iris/Iris;getShaderpacksDirectory()Ljava/nio/file/Path;"))
	private static Path loadReplayModOdsPack(String name) {
		return IrisODSFrameCapturer.INSTANCE != null && "assets/replaymod/iris/ods".equals(name)
				? ModList.get().getModContainerById("replaymod").orElseThrow(() -> {
					return new RuntimeException("Failed to get mod container for ReplayMod");
				}).getModInfo().getOwningFile().getFile().getFilePath()
				: Iris.getShaderpacksDirectory();
	}
}
