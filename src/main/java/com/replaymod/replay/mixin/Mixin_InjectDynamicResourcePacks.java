package com.replaymod.replay.mixin;

import java.util.function.BiConsumer;
import java.util.stream.Collector;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.replaymod.core.ReplayMod;
import com.replaymod.core.versions.LangResourcePack;

import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.PackRepository;

@Mixin({ PackRepository.class })
public class Mixin_InjectDynamicResourcePacks {
	@ModifyArg(method = {
			"openAllSelected" }, at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;collect(Ljava/util/stream/Collector;)Ljava/lang/Object;"))
	private Collector<PackResources, ?, ?> injectReplayModPacks(Collector<PackResources, ?, ?> collector) {
		collector = append(collector, new LangResourcePack());
		if (ReplayMod.jGuiResourcePack != null) {

			collector = append(collector, ReplayMod.jGuiResourcePack);
		}

		return collector;
	}

	private static <T, A, R> Collector<T, A, R> append(Collector<T, A, R> collector, T value) {
		BiConsumer<A, T> accumulator = collector.accumulator();
		return Collector.of(collector.supplier(), accumulator, collector.combiner(), (result) -> {
			accumulator.accept(result, value);
			return collector.finisher().apply(result);
		});
	}
}
