package com.replaymod.render.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.replaymod.render.RenderSettings;
import com.replaymod.replaystudio.lib.guava.base.Optional;
import com.replaymod.replaystudio.pathing.PathingRegistry;
import com.replaymod.replaystudio.pathing.path.Timeline;
import com.replaymod.replaystudio.pathing.serialize.TimelineSerialization;
import com.replaymod.replaystudio.replay.ReplayFile;
import com.replaymod.simplepathing.SPTimeline;

public class RenderJob {
	private Timeline timeline;
	private RenderSettings settings;

	public String getName() {
		return this.settings.getOutputFile().getName();
	}

	public Timeline getTimeline() {
		return this.timeline;
	}

	public RenderSettings getSettings() {
		return this.settings;
	}

	public void setTimeline(Timeline timeline) {
		this.timeline = timeline;
	}

	public void setSettings(RenderSettings settings) {
		this.settings = settings;
	}

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if (o != null && this.getClass() == o.getClass()) {
			RenderJob renderJob = (RenderJob) o;
			return this.timeline.equals(renderJob.timeline) && this.settings.equals(renderJob.settings);
		} else {
			return false;
		}
	}

	public int hashCode() {
		return Objects.hash(new Object[] { this.timeline, this.settings });
	}

	public String toString() {
		return "RenderJob{timeline=" + this.timeline + ", settings=" + this.settings + "}";
	}

	public static List<RenderJob> readQueue(ReplayFile replayFile) throws IOException {
		synchronized (replayFile) {
			Optional<InputStream> optIn = replayFile.get("renderQueue.json");
			if (!optIn.isPresent()) {
				return new ArrayList();
			} else {
				InputStream in = (InputStream) optIn.get();

				Object var6;
				try {
					InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8);

					try {
						List<RenderJob> jobs = (List) (new GsonBuilder())
								.registerTypeAdapter(Timeline.class, new RenderJob.TimelineTypeAdapter()).create()
								.fromJson(reader, (new TypeToken<List<RenderJob>>() {
								}).getType());
						if (jobs == null) {
							jobs = new ArrayList();
						}

						var6 = jobs;
					} catch (Throwable var10) {
						try {
							reader.close();
						} catch (Throwable var9) {
							var10.addSuppressed(var9);
						}

						throw var10;
					}

					reader.close();
				} catch (Throwable var11) {
					if (in != null) {
						try {
							in.close();
						} catch (Throwable var8) {
							var11.addSuppressed(var8);
						}
					}

					throw var11;
				}

				if (in != null) {
					in.close();
				}

				return (List) var6;
			}
		}
	}

	public static void writeQueue(ReplayFile replayFile, List<RenderJob> renderQueue) throws IOException {
		synchronized (replayFile) {
			OutputStream out = replayFile.write("renderQueue.json");

			try {
				OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);

				try {
					(new GsonBuilder()).registerTypeAdapter(Timeline.class, new RenderJob.TimelineTypeAdapter())
							.create().toJson(renderQueue, writer);
				} catch (Throwable var10) {
					try {
						writer.close();
					} catch (Throwable var9) {
						var10.addSuppressed(var9);
					}

					throw var10;
				}

				writer.close();
			} catch (Throwable var11) {
				if (out != null) {
					try {
						out.close();
					} catch (Throwable var8) {
						var11.addSuppressed(var8);
					}
				}

				throw var11;
			}

			if (out != null) {
				out.close();
			}

		}
	}

	private static class TimelineTypeAdapter extends TypeAdapter<Timeline> {
		private final TimelineSerialization serialization;

		public TimelineTypeAdapter(TimelineSerialization serialization) {
			this.serialization = serialization;
		}

		public TimelineTypeAdapter(PathingRegistry registry) {
			this(new TimelineSerialization(registry, (ReplayFile) null));
		}

		public TimelineTypeAdapter() {
			this((PathingRegistry) (new SPTimeline()));
		}

		public void write(JsonWriter out, Timeline value) throws IOException {
			out.value(this.serialization.serialize(Collections.singletonMap("", value)));
		}

		public Timeline read(JsonReader in) throws IOException {
			return (Timeline) this.serialization.deserialize(in.nextString()).get("");
		}
	}
}
