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

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

/**
 * 该 Activity 允许用户编辑笔记的标题。它显示一个包含 EditText 的浮动窗口。
 *
 * 注意：请注意，在此 Activity 中，提供者操作发生在 UI 线程上。
 * 这不是一个好做法。这里只是为了让代码更具可读性。在实际应用中，
 * 应该使用 {@link android.content.AsyncQueryHandler}
 * 或 {@link android.os.AsyncTask} 对象在单独的线程上异步执行操作。
 */
public class TitleEditor extends Activity {

    /**
     * 这是一个特殊的意图动作，表示“编辑笔记的标题”。
     */
    public static final String EDIT_TITLE_ACTION = "com.android.notepad.action.EDIT_TITLE";

    // 创建一个投影，返回笔记的 ID 和标题内容。
    private static final String[] PROJECTION = new String[] {
            NotePad.Notes._ID, // 0
            NotePad.Notes.COLUMN_NAME_TITLE, // 1
    };

    // 游标返回的列中标题的索引位置。
    private static final int COLUMN_INDEX_TITLE = 1;

    // 一个 Cursor 对象，包含从提供者查询笔记结果。
    private Cursor mCursor;

    // 一个 EditText 对象，用于保存编辑后的标题。
    private EditText mText;

    // 一个 URI 对象，表示正在编辑标题的笔记。
    private Uri mUri;

    /**
     * 该方法在 Android 启动 Activity 时调用。它从传入的 Intent 中确定所需的编辑操作，
     * 然后执行相应的操作。
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 设置此 Activity 的 UI 布局。
        setContentView(R.layout.title_editor);

        // 获取启动此 Activity 的 Intent，并从中获取需要编辑的笔记的 URI。
        mUri = getIntent().getData();

        /*
         * 使用传入的 URI 获取笔记。
         *
         * 注意：这是在 UI 线程上执行的。它会阻塞线程直到查询完成。
         * 在一个示例应用中，基于本地数据库的简单提供者查询，阻塞时间是短暂的，
         * 但在实际应用中应该使用
         * android.content.AsyncQueryHandler 或 android.os.AsyncTask。
         */

        mCursor = managedQuery(
                mUri,        // 要检索的笔记的 URI。
                PROJECTION,  // 要检索的列
                null,        // 不使用选择条件，因此不需要 "where" 列。
                null,        // 不使用 "where" 列，因此不需要 "where" 值。
                null         // 不需要排序。
        );

        // 获取 EditText 组件的 ID
        mText = (EditText) this.findViewById(R.id.title);
    }

    /**
     * 该方法在 Activity 即将进入前台时调用。当 Activity 排到任务栈的顶部时，
     * 或者在第一次启动时，都会调用此方法。
     *
     * 显示所选笔记的当前标题。
     */
    @Override
    protected void onResume() {
        super.onResume();

        // 验证在 onCreate() 中执行的查询是否成功。如果查询成功，则
        // Cursor 对象不为空。如果它是 *空的*，则 mCursor.getCount() == 0。
        if (mCursor != null) {

            // 游标刚刚检索到，因此其索引被设置为在检索到的第一个记录之前的位置。
            // 这将游标移到第一条记录。
            mCursor.moveToFirst();

            // 将当前的标题文本显示在 EditText 组件中。
            mText.setText(mCursor.getString(COLUMN_INDEX_TITLE));
        }
    }

    /**
     * 该方法在 Activity 失去焦点时调用。
     *
     * 对于编辑信息的 Activity 对象，onPause() 可能是保存更改的唯一位置。
     * Android 的应用程序模型假定“保存”和“退出”不是必须的操作。
     * 当用户离开一个 Activity 时，他们不应该必须返回它来完成工作。
     * 离开时应该自动保存所有内容，并使 Activity 保持一种可以销毁的状态。
     *
     * 使用当前文本框中的文本更新笔记。
     */
    @Override
    protected void onPause() {
        super.onPause();

        // 验证在 onCreate() 中执行的查询是否成功。如果查询成功，则
        // Cursor 对象不为空。如果它是 *空的*，则 mCursor.getCount() == 0。

        if (mCursor != null) {

            // 创建一个值映射用于更新提供者。
            ContentValues values = new ContentValues();

            // 在值映射中，将标题设置为当前编辑框中的内容。
            values.put(NotePad.Notes.COLUMN_NAME_TITLE, mText.getText().toString());

            /*
             * 使用新标题更新提供者。
             *
             * 注意：这是在 UI 线程上执行的。它会阻塞线程直到更新完成。
             * 在一个示例应用中，基于本地数据库的简单提供者更新，阻塞时间是短暂的，
             * 但在实际应用中应该使用
             * android.content.AsyncQueryHandler 或 android.os.AsyncTask。
             */
            getContentResolver().update(
                    mUri,    // 要更新的笔记的 URI。
                    values,  // 包含要更新的列和相应值的值映射。
                    null,    // 不使用选择条件，因此不需要 "where" 列。
                    null     // 不使用 "where" 列，因此不需要 "where" 值。
            );

        }
    }

    public void onClickOk(View v) {
        finish();
    }
}
