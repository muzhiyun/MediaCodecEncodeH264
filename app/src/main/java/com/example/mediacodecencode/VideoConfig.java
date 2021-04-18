package com.example.mediacodecencode;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class VideoConfig {

    public class MyDBOpenHelper extends SQLiteOpenHelper {
        public MyDBOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                              int version) {super(context, "my.db", null, 1); }
        @Override
        //数据库第一次创建时被调用
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE person(personid INTEGER PRIMARY KEY AUTOINCREMENT,name VARCHAR(20))");

        }
        //软件版本号发生改变时调用
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("ALTER TABLE person ADD phone VARCHAR(12) NULL");
        }
    }
}
