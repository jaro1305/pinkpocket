package com.bioaid.app;

import com.bioaid.BioAidFilterService;
import com.bioaid.app.R;
import android.app.Activity;
import android.content.IntentFilter;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class MainActivity extends Activity {
    private static final int AUDIO_CHANNEL_CONFIG = 2;
    private static final int AUDIO_ENCODING = 2;
    private static final int AUDIO_MODE = 1;
    private static final int AUDIO_SAMPLING_RATE = 44100;
    private static final int AUDIO_SOURCE = 1;
    private static final int AUDIO_STREAM_TYPE = 3;
    private static final int AUDIO_BUFFER_SIZE = AudioTrack.getMinBufferSize(AUDIO_SAMPLING_RATE, AUDIO_CHANNEL_CONFIG, AudioFormat.ENCODING_PCM_16BIT);
    private AudioManager audioManager;
    private AudioTrack audioPlayer;
    private AudioRecord audioRecorder;
    private byte[] data;
    private boolean isGoing;
    private boolean isQuitting;
    private boolean started = false;
    private Streamer streamer;

    private void startPlaying() {
        this.audioPlayer = new AudioTrack(3, 44100, 2, 2, AUDIO_BUFFER_SIZE, 1);
        this.audioPlayer.play();
    }

    private void startRecording() {
        this.audioRecorder = new AudioRecord(1, 44100, 2, 2, AUDIO_BUFFER_SIZE);
        this.audioRecorder.startRecording();
        this.isGoing = true;
        this.isQuitting = false;
        this.streamer = new Streamer();
        this.streamer.start();
    }

    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        setContentView(R.layout.activity_main);
        this.data = new byte[AUDIO_BUFFER_SIZE];
        this.isGoing = false;
        this.isQuitting = false;
        IntentFilter localIntentFilter = new IntentFilter(
                "android.intent.action.HEADSET_PLUG");
        registerReceiver(new HeadsetStateReceiver(this), localIntentFilter);
        this.audioManager = ((AudioManager) getSystemService("audio"));
        int i = this.audioManager.getStreamMaxVolume(3);
        int j = this.audioManager.getStreamVolume(3);
        SeekBar localSeekBar = (SeekBar) findViewById(R.id.seekBar1);
        localSeekBar.setMax(i);
        localSeekBar.setProgress(j);
        localSeekBar
                .setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    public void onProgressChanged(
                            SeekBar paramAnonymousSeekBar,
                            int paramAnonymousInt, boolean paramAnonymousBoolean) {
                        MainActivity.this.audioManager.setStreamVolume(3,
                                paramAnonymousInt, 21);
                    }

                    public void onStartTrackingTouch(
                            SeekBar paramAnonymousSeekBar) {
                    }

                    public void onStopTrackingTouch(
                            SeekBar paramAnonymousSeekBar) {
                    }
                });
    }

    private class Streamer extends Thread {
        public void run() {
            BioAidFilterService bafs = new BioAidFilterService();
            while (!isQuitting) {
                if (isGoing) {
                    int i = MainActivity.this.audioRecorder.read(
                            MainActivity.this.data, 0,
                            MainActivity.AUDIO_BUFFER_SIZE);
                    if ((MainActivity.this.audioPlayer.getPlayState() == 3)
                            && (i != -3)) {
                        // Processing goes on here
                        double[] leftChannel = new double[AUDIO_BUFFER_SIZE / 2]; // assume 2 bytes per sample
                        for(int j = 0; j < AUDIO_BUFFER_SIZE; j += 2) {
                            leftChannel[j / 2] = (double)((data[j] & 0xff) | (data[j + 1] & 0xff) << 8);
                        }
                        double[] output = bafs.processBlock(leftChannel);
                        byte[] outputBytes = new byte[AUDIO_BUFFER_SIZE];
                        for(int j = 0; j < (AUDIO_BUFFER_SIZE / 2); j ++) {
                            outputBytes[(j * 2)] = 0;
                            outputBytes[(j * 2) + 1] = 0;
                        }
                        
                        MainActivity.this.audioPlayer.write(
                                MainActivity.this.data, 0, i);
                    }
                }
            }
        }
    }
    
    public void onActionButtonClicked(View paramView) {
        if(!this.started) {
            startRecording();
            startPlaying();
            paramView.setBackgroundResource(R.drawable.onbutton);
            paramView.setContentDescription("On");
        } else {
            stopRecording();
            stopPlaying();
            paramView.setBackgroundResource(R.drawable.offbutton);
            paramView.setContentDescription("Off");
        }
        this.started = !started;
    }
    
    public boolean onCreateOptionsMenu(Menu paramMenu) {
        getMenuInflater().inflate(R.menu.main, paramMenu);
        return true;
    }

    public void onHelpButtonClicked(View paramView) {
    }

    public void onSettingsButtonClicked(View paramView) {
    }

    public void resume() {
        this.isGoing = true;
    }

    public void suspend() {
        this.isGoing = false;
    }
    
    private void stopPlaying() {
        this.audioPlayer.stop();
        this.audioPlayer.release();
        this.audioPlayer = null;
    }

    private void stopRecording() {
        this.isGoing = false;
        this.isQuitting = true;
        try {
            this.streamer.join();
        } catch (InterruptedException localInterruptedException) {
                localInterruptedException.printStackTrace();
        }
        this.isQuitting = false;
        this.audioRecorder.stop();
        this.audioRecorder.release();
        this.audioRecorder = null;
    }
}