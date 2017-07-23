package pro.shpin.kirill.simplerendering;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

	public static GLSurfaceView glView;
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

		final Button changeColorButton = (Button)findViewById(R.id.changeColorsButton);
		final TextView iterationText = (TextView) findViewById(R.id.iterationsText);
		final SeekBar iterationsSlider = (SeekBar) findViewById(R.id.iterationSlider);
		final ToggleButton dpCheck = (ToggleButton) findViewById(R.id.toggleButton);
		final TextView dpText = (TextView) findViewById(R.id.textView2);
		final ToggleButton showButton = (ToggleButton) findViewById(R.id.toggleButton2);

		changeColorButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				renderer.changeColorScheme();
			}
		});

		iterationsSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				int iterations = progress;
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

		dpCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				renderer.useDoublePrecision(isChecked);
			}
		});

		showButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
				if(isChecked) {
					changeColorButton.setVisibility(View.INVISIBLE);
					iterationText.setVisibility(View.INVISIBLE);
					iterationsSlider.setVisibility(View.INVISIBLE);
					dpCheck.setVisibility(View.INVISIBLE);
					dpText.setVisibility(View.INVISIBLE);
				} else {

					changeColorButton.setVisibility(View.VISIBLE);
					iterationText.setVisibility(View.VISIBLE);
					iterationsSlider.setVisibility(View.VISIBLE);
					dpCheck.setVisibility(View.VISIBLE);
					dpText.setVisibility(View.VISIBLE);
				}
			}
		});

		showButton.setText("Hide UI");
		showButton.setTextOff("Hide UI");
		showButton.setTextOn("Show UI");

		final int preferedVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
				| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
				| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_FULLSCREEN
				| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

		final View decorView = getWindow().getDecorView();
		decorView.setOnSystemUiVisibilityChangeListener
				(new View.OnSystemUiVisibilityChangeListener() {
					@Override
					public void onSystemUiVisibilityChange(int visibility) {
						if((visibility & preferedVisibility) != preferedVisibility) {
							decorView.setSystemUiVisibility(preferedVisibility);
						}
					}
				});

		decorView.setSystemUiVisibility(preferedVisibility);
	}
}
