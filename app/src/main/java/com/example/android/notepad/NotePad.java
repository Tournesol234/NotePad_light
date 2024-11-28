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

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Defines a contract between the Note Pad content provider and its clients. A contract defines the
 * information that a client needs to access the provider as one or more data tables. A contract
 * is a public, non-extendable (final) class that contains constants defining column names and
 * URIs. A well-written client depends only on the constants in the contract.
 */

/**
 * 在笔记本内容提供者和其客户端之间定义了一个契约。契约定义了
 * 客户端访问提供者所需的信息，作为一个或多个数据表。契约是一个公开的、不可扩展的（最终的）类，
 * 包含定义列名和URI的常量。一个编写良好的客户端只依赖于契约中的常量。
 */
public final class NotePad {
    public static final String AUTHORITY = "com.google.provider.NotePad";

    // 此类不能被实例化
    private NotePad() {
    }

    /**
     * 笔记表契约
     */
    public static final class Notes implements BaseColumns {

        // 此类不能被实例化
        private Notes() {}

        /**
         * 此提供者提供的表名
         */
        public static final String TABLE_NAME = "notes";

        /*
         * URI 定义
         */

        /**
         * 此提供者的 URI 的方案部分
         */
        private static final String SCHEME = "content://";

        /**
         * URI 的路径部分
         */

        /**
         * 笔记 URI 的路径部分
         */
        private static final String PATH_NOTES = "/notes";

        /**
         * 笔记 ID URI 的路径部分
         */
        private static final String PATH_NOTE_ID = "/notes/";

        /**
         * 在笔记 ID URI 的路径部分中，笔记 ID 段的相对位置为 0
         */
        public static final int NOTE_ID_PATH_POSITION = 1;

        /**
         * 实时文件夹 URI 的路径部分
         */
        private static final String PATH_LIVE_FOLDER = "/live_folders/notes";

        /**
         * 此表的 content:// 样式 URL
         */
        public static final Uri CONTENT_URI =  Uri.parse(SCHEME + AUTHORITY + PATH_NOTES);

        /**
         * 单个笔记的内容 URI 基础。调用者必须
         * 在此 Uri 末尾追加一个数字笔记 id 来检索笔记
         */
        public static final Uri CONTENT_ID_URI_BASE
                = Uri.parse(SCHEME + AUTHORITY + PATH_NOTE_ID);

        /**
         * 由其 ID 指定的单个笔记的内容 URI 匹配模式。使用此匹配
         * 来入站 URI 或构造 Intent。
         */
        public static final Uri CONTENT_ID_URI_PATTERN
                = Uri.parse(SCHEME + AUTHORITY + PATH_NOTE_ID + "/#");

        /**
         * 实时文件夹的笔记列表的内容 Uri 模式
         */
        public static final Uri LIVE_FOLDER_URI
                = Uri.parse(SCHEME + AUTHORITY + PATH_LIVE_FOLDER);

        /*
         * MIME 类型定义
         */

        /**
         * {@link #CONTENT_URI} 提供的笔记目录的 MIME 类型。
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.google.note";

        /**
         * {@link #CONTENT_URI} 的子目录中单个
         * 笔记的 MIME 类型。
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.google.note";

        /**
         * 此表的默认排序顺序
         */
        public static final String DEFAULT_SORT_ORDER = "modified DESC";

        /*
         * 列定义
         */

        /**
         * 笔记标题的列名
         * <P>类型: TEXT</P>
         */
        public static final String COLUMN_NAME_TITLE = "title";

        /**
         * 笔记内容的列名
         * <P>类型: TEXT</P>
         */
        public static final String COLUMN_NAME_NOTE = "note";

        /**
         * 创建时间戳的列名
         * <P>类型: INTEGER（来自 System.currentTimeMillis() 的长整型）</P>
         */
        public static final String COLUMN_NAME_CREATE_DATE = "created";

        /**
         * 修改时间戳的列名
         * <P>类型: INTEGER（来自 System.currentTimeMillis() 的长整型）</P>
         */
        public static final String COLUMN_NAME_MODIFICATION_DATE = "modified";

        /**
         * Column name for the background color
         */
        public static final String COLUMN_NAME_BACK_COLOR = "color";

        /**
         * background color
         */
        public static final int DEFAULT_COLOR = 0; //白
        public static final int YELLOW_COLOR = 1;
        public static final int BLUE_COLOR = 2;
        public static final int GREEN_COLOR = 3;
        public static final int RED_COLOR = 4;
    }
}
