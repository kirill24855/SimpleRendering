package pro.shpin.kirill.simplerendering;

import static android.opengl.GLES20.*;

import android.opengl.GLSurfaceView;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLES20Renderer implements GLSurfaceView.Renderer{

	private int width;
	private int height;
	private boolean surfaceCreated;

	public void onCreate(int width, int height, boolean contextLost) {
		Log.d("debug", "COLOREOROOFO");
		glClearColor(1.0f, 0.0f, 1.0f, 1.0f);
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		surfaceCreated = true;
		width = -1;
		height = -1;
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int w, int h) {
		if(!surfaceCreated && width == w && height == h) {
			return;
		}

		width = w;
		height = h;

		onCreate(width, height, surfaceCreated);
		surfaceCreated = false;
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		glClear(GL_COLOR_BUFFER_BIT);
	}
}
