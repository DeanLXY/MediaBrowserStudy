package com.wangx.mediabrowserstudy.data;

import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.util.ArrayMap;

import com.wangx.mediabrowserstudy.common.MusicListRequest;
import com.wangx.mediabrowserstudy.common.OkHttpUtils;
import com.wangx.mediabrowserstudy.pool.WorkerFactory;
import com.wangx.mediabrowserstudy.pool.WorkerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * @Author: xujie.wang
 * @Email: xujie.wang@17zuoye.com
 * @Date: 2017/5/24
 * @Project: MediaBrowserStudy
 */

public class MusicProvider {
    public static final String CATELOG_URL = "https://storage.googleapis.com/automotive-media/music.json";
    public static final String CATELOG_URL_PRE = "https://storage.googleapis.com/automotive-media";
    public static final String MEDIA_ID_ROOT = "__ROOT__";
    public static final String MEDIA_ID_EMPTY_ROOT = "__EMPTY__";
    private static final String JSON_MUSIC = "music";
    private static final String JSON_TITLE = "title";
    private static final String JSON_ALBUM = "album";
    private static final String JSON_ARTIST = "artist";
    private static final String JSON_GENRE = "genre";
    private static final String JSON_SOURCE = "source";
    private static final String JSON_IMAGE = "image";
    private static final String JSON_TRACK_NUMBER = "trackNumber";
    private static final String JSON_TOTAL_TRACK_COUNT = "totalTrackCount";
    private static final String JSON_DURATION = "duration";

    private State mCurrentState = State.NON_INITIALIZED;
    private Map<String, MediaMetadataCompat> musicList = new ArrayMap<String, MediaMetadataCompat>();

    public Iterable<MediaMetadataCompat> getMusicList() {
        return musicList.values();
    }

    public void retrieveMediaAsync(final CallBack callBack) {
        if (mCurrentState == State.INITIALIZED) {
            callBack.onMusicCatelogReady(true);
            return;
        }

        MusicDataSource musicDataSource = new MusicDataSource();
        musicDataSource.request(new WorkerFactory.CallBack<String>() {
            @Override
            public void onPostExecute(String s) {
                System.out.println(">>>>>" + s);
                transactMedia(s);
                callBack.onMusicCatelogReady(mCurrentState == State.INITIALIZED);

            }
        });
    }

    private void transactMedia(String json) {
        try {
            if (mCurrentState == State.NON_INITIALIZED) {
                mCurrentState = State.INITIALIZING;
                JSONObject jsonObject = null;
                jsonObject = new JSONObject(json);
                JSONArray jsonArray = jsonObject.optJSONArray(JSON_MUSIC);
                if (jsonArray != null) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        MediaMetadataCompat item = buildFromJSON(jsonArray.optJSONObject(i));
                        musicList.put(item.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID), item);
                    }
                }
                mCurrentState = State.INITIALIZED;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (mCurrentState != State.INITIALIZED) {
                mCurrentState = State.NON_INITIALIZED;
            }
        }
    }

    private MediaMetadataCompat buildFromJSON(JSONObject json) throws JSONException {

        String title = json.getString(JSON_TITLE);
        String album = json.getString(JSON_ALBUM);
        String artist = json.getString(JSON_ARTIST);
        String genre = json.getString(JSON_GENRE);
        String source = json.getString(JSON_SOURCE);
        String iconUrl = json.getString(JSON_IMAGE);
        int trackNumber = json.getInt(JSON_TRACK_NUMBER);
        int totalTrackCount = json.getInt(JSON_TOTAL_TRACK_COUNT);
        int duration = json.getInt(JSON_DURATION) * 1000; // ms

        source = CATELOG_URL_PRE + "/" + source;
        iconUrl = CATELOG_URL_PRE + "/" + iconUrl;

        String id = String.valueOf(source);

        return new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, id)
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, source)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
                .putString(MediaMetadataCompat.METADATA_KEY_GENRE, genre)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, iconUrl)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, trackNumber)
                .putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, totalTrackCount)
                .build();
    }


    /**
     * 是否初始化
     *
     * @return
     */
    public boolean isInit() {
        return mCurrentState == State.INITIALIZED;
    }

    public MediaMetadataCompat getMusic(String mediaId) {
        return musicList.get(mediaId);
    }

    public void updateMusic(String mediaId, MediaMetadataCompat music) {
        MediaMetadataCompat track = musicList.get(mediaId);
        if (track != null) {
            musicList.put(mediaId, track);
        }
    }

    private enum State {
        NON_INITIALIZED, INITIALIZING, INITIALIZED
    }

    public interface CallBack {
        void onMusicCatelogReady(boolean success);
    }
}
