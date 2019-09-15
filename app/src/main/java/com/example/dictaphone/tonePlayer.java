package com.example.dictaphone;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;

import java.util.HashMap;
import java.util.Map;

public class tonePlayer extends AppCompatActivity {
    // originally from http://marblemice.blogspot.com/2010/04/generate-and-play-tone-in-android.html
    // and modified by Steve Pomeroy <steve@staticfree.info>

    // Number of tunes we want to be able to play
    // Chose 8 for {A, B, C, D, E, F, G}
    private final int tunes = 7;

    private double[] base_frequencies = new double[7];

    private Map<Integer, Integer> id2numInsounds = new HashMap<Integer, Integer>();
    private Button[] buttons = new Button[6];

    private final int duration = 1; // seconds
    private final int sampleRate = 11025;
    private final int numSamples = duration * sampleRate;

    private final byte generatedSnd[] = new byte[2 * numSamples];

    private final AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
            sampleRate, AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT, generatedSnd.length,
            AudioTrack.MODE_STATIC);


    private final byte[][] sounds = new byte[tunes][];

    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tone_player);

        setButtons();
        setBase_frequencies();
        setId2numInsounds();

        for(int i = 0; i < tunes; i++) {
            sounds[i] = genTone(base_frequencies[i]);
        }
    }

    private void setBase_frequencies() {
        base_frequencies[0] = 440;      // A4
        base_frequencies[1] = 493.88;   // B4
        base_frequencies[2] = 261.63;   // C4
        base_frequencies[3] = 293.66;   // D4
        base_frequencies[4] = 329.63;   // E4
        base_frequencies[5] = 349.23;   // F4
        base_frequencies[6] = 392;      // G4
    }

    private void setButtons() {
        buttons[0] = findViewById(R.id.playToneC);
        buttons[0] = findViewById(R.id.playToneD);
        buttons[0] = findViewById(R.id.playToneE);
        buttons[0] = findViewById(R.id.playToneF);
        buttons[0] = findViewById(R.id.playToneG);
        buttons[0] = findViewById(R.id.playToneA);
    }

    private void setId2numInsounds() {
        id2numInsounds.put(R.id.playToneC, 0);
        id2numInsounds.put(R.id.playToneD, 1);
        id2numInsounds.put(R.id.playToneE, 2);
        id2numInsounds.put(R.id.playToneF, 3);
        id2numInsounds.put(R.id.playToneG, 4);
        id2numInsounds.put(R.id.playToneA, 5);
    }


//    @Override
//    protected void onResume() {
//        super.onResume();
//
//        // Use a new thread as this can take a while
//        final Thread thread = new Thread(new Runnable() {
//            public void run() {
//                genTone();
//                handler.post(new Runnable() {
//                    public void run() {
//                        playSound();
//                    }
//                });
//            }
//        });
//        thread.start();
//    }

    private byte[] genTone(double freqOfTone) {
        byte[] sample = new byte[numSamples];

        // fill out the array
        for (int i = 0; i < numSamples; ++i) {
            sample[i] = (byte)Math.sin(2 * Math.PI * i / (sampleRate / freqOfTone));    // TODO: byte wasn't here earlier
        }

        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalised.
        int idx = 0;
        for (final double dVal : sample) {
            // scale to maximum amplitude
            final short val = (short) ((dVal * 32767));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
        }

        return sample;
    }

    public void playSound(View view) {
        for(Button b : buttons) {
            b.setEnabled(false);
        }

        byte[] sound = sounds[id2numInsounds.get(view.getId())];
        audioTrack.write(sound, 0, sound.length);
        audioTrack.play();

        for(Button b : buttons) {
            b.setEnabled(true);
        }
    }
}
