package com.soundbyte.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.*;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;

import java.util.ArrayList;

public class MainActivity extends Activity {
    private static final boolean USE_C_LIBRARY = true;
    
    // Load the BioAid library
    static {
        System.loadLibrary("bioaid"); // "libbioaid.so"
    }
    
    // Constants
    private static final int AUDIO_CHANNEL_CONFIG = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    private static final int AUDIO_SOURCE = 1;
    private static final int AUDIO_STREAM_TYPE = AudioManager.STREAM_MUSIC;
    private static final int AUDIO_MODE = AudioTrack.MODE_STREAM;
    public static final int AUDIO_SAMPLING_RATE = 8000; //44100;
    private static final int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private static final int lastN = 10; // number of samples for getting the volume
    private static final int AUDIO_BUFFER_SIZE = AudioTrack.getMinBufferSize(AUDIO_SAMPLING_RATE, 
            AUDIO_CHANNEL_CONFIG,
            AUDIO_ENCODING); // Effective time length of buffer

    // Variables
    private AudioManager audioManager;
    private boolean isGoing;
    private boolean isQuitting;
    private boolean started;
    private Streamer streamer;
    private HeadsetStateReceiver hsr;
    private ArrayList<Integer> inLevels;
    private ArrayList<Integer> outLevels;
    private Handler equaliserHandler;
    private double oldInLevel;
    private double oldOutLevel;

    private void startBioAid() {
        isGoing = true;
        isQuitting = false;
        streamer = new Streamer();
        streamer.start();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(hsr);
    }

    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        setContentView(R.layout.activity_main);
        
        // Initialise variables
        oldInLevel = 1; // Start at 100%
        oldOutLevel = 1; // Start at 100%
        started = false;
        isGoing = false;
        isQuitting = false;
        
        // Populate equaliser view lists, so they can be updated when the
        // volume changes
        inLevels = new ArrayList<Integer>();
        outLevels = new ArrayList<Integer>();
        inLevels.add(R.id.level1a);
        inLevels.add(R.id.level2a);
        inLevels.add(R.id.level3a);
        inLevels.add(R.id.level4a);
        inLevels.add(R.id.level5a);
        inLevels.add(R.id.level6a);
        inLevels.add(R.id.level7a);
        inLevels.add(R.id.level8a);
        inLevels.add(R.id.level9a);
        inLevels.add(R.id.level10a);
        inLevels.add(R.id.level11a);
        inLevels.add(R.id.level12a);
        inLevels.add(R.id.level13a);
        outLevels.add(R.id.level1b);
        outLevels.add(R.id.level2b);
        outLevels.add(R.id.level3b);
        outLevels.add(R.id.level4b);
        outLevels.add(R.id.level5b);
        outLevels.add(R.id.level6b);
        outLevels.add(R.id.level7b);
        outLevels.add(R.id.level8b);
        outLevels.add(R.id.level9b);
        outLevels.add(R.id.level10b);
        outLevels.add(R.id.level11b);
        outLevels.add(R.id.level12b);
        outLevels.add(R.id.level13b);
        
        // Set up the headset monitor
        IntentFilter localIntentFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        hsr = new HeadsetStateReceiver(this);
        registerReceiver(hsr, localIntentFilter);
        
        // Set up the volume slider
        SeekBar localSeekBar = (SeekBar) findViewById(R.id.seekBar1);
        audioManager = ((AudioManager) getSystemService(Context.AUDIO_SERVICE));
        int maxVolume = audioManager.getStreamMaxVolume(AUDIO_STREAM_TYPE);
        int currentVolume = audioManager.getStreamVolume(AUDIO_STREAM_TYPE);
        localSeekBar.setMax(maxVolume);
        localSeekBar.setProgress(currentVolume);
        localSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar,
                    int paramAnonymousInt, 
                    boolean paramAnonymousBoolean) {
                audioManager.setStreamVolume(AUDIO_STREAM_TYPE, 
                        paramAnonymousInt, 
                        21);     // TODO: resolve constant to flags
            }

            public void onStartTrackingTouch(SeekBar paramAnonymousSeekBar) {
            }

            public void onStopTrackingTouch(SeekBar paramAnonymousSeekBar) {
            }
        });
        
        // Set up the equaliser handler
        equaliserHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Bundle bundle = msg.getData();
                float inAmplitude = bundle.getFloat("in");
                float outAmplitude = bundle.getFloat("out");
                updateEqualiser(inAmplitude, outAmplitude);
            }
        };
    }

    private class Streamer extends Thread {
        public void run() {
            startProcessing();
        }
    }
    
    public void updateEqualiser(float inAmplitude, float outAmplitude) {
        int maxLevels = 13;
        
        // Compute effective decibels from amplitudes
        double inLevel = Math.max(1, (inAmplitude * 1000F)); // 0 as first param would give -inf result in log
        inLevel = Math.log10(inLevel) / 3;
        double outLevel = Math.max(1, (outAmplitude * 1000F));
        outLevel = Math.log10(outLevel) / 3;

        // Get the updated IN decibels and OUT decibels, using smoothing
        double inCombination = (0.25 * inLevel) + (0.75 * oldInLevel); // smoothing
        double outCombination = (0.25 * outLevel) + (0.75 * oldOutLevel); // smoothing
        
        // Set the equaliser
        for(int k = 0; k < maxLevels; k++) {
            // Get the equaliser views
            ImageView inView = (ImageView)findViewById(inLevels.get(k));
            ImageView outView = (ImageView)findViewById(outLevels.get(k));
            
            // Get the current percentage
            double percentage = ((double)k) / ((double)(maxLevels - 1));
            
            // Set the views as visible or not, depending on whether the
            // percentage was exceeded
            if((k == 0) || (inCombination > percentage)) {
                inView.setVisibility(View.VISIBLE);
            } else {
                inView.setVisibility(View.INVISIBLE);
            }
            if((k == 0) || (outCombination > percentage)) {
                outView.setVisibility(View.VISIBLE);
            } else {
                outView.setVisibility(View.INVISIBLE);
            }
        }
        
        // Update the IN and OUT decibels
        oldInLevel = inCombination;
        oldOutLevel = outCombination;
    }
    
    public void onActionButtonClicked(View paramView) {
        if(!started) {
            startBioAid();
            paramView.setBackgroundResource(R.drawable.onbutton);
            paramView.setContentDescription("On");
        } else {
            stopBioAid();
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

    /**
     * Resume processing when the headphones are put in
     */
    public void resume() {
        isGoing = true;
    }

    /**
     * Pause processing while the headphones are out
     */
    public void suspend() {
        isGoing = false;
    }

    private void stopBioAid() {
        isGoing = false;
        isQuitting = true;
        try {
            stopProcessing();
            streamer.join();
        } catch (InterruptedException localInterruptedException) {
                localInterruptedException.printStackTrace();
        }
        isQuitting = false;
    }
    
    // Native library calls
    public native String startProcessing();
    public native String stopProcessing();
}