package com.replaymod.core.files;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import com.replaymod.replaystudio.data.Marker;
import com.replaymod.replaystudio.data.ModInfo;
import com.replaymod.replaystudio.data.ReplayAssetEntry;
import com.replaymod.replaystudio.io.ReplayInputStream;
import com.replaymod.replaystudio.io.ReplayOutputStream;
import com.replaymod.replaystudio.lib.guava.base.Optional;
import com.replaymod.replaystudio.pathing.PathingRegistry;
import com.replaymod.replaystudio.pathing.path.Timeline;
import com.replaymod.replaystudio.protocol.PacketTypeRegistry;
import com.replaymod.replaystudio.replay.ReplayFile;
import com.replaymod.replaystudio.replay.ReplayMetaData;

public class DelegatingReplayFile implements ReplayFile {
	private final ReplayFile delegate;

	public DelegatingReplayFile(ReplayFile delegate) {
		this.delegate = delegate;
	}

	public Optional<InputStream> get(String entry) throws IOException {
		return this.delegate.get(entry);
	}

	public Optional<InputStream> getCache(String entry) throws IOException {
		return this.delegate.getCache(entry);
	}

	public Map<String, InputStream> getAll(Pattern pattern) throws IOException {
		return this.delegate.getAll(pattern);
	}

	public OutputStream write(String entry) throws IOException {
		return this.delegate.write(entry);
	}

	public OutputStream writeCache(String entry) throws IOException {
		return this.delegate.writeCache(entry);
	}

	public void remove(String entry) throws IOException {
		this.delegate.remove(entry);
	}

	public void removeCache(String entry) throws IOException {
		this.delegate.removeCache(entry);
	}

	public void save() throws IOException {
		this.delegate.save();
	}

	public void saveTo(File target) throws IOException {
		this.delegate.saveTo(target);
	}

	public ReplayMetaData getMetaData() throws IOException {
		return this.delegate.getMetaData();
	}

	public void writeMetaData(PacketTypeRegistry registry, ReplayMetaData metaData) throws IOException {
		this.delegate.writeMetaData(registry, metaData);
	}

	public ReplayInputStream getPacketData(PacketTypeRegistry registry) throws IOException {
		return this.delegate.getPacketData(registry);
	}

	public ReplayOutputStream writePacketData() throws IOException {
		return this.delegate.writePacketData();
	}

	public Map<Integer, String> getResourcePackIndex() throws IOException {
		return this.delegate.getResourcePackIndex();
	}

	public void writeResourcePackIndex(Map<Integer, String> index) throws IOException {
		this.delegate.writeResourcePackIndex(index);
	}

	public Optional<InputStream> getResourcePack(String hash) throws IOException {
		return this.delegate.getResourcePack(hash);
	}

	public OutputStream writeResourcePack(String hash) throws IOException {
		return this.delegate.writeResourcePack(hash);
	}

	public Map<String, Timeline> getTimelines(PathingRegistry pathingRegistry) throws IOException {
		return this.delegate.getTimelines(pathingRegistry);
	}

	public void writeTimelines(PathingRegistry pathingRegistry, Map<String, Timeline> timelines) throws IOException {
		this.delegate.writeTimelines(pathingRegistry, timelines);
	}

	public Optional<BufferedImage> getThumb() throws IOException {
		return this.delegate.getThumb();
	}

	public void writeThumb(BufferedImage image) throws IOException {
		this.delegate.writeThumb(image);
	}

	public Optional<InputStream> getThumbBytes() throws IOException {
		return this.delegate.getThumbBytes();
	}

	public void writeThumbBytes(byte[] image) throws IOException {
		this.delegate.writeThumbBytes(image);
	}

	public Optional<Set<UUID>> getInvisiblePlayers() throws IOException {
		return this.delegate.getInvisiblePlayers();
	}

	public void writeInvisiblePlayers(Set<UUID> uuids) throws IOException {
		this.delegate.writeInvisiblePlayers(uuids);
	}

	public Optional<Set<Marker>> getMarkers() throws IOException {
		return this.delegate.getMarkers();
	}

	public void writeMarkers(Set<Marker> markers) throws IOException {
		this.delegate.writeMarkers(markers);
	}

	public Collection<ReplayAssetEntry> getAssets() throws IOException {
		return this.delegate.getAssets();
	}

	public Optional<InputStream> getAsset(UUID uuid) throws IOException {
		return this.delegate.getAsset(uuid);
	}

	public OutputStream writeAsset(ReplayAssetEntry asset) throws IOException {
		return this.delegate.writeAsset(asset);
	}

	public void removeAsset(UUID uuid) throws IOException {
		this.delegate.removeAsset(uuid);
	}

	public Collection<ModInfo> getModInfo() throws IOException {
		return this.delegate.getModInfo();
	}

	public void writeModInfo(Collection<ModInfo> modInfo) throws IOException {
		this.delegate.writeModInfo(modInfo);
	}

	public void close() throws IOException {
		this.delegate.close();
	}
}
