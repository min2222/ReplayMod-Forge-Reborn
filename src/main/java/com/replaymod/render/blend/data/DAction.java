package com.replaymod.render.blend.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.blender.dna.BezTriple;
import org.blender.dna.FCurve;
import org.blender.dna.bAction;
import org.cakelab.blender.io.block.BlockCodes;
import org.cakelab.blender.nio.CArrayFacade;
import org.cakelab.blender.nio.CPointer;

public class DAction {
	public final DId id;
	public List<DAction.DFCurve> curves;

	public DAction() {
		this.id = new DId(BlockCodes.ID_AC);
		this.curves = new ArrayList();
	}

	public CPointer<bAction> serialize(Serializer serializer) throws IOException {
		return serializer.maybeMajor(this, this.id, bAction.class, () -> {
			return (bAction) -> {
				serializer.writeDataList(FCurve.class, bAction.getCurves(), this.curves.size(), (i, fCurve) -> {
					DAction.DFCurve dfCurve = (DAction.DFCurve) this.curves.get(i);
					fCurve.setRna_path(serializer.writeString0(dfCurve.rnaPath));
					fCurve.setArray_index(dfCurve.rnaArrayIndex);
					fCurve.setTotvert(dfCurve.keyframes.size());
					fCurve.setBezt(serializer.writeData(BezTriple.class, dfCurve.keyframes.size(), (j, fBezTriple) -> {
						DAction.DKeyframe dKeyframe = (DAction.DKeyframe) dfCurve.keyframes.get(j);
						fBezTriple.setIpo((byte) dKeyframe.interpolationType.ordinal());
						CArrayFacade<Float> vec = (CArrayFacade) fBezTriple.getVec().get(1);
						vec.set(0, (float) dKeyframe.frame);
						vec.set(1, dKeyframe.value);
					}));
				});
			};
		});
	}

	public static class DFCurve {
		public String rnaPath;
		public int rnaArrayIndex;
		public List<DAction.DKeyframe> keyframes = new ArrayList();
	}

	public static class DKeyframe {
		public DAction.InterpolationType interpolationType;
		public int frame;
		public float value;

		public DKeyframe() {
			this.interpolationType = DAction.InterpolationType.CONSTANT;
		}
	}

	public static enum InterpolationType {
		CONSTANT, LINEAR;

		// $FF: synthetic method
		private static DAction.InterpolationType[] $values() {
			return new DAction.InterpolationType[] { CONSTANT, LINEAR };
		}
	}
}
