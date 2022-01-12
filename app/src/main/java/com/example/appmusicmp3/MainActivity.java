package com.example.appmusicmp3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;

public class MainActivity extends Activity {

    private SearchView svFind;
    private ImageButton btnRecent;
    private ImageButton btnLike;
    private ImageButton btnList;
    private RecyclerView rvSong;
    private SongAdapter adapter;
    private ArrayList<Song> listSong = new ArrayList<>();
    private static final int PERMISSION_REQUEST_CODE = 2021;
    private static final int REQUEST_CODE_PLAY = 2022;
    public static final String EXTRA_PLAY_MP3_LIST = "EXTRA_PLAY_MP3-LIST";
    public static final String EXTRA_PLAY_MP3_POSITION = "EXTRA_PLAY_MP3_POSITION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                        Toast.makeText(MainActivity.this, "Đã cấp quyền truy cập", Toast.LENGTH_SHORT).show();
                    }
                }
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
                Intent intent = new Intent(MainActivity.this, PlayMP3Activity.class);
                intent.putExtra(EXTRA_PLAY_MP3_LIST, listSong);
                intent.putExtra(EXTRA_PLAY_MP3_POSITION, position);
                startActivityForResult(intent, REQUEST_CODE_PLAY);
            }
        });
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
            do {
                String currentTitle = cursor.getString(indexTitle);
                String currentArtist = cursor.getString(indexArtist);
                String currentData = cursor.getString(indexData);

                listSong.add(new Song(currentTitle, currentArtist, currentData));
            } while (cursor.moveToNext());
        }
        adapter.notifyDataSetChanged();
    }
}
