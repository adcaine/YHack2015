package com.example.ehc.myapplication;



public class Data {
    private int _id;
    private String _work;
    private String _date;

    public Data(){

    }

    public Data(String work){
        this._work = work;
    }

    public Data(String work, String date){
        this._work = work;
        this._date = date;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public void set_work(String _work) {
        this._work = _work;
    }

    public void set_date(String _date) {
        this._date = _date;
    }

    public int get_id() {
        return _id;
    }

    public String get_work() {
        return _work;
    }

    public String get_date() {
        return _date;
    }
}
