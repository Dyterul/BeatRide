package de.dyterul.beatride;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import com.google.android.material.slider.RangeSlider;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.os.Build.VERSION.SDK_INT;

public class MainActivity extends Activity {

    List<String> availableTracks = new ArrayList<>();
    int nowPlaying;
    int lastSpeed = 0;
    private long lastSpeedUpdate = System.currentTimeMillis();
    private MediaPlayer mediaPlayerBass = new MediaPlayer();
    private MediaPlayer mediaPlayerOther = new MediaPlayer();
    private MediaPlayer mediaPlayerDrums = new MediaPlayer();
    private MediaPlayer mediaPlayerPiano = new MediaPlayer();
    private MediaPlayer mediaPlayerVocals = new MediaPlayer();

    private float musicSpeedNow = 1;

    private float volumeBassNow = 0;
    private float volumeDrumsNow = 0;
    private float volumePianoNow = 0;
    private float volumeVocalsNow = 0;
    private float volumeOtherNow = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new LocationServices(this);

        RangeSlider rangeSlider = findViewById(R.id.rangeSlider);
        rangeSlider.addOnChangeListener((slider, value, fromUser) -> lastSpeed = (int) value);

        Button PlayPauseButton = findViewById(R.id.btnPlayPause);
        PlayPauseButton.setOnClickListener(v -> {
            if (PlayPauseButton.getText().equals("Pause")) {
                PlayPauseButton.setText("Play");

                mediaPlayerBass.pause();
                mediaPlayerDrums.pause();
                mediaPlayerPiano.pause();
                mediaPlayerVocals.pause();
                mediaPlayerOther.pause();
            } else {
                PlayPauseButton.setText("Pause");

                mediaPlayerBass.start();
                mediaPlayerDrums.start();
                mediaPlayerPiano.start();
                mediaPlayerVocals.start();
                mediaPlayerOther.start();
            }
        });

        Button NextButton = findViewById(R.id.btnNext);
        NextButton.setOnClickListener(v -> OnTrackEnd());

        if (SDK_INT >= Build.VERSION_CODES.R) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
        }

        availableTracks = GetAvailableTracks();

        nowPlaying = -1;
        OnTrackEnd();
        mediaPlayerBass.pause();
        mediaPlayerDrums.pause();
        mediaPlayerPiano.pause();
        mediaPlayerVocals.pause();
        mediaPlayerOther.pause();


        // new Task for updating the player settings every 10ms
        new Thread(() -> {
//            float speedLastUpdate = 0;
            while (true) {
                try {
                    Thread.sleep(500);
//                    float finalSpeedLastUpdate = speedLastUpdate;
                    runOnUiThread(() -> {
                        updateVolume(lastSpeed);
//                        updateMusicSpeed(lastSpeed, finalSpeedLastUpdate);
                    });

//                    speedLastUpdate = lastSpeed;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private String GetNextTrack() {
        nowPlaying++;
        if (nowPlaying >= availableTracks.size()) {
            nowPlaying = 0;
        }
        return availableTracks.get(nowPlaying);
    }

    private void OnTrackEnd() {
        String trackName = GetNextTrack();
        PlayTrack(trackName);
    }

    private List<String> GetAvailableTracks() {
        File directory = new File("/storage/emulated/0/BeatDrive/");
        File[] files = directory.listFiles();
        List<String> trackNames = new ArrayList<>();
        for (File file : files) {
            if (file.isDirectory()) {
                trackNames.add(file.getName());
            }
        }
        return trackNames;
    }

    private void PlayTrack(String trackName) {
        TextView txtTrackName = findViewById(R.id.txtTrackName);
        txtTrackName.setText(trackName);


        if (mediaPlayerBass != null) {
            mediaPlayerBass.release();
            mediaPlayerOther.release();
            mediaPlayerDrums.release();
            mediaPlayerPiano.release();
            mediaPlayerVocals.release();
        }


        mediaPlayerBass = LoadMedia(trackName, Instruments.BASS);
        mediaPlayerOther = LoadMedia(trackName, Instruments.OTHER);
        mediaPlayerDrums = LoadMedia(trackName, Instruments.DRUMS);
        mediaPlayerPiano = LoadMedia(trackName, Instruments.PIANO);
        mediaPlayerVocals = LoadMedia(trackName, Instruments.VOCALS);

        updateVolume(lastSpeed);

        mediaPlayerBass.start();
        mediaPlayerOther.start();
        mediaPlayerDrums.start();
        mediaPlayerPiano.start();
        mediaPlayerVocals.start();

        // play the next track after track ends
        mediaPlayerBass.setOnCompletionListener(mp -> OnTrackEnd());
    }

    private MediaPlayer LoadMedia(String trackName, Instruments instruments) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );

        String trackPath = "/storage/emulated/0/BeatDrive/" + trackName + "/";
        switch (instruments) {
            case BASS:
                trackPath += "bass.mp3";
                break;
            case DRUMS:
                trackPath += "drums.mp3";
                break;
            case PIANO:
                trackPath += "piano.mp3";
                break;
            case VOCALS:
                trackPath += "vocals.mp3";
                break;
            case OTHER:
                trackPath += "other.mp3";
                break;
        }

        File file = new File(trackPath);
        try {
            if (file.exists()) {
                mediaPlayer.setDataSource(file.getAbsolutePath());
                mediaPlayer.prepare();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return mediaPlayer;
    }

    public void finish() {
        super.finish();
        System.exit(0);
    }

    private void updateVolume(int currentSpeed) {
        float volumeChangeSpeed = 0.5f;

        float volumeBassTarget = getVolume(currentSpeed, 0, 30, 80);
        float volumeDrumsTarget = getVolume(currentSpeed, 0, 35, 40);
        float volumePianoTarget = getVolume(currentSpeed, 15, 45);
        float volumeVocalTargets = getVolume(currentSpeed, 25, 45);
        float volumeOtherTarget = getVolume(currentSpeed, 20, 50, 10);

        volumeBassNow = getSmoothTransition(volumeBassNow, volumeBassTarget, volumeChangeSpeed);
        volumeDrumsNow = getSmoothTransition(volumeDrumsNow, volumeDrumsTarget, volumeChangeSpeed);
        volumePianoNow = getSmoothTransition(volumePianoNow, volumePianoTarget, volumeChangeSpeed);
        volumeVocalsNow = getSmoothTransition(volumeVocalsNow, volumeVocalTargets, volumeChangeSpeed);
        volumeOtherNow = getSmoothTransition(volumeOtherNow, volumeOtherTarget, volumeChangeSpeed);

        System.out.println("BassTarget: " + volumeBassNow + " BassNow: " + volumeBassNow);
        System.out.println("DrumsTarget: " + volumeDrumsTarget + " DrumsNow: " + volumeDrumsNow);
        System.out.println("PianoTarget: " + volumePianoTarget + " PianoNow: " + volumePianoNow);
        System.out.println("VocalsTarget: " + volumeVocalTargets + " VocalsNow: " + volumeVocalsNow);
        System.out.println("OtherTarget: " + volumeOtherTarget + " OtherNow: " + volumeOtherNow);

        mediaPlayerBass.setVolume(volumeBassNow, volumeBassNow);
        mediaPlayerDrums.setVolume(volumeDrumsNow, volumeDrumsNow);
        mediaPlayerPiano.setVolume(volumePianoNow, volumePianoNow);
        mediaPlayerVocals.setVolume(volumeVocalsNow, volumeVocalsNow);
        mediaPlayerOther.setVolume(volumeOtherNow, volumeOtherNow);
    }

    private void updateMusicSpeed(float speedNow, float speedBefore) {
        long now = System.currentTimeMillis();
        long timeSinceLastUpdate = now - lastSpeedUpdate;
        lastSpeedUpdate = now;

        float speedDifference = speedNow - speedBefore;

        float musicSpeedMax = 1.3f;
        float musicSpeedMin = 0.7f;

        float speedChange = speedDifference / timeSinceLastUpdate;

        float musicSpeedTarget = 1 + speedChange * 2;

        musicSpeedTarget = Math.min(musicSpeedTarget, musicSpeedMax);
        musicSpeedTarget = Math.max(musicSpeedTarget, musicSpeedMin);

        musicSpeedNow = getSmoothTransition(musicSpeedNow, musicSpeedTarget, 1f);

        System.out.println("SpeedChange: " + speedChange + " SpeedDifference: " + speedDifference);
        System.out.println("MusicSpeed: " + musicSpeedNow);


        mediaPlayerBass.setPlaybackParams(mediaPlayerBass.getPlaybackParams().setSpeed(musicSpeedNow));
        mediaPlayerDrums.setPlaybackParams(mediaPlayerDrums.getPlaybackParams().setSpeed(musicSpeedNow));
//        mediaPlayerPiano.setPlaybackParams(mediaPlayerPiano.getPlaybackParams().setSpeed(musicSpeedNow));
        mediaPlayerVocals.setPlaybackParams(mediaPlayerVocals.getPlaybackParams().setSpeed(musicSpeedNow));
        mediaPlayerOther.setPlaybackParams(mediaPlayerOther.getPlaybackParams().setSpeed(musicSpeedNow));
    }

    private float getVolume(int currentSpeed, int startSpeed, int maxSpeed, int... minVolume) {
        int maxVolume = 101;

        int volume = (currentSpeed - startSpeed) * (maxVolume / (maxSpeed - startSpeed));

        if (currentSpeed < startSpeed) {
            volume = 0;
        }

        if (currentSpeed > maxSpeed) {
            volume = 100;
        }

        if (minVolume.length > 0) {
            volume = Math.max(volume, minVolume[0]);
        }

        volume = Math.min(volume, 100);

        return (float) (1 - (Math.log(maxVolume - volume) / Math.log(maxVolume)));
    }

    private float getSmoothTransition(float currentValue, float targetValue, float speed) {
        return currentValue + (targetValue - currentValue) * speed;
    }
}