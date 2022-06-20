package com.arifgolgeci.haritalarseyahat.roomDb;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.arifgolgeci.haritalarseyahat.model.Place;

@Database(entities = {Place.class},version=1)
public abstract class PlaceDatabase extends RoomDatabase {
    public abstract  PlaceDao placeDao();

}
