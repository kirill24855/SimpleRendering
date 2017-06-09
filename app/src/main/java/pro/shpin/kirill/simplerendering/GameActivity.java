package pro.shpin.kirill.simplerendering;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import pro.shpin.kirill.simplerendering.game.GLES20Renderer;
import pro.shpin.kirill.simplerendering.game.Matrix3f;

public class GameActivity extends AppCompatActivity {

	private GLSurfaceView glView;

	public static float originX = 0;
	public static float originY = 0;
	public static boolean originDown = false;

	public static float pinX = 0;
	public static float pinY = 0;
	public static boolean pinDown = false;

	public static Matrix3f transform = new Matrix3f();

	private boolean hasGLES20() {
		ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		ConfigurationInfo info = am.getDeviceConfigurationInfo();
		return info.reqGlEsVersion >= 0x20000;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		//this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		if(hasGLES20()) {
			glView = new GLSurfaceView(this);
			glView.setEGLContextClientVersion(2);
			glView.setPreserveEGLContextOnPause(true);
			glView.setRenderer(new GLES20Renderer());
		} else {
			return;
		}

		glView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int index = event.getActionIndex();

				int action = event.getAction();

				if(index == 0) {
					originX = (((((event.getX()/GLES20Renderer.width) - 0.5f) * 2) * GLES20Renderer.aspectX) + 1)/2.0f;
					originY = (((((event.getY()/GLES20Renderer.height) - 0.5f) * 2) * GLES20Renderer.aspectY) + 1)/2.0f;
					originY = 1 - originY;

					if(action == MotionEvent.ACTION_DOWN) {
						originDown = true;
					} else if (action == MotionEvent.ACTION_UP) {
						originDown = false;
					}
				}



				return true;
			}
		});

		setContentView(glView);
	}
}
