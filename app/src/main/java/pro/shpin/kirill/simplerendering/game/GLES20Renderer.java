package pro.shpin.kirill.simplerendering.game;

import static android.opengl.GLES20.*;

import android.content.Context;
import android.graphics.Point;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import pro.shpin.kirill.simplerendering.GameActivity;
import pro.shpin.kirill.simplerendering.R;

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
	private Matrix4f transformation;

	private int vertShader;
	private int fragShader;
	private int shaderProgram;

	private int transformationLoc;
	private int projectionLoc;
	private int colorLoc;

	private RawModel model;

	private int loadShader(String source, int type) {
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

	private void initShaders() {
		vertShader = loadShader(Utils.readFromFile(R.raw.shader_vert), GL_VERTEX_SHADER);
		fragShader = loadShader(Utils.readFromFile(R.raw.shader_frag), GL_FRAGMENT_SHADER);

		shaderProgram  = glCreateProgram();

		glAttachShader(shaderProgram, vertShader);
		glAttachShader(shaderProgram, fragShader);

		glBindAttribLocation(shaderProgram, 0, "position");

		glLinkProgram(shaderProgram);

		colorLoc = glGetUniformLocation(shaderProgram, "color");
		projectionLoc = glGetUniformLocation(shaderProgram, "projection");
		transformationLoc = glGetUniformLocation(shaderProgram, "transformation");
	}

	private void initProjection () {
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

		glUseProgram(shaderProgram);

		glUniformMatrix4fv(projectionLoc, 1, false, ortho.getData(), 0);

		glUseProgram(0);
	}

	public GLES20Renderer() {
		firstDraw = true;
		surfaceCreated = false;
		width = -1;
		height = -1;
		lastTime = System.currentTimeMillis();
		FPS = 0;
		frames = 0;
		vertShader = 0;
		fragShader = 0;
		shaderProgram = 0;
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

		model = new RawModel(tri, size, indicies);

		initShaders();

		initProjection();
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
		glUseProgram(shaderProgram);

		float time = (float)((System.currentTimeMillis() % 1000000) / 1000.0);
		float ctime = (float)Math.cos(time);
		float stime = (float)Math.sin(time);

		transformation = new Matrix4f();

		transformation.move(GameActivity.touchX, screenHeight - GameActivity.touchY, 0);
		transformation.rotateZ(time);

		glUniformMatrix4fv(transformationLoc, 1, false, transformation.getData(), 0);

		glUniform3f(colorLoc, ctime*ctime, stime*stime, stime*ctime + 0.5f);

		glDrawElements(GL_TRIANGLES, model.getIndexCount(), GL_UNSIGNED_INT, 0);

		int error;
		while((error = glGetError()) != GL_NO_ERROR) {
			Log.i("Info", "GLError: " + error);
		}

		glUseProgram(0);
		model.unbind();
	}
}
