package com.replaymod.render.blend;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;

import org.blender.utils.MainLib;
import org.cakelab.blender.generator.typemap.Renaming;
import org.cakelab.blender.io.BlenderFile;
import org.cakelab.blender.io.FileVersionInfo;
import org.cakelab.blender.io.block.Block;
import org.cakelab.blender.io.block.BlockCodes;
import org.cakelab.blender.io.block.BlockTable;
import org.cakelab.blender.metac.CMetaModel;
import org.cakelab.blender.metac.CStruct;
import org.cakelab.blender.nio.CArrayFacade;
import org.cakelab.blender.nio.CFacade;
import org.cakelab.blender.nio.CPointer;
import org.cakelab.json.JSONArray;
import org.cakelab.json.JSONObject;
import org.cakelab.json.codec.JSONCodec;
import org.cakelab.json.codec.JSONCodecConfiguration;
import org.cakelab.json.codec.JSONCodecException;

public class ToJson {
	private static final String PACKAGE = "com.replaymod.lib.org.blender.dna";
	private static JSONCodec codec;
	private static CMetaModel model;
	private static BlenderFile blend;
	private static BlockTable blockTable;

	public static void main(String[] args) throws IOException, JSONCodecException {
		File fBlend = new File("/home/user/1.blend");
		blend = new BlenderFile(fBlend);
		FileVersionInfo versions = blend.readFileGlobal();
		if (!MainLib.doVersionCheck(versions)) {
			System.err.println("Warning: Conversion will probably fail due to version mismatch!");
		}

		blockTable = blend.getBlockTable();
		model = blend.getMetaModel();
		codec = new JSONCodec(new JSONCodecConfiguration(false));
		JSONObject json = new JSONObject();
		JSONObject oHeader = createHeader(blend);
		json.put("header", oHeader);
		JSONArray aBlocks = new JSONArray();
		json.put("blocks", aBlocks);
		Iterator var6 = blend.getBlocks().iterator();

		while (var6.hasNext()) {
			Block b = (Block) var6.next();
			addBlock(aBlocks, b);
		}

		blend.close();
		File fJsonOut = new File(fBlend.getParentFile(), fBlend.getName().replace(".blend", ".json"));
		FileOutputStream fout = new FileOutputStream(fJsonOut);
		codec.encodeObject(json, fout);
		fout.close();
		System.out.println("Finished");
	}

	private static JSONObject createHeader(BlenderFile blend) {
		JSONObject oHeader = new JSONObject();
		oHeader.put("MAGIC", "BLENDER");
		oHeader.put("addressWidth", blend.getEncoding().getAddressWidth());
		oHeader.put("endianess", blend.getEncoding().getByteOrder().toString());
		oHeader.put("version", blend.getVersion().toString());
		return oHeader;
	}

	private static void addBlock(JSONArray aBlocks, Block b) throws JSONCodecException, IOException {
		JSONObject oBlock = new JSONObject();
		aBlocks.add(oBlock);
		JSONObject header = (JSONObject) codec.encodeObjectJSON(b.header);
		oBlock.put("header", header);
		header.put("code", b.header.getCode().toString());
		CStruct struct = model.getStruct(b.header.getSdnaIndex());
		String var10002 = struct.getSignature();
		header.put("sdnaIndex", var10002 + "(" + b.header.getSdnaIndex() + ")");
		PrintStream var10000 = System.out;
		long var10001 = b.header.getAddress();
		var10000.print("[" + var10001 + ", " + (b.header.getAddress() + (long) b.header.getSize()) + "] ");
		if (b.header.getCode().equals(BlockCodes.ID_DATA) && b.header.getSdnaIndex() == 0) {
			System.out.println(b.header.getCode().toString() + ": Link(0) or undef");
			byte[] buffer = new byte[b.header.getSize()];
			b.data.readFully(buffer);
			String str = toHexStr(buffer);
			oBlock.put("raw", str);
			int requiredSize = b.header.getCount() * struct.sizeof(blend.getEncoding().getAddressWidth());
			if (requiredSize == b.header.getSize()) {
				addStruct(oBlock, b, struct);
			}
		} else {
			var10000 = System.out;
			String var8 = b.header.getCode().toString();
			var10000.println(var8 + ": " + struct.getSignature());
			addStruct(oBlock, b, struct);
		}

	}

	private static String toHexStr(byte[] buffer) {
		StringBuffer str = new StringBuffer();
		byte[] var2 = buffer;
		int var3 = buffer.length;

		for (int var4 = 0; var4 < var3; ++var4) {
			byte v = var2[var4];
			String s = Integer.toHexString(v & 255);
			if (s.length() == 1) {
				s = "0" + s;
			}

			str.append(s);
		}

		return str.toString();
	}

	private static void addStruct(JSONObject oBlock, Block b, CStruct struct) {
		if (b.header.getCount() == 1) {
			oBlock.put(struct.getSignature(), createStruct(b, 0, struct));
		} else {
			JSONArray aStructs = new JSONArray();
			oBlock.put(struct.getSignature() + "_array", aStructs);

			for (int i = 0; i < b.header.getCount(); ++i) {
				aStructs.add(createStruct(b, i, struct));
			}
		}

	}

	private static Object createStruct(Block b, int index, CStruct struct) {
		JSONObject oStruct = null;

		try {
			Class<?> cStruct = Class
					.forName("com.replaymod.lib.org.blender.dna." + Renaming.mapStruct2Class(struct.getSignature()));
			long address = b.header.getAddress()
					+ (long) (index * struct.sizeof(blend.getEncoding().getAddressWidth()));
			Constructor<?> constructor = cStruct.getDeclaredConstructor(Long.TYPE, Block.class, BlockTable.class);
			Object object = constructor.newInstance(address, b, blockTable);
			oStruct = (JSONObject) toJson(cStruct, object);
		} catch (Throwable var9) {
			oStruct = new JSONObject();
			var9.printStackTrace();
			String var10002 = var9.getClass().getSimpleName();
			oStruct.put("error", var10002 + ": " + var9.getMessage());
		}

		return oStruct;
	}

	private static Object toJson(Class<?> type, Object value) throws Throwable {
		try {
			assert value != null;

			if (isPrimitive(type)) {
				return codec.encodeObjectJSON(value);
			} else {
				int i;
				if (value instanceof CArrayFacade) {
					CArrayFacade<?> carray = (CArrayFacade) value;
					JSONArray array = new JSONArray();
					boolean hasString = false;

					for (i = 0; i < carray.length(); ++i) {
						Object elem = carray.get(i);
						if (elem instanceof Byte) {
							hasString = true;
						}

						array.add(toJson(elem.getClass(), elem));
					}

					if (hasString) {
						JSONObject debugArray = new JSONObject();
						debugArray.put("str", carray.asString());
						debugArray.put("data", array);
						return debugArray;
					} else {
						return array;
					}
				} else if (value instanceof CPointer) {
					return ((CPointer) value).getAddress();
				} else if (value instanceof CFacade) {
					JSONObject oStruct = new JSONObject();

					try {
						Method[] var12 = type.getDeclaredMethods();
						int var14 = var12.length;

						for (i = 0; i < var14; ++i) {
							Method getter = var12[i];
							if (getter.getName().startsWith("get")) {
								Object result = getter.invoke(value);
								Class<?> rType = getter.getReturnType();
								oStruct.put(getter.getName(), toJson(rType, result));
							}
						}
					} catch (InvocationTargetException var9) {
						Throwable cause = var9.getCause();
						if (cause instanceof NullPointerException) {
							throw (NullPointerException) cause;
						}

						throw var9;
					}

					return oStruct;
				} else {
					return codec.encodeObjectJSON(value);
				}
			}
		} catch (NullPointerException var10) {
			JSONObject o = new JSONObject();
			o.put("error", "data not found");
			return o;
		}
	}

	private static boolean isPrimitive(Class<?> type) {
		return type.isPrimitive() || type.equals(String.class);
	}
}
