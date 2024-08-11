package de.dyterul.beatride;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.core.app.ActivityCompat;
import com.google.android.material.slider.RangeSlider;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

import static android.os.Build.VERSION.SDK_INT;

public class MainActivity extends Activity implements IBaseGpsListener {

    List<String> availableTracks = new ArrayList<>();
    int nowPlaying;
    int lastSpeed = 0;
    private MediaPlayer mediaPlayerBass = new MediaPlayer();
    private MediaPlayer mediaPlayerOther = new MediaPlayer();
    private MediaPlayer mediaPlayerDrums = new MediaPlayer();
    private MediaPlayer mediaPlayerPiano = new MediaPlayer();
    private MediaPlayer mediaPlayerVocals = new MediaPlayer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        this.updateSpeed(null);

        RangeSlider rangeSlider = findViewById(R.id.rangeSlider);
        rangeSlider.addOnChangeListener((slider, value, fromUser) -> {
            lastSpeed = (int) value;
            MainActivity.this.updateVolume((int) value);
        });

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
        NextButton.setOnClickListener(v -> {
            OnTrackEnd();
        });

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

        mediaPlayerBass.setVolume(100, 100);

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

    private void updateSpeed(CLocation location) {
        // TODO Auto-generated method stub
        float nCurrentSpeed = 0;

        if (location != null) {
            location.setUseMetricunits(false);
            nCurrentSpeed = location.getSpeed();
            nCurrentSpeed = nCurrentSpeed * 3.6f;
        }

        Formatter fmt = new Formatter(new StringBuilder());
        fmt.format(Locale.US, "%5.1f", nCurrentSpeed);
        String strCurrentSpeed = fmt.toString();
        strCurrentSpeed = strCurrentSpeed.replace(' ', '0');

        String strUnits = "km/h";

        TextView txtCurrentSpeed = this.findViewById(R.id.txtCurrentSpeed);
        txtCurrentSpeed.setText(strCurrentSpeed + " " + strUnits);


        // update the volume
        Button PlayPauseButton = findViewById(R.id.btnPlayPause);
        CheckBox checkBox = findViewById(R.id.useGPS);
        if (PlayPauseButton.getText().equals("Pause") && checkBox.isChecked()) {
            lastSpeed = (int) nCurrentSpeed;
            this.updateVolume((int) nCurrentSpeed);
        }
    }

    private void updateVolume(int currentSpeed) {
        int maxVolume = 100;

        float volumeDrums = getVolume(currentSpeed, 15, 25);
        float volumePiano = getVolume(currentSpeed, 30, 35);
        float volumeVocals = getVolume(currentSpeed, 35, 50);
        float volumeOther = getVolume(currentSpeed, 45, 55);

        // print the volume to the console
        System.out.println("Drums: " + volumeDrums);
        System.out.println("Piano: " + volumePiano);
        System.out.println("Vocals: " + volumeVocals);
        System.out.println("Other: " + volumeOther);

        mediaPlayerDrums.setVolume(volumeDrums, volumeDrums);
        mediaPlayerPiano.setVolume(volumePiano, volumePiano);
        mediaPlayerVocals.setVolume(volumeVocals, volumeVocals);
        mediaPlayerOther.setVolume(volumeOther, volumeOther);
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

    @Override
    public void onLocationChanged(Location location) {
        // TODO Auto-generated method stub
        if (location != null) {
            CLocation myLocation = new CLocation(location, false);
            this.updateSpeed(myLocation);
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onGpsStatusChanged(int event) {
        // TODO Auto-generated method stub

    }


}