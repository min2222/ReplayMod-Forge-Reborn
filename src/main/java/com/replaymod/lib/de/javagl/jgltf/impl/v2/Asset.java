package com.replaymod.lib.de.javagl.jgltf.impl.v2;

public class Asset extends GlTFProperty {
	private String copyright;
	private String generator;
	private String version;
	private String minVersion;

	public void setCopyright(String copyright) {
		if (copyright == null) {
			this.copyright = copyright;
		} else {
			this.copyright = copyright;
		}
	}

	public String getCopyright() {
		return this.copyright;
	}

	public void setGenerator(String generator) {
		if (generator == null) {
			this.generator = generator;
		} else {
			this.generator = generator;
		}
	}

	public String getGenerator() {
		return this.generator;
	}

	public void setVersion(String version) {
		if (version == null) {
			throw new NullPointerException("Invalid value for version: " + version + ", may not be null");
		} else {
			this.version = version;
		}
	}

	public String getVersion() {
		return this.version;
	}

	public void setMinVersion(String minVersion) {
		if (minVersion == null) {
			this.minVersion = minVersion;
		} else {
			this.minVersion = minVersion;
		}
	}

	public String getMinVersion() {
		return this.minVersion;
	}
}
