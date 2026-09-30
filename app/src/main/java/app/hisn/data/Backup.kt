package app.hisn.data

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import org.json.JSONArray
import org.json.JSONObject

/**
 * نسخة احتياطية محلية بصيغة JSON.
 *
 * ملف واحد يحمله المستخدم بنفسه إلى حيث يشاء — لا خادم ولا حساب. التصدير عام
 * على مستوى الجداول، فأي جدول يُضاف لاحقًا يدخل تلقائيًا ما دام مسجَّلًا في
 * [Db.BACKUP_TABLES]. الاستعادة تستبدل كل شيء، وتتجاهل الجداول غير المعروفة.
 */
object Backup {

    private const val FORMAT = 1

    fun export(db: SQLiteDatabase, prefs: Map<String, *>, versionName: String): String {
        val root = JSONObject()
        root.put("app", "hisn")
        root.put("format", FORMAT)
        root.put("version", versionName)
        root.put("exportedAt", System.currentTimeMillis())

        val tables = JSONObject()
        Db.BACKUP_TABLES.forEach { t ->
            val rows = JSONArray()
            db.rawQuery("SELECT * FROM $t", null).use { c ->
                while (c.moveToNext()) rows.put(c.toJson())
            }
            tables.put(t, rows)
        }
        root.put("tables", tables)

        val p = JSONObject()
        prefs.forEach { (k, v) -> if (v != null) p.put(k, v) }
        root.put("prefs", p)
        return root.toString(2)
    }

    /** يعيد إعدادات النسخة لتُستعاد في [Prefs.restore]. يرمي استثناءً إن كان الملف غير صالح. */
    fun import(db: SQLiteDatabase, json: String): Map<String, Any?> {
        val root = JSONObject(json)
        require(root.optString("app") == "hisn") { "ليس ملف نسخة حِصن" }
        val tables = root.getJSONObject("tables")

        db.beginTransaction()
        try {
            Db.BACKUP_TABLES.forEach { t -> db.delete(t, null, null) }
            Db.BACKUP_TABLES.forEach { t ->
                val rows = tables.optJSONArray(t) ?: return@forEach
                for (i in 0 until rows.length()) {
                    val row = rows.getJSONObject(i)
                    val cv = ContentValues()
                    row.keys().forEach { k ->
                        when (val v = row.get(k)) {
                            JSONObject.NULL -> cv.putNull(k)
                            is Int -> cv.put(k, v)
                            is Long -> cv.put(k, v)
                            is Double -> cv.put(k, v)
                            is Boolean -> cv.put(k, if (v) 1 else 0)
                            else -> cv.put(k, v.toString())
                        }
                    }
                    db.insertWithOnConflict(t, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
                }
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }

        val prefs = mutableMapOf<String, Any?>()
        root.optJSONObject("prefs")?.let { p ->
            p.keys().forEach { k -> prefs[k] = p.get(k) }
        }
        return prefs
    }

    private fun Cursor.toJson(): JSONObject {
        val o = JSONObject()
        for (i in 0 until columnCount) {
            val name = getColumnName(i)
            when (getType(i)) {
                Cursor.FIELD_TYPE_NULL -> o.put(name, JSONObject.NULL)
                Cursor.FIELD_TYPE_INTEGER -> o.put(name, getLong(i))
                Cursor.FIELD_TYPE_FLOAT -> o.put(name, getDouble(i))
                else -> o.put(name, getString(i))
            }
        }
        return o
    }
}
