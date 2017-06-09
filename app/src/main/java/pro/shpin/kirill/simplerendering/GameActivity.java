package pro.shpin.kirill.simplerendering;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import pro.shpin.kirill.simplerendering.game.GLES20Renderer;
import pro.shpin.kirill.simplerendering.game.GameView;
import pro.shpin.kirill.simplerendering.game.Matrix3f;

public class GameActivity extends AppCompatActivity {

	private GLSurfaceView glView;

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
			glView = new GameView(this);
			glView.setEGLContextClientVersion(2);
			glView.setPreserveEGLContextOnPause(true);
			glView.setRenderer(new GLES20Renderer());
		} else {
			return;
		}

		setContentView(glView);
	}
}
