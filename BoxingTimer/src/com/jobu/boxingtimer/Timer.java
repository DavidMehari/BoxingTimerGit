package com.jobu.boxingtimer;

import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.RadioGroup.OnCheckedChangeListener;

//DONE
//	- Progress bar under the time
//	- Volume control
//	- Fixed orientation
//	- Change buttons to layouts
//	- Monospace digital clock
//	- Screen Lock


//TODO
//	- Stop watch
//	- Skip warm up
//	- Mute
//	- Show current settings on the timer screen (with icons)
//	- Run in Notification if started
//	- Exit (both) finish everything
//	- Run in background problems
//	- Sounds
//	- Exit prompt
//	- Options with tabs?
//	- Timer in landscape also?
//	- Admob
//	- Start with accelero
//	- Grey & white textcolor (white for clickable/grey for text)

public class Timer extends Activity implements OnClickListener {

	static TextView counterDisplay, roundsDisplay;
	Typeface fontDigitClock, webSymbol;

	static ProgressBar pB1;

	static TextView startText;
	static TextView startIcon;

	static TextView clearIcon;
	static TextView optionsIcon;
	static TextView exitIcon;

	static LinearLayout start;
	LinearLayout clear;
	LinearLayout options;
	LinearLayout exit;

	static MyCountDownTimer myCountDownTimer;
	static boolean counterStarted = false;
	static boolean counterEverStarted = false;

	boolean reseted = true;
	static boolean isRest = true;

	static int roundLength;
	static int restLength;
	static int warmLength;
	static int totalRounds;
	static int currentRounds = 0;

	static Context c;

	static SoundPool soundPool;
	static HashMap<Integer, Integer> soundsMap;
	static int SOUND1 = 1; // alert 5sec
	static int SOUND2 = 2; // end round
	static int SOUND3 = 3; // start round

	SharedPreferences sPref;

	WakeLock wL;

	// keep screen
	static boolean keepScr;
	// mute
	static boolean mute;
	// skipWarmp
	static boolean skipWU;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.timer);

		c = getApplicationContext();

		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		initialize();

		// soundpool
		soundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
		soundsMap = new HashMap<Integer, Integer>();
		soundsMap.put(SOUND1, soundPool.load(this, R.raw.boxingbellsingle, 1));
		soundsMap.put(SOUND2, soundPool.load(this, R.raw.boxingbelldouble, 1));
		soundsMap.put(SOUND3, soundPool.load(this, R.raw.boxingbell, 1));

	}

	private void initialize() {

		sPref = getSharedPreferences("timerSettings", 0);

		fontDigitClock = Typeface.createFromAsset(c.getAssets(),
				"digital_8.ttf");
		webSymbol = Typeface.createFromAsset(c.getAssets(), "websymbols.ttf");

		counterDisplay = (TextView) findViewById(R.id.counterDisplay);
		counterDisplay.setTypeface(fontDigitClock);

		roundsDisplay = (TextView) findViewById(R.id.roundsDisplay);

		start = (LinearLayout) findViewById(R.id.llStart_Pause);
		clear = (LinearLayout) findViewById(R.id.llClear);

		start.setOnClickListener(this);
		clear.setOnClickListener(this);

		pB1 = (ProgressBar) findViewById(R.id.progressBar1);

		// DEFAULT
		roundLength = 3 * 60;
		restLength = 1 * 60;
		warmLength = 15;
		totalRounds = 1;
		// Warm Up
		myCountDownTimer = new MyCountDownTimer(warmLength * 1000, 100);
		pB1.setMax((warmLength * 1000) - 100);

		options = (LinearLayout) findViewById(R.id.llOptions);
		options.setOnClickListener(this);

		exit = (LinearLayout) findViewById(R.id.llExit);
		exit.setOnClickListener(this);

		startText = (TextView) findViewById(R.id.tvStart_Pause);
		startIcon = (TextView) findViewById(R.id.tvStartIcon);
		startIcon.setTypeface(webSymbol);

		clearIcon = (TextView) findViewById(R.id.tvClearIcon);
		clearIcon.setTypeface(webSymbol);

		optionsIcon = (TextView) findViewById(R.id.tvOptionsIcon);
		optionsIcon.setTypeface(webSymbol);

		exitIcon = (TextView) findViewById(R.id.tvExitIcon);
		exitIcon.setTypeface(webSymbol);

		// Wake lock
		PowerManager pM = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wL = pM.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "whatever");

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.timer_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.exit:
			finish();
			break;

		case R.id.options:
			options.performClick();
			break;
		}

		return false;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llStart_Pause:
			counterEverStarted = true;

			if (!counterStarted) {

				myCountDownTimer.start();
				counterStarted = true;
				startIcon.setText(Html.fromHtml("&#221;"));
				startText.setText("Pause");
				// if warm up
				if (currentRounds == 0) {
					roundsDisplay.setText("Warm Up!");
					isRest = false;
				}

			} else {
				myCountDownTimer.cancel();
				long resumeFrom = myCountDownTimer.timeLeft;
				int progress = myCountDownTimer.getProgress();
				myCountDownTimer = new MyCountDownTimer(resumeFrom, 100);

				if (isRest) {
					pB1.setMax((roundLength * 1000) - 100);
				} else if (currentRounds == 0) {
					pB1.setMax((warmLength * 1000) - 100);
				} else {
					pB1.setMax((restLength * 1000) - 100);
				}
				myCountDownTimer.setProgress(progress);

				startIcon.setText(Html.fromHtml("&#218;"));
				startText.setText("Resume");
				counterStarted = false;
				reseted = false;
			}
			break;
		case R.id.llClear:
			counterEverStarted = false;

			myCountDownTimer.cancel();

			myCountDownTimer = new MyCountDownTimer(warmLength * 1000, 100);
			pB1.setMax((warmLength * 1000) - 100);

			counterStarted = false;
			reseted = true;
			isRest = true;
			currentRounds = 0;
			roundsDisplay
					.setText("Round: " + currentRounds + "/" + totalRounds);
			counterDisplay.setTextColor(Color.WHITE);
			// counterDisplay.setText("0:00");
			int seconds = roundLength % 60;
			int minutes = roundLength / 60;
			counterDisplay
					.setText((String.format("%d:%02d", minutes, seconds)));

			startIcon.setText(Html.fromHtml("&#218;"));
			startText.setText("Start");
			pB1.setProgress(0);
			start.setEnabled(true);
			break;

		case R.id.llOptions:

			if (counterEverStarted) {
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
						this);

				// set title
				alertDialogBuilder.setTitle("Alert");

				// set dialog message
				alertDialogBuilder
						.setMessage("This will reset your current timer!")
						.setCancelable(false)
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										clear.performClick();
										Intent i = new Intent(c, Options.class);
										startActivity(i);
										overridePendingTransition(
												android.R.anim.slide_in_left,
												android.R.anim.slide_out_right);
									}
								})
						.setNegativeButton("Cancel",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {

										dialog.cancel();
									}
								});
				AlertDialog alertDialog = alertDialogBuilder.create();
				alertDialog.show();

			} else {
				Intent i = new Intent(Timer.this, Options.class);
				startActivity(i);
				overridePendingTransition(android.R.anim.slide_in_left,
						android.R.anim.slide_out_right);
			}
			break;
		case R.id.llExit:
			myCountDownTimer.cancel();
			finish();
			break;
		}
	}

	public static void startRest() {
		if (currentRounds < totalRounds) {
			roundsDisplay.setText("Rest: " + (currentRounds) + "/"
					+ totalRounds);
			myCountDownTimer = new MyCountDownTimer(restLength * 1000, 100);
			pB1.setMax((restLength * 1000) - 100);
			myCountDownTimer.start();
			counterStarted = true;
			startText.setText("Pause");
			startIcon.setText(Html.fromHtml("&#221;"));
			isRest = false;
		} else {
			roundsDisplay.setText("Workout Finished!");
			counterDisplay.setText("0:00");
			start.setEnabled(false);
		}
	}

	public static void startRound() {
		soundPool.play(soundsMap.get(SOUND3), 1, 1, 1, 0, 1);

		currentRounds++;
		roundsDisplay.setText("Round: " + currentRounds + "/" + totalRounds);
		myCountDownTimer = new MyCountDownTimer(roundLength * 1000, 100);
		pB1.setMax((roundLength * 1000) - 100);
		myCountDownTimer.start();
		counterStarted = true;
		startText.setText("Pause");
		startIcon.setText(Html.fromHtml("&#221;"));
		isRest = true;

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		sPref = getSharedPreferences("timerSettings", 0);
		roundLength = sPref.getInt("roundLength", 3 * 60);
		restLength = sPref.getInt("restLength", 1 * 60);
		totalRounds = sPref.getInt("totalRounds", 1);
		keepScr = sPref.getBoolean("keepScr", false);
		mute = sPref.getBoolean("mute", false);
		skipWU = sPref.getBoolean("skipWU", false);

		Toast t = Toast.makeText(getApplicationContext(), "load done"
				+ roundLength + " - " + restLength + " - " + totalRounds,
				Toast.LENGTH_SHORT);
		t.show();

		roundsDisplay.setText("Round: " + currentRounds + "/" + totalRounds);

		int seconds = roundLength % 60;
		int minutes = roundLength / 60;
		counterDisplay.setText((String.format("%d:%02d", minutes, seconds)));

		if (keepScr) {
			if (!wL.isHeld()) {
				wL.acquire();
			}
			
		} else {
			if (wL.isHeld()) {
				wL.release();
			}
		}

		super.onResume();

	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		
		if (wL.isHeld()) {
			wL.release();
		}
		super.onPause();

	}
}
