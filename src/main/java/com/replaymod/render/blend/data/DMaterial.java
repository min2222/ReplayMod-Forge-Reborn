package com.replaymod.render.blend.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.blender.dna.MTex;
import org.blender.dna.Material;
import org.blender.dna.Tex;
import org.cakelab.blender.io.block.BlockCodes;
import org.cakelab.blender.nio.CArrayFacade;
import org.cakelab.blender.nio.CPointer;

import com.replaymod.render.blend.Util;

public class DMaterial {
	public final DId id;
	public List<DMaterial.DMTex> textures;

	public DMaterial() {
		this.id = new DId(BlockCodes.ID_MA);
		this.textures = new ArrayList();
	}

	public CPointer<Material> serialize(Serializer serializer) throws IOException {
		return serializer.maybeMajor(this, this.id, Material.class, () -> {
			List<Util.IOCallable<CPointer<MTex>>> textures = new ArrayList();
			Iterator var3 = this.textures.iterator();

			while (var3.hasNext()) {
				DMaterial.DMTex texture = (DMaterial.DMTex) var3.next();
				textures.add(texture.serialize(serializer));
			}

			return (material) -> {
				material.setMode(65728);
				material.setAlpha(1.0F);
				material.setRef(1.0F);
				material.setR(1.0F);
				material.setG(1.0F);
				material.setB(1.0F);
				CArrayFacade<CPointer<MTex>> mTexPointers = material.getMtex();

				for (int i = 0; i < textures.size(); ++i) {
					mTexPointers.set(i, (CPointer) ((Util.IOCallable) textures.get(i)).call());
				}

			};
		});
	}

	public static class DMTex {
		public DTexture texture;

		public Util.IOCallable<CPointer<MTex>> serialize(Serializer serializer) throws IOException {
			CPointer<Tex> texture = this.texture.serialize(serializer);
			return () -> {
				MTex mTex = (MTex) serializer.writeData(MTex.class);
				mTex.setTex(texture);
				mTex.getUvname().fromString("UVMap");
				mTex.setMapto((short) 129);
				mTex.setBlendtype((short) 1);
				mTex.setColfac(1.0F);
				mTex.setAlphafac(1.0F);
				mTex.setDef_var(1.0F);
				mTex.setTexco((short) 16);
				mTex.setProjx((byte) 1);
				mTex.setProjy((byte) 2);
				mTex.setProjz((byte) 3);
				CArrayFacade<Float> size = mTex.getSize();
				size.set(0, 1.0F);
				size.set(1, 1.0F);
				size.set(2, 1.0F);
				return mTex.__io__addressof();
			};
		}
	}
}
