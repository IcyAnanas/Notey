package com.example.notey;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.TextView;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

public class MainActivity extends AppCompatActivity {
    public static AudioDispatcher dispatcher = null;

    private Button switchToToneGenerator = null;

    private static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 17;

    private static final int[] notes = {R.string.A_note, R.string.A_sharp_note, R.string.B_note, R.string.C_note, R.string.C_sharp_note,
            R.string.D_note, R.string.D_sharp_note, R.string.E_note, R.string.F_note, R.string.F_sharp_note, R.string.G_note,
            R.string.G_sharp_note};

    private static double[] generateFreqArray() {
        double[] freqs = new double[96];

        freqs[0] = 27.50;   // A0
        freqs[1] = 29.14;   // A#0
        freqs[2] = 30.87;   // B0
        freqs[3] = 32.70;   // C1
        freqs[4] = 34.65;   // C#1
        freqs[5] = 36.71;   // D1
        freqs[6] = 38.89;   // D#1
        freqs[7] = 41.20;   // E1
        freqs[8] = 43.65;   // F1
        freqs[9] = 46.25;   // F#1
        freqs[10] = 49.00;  // G1
        freqs[11] = 51.91;  // G#1

        // Fill the array
        for (int i = 12; i < 96; i++) {
            freqs[i] = freqs[i - 12] * 2;
        }

        return freqs;
    }

    static final double[] note_frequencies = generateFreqArray();

    // Base frequency, A4 frequency (440Hz)
    private static final double base_freq = 22.5;

    TextView note_text = null;

    PitchDetectionHandler pdh = new PitchDetectionHandler() {
        @Override
        public void handlePitch(PitchDetectionResult res, AudioEvent e) {
            final float pitch_in_Hz = res.getPitch();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    processPitch(pitch_in_Hz);
                }
            });
        }
    };

    // 22050 - sampling frequency (in Hz), 1024 - buffer size
    AudioProcessor pitchProcessor = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, pdh);

    private final View.OnClickListener handler = new View.OnClickListener() {
        public void onClick(View view) {
            if (view == switchToToneGenerator) {
                Intent intent = new Intent(MainActivity.this,
                        tonePlayer.class);
                startActivity(intent);
            }
        }
    };

    boolean checkAudioRecordPermission() {
        // Check for permissions
        int permission = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO);

        // If we don't have permissions, ask user for permissions
        if (permission != PackageManager.PERMISSION_GRANTED) {
            String[] PERMISSIONS_STORAGE = {
                    Manifest.permission.RECORD_AUDIO,
            };

            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    PERMISSIONS_STORAGE,
                    MY_PERMISSIONS_REQUEST_RECORD_AUDIO
            );
            return false;
        }
        return true;
    }

    private void initDispatcher() {
        // 22050 - sampling frequency (in Hz), 1024 - buffer size, 0 - overLap (see TarsosDP Documentation for details)
        if (dispatcher == null) {
            dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050, 1024, 0);

            dispatcher.addAudioProcessor(pitchProcessor);

            Thread audioThread = new Thread(dispatcher, "Audio Thread");
            audioThread.start();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_RECORD_AUDIO: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initDispatcher();
                    break;
                } else {
                    Toast.makeText(MainActivity.this.getBaseContext(), "You must allow audio recording audio to use this app.", Toast.LENGTH_SHORT).show();
                    MainActivity.this.finish();
                    finish();
                }
            }
            default:
                break;
        }
    }

    private int noteFromFrequencyArray(double freq) {
        if (freq <= 0) {
            return R.string.too_much_noise;
        }

        if (freq > note_frequencies[note_frequencies.length - 1]) {
            return R.string.too_high_frequency;
        } else if (freq < note_frequencies[0]) {
            return R.string.too_low_frequency;
        }

        int left = 0;
        int right = note_frequencies.length - 1;

        while (left <= right) {
            int mid = (right + left) / 2;

            if (freq < note_frequencies[mid]) {
                right = mid - 1;
            } else if (freq > note_frequencies[mid]) {
                left = mid + 1;
            } else {
                return notes[mid % 12];
            }
        }
        // lo == hi + 1
        return (note_frequencies[left] - freq) < (freq - note_frequencies[right]) ? notes[left % 12] : notes[right % 12];
    }

    public void processPitch(final float pitch_in_Hz) {
        TextView note_text = findViewById(R.id.note_text);
        note_text.setText(noteFromFrequencyArray(pitch_in_Hz));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        note_text = findViewById(R.id.note_text);
        note_text.setText(R.string.too_much_noise);

        switchToToneGenerator = findViewById(R.id.switchToToneGenerator);
        switchToToneGenerator.setOnClickListener(handler);

        if (checkAudioRecordPermission()) {
            initDispatcher();
        }
    }
}
