package com.example.pm1e1293512;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ContactDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "contacts.db";
    private static final int DATABASE_VERSION = 1;

    // Nombre de la tabla y columnas
    public static final String TABLE_NAME = "contacts";
    public static final String COLUMN_NAME_NAME = "name";
    public static final String COLUMN_NAME_PHONE = "phone";

    // Sentencia SQL para crear la tabla de contactos
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_NAME_NAME + " TEXT PRIMARY KEY," +
                    COLUMN_NAME_PHONE + " TEXT)";

    public ContactDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // No se necesita implementar en esta etapa
    }

    // Método para eliminar un contacto por nombre y teléfono
    public void deleteContact(String name, String phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME,
                COLUMN_NAME_NAME + " = ? AND " + COLUMN_NAME_PHONE + " = ?",
                new String[]{name, phone});
        db.close();
    }

    // Método para obtener todos los contactos
    public Cursor getAllContacts() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null);
    }

    // Otros métodos para insertar, actualizar, consultar según sea necesario
    public void addContact(String name, String phone, String note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_NAME, name);
        values.put(COLUMN_NAME_PHONE, phone);
        // Agregar nota si es necesario
        // values.put(COLUMN_NAME_NOTE, note); // Asegúrate de tener la columna de nota en tu base de datos
        long newRowId = db.insert(TABLE_NAME, null, values);
        db.close();
    }
}
