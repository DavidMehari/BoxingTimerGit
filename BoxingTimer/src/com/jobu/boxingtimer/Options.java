package com.jobu.boxingtimer;

import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;

public class Options extends Activity implements OnClickListener, OnCheckedChangeListener, OnItemSelectedListener, OnSeekBarChangeListener{

	RadioGroup rgRoundLength, rgRestLength;
	RadioButton selectedRadioButton;
	Spinner spTotalRounds;
	Button bOK, bCancel;
	SharedPreferences sPref;
	SeekBar sbVolume;
	AudioManager am;
	
	//from Timer
	private int roundLength;
	private int restLength;
	private int totalRounds;
	
	static SoundPool soundPool;
	static HashMap<Integer, Integer> soundsMap;
	static int SOUND1 = 1; 
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.options);
		
		init();
		
		loadPreferences();
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
		
		switch(restLength){
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
		//setting the spinner state
		spTotalRounds.setSelection(totalRounds - 1);
		
	}

	private void init() {
		sPref = getSharedPreferences("timerSettings", 0);
		roundLength = sPref.getInt("roundLength", 3 * 60);
		restLength = sPref.getInt("restLength", 1 * 60);
		totalRounds = sPref.getInt("totalRounds", 1);
		
		Toast t = Toast.makeText(getApplicationContext(), "load done" + roundLength + " - " + restLength + " - " + totalRounds,
				Toast.LENGTH_SHORT);
		t.show();
		
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
		
		bOK = (Button)findViewById(R.id.bOK);
		bCancel = (Button)findViewById(R.id.bCancel);
		bOK.setOnClickListener(this);
		bCancel.setOnClickListener(this);
		
		//Volume Control
		setVolumeControlStream(AudioManager.STREAM_MUSIC);		
		sbVolume = (SeekBar)findViewById(R.id.seekBarVolume);
		am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		
		int maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		int currentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
		
		sbVolume.setMax(maxVolume);
		sbVolume.setProgress(currentVolume);
		sbVolume.setOnSeekBarChangeListener(this);
		
		//soundpool
		soundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
		soundsMap = new HashMap<Integer, Integer>();
		soundsMap.put(SOUND1, soundPool.load(this, R.raw.boxingbellsingle, 1));
		
		
		
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
			
			editor.commit();    
			 
			finish();
			overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left );		
			
			break;
		
		case R.id.bCancel:
			
			finish();
			overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left );
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
//		if (!counterStarted && reseted) {
//			myCountDownTimer = new MyCountDownTimer(warmLength * 1000, 100);
//		}
		
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {

		totalRounds = arg2 + 1;
	//	roundsDisplay.setText("Rounds: " + currentRounds + "/" + totalRounds);
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onBackPressed() {
		
		super.onBackPressed();
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left );
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
	
	
}