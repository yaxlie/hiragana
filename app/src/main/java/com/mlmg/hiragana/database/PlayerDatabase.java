package com.mlmg.hiragana.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


/**
 * Created by Marcin on 08.11.2017.
 */

public class PlayerDatabase extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "user.config";

    public PlayerDatabase(Context context) {
        super(context, DATABASE_NAME, null, 2);
        //SQLiteDatabase db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table IF NOT EXISTS levels (uid INTEGER PRIMARY KEY, crown INTEGER, unlocked INTEGER)");
        db.execSQL("create table IF NOT EXISTS user (uid INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "score INTEGER, timescore INTEGER, premium INTEGER)");
        db.execSQL("create table IF NOT EXISTS duel (uid INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "bot_level INTEGER, wins INTEGER, loses INTEGER)");
        setLevels(db);
        setUser(db);
        setDuel(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //db.execSQL("DROP TABLE IF EXISTS levels");
        //db.execSQL("DROP TABLE IF EXISTS user");
        onCreate(db);
    }

    private void setDuel(SQLiteDatabase db) {
        ContentValues values;
        values = new ContentValues();
        values.put("uid", 1);
        values.put("bot_level", 50);
        values.put("wins", 0);
        values.put("loses", 0);
        db.insert("duel", null, values);
    }

    public void winDuel(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE duel set wins = wins + 1 where uid = 1");
        db.execSQL("UPDATE duel set bot_level = MIN(bot_level + 5, 95) where uid = 1");
    }

    public void loseDuel(){
        SQLiteDatabase db = this.getWritableDatabase();
        //db.execSQL("UPDATE duel set bot_level = MAX(bot_level - 5, 10) where uid = 1");
    }

    public int getLoses(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select loses from duel where uid = 1" , null);
        return res.moveToNext()? res.getInt(0): 0;
    }

    public int getWins(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select wins from duel where uid = 1" , null);
        return res.moveToNext()? res.getInt(0): 0;
    }

    public int getBotLevel(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select bot_level from duel where uid = 1" , null);
        return res.moveToNext()? res.getInt(0): 0;
    }

    private void insertLevel(SQLiteDatabase db, int uid, int value, int unlocked) {
        ContentValues values;
        values = new ContentValues();
        values.put("uid", uid);
        values.put("crown", value);
        values.put("unlocked", unlocked);
        db.insert("levels", null, values);
    }

    private void setLevels(SQLiteDatabase db){
        insertLevel(db, HiraganaTable.Category.A, 0, 1);
        insertLevel(db, HiraganaTable.Category.I, 0, 0);
        insertLevel(db, HiraganaTable.Category.U, 0, 0);
        insertLevel(db, HiraganaTable.Category.E, 0, 0);
        insertLevel(db, HiraganaTable.Category.O, 0, 0);
    }

    private void setUser(SQLiteDatabase db) {
        ContentValues values;
        values = new ContentValues();
        values.put("score", 0);
        values.put("timescore", 0);
        values.put("premium", 0);
        db.insert("user", null, values);
    }

    public int getTimescore(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select timescore from user where uid = 1" , null);
        return res.moveToNext()? res.getInt(0): 0;
    }

    public int getScore(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select score from user where uid = 1" , null);
        return res.moveToNext()? res.getInt(0): 0;
    }

    public int getPremium(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select premium from user where uid = 1" , null);
        return res.moveToNext()? res.getInt(0): 0;
    }

    public boolean isCrowned(int levelUid){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select crown from levels where uid = " + Integer.toString(levelUid), null);
        return res.moveToNext() && res.getInt(0) == 1;
    }

    public boolean isUnlocked(int levelUid){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select unlocked from levels where uid = " + Integer.toString(levelUid), null);
        return res.moveToNext() && res.getInt(0) == 1;
    }

    public void setUnCrown(int levelUid){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE levels set crown = 0 where uid = " + Integer.toString(levelUid));
    }

    public void setCrown(int levelUid){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE levels set crown = 1 where uid = " + Integer.toString(levelUid));

        //todo add to playservice
    }

    public void setTimescore(int score){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE user set timescore = " + Integer.toString(score));
    }

    public void addPoints(int points){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE user set score = score + " + Integer.toString(points));
    }

    public void setScore(int score){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE user set score = " + Integer.toString(score));
    }

    public void unlockLevel(int uid){
        SQLiteDatabase db = this.getWritableDatabase();
        if(uid<6) {
            db.execSQL("UPDATE levels set unlocked = 1 where uid = " + Integer.toString(uid));
            db.execSQL("UPDATE levels set crown = 0");
        }
    }
}