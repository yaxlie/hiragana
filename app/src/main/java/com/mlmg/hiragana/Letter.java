package com.mlmg.hiragana;

/**
 * Created by Marcin on 08.11.2017.
 */

public class Letter {
    public Letter(){}
    public Letter(Letter l){
        this.uid = l.getUid();
        this.letter_h = l.getLetter_h();
        this.letter_l = l.getLetter_l();
        this.category = l.getCategory();
        this.exp = l.getExp();
    }
    public Letter(int uid, String letter_h, String letter_l, int category, int exp){
        this.uid = uid;
        this.letter_h = letter_h;
        this.letter_l = letter_l;
        this.category = category;
        this.exp = exp;
    }
    private int uid;
    private String letter_h;
    private String letter_l;
    private int category;
    private int exp;

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getLetter_h() {
        return letter_h;
    }

    public void setLetter_h(String letter_h) {
        this.letter_h = letter_h;
    }

    public String getLetter_l() {
        return letter_l;
    }

    public void setLetter_l(String letter_l) {
        this.letter_l = letter_l;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }
}
