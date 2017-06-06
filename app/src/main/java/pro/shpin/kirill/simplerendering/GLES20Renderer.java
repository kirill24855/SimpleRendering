package pro.shpin.kirill.simplerendering;

import static android.opengl.GLES20.*;

import android.content.Context;
import android.graphics.Point;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLES20Renderer implements GLSurfaceView.Renderer{

	private boolean firstDraw;
	private boolean surfaceCreated;
	private int width;
	private int height;
	private long lastTime;
	private int FPS;
	private int frames;
	private int screenWidth;
	private int screenHeight;
	Matrix4f transformation;

	private Shader shader;
	private RawModel model;

	public GLES20Renderer() {
		firstDraw = true;
		surfaceCreated = false;
		width = -1;
		height = -1;
		lastTime = System.currentTimeMillis();
		FPS = 0;
		frames = 0;
	}

	public void onCreate(int width, int height, boolean contextLost) {
		glClearColor(0.2f, 0.3f, 0.8f, 1f);

		float[] tri = {
				100f,  100f, 0.0f,
				-100f,  100f, 0.0f,
				100f, -100f, 0.0f,
				-100f, -100f, 0.0f
		};

		float[][] vertData = {tri};

		int[] indicies = {0, 1, 2, 2, 1, 3};
		int[] size = {3};
		String[] attribs = {"position"};
		String[] uniforms = {"color", "projection", "transformation"};

		shader = new BasicShader(R.raw.shader_vert, R.raw.shader_frag, attribs, uniforms);
		model = new RawModel(tri, size, indicies);

		WindowManager wm = (WindowManager) GLApplication.context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point sz = new Point();
		display.getSize(sz);
		screenWidth = sz.x;
		screenHeight = sz.y;

		float r = screenWidth;
		float l = 0;
		float t = screenHeight;
		float b = 0;
		float f = 10;
		float n = -10;

		Matrix4f ortho = new Matrix4f();

		ortho.set(0, 0, 2/(r-l));
		ortho.set(0, 3, -(r + l)/(r - l));

		ortho.set(1, 1, 2/(t- b));
		ortho.set(1, 3, -(t + b)/(t - b));

		ortho.set(2, 2, -2/(f- n));
		ortho.set(2, 3, -(f + n)/(f - n));

		shader.bind();
		shader.setUniformMat4("projection", ortho);
		shader.setUniformMat4("transformation", new Matrix4f());
		Shader.unbind();
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
		onDrawFrame(firstDraw);

		frames++;
		long currentTime = System.currentTimeMillis();
		if(currentTime - lastTime >= 1000) {
			FPS = frames;
			frames = 0;
			lastTime = currentTime;
		}

		if(firstDraw) {
			firstDraw = false;
		}
	}

	public void onDrawFrame(boolean firstDraw) {
		glClear(GL_COLOR_BUFFER_BIT);

		model.bind();
		shader.bind();

		float time = (float)((System.currentTimeMillis() % 1000000) / 1000.0);
		float ctime = (float)Math.cos(time);
		float stime = (float)Math.sin(time);

		transformation = new Matrix4f();

		transformation.move(GameActivity.touchX, screenHeight - GameActivity.touchY, 0);
		transformation.rotateZ(time);

		shader.setUniformMat4("transformation", transformation);

		shader.setUniformVec3("color", ctime*ctime, stime*stime, stime*ctime + 0.5f);
		glDrawElements(GL_TRIANGLES, model.getIndexCount(), GL_UNSIGNED_INT, 0);

		int error;
		while((error = glGetError()) != GL_NO_ERROR) {
			Log.i("Info", "GLError: " + error);
		}

		Shader.unbind();
		model.unbind();
	}
}
