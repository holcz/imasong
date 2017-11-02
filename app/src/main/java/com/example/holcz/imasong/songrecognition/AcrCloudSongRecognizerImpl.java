package com.example.holcz.imasong.songrecognition;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.acrcloud.rec.sdk.ACRCloudClient;
import com.acrcloud.rec.sdk.ACRCloudConfig;
import com.acrcloud.rec.sdk.ACRCloudResult;
import com.acrcloud.rec.sdk.IACRCloudResultWithAudioListener;
import com.example.holcz.imasong.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class AcrCloudSongRecognizerImpl implements SongRecognizer, IACRCloudResultWithAudioListener {

    private static final String TAG = "AcrCloudSongRecognizer";

    protected String MODEL_DIRECTORY_STORAGE = "/acrcloud/model";

    protected Context context;

    protected ACRCloudClient client;
    protected ACRCloudConfig config;

    protected boolean processing = false;
    protected boolean initState = false;

    protected SongRecognizerListener songRecognizerListener = null;

    public AcrCloudSongRecognizerImpl(Context context) {
        this.context = context;
        this.config = initConfig();
        this.client = initClient();
        this.initState = this.client.initWithConfig(this.config);
    }

    protected ACRCloudConfig initConfig() {
        ACRCloudConfig config = new ACRCloudConfig();
        config.acrcloudResultWithAudioListener = this;
        config.context = this.context;
        config.dbPath = initOfflineDbPath(); // offline db path, you can change it with other path which this app can access.
        config.host = this.context.getString(R.string.acrcloud_host);
        config.accessKey = this.context.getString(R.string.acrcloud_access_key);
        config.accessSecret = this.context.getString(R.string.acrcloud_access_secret);
        config.protocol = ACRCloudConfig.ACRCloudNetworkProtocol.PROTOCOL_HTTP; // PROTOCOL_HTTPS
        config.reqMode = ACRCloudConfig.ACRCloudRecMode.REC_MODE_REMOTE;
        //this.config.reqMode = ACRCloudConfig.ACRCloudRecMode.REC_MODE_LOCAL;
        //this.config.reqMode = ACRCloudConfig.ACRCloudRecMode.REC_MODE_BOTH;
        return config;
    }

    protected ACRCloudClient initClient() {
        ACRCloudClient client = new ACRCloudClient();
        return client;
    }

    protected String initOfflineDbPath() {
        String path = Environment.getExternalStorageDirectory().toString() + MODEL_DIRECTORY_STORAGE;

        File file = new File(path);
        if(!file.exists()){
            file.mkdirs();
        }
        return path;
    }

    @Override
    public void initialize() {
        if (this.initState) {
            this.client.startPreRecord(3000); //startRecognize prerecord, you can call "this.client.stopPreRecord()" to stop prerecord.
        }
    }

    @Override
    public void start() {
        Log.v(TAG, "startRecognize");
        if (!this.initState) {
            Log.v(TAG, "init error");
            return;
        }

        if (!processing) {
            processing = true;
            Log.v(TAG, "startRecognize");
            if (this.client == null || !this.client.startRecognize()) {
                processing = false;
                fireOnErrorEvent(context.getString(R.string.error_start));
            }
        }
    }

    @Override
    public void stop() {
        Log.v(TAG, "stop");
        if (processing && this.client != null) {
            this.client.stopRecordToRecognize();
        }
        processing = false;
    }

    @Override
    public void restart() {
        this.client.startPreRecord(3000);
        start();
    }

    @Override
    public void cancel() {
        if (processing && this.client != null) {
            processing = false;
            this.client.cancel();
        }
    }

    @Override
    public void setSongRecognizerListener(SongRecognizerListener l) {
        this.songRecognizerListener = l;
    }

    @Override
    public void onResult(ACRCloudResult acrCloudResult) {
        Song songResult = null;
        if (this.client != null) {
            this.client.cancel();
            processing = false;
        }
        Log.v(TAG, "");
        String result = acrCloudResult.getResult();

        try {
            JSONObject j = new JSONObject(result);
            JSONObject j1 = j.getJSONObject("status");
            int j2 = j1.getInt("code");
            if (j2 == 0) {
                JSONObject metadata = j.getJSONObject("metadata");
                //
                if (metadata.has("humming")) {
                    JSONArray hummings = metadata.getJSONArray("humming");
                    for (int i = 0; i < hummings.length(); i++) {
                        JSONObject tt = (JSONObject) hummings.get(i);
                        String title = tt.getString("title");
                        JSONArray artistt = tt.getJSONArray("artists");
                        JSONObject art = (JSONObject) artistt.get(0);
                        String artist = art.getString("name");
                        songResult = new Song(artist, title);
                    }
                }
                if (metadata.has("music")) {
                    JSONArray musics = metadata.getJSONArray("music");
                    JSONObject tt = (JSONObject) musics.get(0);
                    String title = tt.getString("title");
                    JSONArray artistt = tt.getJSONArray("artists");
                    JSONObject art = (JSONObject) artistt.get(0);
                    String artist = art.getString("name");
                    songResult = new Song(artist, title);
//                    for (int i = 0; i < musics.length(); i++) {
//                        JSONObject tt = (JSONObject) musics.get(i);
//                        String title = tt.getString("title");
//                        JSONArray artistt = tt.getJSONArray("artists");
//                        JSONObject art = (JSONObject) artistt.get(0);
//                        String artist = art.getString("name");
//                        songResult = new Song(artist, title);
//                    }
                }
                if (metadata.has("streams")) {
                    JSONArray musics = metadata.getJSONArray("streams");
                    for (int i = 0; i < musics.length(); i++) {
                        JSONObject tt = (JSONObject) musics.get(i);
                        String title = tt.getString("title");
                        String channelId = tt.getString("channel_id");
                    }
                }
                if (metadata.has("custom_files")) {
                    JSONArray musics = metadata.getJSONArray("custom_files");
                    for (int i = 0; i < musics.length(); i++) {
                        JSONObject tt = (JSONObject) musics.get(i);
                        String title = tt.getString("title");
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        if (songResult != null) {
            fireOnResultEvent(songResult);
        } else {
            fireOnErrorEvent(this.context.getString(R.string.error_recognize));
        }
    }

    @Override
    public void onVolumeChanged(double v) {}

    private void fireOnResultEvent(Song song) {
        if (this.songRecognizerListener != null) {
            this.songRecognizerListener.onResult(song);
        }
    }

    private void fireOnErrorEvent(String msg) {
        if (this.songRecognizerListener != null) {
            this.songRecognizerListener.onError(msg);
        }
    }
}
