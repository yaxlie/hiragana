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

public class HiraganaDatabase extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "hiragana.data";

    public HiraganaDatabase(Context context) {
        super(context, DATABASE_NAME, null, 5);
        //SQLiteDatabase db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table IF NOT EXISTS hiragana (uid INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "letter_h TEXT, " +
                "letter_l TEXT, " +
                "category INTEGER, " +
                "exp INTEGER)");
        onCreateDb(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS hiragana");
        onCreate(db);
    }

    private void insert(SQLiteDatabase db, String h, String l, int category) {
        ContentValues values;
        values = new ContentValues();
        values.put("letter_h", h);
        values.put("letter_l", l);
        values.put("category", category);
        values.put("exp", 0);
        db.insert("hiragana", null, values);
    }

    private void onCreateDb(SQLiteDatabase db){
        insert(db, "ん", "N", HiraganaTable.Category.A);
        insert(db, "わ", "WA", HiraganaTable.Category.A);
        insert(db, "ら", "RA", HiraganaTable.Category.A);
        insert(db, "や", "YA", HiraganaTable.Category.A);
        insert(db, "ま", "MA", HiraganaTable.Category.A);
        insert(db, "は", "HA", HiraganaTable.Category.A);
        insert(db, "な", "NA", HiraganaTable.Category.A);
        insert(db, "た", "TA", HiraganaTable.Category.A);
        insert(db, "さ", "SA", HiraganaTable.Category.A);
        insert(db, "か", "KA", HiraganaTable.Category.A);
        insert(db, "あ", "A", HiraganaTable.Category.A);

        insert(db, "うぃ", "WI", HiraganaTable.Category.I);
        insert(db, "り", "RI", HiraganaTable.Category.I);
        insert(db, "み", "MI", HiraganaTable.Category.I);
        insert(db, "ひ", "HI", HiraganaTable.Category.I);
        insert(db, "に", "NI", HiraganaTable.Category.I);
        insert(db, "ち", "CHI", HiraganaTable.Category.I);
        insert(db, "し", "SHI", HiraganaTable.Category.I);
        insert(db, "き", "KI", HiraganaTable.Category.I);
        insert(db, "い", "I", HiraganaTable.Category.I);

        insert(db, "る", "RU", HiraganaTable.Category.U);
        insert(db, "ゆ", "YU", HiraganaTable.Category.U);
        insert(db, "む", "MU", HiraganaTable.Category.U);
        insert(db, "ふ", "FU", HiraganaTable.Category.U);
        insert(db, "ぬ", "NU", HiraganaTable.Category.U);
        insert(db, "つ", "TSU", HiraganaTable.Category.U);
        insert(db, "す", "SU", HiraganaTable.Category.U);
        insert(db, "く", "KU", HiraganaTable.Category.U);
        insert(db, "う", "U", HiraganaTable.Category.U);

        insert(db, "うぇ", "WE", HiraganaTable.Category.E);
        insert(db, "れ", "RE", HiraganaTable.Category.E);
        insert(db, "め", "ME", HiraganaTable.Category.E);
        insert(db, "へ", "HE", HiraganaTable.Category.E);
        insert(db, "ね", "NE", HiraganaTable.Category.E);
        insert(db, "て", "TE", HiraganaTable.Category.E);
        insert(db, "せ", "SE", HiraganaTable.Category.E);
        insert(db, "け", "KE", HiraganaTable.Category.E);
        insert(db, "え", "E", HiraganaTable.Category.E);

        insert(db, "を", "WO", HiraganaTable.Category.O);
        insert(db, "ろ", "RO", HiraganaTable.Category.O);
        insert(db, "よ", "YO", HiraganaTable.Category.O);
        insert(db, "も", "MO", HiraganaTable.Category.O);
        insert(db, "ほ", "HO", HiraganaTable.Category.O);
        insert(db, "の", "NO", HiraganaTable.Category.O);
        insert(db, "と", "TO", HiraganaTable.Category.O);
        insert(db, "そ", "SO", HiraganaTable.Category.O);
        insert(db, "こ", "KO", HiraganaTable.Category.O);
        insert(db, "お", "O", HiraganaTable.Category.O);

    }

    public Letter getRandomAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from hiragana", null);
        int limit = res.getCount();
        Random rand = new Random();
        int i = rand.nextInt(limit) + 1;
        Letter letter = null;

        if(res.move(i)) {
            letter = new Letter(res.getInt(0), res.getString(1), res.getString(2), res.getInt(3), res.getInt(4));
        }
        return letter;
    }

    public Letter getRandomCategory(int category) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from hiragana where category = CAST("+category+" as TEXT)" , null);
        int limit = res.getCount();
        Random rand = new Random();
        int i = rand.nextInt(limit) + 1;
        Letter letter = null;

        if(res.move(i)) {
            letter = new Letter(res.getInt(0), res.getString(1), res.getString(2), res.getInt(3), res.getInt(4));
        }
        return letter;
    }

    public Cursor getAllFromCategory(int category) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from hiragana where category = " + Integer.toString(category), null);
        return res;
    }

    public int getSizeCategory(int category){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from hiragana where category = CAST("+category+" as TEXT)" , null);
        return res.getCount();
    }
}