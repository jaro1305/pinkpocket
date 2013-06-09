package com.bioaid.app;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import com.bioaid.BioAidFilterService;
import com.bioaid.app.R;
import android.app.Activity;
import android.content.IntentFilter;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;

public class MainActivity extends Activity {
    // Constants
    private static final int AUDIO_CHANNEL_CONFIG = 2;
    private static final int AUDIO_SOURCE = 1;
    private static final int AUDIO_STREAM_TYPE = AudioManager.STREAM_MUSIC;
    private static final int AUDIO_MODE = AudioTrack.MODE_STREAM;
    public static final int AUDIO_SAMPLING_RATE = 8000; //44100;
    private static final int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private static final int lastN = 10; // number of samples for getting the volume
    private static final int AUDIO_BUFFER_SIZE = AudioTrack.getMinBufferSize(AUDIO_SAMPLING_RATE, 
            AUDIO_CHANNEL_CONFIG, 
            AudioFormat.ENCODING_PCM_16BIT); // Effective time length of buffer
    private static final short BASE_AMPLITUDE = 15000;
    
    // Variables
    private AudioManager audioManager;
    private AudioTrack audioPlayer;
    private AudioRecord audioRecorder;
    private byte[] data;
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

    private void startPlaying() {
        audioPlayer = new AudioTrack(3, 
                AUDIO_SAMPLING_RATE, 
                AUDIO_CHANNEL_CONFIG, 
                AUDIO_ENCODING, 
                AUDIO_BUFFER_SIZE, 
                AUDIO_MODE);
        audioPlayer.play();
    }

    private void startRecording() {
        audioRecorder = new AudioRecord(1, 
                AUDIO_SAMPLING_RATE, 
                AUDIO_CHANNEL_CONFIG,
                AUDIO_ENCODING, 
                AUDIO_BUFFER_SIZE);
        audioRecorder.startRecording();
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
        data = new byte[AUDIO_BUFFER_SIZE];
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
        IntentFilter localIntentFilter = new IntentFilter("android.intent.action.HEADSET_PLUG");
        hsr = new HeadsetStateReceiver(this);
        registerReceiver(hsr, localIntentFilter);
        
        // Set up the volume slider
        SeekBar localSeekBar = (SeekBar) findViewById(R.id.seekBar1);
        audioManager = ((AudioManager) getSystemService("audio"));
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
                        21);
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
                long inAmplitude = bundle.getLong("in");
                long outAmplitude = bundle.getLong("out");
                updateEqualiser(inAmplitude, outAmplitude);
            }
        };
    }

    private class Streamer extends Thread {
        public void run() {
            BioAidFilterService bafs = new BioAidFilterService();
            while (!isQuitting) {
                if (isGoing) {
                    int sizeInBytes = audioRecorder.read(data, 
                            0,
                            MainActivity.AUDIO_BUFFER_SIZE);
                    if (audioPlayer != null && (audioPlayer.getPlayState() == 3) && (sizeInBytes > 0)) {
                        // Normalise the input to the [-1,1] range
                        float[] input = new float[AUDIO_BUFFER_SIZE / 2]; // assume 2 bytes per sample
                        ByteBuffer bb = ByteBuffer.wrap(data);
                        
                        long inAmplitude = 0;
                        if(true) {
                            // Real input
                            for(int j = 0; j < (AUDIO_BUFFER_SIZE / 2); j++) {
                                input[j] = Float.valueOf(bb.getShort()); //((double)bb.getShort()) / ((double)Short.MAX_VALUE);
                                //input[j / 2] = Math.log10(Math.abs((double)bb.getShort()) / ((double)Short.MAX_VALUE));
                                if(j >= ((AUDIO_BUFFER_SIZE / 2) - lastN)) {
                                    inAmplitude += (long) Math.abs(input[j]);
                                }
                            }
                        } else {
                            // Sine wave
                            int sampleRate = AUDIO_SAMPLING_RATE;
                            int freqOfTone = 500;
                            double angle = 0;
                            double increment = (2 * Math.PI * freqOfTone / sampleRate); // angular increment 
    
                            for (int j = 0; j < (AUDIO_BUFFER_SIZE) / 2; j++) {
                                input[j] = (short) (Math.sin(angle) * Short.MAX_VALUE);
                                angle += increment;
                                if(j >= ((AUDIO_BUFFER_SIZE / 2) - lastN)) {
                                    inAmplitude += (long) Math.abs(input[j]);
                                }
                            }
                        }
                        inAmplitude /= lastN;
                        
                        // Process the input
                        //long time = System.nanoTime();
                        float[] output = bafs.processBlock(input);
                        //Log.e("MainActivity", "time:" + (System.nanoTime() - time));
                        
                        // Scale and then write the output
                        byte[] outputBytes = new byte[AUDIO_BUFFER_SIZE];
                        long outAmpltitude = 0;
                        ByteBuffer bb2 = ByteBuffer.wrap(outputBytes);
                        for(int j = 0; j < (AUDIO_BUFFER_SIZE / 2); j ++) {
                            float value = output[j];
                            //short value = (short)(output[j] * Short.MAX_VALUE);
                            bb2.putShort((short)value);
                            if(j >= ((AUDIO_BUFFER_SIZE / 2) - lastN)) {
                                outAmpltitude += Math.abs((short)value);
                            }
                        }
                        outAmpltitude /= lastN;
                        audioPlayer.write(outputBytes, 0, sizeInBytes);
                        
                        // Update the equalisers
                        Message msg = new Message();
                        Bundle bundle = new Bundle();
                        bundle.putLong("in", inAmplitude);
                        bundle.putLong("out", outAmpltitude);
                        msg.setData(bundle);
                        equaliserHandler.sendMessage(msg);
                    }
                }
            }
        }
    }
    
    public void updateEqualiser(long inAmplitude, long outAmplitude) {
        int maxLevels = 13;
        
        // Compute effective decibels from amplitudes
        double inLevel = Math.max(1, (double)(inAmplitude - BASE_AMPLITUDE)); // 0 as first param would give -inf result in log
        inLevel = Math.log10(inLevel);
        inLevel /= Math.log10(Short.MAX_VALUE - BASE_AMPLITUDE);
        double outLevel = Math.max(1, (double)(outAmplitude - BASE_AMPLITUDE));
        outLevel = Math.log10(outLevel);
        outLevel /= Math.log10(Short.MAX_VALUE - BASE_AMPLITUDE);
        
        // Get the updated IN decibels and OUT decibels, using smoothing
        double inCombination = (0.125 * inLevel) + (0.875 * oldInLevel); // smoothing
        double outCombination = (0.125 * outLevel) + (0.875 * oldOutLevel); // smoothing
        
        // Set the equaliser
        for(int k = 0; k < maxLevels; k++) {
            // Get the equaliser views
            ImageView inView = (ImageView)findViewById(inLevels.get(k));
            ImageView outView = (ImageView)findViewById(outLevels.get(k));
            
            // Get the current percentage
            double percentage = ((double)k) / ((double)(maxLevels - 1));
            
            // Set the views as visible or not, depending on whether the
            // percenatage was exceeded
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
}