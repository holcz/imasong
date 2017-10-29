package com.example.holcz.imasong.songrecognition;

public interface SongRecognizerListener {
    void onResult(Song song);
    void onError(String msg);
}
