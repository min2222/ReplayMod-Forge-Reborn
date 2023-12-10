package com.replaymod.simplepathing;

import static com.replaymod.replaystudio.pathing.change.RemoveKeyframe.create;
import static com.replaymod.simplepathing.ReplayModSimplePathing.LOGGER;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Triple;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.replaymod.pathing.properties.CameraProperties;
import com.replaymod.pathing.properties.SpectatorProperty;
import com.replaymod.pathing.properties.TimestampProperty;
import com.replaymod.replaystudio.pathing.PathingRegistry;
import com.replaymod.replaystudio.pathing.change.AddKeyframe;
import com.replaymod.replaystudio.pathing.change.Change;
import com.replaymod.replaystudio.pathing.change.CombinedChange;
import com.replaymod.replaystudio.pathing.change.RemoveKeyframe;
import com.replaymod.replaystudio.pathing.change.SetInterpolator;
import com.replaymod.replaystudio.pathing.change.UpdateKeyframeProperties;
import com.replaymod.replaystudio.pathing.impl.TimelineImpl;
import com.replaymod.replaystudio.pathing.interpolation.CatmullRomSplineInterpolator;
import com.replaymod.replaystudio.pathing.interpolation.CubicSplineInterpolator;
import com.replaymod.replaystudio.pathing.interpolation.Interpolator;
import com.replaymod.replaystudio.pathing.interpolation.LinearInterpolator;
import com.replaymod.replaystudio.pathing.path.Keyframe;
import com.replaymod.replaystudio.pathing.path.Path;
import com.replaymod.replaystudio.pathing.path.PathSegment;
import com.replaymod.replaystudio.pathing.path.Timeline;
import com.replaymod.replaystudio.pathing.property.Property;
import com.replaymod.replaystudio.util.EntityPositionTracker;
import com.replaymod.replaystudio.util.Location;
import com.replaymod.simplepathing.properties.ExplicitInterpolationProperty;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;

public class SPTimeline implements PathingRegistry {
	private final Timeline timeline;
	private final Path timePath;
	private final Path positionPath;
	private EntityPositionTracker entityTracker;
	private InterpolatorType defaultInterpolatorType;

	public SPTimeline() {
		this(createInitialTimeline());
	}

	public SPTimeline(Timeline timeline) {
		this.timeline = timeline;
		this.timePath = (Path) timeline.getPaths().get(SPTimeline.SPPath.TIME.ordinal());
		this.positionPath = (Path) timeline.getPaths().get(SPTimeline.SPPath.POSITION.ordinal());
	}

	public Timeline getTimeline() {
		return this.timeline;
	}

	public Path getTimePath() {
		return this.timePath;
	}

	public Path getPositionPath() {
		return this.positionPath;
	}

	public EntityPositionTracker getEntityTracker() {
		return this.entityTracker;
	}

	public Path getPath(SPTimeline.SPPath path) {
		switch (path) {
		case TIME:
			return this.getTimePath();
		case POSITION:
			return this.getPositionPath();
		default:
			throw new IllegalArgumentException("Unknown path " + path);
		}
	}

	public Keyframe getKeyframe(SPTimeline.SPPath path, long keyframe) {
		return this.getPath(path).getKeyframe(keyframe);
	}

	public void setEntityTracker(EntityPositionTracker entityTracker) {
		Preconditions.checkState(this.entityTracker == null, "Entity tracker already set");
		this.entityTracker = entityTracker;
	}

	public void setDefaultInterpolatorType(InterpolatorType defaultInterpolatorType) {
		Validate.isTrue(defaultInterpolatorType != InterpolatorType.DEFAULT, "Must not be DEFAULT");
		InterpolatorType prevType = this.defaultInterpolatorType;
		this.defaultInterpolatorType = (InterpolatorType) Validate.notNull(defaultInterpolatorType);
		if (prevType != null && prevType != this.defaultInterpolatorType) {
			this.timeline.pushChange(this.updateInterpolators());
		}

	}

	public Change setDefaultInterpolator(Interpolator interpolator) {
		Preconditions.checkState(this.defaultInterpolatorType != null, "Default interpolator type not set.");
		Validate.isInstanceOf(this.defaultInterpolatorType.getInterpolatorClass(), interpolator);
		this.registerPositionInterpolatorProperties(interpolator);
		Change change = CombinedChange.create((Change[]) this.positionPath.getSegments().stream().filter((s) -> {
			return !s.getStartKeyframe().getValue(ExplicitInterpolationProperty.PROPERTY).isPresent();
		}).filter((s) -> {
			return !this.isSpectatorSegment(s);
		}).map((s) -> {
			return SetInterpolator.create(s, interpolator);
		}).toArray((x$0) -> {
			return new Change[x$0];
		}));
		change.apply(this.timeline);
		return CombinedChange.createFromApplied(change, this.updateInterpolators());
	}

	public boolean isTimeKeyframe(long time) {
		return this.timePath.getKeyframe(time) != null;
	}

	public boolean isPositionKeyframe(long time) {
		return this.positionPath.getKeyframe(time) != null;
	}

	public boolean isSpectatorKeyframe(long time) {
		Keyframe keyframe = this.positionPath.getKeyframe(time);
		return keyframe != null && keyframe.getValue(SpectatorProperty.PROPERTY).isPresent();
	}

	public void addPositionKeyframe(long time, double posX, double posY, double posZ, float yaw, float pitch,
			float roll, int spectated) {
		LOGGER.debug("Adding position keyframe at {} pos {}/{}/{} rot {}/{}/{} entId {}", time, posX, posY, posZ, yaw,
				pitch, roll, spectated);

		Path path = positionPath;

		Preconditions.checkState(positionPath.getKeyframe(time) == null, "Keyframe already exists");

		Change change = AddKeyframe.create(path, time);
		change.apply(timeline);
		Keyframe keyframe = path.getKeyframe(time);

		UpdateKeyframeProperties.Builder builder = UpdateKeyframeProperties.create(path, keyframe);
		builder.setValue(CameraProperties.POSITION, Triple.of(posX, posY, posZ));
		builder.setValue(CameraProperties.ROTATION, Triple.of(yaw, pitch, roll));
		if (spectated != -1) {
			builder.setValue(SpectatorProperty.PROPERTY, spectated);
		}
		UpdateKeyframeProperties updateChange = builder.done();
		updateChange.apply(timeline);
		change = CombinedChange.createFromApplied(change, updateChange);

		// If this new keyframe formed the first segment of the path
		if (path.getSegments().size() == 1) {
			// then create an initial interpolator of default type
			PathSegment segment = path.getSegments().iterator().next();
			Interpolator interpolator = createDefaultInterpolator();
			SetInterpolator setInterpolator = SetInterpolator.create(segment, interpolator);
			setInterpolator.apply(timeline);
			change = CombinedChange.createFromApplied(change, setInterpolator);
		}

		// Update interpolators for spectator keyframes
		// while this is overkill, it is far simpler than updating differently for every
		// possible case
		change = CombinedChange.createFromApplied(change, updateInterpolators());

		Change specPosUpdate = updateSpectatorPositions();
		specPosUpdate.apply(timeline);
		change = CombinedChange.createFromApplied(change, specPosUpdate);

		timeline.pushChange(change);
	}

	public Change updatePositionKeyframe(long time, double posX, double posY, double posZ, float yaw, float pitch,
			float roll) {
		ReplayModSimplePathing.LOGGER.debug("Updating position keyframe at {} to pos {}/{}/{} rot {}/{}/{}", time, posX,
				posY, posZ, yaw, pitch, roll);
		Keyframe keyframe = this.positionPath.getKeyframe(time);
		Preconditions.checkState(keyframe != null, "Keyframe does not exists");
		Preconditions.checkState(!keyframe.getValue(SpectatorProperty.PROPERTY).isPresent(),
				"Cannot update spectator keyframe");
		Change change = UpdateKeyframeProperties.create(this.positionPath, keyframe)
				.setValue(CameraProperties.POSITION, Triple.of(posX, posY, posZ))
				.setValue(CameraProperties.ROTATION, Triple.of(yaw, pitch, roll)).done();
		change.apply(this.timeline);
		return change;
	}

	public void removePositionKeyframe(long time) {
		LOGGER.debug("Removing position keyframe at {}", time);

		Path path = positionPath;
		Keyframe keyframe = path.getKeyframe(time);

		Preconditions.checkState(keyframe != null, "No keyframe at that time");

		Change change = create(path, keyframe);
		change.apply(timeline);

		// Update interpolators for spectator keyframes
		// while this is overkill, it is far simpler than updating differently for every
		// possible case
		change = CombinedChange.createFromApplied(change, updateInterpolators());

		Change specPosUpdate = updateSpectatorPositions();
		specPosUpdate.apply(timeline);
		change = CombinedChange.createFromApplied(change, specPosUpdate);

		timeline.pushChange(change);
	}

	public void addTimeKeyframe(long time, int replayTime) {
		LOGGER.debug("Adding time keyframe at {} time {}", time, replayTime);

		Path path = timePath;

		Preconditions.checkState(path.getKeyframe(time) == null, "Keyframe already exists");

		Change change = AddKeyframe.create(path, time);
		change.apply(timeline);
		Keyframe keyframe = path.getKeyframe(time);

		UpdateKeyframeProperties updateChange = UpdateKeyframeProperties.create(path, keyframe)
				.setValue(TimestampProperty.PROPERTY, replayTime).done();
		updateChange.apply(timeline);
		change = CombinedChange.createFromApplied(change, updateChange);

		// If this new keyframe formed the first segment of the path
		if (path.getSegments().size() == 1) {
			// then create an initial interpolator
			PathSegment segment = path.getSegments().iterator().next();
			Interpolator interpolator = new LinearInterpolator();
			interpolator.registerProperty(TimestampProperty.PROPERTY);
			SetInterpolator setInterpolator = SetInterpolator.create(segment, interpolator);
			setInterpolator.apply(timeline);
			change = CombinedChange.createFromApplied(change, setInterpolator);
		}

		Change specPosUpdate = updateSpectatorPositions();
		specPosUpdate.apply(timeline);
		change = CombinedChange.createFromApplied(change, specPosUpdate);

		timeline.pushChange(change);
	}

	public Change updateTimeKeyframe(long time, int replayTime) {
		ReplayModSimplePathing.LOGGER.debug("Updating time keyframe at {} to time {}", time, replayTime);
		Keyframe keyframe = this.timePath.getKeyframe(time);
		Preconditions.checkState(keyframe != null, "Keyframe does not exists");
		Change change = UpdateKeyframeProperties.create(this.timePath, keyframe)
				.setValue(TimestampProperty.PROPERTY, replayTime).done();
		change.apply(this.timeline);
		return change;
	}

	public void removeTimeKeyframe(long time) {
		LOGGER.debug("Removing time keyframe at {}", time);

		Path path = timePath;
		Keyframe keyframe = path.getKeyframe(time);

		Preconditions.checkState(keyframe != null, "No keyframe at that time");

		Change change = create(path, keyframe);
		change.apply(timeline);

		Change specPosUpdate = updateSpectatorPositions();
		specPosUpdate.apply(timeline);
		change = CombinedChange.createFromApplied(change, specPosUpdate);

		timeline.pushChange(change);
	}

	public Change setInterpolatorToDefault(long time) {
		ReplayModSimplePathing.LOGGER.debug("Setting interpolator of position keyframe at {} to the default", time);
		Keyframe keyframe = this.positionPath.getKeyframe(time);
		Preconditions.checkState(keyframe != null, "Keyframe does not exists");
		Change change = UpdateKeyframeProperties.create(this.positionPath, keyframe)
				.removeProperty(ExplicitInterpolationProperty.PROPERTY).done();
		change.apply(this.timeline);
		return CombinedChange.createFromApplied(change, this.updateInterpolators());
	}

	public Change setInterpolator(long time, Interpolator interpolator) {
		ReplayModSimplePathing.LOGGER.debug("Setting interpolator of position keyframe at {} to {}", time,
				interpolator);
		Keyframe keyframe = this.positionPath.getKeyframe(time);
		Preconditions.checkState(keyframe != null, "Keyframe does not exists");
		PathSegment segment = (PathSegment) this.positionPath.getSegments().stream().filter((s) -> {
			return s.getStartKeyframe() == keyframe;
		}).findFirst().orElseThrow(() -> {
			return new IllegalStateException("Keyframe has no following segment.");
		});
		this.registerPositionInterpolatorProperties(interpolator);
		Change change = CombinedChange.create(
				UpdateKeyframeProperties.create(this.positionPath, keyframe)
						.setValue(ExplicitInterpolationProperty.PROPERTY, ObjectUtils.NULL).done(),
				SetInterpolator.create(segment, interpolator));
		change.apply(this.timeline);
		return CombinedChange.createFromApplied(change, this.updateInterpolators());
	}

	public Change moveKeyframe(SPTimeline.SPPath spPath, long oldTime, long newTime) {
		ReplayModSimplePathing.LOGGER.debug("Moving keyframe on {} from {} to {}", spPath, oldTime, newTime);
		Path path = this.getPath(spPath);
		Keyframe keyframe = path.getKeyframe(oldTime);
		Preconditions.checkState(keyframe != null, "No keyframe at specified time");
		Optional<Interpolator> firstInterpolator = path.getSegments().stream().findFirst()
				.map(PathSegment::getInterpolator);
		Optional<Interpolator> lostInterpolator = path.getSegments().stream().filter((s) -> {
			if (Iterables.getLast(path.getKeyframes()) == keyframe) {
				return s.getEndKeyframe() == keyframe;
			} else {
				return s.getStartKeyframe() == keyframe;
			}
		}).findFirst().map(PathSegment::getInterpolator);
		Change removeChange = RemoveKeyframe.create(path, keyframe);
		removeChange.apply(this.timeline);
		Change addChange = AddKeyframe.create(path, newTime);
		addChange.apply(this.timeline);
		UpdateKeyframeProperties.Builder builder = UpdateKeyframeProperties.create(path, path.getKeyframe(newTime));
		Iterator var13 = keyframe.getProperties().iterator();

		while (var13.hasNext()) {
			Property property = (Property) var13.next();
			this.copyProperty(property, keyframe, builder);
		}

		Change propertyChange = builder.done();
		propertyChange.apply(this.timeline);
		Keyframe newKf = path.getKeyframe(newTime);
		Change restoreInterpolatorChange;
		if (Iterables.getLast(path.getKeyframes()) != newKf) {
			restoreInterpolatorChange = lostInterpolator
					.<Change>flatMap(
							interpolator -> path.getSegments().stream().filter(s -> s.getStartKeyframe() == newKf)
									.findFirst().map(segment -> SetInterpolator.create(segment, interpolator)))
					.orElseGet(CombinedChange::create);
		} else {
			restoreInterpolatorChange = (Change) path.getSegments().stream().filter((s) -> {
				return s.getEndKeyframe() == newKf;
			}).findFirst().flatMap((segment) -> {
				return lostInterpolator.map((interpolator) -> {
					return (Change) (newKf.getValue(ExplicitInterpolationProperty.PROPERTY).isPresent()
							? CombinedChange.create(SetInterpolator.create(segment, interpolator),
									UpdateKeyframeProperties.create(path, segment.getStartKeyframe())
											.setValue(ExplicitInterpolationProperty.PROPERTY, ObjectUtils.NULL).done())
							: SetInterpolator.create(segment, interpolator));
				});
			}).orElseGet(() -> {
				return CombinedChange.create();
			});
		}

		restoreInterpolatorChange.apply(this.timeline);
		Object interpolatorUpdateChange;
		if (spPath == SPTimeline.SPPath.POSITION) {
			interpolatorUpdateChange = this.updateInterpolators();
		} else {
			if (path.getSegments().size() == 1) {
				assert firstInterpolator.isPresent() : "One segment should have existed before as well";

				interpolatorUpdateChange = SetInterpolator.create((PathSegment) path.getSegments().iterator().next(),
						(Interpolator) firstInterpolator.get());
			} else {
				interpolatorUpdateChange = CombinedChange.create();
			}

			((Change) interpolatorUpdateChange).apply(this.timeline);
		}

		Change spectatorChange = this.updateSpectatorPositions();
		spectatorChange.apply(this.timeline);
		return CombinedChange.createFromApplied(removeChange, addChange, propertyChange, restoreInterpolatorChange,
				(Change) interpolatorUpdateChange, spectatorChange);
	}

	private <T> void copyProperty(Property<T> property, Keyframe from, UpdateKeyframeProperties.Builder to) {
		from.getValue(property).ifPresent((value) -> {
			to.setValue(property, value);
		});
	}

	private Change updateInterpolators() {
		Collection<PathSegment> pathSegments = this.positionPath.getSegments();
		Map<PathSegment, Interpolator> updates = new HashMap();
		Interpolator interpolator = null;
		Iterator var4 = pathSegments.iterator();

		while (var4.hasNext()) {
			PathSegment segment = (PathSegment) var4.next();
			if (this.isSpectatorSegment(segment)) {
				if (interpolator == null) {
					interpolator = new LinearInterpolator();
					interpolator.registerProperty(SpectatorProperty.PROPERTY);
				}

				updates.put(segment, interpolator);
			} else {
				interpolator = null;
			}
		}

		pathSegments.stream().filter((s) -> {
			return !s.getStartKeyframe().getValue(ExplicitInterpolationProperty.PROPERTY).isPresent();
		}).filter((s) -> {
			return !this.isSpectatorSegment(s);
		}).filter((s) -> {
			return !s.getInterpolator().getClass().equals(this.defaultInterpolatorType.getInterpolatorClass());
		}).forEach((segmentx) -> {
			updates.put(segmentx, this.createDefaultInterpolator());
		});
		Interpolator lastInterpolator = null;
		Set<Interpolator> used = Collections.newSetFromMap(new IdentityHashMap());
		Iterator var6 = pathSegments.iterator();

		while (var6.hasNext()) {
			PathSegment segment = (PathSegment) var6.next();
			if (this.isSpectatorSegment(segment)) {
				lastInterpolator = null;
			} else {
				Interpolator currentInterpolator = (Interpolator) updates.getOrDefault(segment,
						segment.getInterpolator());
				if (lastInterpolator != currentInterpolator) {
					if (!used.add(interpolator)) {
						currentInterpolator = this.cloneInterpolator(currentInterpolator);
						updates.put(segment, currentInterpolator);
					}

					lastInterpolator = currentInterpolator;
				}
			}
		}

		lastInterpolator = null;
		String lastInterpolatorSerialized = null;
		Iterator var14 = pathSegments.iterator();

		while (true) {
			while (var14.hasNext()) {
				PathSegment segment = (PathSegment) var14.next();
				if (this.isSpectatorSegment(segment)) {
					lastInterpolator = null;
					lastInterpolatorSerialized = null;
				} else {
					Interpolator currentInterpolator = (Interpolator) updates.getOrDefault(segment,
							segment.getInterpolator());
					String serialized = this.serializeInterpolator(currentInterpolator);
					if (lastInterpolator != currentInterpolator && serialized.equals(lastInterpolatorSerialized)) {
						updates.put(segment, lastInterpolator);
					} else {
						lastInterpolator = currentInterpolator;
						lastInterpolatorSerialized = serialized;
					}
				}
			}

			Change change = CombinedChange.create((Change[]) updates.entrySet().stream().map((e) -> {
				return SetInterpolator.create((PathSegment) e.getKey(), (Interpolator) e.getValue());
			}).toArray((x$0) -> {
				return new Change[x$0];
			}));
			change.apply(this.timeline);
			return change;
		}
	}

	private boolean isSpectatorSegment(PathSegment segment) {
		return segment.getStartKeyframe().getValue(SpectatorProperty.PROPERTY).isPresent()
				&& segment.getEndKeyframe().getValue(SpectatorProperty.PROPERTY).isPresent();
	}

	private Change updateSpectatorPositions() {
		if (this.entityTracker == null) {
			return CombinedChange.create();
		} else {
			List<Change> changes = new ArrayList();
			this.timePath.updateAll();
			Iterator var2 = this.positionPath.getKeyframes().iterator();

			while (var2.hasNext()) {
				Keyframe keyframe = (Keyframe) var2.next();
				Optional<Integer> spectator = keyframe.getValue(SpectatorProperty.PROPERTY);
				if (spectator.isPresent()) {
					Optional<Integer> time = this.timePath.getValue(TimestampProperty.PROPERTY, keyframe.getTime());
					if (time.isPresent()) {
						Location expected = this.entityTracker.getEntityPositionAtTimestamp((Integer) spectator.get(),
								(long) (Integer) time.get());
						if (expected != null) {
							Triple<Double, Double, Double> pos = (Triple) keyframe.getValue(CameraProperties.POSITION)
									.orElse(Triple.of(0.0D, 0.0D, 0.0D));
							Triple<Float, Float, Float> rot = (Triple) keyframe.getValue(CameraProperties.ROTATION)
									.orElse(Triple.of(0.0F, 0.0F, 0.0F));
							Location actual = new Location((Double) pos.getLeft(), (Double) pos.getMiddle(),
									(Double) pos.getRight(), (Float) rot.getLeft(), (Float) rot.getRight());
							if (!expected.equals(actual)) {
								changes.add(UpdateKeyframeProperties.create(this.positionPath, keyframe)
										.setValue(CameraProperties.POSITION,
												Triple.of(expected.getX(), expected.getY(), expected.getZ()))
										.setValue(CameraProperties.ROTATION,
												Triple.of(expected.getYaw(), expected.getPitch(), 0.0F))
										.done());
							}
						}
					}
				}
			}

			return CombinedChange.create((Change[]) changes.toArray(new Change[changes.size()]));
		}
	}

	private Interpolator createDefaultInterpolator() {
		return this.registerPositionInterpolatorProperties(this.defaultInterpolatorType.newInstance());
	}

	private Interpolator registerPositionInterpolatorProperties(Interpolator interpolator) {
		interpolator.registerProperty(CameraProperties.POSITION);
		interpolator.registerProperty(CameraProperties.ROTATION);
		return interpolator;
	}

	public Timeline createTimeline() {
		return createTimelineStatic();
	}

	private static Timeline createInitialTimeline() {
		Timeline timeline = createTimelineStatic();
		timeline.createPath();
		timeline.createPath();
		return timeline;
	}

	private static Timeline createTimelineStatic() {
		Timeline timeline = new TimelineImpl();
		timeline.registerProperty(TimestampProperty.PROPERTY);
		timeline.registerProperty(CameraProperties.POSITION);
		timeline.registerProperty(CameraProperties.ROTATION);
		timeline.registerProperty(SpectatorProperty.PROPERTY);
		timeline.registerProperty(ExplicitInterpolationProperty.PROPERTY);
		return timeline;
	}

	public void serializeInterpolator(JsonWriter writer, Interpolator interpolator) throws IOException {
		if (interpolator instanceof LinearInterpolator) {
			writer.value("linear");
		} else if (interpolator instanceof CubicSplineInterpolator) {
			writer.value("cubic-spline");
		} else {
			if (!(interpolator instanceof CatmullRomSplineInterpolator)) {
				throw new IOException("Unknown interpolator type: " + interpolator);
			}

			writer.beginObject();
			writer.name("type").value("catmull-rom-spline");
			writer.name("alpha").value(((CatmullRomSplineInterpolator) interpolator).getAlpha());
			writer.endObject();
		}

	}

	public Interpolator deserializeInterpolator(JsonReader reader) throws IOException {
		String type;
		JsonObject args;
		switch (reader.peek()) {
		case STRING:
			type = reader.nextString();
			args = null;
			break;
		case BEGIN_OBJECT:
			args = (new JsonParser()).parse(reader).getAsJsonObject();
			type = args.get("type").getAsString();
			break;
		default:
			throw new IOException("Unexpected token: " + reader.peek());
		}

		byte var5 = -1;
		switch (type.hashCode()) {
		case -1651603532:
			if (type.equals("cubic-spline")) {
				var5 = 1;
			}
			break;
		case -1234650723:
			if (type.equals("catmull-rom-spline")) {
				var5 = 2;
			}
			break;
		case -1102672091:
			if (type.equals("linear")) {
				var5 = 0;
			}
		}

		switch (var5) {
		case 0:
			return new LinearInterpolator();
		case 1:
			return new CubicSplineInterpolator();
		case 2:
			if (args != null && args.has("alpha")) {
				return new CatmullRomSplineInterpolator(args.get("alpha").getAsDouble());
			}

			throw new IOException("Missing alpha value for catmull-rom-spline.");
		default:
			throw new IOException("Unknown interpolation type: " + type);
		}
	}

	private Interpolator cloneInterpolator(Interpolator interpolator) {
		Interpolator cloned = deserializeInterpolator(serializeInterpolator(interpolator));
		interpolator.getKeyframeProperties().forEach(cloned::registerProperty);
		return cloned;
	}

	private String serializeInterpolator(Interpolator interpolator) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		JsonWriter jsonWriter = new JsonWriter(new PrintWriter(baos));

		try {
			jsonWriter.beginArray();
			this.serializeInterpolator(jsonWriter, interpolator);
			jsonWriter.endArray();
			jsonWriter.flush();
		} catch (IOException var7) {
			CrashReport crash = CrashReport.forThrowable(var7, "Serializing interpolator");
			CrashReportCategory category = crash.addCategory("Serializing interpolator");
			Objects.requireNonNull(interpolator);
			category.setDetail("Interpolator", interpolator::toString);
			throw new ReportedException(crash);
		}

		return baos.toString();
	}

	private Interpolator deserializeInterpolator(String json) {
		JsonReader jsonReader = new JsonReader(new StringReader(json));

		try {
			jsonReader.beginArray();
			return this.deserializeInterpolator(jsonReader);
		} catch (IOException var6) {
			CrashReport crash = CrashReport.forThrowable(var6, "De-serializing interpolator");
			CrashReportCategory category = crash.addCategory("De-serializing interpolator");
			Objects.requireNonNull(json);
			category.setDetail("Interpolator", json::toString);
			throw new ReportedException(crash);
		}
	}

	public static enum SPPath {
		TIME, POSITION;

		private static SPTimeline.SPPath[] $values() {
			return new SPTimeline.SPPath[] { TIME, POSITION };
		}
	}
}
