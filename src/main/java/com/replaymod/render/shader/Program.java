package com.replaymod.render.shader;

import static org.lwjgl.opengl.ARBShaderObjects.GL_OBJECT_COMPILE_STATUS_ARB;
import static org.lwjgl.opengl.ARBShaderObjects.glCompileShaderARB;
import static org.lwjgl.opengl.ARBShaderObjects.glCreateShaderObjectARB;
import static org.lwjgl.opengl.ARBShaderObjects.glDeleteObjectARB;
import static org.lwjgl.opengl.ARBShaderObjects.glGetObjectParameteriARB;
import static org.lwjgl.opengl.ARBShaderObjects.glShaderSourceARB;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.GL11;

import com.replaymod.core.versions.MCVer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

public class Program {
	private final int program;

	public Program(ResourceLocation vertexShader, ResourceLocation fragmentShader) throws Exception {
		int vertShader = this.createShader(vertexShader, 35633);
		int fragShader = this.createShader(fragmentShader, 35632);
		this.program = ARBShaderObjects.glCreateProgramObjectARB();
		if (this.program == 0) {
			throw new Exception("glCreateProgramObjectARB failed");
		} else {
			ARBShaderObjects.glAttachObjectARB(this.program, vertShader);
			ARBShaderObjects.glAttachObjectARB(this.program, fragShader);
			ARBShaderObjects.glLinkProgramARB(this.program);
			if (ARBShaderObjects.glGetObjectParameteriARB(this.program, 35714) == 0) {
				throw new Exception("Error linking: " + getLogInfo(this.program));
			} else {
				ARBShaderObjects.glValidateProgramARB(this.program);
				if (ARBShaderObjects.glGetObjectParameteriARB(this.program, 35715) == 0) {
					throw new Exception("Error validating: " + getLogInfo(this.program));
				}
			}
		}
	}

	private int createShader(ResourceLocation resourceLocation, int shaderType) throws Exception {
		int shader = 0;
		try {
			shader = glCreateShaderObjectARB(shaderType);

			if (shader == 0)
				throw new Exception("glCreateShaderObjectARB failed");

			Resource resource = MCVer.getMinecraft().getResourceManager().getResourceOrThrow(resourceLocation);
			try (InputStream is = resource.open()) {
				glShaderSourceARB(shader, IOUtils.toString(is));
			}
			glCompileShaderARB(shader);

			if (glGetObjectParameteriARB(shader, GL_OBJECT_COMPILE_STATUS_ARB) == GL11.GL_FALSE)
				throw new RuntimeException("Error creating shader: " + getLogInfo(shader));

			return shader;
		} catch (Exception exc) {
			glDeleteObjectARB(shader);
			throw exc;
		}
	}

	private static String getLogInfo(int obj) {
		return ARBShaderObjects.glGetInfoLogARB(obj, ARBShaderObjects.glGetObjectParameteriARB(obj, 35716));
	}

	public void use() {
		ARBShaderObjects.glUseProgramObjectARB(this.program);
	}

	public void stopUsing() {
		ARBShaderObjects.glUseProgramObjectARB(0);
	}

	public void delete() {
		ARBShaderObjects.glDeleteObjectARB(this.program);
	}

	public Program.Uniform getUniformVariable(String name) {
		return new Program.Uniform(ARBShaderObjects.glGetUniformLocationARB(this.program, name));
	}

	public class Uniform {
		private final int location;

		public Uniform(int location) {
			this.location = location;
		}

		public void set(boolean bool) {
			ARBShaderObjects.glUniform1iARB(this.location, bool ? 1 : 0);
		}

		public void set(int integer) {
			ARBShaderObjects.glUniform1iARB(this.location, integer);
		}
	}
}
