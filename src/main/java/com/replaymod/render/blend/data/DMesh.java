package com.replaymod.render.blend.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.blender.dna.CustomData;
import org.blender.dna.CustomDataLayer;
import org.blender.dna.Image;
import org.blender.dna.MEdge;
import org.blender.dna.MFace;
import org.blender.dna.MLoop;
import org.blender.dna.MLoopCol;
import org.blender.dna.MLoopUV;
import org.blender.dna.MPoly;
import org.blender.dna.MTexPoly;
import org.blender.dna.MVert;
import org.blender.dna.Material;
import org.blender.dna.Mesh;
import org.cakelab.blender.io.block.BlockCodes;
import org.cakelab.blender.nio.CArrayFacade;
import org.cakelab.blender.nio.CPointer;

import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.vector.Vector2f;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.vector.Vector3f;

public class DMesh {
	public final DId id;
	public final List<DMesh.Vertex> vertices;
	public final List<DMesh.Edge> edges;
	public final List<DMesh.Face> faces;
	public final List<DMesh.Loop> loops;
	public final List<DMesh.Poly> polys;
	public final List<DMaterial> materials;

	public DMesh() {
		this.id = new DId(BlockCodes.ID_ME);
		this.vertices = new ArrayList();
		this.edges = new ArrayList();
		this.faces = new ArrayList();
		this.loops = new ArrayList();
		this.polys = new ArrayList();
		this.materials = new ArrayList();
	}

	public CPointer<Mesh> serialize(Serializer serializer) throws IOException {
		return serializer.maybeMajor(this, this.id, Mesh.class, () -> {
			List<CPointer<Material>> materials = new ArrayList();
			List<CPointer<Image>> images = new ArrayList();
			Iterator var4 = this.materials.iterator();

			while (var4.hasNext()) {
				DMaterial material = (DMaterial) var4.next();
				materials.add(material.serialize(serializer));
				images.add(material.textures.isEmpty() ? null
						: ((DMaterial.DMTex) material.textures.get(0)).texture.image.serialize(serializer));
			}

			return (mesh) -> {
				mesh.setDrawflag(3);
				int var10002 = materials.size();
				Objects.requireNonNull(materials);
				CArrayFacade<CPointer<Material>> mat = serializer.writeDataPArray(Material.class, var10002,
						materials::get);
				mesh.setMat(mat);
				mesh.setTotcol((short) materials.size());
				CArrayFacade mPolys;
				CustomDataLayer eDataLayer;
				CustomData eData;
				if (!this.vertices.isEmpty()) {
					mPolys = serializer.writeData(MVert.class, this.vertices.size(), (ixxx, mVert) -> {
						((DMesh.Vertex) this.vertices.get(ixxx)).serialize(mVert);
					});
					eDataLayer = (CustomDataLayer) serializer.writeData(CustomDataLayer.class);
					eDataLayer.setType(0);
					eDataLayer.setData(mPolys.cast(Object.class));
					eData = mesh.getVdata();
					eData.setMaxlayer(1);
					eData.setTotlayer(1);
					eData.setLayers(eDataLayer.__io__addressof());
					mesh.setMvert(mPolys);
					mesh.setTotvert(this.vertices.size());
				}

				if (!this.edges.isEmpty()) {
					mPolys = serializer.writeData(MEdge.class, this.edges.size(), (ixxx, mEdge) -> {
						((DMesh.Edge) this.edges.get(ixxx)).serialize(mEdge);
					});
					eDataLayer = (CustomDataLayer) serializer.writeData(CustomDataLayer.class);
					eDataLayer.setType(3);
					eDataLayer.setData(mPolys.cast(Object.class));
					eData = mesh.getEdata();
					eData.setMaxlayer(1);
					eData.setTotlayer(1);
					eData.setLayers(eDataLayer.__io__addressof());
					mesh.setMedge(mPolys);
					mesh.setTotedge(this.edges.size());
					mesh.setMface(serializer.writeData(MFace.class, this.faces.size(), (ixxx, mFace) -> {
						((DMesh.Face) this.faces.get(ixxx)).serialize(mFace);
					}));
					mesh.setTotface(this.faces.size());
				}

				CArrayFacade mTexPolys;
				CArrayFacade dataLayers;
				if (!this.loops.isEmpty()) {
					mPolys = serializer.writeData(MLoop.class, this.loops.size(), (ixxx, mLoop) -> {
						((DMesh.Loop) this.loops.get(ixxx)).serialize(mLoop);
					});
					mTexPolys = serializer.writeData(MLoopUV.class, this.loops.size(), (ixxx, mLoopUV) -> {
						((DMesh.Loop) this.loops.get(ixxx)).serialize(mLoopUV);
					});
					dataLayers = serializer.writeData(MLoopCol.class, this.loops.size(), (ixxx, mLoopCol) -> {
						((DMesh.Loop) this.loops.get(ixxx)).serialize(mLoopCol);
					});
					CArrayFacade<CustomDataLayer> dataLayersx = serializer.writeData(CustomDataLayer.class, 3);
					int ixx = 0;
					int ix = ixx + 1;
					CustomDataLayer luvDataLayer = (CustomDataLayer) dataLayersx.get(ixx);
					luvDataLayer.setType(16);
					luvDataLayer.setData(mTexPolys.cast(Object.class));
					CustomDataLayer lcolDataLayer = (CustomDataLayer) dataLayersx.get(ix++);
					lcolDataLayer.setType(17);
					lcolDataLayer.setData(dataLayers.cast(Object.class));
					CustomDataLayer lDataLayer = (CustomDataLayer) dataLayersx.get(ix++);
					lDataLayer.setType(26);
					lDataLayer.setData(mPolys.cast(Object.class));
					CustomData lData = mesh.getLdata();
					lData.setMaxlayer(ix);
					lData.setTotlayer(ix);
					lData.setLayers(dataLayersx);
					mesh.setMloop(mPolys);
					mesh.setMloopuv(mTexPolys);
					mesh.setTotloop(this.loops.size());
				}

				if (!this.polys.isEmpty()) {
					mPolys = serializer.writeData(MPoly.class, this.polys.size(), (ixxx, mPoly) -> {
						((DMesh.Poly) this.polys.get(ixxx)).serialize(mPoly);
					});
					mTexPolys = serializer.writeData(MTexPoly.class, this.polys.size(), (ixxx, mTexPoly) -> {
						mTexPoly.setTpage((CPointer) images.get(((DMesh.Poly) this.polys.get(ixxx)).materialSlot));
					});
					dataLayers = serializer.writeData(CustomDataLayer.class, mTexPolys != null ? 2 : 1);
					int i = 0;
					CustomDataLayer pDataLayer;
					if (mTexPolys != null) {
						pDataLayer = (CustomDataLayer) dataLayers.get(i++);
						pDataLayer.getName().fromString("UVMap");
						pDataLayer.setType(15);
						pDataLayer.setData(mTexPolys.cast(Object.class));
					}

					pDataLayer = (CustomDataLayer) dataLayers.get(i++);
					pDataLayer.setType(25);
					pDataLayer.setData(mPolys.cast(Object.class));
					CustomData pData = mesh.getPdata();
					pData.setMaxlayer(i);
					pData.setTotlayer(i);
					pData.setLayers(dataLayers);
					mesh.setMpoly(mPolys);
					mesh.setMtpoly(mTexPolys);
					mesh.setTotpoly(this.polys.size());
				}

			};
		});
	}

	public void addTriangle(DMesh.Vertex v1, DMesh.Vertex v2, DMesh.Vertex v3, Vector2f uv1, Vector2f uv2, Vector2f uv3,
			int c1, int c2, int c3, int materialSlot) {
		int vOffset = this.vertices.size();
		int eOffset = this.edges.size();
		int lOffset = this.loops.size();
		this.vertices.add(v1);
		this.vertices.add(v2);
		this.vertices.add(v3);
		this.edges.add(new DMesh.Edge(vOffset, vOffset + 1));
		this.edges.add(new DMesh.Edge(vOffset + 1, vOffset + 2));
		this.edges.add(new DMesh.Edge(vOffset + 2, vOffset));
		this.loops.add(new DMesh.Loop(vOffset, eOffset, uv1.x, uv1.y, c1));
		this.loops.add(new DMesh.Loop(vOffset + 1, eOffset + 1, uv2.x, uv2.y, c2));
		this.loops.add(new DMesh.Loop(vOffset + 2, eOffset + 2, uv3.x, uv3.y, c3));
		this.polys.add(new DMesh.Poly(lOffset, 3, materialSlot));
	}

	public void addQuad(DMesh.Vertex v1, DMesh.Vertex v2, DMesh.Vertex v3, DMesh.Vertex v4, Vector2f uv1, Vector2f uv2,
			Vector2f uv3, Vector2f uv4, int c1, int c2, int c3, int c4, int materialSlot) {
		int vOffset = this.vertices.size();
		int eOffset = this.edges.size();
		int lOffset = this.loops.size();
		this.vertices.add(v1);
		this.vertices.add(v2);
		this.vertices.add(v3);
		this.vertices.add(v4);
		this.edges.add(new DMesh.Edge(vOffset, vOffset + 1));
		this.edges.add(new DMesh.Edge(vOffset + 1, vOffset + 2));
		this.edges.add(new DMesh.Edge(vOffset + 2, vOffset + 3));
		this.edges.add(new DMesh.Edge(vOffset + 3, vOffset));
		this.loops.add(new DMesh.Loop(vOffset, eOffset, uv1.x, uv1.y, c1));
		this.loops.add(new DMesh.Loop(vOffset + 1, eOffset + 1, uv2.x, uv2.y, c2));
		this.loops.add(new DMesh.Loop(vOffset + 2, eOffset + 2, uv3.x, uv3.y, c3));
		this.loops.add(new DMesh.Loop(vOffset + 3, eOffset + 3, uv4.x, uv4.y, c4));
		this.polys.add(new DMesh.Poly(lOffset, 4, materialSlot));
	}

	public float getSizeX() {
		float minX = Float.POSITIVE_INFINITY;
		float maxX = Float.NEGATIVE_INFINITY;
		Iterator var3 = this.vertices.iterator();

		while (var3.hasNext()) {
			DMesh.Vertex vertex = (DMesh.Vertex) var3.next();
			if (vertex.pos.x < minX) {
				minX = vertex.pos.x;
			}

			if (vertex.pos.x > maxX) {
				maxX = vertex.pos.x;
			}
		}

		return maxX - minX;
	}

	public boolean hasZeroLengthEdge(float delta) {
		float deltaSquared = delta * delta;
		Vector3f dst = new Vector3f();
		Iterator var4 = this.edges.iterator();

		DMesh.Vertex v1;
		DMesh.Vertex v2;
		do {
			if (!var4.hasNext()) {
				return false;
			}

			DMesh.Edge edge = (DMesh.Edge) var4.next();
			v1 = (DMesh.Vertex) this.vertices.get(edge.v1);
			v2 = (DMesh.Vertex) this.vertices.get(edge.v2);
		} while (!(Vector3f.sub(v1.pos, v2.pos, dst).lengthSquared() < deltaSquared));

		return true;
	}

	public static class Edge {
		public int v1;
		public int v2;

		public Edge(int v1, int v2) {
			this.v1 = v1;
			this.v2 = v2;
		}

		public void serialize(MEdge mEdge) throws IOException {
			mEdge.setV1(this.v1);
			mEdge.setV2(this.v2);
		}
	}

	public static class Loop {
		public int vertex;
		public int edge;
		public float u;
		public float v;
		public int col;

		public Loop(int vertex, int edge, float u, float v, int col) {
			this.vertex = vertex;
			this.edge = edge;
			this.u = u;
			this.v = v;
			this.col = col;
		}

		public void serialize(MLoop mLoop) throws IOException {
			mLoop.setV(this.vertex);
			mLoop.setE(this.edge);
		}

		public void serialize(MLoopUV mLoop) throws IOException {
			CArrayFacade<Float> uv = mLoop.getUv();
			uv.set(0, this.u);
			uv.set(1, this.v);
		}

		public void serialize(MLoopCol mLoop) throws IOException {
			mLoop.setR((byte) (this.col & 255));
			mLoop.setG((byte) (this.col >> 8 & 255));
			mLoop.setB((byte) (this.col >> 16 & 255));
			mLoop.setA((byte) (this.col >> 24 & 255));
		}
	}

	public static class Poly {
		public int loopStart;
		public int size;
		public short materialSlot;

		public Poly(int loopStart, int size, int materialSlot) {
			this.loopStart = loopStart;
			this.size = size;
			this.materialSlot = (short) materialSlot;
		}

		public void serialize(MPoly mPoly) throws IOException {
			mPoly.setLoopstart(this.loopStart);
			mPoly.setTotloop(this.size);
			mPoly.setMat_nr(this.materialSlot);
		}
	}

	public static class Vertex {
		public Vector3f pos;
		public short normX;
		public short normY;
		public short normZ;

		public Vertex(float x, float y, float z) {
			this.pos = new Vector3f(x, y, z);
		}

		public void serialize(MVert mVert) throws IOException {
			CArrayFacade<Float> pos = mVert.getCo();
			pos.set(0, this.pos.getX());
			pos.set(1, this.pos.getY());
			pos.set(2, this.pos.getZ());
			CArrayFacade<Short> norm = mVert.getNo();
			norm.set(0, this.normX);
			norm.set(1, this.normY);
			norm.set(2, this.normZ);
		}
	}

	public static class Face {
		public int v1;
		public int v2;
		public int v3;
		public int v4;

		public Face(int v1, int v2, int v3, int v4) {
			this.v1 = v1;
			this.v2 = v2;
			this.v3 = v3;
			this.v4 = v4;
		}

		public void serialize(MFace mFace) throws IOException {
			mFace.setV1(this.v1);
			mFace.setV2(this.v2);
			mFace.setV3(this.v3);
			mFace.setV4(this.v4);
		}
	}
}
