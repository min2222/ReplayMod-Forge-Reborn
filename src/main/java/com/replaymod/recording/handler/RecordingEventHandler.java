package com.replaymod.recording.handler;

import static com.replaymod.core.versions.MCVer.getMinecraft;

import java.util.Collections;
import java.util.Objects;
import java.util.Random;

import com.mojang.datafixers.util.Pair;
import com.replaymod.core.events.PreRenderCallback;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.EventRegistrations;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.callbacks.PreTickCallback;
import com.replaymod.recording.packet.PacketListener;
import com.replaymod.replay.mixin.EntityLivingBaseAccessorRecording;
import com.replaymod.replay.mixin.IntegratedServerAccessor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddPlayerPacket;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent.ItemPickupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class RecordingEventHandler extends EventRegistrations {

	private final Minecraft mc = getMinecraft();
	private final PacketListener packetListener;

	private Double lastX, lastY, lastZ;
	private ItemStack[] playerItems = new ItemStack[6];
	private int ticksSinceLastCorrection;
	private boolean wasSleeping;
	private int lastRiding = -1;
	private Integer rotationYawHeadBefore;
	private boolean wasHandActive;
	private InteractionHand lastActiveHand;

	public RecordingEventHandler(PacketListener packetListener) {
		this.packetListener = packetListener;
	}

	@Override
	public void register() {
		super.register();
		((RecordingEventSender) mc.levelRenderer).setRecordingEventHandler(this);
	}

	@Override
	public void unregister() {
		super.unregister();
		RecordingEventSender recordingEventSender = ((RecordingEventSender) mc.levelRenderer);
		if (recordingEventSender.getRecordingEventHandler() == this) {
			recordingEventSender.setRecordingEventHandler(null);
		}
	}

	public void onPacket(Packet<?> packet) {
		packetListener.save(packet);
	}

	public void spawnRecordingPlayer() {
		try {
			LocalPlayer player = mc.player;
			assert player != null;
			packetListener.save(new ClientboundAddPlayerPacket(player));
			packetListener.save(new ClientboundSetEntityDataPacket(player.getId(), player.getEntityData(), true));
			lastX = lastY = lastZ = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void onClientSound(SoundEvent sound, SoundSource category, double x, double y, double z, float volume,
			float pitch) {
		try {
			// Send to all other players in ServerWorldEventHandler#playSoundToAllNearExcept
			Random random = new Random();
			packetListener.save(new ClientboundSoundPacket(sound, category, x, y, z, volume, pitch, random.nextLong()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void onClientEffect(int type, BlockPos pos, int data) {
		try {
			// Send to all other players in ServerWorldEventHandler#playEvent
			packetListener.save(new ClientboundLevelEventPacket(type, pos, data, false));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	{
		on(PreTickCallback.EVENT, this::onPlayerTick);
	}

	private void onPlayerTick() {
		if (mc.player == null)
			return;
		LocalPlayer player = mc.player;
		try {

			boolean force = false;
			if (lastX == null || lastY == null || lastZ == null) {
				force = true;
				lastX = player.getX();
				lastY = player.getY();
				lastZ = player.getZ();
			}

			ticksSinceLastCorrection++;
			if (ticksSinceLastCorrection >= 100) {
				ticksSinceLastCorrection = 0;
				force = true;
			}

			double dx = player.getX() - lastX;
			double dy = player.getY() - lastY;
			double dz = player.getZ() - lastZ;

			lastX = player.getX();
			lastY = player.getY();
			lastZ = player.getZ();
			Packet packet;
			if (force || Math.abs(dx) > 8.0 || Math.abs(dy) > 8.0 || Math.abs(dz) > 8.0) {
				packet = new ClientboundTeleportEntityPacket(player);
			} else {
				byte newYaw = (byte) ((int) (player.getYRot() * 256.0F / 360.0F));
				byte newPitch = (byte) ((int) (player.getXRot() * 256.0F / 360.0F));

				packet = new ClientboundMoveEntityPacket.PosRot(player.getId(), (short) Math.round(dx * 4096),
						(short) Math.round(dy * 4096), (short) Math.round(dz * 4096), newYaw, newPitch,
						player.isOnGround());
			}

			packetListener.save(packet);

			// HEAD POS
			int rotationYawHead = ((int) (player.getYHeadRot() * 256.0F / 360.0F));

			if (!Objects.equals(rotationYawHead, rotationYawHeadBefore)) {
				packetListener.save(new ClientboundRotateHeadPacket(player, (byte) rotationYawHead));
				rotationYawHeadBefore = rotationYawHead;
			}

			packetListener.save(new ClientboundSetEntityMotionPacket(player.getId(), player.getDeltaMovement()));

			// Animation Packets
			// Swing Animation
			if (player.swinging && player.swingTime == 0) {
				packetListener.save(
						new ClientboundAnimatePacket(player, player.swingingArm == InteractionHand.MAIN_HAND ? 0 : 3));
			}

			/*
			 * //Potion Effect Handling List<Integer> found = new ArrayList<Integer>();
			 * for(PotionEffect pe :
			 * (Collection<PotionEffect>)player.getActivePotionEffects()) {
			 * found.add(pe.getPotionID()); if(lastEffects.contains(found)) continue;
			 * S1DPacketEntityEffect pee = new S1DPacketEntityEffect(entityID, pe);
			 * packetListener.save(pee); }
			 * 
			 * for(int id : lastEffects) { if(!found.contains(id)) {
			 * S1EPacketRemoveEntityEffect pre = new S1EPacketRemoveEntityEffect(entityID,
			 * new PotionEffect(id, 0)); packetListener.save(pre); } }
			 * 
			 * lastEffects = found;
			 */

			// Inventory Handling
			for (EquipmentSlot slot : EquipmentSlot.values()) {
				ItemStack stack = player.getItemBySlot(slot);
				if (playerItems[slot.ordinal()] != stack) {
					playerItems[slot.ordinal()] = stack;
					packetListener.save(new ClientboundSetEquipmentPacket(player.getId(),
							Collections.singletonList(Pair.of(slot, stack))));
				}
			}

			// Leaving Ride

			Entity vehicle = player.getVehicle();
			int vehicleId = vehicle == null ? -1 : vehicle.getId();
			if (lastRiding != vehicleId) {
				lastRiding = vehicleId;
				packetListener.save(new ClientboundSetEntityLinkPacket(player, vehicle));
			}

			// Sleeping
			if (!player.isSleeping() && wasSleeping) {
				packetListener.save(new ClientboundAnimatePacket(player, 2));
				wasSleeping = false;
			}

			// Active hand (e.g. eating, drinking, blocking)
			if (player.isUsingItem() ^ wasHandActive || player.getUsedItemHand() != lastActiveHand) {
				wasHandActive = player.isUsingItem();
				lastActiveHand = player.getUsedItemHand();
				SynchedEntityData dataManager = new SynchedEntityData(null);
				int state = (wasHandActive ? 1 : 0) | (lastActiveHand == InteractionHand.OFF_HAND ? 2 : 0);
				dataManager.define(EntityLivingBaseAccessorRecording.getLivingFlags(), (byte) state);
				packetListener.save(new ClientboundSetEntityDataPacket(player.getId(), dataManager, true));
			}

		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	@SubscribeEvent
	public void onPickupItem(ItemPickupEvent event) {
		try {
			ItemStack stack = event.getStack();
			packetListener.save(new ClientboundTakeItemEntityPacket(event.getOriginalEntity().getId(),
					event.getEntity().getId(), event.getStack().getCount()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// FIXME fabric

	/*
	 * FIXME event not (yet?) on 1.13
	 * 
	 * @SubscribeEvent public void enterMinecart(MinecartInteractEvent event) { try
	 * { if(event.getEntity() != mc.player) { return; }
	 * 
	 * packetListener.save(new SPacketEntityAttach(event.getPlayer(),
	 * event.getMinecart()));
	 * 
	 * lastRiding = event.getMinecart().getEntityId(); } catch(Exception e) {
	 * e.printStackTrace(); } }
	 */

	public void onBlockBreakAnim(int breakerId, BlockPos pos, int progress) {
		Player thePlayer = mc.player;
		if (thePlayer != null && breakerId == thePlayer.getId()) {
			packetListener.save(new ClientboundBlockDestructionPacket(breakerId, pos, progress));
		}
	}

	{
		on(PreRenderCallback.EVENT, this::checkForGamePaused);
	}

	private void checkForGamePaused() {
		if (mc.hasSingleplayerServer()) {
			IntegratedServer server = mc.getSingleplayerServer();
			if (server != null && ((IntegratedServerAccessor) server).isGamePaused()) {
				packetListener.setServerWasPaused();
			}
		}
	}

	public interface RecordingEventSender {
		void setRecordingEventHandler(RecordingEventHandler recordingEventHandler);

		RecordingEventHandler getRecordingEventHandler();
	}
}