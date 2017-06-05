package pro.shpin.kirill.simplerendering;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity {

	private GLSurfaceView glView;

	public static float touchX = 0;
	public static float touchY = 0;
	public static boolean touchDown = false;

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

		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		if(hasGLES20()) {
			glView = new GLSurfaceView(this);
			glView.setEGLContextClientVersion(2);
			glView.setPreserveEGLContextOnPause(true);
			glView.setRenderer(new GLES20Renderer());
			Log.d("debug", "OPENGL!");
		} else {
			Log.d("debug", "Idoit!");
			setContentView(R.layout.activity_renderer);
			return;
		}

		glView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN) {
					touchDown = true;
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					touchDown = false;
				}

				touchX = event.getX();
				touchY = event.getY();

				return true;
			}
		});

		setContentView(glView);
	}
}
