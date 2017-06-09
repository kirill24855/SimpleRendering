package pro.shpin.kirill.simplerendering;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

public class MainActivity extends AppCompatActivity {

	private AdView adView;

	public AppCompatActivity getThisReference() {
		return this;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		MobileAds.initialize(this, "ca-app-pub-9547939337076978~2610215040");
		adView = (AdView) findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest .Builder()
											.addTestDevice(AdRequest.DEVICE_ID_EMULATOR) // This line and the following are for testing purposes
											.addTestDevice("223D09564A4F391D6865A10891FE0A7D")// TODO Remove before posting app
											.addTestDevice("CCC5236CC2AD761D21F14E75347D7A01")
											.build();

		adView.loadAd(adRequest);

		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		Button gameButton = (Button)findViewById(R.id.button);
		gameButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(getThisReference(), GameActivity.class));
			}
		});

		Button aboutButton = (Button) findViewById(R.id.button2);
		aboutButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(getThisReference(), AboutActivity.class));
			}
		});
	}
}
