package com.basement.panosx2.moviedatabase.Helpers;

/*
 * Created by panos on 5/9/2019
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import com.basement.panosx2.moviedatabase.Objects.Saved;

import java.util.ArrayList;
import java.util.List;

public class DBStuff {
    private static final String dbName = "MovieDB";
    private static final int dbVersion = 1;
    private static final String watchlistTable = "watchlist";

    private final Context context;
    private DbHelper dbHelper;
    private SQLiteDatabase database;
    private ContentValues cv;

    private static class DbHelper extends SQLiteOpenHelper {
        public DbHelper(Context context) {
            super(context, dbName, null, dbVersion);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + watchlistTable + " (" +
                    "id TEXT PRIMARY KEY NOT NULL, " +
                    "poster TEXT NOT NULL, " +
                    "title TEXT NOT NULL, " +
                    "type TEXT NOT NULL," +
                    "description TEXT NOT NULL);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + watchlistTable);
            onCreate(db);
        }
    }

    public DBStuff(Context c) {
        context = c;
    }

    public DBStuff open() throws SQLException {
        dbHelper = new DbHelper(context);
        database = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    public void addToWatchlist(Context _context, Saved saved) {
        cv = new ContentValues();
        cv.put("id", "" + saved.getId());
        cv.put("poster", saved.getPoster());
        cv.put("title", saved.getTitle());
        cv.put("type", saved.getType());
        cv.put("description", saved.getDescription());
        database.insert(watchlistTable, null, cv);

        Toast.makeText(_context, "Added to watchlist!", Toast.LENGTH_SHORT).show();
    }

    public void removeFromWatchList(Context _context, int id) {
        database.delete(watchlistTable, "id="+id+"", null);

        Toast.makeText(_context, "Removed from watchlist!", Toast.LENGTH_SHORT).show();
    }

    public boolean isInWatchlist(int id) {
        String s = "SELECT id FROM " + watchlistTable + " WHERE id=?";

        Cursor c = database.rawQuery(s, new String[]{"" + id});
        if (c.getCount() != 0) {
            c.close();
            return true;
        }
        else {
            c.close();
            return false;
        }
    }

    public List<Saved> getWatchlist() {
        String id, poster, title, type, description;
        List<Saved> watchlist = new ArrayList();

        String s = "SELECT id,poster,title,type,description FROM " + watchlistTable;

        Cursor c = database.rawQuery(s, null);
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            id = c.getString(0);
            poster = c.getString(1);
            title = c.getString(2);
            type = c.getString(3);
            description = c.getString(4);
            watchlist.add(new Saved(Integer.parseInt(id), poster, title, type, description));
        }
        c.close();

        return watchlist;
    }

    public List<Saved> getFilteredWatchList(String query) {
        String id, poster, title, type, description;
        List<Saved> filteredWatchlist = new ArrayList();

        String s = "SELECT * FROM " + watchlistTable + " WHERE title LIKE '%"+query+"%'";

        Cursor c = database.rawQuery(s, null);
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            id = c.getString(0);
            poster = c.getString(1);
            title = c.getString(2);
            type = c.getString(3);
            description = c.getString(4);
            filteredWatchlist.add(new Saved(Integer.parseInt(id), poster, title, type, description));

            Log.d("DB", "search result: "+title);
        }
        c.close();

        return filteredWatchlist;
    }
}
