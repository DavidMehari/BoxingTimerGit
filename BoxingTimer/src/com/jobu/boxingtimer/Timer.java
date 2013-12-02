package com.jobu.boxingtimer;

import java.util.HashMap;
import com.google.ads.AdRequest;
import com.google.ads.AdView;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Vibrator;
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
//	- Mute
//	- Skip warm up
//	- Exit (both) stops the timer
//	- Show current settings on the timer screen
//	- Admob
//	- Accelero start
//	- Options with tabs
//	- Options with icons - laucher type
//	- After 1 shake block sensor for 1 sec - unregister
//	- ProgressBar color blue
//	- Vibrate on shake
//	- Exit prompt

//TODO

//	- Exit? Quit? Close?
//	- Run in Notification if counterStarted
//	- Icon
//	- Official Round length check (pro 3-1, amat 2-1 or kids 1,5-1? & 30sec rest for fast paced workout)
//	- Sounds
//	- Alarm at 5 just buzz

//	- Grey & white textcolor (white for clickable/grey for text)
//	- Test on different screen sizes
//	- Shake sensitivity - now too sensitive on sony
//	- Font change?

//	- Comment out all Toast
//	- Hide Test options for later use
//	- Option buttons to ll
//	- Vibrate on 

//BUG
//	- Mute onPause 

//MAYBE LATER
//	- Timer in landscape also?
//	- Add more time variation
//	- Templates

public class Timer extends Activity implements OnClickListener,
		SensorEventListener {

	static TextView counterDisplay, roundsDisplay, roundSettingDisp,
			restSettingDisp;
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
	static boolean counterStarted = false; // is it ticking now
	static boolean counterEverStarted = false; // was it ever started

	boolean reseted = true; // ???
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
	static AudioManager am;
	// skipWarmp
	static boolean skipWU;

	// Accelerometer & SensorManager
	static boolean accel;
	SensorManager sm;
	float ax;
	float ay;
	float az;
	long lastUpdate;
	float last_x;
	float last_y;
	float last_z;
	private static final int SHAKE_THRESHOLD = 800;
	Sensor sensorAccelero;
	final Handler handler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.timer);

		c = getApplicationContext();

		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		initialize();

		// AdMob
		AdView av = (AdView) findViewById(R.id.advert);
		av.loadAd(new AdRequest());
	}

	private void initialize() {

		sPref = getSharedPreferences("timerSettings", 0);

		fontDigitClock = Typeface.createFromAsset(c.getAssets(),
				"digital_8.ttf");
		webSymbol = Typeface.createFromAsset(c.getAssets(), "websymbols.ttf");

		counterDisplay = (TextView) findViewById(R.id.counterDisplay);
		counterDisplay.setTypeface(fontDigitClock);

		roundsDisplay = (TextView) findViewById(R.id.roundsDisplay);
		roundSettingDisp = (TextView) findViewById(R.id.tvRoundSetting);
		restSettingDisp = (TextView) findViewById(R.id.tvRestSetting);

		start = (LinearLayout) findViewById(R.id.llStart_Pause);
		clear = (LinearLayout) findViewById(R.id.llClear);

		start.setOnClickListener(this);
		clear.setOnClickListener(this);

		pB1 = (ProgressBar) findViewById(R.id.progressBar1);
		pB1.setProgressDrawable(getResources().getDrawable(R.drawable.myprogress));

		// DEFAULT
		roundLength = 3 * 60;
		restLength = 1 * 60;
		warmLength = 5;
		totalRounds = 1;
		// DEFAULT COUNTDOWN
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
		
		//icon color
//		startIcon.setTextColor(Color.parseColor("#33B5E5"));
//		clearIcon.setTextColor(Color.parseColor("#33B5E5"));
//		optionsIcon.setTextColor(Color.parseColor("#33B5E5"));
//		exitIcon.setTextColor(Color.parseColor("#33B5E5"));
//		startText.setTextColor(Color.parseColor("#33B5E5"));
//		

		// Wake lock
		PowerManager pM = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wL = pM.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "whatever");

		// soundpool
		soundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
		soundsMap = new HashMap<Integer, Integer>();
		soundsMap.put(SOUND1, soundPool.load(this, R.raw.boxingbellsingle, 1));
		soundsMap.put(SOUND2, soundPool.load(this, R.raw.boxingbelldouble, 1));
		soundsMap.put(SOUND3, soundPool.load(this, R.raw.boxingbell, 1));

		// Accelero
		ax = ay = az = 0;
		sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		if (sm.getSensorList(Sensor.TYPE_ACCELEROMETER).size() != 0) {
			sensorAccelero = sm.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
		}
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
			exit.performClick();
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
			if (!counterEverStarted) {
				startRound();
				counterEverStarted = true;
				break;
			}

			if (!counterStarted) {

				myCountDownTimer.start();
				counterStarted = true;
				startIcon.setText(Html.fromHtml("&#221;"));
				startText.setText("Pause");

			} else {
				myCountDownTimer.cancel();
				long resumeFrom = myCountDownTimer.timeLeft;
				int progress = myCountDownTimer.getProgress();
				myCountDownTimer = new MyCountDownTimer(resumeFrom, 100);

				if (isRest) {
					pB1.setMax((roundLength * 1000) - 100);
				} else {
					pB1.setMax((restLength * 1000) - 100);
				}
				if (roundsDisplay.getText().equals("Warm Up")) {
					pB1.setMax((warmLength * 1000) - 100);
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

			if (!skipWU) {
				currentRounds = 0;
				// myCountDownTimer = new MyCountDownTimer(warmLength * 1000,
				// 100);
				// pB1.setMax((warmLength * 1000) - 100);
			} else {
				currentRounds = 1;
			}
			counterStarted = false;
			reseted = true;
			isRest = true;

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
										// unmute
										if (mute) {
											am.setStreamMute(
													AudioManager.STREAM_MUSIC,
													false);
										}
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
				// unmute
				if (mute) {
					am.setStreamMute(AudioManager.STREAM_MUSIC, false);
				}

				Intent i = new Intent(Timer.this, Options.class);
				startActivity(i);
				overridePendingTransition(android.R.anim.slide_in_left,
						android.R.anim.slide_out_right);
			}
			break;
		case R.id.llExit:
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					this);

			// set title
			alertDialogBuilder.setTitle("Exit");

			// set dialog message
			alertDialogBuilder
					.setMessage("Are you sure?")
					.setCancelable(false)
					.setPositiveButton("Exit",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									myCountDownTimer.cancel();
									finish();
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

			
			break;
		}
	}

	public static void startRest() {
		currentRounds++;

		if (currentRounds <= totalRounds) {
			roundsDisplay.setText("Rest");
			myCountDownTimer = new MyCountDownTimer(restLength * 1000, 100);
			pB1.setMax((restLength * 1000) - 100);
			myCountDownTimer.start();
			counterStarted = true;
			startText.setText("Pause");
			startIcon.setText(Html.fromHtml("&#221;"));
			isRest = false;
		} else {
			roundsDisplay.setText("Finished!");
			counterDisplay.setText("0:00");
			start.setEnabled(false);
		}
	}

	public static void startRound() {

		if (currentRounds == 0) {
			// Warm Up
			roundsDisplay.setText("Warm Up");
			myCountDownTimer = new MyCountDownTimer(warmLength * 1000, 100);
			pB1.setMax((warmLength * 1000) - 100);
			isRest = false;
			currentRounds++;
		} else {
			soundPool.play(soundsMap.get(SOUND3), 1, 1, 1, 0, 1);

			roundsDisplay
					.setText("Round: " + currentRounds + "/" + totalRounds);
			myCountDownTimer = new MyCountDownTimer(roundLength * 1000, 100);
			pB1.setMax((roundLength * 1000) - 100);
			isRest = true;
		}
		myCountDownTimer.start();
		counterStarted = true;
		startText.setText("Pause");
		startIcon.setText(Html.fromHtml("&#221;"));

	}

	@Override
	protected void onResume() {
		super.onResume();

		sPref = getSharedPreferences("timerSettings", 0);
		roundLength = sPref.getInt("roundLength", 3 * 60);
		restLength = sPref.getInt("restLength", 1 * 60);
		totalRounds = sPref.getInt("totalRounds", 1);
		keepScr = sPref.getBoolean("keepScr", true);
		mute = sPref.getBoolean("mute", false);
		skipWU = sPref.getBoolean("skipWU", false);
		accel = sPref.getBoolean("accel", false);

		roundsDisplay.setText("Round: " + currentRounds + "/" + totalRounds);

		int seconds = restLength % 60;
		int minutes = restLength / 60;
		restSettingDisp.setText((String.format("Rest: %d:%02d", minutes,
				seconds)));
		seconds = roundLength % 60;
		minutes = roundLength / 60;
		roundSettingDisp.setText((String.format("Round: %d:%02d", minutes,
				seconds)));

		if (!counterEverStarted) {
			counterDisplay
					.setText((String.format("%d:%02d", minutes, seconds)));
		}
		// keep screen
		if (keepScr) {
			if (!wL.isHeld()) {
				wL.acquire();
			}

		} else {
			if (wL.isHeld()) {
				wL.release();
			}
		}

		// mute
		am = (AudioManager) getApplicationContext().getSystemService(
				Context.AUDIO_SERVICE);

		if (mute) {
			am.setStreamMute(AudioManager.STREAM_MUSIC, true);
		}

		// skip WarmUP
		if (skipWU && !counterEverStarted) {
			currentRounds = 1;
		}
		if (!skipWU && !counterEverStarted) {
			currentRounds = 0;
		}

		// accelero
		if (accel) {
			sm.registerListener(this, sensorAccelero,
					SensorManager.SENSOR_DELAY_GAME);
		//	Toast.makeText(this, "reg", Toast.LENGTH_SHORT).show();
		} else {
			sm.unregisterListener(this);
		//	Toast.makeText(this, "unreg", Toast.LENGTH_SHORT).show();
		}

	}

	@Override
	public void onBackPressed() {
		// super.onBackPressed();
		moveTaskToBack(true); // on back button just hide
	}

	@Override
	protected void onPause() {
		super.onPause();
		// turn off wakelock
		if (wL.isHeld()) {
			wL.release();
		}
		// turn off motion sensor
		sm.unregisterListener(this);

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		clear.performClick();
		// reset volume
		if (mute) {
			am.setStreamMute(AudioManager.STREAM_MUSIC, false);
		}
		// turn off motion sensor
		sm.unregisterListener(this);

	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent event) {

		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			long curTime = System.currentTimeMillis();

			// only allow one update every 100ms.
			if ((curTime - lastUpdate) > 100) {
				long diffTime = (curTime - lastUpdate);
				lastUpdate = curTime;

				ax = event.values[0];
				ay = event.values[1];
				az = event.values[2];

				float speed = Math.abs(ax + ay + (az) - last_x - last_y
						- last_z)
						/ diffTime * 10000;

				if (speed > SHAKE_THRESHOLD) {

					//Toast.makeText(this, "shake detected w/ speed: " + speed,	Toast.LENGTH_SHORT).show();
					start.performClick();
					//Vibrate on move
					Vibrator vb = (Vibrator)   getSystemService(Context.VIBRATOR_SERVICE);
		            vb.vibrate(100);
		            
					sm.unregisterListener(this);

					handler.postDelayed(new Runnable() {
						@Override
						public void run() {
							// Re-register the listener after 1500ms --> only on
							// performclick performclick
							sm.registerListener(Timer.this, sensorAccelero,
									SensorManager.SENSOR_DELAY_GAME);
						}
					}, 1500);

				}
				last_x = ax;
				last_y = ay;
				last_z = az;
			}
		}
	}
}
