package com.replaymod.replay.camera;

import static com.replaymod.core.versions.MCVer.getMinecraft;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import com.replaymod.core.KeyBindingRegistry;
import com.replaymod.core.ReplayMod;
import com.replaymod.core.SettingsRegistry;
import com.replaymod.core.events.KeyBindingEventCallback;
import com.replaymod.core.events.PreRenderCallback;
import com.replaymod.core.events.PreRenderHandCallback;
import com.replaymod.core.events.SettingsChangedCallback;
import com.replaymod.core.utils.Utils;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.EventRegistrations;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.callbacks.PreTickCallback;
import com.replaymod.replay.ReplayHandler;
import com.replaymod.replay.ReplayModReplay;
import com.replaymod.replay.Setting;
import com.replaymod.replay.events.RenderHotbarCallback;
import com.replaymod.replay.events.RenderSpectatorCrosshairCallback;
import com.replaymod.replay.mixin.FirstPersonRendererAccessor;
import com.replaymod.replaystudio.util.Location;

import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatsCounter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.fluids.FluidType;

/**
 * The camera entity used as the main player entity during replay viewing.
 * During a replay the player should be an instance of this class. Camera
 * movement is controlled by a separate {@link CameraController}.
 */
public class CameraEntity extends LocalPlayer {
	private static final UUID CAMERA_UUID = UUID.nameUUIDFromBytes("ReplayModCamera".getBytes(StandardCharsets.UTF_8));

	/**
	 * Roll of this camera in degrees.
	 */
	public float roll;

	private CameraController cameraController;

	private long lastControllerUpdate = System.currentTimeMillis();

	/**
	 * The entity whose hand was the last one rendered.
	 */
	private Entity lastHandRendered = null;

	/**
	 * The hashCode and equals methods of Entity are not stable. Therefore we cannot
	 * register any event handlers directly in the CameraEntity class and instead
	 * have this inner class.
	 */
	private EventHandler eventHandler = new EventHandler();

	public CameraEntity(Minecraft mcIn, ClientLevel worldIn, ClientPacketListener netHandlerPlayClient,
			StatsCounter statisticsManager, ClientRecipeBook recipeBook) {
		super(mcIn, worldIn, netHandlerPlayClient, statisticsManager, recipeBook, false, false);
		setUUID(CAMERA_UUID);
		eventHandler.register();
		if (ReplayModReplay.instance.getReplayHandler().getSpectatedUUID() == null) {
			cameraController = ReplayModReplay.instance.createCameraController(this);
		} else {
			cameraController = new SpectatorCameraController(this);
		}
	}

	public CameraController getCameraController() {
		return cameraController;
	}

	public void setCameraController(CameraController cameraController) {
		this.cameraController = cameraController;
	}

	/**
	 * Moves the camera by the specified delta.
	 *
	 * @param x Delta in X direction
	 * @param y Delta in Y direction
	 * @param z Delta in Z direction
	 */
	public void moveCamera(double x, double y, double z) {
		setCameraPosition(this.getX() + x, this.getY() + y, this.getZ() + z);
	}

	/**
	 * Set the camera position.
	 *
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @param z Z coordinate
	 */
	public void setCameraPosition(double x, double y, double z) {
		this.xOld = this.xo = x;
		this.yOld = this.yo = y;
		this.zOld = this.zo = z;
		this.setPosRaw(x, y, z);
		updateBoundingBox();
	}

	/**
	 * Sets the camera rotation.
	 *
	 * @param yaw   Yaw in degrees
	 * @param pitch Pitch in degrees
	 * @param roll  Roll in degrees
	 */
	public void setCameraRotation(float yaw, float pitch, float roll) {
		this.yRotO = yaw;
		this.xRotO = pitch;
		this.setYRot(yaw);
		this.setXRot(pitch);
		this.roll = roll;
	}

	/**
	 * Sets the camera position and rotation to that of the specified
	 * AdvancedPosition
	 *
	 * @param pos The position and rotation to set
	 */
	public void setCameraPosRot(Location pos) {
		setCameraRotation(pos.getYaw(), pos.getPitch(), roll);
		setCameraPosition(pos.getX(), pos.getY(), pos.getZ());
	}

	/**
	 * Sets the camera position and rotation to that of the specified entity.
	 *
	 * @param to The entity whose position to copy
	 */
	public void setCameraPosRot(Entity to) {
		if (to == this)
			return;
		float yOffset = 0;
		this.xo = to.xo;
		this.yo = to.yo + yOffset;
		this.zo = to.zo;
		this.yRotO = to.yRotO;
		this.xRotO = to.xRotO;
		this.setPosRaw(to.getX(), to.getY(), to.getZ());
		this.setYRot(to.getYRot());
		this.setXRot(to.getXRot());
		this.xOld = to.xOld;
		this.yOld = to.yOld + yOffset;
		this.zOld = to.zOld;
		updateBoundingBox();
	}

	private void updateBoundingBox() {
		float width = getBbWidth();
		float height = getBbHeight();
		setBoundingBox(new AABB(this.getX() - width / 2, this.getY(), this.getZ() - width / 2, this.getX() + width / 2,
				this.getY() + height, this.getZ() + width / 2));
	}

	@Override
	public void tick() {
		Entity view = this.minecraft.getCameraEntity();
		if (view != null) {
			// Make sure we're always spectating the right entity
			// This is important if the spectated player respawns as their
			// entity is recreated and we have to spectate a new entity
			UUID spectating = ReplayModReplay.instance.getReplayHandler().getSpectatedUUID();
			if (spectating != null && (view.getUUID() != spectating || view.level != this.level)
					|| this.level.getEntity(view.getId()) != view) {
				if (spectating == null) {
					// Entity (non-player) died, stop spectating
					ReplayModReplay.instance.getReplayHandler().spectateEntity(this);
					return;
				}
				view = this.level.getPlayerByUUID(spectating);
				if (view != null) {
					this.minecraft.setCameraEntity(view);
				} else {
					this.minecraft.setCameraEntity(this);
					return;
				}
			}
			// Move cmera to their position so when we exit the first person view
			// we don't jump back to where we entered it
			if (view != this) {
				setCameraPosRot(view);
			}
		}
	}

	@Override
	public void resetPos() {
		// Make sure our world is up-to-date in case of world changes
		if (this.minecraft.level != null) {
			this.level = this.minecraft.level;
		}
		super.resetPos();
	}

	@Override
	public void setRot(float yaw, float pitch) {
		if (this.minecraft.getCameraEntity() == this) {
			// Only update camera rotation when the camera is the view
			super.setRot(yaw, pitch);
		}
	}

	@Override
	public boolean isInWall() {
		return falseUnlessSpectating(Entity::isInWall); // Make sure no suffocation overlay is rendered
	}

	@Override
	public boolean isEyeInFluidType(FluidType fluid) {
		return falseUnlessSpectating(entity -> entity.isEyeInFluidType(fluid));
	}

	@Override
	public boolean isOnFire() {
		return falseUnlessSpectating(Entity::isOnFire); // Make sure no fire overlay is rendered
	}

	private boolean falseUnlessSpectating(Function<Entity, Boolean> property) {
		Entity view = this.minecraft.getCameraEntity();
		if (view != null && view != this) {
			return property.apply(view);
		}
		return false;
	}

	@Override
	public boolean isPushable() {
		return false; // We are in full control of ourselves
	}

	@Override
	protected void spawnSprintParticle() {
		// We do not produce any particles, we are a camera
	}

	@Override
	public boolean canBeCollidedWith() {
		return false; // We are a camera, we cannot collide
	}

	@Override
	public boolean isSpectator() {
		ReplayHandler replayHandler = ReplayModReplay.instance.getReplayHandler();
		return replayHandler == null || replayHandler.isCameraView(); // Make sure we're treated as spectator
	}

	@Override
	public boolean shouldRender(double double_1, double double_2, double double_3) {
		return false; // never render the camera otherwise it'd be visible e.g. in 3rd-person or with
						// shaders
	}

	@Override
	public float getFieldOfViewModifier() {
		Entity view = this.minecraft.getCameraEntity();
		if (view != this && view instanceof AbstractClientPlayer) {
			return ((AbstractClientPlayer) view).getFieldOfViewModifier();
		}
		return 1;
	}

	@Override
	public boolean isInvisible() {
		Entity view = this.minecraft.getCameraEntity();
		if (view != this) {
			return view.isInvisible();
		}
		return super.isInvisible();
	}

	@Override
	public ResourceLocation getSkinTextureLocation() {
		Entity view = this.minecraft.getCameraEntity();
		if (view != this && view instanceof Player) {
			return ((AbstractClientPlayer) view).getSkinTextureLocation();
		}
		return super.getSkinTextureLocation();
	}

	@Override
	public String getModelName() {
		Entity view = this.minecraft.getCameraEntity();
		if (view != this && view instanceof AbstractClientPlayer) {
			return ((AbstractClientPlayer) view).getModelName();
		}
		return super.getModelName();
	}

	@Override
	public boolean isModelPartShown(PlayerModelPart modelPart) {
		Entity view = this.minecraft.getCameraEntity();
		if (view != this && view instanceof Player) {
			return ((Player) view).isModelPartShown(modelPart);
		}
		return super.isModelPartShown(modelPart);
	}

	@Override
	public float getAttackAnim(float renderPartialTicks) {
		Entity view = this.minecraft.getCameraEntity();
		if (view != this && view instanceof Player) {
			return ((Player) view).getAttackAnim(renderPartialTicks);
		}
		return 0;
	}

	@Override
	public float getCurrentItemAttackStrengthDelay() {
		Entity view = this.minecraft.getCameraEntity();
		if (view != this && view instanceof Player) {
			return ((Player) view).getCurrentItemAttackStrengthDelay();
		}
		return 1;
	}

	@Override
	public float getAttackStrengthScale(float adjustTicks) {
		Entity view = this.minecraft.getCameraEntity();
		if (view != this && view instanceof Player) {
			return ((Player) view).getAttackStrengthScale(adjustTicks);
		}
		// Default to 1 as to not render the cooldown indicator (renders for < 1)
		return 1;
	}

	@Override
	public InteractionHand getUsedItemHand() {
		Entity view = this.minecraft.getCameraEntity();
		if (view != this && view instanceof Player) {
			return ((Player) view).getUsedItemHand();
		}
		return super.getUsedItemHand();
	}

	@Override
	public boolean isUsingItem() {
		Entity view = this.minecraft.getCameraEntity();
		if (view != this && view instanceof Player) {
			return ((Player) view).isUsingItem();
		}
		return super.isUsingItem();
	}

	@Override
	protected void playEquipSound(ItemStack itemStack_1) {
		// Suppress equip sounds
	}

	@Override
	public HitResult pick(double maxDistance, float tickDelta, boolean fluids) {
		HitResult result = super.pick(maxDistance, tickDelta, fluids);

		// Make sure we can never look at blocks (-> no outline)
		if (result instanceof BlockHitResult) {
			BlockHitResult blockResult = (BlockHitResult) result;
			result = BlockHitResult.miss(result.getLocation(), blockResult.getDirection(), blockResult.getBlockPos());
		}

		return result;
	}

	@Override
	public void remove(RemovalReason reason) {
		super.remove(reason);
		if (eventHandler != null) {
			eventHandler.unregister();
			eventHandler = null;
		}
	}

	private void update() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level != this.level) {
			if (eventHandler != null) {
				eventHandler.unregister();
				eventHandler = null;
			}
			return;
		}

		long now = System.currentTimeMillis();
		long timePassed = now - lastControllerUpdate;
		cameraController.update(timePassed / 50f);
		lastControllerUpdate = now;

		handleInputEvents();

		Map<String, KeyBindingRegistry.Binding> keyBindings = ReplayMod.instance.getKeyBindingRegistry().getBindings();
		if (keyBindings.get("replaymod.input.rollclockwise").keyBinding.isDown()) {
			roll += Utils.isCtrlDown() ? 0.2 : 1;
		}
		if (keyBindings.get("replaymod.input.rollcounterclockwise").keyBinding.isDown()) {
			roll -= Utils.isCtrlDown() ? 0.2 : 1;
		}

		this.noPhysics = this.isSpectator();
	}

	private void handleInputEvents() {
		if (this.minecraft.options.keyAttack.consumeClick() || this.minecraft.options.keyUse.consumeClick()) {
			if (this.minecraft.screen == null && canSpectate(this.minecraft.crosshairPickEntity)) {
				ReplayModReplay.instance.getReplayHandler().spectateEntity(this.minecraft.crosshairPickEntity);
				// Make sure we don't exit right away
				// noinspection StatementWithEmptyBody
				while (this.minecraft.options.keyShift.consumeClick())
					;
			}
		}
	}

	private void updateArmYawAndPitch() {
		this.yBobO = this.yBob;
		this.xBobO = this.xBob;
		this.xBob = this.xBob + (this.getXRot() - this.xBob) * 0.5f;
		this.yBob = this.yBob + (this.getYRot() - this.yBob) * 0.5f;
	}

	public boolean canSpectate(Entity e) {
		return e != null && !e.isInvisible();
	}

	private class EventHandler extends EventRegistrations {
		private final Minecraft mc = getMinecraft();

		private EventHandler() {
		}

		{
			on(PreTickCallback.EVENT, this::onPreClientTick);
		}

		private void onPreClientTick() {
			updateArmYawAndPitch();
		}

		{
			on(PreRenderCallback.EVENT, this::onRenderUpdate);
		}

		private void onRenderUpdate() {
			update();
		}

		{
			on(KeyBindingEventCallback.EVENT, CameraEntity.this::handleInputEvents);
		}

		{
			on(RenderSpectatorCrosshairCallback.EVENT, this::shouldRenderSpectatorCrosshair);
		}

		private Boolean shouldRenderSpectatorCrosshair() {
			return canSpectate(mc.crosshairPickEntity);
		}

		{
			on(RenderHotbarCallback.EVENT, this::shouldRenderHotbar);
		}

		private Boolean shouldRenderHotbar() {
			return false;
		}

		{
			on(SettingsChangedCallback.EVENT, this::onSettingsChanged);
		}

		private void onSettingsChanged(SettingsRegistry registry, SettingsRegistry.SettingKey<?> key) {
			if (key == Setting.CAMERA) {
				if (ReplayModReplay.instance.getReplayHandler().getSpectatedUUID() == null) {
					cameraController = ReplayModReplay.instance.createCameraController(CameraEntity.this);
				} else {
					cameraController = new SpectatorCameraController(CameraEntity.this);
				}
			}
		}

		{
			on(PreRenderHandCallback.EVENT, this::onRenderHand);
		}

		private boolean onRenderHand() {
			// Unless we are spectating another player, don't render our hand
			Entity view = mc.getCameraEntity();
			if (view == CameraEntity.this || !(view instanceof Player)) {
				return true; // cancel hand rendering
			} else {
				Player player = (Player) view;
				// When the spectated player has changed, force equip their items to prevent the
				// equip animation
				if (lastHandRendered != player) {
					lastHandRendered = player;

					FirstPersonRendererAccessor acc = (FirstPersonRendererAccessor) mc.gameRenderer.itemInHandRenderer;
					acc.setPrevEquippedProgressMainHand(1);
					acc.setPrevEquippedProgressOffHand(1);
					acc.setEquippedProgressMainHand(1);
					acc.setEquippedProgressOffHand(1);
					acc.setItemStackMainHand(player.getItemBySlot(EquipmentSlot.MAINHAND));
					acc.setItemStackOffHand(player.getItemBySlot(EquipmentSlot.OFFHAND));

					mc.player.yBob = mc.player.yBobO = player.getYRot();
					mc.player.xBob = mc.player.xBobO = player.getXRot();
				}
				return false;
			}
		}
	}
}