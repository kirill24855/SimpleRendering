package pro.shpin.kirill.simplerendering.game;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Map;
import java.util.HashMap;

import static android.opengl.GLES20.*;

/**
 * Created by wiish on 10/16/2016.
 */

abstract public class Shader {
    private int vert;
    private int frag;
    private int vertId;
    private int fragId;
    protected int program;
    protected Map<String, Integer> uniforms;

    public Shader(int vertId, int fragId) {
        this.vertId = vertId;
        this.fragId = fragId;

        uniforms = new HashMap<String, Integer>();
    }

    public void create() {
        String vertFile = Utils.readFromFile(vertId);
        String fragFile = Utils.readFromFile(fragId);

        vert = loadShader(vertFile, GL_VERTEX_SHADER);
        frag = loadShader(fragFile, GL_FRAGMENT_SHADER);

        program  = glCreateProgram();

        glAttachShader(program, vert);
        glAttachShader(program, frag);

        bindAttributeLocations();

        glLinkProgram(program);

        bindUniforms();
    }

    public void bind() {
        glUseProgram(program);
    }

    protected void setUniform(String name, int location) {
        uniforms.put(name, location);
    }

    public void setUniformVec3(String name, float x, float y, float z) {
        glUniform3f(uniforms.get(name), x, y, z);
    }

    public void setUniformMat4(String name, Matrix4f matrix) {
        glUniformMatrix4fv(uniforms.get(name), 1, false, matrix.getData(), 0);
    }

    public static void unbind() {
        glUseProgram(0);
    }

    abstract public void bindAttributeLocations();
    abstract public void bindUniforms();

    public int loadShader(String source, int type) {
        int shader;

        shader = glCreateShader(type);

        if (shader == 0) {
            return 0;
        }

        glShaderSource(shader, source);

        glCompileShader(shader);

        IntBuffer intBuf= ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asIntBuffer();

        int status;
        glGetShaderiv(shader, GL_COMPILE_STATUS, intBuf);
        status=intBuf.get(0);

        if(status==0){
            glGetShaderiv(shader,GL_INFO_LOG_LENGTH,intBuf);
            status=intBuf.get(0);
            if (status>1){
                Log.i("Shader","Shader: " + glGetShaderInfoLog(shader));
            }
            glDeleteShader(shader);
            Log.w("Shader","Shader error.");
            return 0;
        }

        return shader;
    }
}
