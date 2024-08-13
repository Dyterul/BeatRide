package de.dyterul.beatride;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.core.app.ActivityCompat;

import java.util.Formatter;
import java.util.Locale;

public class LocationServices implements IBaseGpsListener {

    MainActivity _mainActivity;

    public LocationServices(MainActivity mainActivity) {
        _mainActivity = mainActivity;
        LocationManager locationManager = (LocationManager) mainActivity.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(mainActivity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mainActivity, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }

    private void updateSpeed(CLocation location) {
        float nCurrentSpeed = 0;

        if (location != null) {
            location.setUseMetricunits(false);
            nCurrentSpeed = location.getSpeed();
            nCurrentSpeed = nCurrentSpeed * 3.6f;
            nCurrentSpeed = nCurrentSpeed / 2.2f;
        }

        Formatter fmt = new Formatter(new StringBuilder());
        fmt.format(Locale.US, "%5.1f", nCurrentSpeed);
        String strCurrentSpeed = fmt.toString();
        strCurrentSpeed = strCurrentSpeed.replace(' ', '0');

        TextView txtCurrentSpeed = _mainActivity.findViewById(R.id.txtCurrentSpeed);
        txtCurrentSpeed.setText(strCurrentSpeed + " km/h");


        // update the volume
        Button PlayPauseButton = _mainActivity.findViewById(R.id.btnPlayPause);
        CheckBox checkBox = _mainActivity.findViewById(R.id.useGPS);
        if (PlayPauseButton.getText().equals("Pause") && checkBox.isChecked()) {
            _mainActivity.lastSpeed = (int) nCurrentSpeed;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            CLocation myLocation = new CLocation(location, false);
            updateSpeed(myLocation);
        }
    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onGpsStatusChanged(int event) {

    }
}
