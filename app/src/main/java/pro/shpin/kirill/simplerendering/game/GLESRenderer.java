package pro.shpin.kirill.simplerendering.game;

import static android.opengl.GLES20.*;
import static javax.microedition.khronos.opengles.GL11ExtensionPack.GL_RGBA8;

import android.opengl.GLSurfaceView;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import pro.shpin.kirill.simplerendering.R;

public class GLESRenderer implements GLSurfaceView.Renderer{

	public static float width;
	public static float height;
	public static int fbowidth;
	public static int fboheight;
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

	private int bgvertShader;
	private int bgfragShader;
	private int bgshaderProgram;

	private int aspectLoc;
	private int transformLoc;
	private int cLoc;
	private int maxIterationLoc;
	private int colorSchemeLoc;
	private int colorInsideLoc;
	private int colorOutsideLoc;
	private int scaleLoc;

	private int texLoc;

	private int fbo;
	private int renderBuffer;
	private int fboTex;

	public static float SCALING = 1.5f;

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

		int[] indicies = {1, 0, 2, 2, 1, 3};

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

		cLoc = glGetUniformLocation(shaderProgram, "c");
		maxIterationLoc = glGetUniformLocation(shaderProgram, "maxIteration");
		colorSchemeLoc = glGetUniformLocation(shaderProgram, "colorScheme");
		colorInsideLoc = glGetUniformLocation(shaderProgram, "colorInside");
		colorOutsideLoc = glGetUniformLocation(shaderProgram, "colorOutside");
		scaleLoc = glGetUniformLocation(shaderProgram, "scale");

		glUseProgram(shaderProgram);

		glUniform2f(cLoc, 0, 0);
		glUniform1i(maxIterationLoc, 50);
		glUniform1i(colorSchemeLoc, 2);
		glUniform3f(colorInsideLoc, 0, 0, 0);
		glUniform3f(colorOutsideLoc, 0, 1, 0);
		glUniform1f(scaleLoc, SCALING);

		glUseProgram(0);

		bgvertShader = loadShader(Utils.readFromFile(R.raw.bg_vert), GL_VERTEX_SHADER);
		bgfragShader = loadShader(Utils.readFromFile(R.raw.bg_frag), GL_FRAGMENT_SHADER);

		bgshaderProgram  = glCreateProgram();

		glAttachShader(bgshaderProgram, bgvertShader);
		glAttachShader(bgshaderProgram, bgfragShader);

		glBindAttribLocation(bgshaderProgram, 0, "position");

		glLinkProgram(bgshaderProgram);

		texLoc = glGetUniformLocation(bgshaderProgram, "tex");

		glUseProgram(bgshaderProgram);

		glUniform1i(texLoc, 0);

		glUseProgram(0);
	}

	private void initFrameBuffer() {
		int[] fboa = new int[1];
		glGenFramebuffers(1, fboa, 0);
		fbo = fboa[0];

		glBindFramebuffer(GL_FRAMEBUFFER, fbo);

		int[] rba = new int[1];
		glGenRenderbuffers(1, rba, 0);
		renderBuffer = rba[0];

		int wdt = fbowidth;
		int hgt = fboheight;

		glBindRenderbuffer(GL_RENDERBUFFER, renderBuffer);
		glRenderbufferStorage(GL_RENDERBUFFER, GL_RGBA8, wdt, hgt);

		glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_RENDERBUFFER, renderBuffer);

		int[] texa = new int[1];
		glGenTextures(1, texa, 0);
		fboTex = texa[0];

		glBindTexture(GL_TEXTURE_2D, fboTex);

		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, wdt, hgt, 0, GL_RGBA, GL_UNSIGNED_BYTE, null);

		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, fboTex, 0);

		int status = glCheckFramebufferStatus(GL_FRAMEBUFFER) ;
		if(status != GL_FRAMEBUFFER_COMPLETE) {
			String frameBufferError = "Unknown";

			if(status == GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT) {
				frameBufferError = "incomplete attachment";
			} else if (status == GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS) {
				frameBufferError = "incomplete dimentions";
			} else if (status == GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT) {
				frameBufferError = "incomplete missing attachment";
			} else if (status == GL_FRAMEBUFFER_UNSUPPORTED) {
				frameBufferError = "unsupported";
			}

			Log.e("FrameBuffer", "FrameBuffer error: " + frameBufferError);
		}

		glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}

	public void initGL() {
		glClearColor(0.2f, 0.3f, 0.8f, 1f);

		initModel();
		initShaders();
		initFrameBuffer();
	}

	public GLESRenderer() {
		firstDraw = true;
		surfaceCreated = false;
		width = -1;
		height = -1;
		lastTime = System.currentTimeMillis();
		FPS = 0;
		vertShader = 0;
		fragShader = 0;
		shaderProgram = 0;
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

		fbowidth = (int)(width/SCALING);
		fboheight = (int)(height/SCALING);

		float aspect = height/width;

		if(aspect >= 1) {
			aspectX = 1;
			aspectY = aspect;
		} else {
			aspectX = 1.0f/aspect;
			aspectY = 1;
		}

		initGL();

		surfaceCreated = false;
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		onDrawFrame(firstDraw);

		frames++;
		long currentTime = System.currentTimeMillis();
		if(currentTime - lastTime >= 1000) {
			FPS = frames;
			Log.d("FPS", "FPS: " + FPS);
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

		glBindFramebuffer(GL_FRAMEBUFFER, fbo);

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

		glBindFramebuffer(GL_FRAMEBUFFER, 0);

		glUseProgram(bgshaderProgram);

		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, fboTex);

		glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);

		glUseProgram(0);

		glDisableVertexAttribArray(0);

		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

		int error;
		while((error = glGetError()) != GL_NO_ERROR) {
			Log.i("Info", "GLError: " + error);
		}
	}
}
