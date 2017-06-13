package pro.shpin.kirill.simplerendering;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import pro.shpin.kirill.simplerendering.game.GLESRenderer;
import pro.shpin.kirill.simplerendering.game.GameView;

public class GameActivity extends AppCompatActivity {

	private GLSurfaceView glView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		glView = new GameView(this);
		glView.setEGLContextClientVersion(2);
		glView.setPreserveEGLContextOnPause(true);
		glView.setRenderer(new GLESRenderer());

		setContentView(glView);
	}
}
