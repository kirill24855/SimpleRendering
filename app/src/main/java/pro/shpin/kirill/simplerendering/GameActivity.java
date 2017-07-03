package pro.shpin.kirill.simplerendering;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import javax.microedition.khronos.opengles.GL;

import pro.shpin.kirill.simplerendering.game.GLESRenderer;
import pro.shpin.kirill.simplerendering.game.GameView;

public class GameActivity extends AppCompatActivity {

	private GLSurfaceView glView;
	public static GLESRenderer renderer;

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
			glView.setRenderer(renderer = new GLESRenderer());
		} else {
			return;
		}

		setContentView(R.layout.activity_game_overlay);
		ConstraintLayout layout = (ConstraintLayout) findViewById(R.id.activity_game_overlay);
		layout.addView(glView, 0);

		findViewById(R.id.changeColorsButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				renderer.changeColorScheme();
			}
		});

		final TextView iterationText = (TextView) findViewById(R.id.iterationsText);
		((SeekBar) findViewById(R.id.iterationSlider)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				int iterations = progress;
				//int iterations = (int) Math.pow(2f, (float) progress/100f);
				iterationText.setText(getApplicationContext().getString(R.string.iterationsText, iterations));
				renderer.setIterations(iterations);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				renderer.setRenderMode(GLESRenderer.RENDER_DOWNSCALE);
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				renderer.setRenderMode(GLESRenderer.RENDER_UPSCALE);
			}
		});

		((ToggleButton) findViewById(R.id.doublePrecision)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				renderer.useDoublePrecision(isChecked);
			}
		});
	}
}
