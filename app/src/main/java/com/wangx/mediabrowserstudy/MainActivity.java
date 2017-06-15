package com.wangx.mediabrowserstudy;

import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements MediaListFragment.FragmentDataHelper {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fl_container, MediaListFragment.newInstance())
                .commit();
    }

    @Override
    public void onMediaItemSelected(MediaBrowserCompat.MediaItem item, boolean isPlaying) {
        if (item.isPlayable()) {
            MediaControllerCompat mediaController = MediaControllerCompat.getMediaController(this);
            MediaControllerCompat.TransportControls transportControls = mediaController.getTransportControls();
            if (isPlaying){
                transportControls.pause();
            }else{
                transportControls.playFromMediaId(item.getMediaId(), null);
            }
//            transportControls.
        }

    }
}
