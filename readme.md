# BeatRide

A GPS speed-based playback system for split audio tracks.

## What is BeatRide?

**BeatRide** is a work-in-progress Android application designed to enhance your music listening experience while
driving. It
dynamically adjusts the volume of different stems of a track based on your current speed, providing a unique and
immersive audio experience.

## How to use

1. **Install the App**: Compile and install the BeatRide app on your Android device.

2. **Grant Permissions**: Ensure the app has the necessary permissions to access your location and manage files on your
   device.

3. **Prepare Your Music**: Create a directory on your device at `/storage/emulated/0/BeatRide/`. For each track, create
   a separate folder and place the four stems of the track in it. The required stems are:
    - `bass.mp3`
    - `drums.mp3`
    - `other.mp3`
    - `vocals.mp3`

   You can easily generate these stems using [Spleeter](https://github.com/deezer/spleeter).

4. **Start the App**: Launch the BeatRide app. It will automatically begin tracking your location and adjusting the
   music playback according to your speed.

**Enjoy your ride with music that dynamically adapts to your driving speed :)**