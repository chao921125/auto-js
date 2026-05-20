package net.cc.stardust.autojs.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SQLite数据库封装
 * Auto.js Pro新特性: 新增数据库模块
 * 
 * 功能:
 * - 本地SQLite数据库操作
 * - JSON格式数据读写
 * - 事务支持
 * - 预处理语句防SQL注入
 */
public class AutoJsDatabase {
    
    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private String databaseName;
    private int version;
    
    // 缓存查询结果
    private ConcurrentHashMap<String, List<Map<String, Object>>> queryCache;
    
    public AutoJsDatabase(Context context, String databaseName, int version) {
        this.databaseName = databaseName;
        this.version = version;
        this.queryCache = new ConcurrentHashMap<>();
        
        dbHelper = new DatabaseHelper(context);
        openDatabase();
    }
    
    /**
     * 打开或创建数据库
     */
    private void openDatabase() {
        try {
            db = dbHelper.openDatabase(databaseName);
            if (db != null && db.isOpen()) {
                System.out.println("数据库打开成功: " + databaseName);
            }
        } catch (Exception e) {
            System.err.println("数据库打开失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 执行SQL语句(INSERT, UPDATE, DELETE等)
     */
    public int execute(String sql) {
        try {
            db.execSQL(sql);
            return db.getVersion();
        } catch (Exception e) {
            System.err.println("执行SQL失败: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }
    
    /**
     * 执行带参数的SQL语句
     */
    public int execute(String sql, Object[] args) {
        try {
            db.execSQL(sql, args);
            return db.getLastCauseStackTrace()[0]; // 返回影响行数
        } catch (Exception e) {
            System.err.println("执行参数化SQL失败: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }
    
    /**
     * 查询数据
     */
    public Cursor query(String table, String[] columns, String selection, 
                       String[] selectionArgs, String groupBy, 
                       String having, String orderBy) {
        try {
            return db.query(table, columns, selection, selectionArgs, 
                           groupBy, having, orderBy);
        } catch (Exception e) {
            System.err.println("查询失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 简化查询 - 返回JSON数组
     */
    public JSONArray queryToJsonArray(String table, String[] columns, 
                                     String whereClause) {
        JSONArray result = new JSONArray();
        Cursor cursor = null;
        
        try {
            cursor = db.query(table, columns, whereClause, null, null, null, null);
            
            while (cursor != null && cursor.moveToNext()) {
                JSONObject row = new JSONObject();
                int columnCount = cursor.getColumnCount();
                
                for (int i = 0; i < columnCount; i++) {
                    String columnName = cursor.getColumnName(i);
                    int columnIndex = cursor.getColumnIndex(columnName);
                    
                    if (columnIndex != -1) {
                        int type = cursor.getType(columnIndex);
                        switch (type) {
                            case Cursor.FIELD_TYPE_NULL:
                                row.putNull(columnName);
                                break;
                            case Cursor.FIELD_TYPE_INTEGER:
                                row.put(columnName, cursor.getLong(columnIndex));
                                break;
                            case Cursor.FIELD_TYPE_FLOAT:
                                row.put(columnName, cursor.getDouble(columnIndex));
                                break;
                            case Cursor.FIELD_TYPE_STRING:
                            default:
                                row.put(columnName, cursor.getString(columnIndex));
                                break;
                        }
                    }
                }
                
                result.put(row);
            }
            
            // 清除缓存
            queryCache.remove(table);
            
        } catch (Exception e) {
            System.err.println("查询转换为JSON失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        return result;
    }
    
    /**
     * 插入数据
     */
    public long insert(String table, String nullColumnHack, ContentValues values) {
        try {
            return db.insert(table, nullColumnHack, values.toContentValues());
        } catch (Exception e) {
            System.err.println("插入数据失败: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }
    
    /**
     * 更新数据
     */
    public int update(String table, ContentValues values, String whereClause, 
                     String[] whereArgs) {
        try {
            return db.update(table, values.toContentValues(), whereClause, whereArgs);
        } catch (Exception e) {
            System.err.println("更新数据失败: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * 删除数据
     */
    public int delete(String table, String whereClause, String[] whereArgs) {
        try {
            return db.delete(table, whereClause, whereArgs);
        } catch (Exception e) {
            System.err.println("删除数据失败: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * 开始事务
     */
    public void beginTransaction() {
        try {
            db.beginTransaction();
        } catch (Exception e) {
            System.err.println("开始事务失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 提交事务
     */
    public void commit() {
        try {
            if (db != null && db.inTransaction()) {
                db.setTransactionSuccessful();
                db.endTransaction();
            }
        } catch (Exception e) {
            System.err.println("提交事务失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 回滚事务
     */
    public void rollback() {
        try {
            if (db != null && db.inTransaction()) {
                db.endTransaction();
            }
        } catch (Exception e) {
            System.err.println("回滚事务失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 创建表
     */
    public boolean createTable(String tableName, String createTableSql) {
        try {
            db.execSQL(createTableSql);
            return true;
        } catch (Exception e) {
            System.err.println("创建表失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 删除表
     */
    public boolean dropTable(String tableName) {
        try {
            db.execSQL("DROP TABLE IF EXISTS " + tableName);
            return true;
        } catch (Exception e) {
            System.err.println("删除表失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 检查数据库是否打开
     */
    public boolean isOpen() {
        return db != null && db.isOpen();
    }
    
    /**
     * 关闭数据库
     */
    public void close() {
        try {
            if (db != null && db.isOpen()) {
                db.close();
            }
            if (dbHelper != null) {
                dbHelper.close();
            }
        } catch (Exception e) {
            System.err.println("关闭数据库失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 获取记录数量
     */
    public int getCount(String table, String whereClause) {
        try {
            Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + table + 
                (whereClause != null ? " WHERE " + whereClause : ""), 
                null
            );
            
            int count = 0;
            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);
                cursor.close();
            }
            
            return count;
        } catch (Exception e) {
            System.err.println("获取记录数失败: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * 预处理语句包装类
     */
    public static class PreparedStatement {
        private SQLiteDatabase db;
        private String sql;
        
        public PreparedStatement(SQLiteDatabase db, String sql) {
            this.db = db;
            this.sql = sql;
        }
        
        /**
         * 执行预处理语句
         */
        public int execute(Object... args) {
            return db.execSQL(sql, args);
        }
        
        /**
         * 查询
         */
        public Cursor query(Object... args) {
            return db.rawQuery(sql, toStringArray(args));
        }
    }
    
    /**
     * 创建预处理语句(防SQL注入)
     */
    public PreparedStatement prepareStatement(String sql) {
        return new PreparedStatement(db, sql);
    }
    
    // 辅助方法
    private String[] toStringArray(Object[] args) {
        List<String> list = new ArrayList<>();
        if (args != null) {
            for (Object arg : args) {
                list.add(arg != null ? arg.toString() : null);
            }
        }
        return list.toArray(new String[0]);
    }
    
    /**
     * ContentValues包装器
     */
    public static class ContentValues {
        private android.content.ContentValues values;
        
        public ContentValues() {
            values = new android.content.ContentValues();
        }
        
        public void put(String key, String value) {
            values.put(key, value);
        }
        
        public void put(String key, int value) {
            values.put(key, value);
        }
        
        public void put(String key, long value) {
            values.put(key, value);
        }
        
        public void put(String key, double value) {
            values.put(key, value);
        }
        
        public void put(String key, Boolean value) {
            values.put(key, value);
        }
        
        public android.content.ContentValues toContentValues() {
            return values;
        }
    }
    
    /**
     * 数据库帮助类
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {
        
        private static final String DB_PATH_SUFFIX = "/databases";
        private Context context;
        
        public DatabaseHelper(Context context) {
            super(context, ".helper", null, 1);
            this.context = context;
        }
        
        @Override
        public void onCreate(SQLiteDatabase db) {
            // 创建时初始化
        }
        
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // 升级处理
        }
        
        /**
         * 打开数据库
         */
        public SQLiteDatabase openDatabase(String name) {
            return context.getApplicationContext().openOrCreateDatabase(name, 
                Context.MODE_PRIVATE, null);
        }
    }
}
