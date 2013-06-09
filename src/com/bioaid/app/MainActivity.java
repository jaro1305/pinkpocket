package com.bioaid.app;

import java.nio.ByteBuffer;

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
    private static final int AUDIO_SAMPLING_RATE = 44100;
    private static final int AUDIO_BUFFER_SIZE = AudioTrack.getMinBufferSize(AUDIO_SAMPLING_RATE, 
            AUDIO_CHANNEL_CONFIG, 
            AudioFormat.ENCODING_PCM_16BIT);
    private AudioManager audioManager;
    private AudioTrack audioPlayer;
    private AudioRecord audioRecorder;
    private byte[] data;
    private boolean isGoing;
    private boolean isQuitting;
    private boolean started = false;
    private Streamer streamer;

    private void startPlaying() {
        audioPlayer = new AudioTrack(3, 
                44100, 
                2, 
                2, 
                AUDIO_BUFFER_SIZE, 
                1);
        audioPlayer.play();
    }

    private void startRecording() {
        audioRecorder = new AudioRecord(1, 
                44100, 
                2, 
                2, 
                AUDIO_BUFFER_SIZE);
        audioRecorder.startRecording();
        isGoing = true;
        isQuitting = false;
        streamer = new Streamer();
        streamer.start();
    }

    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        setContentView(R.layout.activity_main);
        
        // Initialise variables
        data = new byte[AUDIO_BUFFER_SIZE];
        isGoing = false;
        isQuitting = false;
        
        // Set up the headset monitor
        IntentFilter localIntentFilter = new IntentFilter("android.intent.action.HEADSET_PLUG");
        registerReceiver(new HeadsetStateReceiver(this), localIntentFilter);
        
        // Set up the volume slider
        SeekBar localSeekBar = (SeekBar) findViewById(R.id.seekBar1);
        audioManager = ((AudioManager) getSystemService("audio"));
        int i = audioManager.getStreamMaxVolume(3);
        int j = audioManager.getStreamVolume(3);
        localSeekBar.setMax(i);
        localSeekBar.setProgress(j);
        localSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar paramAnonymousSeekBar,
                    int paramAnonymousInt, 
                    boolean paramAnonymousBoolean) {
                audioManager.setStreamVolume(3, paramAnonymousInt, 21);
            }

            public void onStartTrackingTouch(SeekBar paramAnonymousSeekBar) {
            }

            public void onStopTrackingTouch(SeekBar paramAnonymousSeekBar) {
            }
        });
    }

    private class Streamer extends Thread {
        public void run() {
            BioAidFilterService bafs = new BioAidFilterService();
            while (!isQuitting) {
                if (isGoing) {
                    int i = audioRecorder.read(data, 
                            0,
                            MainActivity.AUDIO_BUFFER_SIZE);
                    if ((audioPlayer.getPlayState() == 3) && (i != -3)) {
                        // Normalise the input to the [-1,1] range
                        float[] input = new float[AUDIO_BUFFER_SIZE / 2]; // assume 2 bytes per sample
                        ByteBuffer bb = ByteBuffer.wrap(data);
                        for(int j = 0; j < AUDIO_BUFFER_SIZE; j += 2) {
                            input[j / 2] = ((float)bb.getShort()) / ((float)Short.MAX_VALUE);
                        }
                        
                        // Process the input
                        float[] output = bafs.processBlock(input);
                        
                        // Scale and then write the output
                        byte[] outputBytes = new byte[AUDIO_BUFFER_SIZE];
                        ByteBuffer bb2 = ByteBuffer.wrap(outputBytes);
                        for(int j = 0; j < (AUDIO_BUFFER_SIZE / 2); j ++) {
                            short value = (short)(output[j] * Short.MAX_VALUE);
                            bb2.putShort(value);
                        }
                        audioPlayer.write(outputBytes, 0, i);
                    }
                }
            }
        }
    }
    
    public void onActionButtonClicked(View paramView) {
        if(!started) {
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
        started = !started;
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
        isGoing = true;
    }

    public void suspend() {
        isGoing = false;
    }
    
    private void stopPlaying() {
        audioPlayer.stop();
        audioPlayer.release();
        audioPlayer = null;
    }

    private void stopRecording() {
        isGoing = false;
        isQuitting = true;
        try {
            streamer.join();
        } catch (InterruptedException localInterruptedException) {
                localInterruptedException.printStackTrace();
        }
        isQuitting = false;
        audioRecorder.stop();
        audioRecorder.release();
        audioRecorder = null;
    }
    
    // Old constants
    private static final int AUDIO_ENCODING = 2;
    private static final int AUDIO_MODE = 1;
    private static final int AUDIO_SOURCE = 1;
    private static final int AUDIO_STREAM_TYPE = 3;
}