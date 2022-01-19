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

import java.util.ArrayList;

public class MainActivity extends Activity {
    private SearchView svFind;
    private ImageButton btnRecent;
    private ImageButton btnLike;
    private ImageButton btnList;
    private RecyclerView rvSong;
    private LinearLayout layoutPlayMP3;
    private SongAdapter adapter;
    private ArrayList<Song> listSong = new ArrayList<>();
    private Song songPlaying;
    private TextView tvMainTitle, tvMainArtist;
    private ImageView btnMainPlay, btnMainNext;
    private static final int PERMISSION_REQUEST_CODE = 2021;
    private static final int REQUEST_CODE_PLAY = 2022;
    public static final String EXTRA_MP3_SONG = "EXTRA_PLAY_SONG";
    public static final String EXTRA_SERVICE_SONG = "EXTRA_SERVICE_SONG";
    public static final String EXTRA_TO_PLAY = "EXTRA_TO_PLAY";
    private SongService mService;
    private boolean isServiceConnected;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SongService.SongBinder mBinder = (SongService.SongBinder) service;
            mService = mBinder.getSongService();
            isServiceConnected = true;
            setDataLayoutPlayMP3();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            isServiceConnected = false;
        }
    };

    private BroadcastReceiver broadcastNext = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (PlayMP3Activity.ACTION_NEXT.equals(intent.getAction())) {
                songPlaying = (Song) intent.getSerializableExtra(PlayMP3Activity.EXTRA_NEXT);
                updateLayoutPlayMP3();
            }
        }
    };

    private BroadcastReceiver broadcastPrev = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (PlayMP3Activity.ACTION_PREV.equals(intent.getAction())) {
                songPlaying = (Song) intent.getSerializableExtra(PlayMP3Activity.EXTRA_PREV);
                updateLayoutPlayMP3();
            }
        }
    };

    private BroadcastReceiver broadcastPlayPause = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (PlayMP3Activity.ACTION_PLAY_PAUSE.equals(intent.getAction())) {
                boolean isPlaying = intent.getBooleanExtra(PlayMP3Activity.EXTRA_PLAY_PAUSE, false);
                if (isPlaying) {
                    btnMainPlay.setImageResource(R.drawable.ic_pause);
                } else {
                    btnMainPlay.setImageResource(R.drawable.ic_play);
                }
            }
        }
    };

    private BroadcastReceiver autoNext = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (SongService.ACTION_SEND_MAIN.equals(intent.getAction())) {
                songPlaying = (Song) intent.getSerializableExtra(SongService.SEND_TO_MAIN);
                tvMainTitle.setText(songPlaying.getTitle());
                tvMainArtist.setText(songPlaying.getArtist());
                btnMainPlay.setImageResource(R.drawable.ic_pause);
            }
        }
    };

    private void updateLayoutPlayMP3() {
        tvMainTitle.setText(songPlaying.getTitle());
        tvMainArtist.setText(songPlaying.getArtist());
        if (MusicBuilder.g().getMediaPlayer().isPlaying()) {
            btnMainPlay.setImageResource(R.drawable.ic_pause);
        } else {
            btnMainPlay.setImageResource(R.drawable.ic_play);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IntentFilter filterNext = new IntentFilter(PlayMP3Activity.ACTION_NEXT);
        registerReceiver(broadcastNext, filterNext);

        IntentFilter filterPrev = new IntentFilter(PlayMP3Activity.ACTION_PREV);
        registerReceiver(broadcastPrev, filterPrev);

        IntentFilter filterAutoNext = new IntentFilter(SongService.ACTION_SEND_MAIN);
        registerReceiver(autoNext, filterAutoNext);

        IntentFilter filterPlayPause = new IntentFilter(PlayMP3Activity.ACTION_PLAY_PAUSE);
        registerReceiver(broadcastPlayPause, filterPlayPause);

        findView();
        initView();
        checkPermission();
        initAction();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        getSong();
                        initView();
                        initAction();
                    }
                }
        }
    }

    private void setDataLayoutPlayMP3() {
        layoutPlayMP3.setVisibility(View.VISIBLE);
        tvMainTitle.setText(mService.getSongPlaying().getTitle());
        tvMainArtist.setText(mService.getSongPlaying().getArtist());
        setImage();
    }

    private void setImage() {
        if (mService == null) {
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
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
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
        btnMainNext = findViewById(R.id.btnMainNNext);
        layoutPlayMP3 = findViewById(R.id.layoutPlayMP3Main);
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
                intentPlayMP3.putExtra(EXTRA_MP3_SONG, songPlaying);
                startActivityForResult(intentPlayMP3, REQUEST_CODE_PLAY);

                // gửi Data cho service
                Intent intent = new Intent(MainActivity.this, SongService.class);
                intent.putExtra(EXTRA_SERVICE_SONG, listSong.get(position));
                startService(intent);

                bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
            }
        });

        btnMainPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MusicBuilder.g().getMediaPlayer().isPlaying()) {
                    MusicBuilder.g().pause();
                    btnMainPlay.setImageResource(R.drawable.ic_play);
                } else {
                    MusicBuilder.g().play();
                    btnMainPlay.setImageResource(R.drawable.ic_pause);
                }
            }
        });

        btnMainNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicBuilder.g().stop();
                MusicBuilder.g().nextSong();
                songPlaying = MusicBuilder.g().getSongPlaying();
                MusicBuilder.g().initMediaPlayer(MainActivity.this, songPlaying);
                MusicBuilder.g().play();
                tvMainTitle.setText(songPlaying.getTitle());
                tvMainArtist.setText(songPlaying.getArtist());
                mService.sendNotification(songPlaying);
            }
        });

        layoutPlayMP3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastNext);
        unregisterReceiver(broadcastPrev);
        unregisterReceiver(autoNext);
        unregisterReceiver(broadcastPlayPause);
        Intent intent = new Intent(MainActivity.this, SongService.class);
        stopService(intent);
        MusicBuilder.g().stop();
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
