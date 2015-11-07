package com.example.ehc.myapplication;

/**
 * Created by Sam on 2015-11-07.
 */
public class Data {
    private int _id;
    private String _location;
    private String _date;

    public Data(){

    }

    public Data(String work){
        this._location = work;
    }

    public Data(String work, String date){
        this._location = work;
        this._date = date;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public void set_location(String _location) {
        this._location = _location;
    }

    public void set_date(String _date) {
        this._date = _date;
    }

    public int get_id() {
        return _id;
    }

    public String get_location() {
        return _location;
    }

    public String get_date() {
        return _date;
    }
}
