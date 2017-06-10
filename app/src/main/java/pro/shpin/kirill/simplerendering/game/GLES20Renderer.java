package pro.shpin.kirill.simplerendering.game;

import static android.opengl.GLES20.*;

import android.opengl.GLSurfaceView;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import pro.shpin.kirill.simplerendering.GameActivity;
import pro.shpin.kirill.simplerendering.R;

public class GLES20Renderer implements GLSurfaceView.Renderer{

	public static float width;
	public static float height;
	public static float aspectX;
	public static float aspectY;

	private boolean firstDraw;
	private boolean surfaceCreated;

	private long lastTime;
	private int FPS;
	private int frames;

	private int vbo;
	private int ibo;

	private int vertShader;
	private int fragShader;
	private int shaderProgram;

	private int aspectLoc;

	private int transformLoc;

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

	private void initModel() {
		float[] tri = {
				 1,  1,
				-1,  1,
				 1, -1,
				-1, -1
		};

		int[] indicies = {0, 1, 2, 2, 1, 3};

		FloatBuffer buffer = ByteBuffer.allocateDirect(tri.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
		buffer.put(tri);
		buffer.position(0);

		IntBuffer intBuf = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asIntBuffer();

		glGenBuffers(1, intBuf);
		vbo = intBuf.get(0);
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferData(GL_ARRAY_BUFFER, buffer.capacity()* 4, buffer, GL_STATIC_DRAW);
		glBindBuffer(GL_ARRAY_BUFFER, 0);

		IntBuffer indexBuffer = ByteBuffer.allocateDirect(indicies.length * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
		indexBuffer.put(indicies);
		indexBuffer.position(0);

		glGenBuffers(1, intBuf);
		ibo = intBuf.get(0);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer.capacity() * 4, indexBuffer, GL_STATIC_DRAW);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
	}

	private void initShaders() {
		vertShader = loadShader(Utils.readFromFile(R.raw.shader_vert), GL_VERTEX_SHADER);
		fragShader = loadShader(Utils.readFromFile(R.raw.shader_frag), GL_FRAGMENT_SHADER);

		shaderProgram  = glCreateProgram();

		glAttachShader(shaderProgram, vertShader);
		glAttachShader(shaderProgram, fragShader);

		glBindAttribLocation(shaderProgram, 0, "position");

		glLinkProgram(shaderProgram);

		aspectLoc = glGetUniformLocation(shaderProgram, "aspect");
		transformLoc = glGetUniformLocation(shaderProgram, "transform");
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

	public void initGL() {
		glClearColor(0.2f, 0.3f, 0.8f, 1f);

		initModel();

		initShaders();
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		surfaceCreated = true;
		width = -1;
		height = -1;

		initGL();
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int w, int h) {
		if(!surfaceCreated && width == w && height == h) {
			return;
		}

		width = w;
		height = h;

		float aspect = height/width;

		if(aspect >= 1) {
			aspectX = 1;
			aspectY = aspect;
		} else {
			aspectX = 1.0f/aspect;
			aspectY = 1;
		}

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

		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);

		glEnableVertexAttribArray(0);
		glVertexAttribPointer(0, 2, GL_FLOAT, false, 2 * 4, 0);

		glUseProgram(shaderProgram);

		glUniform2f(aspectLoc, aspectX, aspectY);

		try {
			GameView.semaphore.acquire();

			glUniformMatrix3fv(transformLoc, 1, false, GameView.transform.getData(), 0);

			GameView.semaphore.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);

		int error;
		while((error = glGetError()) != GL_NO_ERROR) {
			Log.i("Info", "GLError: " + error);
		}

		glUseProgram(0);

		glDisableVertexAttribArray(0);

		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
	}
}
