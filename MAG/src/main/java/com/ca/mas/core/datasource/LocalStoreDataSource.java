/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.datasource;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.util.Pair;

import com.ca.mas.core.storage.StorageException;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LocalStoreDataSource implements DataSource<LocalStoreKey, LocalStoreEntity> {


    public static final String TAG = "LocalStoreDataSource";

    private Context context;

    /**
     * The reference to DB helper class.
     */
    private LocalStorageDbHelper mLocalStorageDbHelper = null;

    public LocalStoreDataSource(Context context, JSONObject param, DataConverter converter) {
        this.context = context.getApplicationContext();
        try {
            mLocalStorageDbHelper = new LocalStorageDbHelper(this.context);
            mLocalStorageDbHelper.getDatabaseHandle();
        } catch (Exception e) {
            throw new DataSourceException(e);
        }
    }

    @Override
    public void put(@NonNull LocalStoreKey key, @NonNull LocalStoreEntity localStoreItem) {

        SQLiteDatabase myDatabase = null;

        try {
            myDatabase = mLocalStorageDbHelper.getDatabaseHandle();
            ContentValues values = new ContentValues();
            values.put(LocalStorageContract.LocalStorageEntry.COLUMN_KEY, key.getKey());
            if (key.getCreatedBy() == null) {
                values.putNull(LocalStorageContract.LocalStorageEntry.COLUMN_CREATED_BY);
            } else {
                values.put(LocalStorageContract.LocalStorageEntry.COLUMN_CREATED_BY, key.getCreatedBy());
            }
            values.put(LocalStorageContract.LocalStorageEntry.COLUMN_SEGMENT, key.getSegment());
            values.put(LocalStorageContract.LocalStorageEntry.COLUMN_VALUE, localStoreItem.getData());
            values.put(LocalStorageContract.LocalStorageEntry.COLUMN_TYPE, localStoreItem.getType());
            values.put(LocalStorageContract.LocalStorageEntry.COLUMN_LAST_UPDATED_DATE, new Date().getTime());

            long rowID;
            rowID = myDatabase.insertWithOnConflict(LocalStorageContract.LocalStorageEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            if (rowID == -1) {
                throw new StorageException(StorageException.OPERATION_FAILED);
            }

        } catch (Exception e) {
            throw new DataSourceException("Error in put() ", e);
        }

    }

    @Override
    public void put(LocalStoreKey key, LocalStoreEntity localStoreItem, DataSourceCallback dataSourceCallback) {
        throw new DataSourceException("Not Implemented");
    }

    @Override
    public LocalStoreEntity get(@NonNull LocalStoreKey key) {

        SQLiteDatabase myDatabase = null;
        try {
            myDatabase = mLocalStorageDbHelper.getDatabaseHandle();
            String[] columns = new String[]{LocalStorageContract.LocalStorageEntry.COLUMN_VALUE, LocalStorageContract.LocalStorageEntry.COLUMN_TYPE};
            Pair<String, String[]> selection = getSelection(key);

            Cursor resultCursor = myDatabase.query(LocalStorageContract.LocalStorageEntry.TABLE_NAME, columns, selection.first, selection.second,
                    null, null, null, null);
            if (resultCursor.getCount() == 0) {
                return null;
            } else {
                resultCursor.moveToFirst();
                byte[] value = resultCursor.getBlob(resultCursor.getColumnIndexOrThrow(LocalStorageContract.LocalStorageEntry.COLUMN_VALUE));
                String type = resultCursor.getString(resultCursor.getColumnIndexOrThrow(LocalStorageContract.LocalStorageEntry.COLUMN_TYPE));
                resultCursor.close();
                return new LocalStoreEntity(type, value);
            }

        } catch (Exception e) {
            throw new DataSourceException("Error in get() ", e);
        }
    }

    @Override
    public void get(LocalStoreKey s, DataSourceCallback dataSourceCallback) {
        throw new DataSourceException("Not Implemented");
    }

    @Override
    public void remove(@NonNull LocalStoreKey key) {
        SQLiteDatabase myDatabase = null;
        try {
            get(key);//will throw exception if READ_DATA_NOT_FOUND
            myDatabase = mLocalStorageDbHelper.getDatabaseHandle();
            Pair<String, String[]> selection = getSelection(key);
            int val = myDatabase.delete(LocalStorageContract.LocalStorageEntry.TABLE_NAME, selection.first, selection.second);
            if (val < 0) {
                throw new StorageException(StorageException.OPERATION_FAILED);
            }
        } catch (Exception e) {
            throw new DataSourceException("Error in remove() ", e);
        }
    }

    @Override
    public void remove(LocalStoreKey s, DataSourceCallback dataSourceCallback) {
        throw new DataSourceException("Not Implemented");
    }

    @Override
    public void removeAll(Object filter) {
        SQLiteDatabase myDatabase = null;
        try {
            myDatabase = mLocalStorageDbHelper.getDatabaseHandle();
            Pair<String, String[]> selection = getSelection((LocalStoreKey) filter);
            int val = myDatabase.delete(LocalStorageContract.LocalStorageEntry.TABLE_NAME, selection.first, selection.second);
            if (val < 0) {
                throw new StorageException(StorageException.OPERATION_FAILED);
            }
        } catch (Exception e) {
            throw new DataSourceException("Error in removeAll() ", e);
        }
    }

    @Override
    public void removeAll(Object filter, DataSourceCallback dataSourceCallback) {
        throw new DataSourceException("Not Implemented");
    }

    @Override
    public List<LocalStoreKey> getKeys(Object filter) {
        SQLiteDatabase myDatabase = null;
        List<LocalStoreKey> allKeys = new ArrayList<>();
        try {
            myDatabase = mLocalStorageDbHelper.getWritableDatabase();
            String[] columns = new String[]{LocalStorageContract.LocalStorageEntry.COLUMN_KEY,
                    LocalStorageContract.LocalStorageEntry.COLUMN_SEGMENT,
                    LocalStorageContract.LocalStorageEntry.COLUMN_CREATED_BY};
            Pair<String, String[]> selection = getSelection((LocalStoreKey) filter);
            Cursor resultCursor = myDatabase.query(LocalStorageContract.LocalStorageEntry.TABLE_NAME,
                    columns, selection.first, selection.second, null, null, null);
            if (resultCursor.getCount() != 0) {
                resultCursor.moveToFirst();
                while (resultCursor.isAfterLast() == false) {
                    allKeys.add(new LocalStoreKey(
                            resultCursor.getString(resultCursor.getColumnIndexOrThrow(LocalStorageContract.LocalStorageEntry.COLUMN_KEY)),
                            resultCursor.getInt(resultCursor.getColumnIndexOrThrow(LocalStorageContract.LocalStorageEntry.COLUMN_SEGMENT)),
                            resultCursor.getString(resultCursor.getColumnIndexOrThrow(LocalStorageContract.LocalStorageEntry.COLUMN_CREATED_BY))
                            ));

                    resultCursor.moveToNext();
                }
                resultCursor.close();
            }
            return allKeys;

        } catch (Exception e) {
            throw new DataSourceException("error getKeys()", e);
        }
    }

    private Pair<String, String[]> getSelection(LocalStoreKey key) {
        StringBuilder selection = new StringBuilder();
        List<String> selectionArgs = new ArrayList<>();
        if (key.getKey() != null) {
            selection.append(LocalStorageContract.LocalStorageEntry.COLUMN_KEY);
            selection.append(" =? ");
            selectionArgs.add(key.getKey());
        }
        if (selection.length() != 0 && key.getSegment() != null) {
            selection.append(" AND ");
        }
        if (key.getSegment() != null) {
            selection.append(LocalStorageContract.LocalStorageEntry.COLUMN_SEGMENT);
            selection.append(" =? ");
            selectionArgs.add(Integer.toString(key.getSegment()));
        }
        if (selection.length() != 0 && key.getCreatedBy() != null) {
            selection.append(" AND ");
        }
        if (key.getCreatedBy() != null) {
            selection.append(LocalStorageContract.LocalStorageEntry.COLUMN_CREATED_BY);
            selection.append(" =? ");
            selectionArgs.add(key.getCreatedBy());
        }
        return new Pair<>(selection.toString(), selectionArgs.toArray(new String[selectionArgs.size()]));

    }

    @Override
    public void getKeys(Object filter, DataSourceCallback dataSourceCallback) {

    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void unlock() {
        //nothing to do
    }

    /**
     * Utility Functions
     */

    /**
     * SQLite Database related classes
     */
    private final class LocalStorageContract {
        public LocalStorageContract() {
        }

        /* Inner class that defines the table contents */
        public abstract class LocalStorageEntry implements BaseColumns {
            public static final String TABLE_NAME = "LocalStore";
            public static final String COLUMN_KEY = "key";
            public static final String COLUMN_VALUE = "value";
            public static final String COLUMN_TYPE = "content_type";
            public static final String COLUMN_CREATED_BY = "created_by";
            public static final String COLUMN_CREATED_DATE = "created_date";
            public static final String COLUMN_LAST_UPDATED_DATE = "last_updated_date";
            public static final String COLUMN_SEGMENT = "segment";
        }
    }

    private class LocalStorageDbHelper extends SQLiteOpenHelper {

        public static final int DATABASE_VERSION = 2;
        private static final String DATABASE_NAME = "LS.db";
        private static final String TEXT_TYPE = " TEXT";
        private static final String INT_TYPE = " INTEGER";
        private static final String BLOB_TYPE = " BLOB";
        private static final String DATETIME_TYPE = " DATETIME";
        private static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + LocalStorageContract.LocalStorageEntry.TABLE_NAME + " (" +
                        LocalStorageContract.LocalStorageEntry.COLUMN_KEY + TEXT_TYPE + " NOT NULL," +
                        LocalStorageContract.LocalStorageEntry.COLUMN_VALUE + BLOB_TYPE + " NOT NULL," +
                        LocalStorageContract.LocalStorageEntry.COLUMN_TYPE + TEXT_TYPE + " NOT NULL," +
                        LocalStorageContract.LocalStorageEntry.COLUMN_SEGMENT + INT_TYPE + " NOT NULL," +
                        LocalStorageContract.LocalStorageEntry.COLUMN_CREATED_BY + TEXT_TYPE + " NOT NULL," +
                        LocalStorageContract.LocalStorageEntry.COLUMN_CREATED_DATE + DATETIME_TYPE + "  DEFAULT CURRENT_TIMESTAMP, " +
                        LocalStorageContract.LocalStorageEntry.COLUMN_LAST_UPDATED_DATE + DATETIME_TYPE + "  DEFAULT CURRENT_TIMESTAMP, " +
                        "PRIMARY KEY (" + LocalStorageContract.LocalStorageEntry.COLUMN_KEY + "," +
                        LocalStorageContract.LocalStorageEntry.COLUMN_SEGMENT + "," +
                        LocalStorageContract.LocalStorageEntry.COLUMN_CREATED_BY + ")" +
                        " )";

        private final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + LocalStorageContract.LocalStorageEntry.TABLE_NAME;

        public LocalStorageDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ENTRIES);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int i, int i1) {
            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);
        }

        public SQLiteDatabase getDatabaseHandle() throws SQLiteException {
            return getWritableDatabase();

        }
    }


}
