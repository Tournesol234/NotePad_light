package com.example.android.notepad;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
public class NoteColor extends Activity {

    private Cursor mCursor;
    private Uri mUri;
    private int color;

    private static final String[] PROJECTION = new String[] {
            NotePad.Notes._ID,                // 0
            NotePad.Notes.COLUMN_NAME_BACK_COLOR,  // 1
    };

    // 记录背景色的列索引
    private static final int COLUMN_INDEX_BACK_COLOR = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_color);

        mUri = getIntent().getData();  // 获取传入的 URI
        // 使用 ContentResolver.query() 替代过时的 managedQuery()
        mCursor = getContentResolver().query(
                mUri,        // The URI for the note that is to be retrieved.
                PROJECTION,  // The columns to retrieve
                null,         // No selection criteria are used
                null,         // No selection arguments
                null          // No sort order is needed
        );

        if (mCursor != null && mCursor.moveToFirst()) {
            color = mCursor.getInt(COLUMN_INDEX_BACK_COLOR); // 获取当前背景色
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mCursor != null && mCursor.moveToFirst()) {
            // 获取当前颜色
            color = mCursor.getInt(COLUMN_INDEX_BACK_COLOR);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mCursor != null) {
            // 保存更新的背景颜色
            ContentValues values = new ContentValues();
            values.put(NotePad.Notes.COLUMN_NAME_BACK_COLOR, color);
            getContentResolver().update(mUri, values, null, null);
            mCursor.close(); // 关闭 Cursor，释放资源
        }
    }

    public void white(View view) {
        color = NotePad.Notes.DEFAULT_COLOR;
        finish();  // 完成操作后结束 Activity
    }

    public void yellow(View view) {
        color = NotePad.Notes.YELLOW_COLOR;
        finish();  // 完成操作后结束 Activity
    }

    public void blue(View view) {
        color = NotePad.Notes.BLUE_COLOR;
        finish();  // 完成操作后结束 Activity
    }

    public void green(View view) {
        color = NotePad.Notes.GREEN_COLOR;
        finish();  // 完成操作后结束 Activity
    }

    public void red(View view) {
        color = NotePad.Notes.RED_COLOR;
        finish();  // 完成操作后结束 Activity
    }
}
