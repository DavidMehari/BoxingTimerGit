package com.jobu.boxingtimer;

import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.CountDownTimer;

public class MyCountDownTimer extends CountDownTimer {

	public long timeLeft;
	int max;
	int interval;
	private boolean warningPlayed = false;
	int progress = 0;
	
	public MyCountDownTimer(long millisInFuture, long countDownInterval) {
		super(millisInFuture, countDownInterval);
		max = (int) millisInFuture;
		interval = (int) countDownInterval;
	}
	
	

	public int getProgress() {
		return progress;
	}



	public void setProgress(int progress) {
		this.progress = progress;
	}



	@Override
	public void onTick(long millisUntilFinished) {
		// TODO Auto-generated method stub
		timeLeft = millisUntilFinished;
		
		if(timeLeft < 5000 && !warningPlayed ){
			Timer.soundPool.play(Timer.soundsMap.get(Timer.SOUND4), 1, 1, 1, 0, 1);

			warningPlayed = true;
		}
		
		if (timeLeft < 5000 && timeLeft > 1) {
			Timer.counterDisplay.setTextColor(Color.RED);
			
		} else {
			Timer.counterDisplay.setTextColor(Color.WHITE);
		}
		// counterDisplay.setText("" + ((millisUntilFinished / 1000)+1));
		int seconds = (int) (((millisUntilFinished / 1000) + 1) % 60);
		int minutes = (int) (((millisUntilFinished / 1000) + 1) / 60);
		Timer.counterDisplay.setText(String.format("%d:%02d", minutes, seconds));

		//Timer.pB1.setMax(this.max - interval);
		
		//Counting down
		//Timer.pB1.setProgress((int) (millisUntilFinished + progress));
		
		progress = progress + interval;
		Timer.pB1.setProgress((int) (progress));
		;
	}

	@Override
	public void onFinish() {
		Timer.counterDisplay.setTextColor(Color.WHITE);
		
		
	
		if(Timer.isRest){
			Timer.soundPool.play(Timer.soundsMap.get(Timer.SOUND2), 1, 1, 1, 0, 1);

			Timer.startRest();
			
		}else{
			warningPlayed = false;
			Timer.startRound();
			
		}
	}

}
