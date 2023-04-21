package com.replaymod.render.shader;

import static org.lwjgl.opengl.ARBShaderObjects.GL_OBJECT_COMPILE_STATUS_ARB;
import static org.lwjgl.opengl.ARBShaderObjects.GL_OBJECT_INFO_LOG_LENGTH_ARB;
import static org.lwjgl.opengl.ARBShaderObjects.GL_OBJECT_LINK_STATUS_ARB;
import static org.lwjgl.opengl.ARBShaderObjects.GL_OBJECT_VALIDATE_STATUS_ARB;
import static org.lwjgl.opengl.ARBShaderObjects.glAttachObjectARB;
import static org.lwjgl.opengl.ARBShaderObjects.glCompileShaderARB;
import static org.lwjgl.opengl.ARBShaderObjects.glCreateProgramObjectARB;
import static org.lwjgl.opengl.ARBShaderObjects.glCreateShaderObjectARB;
import static org.lwjgl.opengl.ARBShaderObjects.glDeleteObjectARB;
import static org.lwjgl.opengl.ARBShaderObjects.glGetInfoLogARB;
import static org.lwjgl.opengl.ARBShaderObjects.glGetObjectParameteriARB;
import static org.lwjgl.opengl.ARBShaderObjects.glLinkProgramARB;
import static org.lwjgl.opengl.ARBShaderObjects.glShaderSourceARB;
import static org.lwjgl.opengl.ARBShaderObjects.glValidateProgramARB;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.lwjgl.opengl.ARBFragmentShader;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.ARBVertexShader;
import org.lwjgl.opengl.GL11;

import com.replaymod.core.versions.MCVer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

public class Program {
    private final int program;

    public Program(ResourceLocation vertexShader, ResourceLocation fragmentShader) throws Exception {
        int vertShader = createShader(vertexShader, ARBVertexShader.GL_VERTEX_SHADER_ARB);
        int fragShader = createShader(fragmentShader, ARBFragmentShader.GL_FRAGMENT_SHADER_ARB);

        program = glCreateProgramObjectARB();
        if (program == 0) {
            throw new Exception("glCreateProgramObjectARB failed");
        }

        glAttachObjectARB(program, vertShader);
        glAttachObjectARB(program, fragShader);

        glLinkProgramARB(program);
        if (glGetObjectParameteriARB(program, GL_OBJECT_LINK_STATUS_ARB) == GL11.GL_FALSE) {
            throw new Exception("Error linking: " + getLogInfo(program));
        }

        glValidateProgramARB(program);
        if (glGetObjectParameteriARB(program, GL_OBJECT_VALIDATE_STATUS_ARB) == GL11.GL_FALSE) {
            throw new Exception("Error validating: " + getLogInfo(program));
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
        return glGetInfoLogARB(obj, glGetObjectParameteriARB(obj, GL_OBJECT_INFO_LOG_LENGTH_ARB));
    }

    public void use() {
        ARBShaderObjects.glUseProgramObjectARB(program);
    }

    public void stopUsing() {
        ARBShaderObjects.glUseProgramObjectARB(0);
    }

    public void delete() {
        ARBShaderObjects.glDeleteObjectARB(program);
    }

    public Uniform getUniformVariable(String name) {
        return new Uniform(ARBShaderObjects.glGetUniformLocationARB(program, name));
    }

    public class Uniform {
        private final int location;

        public Uniform(int location) {
            this.location = location;
        }

        public void set(boolean bool) {
            ARBShaderObjects.glUniform1iARB(location, bool ? 1 : 0);
        }

        public void set(int integer) {
            ARBShaderObjects.glUniform1iARB(location, integer);
        }
    }
}
