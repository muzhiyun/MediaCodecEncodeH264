package com.example.mediacodecencode;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class YUVPlayerActivity extends AppCompatActivity {
    String TAG = "YUVEncodeActivity";
    public static String path="/sdcard/test_NV21.yuv";
    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yuvplayer);
        Intent intent = getIntent();
        Uri uri = intent.getData();
        if (uri != null) {
            path=uri.getPath();
            Log.e(TAG, "Load path = " +path);
        }

        // Example of a call to a native method
//        TextView tv = findViewById(R.id.sample_text);
//        tv.setText(stringFromJNI());
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    //public native String stringFromJNI();
}
