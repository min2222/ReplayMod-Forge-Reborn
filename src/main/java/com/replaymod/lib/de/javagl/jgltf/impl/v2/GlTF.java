package com.replaymod.lib.de.javagl.jgltf.impl.v2;

import java.util.ArrayList;
import java.util.List;

public class GlTF extends GlTFProperty {
	private List<String> extensionsUsed;
	private List<String> extensionsRequired;
	private List<Accessor> accessors;
	private List<Animation> animations;
	private Asset asset;
	private List<Buffer> buffers;
	private List<BufferView> bufferViews;
	private List<Camera> cameras;
	private List<Image> images;
	private List<Material> materials;
	private List<Mesh> meshes;
	private List<Node> nodes;
	private List<Sampler> samplers;
	private Integer scene;
	private List<Scene> scenes;
	private List<Skin> skins;
	private List<Texture> textures;

	public void setExtensionsUsed(List<String> extensionsUsed) {
		if (extensionsUsed == null) {
			this.extensionsUsed = extensionsUsed;
		} else if (extensionsUsed.size() < 1) {
			throw new IllegalArgumentException("Number of extensionsUsed elements is < 1");
		} else {
			this.extensionsUsed = extensionsUsed;
		}
	}

	public List<String> getExtensionsUsed() {
		return this.extensionsUsed;
	}

	public void addExtensionsUsed(String element) {
		if (element == null) {
			throw new NullPointerException("The element may not be null");
		} else {
			List<String> oldList = this.extensionsUsed;
			List<String> newList = new ArrayList();
			if (oldList != null) {
				newList.addAll(oldList);
			}

			newList.add(element);
			this.extensionsUsed = newList;
		}
	}

	public void removeExtensionsUsed(String element) {
		if (element == null) {
			throw new NullPointerException("The element may not be null");
		} else {
			List<String> oldList = this.extensionsUsed;
			List<String> newList = new ArrayList();
			if (oldList != null) {
				newList.addAll(oldList);
			}

			newList.remove(element);
			if (newList.isEmpty()) {
				this.extensionsUsed = null;
			} else {
				this.extensionsUsed = newList;
			}

		}
	}

	public void setExtensionsRequired(List<String> extensionsRequired) {
		if (extensionsRequired == null) {
			this.extensionsRequired = extensionsRequired;
		} else if (extensionsRequired.size() < 1) {
			throw new IllegalArgumentException("Number of extensionsRequired elements is < 1");
		} else {
			this.extensionsRequired = extensionsRequired;
		}
	}

	public List<String> getExtensionsRequired() {
		return this.extensionsRequired;
	}

	public void addExtensionsRequired(String element) {
		if (element == null) {
			throw new NullPointerException("The element may not be null");
		} else {
			List<String> oldList = this.extensionsRequired;
			List<String> newList = new ArrayList();
			if (oldList != null) {
				newList.addAll(oldList);
			}

			newList.add(element);
			this.extensionsRequired = newList;
		}
	}

	public void removeExtensionsRequired(String element) {
		if (element == null) {
			throw new NullPointerException("The element may not be null");
		} else {
			List<String> oldList = this.extensionsRequired;
			List<String> newList = new ArrayList();
			if (oldList != null) {
				newList.addAll(oldList);
			}

			newList.remove(element);
			if (newList.isEmpty()) {
				this.extensionsRequired = null;
			} else {
				this.extensionsRequired = newList;
			}

		}
	}

	public void setAccessors(List<Accessor> accessors) {
		if (accessors == null) {
			this.accessors = accessors;
		} else if (accessors.size() < 1) {
			throw new IllegalArgumentException("Number of accessors elements is < 1");
		} else {
			this.accessors = accessors;
		}
	}

	public List<Accessor> getAccessors() {
		return this.accessors;
	}

	public void addAccessors(Accessor element) {
		if (element == null) {
			throw new NullPointerException("The element may not be null");
		} else {
			List<Accessor> oldList = this.accessors;
			List<Accessor> newList = new ArrayList();
			if (oldList != null) {
				newList.addAll(oldList);
			}

			newList.add(element);
			this.accessors = newList;
		}
	}

	public void removeAccessors(Accessor element) {
		if (element == null) {
			throw new NullPointerException("The element may not be null");
		} else {
			List<Accessor> oldList = this.accessors;
			List<Accessor> newList = new ArrayList();
			if (oldList != null) {
				newList.addAll(oldList);
			}

			newList.remove(element);
			if (newList.isEmpty()) {
				this.accessors = null;
			} else {
				this.accessors = newList;
			}

		}
	}

	public void setAnimations(List<Animation> animations) {
		if (animations == null) {
			this.animations = animations;
		} else if (animations.size() < 1) {
			throw new IllegalArgumentException("Number of animations elements is < 1");
		} else {
			this.animations = animations;
		}
	}

	public List<Animation> getAnimations() {
		return this.animations;
	}

	public void addAnimations(Animation element) {
		if (element == null) {
			throw new NullPointerException("The element may not be null");
		} else {
			List<Animation> oldList = this.animations;
			List<Animation> newList = new ArrayList();
			if (oldList != null) {
				newList.addAll(oldList);
			}

			newList.add(element);
			this.animations = newList;
		}
	}

	public void removeAnimations(Animation element) {
		if (element == null) {
			throw new NullPointerException("The element may not be null");
		} else {
			List<Animation> oldList = this.animations;
			List<Animation> newList = new ArrayList();
			if (oldList != null) {
				newList.addAll(oldList);
			}

			newList.remove(element);
			if (newList.isEmpty()) {
				this.animations = null;
			} else {
				this.animations = newList;
			}

		}
	}

	public void setAsset(Asset asset) {
		if (asset == null) {
			throw new NullPointerException("Invalid value for asset: " + asset + ", may not be null");
		} else {
			this.asset = asset;
		}
	}

	public Asset getAsset() {
		return this.asset;
	}

	public void setBuffers(List<Buffer> buffers) {
		if (buffers == null) {
			this.buffers = buffers;
		} else if (buffers.size() < 1) {
			throw new IllegalArgumentException("Number of buffers elements is < 1");
		} else {
			this.buffers = buffers;
		}
	}

	public List<Buffer> getBuffers() {
		return this.buffers;
	}

	public void addBuffers(Buffer element) {
		if (element == null) {
			throw new NullPointerException("The element may not be null");
		} else {
			List<Buffer> oldList = this.buffers;
			List<Buffer> newList = new ArrayList();
			if (oldList != null) {
				newList.addAll(oldList);
			}

			newList.add(element);
			this.buffers = newList;
		}
	}

	public void removeBuffers(Buffer element) {
		if (element == null) {
			throw new NullPointerException("The element may not be null");
		} else {
			List<Buffer> oldList = this.buffers;
			List<Buffer> newList = new ArrayList();
			if (oldList != null) {
				newList.addAll(oldList);
			}

			newList.remove(element);
			if (newList.isEmpty()) {
				this.buffers = null;
			} else {
				this.buffers = newList;
			}

		}
	}

	public void setBufferViews(List<BufferView> bufferViews) {
		if (bufferViews == null) {
			this.bufferViews = bufferViews;
		} else if (bufferViews.size() < 1) {
			throw new IllegalArgumentException("Number of bufferViews elements is < 1");
		} else {
			this.bufferViews = bufferViews;
		}
	}

	public List<BufferView> getBufferViews() {
		return this.bufferViews;
	}

	public void addBufferViews(BufferView element) {
		if (element == null) {
			throw new NullPointerException("The element may not be null");
		} else {
			List<BufferView> oldList = this.bufferViews;
			List<BufferView> newList = new ArrayList();
			if (oldList != null) {
				newList.addAll(oldList);
			}

			newList.add(element);
			this.bufferViews = newList;
		}
	}

	public void removeBufferViews(BufferView element) {
		if (element == null) {
			throw new NullPointerException("The element may not be null");
		} else {
			List<BufferView> oldList = this.bufferViews;
			List<BufferView> newList = new ArrayList();
			if (oldList != null) {
				newList.addAll(oldList);
			}

			newList.remove(element);
			if (newList.isEmpty()) {
				this.bufferViews = null;
			} else {
				this.bufferViews = newList;
			}

		}
	}

	public void setCameras(List<Camera> cameras) {
		if (cameras == null) {
			this.cameras = cameras;
		} else if (cameras.size() < 1) {
			throw new IllegalArgumentException("Number of cameras elements is < 1");
		} else {
			this.cameras = cameras;
		}
	}

	public List<Camera> getCameras() {
		return this.cameras;
	}

	public void addCameras(Camera element) {
		if (element == null) {
			throw new NullPointerException("The element may not be null");
		} else {
			List<Camera> oldList = this.cameras;
			List<Camera> newList = new ArrayList();
			if (oldList != null) {
				newList.addAll(oldList);
			}

			newList.add(element);
			this.cameras = newList;
		}
	}

	public void removeCameras(Camera element) {
		if (element == null) {
			throw new NullPointerException("The element may not be null");
		} else {
			List<Camera> oldList = this.cameras;
			List<Camera> newList = new ArrayList();
			if (oldList != null) {
				newList.addAll(oldList);
			}

			newList.remove(element);
			if (newList.isEmpty()) {
				this.cameras = null;
			} else {
				this.cameras = newList;
			}

		}
	}

	public void setImages(List<Image> images) {
		if (images == null) {
			this.images = images;
		} else if (images.size() < 1) {
			throw new IllegalArgumentException("Number of images elements is < 1");
		} else {
			this.images = images;
		}
	}

	public List<Image> getImages() {
		return this.images;
	}

	public void addImages(Image element) {
		if (element == null) {
			throw new NullPointerException("The element may not be null");
		} else {
			List<Image> oldList = this.images;
			List<Image> newList = new ArrayList();
			if (oldList != null) {
				newList.addAll(oldList);
			}

			newList.add(element);
			this.images = newList;
		}
	}

	public void removeImages(Image element) {
		if (element == null) {
			throw new NullPointerException("The element may not be null");
		} else {
			List<Image> oldList = this.images;
			List<Image> newList = new ArrayList();
			if (oldList != null) {
				newList.addAll(oldList);
			}

			newList.remove(element);
			if (newList.isEmpty()) {
				this.images = null;
			} else {
				this.images = newList;
			}

		}
	}

	public void setMaterials(List<Material> materials) {
		if (materials == null) {
			this.materials = materials;
		} else if (materials.size() < 1) {
			throw new IllegalArgumentException("Number of materials elements is < 1");
		} else {
			this.materials = materials;
		}
	}

	public List<Material> getMaterials() {
		return this.materials;
	}

	public void addMaterials(Material element) {
		if (element == null) {
			throw new NullPointerException("The element may not be null");
		} else {
			List<Material> oldList = this.materials;
			List<Material> newList = new ArrayList();
			if (oldList != null) {
				newList.addAll(oldList);
			}

			newList.add(element);
			this.materials = newList;
		}
	}

	public void removeMaterials(Material element) {
		if (element == null) {
			throw new NullPointerException("The element may not be null");
		} else {
			List<Material> oldList = this.materials;
			List<Material> newList = new ArrayList();
			if (oldList != null) {
				newList.addAll(oldList);
			}

			newList.remove(element);
			if (newList.isEmpty()) {
				this.materials = null;
			} else {
				this.materials = newList;
			}

		}
	}

	public void setMeshes(List<Mesh> meshes) {
		if (meshes == null) {
			this.meshes = meshes;
		} else if (meshes.size() < 1) {
			throw new IllegalArgumentException("Number of meshes elements is < 1");
		} else {
			this.meshes = meshes;
		}
	}

	public List<Mesh> getMeshes() {
		return this.meshes;
	}

	public void addMeshes(Mesh element) {
		if (element == null) {
			throw new NullPointerException("The element may not be null");
		} else {
			List<Mesh> oldList = this.meshes;
			List<Mesh> newList = new ArrayList();
			if (oldList != null) {
				newList.addAll(oldList);
			}

			newList.add(element);
			this.meshes = newList;
		}
	}

	public void removeMeshes(Mesh element) {
		if (element == null) {
			throw new NullPointerException("The element may not be null");
		} else {
			List<Mesh> oldList = this.meshes;
			List<Mesh> newList = new ArrayList();
			if (oldList != null) {
				newList.addAll(oldList);
			}

			newList.remove(element);
			if (newList.isEmpty()) {
				this.meshes = null;
			} else {
				this.meshes = newList;
			}

		}
	}

	public void setNodes(List<Node> nodes) {
		if (nodes == null) {
			this.nodes = nodes;
		} else if (nodes.size() < 1) {
			throw new IllegalArgumentException("Number of nodes elements is < 1");
		} else {
			this.nodes = nodes;
		}
	}

	public List<Node> getNodes() {
		return this.nodes;
	}

	public void addNodes(Node element) {
		if (element == null) {
			throw new NullPointerException("The element may not be null");
		} else {
			List<Node> oldList = this.nodes;
			List<Node> newList = new ArrayList();
			if (oldList != null) {
				newList.addAll(oldList);
			}

			newList.add(element);
			this.nodes = newList;
		}
	}

	public void removeNodes(Node element) {
		if (element == null) {
			throw new NullPointerException("The element may not be null");
		} else {
			List<Node> oldList = this.nodes;
			List<Node> newList = new ArrayList();
			if (oldList != null) {
				newList.addAll(oldList);
			}

			newList.remove(element);
			if (newList.isEmpty()) {
				this.nodes = null;
			} else {
				this.nodes = newList;
			}

		}
	}

	public void setSamplers(List<Sampler> samplers) {
		if (samplers == null) {
			this.samplers = samplers;
		} else if (samplers.size() < 1) {
			throw new IllegalArgumentException("Number of samplers elements is < 1");
		} else {
			this.samplers = samplers;
		}
	}

	public List<Sampler> getSamplers() {
		return this.samplers;
	}

	public void addSamplers(Sampler element) {
		if (element == null) {
			throw new NullPointerException("The element may not be null");
		} else {
			List<Sampler> oldList = this.samplers;
			List<Sampler> newList = new ArrayList();
			if (oldList != null) {
				newList.addAll(oldList);
			}

			newList.add(element);
			this.samplers = newList;
		}
	}

	public void removeSamplers(Sampler element) {
		if (element == null) {
			throw new NullPointerException("The element may not be null");
		} else {
			List<Sampler> oldList = this.samplers;
			List<Sampler> newList = new ArrayList();
			if (oldList != null) {
				newList.addAll(oldList);
			}

			newList.remove(element);
			if (newList.isEmpty()) {
				this.samplers = null;
			} else {
				this.samplers = newList;
			}

		}
	}

	public void setScene(Integer scene) {
		if (scene == null) {
			this.scene = scene;
		} else {
			this.scene = scene;
		}
	}

	public Integer getScene() {
		return this.scene;
	}

	public void setScenes(List<Scene> scenes) {
		if (scenes == null) {
			this.scenes = scenes;
		} else if (scenes.size() < 1) {
			throw new IllegalArgumentException("Number of scenes elements is < 1");
		} else {
			this.scenes = scenes;
		}
	}

	public List<Scene> getScenes() {
		return this.scenes;
	}

	public void addScenes(Scene element) {
		if (element == null) {
			throw new NullPointerException("The element may not be null");
		} else {
			List<Scene> oldList = this.scenes;
			List<Scene> newList = new ArrayList();
			if (oldList != null) {
				newList.addAll(oldList);
			}

			newList.add(element);
			this.scenes = newList;
		}
	}

	public void removeScenes(Scene element) {
		if (element == null) {
			throw new NullPointerException("The element may not be null");
		} else {
			List<Scene> oldList = this.scenes;
			List<Scene> newList = new ArrayList();
			if (oldList != null) {
				newList.addAll(oldList);
			}

			newList.remove(element);
			if (newList.isEmpty()) {
				this.scenes = null;
			} else {
				this.scenes = newList;
			}

		}
	}

	public void setSkins(List<Skin> skins) {
		if (skins == null) {
			this.skins = skins;
		} else if (skins.size() < 1) {
			throw new IllegalArgumentException("Number of skins elements is < 1");
		} else {
			this.skins = skins;
		}
	}

	public List<Skin> getSkins() {
		return this.skins;
	}

	public void addSkins(Skin element) {
		if (element == null) {
			throw new NullPointerException("The element may not be null");
		} else {
			List<Skin> oldList = this.skins;
			List<Skin> newList = new ArrayList();
			if (oldList != null) {
				newList.addAll(oldList);
			}

			newList.add(element);
			this.skins = newList;
		}
	}

	public void removeSkins(Skin element) {
		if (element == null) {
			throw new NullPointerException("The element may not be null");
		} else {
			List<Skin> oldList = this.skins;
			List<Skin> newList = new ArrayList();
			if (oldList != null) {
				newList.addAll(oldList);
			}

			newList.remove(element);
			if (newList.isEmpty()) {
				this.skins = null;
			} else {
				this.skins = newList;
			}

		}
	}

	public void setTextures(List<Texture> textures) {
		if (textures == null) {
			this.textures = textures;
		} else if (textures.size() < 1) {
			throw new IllegalArgumentException("Number of textures elements is < 1");
		} else {
			this.textures = textures;
		}
	}

	public List<Texture> getTextures() {
		return this.textures;
	}

	public void addTextures(Texture element) {
		if (element == null) {
			throw new NullPointerException("The element may not be null");
		} else {
			List<Texture> oldList = this.textures;
			List<Texture> newList = new ArrayList();
			if (oldList != null) {
				newList.addAll(oldList);
			}

			newList.add(element);
			this.textures = newList;
		}
	}

	public void removeTextures(Texture element) {
		if (element == null) {
			throw new NullPointerException("The element may not be null");
		} else {
			List<Texture> oldList = this.textures;
			List<Texture> newList = new ArrayList();
			if (oldList != null) {
				newList.addAll(oldList);
			}

			newList.remove(element);
			if (newList.isEmpty()) {
				this.textures = null;
			} else {
				this.textures = newList;
			}

		}
	}
}
