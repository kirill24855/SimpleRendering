package pro.shpin.kirill.simplerendering;

/**
 * Created by wiish on 10/16/2016.
 */

import android.util.Log;

import static android.opengl.GLES20.*;

public class BasicShader extends Shader{
    private String[] attribs;
    private String[] uniformNames;

    public BasicShader(int vertId, int fragId, String[] attribs, String[] uniformNames) {
        super(vertId, fragId);
        this.attribs = attribs;
        this.uniformNames = uniformNames;
        super.create();
    }

    public void bindAttributeLocations() {
        for(int i = 0;i < attribs.length;i++) {
            glBindAttribLocation(program, i, attribs[i]);
        }
    }

    public void bindUniforms() {
        for(int i = 0;i < uniformNames.length;i++) {
            super.setUniform(uniformNames[i], glGetUniformLocation(program, uniformNames[i]));
        }
    }
}
