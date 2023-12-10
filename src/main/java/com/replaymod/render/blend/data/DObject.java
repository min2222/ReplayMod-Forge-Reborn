package com.replaymod.render.blend.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.blender.dna.AnimData;
import org.blender.dna.BlenderObject;
import org.blender.dna.Material;
import org.blender.dna.Mesh;
import org.blender.dna.bAction;
import org.blender.dna.bConstraint;
import org.blender.dna.bTrackToConstraint;
import org.cakelab.blender.io.block.BlockCodes;
import org.cakelab.blender.nio.CArrayFacade;
import org.cakelab.blender.nio.CPointer;

import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.vector.Quaternion;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.vector.Vector3f;
import com.replaymod.render.blend.Util;

public class DObject {
	public final DId id;
	public Vector3f loc;
	public Vector3f scale;
	public Quaternion rot;
	private DObject parent;
	private List<DObject> children;
	private List<DObject> unmodifiableChildren;
	public DAction action;
	public DMesh mesh;
	public DObject.Type type;
	public int layers;
	public DObject pointAt;
	public int lastFrame;
	private int lastVisibleFrame;

	public DObject(DObject.Type type) {
		this.id = new DId(BlockCodes.ID_OB);
		this.loc = new Vector3f(0.0F, 0.0F, 0.0F);
		this.scale = new Vector3f(1.0F, 1.0F, 1.0F);
		this.rot = new Quaternion();
		this.children = new ArrayList();
		this.layers = 1;
		this.lastVisibleFrame = -2;
		this.type = type;
	}

	public DObject(DMesh mesh) {
		this(DObject.Type.OB_MESH);
		this.mesh = mesh;
	}

	public boolean isValid() {
		return true;
	}

	public void setVisible(int frame) {
		if (frame < this.lastVisibleFrame) {
			throw new IllegalStateException("Already at frame " + this.lastVisibleFrame);
		} else {
			if (this.lastVisibleFrame < frame - 1) {
				if (this.lastVisibleFrame < 0) {
					if (frame > 0) {
						this.keyframe("hide", 0, 0, 1.0F);
					}
				} else {
					this.keyframe("hide", 0, this.lastVisibleFrame + 1, 1.0F);
				}

				this.keyframe("hide", 0, frame, 0.0F);
			}

			this.lastVisibleFrame = frame;
		}
	}

	public void setParent(DObject parent) {
		if (this.parent != null) {
			this.parent.children.remove(this);
		}

		this.parent = parent;
		parent.children.add(this);
	}

	public DObject getParent() {
		return this.parent;
	}

	public List<DObject> getChildren() {
		if (this.unmodifiableChildren == null) {
			this.unmodifiableChildren = Collections.unmodifiableList(this.children);
		}

		return this.unmodifiableChildren;
	}

	public void keyframeLocRotScale(int frame) {
		this.keyframeLoc(frame);
		this.keyframeRot(frame);
		this.keyframeScale(frame);
	}

	public void keyframeLoc(int frame) {
		this.keyframe("location", frame, this.loc);
	}

	public void keyframeRot(int frame) {
		this.keyframe("rotation_quaternion", frame, this.rot);
	}

	public void keyframeScale(int frame) {
		this.keyframe("scale", frame, this.scale);
	}

	public void keyframe(String rnaPath, int frame, Quaternion q) {
		this.keyframe(rnaPath, 0, frame, q.w);
		this.keyframe(rnaPath, 1, frame, q.x);
		this.keyframe(rnaPath, 2, frame, q.y);
		this.keyframe(rnaPath, 3, frame, q.z);
	}

	public void keyframe(String rnaPath, int frame, Vector3f vec) {
		this.keyframe(rnaPath, 0, frame, vec.x);
		this.keyframe(rnaPath, 1, frame, vec.y);
		this.keyframe(rnaPath, 2, frame, vec.z);
	}

	public void keyframe(String rnaPath, int rnaArrayIndex, int frame, float value) {
		if (frame >= 0) {
			DAction.DKeyframe keyframe = new DAction.DKeyframe();
			keyframe.frame = frame;
			keyframe.value = value;
			this.keyframe(rnaPath, rnaArrayIndex, keyframe, rnaPath.startsWith("hide"));
			if (rnaPath.equals("hide")) {
				this.keyframe("hide_render", rnaArrayIndex, frame, value);
			}

		}
	}

	public void keyframe(String rnaPath, int rnaArrayIndex, DAction.DKeyframe keyframe, boolean constant) {
		if (this.action == null) {
			this.action = new DAction();
		}

		DAction.DFCurve theCurve = null;
		Iterator var6 = this.action.curves.iterator();

		while (var6.hasNext()) {
			DAction.DFCurve curve = (DAction.DFCurve) var6.next();
			if (curve.rnaArrayIndex == rnaArrayIndex && curve.rnaPath.equals(rnaPath)) {
				theCurve = curve;
				break;
			}
		}

		if (theCurve == null) {
			theCurve = new DAction.DFCurve();
			theCurve.rnaPath = rnaPath;
			theCurve.rnaArrayIndex = rnaArrayIndex;
			this.action.curves.add(theCurve);
		}

		keyframe.interpolationType = constant ? DAction.InterpolationType.CONSTANT : DAction.InterpolationType.LINEAR;
		DAction.DKeyframe prev;
		if (constant) {
			if (!theCurve.keyframes.isEmpty()) {
				prev = (DAction.DKeyframe) theCurve.keyframes.get(theCurve.keyframes.size() - 1);
				if ((double) Math.abs(prev.value - keyframe.value) < 1.0E-4D) {
					return;
				}
			}
		} else if (theCurve.keyframes.size() >= 2) {
			prev = (DAction.DKeyframe) theCurve.keyframes.get(theCurve.keyframes.size() - 1);
			DAction.DKeyframe prev2 = (DAction.DKeyframe) theCurve.keyframes.get(theCurve.keyframes.size() - 2);
			float m = (prev.value - prev2.value) / (float) (prev.frame - prev2.frame);
			float interpolatedValue = prev.value + m * (float) (keyframe.frame - prev.frame);
			if ((double) Math.abs(interpolatedValue - keyframe.value) < 1.0E-4D) {
				theCurve.keyframes.remove(theCurve.keyframes.size() - 1);
			}
		}

		theCurve.keyframes.add(keyframe);
	}

	public CPointer<BlenderObject> serialize(Serializer serializer) throws IOException {
		return serializer.maybeMajor(this, this.id, BlenderObject.class, () -> {
			if (this.lastVisibleFrame >= 0) {
				this.keyframe("hide", 0, this.lastVisibleFrame + 1, 1.0F);
			}

			CPointer<BlenderObject> parent = this.parent == null ? null : this.parent.serialize(serializer);
			CPointer<Mesh> mesh = this.mesh == null ? null : this.mesh.serialize(serializer);
			CPointer<bAction> action = this.action == null ? null : this.action.serialize(serializer);
			CPointer<BlenderObject> pointAt = this.pointAt == null ? null : this.pointAt.serialize(serializer);
			return (object) -> {
				object.setParent(parent);
				CArrayFacade loc;
				if (parent != null) {
					loc = object.getParentinv();
					((CArrayFacade) loc.get(0)).set(0, 1.0F);
					((CArrayFacade) loc.get(1)).set(1, 1.0F);
					((CArrayFacade) loc.get(2)).set(2, 1.0F);
					((CArrayFacade) loc.get(3)).set(3, 1.0F);
				}

				if (mesh != null) {
					object.setData(mesh.cast(Object.class));
					Mesh meshObj = (Mesh) mesh.get();
					short totcol = meshObj.getTotcol();
					if (totcol > 0) {
						byte[] matbits = new byte[totcol];
						Arrays.fill(matbits, (byte) 1);
						object.setMatbits(serializer.writeBytes(matbits));
						object.setMat(serializer.writeDataPArray(Material.class, totcol, (i) -> {
							return (CPointer) Util.plus(meshObj.getMat(), i).get();
						}));
						object.setTotcol(totcol);
					}
				}

				object.setType((short) this.type.ordinal());
				object.setLay(this.layers);
				object.setDt((byte) 5);
				loc = object.getLoc();
				loc.set(0, this.loc.x);
				loc.set(1, this.loc.y);
				loc.set(2, this.loc.z);
				CArrayFacade<Float> size = object.getSize();
				size.set(0, this.scale.x);
				size.set(1, this.scale.y);
				size.set(2, this.scale.z);
				CArrayFacade<Float> quat = object.getQuat();
				quat.set(0, this.rot.w);
				quat.set(1, this.rot.x);
				quat.set(2, this.rot.y);
				quat.set(3, this.rot.z);
				CArrayFacade<Float> dquat = object.getDquat();
				dquat.set(0, 1.0F);
				dquat.set(1, 0.0F);
				dquat.set(2, 0.0F);
				dquat.set(3, 0.0F);
				object.getDscale().fromArray(new float[] { 1.0F, 1.0F, 1.0F });
				AnimData animData = (AnimData) serializer.writeData(AnimData.class);
				animData.setAction(action);
				object.setAdt(animData.__io__addressof());
				if (pointAt != null) {
					serializer.writeDataList(bConstraint.class, object.getConstraints(), 1, (i, bConstraint) -> {
						bConstraint.setEnforce(1.0F);
						bConstraint.setType((short) 2);
						bTrackToConstraint constraint = (bTrackToConstraint) serializer
								.writeData(bTrackToConstraint.class);
						constraint.setTar(pointAt);
						constraint.setReserved1(1);
						constraint.setReserved2(2);
						bConstraint.setData(constraint.__io__addressof().cast(Object.class));
					});
				}

			};
		});
	}

	public static enum Type {
		OB_EMPTY, OB_MESH, OB_CURVE, OB_SURF, OB_FONT, OB_MBALL, OB_6, OB_7, OB_8, OB_9, OB_LAMP, OB_CAMERA, OB_SPEAKER,
		OB_13, OB_14, OB_15, OB_16, OB_17, OB_18, OB_19, OB_20, OB_WAVE, OB_LATTICE, OB_23, OB_24, OB_ARMATURE;

		// $FF: synthetic method
		private static DObject.Type[] $values() {
			return new DObject.Type[] { OB_EMPTY, OB_MESH, OB_CURVE, OB_SURF, OB_FONT, OB_MBALL, OB_6, OB_7, OB_8, OB_9,
					OB_LAMP, OB_CAMERA, OB_SPEAKER, OB_13, OB_14, OB_15, OB_16, OB_17, OB_18, OB_19, OB_20, OB_WAVE,
					OB_LATTICE, OB_23, OB_24, OB_ARMATURE };
		}
	}
}
