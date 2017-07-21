package com.example.kwy2868.boostcamp_3rd.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.kwy2868.boostcamp_3rd.Model.Restaurant;

/**
 * Created by kwy2868 on 2017-07-19.
 */

public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "restaurantDB.db";
    private static final String DATABASE_TABLE = "restaurants";

    // 자동으로 증가되게 해주자.
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_ADDRESS = "address";
    private static final String COLUMN_NUMBER = "number";
    private static final String COLUMNN_REPLY = "reply";

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CREATE_TABLE = "create table if not exists " + DATABASE_TABLE + " (" + COLUMN_ID
                + " integer primary key autoincrement, " + COLUMN_NAME + " text, " + COLUMN_ADDRESS + " text, "
                + COLUMN_NUMBER + " text, " + COLUMNN_REPLY + " text" + ")";
        sqLiteDatabase.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("drop table if exists " + DATABASE_TABLE);
        onCreate(sqLiteDatabase);
    }

    public void addRestaurant(Restaurant restaurant){
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, restaurant.getName());
        values.put(COLUMN_ADDRESS, restaurant.getAddress());
        values.put(COLUMN_NUMBER, restaurant.getNumber());
        values.put(COLUMNN_REPLY, restaurant.getReply());

        SQLiteDatabase database = this.getWritableDatabase();
        database.insert(DATABASE_TABLE, null, values);
        database.close();
    }

    public void changeRestaurant(Restaurant restaurant){

    }


    public Cursor findAll(){
        String query = "select * from " + DATABASE_TABLE;
        SQLiteDatabase database = this.getWritableDatabase();

        return database.rawQuery(query, null);
    }
}
