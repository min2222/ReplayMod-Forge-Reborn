package com.replaymod.editor.gui;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.replaymod.core.ReplayMod;
import com.replaymod.core.versions.MCVer;
import com.replaymod.replaystudio.PacketData;
import com.replaymod.replaystudio.data.Marker;
import com.replaymod.replaystudio.filter.DimensionTracker;
import com.replaymod.replaystudio.filter.SquashFilter;
import com.replaymod.replaystudio.filter.StreamFilter;
import com.replaymod.replaystudio.io.ReplayInputStream;
import com.replaymod.replaystudio.io.ReplayOutputStream;
import com.replaymod.replaystudio.protocol.PacketTypeRegistry;
import com.replaymod.replaystudio.replay.ReplayFile;
import com.replaymod.replaystudio.replay.ReplayMetaData;
import com.replaymod.replaystudio.stream.IteratorStream;
import com.replaymod.replaystudio.stream.PacketStream;
import com.replaymod.replaystudio.util.Utils;

public class MarkerProcessor {
	public static final String MARKER_NAME_START_CUT = "_RM_START_CUT";
	public static final String MARKER_NAME_END_CUT = "_RM_END_CUT";
	public static final String MARKER_NAME_SPLIT = "_RM_SPLIT";

	private static boolean hasWork(Path path) throws IOException {
		try (ReplayFile inputReplayFile = ReplayMod.instance.files.open(path)) {
			return inputReplayFile.getMarkers().or(HashSet::new).stream()
					.anyMatch(m -> m.getName() != null && m.getName().startsWith("_RM_"));
		}
	}

	public static boolean producesAnyOutput(ReplayFile replayFile) throws IOException {
		return !getOutputSuffixes(replayFile).isEmpty();
	}

	private static List<String> getOutputSuffixes(ReplayFile inputReplayFile) throws IOException {
		List<Marker> markers = (inputReplayFile.getMarkers().or(HashSet::new)).stream()
				.sorted(Comparator.comparing(Marker::getTime)).collect(Collectors.toList());
		int nextSuffix = 0;
		List<String> suffixes = new ArrayList();
		MarkerProcessor.OutputState state = MarkerProcessor.OutputState.Writing;
		Iterator var5 = markers.iterator();

		while (var5.hasNext()) {
			Marker marker = (Marker) var5.next();
			if ("_RM_START_CUT".equals(marker.getName())) {
				if (marker.getTime() == 0) {
					state = MarkerProcessor.OutputState.NotYetWriting;
				} else {
					state = MarkerProcessor.OutputState.Paused;
				}
			} else if ("_RM_END_CUT".equals(marker.getName())) {
				state = MarkerProcessor.OutputState.Writing;
			} else if ("_RM_SPLIT".equals(marker.getName())) {
				int var10001;
				switch (state) {
				case NotYetWriting:
				default:
					break;
				case Writing:
					var10001 = nextSuffix++;
					suffixes.add("_" + var10001);
					state = MarkerProcessor.OutputState.Writing;
					break;
				case Paused:
					var10001 = nextSuffix++;
					suffixes.add("_" + var10001);
					state = MarkerProcessor.OutputState.NotYetWriting;
				}
			}
		}

		if (state != MarkerProcessor.OutputState.NotYetWriting) {
			suffixes.add("_" + nextSuffix);
		}

		if (suffixes.size() == 1) {
			return Collections.singletonList("");
		} else {
			return suffixes;
		}
	}

	public static List<Pair<Path, ReplayMetaData>> apply(Path path, Consumer<Float> progress) throws IOException {
		ReplayMod mod = ReplayMod.instance;
		if (!hasWork(path)) {
			ReplayFile inputReplayFile = mod.files.open(path);

			ReplayMetaData metaData;
			try {
				metaData = inputReplayFile.getMetaData();
			} catch (Throwable var42) {
				if (inputReplayFile != null) {
					try {
						inputReplayFile.close();
					} catch (Throwable var37) {
						var42.addSuppressed(var37);
					}
				}

				throw var42;
			}

			if (inputReplayFile != null) {
				inputReplayFile.close();
			}

			return Collections.singletonList(Pair.of(path, metaData));
		} else {
			String replayName = FilenameUtils.getBaseName(path.getFileName().toString());
			int splitCounter = 0;
			PacketTypeRegistry registry = MCVer.getPacketTypeRegistry(true);
			DimensionTracker dimensionTracker = new DimensionTracker();
			SquashFilter squashFilter = new SquashFilter(null, null, null);
			List<Pair<Path, ReplayMetaData>> outputPaths = new ArrayList();
			Path rawFolder = ReplayMod.instance.folders.getRawReplayFolder();
			Path inputPath = rawFolder.resolve(path.getFileName());

			for (int i = 1; Files.exists(inputPath, new LinkOption[0]); ++i) {
				inputPath = inputPath.resolveSibling(replayName + "." + i + ".mcpr");
			}

			Files.move(path, inputPath);
			ReplayFile inputReplayFile = mod.files.open(inputPath);

			try {
				List<Marker> markers = inputReplayFile.getMarkers().or(HashSet::new).stream()
						.sorted(Comparator.comparing(Marker::getTime)).collect(Collectors.toList());
				Iterator<Marker> markerIterator = markers.iterator();
				Iterator<String> outputFileSuffixes = getOutputSuffixes(inputReplayFile).iterator();
				boolean anySplit = markers.stream().anyMatch((m) -> {
					return "_RM_SPLIT".equals(m.getName());
				});
				int inputDuration = inputReplayFile.getMetaData().getDuration();
				ReplayInputStream replayInputStream = inputReplayFile.getPacketData(registry);
				int timeOffset = 0;
				SquashFilter cutFilter = null;
				int startCutOffset = 0;
				PacketData nextPacket = replayInputStream.readPacket();
				Marker nextMarker = (Marker) markerIterator.next();

				while (true) {
					if (nextPacket == null || !outputFileSuffixes.hasNext()) {
						squashFilter.release();
						if (cutFilter != null) {
							cutFilter.release();
						}
						break;
					}

					Path outputPath = path.resolveSibling(replayName + (String) outputFileSuffixes.next() + ".mcpr");
					ReplayFile outputReplayFile = mod.files.open((Path) null, outputPath);

					try {
						long duration = 0L;
						Set<Marker> outputMarkers = new HashSet();
						ReplayMetaData metaData = inputReplayFile.getMetaData();
						metaData.setDate(metaData.getDate() + (long) timeOffset);
						ReplayOutputStream replayOutputStream = outputReplayFile.writePacketData();

						try {
							if (cutFilter != null) {
								cutFilter.release();
								cutFilter = squashFilter.copy();
							} else if (splitCounter > 0) {
								List<PacketData> packets = new ArrayList();
								squashFilter.copy().onEnd(
										new IteratorStream(packets.listIterator(), (StreamFilter) null),
										(long) timeOffset);
								Iterator var31 = packets.iterator();

								while (var31.hasNext()) {
									PacketData packet = (PacketData) var31.next();
									replayOutputStream.write(0L, packet.getPacket());
								}
							}

							boolean hasFurtherOutputs = outputFileSuffixes.hasNext();

							label222: while (true) {
								while (true) {
									if (nextPacket == null) {
										break label222;
									}

									if (nextMarker != null && nextPacket.getTime() >= (long) nextMarker.getTime()) {
										if ("_RM_START_CUT".equals(nextMarker.getName())) {
											if (cutFilter != null) {
												cutFilter.release();
											}

											startCutOffset = nextMarker.getTime();
											cutFilter = new SquashFilter(dimensionTracker);
										} else if (!"_RM_END_CUT".equals(nextMarker.getName())) {
											if ("_RM_SPLIT".equals(nextMarker.getName())) {
												++splitCounter;
												timeOffset = nextMarker.getTime();
												startCutOffset = timeOffset;
												nextMarker = markerIterator.hasNext() ? (Marker) markerIterator.next()
														: null;
												break label222;
											}

											nextMarker.setTime(nextMarker.getTime() - timeOffset);
											outputMarkers.add(nextMarker);
										} else {
											timeOffset += nextMarker.getTime() - startCutOffset;
											if (cutFilter != null) {
												List<PacketData> packets = new ArrayList();
												cutFilter.onEnd(
														new IteratorStream(packets.listIterator(), (StreamFilter) null),
														(long) nextMarker.getTime());
												Iterator var56 = packets.iterator();

												while (var56.hasNext()) {
													PacketData packet = (PacketData) var56.next();
													replayOutputStream.write((long) (nextMarker.getTime() - timeOffset),
															packet.getPacket());
												}

												cutFilter = null;
											}
										}

										nextMarker = markerIterator.hasNext() ? (Marker) markerIterator.next() : null;
									} else {
										dimensionTracker.onPacket((PacketStream) null, nextPacket);
										if (hasFurtherOutputs) {
											squashFilter.onPacket((PacketStream) null, nextPacket);
										}

										if (cutFilter != null) {
											cutFilter.onPacket((PacketStream) null, nextPacket);
										} else {
											replayOutputStream.write(nextPacket.getTime() - (long) timeOffset,
													nextPacket.getPacket().copy());
											duration = nextPacket.getTime() - (long) timeOffset;
										}

										nextPacket.release();
										nextPacket = replayInputStream.readPacket();
										if (nextPacket != null) {
											progress.accept((float) nextPacket.getTime() / (float) inputDuration);
										} else {
											progress.accept(1.0F);
										}
									}
								}
							}
						} catch (Throwable var45) {
							if (replayOutputStream != null) {
								try {
									replayOutputStream.close();
								} catch (Throwable var39) {
									var45.addSuppressed(var39);
								}
							}

							throw var45;
						}

						if (replayOutputStream != null) {
							replayOutputStream.close();
						}

						metaData.setDuration((int) duration);
						outputReplayFile.writeMetaData(registry, metaData);
						outputReplayFile.writeMarkers(outputMarkers);
						outputReplayFile.writeModInfo(inputReplayFile.getModInfo());
						Map<Integer, String> resourcePackIndex = inputReplayFile.getResourcePackIndex();
						if (resourcePackIndex != null) {
							outputReplayFile.writeResourcePackIndex(resourcePackIndex);
							Iterator var53 = resourcePackIndex.values().iterator();

							while (var53.hasNext()) {
								String hash = (String) var53.next();
								InputStream in = (InputStream) inputReplayFile.getResourcePack(hash).get();

								try {
									OutputStream out = outputReplayFile.writeResourcePack(hash);

									try {
										Utils.copy(in, out);
									} catch (Throwable var43) {
										if (out != null) {
											try {
												out.close();
											} catch (Throwable var41) {
												var43.addSuppressed(var41);
											}
										}

										throw var43;
									}

									if (out != null) {
										out.close();
									}
								} catch (Throwable var44) {
									if (in != null) {
										try {
											in.close();
										} catch (Throwable var40) {
											var44.addSuppressed(var40);
										}
									}

									throw var44;
								}

								if (in != null) {
									in.close();
								}
							}
						}

						outputReplayFile.save();
						outputPaths.add(Pair.of(outputPath, metaData));
					} catch (Throwable var46) {
						if (outputReplayFile != null) {
							try {
								outputReplayFile.close();
							} catch (Throwable var38) {
								var46.addSuppressed(var38);
							}
						}

						throw var46;
					}

					if (outputReplayFile != null) {
						outputReplayFile.close();
					}
				}
			} catch (Throwable var47) {
				if (inputReplayFile != null) {
					try {
						inputReplayFile.close();
					} catch (Throwable var36) {
						var47.addSuppressed(var36);
					}
				}

				throw var47;
			}

			if (inputReplayFile != null) {
				inputReplayFile.close();
			}

			return outputPaths;
		}
	}

	private static enum OutputState {
		NotYetWriting, Writing, Paused;

		// $FF: synthetic method
		private static MarkerProcessor.OutputState[] $values() {
			return new MarkerProcessor.OutputState[] { NotYetWriting, Writing, Paused };
		}
	}
}
