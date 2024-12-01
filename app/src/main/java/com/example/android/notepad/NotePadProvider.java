/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.notepad;

import com.example.android.notepad.NotePad;

import android.content.ClipDescription;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.ContentProvider.PipeDataWriter;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.LiveFolders;
import android.text.TextUtils;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

/**
 * Provides access to a database of notes. Each note has a title, the note
 * itself, a creation date and a modified data.
 */

/**
 * 提供对笔记数据库的访问。每个笔记包含一个标题，笔记内容本身，创建日期和修改日期。
 */
public class NotePadProvider extends ContentProvider implements PipeDataWriter<Cursor> {
    // 用于调试和日志记录
    private static final String TAG = "NotePadProvider";

    /**
     * 提供者使用的底层数据存储数据库
     */
    private static final String DATABASE_NAME = "note_pad.db";

    /**
     * 数据库版本
     */
    private static final int DATABASE_VERSION = 2;

    /**
     * 用于从数据库选择列的投影映射
     */
    private static HashMap<String, String> sNotesProjectionMap;

    /**
     * 用于从数据库选择列的投影映射
     */
    private static HashMap<String, String> sLiveFolderProjectionMap;

    /**
     * 标准投影，用于选择普通笔记的有趣列。
     */
    private static final String[] READ_NOTE_PROJECTION = new String[]{
            NotePad.Notes._ID,               // 投影位置 0，笔记的 ID
            NotePad.Notes.COLUMN_NAME_NOTE,  // 投影位置 1，笔记的内容
            NotePad.Notes.COLUMN_NAME_TITLE, // 投影位置 2，笔记的标题
    };
    private static final int READ_NOTE_NOTE_INDEX = 1;
    private static final int READ_NOTE_TITLE_INDEX = 2;

    /*
     * 根据传入 URI 的模式通过 Uri 匹配器选择操作的常量
     */
    // 传入的 URI 与笔记 URI 模式匹配
    private static final int NOTES = 1;

    // 传入的 URI 与笔记 ID URI 模式匹配
    private static final int NOTE_ID = 2;

    // 传入的 URI 与实时文件夹 URI 模式匹配
    private static final int LIVE_FOLDER_NOTES = 3;

    /**
     * 一个 UriMatcher 实例
     */
    private static final UriMatcher sUriMatcher;

    // 数据库助手的句柄。
    private DatabaseHelper mOpenHelper;

    /**
     * A block that instantiates and sets static objects
     */
    static {

        /*
         * 创建并初始化 URI 匹配器
         */
        // 创建一个新的实例
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        // 添加一个模式，将以 "notes" 结尾的 URI 定向到 NOTES 操作
        sUriMatcher.addURI(NotePad.AUTHORITY, "notes", NOTES);

        // 添加一个模式，将以 "notes" 加一个整数结尾的 URI 定向到笔记 ID 操作
        sUriMatcher.addURI(NotePad.AUTHORITY, "notes/#", NOTE_ID);

        // 添加一个模式，将以 live_folders/notes 结尾的 URI 定向到
        // 实时文件夹操作
        sUriMatcher.addURI(NotePad.AUTHORITY, "live_folders/notes", LIVE_FOLDER_NOTES);

        /*
         * 创建并初始化一个投影映射，返回所有列
         */

        // 创建一个新的投影映射实例。映射在给定字符串的情况下返回列名。
        // 这两者通常是相等的。
        sNotesProjectionMap = new HashMap<String, String>();

        // 将字符串 "_ID" 映射到列名 "_ID"
        sNotesProjectionMap.put(NotePad.Notes._ID, NotePad.Notes._ID);

        // 将 "title" 映射到 "title"
        sNotesProjectionMap.put(NotePad.Notes.COLUMN_NAME_TITLE, NotePad.Notes.COLUMN_NAME_TITLE);

        // 将 "note" 映射到 "note"
        sNotesProjectionMap.put(NotePad.Notes.COLUMN_NAME_NOTE, NotePad.Notes.COLUMN_NAME_NOTE);

        // 将 "created" 映射到 "created"
        sNotesProjectionMap.put(NotePad.Notes.COLUMN_NAME_CREATE_DATE,
                NotePad.Notes.COLUMN_NAME_CREATE_DATE);

        // 将 "modified" 映射到 "modified"
        sNotesProjectionMap.put(
                NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE,
                NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE);
//颜色
        sNotesProjectionMap.put(
                NotePad.Notes.COLUMN_NAME_BACK_COLOR,
                NotePad.Notes.COLUMN_NAME_BACK_COLOR);

        /*
         * 创建并初始化一个用于处理实时文件夹的投影映射
         */

        // 创建一个新的投影映射实例
        sLiveFolderProjectionMap = new HashMap<String, String>();

        // 将 "_ID" 映射到 "_ID AS _ID"，用于实时文件夹
        sLiveFolderProjectionMap.put(LiveFolders._ID, NotePad.Notes._ID + " AS " + LiveFolders._ID);

        // 将 "NAME" 映射到 "title AS NAME"
        sLiveFolderProjectionMap.put(LiveFolders.NAME, NotePad.Notes.COLUMN_NAME_TITLE + " AS " +
                LiveFolders.NAME);
    }


    /**
     * 该类用于打开、创建和升级数据库文件。为了测试目的，设置为包可见。
     */
    static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {

            // 调用父类构造函数，请求默认的光标工厂。
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        /**
         * 创建底层数据库，表名和列名来自 NotePad 类。
         */
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + NotePad.Notes.TABLE_NAME + "   ("
                    + NotePad.Notes._ID + " INTEGER PRIMARY KEY,"
                    + NotePad.Notes.COLUMN_NAME_TITLE + " TEXT,"
                    + NotePad.Notes.COLUMN_NAME_NOTE + " TEXT,"
                    + NotePad.Notes.COLUMN_NAME_CREATE_DATE + " INTEGER,"
                    + NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE + " INTEGER,"
                    + NotePad.Notes.COLUMN_NAME_BACK_COLOR + " INTEGER,"
                    + NotePad.Notes.COLUMN_NAME_CATEGORY + " TEXT" // 新增分类字段
                    + ");");
        }

        /**
         * 演示了当底层数据存储发生变化时，提供者应该如何处理。在此示例中，数据库通过销毁现有数据来进行升级。
         * 一个真正的应用程序应该在原地升级数据库。
         */
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            // 记录数据库正在升级的日志
            Log.w(TAG, "正在将数据库从版本 " + oldVersion + " 升级到 " + newVersion + "，这将销毁所有旧数据");

            // 删除表和现有数据
            db.execSQL("DROP TABLE IF EXISTS notes");

            // 使用新版本重新创建数据库
            onCreate(db);
        }
    }

    /**
     * 初始化提供者，通过创建一个新的 DatabaseHelper。onCreate() 会在 Android 响应客户端的解析器请求时自动调用。
     */
    @Override
    public boolean onCreate() {

        // 创建一个新的帮助对象。注意，数据库本身直到有人尝试访问它时才会打开，且只有在数据库不存在时才会创建。
        mOpenHelper = new DatabaseHelper(getContext());

        // 假设任何失败都将通过抛出异常报告。
        return true;
    }

    /**
     * 当客户端调用 {@link android.content.ContentResolver#query(Uri, String[], String, String[], String)} 时调用此方法。
     * 查询数据库并返回一个包含查询结果的光标。
     *
     * @return 包含查询结果的光标。如果查询没有结果或发生异常，光标存在但为空。
     * @throws IllegalArgumentException 如果传入的 URI 模式无效。
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(NotePad.Notes.TABLE_NAME);

        // 确保 projection 中包含所有需要的列
        if (projection == null) {
            projection = new String[] {
                    NotePad.Notes._ID,               // 确保 ID 列包含在内
                    NotePad.Notes.COLUMN_NAME_TITLE,  // 标题列
                    NotePad.Notes.COLUMN_NAME_NOTE,   // 内容列
                    NotePad.Notes.COLUMN_NAME_BACK_COLOR  // 背景颜色列
            };
        }

        // 根据 URI 选择不同的查询模式
        switch (sUriMatcher.match(uri)) {
            case NOTES:
                qb.setProjectionMap(sNotesProjectionMap);
                break;

            case NOTE_ID:
                qb.setProjectionMap(sNotesProjectionMap);
                qb.appendWhere(NotePad.Notes._ID + "=" + uri.getPathSegments().get(NotePad.Notes.NOTE_ID_PATH_POSITION));
                break;

            case LIVE_FOLDER_NOTES:
                qb.setProjectionMap(sLiveFolderProjectionMap);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // 添加查询条件（按内容模糊查询）
        if (!TextUtils.isEmpty(selection)) {
            String queryText = selectionArgs[0]; // 获取用户输入的查询文本
            selection = NotePad.Notes.COLUMN_NAME_TITLE + " LIKE ? OR " + NotePad.Notes.COLUMN_NAME_NOTE + " LIKE ?";
            selectionArgs = new String[] { "%" + queryText + "%", "%" + queryText + "%" };
        }

        // 设置排序顺序
        String orderBy = TextUtils.isEmpty(sortOrder) ? NotePad.Notes.DEFAULT_SORT_ORDER : sortOrder;

        // 执行查询
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);

        // 设置通知 URI
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }




    /**
     * 当客户端调用 {@link android.content.ContentResolver#getType(Uri)} 时调用此方法。
     * 返回作为参数传入的 URI 的 MIME 数据类型。
     *
     * @param uri 需要获取 MIME 类型的 URI。
     * @return URI 的 MIME 类型。
     * @throws IllegalArgumentException 如果传入的 URI 模式无效。
     */
    @Override
    public String getType(Uri uri) {

        /**
         * 根据传入的 URI 模式选择 MIME 类型
         */
        switch (sUriMatcher.match(uri)) {

            // 如果模式是笔记或实时文件夹，返回通用的内容类型。
            case NOTES:
            case LIVE_FOLDER_NOTES:
                return NotePad.Notes.CONTENT_TYPE;

            // 如果模式是笔记 ID，返回笔记 ID 的内容类型。
            case NOTE_ID:
                return NotePad.Notes.CONTENT_ITEM_TYPE;

            // 如果 URI 模式不匹配任何已知模式，抛出异常。
            default:
                throw new IllegalArgumentException("未知的 URI " + uri);
        }
    }

//BEGIN_INCLUDE(stream)
    /**
     * 该方法描述了支持打开笔记 URI 作为流的 MIME 类型。
     */
    static ClipDescription NOTE_STREAM_TYPES = new ClipDescription(null,
            new String[]{ClipDescription.MIMETYPE_TEXT_PLAIN});

    /**
     * 返回可用数据流的类型。支持特定笔记的 URI。
     * 应用程序可以将这些笔记转换为纯文本流。
     *
     * @param uri            要分析的 URI
     * @param mimeTypeFilter MIME 类型过滤器。此方法仅返回匹配该过滤器的 MIME 类型的数据流类型。
     *                       当前仅支持 text/plain MIME 类型。
     * @return 数据流 MIME 类型。当前仅返回 text/plain。
     * @throws IllegalArgumentException 如果 URI 模式不匹配任何支持的模式。
     */
    @Override
    public String[] getStreamTypes(Uri uri, String mimeTypeFilter) {
        /**
         * 根据传入的 URI 模式选择数据流类型。
         */
        switch (sUriMatcher.match(uri)) {

            // 如果模式是笔记或实时文件夹，返回 null。此类型的 URI 不支持数据流。
            case NOTES:
            case LIVE_FOLDER_NOTES:
                return null;

            // 如果模式是笔记 ID 并且 MIME 过滤器是 text/plain，则返回 text/plain
            case NOTE_ID:
                return NOTE_STREAM_TYPES.filterMimeTypes(mimeTypeFilter);

            // 如果 URI 模式不匹配任何已知模式，抛出异常。
            default:
                throw new IllegalArgumentException("未知的 URI " + uri);
        }
    }

    /**
     * 返回每种支持的流类型的数据流。此方法对传入的 URI 执行查询，然后使用
     * {@link android.content.ContentProvider#openPipeHelper(Uri, String, Bundle, Object,
     * PipeDataWriter)} 启动另一个线程，将数据转换为流。
     *
     * @param uri            URI 模式，指向数据流
     * @param mimeTypeFilter 包含 MIME 类型的字符串。此方法尝试获取具有此 MIME 类型的数据流。
     * @param opts           客户端提供的附加选项。可以根据内容提供者的要求进行解释。
     * @return AssetFileDescriptor 文件的句柄。
     * @throws FileNotFoundException 如果传入的 URI 没有关联的文件。
     */
    @Override
    public AssetFileDescriptor openTypedAssetFile(Uri uri, String mimeTypeFilter, Bundle opts)
            throws FileNotFoundException {

        // 检查 MIME 类型过滤器是否匹配支持的 MIME 类型。
        String[] mimeTypes = getStreamTypes(uri, mimeTypeFilter);

        // 如果 MIME 类型受支持
        if (mimeTypes != null) {

            // 检索此 URI 对应的笔记。使用为此提供者定义的查询方法，
            // 而不是直接使用数据库查询方法。
            Cursor c = query(
                    uri,                    // 笔记的 URI
                    READ_NOTE_PROJECTION,   // 获取包含笔记 ID、标题和内容的投影
                    null,                   // 没有 WHERE 子句，获取所有匹配的记录
                    null,                   // 由于没有 WHERE 子句，无需选择标准
                    null                    // 使用默认的排序顺序（按修改日期降序）
            );

            // 如果查询失败或光标为空，则停止
            if (c == null || !c.moveToFirst()) {

                // 如果光标为空，则直接关闭光标并返回
                if (c != null) {
                    c.close();
                }

                // 如果光标为 null，抛出异常
                throw new FileNotFoundException("无法查询 " + uri);
            }

            // 启动一个新线程，将流数据传回给调用者。
            return new AssetFileDescriptor(
                    openPipeHelper(uri, mimeTypes[0], opts, c, this), 0,
                    AssetFileDescriptor.UNKNOWN_LENGTH);
        }

        // 如果 MIME 类型不受支持，返回一个只读的文件句柄。
        return super.openTypedAssetFile(uri, mimeTypeFilter, opts);
    }

    /**
     * 实现 {@link android.content.ContentProvider.PipeDataWriter} 来执行实际的工作，
     * 将光标中的数据转换为客户端可以读取的数据流。
     */
    @Override
    public void writeDataToPipe(ParcelFileDescriptor output, Uri uri, String mimeType,
                                Bundle opts, Cursor c) {
        // 当前我们只支持将单个笔记条目转换为文本，因此这里不需要对光标的数据类型进行检查。
        FileOutputStream fout = new FileOutputStream(output.getFileDescriptor());
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new OutputStreamWriter(fout, "UTF-8"));
            pw.println(c.getString(READ_NOTE_TITLE_INDEX));
            pw.println("");
            pw.println(c.getString(READ_NOTE_NOTE_INDEX));
        } catch (UnsupportedEncodingException e) {
            Log.w(TAG, "发生错误", e);
        } finally {
            c.close();
            if (pw != null) {
                pw.flush();
            }
            try {
                fout.close();
            } catch (IOException e) {
            }
        }
    }
//END_INCLUDE(stream)

    /**
     * 当客户端调用 {@link android.content.ContentResolver#insert(Uri, ContentValues)} 时调用此方法。
     * 向数据库插入一行新数据。此方法为所有未包含在传入映射中的列设置默认值。
     * 如果插入了行数据，则会通知监听器数据已更改。
     *
     * @return 插入行的行 ID。
     * @throws SQLException 如果插入失败。
     */
    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        // 验证传入的 URI。仅允许使用完整的提供者 URI 来执行插入。
        if (sUriMatcher.match(uri) != NOTES) {
            throw new IllegalArgumentException("未知的 URI " + uri);
        }

        // 用于存储新记录值的映射。
        ContentValues values;

        // 如果传入的值映射不为空，则使用该映射作为新值。
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            // 否则，创建一个新的值映射
            values = new ContentValues();
        }

        // 获取当前系统时间（毫秒）
        Long now = Long.valueOf(System.currentTimeMillis());

        // 如果值映射中不包含创建日期，则将其设置为当前时间。
        if (values.containsKey(NotePad.Notes.COLUMN_NAME_CREATE_DATE) == false) {
            values.put(NotePad.Notes.COLUMN_NAME_CREATE_DATE, now);
        }

        // 如果值映射中不包含修改日期，则将其设置为当前时间。
        if (values.containsKey(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE) == false) {
            values.put(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE, now);
        }

        // 如果值映射中不包含标题，则将其设置为默认标题。
        if (values.containsKey(NotePad.Notes.COLUMN_NAME_TITLE) == false) {
            Resources r = Resources.getSystem();
            values.put(NotePad.Notes.COLUMN_NAME_TITLE, r.getString(android.R.string.untitled));
        }

        // 如果值映射中不包含笔记文本，则将其设置为空字符串。
        if (values.containsKey(NotePad.Notes.COLUMN_NAME_NOTE) == false) {
            values.put(NotePad.Notes.COLUMN_NAME_NOTE, "");
        }

        // 如果值映射中不包含背景颜色，则将其设置为默认颜色（白色）。
        if (values.containsKey(NotePad.Notes.COLUMN_NAME_BACK_COLOR) == false) {
            values.put(NotePad.Notes.COLUMN_NAME_BACK_COLOR, NotePad.Notes.DEFAULT_COLOR);
        }

        // 如果值映射中不包含分类，则将其设置为默认分类（任务）。
        if (values.containsKey(NotePad.Notes.COLUMN_NAME_CATEGORY) == false) {
            values.put(NotePad.Notes.COLUMN_NAME_CATEGORY, NotePad.Notes.CATEGORY_TASK);  // 默认分类为 "任务"
        }

        // 以“写入”模式打开数据库对象。
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        // 执行插入操作并返回新笔记的 ID。
        long rowId = db.insert(
                NotePad.Notes.TABLE_NAME,        // 插入的表格名称
                NotePad.Notes.COLUMN_NAME_NOTE,  // 一个特殊处理，SQLite 会将该列值设置为 null
                values                           // 包含列名和要插入的列值的映射
        );

        // 如果插入成功，rowId 会大于 0。
        if (rowId > 0) {
            // 创建一个包含笔记 ID 模式和新行 ID 的 URI。
            Uri noteUri = ContentUris.withAppendedId(NotePad.Notes.CONTENT_ID_URI_BASE, rowId);

            // 通知注册到此提供者的观察者数据发生了变化。
            getContext().getContentResolver().notifyChange(noteUri, null);
            return noteUri;
        }

        // 如果插入失败，则 rowId <= 0。抛出异常。
        throw new SQLException("插入行失败，URI " + uri);
    }

    /**
     * 当客户端调用 {@link android.content.ContentResolver#delete(Uri, String, String[])} 时调用此方法。
     * 从数据库中删除记录。如果传入的 URI 匹配笔记 ID URI 模式，则此方法删除 URI 中指定的单个记录。
     * 否则，它会删除一组记录。记录必须同时匹配输入的选择条件（由 where 和 whereArgs 指定）。
     * <p>
     * 如果删除了行数据，则会通知监听器数据已更改。
     *
     * @return 如果使用了 "where" 子句，则返回受影响的行数，否则返回 0。
     * 如果要删除所有行并获取行数，使用 "1" 作为 where 子句。
     * @throws IllegalArgumentException 如果传入的 URI 模式无效。
     */
    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {

        // 以“写入”模式打开数据库对象。
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        String finalWhere;

        int count;

        // 根据传入的 URI 模式执行删除操作。
        switch (sUriMatcher.match(uri)) {

            // 如果传入的模式匹配笔记的一般模式，根据传入的 "where" 列名和参数执行删除。
            case NOTES:
                count = db.delete(
                        NotePad.Notes.TABLE_NAME,  // 数据库表名称
                        where,                     // 传入的 where 子句列名
                        whereArgs                  // 传入的 where 子句值
                );
                break;

            // 如果传入的 URI 匹配单个笔记 ID，则基于传入数据执行删除，但会修改 where 子句以限制删除特定的笔记 ID。
            case NOTE_ID:
                /*
                 * 开始构建最终的 WHERE 子句，限制为特定的笔记 ID。
                 */
                finalWhere =
                        NotePad.Notes._ID +                              // ID 列名
                                " = " +                                          // 检查是否相等
                                uri.getPathSegments().                           // 获取传入的笔记 ID
                                        get(NotePad.Notes.NOTE_ID_PATH_POSITION)
                ;

                // 如果有其他选择条件，将其附加到最终的 WHERE 子句。
                if (where != null) {
                    finalWhere = finalWhere + " AND " + where;
                }

                // 执行删除操作。
                count = db.delete(
                        NotePad.Notes.TABLE_NAME,  // 数据库表名称。
                        finalWhere,                // 最终的 WHERE 子句。
                        whereArgs                  // 传入的 where 子句值。
                );
                break;

            // 如果传入的模式无效，则抛出异常。
            default:
                throw new IllegalArgumentException("未知的 URI " + uri);
        }

        /* 获取当前上下文的 content resolver 对象，并通知它传入的 URI 已更改。
         * 该对象将此信息传递给 resolver 框架，已注册的观察者将会收到通知。
         */
        getContext().getContentResolver().notifyChange(uri, null);

        // 返回删除的行数。
        return count;
    }

    /**
     * 当客户端调用 {@link android.content.ContentResolver#update(Uri, ContentValues, String, String[])} 时调用此方法。
     * 更新数据库中的记录。值映射中指定的列名将被新的数据更新。如果传入的 URI 匹配笔记 ID URI 模式，
     * 则该方法更新 URI 中指定的单条记录；否则，它更新一组记录。记录必须匹配由 where 和 whereArgs 指定的输入选择条件。
     * 如果更新了行数据，则会通知监听器数据已更改。
     *
     * @param uri       需要匹配并更新的 URI 模式。
     * @param values    包含列名（键）和新值（值）的映射。
     * @param where     SQL "WHERE" 子句，根据列值选择记录。如果为 null，则会选择所有匹配 URI 模式的记录。
     * @param whereArgs 选择条件的数组。如果 "where" 参数包含占位符（"?"），则每个占位符将被数组中相应的元素替换。
     * @return 更新的行数。
     * @throws IllegalArgumentException 如果传入的 URI 模式无效。
     */
    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {

        // 以“写入”模式打开数据库对象。
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        String finalWhere;
        // 根据传入的 URI 模式执行更新操作。
        switch (sUriMatcher.match(uri)) {

            // 如果传入的 URI 匹配一般的笔记模式，则根据传入的数据执行更新。
            case NOTES:

                // 执行更新并返回更新的行数。
                count = db.update(
                        NotePad.Notes.TABLE_NAME, // 数据库表名称。
                        values,                   // 包含列名和新值的映射。
                        where,                    // where 子句列名。
                        whereArgs                 // where 子句列值。
                );
                break;

            // 如果传入的 URI 匹配单个笔记 ID，则基于传入数据执行更新，但会修改 where 子句以限制更新特定的笔记 ID。
            case NOTE_ID:
                // 从传入的 URI 获取笔记 ID
                String noteId = uri.getPathSegments().get(NotePad.Notes.NOTE_ID_PATH_POSITION);

                /*
                 * 开始构建最终的 WHERE 子句，限制为传入的笔记 ID。
                 */
                finalWhere =
                        NotePad.Notes._ID +                              // ID 列名
                                " = " +                                          // 检查是否相等
                                uri.getPathSegments().                           // 获取传入的笔记 ID
                                        get(NotePad.Notes.NOTE_ID_PATH_POSITION)
                ;

                // 如果有其他选择条件，将其附加到最终的 WHERE 子句。
                if (where != null) {
                    finalWhere = finalWhere + " AND " + where;
                }

                // 执行更新操作并返回更新的行数。
                count = db.update(
                        NotePad.Notes.TABLE_NAME, // 数据库表名称。
                        values,                   // 包含列名和新值的映射。
                        finalWhere,               // 最终的 WHERE 子句。
                        // whereArgs 中的占位符值
                        whereArgs                 // 选择条件的列值，或者为 null（如果值在 where 参数中）。
                );
                break;

            // 如果传入的模式无效，则抛出异常。
            default:
                throw new IllegalArgumentException("未知的 URI " + uri);
        }

        /* 获取当前上下文的 content resolver 对象，并通知它传入的 URI 已更改。
         * 该对象将此信息传递给 resolver 框架，已注册的观察者将会收到通知。
         */
        getContext().getContentResolver().notifyChange(uri, null);

        // 返回更新的行数。
        return count;
    }

    /**
     * 测试包可以调用此方法以获取底层 NotePadProvider 数据库的句柄，
     * 以便将测试数据插入数据库。测试用例类负责在测试上下文中实例化提供者；
     * {@link android.test.ProviderTestCase2} 在调用 setUp() 时会执行此操作。
     *
     * @return 提供者数据的数据库帮助对象句柄。
     */
    DatabaseHelper getOpenHelperForTest() {
        return mOpenHelper;
    }
}