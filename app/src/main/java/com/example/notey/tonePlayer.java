package com.example.notey;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.util.HashMap;
import java.util.Map;

public class tonePlayer extends AppCompatActivity {
    // Number of tunes we want to be able to play
    // Chose 7 for {A, B, C, D, E, F, G}
    private final int tunes = 7;

    private double[] base_frequencies = new double[7];

    private Map<Integer, Integer> id2numInsounds = new HashMap<>();

    private final int duration = 1; // seconds
    private final int sampleRate = 11025;
    private final int numOfSamples = duration * sampleRate;

    private final byte[][] sounds = new byte[tunes][];

    AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
            sampleRate, AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT, 2 * numOfSamples,
            AudioTrack.MODE_STATIC);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tone_player);

        setBase_frequencies();
        setId2numInsounds();

        for (int i = 0; i < tunes; i++) {
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

    private void setId2numInsounds() {
        id2numInsounds.put(R.id.playToneE, 4);
        id2numInsounds.put(R.id.playToneA, 0);
        id2numInsounds.put(R.id.playToneD, 3);
        id2numInsounds.put(R.id.playToneG, 6);
        id2numInsounds.put(R.id.playToneB, 1);
    }

    private byte[] genTone(double freqOfTone) {
        Double[] sample = new Double[numOfSamples];
        byte[] sound = new byte[2 * numOfSamples];

        // fill out the array
        for (int i = 0; i < numOfSamples; ++i) {
            sample[i] = Math.sin(2 * Math.PI * i / (sampleRate / freqOfTone));
        }

        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalised.
        int idx = 0;
        for (final double dVal : sample) {
            // scale to maximum amplitude
            final short val = (short) ((dVal * 32767));
            // in 16 bit wav PCM, first byte is the low order byte
            sound[idx++] = (byte) (val & 0x00ff);
            sound[idx++] = (byte) ((val & 0xff00) >>> 8);
        }

        return sound;
    }

    public void playSound(View view) {
        if(audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
            audioTrack.stop();
            audioTrack.flush();
        }

        byte[] sound = sounds[id2numInsounds.get(view.getId())];
        audioTrack.write(sound, 0, sound.length);
        audioTrack.play();
    }
}
