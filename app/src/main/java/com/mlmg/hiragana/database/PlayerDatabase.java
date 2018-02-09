package com.mlmg.hiragana.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.mlmg.hiragana.Letter;

import java.util.Random;


/**
 * Created by Marcin on 08.11.2017.
 */

public class PlayerDatabase extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "user.config";

    public PlayerDatabase(Context context) {
        super(context, DATABASE_NAME, null, 4);
        //SQLiteDatabase db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table IF NOT EXISTS levels (uid INTEGER PRIMARY KEY, crown INTEGER, unlocked INTEGER)");
        db.execSQL("create table IF NOT EXISTS user (uid INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "score INTEGER, timescore INTEGER, premium INTEGER)");
        db.execSQL("create table IF NOT EXISTS duel (uid INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "bot_level INTEGER, wins INTEGER, loses INTEGER)");

        //signs stats
        db.execSQL("create table IF NOT EXISTS stats (name TEXT PRIMARY KEY, " +
                "correct_answers INTEGER, wrong_answers INTEGER, quiz_value Real, draw_value REAL)");

        setLevels(db);
        setUser(db);
        setDuel(db);
        setStats(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //db.execSQL("DROP TABLE IF EXISTS levels");
        //db.execSQL("DROP TABLE IF EXISTS user");
        onCreate(db);
    }


    //------------STATS
    private void insertStat(SQLiteDatabase db, String hir, String name) {
        ContentValues values;
        values = new ContentValues();
        values.put("name", name);
        values.put("correct_answers", 0);
        values.put("wrong_answers", 0);
        values.put("quiz_value", 0);
        values.put("draw_value", 0);
        db.insert("stats", null, values);
    }

    public float getStatsDrawValue(String name){
        name = "'" + name.toUpperCase() + "'";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select draw_value from stats where name like " + name , null);
        float f =  res.moveToNext()? res.getFloat(0): 0;
        res.close();
        return f;
    }
    public float getStatsQuizValue(String name){
        name = "'" + name.toUpperCase() + "'";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select quiz_value from stats where name like " + name , null);
        float f =  res.moveToNext()? res.getFloat(0): 0;
        res.close();
        return f;
    }
    public int getStatsCAnswers(String name){
        name = "'" + name.toUpperCase() + "'";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select correct_answers from stats where name like " + name , null);
        int i =  res.moveToNext()? res.getInt(0): 0;
        res.close();
        return i;
    }
    public int getStatsWAnswers(String name){
        name = "'" + name.toUpperCase() + "'";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select wrong_answers from stats where name like " + name , null);
        int i =  res.moveToNext()? res.getInt(0): 0;
        res.close();
        return i;
    }

    public void upStatsDrawValue(String name, float accuracy){
        name = "'" + name.toUpperCase() + "'";
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE stats set draw_value = (draw_value * 4 +" + accuracy +") /5 where name like " + name);
    }
    public void upStatsQuizValue(String name, boolean correct){
        float value = correct? 1: 0;
        name = "'" + name.toUpperCase() + "'";
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE stats set quiz_value = (quiz_value * 9 +" + value +") /10 where name like " + name);
    }

    public String getWorseDrawLetter(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT name FROM stats order by (draw_value) desc LIMIT 10;", null);
        Random rand = new Random();
        int i = rand.nextInt(10);
        String s =  res.move(i)? res.getString(0): "A";
        res.close();
        return s;
    }

    public float getDrawScoreAll(){
        SQLiteDatabase db = this.getWritableDatabase();
        int i=0;
        float sum = 0;
        Cursor res = db.rawQuery("SELECT draw_value FROM stats;", null);
        while(res.moveToNext()){
            i++;
            sum+=res.getFloat(0);
        }
        res.close();
        return sum/i;
    }

    public void incStatsCorrectAns(String name){
        name = "'" + name.toUpperCase() + "'";
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE stats set correct_answers = correct_answers + 1 where name like " + name);
    }
    public void incStatsWrongAns(String name){
        name = "'" + name.toUpperCase() + "'";
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE stats set wrong_answers = wrong_answers + 1 where name like " + name);
    }

    private void setStats(SQLiteDatabase db){
        insertStat(db, "ん", "N");
        insertStat(db, "わ", "WA");
        insertStat(db, "ら", "RA");
        insertStat(db, "や", "YA");
        insertStat(db, "ま", "MA");
        insertStat(db, "は", "HA");
        insertStat(db, "な", "NA");
        insertStat(db, "た", "TA");
        insertStat(db, "さ", "SA");
        insertStat(db, "か", "KA");
        insertStat(db, "あ", "A");

        insertStat(db, "うぃ", "WI");
        insertStat(db, "り", "RI");
        insertStat(db, "み", "MI");
        insertStat(db, "ひ", "HI");
        insertStat(db, "に", "NI");
        insertStat(db, "ち", "CHI");
        insertStat(db, "し", "SHI");
        insertStat(db, "き", "KI");
        insertStat(db, "い", "I");

        insertStat(db, "る", "RU");
        insertStat(db, "ゆ", "YU");
        insertStat(db, "む", "MU");
        insertStat(db, "ふ", "FU");
        insertStat(db, "ぬ", "NU");
        insertStat(db, "つ", "TSU");
        insertStat(db, "す", "SU");
        insertStat(db, "く", "KU");
        insertStat(db, "う", "U");

        insertStat(db, "うぇ", "WE");
        insertStat(db, "れ", "RE");
        insertStat(db, "め", "ME");
        insertStat(db, "へ", "HE");
        insertStat(db, "ね", "NE");
        insertStat(db, "て", "TE");
        insertStat(db, "せ", "SE");
        insertStat(db, "け", "KE");
        insertStat(db, "え", "E");

        insertStat(db, "を", "WO");
        insertStat(db, "ろ", "RO");
        insertStat(db, "よ", "YO");
        insertStat(db, "も", "MO");
        insertStat(db, "ほ", "HO");
        insertStat(db, "の", "NO");
        insertStat(db, "と", "TO");
        insertStat(db, "そ", "SO");
        insertStat(db, "こ", "KO");
        insertStat(db, "お", "O");
    }

    // --------------END Stats

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
        int i = res.moveToNext()? res.getInt(0): 0;
        res.close();
        return i;
    }

    public int getWins(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select wins from duel where uid = 1" , null);
        int i =  res.moveToNext()? res.getInt(0): 0;
        res.close();
        return i;
    }

    public int getBotLevel(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select bot_level from duel where uid = 1" , null);
        int i =  res.moveToNext()? res.getInt(0): 0;
        res.close();
        return i;
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
        int i = res.moveToNext()? res.getInt(0): 0;
        res.close();
        return i;
    }

    public int getScore(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select score from user where uid = 1" , null);
        int i =  res.moveToNext()? res.getInt(0): 0;
        res.close();
        return i;
    }

    public int getPremium(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select premium from user where uid = 1" , null);
        int i = res.moveToNext()? res.getInt(0): 0;
        res.close();
        return i;
    }

    public boolean isCrowned(int levelUid){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select crown from levels where uid = " + Integer.toString(levelUid), null);
        boolean b =  res.moveToNext() && res.getInt(0) == 1;
        res.close();
        return b;
    }

    public boolean isUnlocked(int levelUid){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select unlocked from levels where uid = " + Integer.toString(levelUid), null);
        boolean b = res.moveToNext() && res.getInt(0) == 1;
        res.close();
        return b;
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