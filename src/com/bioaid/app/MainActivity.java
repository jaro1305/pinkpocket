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
import android.view.View;
import android.widget.SeekBar;

public class MainActivity extends Activity {
    private static final int AUDIO_CHANNEL_CONFIG = 2;
    private static final int AUDIO_MODE = AudioTrack.MODE_STREAM;
    private static final int AUDIO_SAMPLING_RATE = 8000; //44100; //8000;
    private static final int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    // Effective time length of buffer
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
    private HeadsetStateReceiver hsr;

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
        data = new byte[AUDIO_BUFFER_SIZE];
        isGoing = false;
        isQuitting = false;
        
        // Set up the headset monitor
        IntentFilter localIntentFilter = new IntentFilter("android.intent.action.HEADSET_PLUG");
        hsr = new HeadsetStateReceiver(this);
        registerReceiver(hsr, localIntentFilter);
        
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
                        
                        if(true) {
                            // Real input
                            for(int j = 0; j < AUDIO_BUFFER_SIZE; j += 2) {
                                input[j / 2] = bb.getShort(); //((double)bb.getShort()) / ((double)Short.MAX_VALUE);
                                //input[j / 2] = Math.log10(Math.abs((double)bb.getShort()) / ((double)Short.MAX_VALUE));
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
                            }
                        }
                        
                        // Process the input
                        //long time = System.nanoTime();
                        float[] output = bafs.processBlock(input);
                        //Log.e("MainActivity", "time:" + (System.nanoTime() - time));
                        
                        // Scale and then write the output
                        byte[] outputBytes = new byte[AUDIO_BUFFER_SIZE];
                        ByteBuffer bb2 = ByteBuffer.wrap(outputBytes);
                        for(int j = 0; j < (AUDIO_BUFFER_SIZE / 2); j ++) {
                            //short value = (short)output[j];
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
    
    private static final int AUDIO_SOURCE = 1;
    private static final int AUDIO_STREAM_TYPE = 3;
}