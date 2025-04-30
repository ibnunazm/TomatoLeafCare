package com.example.tomatoleafcare.helper

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.tomatoleafcare.model.History

class HistoryDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "history.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE history (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                diseaseName TEXT,
                date TEXT,
                imagePath TEXT
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS history")
        onCreate(db)
    }

    fun insertHistory(item: History): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("diseaseName", item.diseaseName)
            put("date", item.date)
            put("imagePath", item.imagePath)
        }
        return db.insert("history", null, values)
    }

    fun getAllHistory(): List<History> {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM history ORDER BY id DESC", null)
        val items = mutableListOf<History>()

        while (cursor.moveToNext()) {
            items.add(
                History(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                    diseaseName = cursor.getString(cursor.getColumnIndexOrThrow("diseaseName")),
                    date = cursor.getString(cursor.getColumnIndexOrThrow("date")),
                    imagePath = cursor.getString(cursor.getColumnIndexOrThrow("imagePath"))
                )
            )
        }

        cursor.close()
        return items
    }

    fun deleteHistory(id: Long): Boolean {
        val db = this.writableDatabase
        val result = db.delete("history", "id=?", arrayOf(id.toString()))
        db.close()
        return result > 0
    }

}
