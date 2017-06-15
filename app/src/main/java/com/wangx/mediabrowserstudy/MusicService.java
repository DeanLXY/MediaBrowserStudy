package com.wangx.mediabrowserstudy;

import android.app.Notification;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.wangx.mediabrowserstudy.common.MediaNotificationHelper;
import com.wangx.mediabrowserstudy.data.MusicProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MusicService extends MediaBrowserServiceCompat {

    private static final int NOTIFICATION_ID = 102;
    private MusicProvider mMusicProvider;
    private MediaSessionCompat mSession;
    private MediaSessionCompat.QueueItem mCurrentMedia;
    private NotificationManagerCompat mNotificationManager;
    private Playback mPlayback;
    private android.support.v4.media.session.MediaSessionCompat.Callback mCallBack = new MediaSessionCompat.Callback() {
        @Override
        public void onPlay() {
            super.onPlay();
            if (mCurrentMedia != null) {
                handlePlayRequest();
            }
        }

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            super.onPlayFromMediaId(mediaId, extras);
            MediaMetadataCompat media = mMusicProvider.getMusic(mediaId);
            if (media != null) {
                mCurrentMedia = new MediaSessionCompat.QueueItem(media.getDescription(), mediaId.hashCode());
                handlePlayRequest();
            }
        }

        @Override
        public void onSeekTo(long pos) {
            super.onSeekTo(pos);
            mPlayback.seekTo((int) pos);
        }

        @Override
        public void onPause() {
            super.onPause();
//            mPlayback.pause();
            handlePauseRequest();
        }

        @Override
        public void onStop() {
            super.onStop();
//            mPlayback.stop();
            handleStopRequest();
        }
    };

    private void handleStopRequest() {
        mPlayback.stop();
        updatePlaybackState(null);
    }

    private void handlePauseRequest() {
        mPlayback.pause();
    }

    private void handlePlayRequest() {
        if (mCurrentMedia == null) {
            return;
        }

        if (!mSession.isActive()) {
            mSession.setActive(true);
        }

        updateMetaData();
        mPlayback.play(mCurrentMedia);
    }

    private void updateMetaData() {
        MediaSessionCompat.QueueItem queueItem = mCurrentMedia;
        String mediaId = queueItem.getDescription().getMediaId();
        MediaMetadataCompat music = mMusicProvider.getMusic(mediaId);
        final String trackId = music.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
        mSession.setMetadata(music);

        if (music.getDescription().getIconBitmap() == null && music.getDescription().getIconUri() != null) {
            fetchArtwork(trackId, music.getDescription().getIconUri());
            postNotification();
        }

    }

    private void fetchArtwork(String mediaId, Uri iconUri) {
        Drawable drawable = getResources().getDrawable(R.mipmap.ic_launcher);
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        //TODO
        MediaSessionCompat.QueueItem queueItem = mCurrentMedia;
        MediaMetadataCompat music = mMusicProvider.getMusic(mediaId);
         music = new MediaMetadataCompat.Builder(music)
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ART, bitmap)
                .putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, bitmap)
                .build();
        mMusicProvider.updateMusic(mediaId, music);
        String currentPlayingMediaId = queueItem.getDescription().getMediaId();
        if (mediaId.equals(currentPlayingMediaId)) {
            mSession.setMetadata(music);
            postNotification();
        }
    }


    private Notification postNotification() {
        Notification notification = MediaNotificationHelper.createNotification(this, mSession);
        if (notification == null) {
            return null;
        }

        mNotificationManager.notify(NOTIFICATION_ID, notification);
        return notification;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println(">>>>onCreate");
        //提供数据---->MediaItem  or  List<MediaItem>
        mMusicProvider = new MusicProvider();

        mSession = new MediaSessionCompat(this, "musicservice");
        setSessionToken(mSession.getSessionToken());
        mSession.setCallback(mCallBack);
        mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mPlayback = new Playback(this, mMusicProvider);

        mPlayback.setCallback(new Playback.Callback() {
            @Override
            public void onPlaybackStatusChanged(int state) {
                updatePlaybackState(null);
            }

            @Override
            public void onCompletion() {
                // In this simple implementation there isn't a play queue, so we simply 'stop' after
                // the song is over.
                handleStopRequest();

            }

            @Override
            public void onError(String error) {
                updatePlaybackState(error);
            }
        });

        mNotificationManager = NotificationManagerCompat.from(this);

        updatePlaybackState(null);
    }

    /**
     * 更新状态  包括 notification
     *
     * @param error
     */
    private void updatePlaybackState(String error) {
        long position = PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN;
        if (mPlayback != null && mPlayback.isConnected()) {
            position = mPlayback.getCurrentStreamPosition();
        }
        long playbackActions = PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID;
        if (mPlayback.isPlaying()) {
            playbackActions |= PlaybackStateCompat.ACTION_PAUSE;
            playbackActions |= PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS;
            playbackActions |= PlaybackStateCompat.ACTION_SKIP_TO_NEXT;
        }

        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder().setActions(playbackActions);
        int state = mPlayback.getState();
        if (error != null) {
            stateBuilder.setErrorMessage(error);
            state = PlaybackStateCompat.STATE_ERROR;
        }

        stateBuilder.setState(state, position, 1.0f, SystemClock.elapsedRealtime());
        if (mCurrentMedia != null) {
            stateBuilder.setActiveQueueItemId(mCurrentMedia.getQueueId());
        }
        mSession.setPlaybackState(stateBuilder.build());
        if (state == PlaybackStateCompat.STATE_PLAYING) {
            Notification notification = postNotification();
            startForeground(NOTIFICATION_ID, notification);
        } else {
            if (state == PlaybackStateCompat.STATE_PAUSED) {
                postNotification();
            } else {
                mNotificationManager.cancel(NOTIFICATION_ID);
            }
            stopForeground(false);
        }
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        // 安全校验  是否是自己应用调起
        if (!clientPackageName.equals(getPackageName())) {
            return new BrowserRoot(MusicProvider.MEDIA_ID_EMPTY_ROOT, null);
        }

        return new BrowserRoot(MusicProvider.MEDIA_ID_ROOT, null);
    }

    @Override
    public void onLoadChildren(@NonNull final String parentId, @NonNull final Result<List<MediaBrowserCompat.MediaItem>> result) {
        System.out.println("onLoadChildren>>>>");
        if (!mMusicProvider.isInit()) {
            result.detach();

            mMusicProvider.retrieveMediaAsync(new MusicProvider.CallBack() {
                @Override
                public void onMusicCatelogReady(boolean success) {
                    if (success) {
                        loadChildrenImpl(parentId, result);
                    } else {
                        result.sendResult(Collections.<MediaBrowserCompat.MediaItem>emptyList());
                    }
                }
            });
        } else {
            loadChildrenImpl(parentId, result);
        }
    }

    /**
     * 处理加载数据成功
     *
     * @param parentId
     * @param result
     */
    private void loadChildrenImpl(String parentId, Result<List<MediaBrowserCompat.MediaItem>> result) {
        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();
        if (parentId.equals(MusicProvider.MEDIA_ID_ROOT)) {
            Iterable<MediaMetadataCompat> musicList = mMusicProvider.getMusicList();
            for (MediaMetadataCompat track : musicList) {
                MediaBrowserCompat.MediaItem mediaItem = new MediaBrowserCompat.MediaItem(track.getDescription(), MediaBrowserCompat.MediaItem.FLAG_PLAYABLE);
                mediaItems.add(mediaItem);
            }
        }
        result.sendResult(mediaItems);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("service ondestory");
        handleStopRequest();
        mSession.release();
    }
}