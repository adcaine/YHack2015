package com.example.ehc.myapplication;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;
import android.content.Context;
import android.content.ContentValues;


import java.util.ArrayList;

public class DBHandler extends SQLiteOpenHelper{

    private static final int DATABASE_VERSION=1;
    private static final String DATABASE_NAME = "asmnts.db";
    public static final String TABLE_DATA = "data";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_WORK = "_work";
    public static final String COLUMN_DATE = "_date";

    public DBHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_DATA + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_WORK + " TEXT, " +COLUMN_DATE+ " TEXT "+
                ");";
        db.execSQL(query);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DATA);
        onCreate(db);

    }

    public void addData(Data data){
        ContentValues values = new ContentValues();
        values.put(COLUMN_WORK, data.get_work());
        values.put(COLUMN_DATE, data.get_date());

        SQLiteDatabase db = getWritableDatabase();
        db.insert(TABLE_DATA, null, values);
        db.close();

    }

    public void deleteData(String dataName){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_DATA + " WHERE " + COLUMN_WORK+ "=\"" + dataName +"\";");

    }

    public ArrayList toArray(){
        ArrayList<String> listItems=new ArrayList<String>();
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " +TABLE_DATA+ " WHERE 1";

        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();


        while (!c.isAfterLast()){
            if(c.getString(c.getColumnIndex("_work"))!=null){
                listItems.add("\n"+(c.getString(c.getColumnIndex("_work"))).toUpperCase()+"\n"+c.getString(c.getColumnIndex("_date"))+"\n");


            }
            c.moveToNext();
        }
        db.close();
        return listItems;



    }
}