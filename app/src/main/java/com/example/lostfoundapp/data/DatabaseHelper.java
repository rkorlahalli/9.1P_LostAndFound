package com.example.lostfoundapp.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "lost_found.db";
    private static final int DATABASE_VERSION = 2;

    public static final String TABLE_ITEMS = "items";
    public static final String COL_ID = "id";
    public static final String COL_POST_TYPE = "post_type";
    public static final String COL_NAME = "name";
    public static final String COL_PHONE = "phone";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_DATE_TEXT = "date_text";
    public static final String COL_LOCATION = "location";
    public static final String COL_CATEGORY = "category";
    public static final String COL_IMAGE_URI = "image_uri";
    public static final String COL_TIMESTAMP = "timestamp";
    public static final String COL_LATITUDE = "latitude";
    public static final String COL_LONGITUDE = "longitude";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_ITEMS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_POST_TYPE + " TEXT NOT NULL, "
                + COL_NAME + " TEXT NOT NULL, "
                + COL_PHONE + " TEXT NOT NULL, "
                + COL_DESCRIPTION + " TEXT NOT NULL, "
                + COL_DATE_TEXT + " TEXT NOT NULL, "
                + COL_LOCATION + " TEXT NOT NULL, "
                + COL_CATEGORY + " TEXT NOT NULL, "
                + COL_IMAGE_URI + " TEXT NOT NULL, "
                + COL_TIMESTAMP + " TEXT NOT NULL, "
                + COL_LATITUDE + " REAL NOT NULL DEFAULT 0, "
                + COL_LONGITUDE + " REAL NOT NULL DEFAULT 0"
                + ")";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            addColumnIfMissing(db, COL_LATITUDE, "REAL NOT NULL DEFAULT 0");
            addColumnIfMissing(db, COL_LONGITUDE, "REAL NOT NULL DEFAULT 0");
        }
    }

    private void addColumnIfMissing(SQLiteDatabase db, String columnName, String columnDefinition) {
        try {
            db.execSQL("ALTER TABLE " + TABLE_ITEMS + " ADD COLUMN " + columnName + " " + columnDefinition);
        } catch (Exception ignored) {
            // Column already exists. Nothing else is needed.
        }
    }

    public long insertItem(LostFoundItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_POST_TYPE, item.getPostType());
        values.put(COL_NAME, item.getName());
        values.put(COL_PHONE, item.getPhone());
        values.put(COL_DESCRIPTION, item.getDescription());
        values.put(COL_DATE_TEXT, item.getDateText());
        values.put(COL_LOCATION, item.getLocation());
        values.put(COL_CATEGORY, item.getCategory());
        values.put(COL_IMAGE_URI, item.getImageUri());
        values.put(COL_TIMESTAMP, item.getTimestamp());
        values.put(COL_LATITUDE, item.getLatitude());
        values.put(COL_LONGITUDE, item.getLongitude());
        return db.insert(TABLE_ITEMS, null, values);
    }

    public List<LostFoundItem> getAllItems() {
        return searchItems("", "All Categories");
    }

    public List<LostFoundItem> searchItems(String query, String category) {
        List<LostFoundItem> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        StringBuilder sql = new StringBuilder("SELECT * FROM " + TABLE_ITEMS + " WHERE 1=1");
        List<String> args = new ArrayList<>();

        if (query != null && !query.trim().isEmpty()) {
            sql.append(" AND (")
                    .append(COL_NAME).append(" LIKE ? OR ")
                    .append(COL_DESCRIPTION).append(" LIKE ? OR ")
                    .append(COL_LOCATION).append(" LIKE ?)");
            String like = "%" + query.trim() + "%";
            args.add(like);
            args.add(like);
            args.add(like);
        }

        if (category != null && !category.equals("All Categories")) {
            sql.append(" AND ").append(COL_CATEGORY).append(" = ?");
            args.add(category);
        }

        sql.append(" ORDER BY ").append(COL_ID).append(" DESC");

        Cursor cursor = db.rawQuery(sql.toString(), args.toArray(new String[0]));
        if (cursor.moveToFirst()) {
            do {
                items.add(itemFromCursor(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return items;
    }

    public List<LostFoundItem> getItemsWithinRadius(double userLatitude, double userLongitude, double radiusKm) {
        List<LostFoundItem> nearbyItems = new ArrayList<>();
        List<LostFoundItem> allItems = getAllItems();

        for (LostFoundItem item : allItems) {
            if (!item.hasValidCoordinates()) {
                continue;
            }
            float[] results = new float[1];
            Location.distanceBetween(
                    userLatitude,
                    userLongitude,
                    item.getLatitude(),
                    item.getLongitude(),
                    results
            );
            double distanceKm = results[0] / 1000.0;
            if (distanceKm <= radiusKm) {
                nearbyItems.add(item);
            }
        }

        return nearbyItems;
    }

    public LostFoundItem getItemById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_ITEMS, null, COL_ID + "=?", new String[]{String.valueOf(id)},
                null, null, null);
        LostFoundItem item = null;
        if (cursor.moveToFirst()) {
            item = itemFromCursor(cursor);
        }
        cursor.close();
        return item;
    }

    private LostFoundItem itemFromCursor(Cursor cursor) {
        LostFoundItem item = new LostFoundItem();
        item.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)));
        item.setPostType(cursor.getString(cursor.getColumnIndexOrThrow(COL_POST_TYPE)));
        item.setName(cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)));
        item.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(COL_PHONE)));
        item.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPTION)));
        item.setDateText(cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE_TEXT)));
        item.setLocation(cursor.getString(cursor.getColumnIndexOrThrow(COL_LOCATION)));
        item.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORY)));
        item.setImageUri(cursor.getString(cursor.getColumnIndexOrThrow(COL_IMAGE_URI)));
        item.setTimestamp(cursor.getString(cursor.getColumnIndexOrThrow(COL_TIMESTAMP)));
        item.setLatitude(getDoubleOrDefault(cursor, COL_LATITUDE));
        item.setLongitude(getDoubleOrDefault(cursor, COL_LONGITUDE));
        return item;
    }

    private double getDoubleOrDefault(Cursor cursor, String columnName) {
        int index = cursor.getColumnIndex(columnName);
        if (index == -1 || cursor.isNull(index)) {
            return 0.0;
        }
        return cursor.getDouble(index);
    }

    public int deleteItem(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_ITEMS, COL_ID + "=?", new String[]{String.valueOf(id)});
    }
}
