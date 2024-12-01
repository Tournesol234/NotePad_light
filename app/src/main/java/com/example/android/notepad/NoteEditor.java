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
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

/**
 * 这个活动处理“编辑”笔记，其中编辑是响应
 * {@link Intent#ACTION_VIEW}（请求查看数据）、编辑笔记
 * {@link Intent#ACTION_EDIT}、创建笔记 {@link Intent#ACTION_INSERT} 或
 * 从剪贴板当前内容创建新笔记 {@link Intent#ACTION_PASTE}。
 *
 * 注意：注意这个活动中的提供者操作是在UI线程上进行的。
 * 这不是一个好的实践。这里只是为了使代码更易读而这样做。一个真正的
 * 应用应该使用 {@link android.content.AsyncQueryHandler}
 * 或 {@link android.os.AsyncTask} 对象在单独的线程上异步执行操作。
 */

public class NoteEditor extends Activity {
    // For logging and debugging purposes
    private static final String TAG = "NoteEditor";

    /*
     * Creates a projection that returns the note ID and the note contents.
     */
    private static final String[] PROJECTION = new String[] {
            NotePad.Notes._ID,
            NotePad.Notes.COLUMN_NAME_TITLE,
            NotePad.Notes.COLUMN_NAME_NOTE,
            NotePad.Notes.COLUMN_NAME_BACK_COLOR,
            NotePad.Notes.COLUMN_NAME_CATEGORY // 添加分类字段
    };



    // A label for the saved state of the activity
    private static final String ORIGINAL_CONTENT = "origContent";

    // This Activity can be started by more than one action. Each action is represented
    // as a "state" constant
    private static final int STATE_EDIT = 0;
    private static final int STATE_INSERT = 1;

    // Global mutable variables
    private int mState;
    private Uri mUri;
    private Cursor mCursor;
    private EditText mText;
    private String mOriginalContent;

    /**
     * Defines a custom EditText View that draws lines between each line of text that is displayed.
     */
    public static class LinedEditText extends EditText {
        private Rect mRect;
        private Paint mPaint;

        // This constructor is used by LayoutInflater
        public LinedEditText(Context context, AttributeSet attrs) {
            super(context, attrs);

            // Creates a Rect and a Paint object, and sets the style and color of the Paint object.
            mRect = new Rect();
            mPaint = new Paint();
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setColor(0x800000FF);
        }

        /**
         * This is called to draw the LinedEditText object
         * @param canvas The canvas on which the background is drawn.
         */
        @Override
        protected void onDraw(Canvas canvas) {

            // Gets the number of lines of text in the View.
            int count = getLineCount();

            // Gets the global Rect and Paint objects
            Rect r = mRect;
            Paint paint = mPaint;

            /*
             * Draws one line in the rectangle for every line of text in the EditText
             */
            for (int i = 0; i < count; i++) {

                // Gets the baseline coordinates for the current line of text
                int baseline = getLineBounds(i, r);

                /*
                 * Draws a line in the background from the left of the rectangle to the right,
                 * at a vertical position one dip below the baseline, using the "paint" object
                 * for details.
                 */
                canvas.drawLine(r.left, baseline + 1, r.right, baseline + 1, paint);
            }

            // Finishes up by calling the parent method
            super.onDraw(canvas);
        }
    }

    /**
     * This method is called by Android when the Activity is first started. From the incoming
     * Intent, it determines what kind of editing is desired, and then does it.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 获取 Intent 对象
        final Intent intent = getIntent();

        // 获取触发该 Activity 的 Action
        final String action = intent.getAction();

        // 设置布局文件
        setContentView(R.layout.note_editor);

// 获取 Spinner 视图
        Spinner categorySpinner = (Spinner) findViewById(R.id.spinner_category);

// 创建 ArrayAdapter，加载字符串数组
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,                  // 当前上下文
                R.array.categories_array, // 引用 string-array 中的数组
                android.R.layout.simple_spinner_item // 使用简单的布局显示每个项
        );

// 设置下拉列表的样式
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

// 设置适配器给 Spinner
        categorySpinner.setAdapter(adapter);


        // 获取笔记内容
        mText = (EditText) findViewById(R.id.note);

        // 获取 URI 和状态，准备进行插入或编辑
        if (Intent.ACTION_EDIT.equals(action)) {
            mState = STATE_EDIT;
            mUri = intent.getData();
        } else if (Intent.ACTION_INSERT.equals(action) || Intent.ACTION_PASTE.equals(action)) {
            mState = STATE_INSERT;
            mUri = getContentResolver().insert(intent.getData(), null);

            // 插入失败时关闭 Activity
            if (mUri == null) {
                Log.e(TAG, "Failed to insert new note into " + getIntent().getData());
                finish();
                return;
            }

            // 设置默认分类为 "task"
            ContentValues values = new ContentValues();
            values.put(NotePad.Notes.COLUMN_NAME_CATEGORY, NotePad.Notes.CATEGORY_TASK);  // 默认分类为任务
            getContentResolver().update(mUri, values, null, null);

            setResult(RESULT_OK, (new Intent()).setAction(mUri.toString()));
        }

        // 加载笔记内容
        mCursor = managedQuery(
                mUri,
                PROJECTION,
                null,
                null,
                null
        );

        // 如果是粘贴操作，执行粘贴功能
        if (Intent.ACTION_PASTE.equals(action)) {
            performPaste();
            mState = STATE_EDIT;
        }

        // 处理保存的原始内容
        if (savedInstanceState != null) {
            mOriginalContent = savedInstanceState.getString(ORIGINAL_CONTENT);
        }

        // 绑定分类选择，恢复用户的选择
        if (mState == STATE_EDIT) {
            int position = getCategoryPositionFromDatabase(mUri);
            categorySpinner.setSelection(position);
        }
    }

    // 根据数据库中的分类获取对应的位置
    private int getCategoryPositionFromDatabase(Uri noteUri) {
        Cursor cursor = getContentResolver().query(noteUri,
                new String[]{NotePad.Notes.COLUMN_NAME_CATEGORY}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            String category = cursor.getString(cursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_CATEGORY));
            cursor.close();

            if ("学习".equals(category)) {
                return 0;
            } else if ("生活".equals(category)) {
                return 1;
            } else {
                return 2;
            }
        }
        return 2;
    }

    /**
     * This method is called when the Activity is about to come to the foreground. This happens
     * when the Activity comes to the top of the task stack, OR when it is first starting.
     *
     * Moves to the first note in the list, sets an appropriate title for the action chosen by
     * the user, puts the note contents into the TextView, and saves the original text as a
     * backup.
     */
    @Override
    protected void onResume() {
        super.onResume();

        // 确保 mCursor 不为空
        if (mCursor != null) {
            // 确保 Cursor 已经正确获取数据并移到第一条记录
            if (mCursor.moveToFirst()) {
                // 根据当前状态设置标题
                if (mState == STATE_EDIT) {
                    int colTitleIndex = mCursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_TITLE);
                    if (colTitleIndex != -1) {
                        String title = mCursor.getString(colTitleIndex);
                        Resources res = getResources();
                        String text = String.format(res.getString(R.string.title_edit), title);
                        setTitle(text);
                    }
                } else if (mState == STATE_INSERT) {
                    setTitle(getText(R.string.title_create));
                }

                // 获取笔记内容并显示
                int colNoteIndex = mCursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_NOTE);
                if (colNoteIndex != -1) {
                    String note = mCursor.getString(colNoteIndex);
                    mText.setTextKeepState(note); // 保持文本框状态
                }

                // 存储原始内容
                if (mOriginalContent == null) {
                    mOriginalContent = mText.getText().toString();
                }

                // 读取背景颜色并设置背景
                int colorIndex = mCursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_BACK_COLOR);
                if (colorIndex != -1) {
                    int colorCode = mCursor.getInt(colorIndex);

                    // 设置背景颜色
                    switch (colorCode) {
                        case NotePad.Notes.DEFAULT_COLOR:
                            mText.setBackgroundColor(Color.rgb(255, 255, 255)); // 白色
                            break;
                        case NotePad.Notes.YELLOW_COLOR:
                            mText.setBackgroundColor(Color.rgb(247, 216, 133)); // 黄色
                            break;
                        case NotePad.Notes.BLUE_COLOR:
                            mText.setBackgroundColor(Color.rgb(165, 202, 237)); // 蓝色
                            break;
                        case NotePad.Notes.GREEN_COLOR:
                            mText.setBackgroundColor(Color.rgb(161, 214, 174)); // 绿色
                            break;
                        case NotePad.Notes.RED_COLOR:
                            mText.setBackgroundColor(Color.rgb(244, 149, 133)); // 红色
                            break;
                        default:
                            mText.setBackgroundColor(Color.rgb(255, 255, 255)); // 默认背景色为白色
                            break;
                    }
                }

                // 加载分类信息并设置到 Spinner
                int categoryIndex = mCursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_CATEGORY);
                if (categoryIndex != -1) {
                    String category = mCursor.getString(categoryIndex);
                    Spinner categorySpinner = (Spinner) findViewById(R.id.spinner_category);

                    // 查找分类的索引并设置 Spinner
                    ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) categorySpinner.getAdapter();
                    int position = adapter.getPosition(category);
                    categorySpinner.setSelection(position);
                }

            } else {
                // 如果没有数据，则设置错误信息
                setTitle(getText(R.string.error_title));
                mText.setText(getText(R.string.error_message));
            }
        } else {
            // 如果 Cursor 为 null，则显示错误信息
            setTitle(getText(R.string.error_title));
            mText.setText(getText(R.string.error_message));
        }
    }


    /**
     * This method is called when an Activity loses focus during its normal operation, and is then
     * later on killed. The Activity has a chance to save its state so that the system can restore
     * it.
     *
     * Notice that this method isn't a normal part of the Activity lifecycle. It won't be called
     * if the user simply navigates away from the Activity.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Save away the original text, so we still have it if the activity
        // needs to be killed while paused.
        outState.putString(ORIGINAL_CONTENT, mOriginalContent);
    }

    /**
     * This method is called when the Activity loses focus.
     *
     * For Activity objects that edit information, onPause() may be the one place where changes are
     * saved. The Android application model is predicated on the idea that "save" and "exit" aren't
     * required actions. When users navigate away from an Activity, they shouldn't have to go back
     * to it to complete their work. The act of going away should save everything and leave the
     * Activity in a state where Android can destroy it if necessary.
     *
     * If the user hasn't done anything, then this deletes or clears out the note, otherwise it
     * writes the user's work to the provider.
     */
    @Override
    protected void onPause() {
        super.onPause();

        // 检查 Cursor 是否为空
        if (mCursor != null) {

            // 获取当前笔记文本
            String text = mText.getText().toString();
            int length = text.length();

            /*
             * 如果 Activity 正在关闭并且当前笔记没有任何内容，则返回取消结果并删除笔记。
             */
            if (isFinishing() && (length == 0)) {
                setResult(RESULT_CANCELED);  // 返回取消结果
                deleteNote();  // 删除笔记
            } else {
                /*
                 * 如果当前是编辑状态，更新笔记。
                 * 如果是插入新笔记的状态，创建新的笔记。
                 */
                if (mState == STATE_EDIT) {
                    updateNote(text, null);  // 更新现有笔记
                } else if (mState == STATE_INSERT) {
                    updateNote(text, text);  // 插入新笔记
                    mState = STATE_EDIT;  // 切换为编辑状态
                }

                // 保存用户选择的分类
                Spinner categorySpinner = (Spinner) findViewById(R.id.spinner_category);
                String selectedCategory = categorySpinner.getSelectedItem().toString();

                // 更新数据库中的分类
                ContentValues values = new ContentValues();
                values.put(NotePad.Notes.COLUMN_NAME_CATEGORY, selectedCategory);  // 设置用户选择的分类

                // 更新笔记的分类
                getContentResolver().update(mUri, values, null, null);
            }
        }
    }


    /**
     * This method is called when the user clicks the device's Menu button the first time for
     * this Activity. Android passes in a Menu object that is populated with items.
     *
     * Builds the menus for editing and inserting, and adds in alternative actions that
     * registered themselves to handle the MIME types for this application.
     *
     * @param menu A Menu object to which items should be added.
     * @return True to display the menu.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu from XML resource
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.editor_options_menu, menu);

        // Only add extra menu items for a saved note 
        if (mState == STATE_EDIT) {
            // Append to the
            // menu items for any other activities that can do stuff with it
            // as well.  This does a query on the system for any activities that
            // implement the ALTERNATIVE_ACTION for our data, adding a menu item
            // for each one that is found.
            Intent intent = new Intent(null, mUri);
            intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
            menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
                    new ComponentName(this, NoteEditor.class), null, intent, 0, null);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Check if note has changed and enable/disable the revert option
        int colNoteIndex = mCursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_NOTE);
        String savedNote = mCursor.getString(colNoteIndex);
        String currentNote = mText.getText().toString();
        if (savedNote.equals(currentNote)) {
            menu.findItem(R.id.menu_revert).setVisible(false);
        } else {
            menu.findItem(R.id.menu_revert).setVisible(true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * This method is called when a menu item is selected. Android passes in the selected item.
     * The switch statement in this method calls the appropriate method to perform the action the
     * user chose.
     *
     * @param item The selected MenuItem
     * @return True to indicate that the item was processed, and no further work is necessary. False
     * to proceed to further processing as indicated in the MenuItem object.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle all of the possible menu actions.
        switch (item.getItemId()) {
        case R.id.menu_save:
            String text = mText.getText().toString();
            updateNote(text, null);
            finish();
            break;
        case R.id.menu_delete:
            deleteNote();
            finish();
            break;
        case R.id.menu_revert:
            cancelNote();
            break;

            case R.id.menu_color:
                changeColor();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    //跳转改变颜色的activity，将uri信息传到新的activity
    private final void changeColor() {
        Intent intent = new Intent(null,mUri);
        intent.setClass(NoteEditor.this,NoteColor.class);
        NoteEditor.this.startActivity(intent);
    }
//BEGIN_INCLUDE(paste)
    /**
     * A helper method that replaces the note's data with the contents of the clipboard.
     */
    private final void performPaste() {

        // Gets a handle to the Clipboard Manager
        ClipboardManager clipboard = (ClipboardManager)
                getSystemService(Context.CLIPBOARD_SERVICE);

        // Gets a content resolver instance
        ContentResolver cr = getContentResolver();

        // Gets the clipboard data from the clipboard
        ClipData clip = clipboard.getPrimaryClip();
        if (clip != null) {

            String text=null;
            String title=null;

            // Gets the first item from the clipboard data
            ClipData.Item item = clip.getItemAt(0);

            // Tries to get the item's contents as a URI pointing to a note
            Uri uri = item.getUri();

            // Tests to see that the item actually is an URI, and that the URI
            // is a content URI pointing to a provider whose MIME type is the same
            // as the MIME type supported by the Note pad provider.
            if (uri != null && NotePad.Notes.CONTENT_ITEM_TYPE.equals(cr.getType(uri))) {

                // The clipboard holds a reference to data with a note MIME type. This copies it.
                Cursor orig = cr.query(
                        uri,            // URI for the content provider
                        PROJECTION,     // Get the columns referred to in the projection
                        null,           // No selection variables
                        null,           // No selection variables, so no criteria are needed
                        null            // Use the default sort order
                );

                // If the Cursor is not null, and it contains at least one record
                // (moveToFirst() returns true), then this gets the note data from it.
                if (orig != null) {
                    if (orig.moveToFirst()) {
                        int colNoteIndex = mCursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_NOTE);
                        int colTitleIndex = mCursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_TITLE);
                        text = orig.getString(colNoteIndex);
                        title = orig.getString(colTitleIndex);
                    }

                    // Closes the cursor.
                    orig.close();
                }
            }

            // If the contents of the clipboard wasn't a reference to a note, then
            // this converts whatever it is to text.
            if (text == null) {
                text = item.coerceToText(this).toString();
            }

            // Updates the current note with the retrieved title and text.
            updateNote(text, title);
        }
    }
//END_INCLUDE(paste)

    /**
     * Replaces the current note contents with the text and title provided as arguments.
     * @param text The new note contents to use.
     * @param title The new note title to use
     */
    private final void updateNote(String text, String title) {

        // Sets up a map to contain values to be updated in the provider.
        ContentValues values = new ContentValues();
        values.put(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE, System.currentTimeMillis());

        // If the action is to insert a new note, this creates an initial title for it.
        if (mState == STATE_INSERT) {

            // If no title was provided as an argument, create one from the note text.
            if (title == null) {
  
                // Get the note's length
                int length = text.length();

                // Sets the title by getting a substring of the text that is 31 characters long
                // or the number of characters in the note plus one, whichever is smaller.
                title = text.substring(0, Math.min(30, length));
  
                // If the resulting length is more than 30 characters, chops off any
                // trailing spaces
                if (length > 30) {
                    int lastSpace = title.lastIndexOf(' ');
                    if (lastSpace > 0) {
                        title = title.substring(0, lastSpace);
                    }
                }
            }
            // In the values map, sets the value of the title
            values.put(NotePad.Notes.COLUMN_NAME_TITLE, title);
        } else if (title != null) {
            // In the values map, sets the value of the title
            values.put(NotePad.Notes.COLUMN_NAME_TITLE, title);
        }

        // This puts the desired notes text into the map.
        values.put(NotePad.Notes.COLUMN_NAME_NOTE, text);

        /*
         * Updates the provider with the new values in the map. The ListView is updated
         * automatically. The provider sets this up by setting the notification URI for
         * query Cursor objects to the incoming URI. The content resolver is thus
         * automatically notified when the Cursor for the URI changes, and the UI is
         * updated.
         * Note: This is being done on the UI thread. It will block the thread until the
         * update completes. In a sample app, going against a simple provider based on a
         * local database, the block will be momentary, but in a real app you should use
         * android.content.AsyncQueryHandler or android.os.AsyncTask.
         */
        getContentResolver().update(
                mUri,    // The URI for the record to update.
                values,  // The map of column names and new values to apply to them.
                null,    // No selection criteria are used, so no where columns are necessary.
                null     // No where columns are used, so no where arguments are necessary.
            );


    }

    /**
     * This helper method cancels the work done on a note.  It deletes the note if it was
     * newly created, or reverts to the original text of the note i
     */
    private final void cancelNote() {
        if (mCursor != null) {
            if (mState == STATE_EDIT) {
                // Put the original note text back into the database
                mCursor.close();
                mCursor = null;
                ContentValues values = new ContentValues();
                values.put(NotePad.Notes.COLUMN_NAME_NOTE, mOriginalContent);
                getContentResolver().update(mUri, values, null, null);
            } else if (mState == STATE_INSERT) {
                // We inserted an empty note, make sure to delete it
                deleteNote();
            }
        }
        setResult(RESULT_CANCELED);
        finish();
    }

    /**
     * Take care of deleting a note.  Simply deletes the entry.
     */
    private final void deleteNote() {
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
            getContentResolver().delete(mUri, null, null);
            mText.setText("");
        }
    }
}
