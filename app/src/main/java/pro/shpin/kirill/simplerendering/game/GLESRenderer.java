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

	public static final int RENDER_BUFFER = 0;
	public static final int RENDER_DOWNSCALE = -1;
	public static final int RENDER_UPSCALE = 64;

	public static int renderMode = RENDER_UPSCALE;

	private boolean firstDraw;

	private boolean surfaceCreated;

	private static int colorScheme = 2;
	private static int maxIteration = 200;

	private long lastTime;
	private int FPS;
	private int frames;

	private int vbo;
	private int ibo;

	private int vertShader;
	private int fragShader;
	private int shaderProgram;

	private int fractalvertShader;
	private int fractalfragShader;
	private int fractalshaderProgram;

	private int aspectLoc;
	private int cLoc;
	private int maxIterationLoc;
	private int scLoc;
	private int offLoc;
	private int scaleLoc;
	private int colorSchemeLoc;
	private int colorInsideLoc;
	private int colorOutsideLoc;
	private int transLoc;
	private int windowLoc;

	private int texLoc;

	private int fbo;
	private int renderBuffer;
	private int fboTex;

	private int fbof;
	private int renderBufferf;
	private int fboTexf;

	private int fbob;
	private int renderBufferb;
	private int fboTexb;

	public static float SCALING = 8.0f;

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
		cLoc = glGetUniformLocation(shaderProgram, "c");
		maxIterationLoc = glGetUniformLocation(shaderProgram, "maxIteration");
		colorSchemeLoc = glGetUniformLocation(shaderProgram, "colorScheme");
		colorInsideLoc = glGetUniformLocation(shaderProgram, "colorInside");
		colorOutsideLoc = glGetUniformLocation(shaderProgram, "colorOutside");
		scaleLoc = glGetUniformLocation(shaderProgram, "scale");
		scLoc = glGetUniformLocation(shaderProgram, "sc");
		offLoc = glGetUniformLocation(shaderProgram, "off");
		windowLoc = glGetUniformLocation(shaderProgram, "window");

		glUseProgram(shaderProgram);

		glUniform2f(cLoc, 0, 0);
		glUniform1i(maxIterationLoc, maxIteration);
		glUniform1i(colorSchemeLoc, colorScheme);
		glUniform3f(colorInsideLoc, 0, 0, 0);
		glUniform3f(colorOutsideLoc, 0, 1, 0);

		glUseProgram(0);

		fractalvertShader = loadShader(Utils.readFromFile(R.raw.fractal_vert), GL_VERTEX_SHADER);
		fractalfragShader = loadShader(Utils.readFromFile(R.raw.fractal_frag), GL_FRAGMENT_SHADER);

		fractalshaderProgram  = glCreateProgram();

		glAttachShader(fractalshaderProgram, fractalvertShader);
		glAttachShader(fractalshaderProgram, fractalfragShader);

		glBindAttribLocation(fractalshaderProgram, 0, "position");

		glLinkProgram(fractalshaderProgram);

		texLoc = glGetUniformLocation(fractalshaderProgram, "tex");
		transLoc = glGetUniformLocation(fractalshaderProgram, "trans");

		glUseProgram(fractalshaderProgram);

		glUniform1i(texLoc, 0);

		glUseProgram(0);
	}

	private void initFrameBuffer() {
		int[] fboa = new int[3];
		glGenFramebuffers(3, fboa, 0);
		fbo = fboa[0];
		fbof = fboa[1];
		fbob = fboa[2];

		int[] rba = new int[3];
		glGenRenderbuffers(3, rba, 0);
		renderBuffer = rba[0];
		renderBufferf = rba[1];
		renderBufferb = rba[2];

		int[] texa = new int[3];
		glGenTextures(3, texa, 0);
		fboTex = texa[0];
		fboTexf = texa[1];
		fboTexb = texa[2];

		//Small FBO
		glBindFramebuffer(GL_FRAMEBUFFER, fbo);

		int wdt = fbowidth;
		int hgt = fboheight;

		glBindRenderbuffer(GL_RENDERBUFFER, renderBuffer);
		glRenderbufferStorage(GL_RENDERBUFFER, GL_RGBA8, wdt, hgt);

		glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_RENDERBUFFER, renderBuffer);

		glBindTexture(GL_TEXTURE_2D, fboTex);

		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

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

		//Large FBO
		glBindFramebuffer(GL_FRAMEBUFFER, fbof);

		glBindRenderbuffer(GL_RENDERBUFFER, renderBufferf);
		glRenderbufferStorage(GL_RENDERBUFFER, GL_RGBA8, wdt, hgt);

		glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_RENDERBUFFER, renderBufferf);

		glBindTexture(GL_TEXTURE_2D, fboTexf);

		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, wdt, hgt, 0, GL_RGBA, GL_UNSIGNED_BYTE, null);

		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, fboTexf, 0);

		status = glCheckFramebufferStatus(GL_FRAMEBUFFER) ;
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

		//Large FBO
		glBindFramebuffer(GL_FRAMEBUFFER, fbob);

		glBindRenderbuffer(GL_RENDERBUFFER, renderBufferb);
		glRenderbufferStorage(GL_RENDERBUFFER, GL_RGBA8, (int)width, (int)height);

		glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_RENDERBUFFER, renderBufferb);

		glBindTexture(GL_TEXTURE_2D, fboTexb);

		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, (int)width, (int)height, 0, GL_RGBA, GL_UNSIGNED_BYTE, null);

		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, fboTexb, 0);

		status = glCheckFramebufferStatus(GL_FRAMEBUFFER) ;
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

	public void renderFractal(float sc, int bindFBO) {
		glBindFramebuffer(GL_FRAMEBUFFER, bindFBO);

		glUniform2f(aspectLoc, aspectX, aspectY);

		glUniform1i(maxIterationLoc, maxIteration);

		glUniform1f(scaleLoc, sc);

		glUniform1i(colorSchemeLoc, colorScheme);

		try {
			GameView.semaphore.acquire();

			float scHigh = (float)GameView.totalScale;
			float scLow = (float)(GameView.totalScale - (double)scHigh);

			glUniform2f(scLoc, scHigh, scLow);

			float offXHigh = (float)GameView.offsetX;
			float offYHigh = (float)GameView.offsetY;
			float offXLow = (float)(GameView.offsetX - (double)offXHigh);
			float offYLow = (float)(GameView.offsetY - (double)offYHigh);

			glUniform4f(offLoc, offXHigh, offYHigh, offXLow, offYLow);

			GameView.semaphore.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);

		glUseProgram(0);
	}

	public void onDrawFrame(boolean firstDraw) {
		glClear(GL_COLOR_BUFFER_BIT);

		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);

		glEnableVertexAttribArray(0);
		glVertexAttribPointer(0, 2, GL_FLOAT, false, 2 * 4, 0);

		if(renderMode == -1) {
			glUseProgram(shaderProgram);

			glUniform2f(windowLoc, 0, 0);

			renderFractal(SCALING, fbo);

			glBindFramebuffer(GL_FRAMEBUFFER, fbob);

			glUseProgram(fractalshaderProgram);

			glActiveTexture(GL_TEXTURE0);

			if(renderMode == -1) {
				glBindTexture(GL_TEXTURE_2D, fboTex);
			} else {
				glBindTexture(GL_TEXTURE_2D, fboTexf);
			}

			glUniform3f(transLoc, 0, 0, 1);

			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);

			glBindFramebuffer(GL_FRAMEBUFFER, 0);
		} else if(renderMode > 0) {
			glUseProgram(shaderProgram);

			int posID = renderMode - 1;

			int xPos = posID % 8;
			int yPos = (posID - xPos) / 8;

			glUniform2f(windowLoc, (xPos*2.0f)/8.0f, (yPos*2.0f)/8.0f);

			renderFractal(1.0f, fbof);

			glBindFramebuffer(GL_FRAMEBUFFER, fbob);

			glUseProgram(fractalshaderProgram);

			glActiveTexture(GL_TEXTURE0);

			if (renderMode == -1) {
				glBindTexture(GL_TEXTURE_2D, fboTex);
			} else {
				glBindTexture(GL_TEXTURE_2D, fboTexf);
			}

			glUniform3f(transLoc, (xPos*2.0f - 7.0f)/8.0f, (yPos*2.0f - 7.0f)/8.0f, 8);

			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);

			glBindFramebuffer(GL_FRAMEBUFFER, 0);

			renderMode--;
		}

		glUseProgram(fractalshaderProgram);

		glBindTexture(GL_TEXTURE_2D, fboTexb);

		glUniform3f(transLoc, 0, 0, 1);

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

	public void changeColorScheme() {
		colorScheme++;
		if (colorScheme == 3) colorScheme = 0;
		renderMode = RENDER_UPSCALE;
	}

	public void setIterations(int iterations) {
		maxIteration = iterations;
	}
}
