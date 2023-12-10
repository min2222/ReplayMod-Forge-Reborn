package com.replaymod.lib.de.javagl.jgltf.impl.v2;

import java.util.ArrayList;
import java.util.List;

public class Animation extends GlTFChildOfRootProperty {
	private List<AnimationChannel> channels;
	private List<AnimationSampler> samplers;

	public void setChannels(List<AnimationChannel> channels) {
		if (channels == null) {
			throw new NullPointerException("Invalid value for channels: " + channels + ", may not be null");
		} else if (channels.size() < 1) {
			throw new IllegalArgumentException("Number of channels elements is < 1");
		} else {
			this.channels = channels;
		}
	}

	public List<AnimationChannel> getChannels() {
		return this.channels;
	}

	public void addChannels(AnimationChannel element) {
		if (element == null) {
			throw new NullPointerException("The element may not be null");
		} else {
			List<AnimationChannel> oldList = this.channels;
			List<AnimationChannel> newList = new ArrayList();
			if (oldList != null) {
				newList.addAll(oldList);
			}

			newList.add(element);
			this.channels = newList;
		}
	}

	public void removeChannels(AnimationChannel element) {
		if (element == null) {
			throw new NullPointerException("The element may not be null");
		} else {
			List<AnimationChannel> oldList = this.channels;
			List<AnimationChannel> newList = new ArrayList();
			if (oldList != null) {
				newList.addAll(oldList);
			}

			newList.remove(element);
			this.channels = newList;
		}
	}

	public void setSamplers(List<AnimationSampler> samplers) {
		if (samplers == null) {
			throw new NullPointerException("Invalid value for samplers: " + samplers + ", may not be null");
		} else if (samplers.size() < 1) {
			throw new IllegalArgumentException("Number of samplers elements is < 1");
		} else {
			this.samplers = samplers;
		}
	}

	public List<AnimationSampler> getSamplers() {
		return this.samplers;
	}

	public void addSamplers(AnimationSampler element) {
		if (element == null) {
			throw new NullPointerException("The element may not be null");
		} else {
			List<AnimationSampler> oldList = this.samplers;
			List<AnimationSampler> newList = new ArrayList();
			if (oldList != null) {
				newList.addAll(oldList);
			}

			newList.add(element);
			this.samplers = newList;
		}
	}

	public void removeSamplers(AnimationSampler element) {
		if (element == null) {
			throw new NullPointerException("The element may not be null");
		} else {
			List<AnimationSampler> oldList = this.samplers;
			List<AnimationSampler> newList = new ArrayList();
			if (oldList != null) {
				newList.addAll(oldList);
			}

			newList.remove(element);
			this.samplers = newList;
		}
	}
}
