package com.example.holcz.imasong.songrecognition;

public interface SongRecognizer {
    void initialize();
    void start();
    void stop();
    void restart();
    void cancel();
    void setSongRecognizerListener(SongRecognizerListener l);
}
