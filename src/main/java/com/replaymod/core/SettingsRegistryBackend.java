package com.replaymod.core;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.replaymod.core.events.SettingsChangedCallback;
import com.replaymod.core.utils.Utils;
import com.replaymod.core.versions.MCVer;

import net.minecraft.client.Minecraft;

class SettingsRegistryBackend {
	private static final Logger LOGGER = LogManager.getLogger();
	private final Map<SettingsRegistry.SettingKey<?>, Object> settings;
	private final Path configFile;

	SettingsRegistryBackend(Map<SettingsRegistry.SettingKey<?>, Object> settings) {
		this.configFile = MCVer.getMinecraft().gameDirectory.toPath().resolve("config/replaymod.json");
		this.settings = settings;
	}

	public void register() {
		this.load(true);

		try {
			this.registerWatcher();
		} catch (IOException var2) {
			LOGGER.warn("Failed to setup file watcher for {}, live-reloading is disabled. Cause: {}", this.configFile,
					var2);
		}

	}

	private void load(boolean createIfMissingOrBroken) {
		if (!Files.exists(this.configFile, new LinkOption[0])) {
			if (createIfMissingOrBroken) {
				this.save();
			}

		} else {
			String config;
			try {
				config = new String(Files.readAllBytes(this.configFile), StandardCharsets.UTF_8);
			} catch (IOException var13) {
				var13.printStackTrace();
				return;
			}

			Gson gson = new Gson();
			JsonObject root = (JsonObject) gson.fromJson(config, JsonObject.class);
			if (root == null) {
				LOGGER.error("Config file {} appears corrupted: {}", this.configFile, config);
				if (createIfMissingOrBroken) {
					this.save();
				}

			} else {
				Iterator var5 = this.settings.entrySet().iterator();

				while (true) {
					Entry entry;
					String valueStr;
					Stream var10000;
					do {
						SettingsRegistry.SettingKey key;
						JsonPrimitive value;
						do {
							do {
								JsonElement valueElem;
								do {
									do {
										JsonElement category;
										do {
											do {
												if (!var5.hasNext()) {
													return;
												}

												entry = (Entry) var5.next();
												key = (SettingsRegistry.SettingKey) entry.getKey();
												category = root.get(key.getCategory());
											} while (category == null);
										} while (!category.isJsonObject());

										valueElem = category.getAsJsonObject().get(key.getKey());
									} while (valueElem == null);
								} while (!valueElem.isJsonPrimitive());

								value = valueElem.getAsJsonPrimitive();
								if (key.getDefault() instanceof Boolean && value.isBoolean()) {
									entry.setValue(value.getAsBoolean());
								}

								if (key.getDefault() instanceof Integer && value.isNumber()) {
									entry.setValue(value.getAsNumber().intValue());
								}

								if (key.getDefault() instanceof Double && value.isNumber()) {
									entry.setValue(value.getAsNumber().doubleValue());
								}
							} while (!(key.getDefault() instanceof String));
						} while (!value.isString());

						valueStr = value.getAsString();
						if (!(entry instanceof SettingsRegistry.MultipleChoiceSettingKey)) {
							break;
						}

						List<String> choices = ((SettingsRegistry.MultipleChoiceSettingKey) entry).getChoices();
						var10000 = choices.stream();
						Objects.requireNonNull(valueStr);
					} while (var10000.noneMatch(valueStr::equals));

					entry.setValue(valueStr);
				}
			}
		}
	}

	private void registerWatcher() throws IOException {
		WatchService watchService = this.configFile.getFileSystem().newWatchService();
		this.configFile.getParent().register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
				StandardWatchEventKinds.ENTRY_MODIFY);
		Thread thread = new Thread(() -> {
			WatchKey nextKey;
			do {
				try {
					nextKey = watchService.take();
				} catch (InterruptedException var7) {
					return;
				}

				Iterator var3 = nextKey.pollEvents().iterator();

				while (var3.hasNext()) {
					WatchEvent<?> event = (WatchEvent) var3.next();
					Kind<?> kind = event.kind();
					if (kind != StandardWatchEventKinds.OVERFLOW) {
						Path fileName = (Path) event.context();
						if (fileName.equals(this.configFile.getFileName())) {
							Minecraft.getInstance().tell(this::reload);
						}
					}
				}
			} while (nextKey.reset());

		});
		thread.setName("replaymod-config-watcher");
		thread.setDaemon(true);
		thread.start();
	}

	private void reload() {
		this.load(false);
		SettingsRegistry settingsRegistry = ReplayMod.instance.getSettingsRegistry();
		Iterator var2 = this.settings.keySet().iterator();

		while (var2.hasNext()) {
			SettingsRegistry.SettingKey<?> key = (SettingsRegistry.SettingKey) var2.next();
			((SettingsChangedCallback) SettingsChangedCallback.EVENT.invoker()).onSettingsChanged(settingsRegistry,
					key);
		}

	}

	public void register(SettingsRegistry.SettingKey<?> key) {
	}

	public <T> void update(SettingsRegistry.SettingKey<T> key, T value) {
	}

	public void save() {
		JsonObject root = new JsonObject();
		Iterator var2 = this.settings.entrySet().iterator();

		while (true) {
			SettingsRegistry.SettingKey key;
			JsonObject category;
			do {
				Object value;
				do {
					if (!var2.hasNext()) {
						Gson gson = (new GsonBuilder()).setPrettyPrinting().create();
						String config = gson.toJson(root);

						try {
							Utils.ensureDirectoryExists(this.configFile.getParent());
							Files.write(this.configFile, config.getBytes(StandardCharsets.UTF_8), new OpenOption[0]);
						} catch (IOException var11) {
							var11.printStackTrace();
						}

						return;
					}

					Entry<SettingsRegistry.SettingKey<?>, Object> entry = (Entry) var2.next();
					key = (SettingsRegistry.SettingKey) entry.getKey();
					category = root.getAsJsonObject(key.getCategory());
					if (category == null) {
						category = new JsonObject();
						root.add(key.getCategory(), category);
					}

					value = entry.getValue();
					if (value instanceof Boolean) {
						category.addProperty(key.getKey(), (Boolean) value);
					}

					if (value instanceof Number) {
						category.addProperty(key.getKey(), (Number) value);
					}
				} while (!(value instanceof String));

				category.addProperty(key.getKey(), (String) value);
			} while (!(key instanceof SettingsRegistry.MultipleChoiceSettingKey));

			List<String> choices = ((SettingsRegistry.MultipleChoiceSettingKey) key).getChoices();
			JsonArray array = new JsonArray();
			Iterator var9 = choices.iterator();

			while (var9.hasNext()) {
				String choice = (String) var9.next();
				array.add(choice);
			}

			category.add(key.getKey() + "_valid_values", array);
		}
	}
}
