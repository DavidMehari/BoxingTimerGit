package com.jobu.boxingtimer;

import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TabHost;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;

public class Options extends Activity implements OnClickListener,
		OnCheckedChangeListener, OnItemSelectedListener,
		OnSeekBarChangeListener, CompoundButton.OnCheckedChangeListener {

	RadioGroup rgRoundLength, rgRestLength;
	RadioButton selectedRadioButton;
	Spinner spTotalRounds;
	Button bOK, bCancel;
	SharedPreferences sPref;
	SeekBar sbVolume;
	static AudioManager am;
	CheckBox cbMute, cbSkipWU, cbKeepScr, cbAccel;

	// from Timer
	private int roundLength;
	private int restLength;
	private int totalRounds;
	// keep screen
	private boolean keepScr;
	// mute
	private boolean mute;
	// skipWarmp
	private boolean skipWU;
	// accelero
	private boolean accel;

	static SoundPool soundPool;
	static HashMap<Integer, Integer> soundsMap;
	static int SOUND1 = 1;
	
	//Tabs
	TabHost th;
	TabSpec tabSpecs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.options);
		
		th = (TabHost)findViewById(R.id.tabhost);
		th.setup();
		
		tabSpecs = th.newTabSpec("tabTimeTag");
		tabSpecs.setContent(R.id.tabTime);
		tabSpecs.setIndicator("Timer");
		
		th.addTab(tabSpecs);
		
		tabSpecs = th.newTabSpec("tabVolTag");
		tabSpecs.setContent(R.id.tabVol);
		tabSpecs.setIndicator("Volume");
		
		th.addTab(tabSpecs);
		
		tabSpecs = th.newTabSpec("tabAppTag");
		tabSpecs.setContent(R.id.tabApp);
		tabSpecs.setIndicator("App");
		
		th.addTab(tabSpecs);

	}

	private void init() {
		sPref = getSharedPreferences("timerSettings", 0);
		roundLength = sPref.getInt("roundLength", 3 * 60);
		restLength = sPref.getInt("restLength", 1 * 60);
		totalRounds = sPref.getInt("totalRounds", 1);
		keepScr = sPref.getBoolean("keepScr", true);
		mute = sPref.getBoolean("mute", false);
		skipWU = sPref.getBoolean("skipWU", false);
		accel = sPref.getBoolean("accel", false);

		rgRoundLength = (RadioGroup) findViewById(R.id.roundLengthGroup);
		rgRestLength = (RadioGroup) findViewById(R.id.restLengthGroup);

		rgRoundLength.setOnCheckedChangeListener(this);
		rgRestLength.setOnCheckedChangeListener(this);

		spTotalRounds = (Spinner) findViewById(R.id.spRounds);
		Integer[] totalRoundsArray = new Integer[20];
		for (int x = 0; x < 20; x++) {
			totalRoundsArray[x] = x + 1;
		}
		ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(this,
				android.R.layout.simple_spinner_item, totalRoundsArray);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spTotalRounds.setAdapter(adapter);
		spTotalRounds.setOnItemSelectedListener(this);

		bOK = (Button) findViewById(R.id.bOK);
		bCancel = (Button) findViewById(R.id.bCancel);
		bOK.setOnClickListener(this);
		bCancel.setOnClickListener(this);

		// Volume Control
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		sbVolume = (SeekBar) findViewById(R.id.seekBarVolume);
		am = (AudioManager) getApplicationContext().getSystemService(
				Context.AUDIO_SERVICE);

		int maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		int currentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);

		sbVolume.setMax(maxVolume);
		sbVolume.setProgress(currentVolume);
		sbVolume.setOnSeekBarChangeListener(this);

		// soundpool
		soundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
		soundsMap = new HashMap<Integer, Integer>();
		soundsMap.put(SOUND1, soundPool.load(this, R.raw.boxingbellsingle, 1));

		cbMute = (CheckBox) findViewById(R.id.cbMute);
		cbSkipWU = (CheckBox) findViewById(R.id.cbSkipWarmUp);
		cbKeepScr = (CheckBox) findViewById(R.id.cbKeepScreen);
		cbAccel = (CheckBox) findViewById(R.id.cbAccel);

		cbMute.setOnCheckedChangeListener(this);
		cbSkipWU.setOnCheckedChangeListener(this);
		cbKeepScr.setOnCheckedChangeListener(this);
		cbAccel.setOnCheckedChangeListener(this);
	}

	private void loadPreferences() {

		switch (roundLength) {
		case 180:
			rgRoundLength.check(R.id.rbroundLength3min);
			break;
		case 120:
			rgRoundLength.check(R.id.rbroundLength2min);
			break;
		case 10:
			rgRoundLength.check(R.id.rbroundLengthTest);
			break;
		}

		switch (restLength) {
		case 60:
			rgRestLength.check(R.id.rbrestLength1min);
			break;
		case 30:
			rgRestLength.check(R.id.rbrestLength30sec);
			break;
		case 10:
			rgRestLength.check(R.id.rbrestLengthTest);
			break;
		}
		// setting the spinner state
		spTotalRounds.setSelection(totalRounds - 1);

		// load screen settings
		if (keepScr) {
			cbKeepScr.setChecked(true);
		} else {
			cbKeepScr.setChecked(false);
		}

		// load mute settings
		if (mute) {
			cbMute.setChecked(true);
		} else {
			cbMute.setChecked(false);
		}

		// load skip settings
		if (skipWU) {
			cbSkipWU.setChecked(true);
		} else {
			cbSkipWU.setChecked(false);
		}

		// load accelero settings
		if (accel) {
			cbAccel.setChecked(true);
		} else {
			cbAccel.setChecked(false);
		}

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.bOK:
			SharedPreferences.Editor editor = sPref.edit();
			editor.putInt("roundLength", roundLength);
			editor.putInt("restLength", restLength);
			editor.putInt("totalRounds", totalRounds);
			editor.putBoolean("keepScr", keepScr);
			editor.putBoolean("mute", mute);
			editor.putBoolean("skipWU", skipWU);
			editor.putBoolean("accel", accel);

			editor.commit();

			// because of setStreamMute bug
			am.setStreamMute(AudioManager.STREAM_MUSIC, false);

			finish();
			overridePendingTransition(R.anim.slide_in_right,
					R.anim.slide_out_left);

			break;

		case R.id.bCancel:

			// because of setStreamMute bug
			am.setStreamMute(AudioManager.STREAM_MUSIC, false);

			finish();
			overridePendingTransition(R.anim.slide_in_right,
					R.anim.slide_out_left);
			break;
		}
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {

		switch (checkedId) {
		case R.id.rbroundLength3min:
			roundLength = 3 * 60;
			break;
		case R.id.rbroundLength2min:
			roundLength = 2 * 60;
			break;
		case R.id.rbroundLengthTest:
			roundLength = 10;
			break;
		case R.id.rbrestLength1min:
			restLength = 1 * 60;
			break;
		case R.id.rbrestLength30sec:
			restLength = 30;
			break;
		case R.id.rbrestLengthTest:
			restLength = 10;
			break;
		}

	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {

		totalRounds = arg2 + 1;

	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onBackPressed() {

		super.onBackPressed();
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
	}

	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// TODO Auto-generated method stub
		am.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);

	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

		soundPool.play(soundsMap.get(SOUND1), 1, 1, 1, 0, 1);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub
		switch (buttonView.getId()) {
		case R.id.cbMute:
			if (isChecked) {
				mute = true;
				am.setStreamMute(AudioManager.STREAM_MUSIC, true);
			} else {
				mute = false;
				am.setStreamMute(AudioManager.STREAM_MUSIC, false);
			}
			break;

		case R.id.cbSkipWarmUp:
			if (isChecked) {
				skipWU = true;
			} else {
				skipWU = false;
			}
			break;

		case R.id.cbKeepScreen:
			if (isChecked) {
				keepScr = true;
			} else {
				keepScr = false;
			}
			break;
		case R.id.cbAccel:
			if (isChecked) {
				accel = true;
			} else {
				accel = false;
			}
			break;
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		// because of setStreamMute bug
		am.setStreamMute(AudioManager.STREAM_MUSIC, false);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		init();

		loadPreferences();

	}

}
