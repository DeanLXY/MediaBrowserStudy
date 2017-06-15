package com.wangx.mediabrowserstudy;


import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;


/**
 * A simple {@link Fragment} subclass.
 */
public class MediaListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {


    private MediaAdapter mMediaAdapter;
    private String mMediaId;
    private android.support.v4.media.session.MediaControllerCompat.Callback mControllerCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
        }

    };
    private MediaBrowserCompat mMediaBrowser;
    private SwipeRefreshLayout mRefreshLayout;
    private MediaBrowserCompat.SubscriptionCallback mSubscriptionCallback = new MediaBrowserCompat.SubscriptionCallback() {
        @Override
        public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children) {
            super.onChildrenLoaded(parentId, children);
            mMediaAdapter.clear();
            mMediaAdapter.addAll(children);
            mRefreshLayout.setRefreshing(false);
        }
    };
    private MediaBrowserCompat.ConnectionCallback mConnectionCallBack = new MediaBrowserCompat.ConnectionCallback() {
        @Override
        public void onConnectionFailed() {
            super.onConnectionFailed();
            System.out.println(">>>onConnectionFailed");

        }

        @Override
        public void onConnectionSuspended() {
            super.onConnectionSuspended();
            System.out.println(">>>onConnectionSuspended");
            MediaControllerCompat mediaController = MediaControllerCompat.getMediaController(getActivity());
            if (mediaController != null) {
                mediaController.unregisterCallback(mControllerCallback);
                MediaControllerCompat.setMediaController(getActivity(), null);
            }
        }

        @Override
        public void onConnected() {
            super.onConnected();
            System.out.println(">>>onConnected");
            handleConnected();
        }
    };

    public MediaListFragment() {
        // Required empty public constructor
    }

    public static MediaListFragment newInstance() {
        MediaListFragment fragment = new MediaListFragment();
        return fragment;
    }

    private void handleConnected() {
        if (mMediaId == null) {
            mMediaId = mMediaBrowser.getRoot();
        }
        mMediaBrowser.subscribe(mMediaId, mSubscriptionCallback);

        try {
            MediaControllerCompat mediaController =
                    new MediaControllerCompat(getActivity(),
                            mMediaBrowser.getSessionToken());
            MediaControllerCompat.setMediaController(getActivity(), mediaController);

            // Register a Callback to stay in sync
            mediaController.registerCallback(mControllerCallback);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to connect to MediaController", e);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // don't do anything other  ,fragmentManager would deal the error

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_media_list, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.listview);
        mMediaAdapter = new MediaAdapter(getActivity());
        listView.setAdapter(mMediaAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MediaBrowserCompat.MediaItem item = mMediaAdapter.getItem(position);
                FragmentDataHelper dataHelper = (FragmentDataHelper) getActivity();
                dataHelper.onMediaItemSelected(item, false);
            }
        });

        mRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.refresh);
        mRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mRefreshLayout.setRefreshing(true);
            }
        });
        mRefreshLayout.setOnRefreshListener(this);
        //MediaBrowser
        mMediaBrowser = new MediaBrowserCompat(
                getActivity(),
                new ComponentName(getActivity(), MusicService.class), mConnectionCallBack, null);
        return rootView;
    }

    @Override
    public void onRefresh() {
//        handleConnected();
        if (mMediaId == null) {
            mMediaId = mMediaBrowser.getRoot();
        }
        mMediaBrowser.unsubscribe(mMediaId);
        mMediaBrowser.subscribe(mMediaId, mSubscriptionCallback);
    }

    @Override
    public void onStart() {
        super.onStart();
        mMediaBrowser.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMediaBrowser.disconnect();
    }

    public interface FragmentDataHelper {
        void onMediaItemSelected(MediaBrowserCompat.MediaItem item, boolean isPlaying);
    }

    public class MediaAdapter extends ArrayAdapter<MediaBrowserCompat.MediaItem> {

        public MediaAdapter(@NonNull Context context) {
            super(context, R.layout.item_media_info, new ArrayList<MediaBrowserCompat.MediaItem>());
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.item_media_info, parent, false);
            }
            TextView tvAuthor = (TextView) convertView.findViewById(R.id.tv_media_author);
            tvAuthor.setText(getItem(position).getDescription().getSubtitle());
            TextView tvName = (TextView) convertView.findViewById(R.id.tv_media_name);
            tvName.setText(getItem(position).getDescription().getTitle());
            ImageView ivCover = (ImageView) convertView.findViewById(R.id.iv_cover);
            Glide
                    .with(getActivity())
                    .load(getItem(position).getDescription().getIconUri())
                    .into(ivCover);
            return convertView;
        }
    }
}
