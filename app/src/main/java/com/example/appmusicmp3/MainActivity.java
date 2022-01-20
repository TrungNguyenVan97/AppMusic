package com.example.appmusicmp3;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.security.cert.TrustAnchor;
import java.util.ArrayList;

public class MainActivity extends Activity {
    private SearchView svFind;
    private ImageButton btnRecent;
    private ImageButton btnLike;
    private ImageButton btnList;
    private RecyclerView rvSong;
    private LinearLayout layoutBottom;
    private SongAdapter adapter;
    private ArrayList<Song> listSong = new ArrayList<>();
    private Song songPlaying;
    private TextView tvMainTitle, tvMainArtist;
    private ImageView btnMainPlay, btnMainStop;
    SongService mService;
    private boolean isServiceConnected;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("Bind", "Connected");
            SongService.SongBinder mBinder = (SongService.SongBinder) service;
            mService = mBinder.getSongService();
            isServiceConnected = true;
            setDataLayoutBottom();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("Bind", "Disconnected");
            mService = null;
            isServiceConnected = false;
        }
    };

    private BroadcastReceiver broadcastNext = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Tags.ACTION_MP3_NEXT.equals(intent.getAction())) {
                updateLayoutBottom();
            }
        }
    };

    private BroadcastReceiver broadcastPrev = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Tags.ACTION_MP3_PREV.equals(intent.getAction())) {
                updateLayoutBottom();
            }
        }
    };

    private BroadcastReceiver broadcastPlayPause = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Tags.ACTION_MP3_PLAY_PAUSE.equals(intent.getAction())) {
                updateLayoutBottom();
            }
        }
    };

    private BroadcastReceiver autoNext = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Tags.ACTION_SERVICE_AUTO_NEXT.equals(intent.getAction())) {
                updateLayoutBottom();
            }
        }
    };

    private BroadcastReceiver notificationPlayPause = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Tags.NOTIFICATION_PLAY_PAUSE.equals(intent.getAction())) {
                updateLayoutBottom();
            }
        }
    };

    private BroadcastReceiver notificationNext = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Tags.NOTIFICATION_NEXT.equals(intent.getAction())) {
                updateLayoutBottom();
            }
        }
    };

    private BroadcastReceiver notificationPrev = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Tags.NOTIFICATION_PREV.equals(intent.getAction())) {
                updateLayoutBottom();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        registerReceiver();
        findView();
        initView();
        checkPermission();
        initAction();
        setDataLayoutBottom();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Tags.PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        getSong();
                        initView();
                        initAction();
                    }
                }
        }
    }

    private void setDataLayoutBottom() {
        Log.d("Bind", "setDataLayoutBottom");
        if (MusicBuilder.g().getSongPlaying() == null) {
            return;
        }
        if (MusicBuilder.g().getMediaPlayer() != null) {
            layoutBottom.setVisibility(View.VISIBLE);
            songPlaying = MusicBuilder.g().getSongPlaying();
            tvMainTitle.setText(songPlaying.getTitle());
            tvMainArtist.setText(songPlaying.getArtist());
            setImage();
        } else {
            layoutBottom.setVisibility(View.GONE);
        }
    }

    private void updateLayoutBottom() {
        if (MusicBuilder.g().getSongPlaying() == null) {
            return;
        }
        songPlaying = MusicBuilder.g().getSongPlaying();
        tvMainTitle.setText(songPlaying.getTitle());
        tvMainArtist.setText(songPlaying.getArtist());
        setImage();
    }

    private void setImage() {
        if (MusicBuilder.g().getMediaPlayer() == null) {
            return;
        }
        if (MusicBuilder.g().getMediaPlayer().isPlaying()) {
            btnMainPlay.setImageResource(R.drawable.ic_pause);
        } else {
            btnMainPlay.setImageResource(R.drawable.ic_play);
        }
    }

    // kiểm tra cấp quyền
    private void checkPermission() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, Tags.PERMISSION_REQUEST_CODE);
        } else {
            getSong();
        }
    }

    private void findView() {
        rvSong = findViewById(R.id.rcvListSong);
        //rvSong.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        svFind = findViewById(R.id.svFind);
        btnRecent = findViewById(R.id.btnListRecent);
        btnLike = findViewById(R.id.btnListLike);
        btnList = findViewById(R.id.btnList);
        tvMainTitle = findViewById(R.id.tvMainTitle);
        tvMainArtist = findViewById(R.id.tvMainArtist);
        btnMainPlay = findViewById(R.id.btnMainPlay);
        btnMainStop = findViewById(R.id.btnMainStop);
        layoutBottom = findViewById(R.id.layoutPlayMP3Main);
    }

    // khởi tạo view
    private void initView() {
        adapter = new SongAdapter(listSong);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this, RecyclerView.VERTICAL, false);
        rvSong.setLayoutManager(linearLayoutManager);
        rvSong.setAdapter(adapter);
    }

    // sự kiện click
    private void initAction() {

        adapter.setCallBack(new SongAdapter.CallBack() {
            @Override
            public void playMP3(int position) {
                // gửi Data cho PlayMP3
                songPlaying = listSong.get(position);
                Intent intentPlayMP3 = new Intent(MainActivity.this, PlayMP3Activity.class);
                intentPlayMP3.putExtra(Tags.EXTRA_MP3_SONG, songPlaying);
                startActivity(intentPlayMP3);

                // gửi Data cho service
                Intent intent = new Intent(MainActivity.this, SongService.class);
                intent.putExtra(Tags.EXTRA_SERVICE_SONG, songPlaying);
                startService(intent);
                MusicBuilder.g().setConnectedService(true);
                bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
            }
        });

        btnMainPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MusicBuilder.g().getMediaPlayer() == null) {
                    return;
                }
                if (MusicBuilder.g().getMediaPlayer().isPlaying()) {
                    MusicBuilder.g().pause();
                    btnMainPlay.setImageResource(R.drawable.ic_play);
                } else {
                    MusicBuilder.g().play();
                    btnMainPlay.setImageResource(R.drawable.ic_pause);
                }

            }
        });

        btnMainStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isServiceConnected) {
                    unbindService(mServiceConnection);
                    isServiceConnected = false;
                }
                if (MusicBuilder.g().getMediaPlayer() == null) {
                    return;
                }
                layoutBottom.setVisibility(View.GONE);
                MusicBuilder.g().stop();
                Intent intent = new Intent(MainActivity.this, SongService.class);
                stopService(intent);
                MusicBuilder.g().setConnectedService(false);
            }
        });

        layoutBottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentPlayMP3 = new Intent(MainActivity.this, PlayMP3Activity.class);
                intentPlayMP3.putExtra(Tags.EXTRA_MP3_SONG, songPlaying);
                startActivity(intentPlayMP3);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isServiceConnected) {
            unbindService(mServiceConnection);
            isServiceConnected = false;
        }
    }

    private void registerReceiver() {

        IntentFilter filterNext = new IntentFilter(Tags.ACTION_MP3_NEXT);
        registerReceiver(broadcastNext, filterNext);

        IntentFilter filterPrev = new IntentFilter(Tags.ACTION_MP3_PREV);
        registerReceiver(broadcastPrev, filterPrev);

        IntentFilter filterPlayPause = new IntentFilter(Tags.ACTION_MP3_PLAY_PAUSE);
        registerReceiver(broadcastPlayPause, filterPlayPause);

        IntentFilter filterAutoNext = new IntentFilter(Tags.ACTION_SERVICE_AUTO_NEXT);
        registerReceiver(autoNext, filterAutoNext);

        IntentFilter filterNotificationPlayPause = new IntentFilter(Tags.NOTIFICATION_PLAY_PAUSE);
        registerReceiver(notificationPlayPause, filterNotificationPlayPause);

        IntentFilter filterNotificationNext = new IntentFilter(Tags.NOTIFICATION_NEXT);
        registerReceiver(notificationNext, filterNotificationNext);

        IntentFilter filterNotificationPrev = new IntentFilter(Tags.NOTIFICATION_PREV);
        registerReceiver(notificationPrev, filterNotificationPrev);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastNext);
        unregisterReceiver(broadcastPrev);
        unregisterReceiver(autoNext);
        unregisterReceiver(broadcastPlayPause);
        unregisterReceiver(notificationPlayPause);
        unregisterReceiver(notificationNext);
        unregisterReceiver(notificationPrev);
    }

    // lấy danh sách bài hát từ điện thoại
    private void getSong() {
        ContentResolver contentResolver = getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = contentResolver.query(uri, null, null, null, MediaStore.Audio.Media.TITLE_KEY);

        if (cursor != null && cursor.moveToFirst()) {
            int indexTitle = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int indexArtist = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int indexData = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            int indexID = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
            do {
                String currentTitle = cursor.getString(indexTitle);
                String currentArtist = cursor.getString(indexArtist);
                String currentData = cursor.getString(indexData);
                String currentID = cursor.getString(indexID);
                listSong.add(new Song(currentID, currentTitle, currentArtist, currentData));
            } while (cursor.moveToNext());
        }
        MusicBuilder.g().setListSong(listSong);
        adapter.notifyDataSetChanged();
    }
}
