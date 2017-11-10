package com.mlmg.hiragana.database;

import android.provider.BaseColumns;

/**
 * Created by Marcin on 08.11.2017.
 */

public class HiraganaTable implements BaseColumns {

    public static final String TABLE_NAME = "hiragana";
    public static final String UID = "uid";
    public static final String LETTER_H = "letter_h";
    public static final String LETTER_L = "letter_l";
    public static final String CATEGORY = "category";
    public static final String EXP = "exp";

    public static class Category{
        public static final int A = 1;
        public static final int I = 2;
        public static final int U = 3;
        public static final int E = 4;
        public static final int O = 5;
        public static final int ALL = 6;
    }
}