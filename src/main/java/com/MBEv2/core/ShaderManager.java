package com.MBEv2.core;

import org.joml.*;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryStack;

import java.util.HashMap;
import java.util.Map;

public class ShaderManager {

    private final int programID;
    private int vertexShaderID, fragmentShaderID;

    private final Map<String, Integer> uniforms;

    public ShaderManager() throws Exception {
        programID = GL20.glCreateProgram();
        if (programID == 0)
            throw new Exception("Could not create Shader");

        uniforms = new HashMap<>();
    }

    public void createUniform(String uniformName) throws Exception {
        int uniformLocation = GL20.glGetUniformLocation(programID, uniformName);
        if (uniformLocation < 0)
            throw new Exception("Could not find uniform " + uniformName);
        uniforms.put(uniformName, uniformLocation);
    }

    public void setUniform(String uniformName, Matrix4f value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            GL20.glUniformMatrix4fv(uniforms.get(uniformName), false, value.get(stack.mallocFloat(16)));
        }
    }

    public void setUniform(String uniformName, int[] data) {
        GL20.glUniform1iv(uniforms.get(uniformName), data);
    }

    public void setUniform(String uniformName, int value_X, int value_Y) {
        GL20.glUniform2i(uniforms.get(uniformName), value_X, value_Y);
    }

    public void setUniform(String uniformName, Vector3i value) {
        GL20.glUniform3i(uniforms.get(uniformName), value.x, value.y, value.z);
    }

    public void setUniform(String uniformName, Vector3f value) {
        GL20.glUniform3f(uniforms.get(uniformName), value.x, value.y, value.z);
    }

    public void setUniform(String uniformName, float x, float y, float z) {
        GL20.glUniform3f(uniforms.get(uniformName), x, y, z);
    }

    public void setUniform(String uniformName, Vector2f value) {
        GL20.glUniform2f(uniforms.get(uniformName), value.x, value.y);
    }

    public void setUniform(String uniformName, int value) {
        GL20.glUniform1i(uniforms.get(uniformName), value);
    }

    public void setUniform(String uniformName, float value) {
        GL20.glUniform1f(uniforms.get(uniformName), value);
    }

    public void createVertexShader(String shaderCode) throws Exception {
        vertexShaderID = createShader(shaderCode, GL20.GL_VERTEX_SHADER);
    }

    public void createFragmentShader(String shaderCode) throws Exception {
        fragmentShaderID = createShader(shaderCode, GL20.GL_FRAGMENT_SHADER);
    }

    public int createShader(String shaderCode, int shaderType) throws Exception {
        int shaderID = GL20.glCreateShader(shaderType);
        if (shaderID == 0)
            throw new Exception("Error creating shader. Type: " + shaderType);

        GL20.glShaderSource(shaderID, shaderCode);
        GL20.glCompileShader(shaderID);

        if (GL20.glGetShaderi(shaderID, GL20.GL_COMPILE_STATUS) == 0)
            throw new Exception("Error compiling shader code: Type: " + shaderType + "Info: " + GL20.glGetShaderInfoLog(shaderID, 1024));

        GL20.glAttachShader(programID, shaderID);

        return shaderID;
    }

    public void link() throws Exception {
        GL20.glLinkProgram(programID);

        if (GL20.glGetProgrami(programID, GL20.GL_LINK_STATUS) == 0)
            throw new Exception("Error linking shader code: " + GL20.glGetProgramInfoLog(programID, 1024));

        if (vertexShaderID != 0)
            GL20.glDetachShader(programID, vertexShaderID);

        if (fragmentShaderID != 0)
            GL20.glDetachShader(programID, fragmentShaderID);

        GL20.glValidateProgram(programID);
        if (GL20.glGetProgrami(programID, GL20.GL_VALIDATE_STATUS) == 0)
            throw new Exception("Unable to validate shader code: " + GL20.glGetProgramInfoLog(programID, 1024));

    }

    public void bind() {
        GL20.glUseProgram(programID);
    }

    public void unBind() {
        GL20.glUseProgram(0);
    }

    public void cleanUp() {
        unBind();
        if (programID != 0)
            GL20.glDeleteProgram(programID);
    }
}
