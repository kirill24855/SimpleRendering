package pro.shpin.kirill.simplerendering;

/**
 * Created by wiish on 10/16/2016.
 */


import android.util.Log;

import static android.opengl.GLES20.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.FloatBuffer;

public class RawModel {
    private FloatBuffer buffer;
    private IntBuffer indexBuffer;
    private int vbo;
    private int ibo;
    private int[] offsets;
    private int[] strides;
    private int indexCount;

    public RawModel(float[] data, int[] size, int[] indicies) {
        indexCount = indicies.length;

        buffer = ByteBuffer.allocateDirect(data.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        buffer.put(data);
        buffer.position(0);

        IntBuffer intBuf = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asIntBuffer();

        glGenBuffers(1, intBuf);
        vbo = intBuf.get(0);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, buffer.capacity()* 4, buffer, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        offsets = new int[size.length];
        strides = new int[size.length];
        for(int i = 0;i < size.length;i++) {
            strides[i] = 0;
            offsets[i] = 0;
            for(int j = 0;j < size.length;j++) {
                strides[i] += size[j];

                if(j < i) {
                    offsets[i] += size[j];
                }
            }
        }

        indexBuffer = ByteBuffer.allocateDirect(indicies.length * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
        indexBuffer.put(indicies);
        indexBuffer.position(0);

        glGenBuffers(1, intBuf);
        ibo = intBuf.get(0);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer.capacity() * 4, indexBuffer, GL_STATIC_DRAW);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    public void bind() {
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);

        for(int i = 0;i < offsets.length;i++) {
            glEnableVertexAttribArray(i);
            glVertexAttribPointer(i, 3, GL_FLOAT, false, strides[i] * 4, offsets[i] * 4);
        }
    }

    public void unbind() {
        for(int i = 0;i < offsets.length;i++) {
            glDisableVertexAttribArray(i);
        }
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

    }

    public int getIndexCount() {
        return indexCount;
    }
}
