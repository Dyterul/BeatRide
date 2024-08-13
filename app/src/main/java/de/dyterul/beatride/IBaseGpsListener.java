package de.dyterul.beatride;

import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

public interface IBaseGpsListener extends LocationListener, GpsStatus.Listener {

    @Override
    void onLocationChanged(Location location);

    @Override
    void onProviderDisabled(String provider);

    @Override
    void onProviderEnabled(String provider);

    @Override
    void onStatusChanged(String provider, int status, Bundle extras);

    @Override
    void onGpsStatusChanged(int event);

}
