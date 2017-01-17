package com.alan.washer.washercleaner.model.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.alan.washer.washercleaner.model.Service;
import com.alan.washer.washercleaner.model.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DataBase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Gilton.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TEXT_TYPE = " TEXT";
    private static final String DECIMAL_TYPE = " DECIMAL(1,1)";
    private static final String PRIMARY_KEY = " INTEGER PRIMARY KEY";
    private static final String COMMA_SEP = ",";
    private static final String DESC = " DESC ";
    private static final String AND = " AND ";
    private static final String SQL_CREATE_ENTRIES_FOR_USER =
            "CREATE TABLE " + UserEntry.TABLE_NAME + " (" +
                    UserEntry.ID + PRIMARY_KEY + COMMA_SEP +
                    UserEntry.NAME + TEXT_TYPE + COMMA_SEP +
                    UserEntry.LAST_NAME + TEXT_TYPE + COMMA_SEP +
                    UserEntry.MAIL + TEXT_TYPE + COMMA_SEP +
                    UserEntry.PHONE + TEXT_TYPE + COMMA_SEP +
                    UserEntry.IMAGE + TEXT_TYPE + COMMA_SEP +
                    UserEntry.RATING + DECIMAL_TYPE +
                    " )";
    private static final String SQL_CREATE_ENTRIES_FOR_SERVICE =
            "CREATE TABLE " + ServiceEntry.TABLE_NAME + " (" +
                    ServiceEntry.ID + PRIMARY_KEY + COMMA_SEP +
                    ServiceEntry.CAR + TEXT_TYPE + COMMA_SEP +
                    ServiceEntry.SERVICE + TEXT_TYPE + COMMA_SEP +
                    ServiceEntry.PRICE + TEXT_TYPE + COMMA_SEP +
                    ServiceEntry.DESCRIPTION + TEXT_TYPE + COMMA_SEP +
                    ServiceEntry.STARTED_DATE + TEXT_TYPE + COMMA_SEP +
                    ServiceEntry.LATITUD + TEXT_TYPE + COMMA_SEP +
                    ServiceEntry.LONGITUD + TEXT_TYPE + COMMA_SEP +
                    ServiceEntry.STATUS + TEXT_TYPE + COMMA_SEP +
                    ServiceEntry.FINAL_TIME + TEXT_TYPE + COMMA_SEP +
                    ServiceEntry.CLIENT_NAME + TEXT_TYPE + COMMA_SEP +
                    ServiceEntry.CLIENT_CEL + TEXT_TYPE + COMMA_SEP +
                    ServiceEntry.PLATES + TEXT_TYPE + COMMA_SEP +
                    ServiceEntry.BRAND + TEXT_TYPE + COMMA_SEP +
                    ServiceEntry.COLOR + TEXT_TYPE + COMMA_SEP +
                    ServiceEntry.ADDRESS + TEXT_TYPE + COMMA_SEP +
                    ServiceEntry.ESTIMATED_TIME + TEXT_TYPE +
                    " )";

    private static final String SQL_DELETE_ENTRIES_USER =
            "DROP TABLE IF EXISTS " + UserEntry.TABLE_NAME;
    private static final String SQL_DELETE_ENTRIES_SERVICE =
            "DROP TABLE IF EXISTS " + ServiceEntry.TABLE_NAME;

    public DataBase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES_FOR_USER);
        db.execSQL(SQL_CREATE_ENTRIES_FOR_SERVICE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES_USER);
        db.execSQL(SQL_DELETE_ENTRIES_SERVICE);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void deleteTableUser() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(UserEntry.TABLE_NAME, null, null);
    }

    public void saveUser(User user) {
        SQLiteDatabase db = getWritableDatabase();
        deleteTableUser();
        ContentValues row = new ContentValues();
        row.put(UserEntry.ID, user.id);
        row.put(UserEntry.NAME, user.name);
        row.put(UserEntry.LAST_NAME, user.lastName);
        row.put(UserEntry.MAIL, user.email);
        row.put(UserEntry.PHONE, user.phone);
        row.put(UserEntry.RATING, user.rating);
        row.put(UserEntry.IMAGE, user.imagePath);
        db.insert(UserEntry.TABLE_NAME, null, row);
        db.close();
    }

    public User readUser() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = configureQuery(db, UserEntry.TABLE_NAME, null, null, null);

        cursor.moveToFirst();
        User user = new User();
        user.id = cursor.getString(cursor.getColumnIndexOrThrow(UserEntry.ID));
        user.name = cursor.getString(cursor.getColumnIndexOrThrow(UserEntry.NAME));
        user.lastName = cursor.getString(cursor.getColumnIndexOrThrow(UserEntry.LAST_NAME));
        user.email = cursor.getString(cursor.getColumnIndexOrThrow(UserEntry.MAIL));
        user.phone = cursor.getString(cursor.getColumnIndexOrThrow(UserEntry.PHONE));
        user.imagePath = cursor.getString(cursor.getColumnIndexOrThrow(UserEntry.IMAGE));
        user.rating = cursor.getFloat(cursor.getColumnIndexOrThrow(UserEntry.RATING));
        db.close();
        cursor.close();
        return user;
    }

    private Cursor configureQuery(SQLiteDatabase db, String table, String whereClause, String[] whereArgs, String sortOrder) {
        return db.query(
                table,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                sortOrder
        );
    }


    public void deleteTableService() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(ServiceEntry.TABLE_NAME, null, null);
    }

    public void saveServices(List<Service> services) {
        SQLiteDatabase db = getWritableDatabase();
        deleteTableService();
        for (Service service : services) {
            ContentValues row = new ContentValues();
            row.put(ServiceEntry.ID, service.id);
            row.put(ServiceEntry.CAR, service.car);
            row.put(ServiceEntry.SERVICE, service.service);
            row.put(ServiceEntry.PRICE, service.price);
            row.put(ServiceEntry.DESCRIPTION, service.description);
            row.put(ServiceEntry.STARTED_DATE, service.startedTime);
            row.put(ServiceEntry.LATITUD, service.latitud);
            row.put(ServiceEntry.LONGITUD, service.longitud);
            row.put(ServiceEntry.STATUS, service.status);
            row.put(ServiceEntry.CLIENT_NAME, service.clientName);
            row.put(ServiceEntry.CLIENT_CEL, service.clientCel);
            row.put(ServiceEntry.ESTIMATED_TIME, service.estimatedTime);
            row.put(ServiceEntry.PLATES, service.plates);
            row.put(ServiceEntry.BRAND, service.brand);
            row.put(ServiceEntry.COLOR, service.color);
            row.put(ServiceEntry.ADDRESS, service.address);

            if (service.finalTime != null) {
                //SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
                row.put(ServiceEntry.FINAL_TIME, format.format(service.finalTime));
            }
            db.insert(ServiceEntry.TABLE_NAME, null, row);
        }
        db.close();
    }

    public List<Service> readServices() {
        SQLiteDatabase db = getReadableDatabase();
        String sortOrder = ServiceEntry.STARTED_DATE + DESC;
        Cursor cursor = configureQuery(db, ServiceEntry.TABLE_NAME, null, null, sortOrder);
        List<Service> services = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                Service service = new Service();
                service.id = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.ID));
                service.car = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.CAR));
                service.service = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.SERVICE));
                service.price = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.PRICE));
                service.description = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.DESCRIPTION));
                service.startedTime = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.STARTED_DATE));
                service.latitud = cursor.getDouble(cursor.getColumnIndexOrThrow(ServiceEntry.LATITUD));
                service.longitud = cursor.getDouble(cursor.getColumnIndexOrThrow(ServiceEntry.LONGITUD));
                service.status = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.STATUS));
                service.clientName = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.CLIENT_NAME));
                service.clientCel = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.CLIENT_CEL));
                service.estimatedTime = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.ESTIMATED_TIME));
                service.plates = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.PLATES));
                service.brand = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.BRAND));
                service.color = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.COLOR));
                service.address = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.ADDRESS));
                try {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
                    service.finalTime = format.parse(cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.FINAL_TIME)));
                } catch (Exception e) {
                    service.finalTime = null;
                }
                services.add(service);
            } while (cursor.moveToNext());
        }
        db.close();
        cursor.close();
        return services;
    }

    public Service getActiveService() {
        SQLiteDatabase db = getReadableDatabase();
        String whereClause = ServiceEntry.STATUS + " != ?" + AND + ServiceEntry.STATUS + " != ?";
        String[] whereArgs = {
                "Canceled",
                "Finished"
        };
        String sortOrder = ServiceEntry.STARTED_DATE + DESC;
        Cursor cursor = configureQuery(db, ServiceEntry.TABLE_NAME, whereClause, whereArgs, sortOrder);
        Service service = null;
        if (cursor.moveToFirst()) {
            do {
                String status = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.STATUS));
                if (status.equals("Finished") || status.equals("Canceled"))
                    continue;

                service = new Service();
                service.id = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.ID));
                service.car = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.CAR));
                service.service = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.SERVICE));
                service.price = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.PRICE));
                service.description = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.DESCRIPTION));
                service.startedTime = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.STARTED_DATE));
                service.latitud = cursor.getDouble(cursor.getColumnIndexOrThrow(ServiceEntry.LATITUD));
                service.longitud = cursor.getDouble(cursor.getColumnIndexOrThrow(ServiceEntry.LONGITUD));
                service.status = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.STATUS));
                service.clientName = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.CLIENT_NAME));
                service.clientCel = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.CLIENT_CEL));
                service.estimatedTime = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.ESTIMATED_TIME));
                service.plates = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.PLATES));
                service.brand = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.BRAND));
                service.color = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.COLOR));
                service.address = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.ADDRESS));
                try {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
                    service.finalTime = format.parse(cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.FINAL_TIME)));
                } catch (Exception e) {
                    service.finalTime = null;
                }
                break;
            } while (cursor.moveToNext());
        }
        db.close();
        cursor.close();
        return service;
    }

    public List<Service> getFinishedServices() {
        SQLiteDatabase db = getReadableDatabase();
        String whereClause = ServiceEntry.STATUS + " == ?";
        String[] whereArgs = {
                "Finished",
        };
        String sortOrder = ServiceEntry.STARTED_DATE + DESC;
        Cursor cursor = configureQuery(db, ServiceEntry.TABLE_NAME, whereClause, whereArgs, sortOrder);
        List<Service> services = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                String status = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.STATUS));
                if (!status.equals("Finished"))
                    continue;

                Service service = new Service();
                service.id = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.ID));
                service.car = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.CAR));
                service.service = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.SERVICE));
                service.price = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.PRICE));
                service.description = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.DESCRIPTION));
                service.startedTime = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.STARTED_DATE));
                service.latitud = cursor.getDouble(cursor.getColumnIndexOrThrow(ServiceEntry.LATITUD));
                service.longitud = cursor.getDouble(cursor.getColumnIndexOrThrow(ServiceEntry.LONGITUD));
                service.status = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.STATUS));
                service.clientName = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.CLIENT_NAME));
                service.clientCel = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.CLIENT_CEL));
                service.estimatedTime = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.ESTIMATED_TIME));
                service.plates = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.PLATES));
                service.brand = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.BRAND));
                service.color = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.COLOR));
                service.address = cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.ADDRESS));
                try {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
                    service.finalTime = format.parse(cursor.getString(cursor.getColumnIndexOrThrow(ServiceEntry.FINAL_TIME)));
                } catch (Exception e) {
                    service.finalTime = null;
                }
                services.add(service);
            } while (cursor.moveToNext());
        }
        db.close();
        cursor.close();
        return services;
    }


    static abstract class UserEntry implements BaseColumns {
        static final String TABLE_NAME = "User";
        static final String ID = "id";
        static final String NAME = "name";
        static final String LAST_NAME = "lastName";
        static final String MAIL = "email";
        static final String PHONE = "phone";
        static final String IMAGE = "image";
        static final String RATING = "rating";
    }

    static abstract class ServiceEntry implements BaseColumns {
        static final String TABLE_NAME = "Service";
        static final String ID = "id";
        static final String CAR = "car";
        static final String SERVICE = "service";
        static final String PRICE = "price";
        static final String DESCRIPTION = "description";
        static final String STARTED_DATE = "startedDate";
        static final String LATITUD = "latitud";
        static final String LONGITUD = "longitud";
        static final String STATUS = "status";
        static final String FINAL_TIME = "finalTime";
        static final String CLIENT_NAME = "clientName";
        static final String CLIENT_CEL = "clientCel";
        static final String ESTIMATED_TIME = "estimatedTime";
        static final String PLATES = "plates";
        static final String BRAND = "brand";
        static final String COLOR = "color";
        static final String ADDRESS = "address";
    }
}
