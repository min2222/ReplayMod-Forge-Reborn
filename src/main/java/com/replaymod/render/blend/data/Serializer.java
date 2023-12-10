package com.replaymod.render.blend.data;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.blender.dna.ID;
import org.blender.dna.Link;
import org.blender.dna.ListBase;
import org.cakelab.blender.io.block.BlockCodes;
import org.cakelab.blender.nio.CArrayFacade;
import org.cakelab.blender.nio.CFacade;
import org.cakelab.blender.nio.CPointer;

import com.replaymod.render.blend.Util;

public class Serializer {
	private final Map<Object, CPointer<Object>> serialized = new HashMap();
	private final Map<String, Integer> usedNames = new HashMap();

	public <T> CPointer<T> getMajor(Object obj, Class<T> clazz) {
		CPointer<Object> pointer = this.serialized.get(obj);
		return pointer == null ? null : pointer.cast(clazz);
	}

	public <T extends CFacade> CPointer<T> writeMajor(Object obj, DId id, Class<T> clazz) throws IOException {
		if (this.serialized.containsKey(obj)) {
			throw new IllegalStateException("Object " + obj + " already serialized.");
		} else {
			T val = Util.factory().newCStructBlock(id.code, clazz);
			CPointer<T> pointer = CFacade.__io__addressof(val);
			this.serialized.put(obj, pointer.cast(Object.class));
			if (id.code != BlockCodes.ID_DATA) {
				ID asID = (ID) pointer.cast(ID.class).get();
				String var10000 = id.code.toString().substring(0, 2);
				String name = var10000 + id.name;
				int counter = (Integer) this.usedNames.compute(name, (n, i) -> {
					return i == null ? 0 : i + 1;
				});
				String fullName = counter == 0 ? name : name + "." + counter;
				asID.getName().fromString(fullName);
			}

			return pointer;
		}
	}

	public <T extends CFacade> CPointer<T> maybeMajor(Object obj, DId id, Class<T> clazz,
			Util.IOCallable<Util.IOConsumer<T>> prepare) throws IOException {
		CPointer<T> result = this.getMajor(obj, clazz);
		if (result == null) {
			Util.IOConsumer<T> configure = prepare.call();
			result = this.writeMajor(obj, id, clazz);
			configure.accept(result.get());
		}

		return result;
	}

	public CArrayFacade<Byte> writeString0(String str) throws IOException {
		byte[] bytes = (str + "\u0000").getBytes();
		CArrayFacade<Byte> pointer = Util.factory().newCArrayBlock(BlockCodes.ID_DATA, Byte.class,
				Util.align4(bytes.length));
		pointer.fromArray(bytes);
		return pointer;
	}

	public CPointer<Byte> writeBytes(byte[] bytes) throws IOException {
		CArrayFacade<Byte> pointer = Util.factory().newCArrayBlock(BlockCodes.ID_DATA, Byte.class,
				Util.align4(bytes.length));
		pointer.fromArray(bytes);
		return pointer;
	}

	public <T extends CFacade> T writeData(Class<T> clazz) throws IOException {
		return Util.factory().newCStructBlock(BlockCodes.ID_DATA, clazz);
	}

	public <T extends CFacade> CArrayFacade<T> writeData(Class<T> clazz, int count) throws IOException {
		return Util.factory().newCStructBlock(BlockCodes.ID_DATA, clazz, count);
	}

	public <T extends CFacade> CArrayFacade<T> writeData(Class<T> clazz, int count,
			Util.IOBiConsumer<Integer, T> forElem) throws IOException {
		if (count == 0) {
			return null;
		} else {
			CArrayFacade<T> arrayFacade = this.writeData(clazz, count);
			CPointer<T> pointer = arrayFacade;

			for (int i = 0; i < count; ++i) {
				forElem.accept(i, (pointer).get());
				pointer = Util.plus(pointer, 1);
			}

			return arrayFacade;
		}
	}

	public <T> CArrayFacade<CPointer<T>> writeDataPArray(Class<T> clazz, int count,
			Util.IOFunction<Integer, CPointer<T>> forElem) throws IOException {
		if (count == 0) {
			return null;
		} else {
			CArrayFacade<CPointer<T>> arrayFacade = Util.factory().newCPointerBlock(BlockCodes.ID_DATA,
					new Class[] { CPointer.class, clazz }, count);

			for (int i = 0; i < count; ++i) {
				arrayFacade.set(i, (CPointer) forElem.apply(i));
			}

			return arrayFacade;
		}
	}

	public <T extends CFacade> void writeDataList(Class<T> clazz, ListBase listBase, int size,
			Util.IOBiConsumer<Integer, T> forElem) throws IOException {
		CPointer<Link> prevPointer = null;
		Link prev = null;

		for (int i = 0; i < size; ++i) {
			CPointer<T> pointer = CFacade.__io__addressof(this.writeData(clazz));
			CPointer<Link> linkPointer = pointer.cast(Link.class);
			Link linkElem = (Link) linkPointer.get();
			if (prevPointer == null) {
				listBase.setFirst(pointer.cast(Object.class));
			} else {
				prev.setNext(linkPointer);
				linkElem.setPrev(prevPointer);
			}

			forElem.accept(i, pointer.get());
			prevPointer = linkPointer;
			prev = linkElem;
		}

		if (prevPointer != null) {
			listBase.setLast(prevPointer.cast(Object.class));
		}

	}
}
