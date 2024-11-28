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

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
//import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Displays a list of notes. Will display notes from the {@link Uri}
 * provided in the incoming Intent if there is one, otherwise it defaults to displaying the
 * contents of the {@link NotePadProvider}.
 *
 * NOTE: Notice that the provider operations in this Activity are taking place on the UI thread.
 * This is not a good practice. It is only done here to make the code more readable. A real
 * application should use the {@link android.content.AsyncQueryHandler} or
 * {@link android.os.AsyncTask} object to perform operations asynchronously on a separate thread.
 */
/**
 * 显示笔记列表。如果传入的 Intent 中提供了 {@link Uri}，将显示对应的笔记内容，
 * 否则默认显示 {@link NotePadProvider} 的内容。
 *
 * 注意：请注意，本 Activity 中的内容提供者操作是在 UI 线程上执行的。
 * 这不是一种好的实践。这里只是为了使代码更易于阅读。一个真正的
 * 应用程序应该使用 {@link android.content.AsyncQueryHandler} 或
 * {@link android.os.AsyncTask} 对象在单独的线程上异步执行操作。
 */

public class NotesList extends ListActivity {

    // 用于日志记录和调试
    private static final String TAG = "NotesList";

    /**
     * 光标适配器需要的列
     */
    private static final String[] PROJECTION = new String[] {
            NotePad.Notes._ID, // 0
            NotePad.Notes.COLUMN_NAME_TITLE, // 1
            //扩展 显示时间 颜色
            NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE, // 2
            NotePad.Notes.COLUMN_NAME_BACK_COLOR,
    };

    /**
     * 标题列的索引
     */
    private static final int COLUMN_INDEX_TITLE = 1;

    /**
     * onCreate 方法在 Android 从头启动此 Activity 时调用。
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        int currentTheme = getSharedPreferences("prefs", MODE_PRIVATE)
                .getInt("current_theme", R.style.light); // 默认亮色主题
        setTheme(currentTheme); // 在 setContentView 之前设置主题
        super.onCreate(savedInstanceState);

        setContentView(R.layout.noteslist_item);
        // 用户不需要按住键来使用菜单快捷键。
        setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);

        /* 如果启动此 Activity 的 Intent 中没有提供数据，
         * 则表示此 Activity 是在意图过滤器匹配 MAIN 动作时启动的。
         * 我们应该使用默认的提供者 URI。
         */
        // 获取启动此 Activity 的 Intent。
        Intent intent = getIntent();

        // 如果 Intent 中没有关联数据，则将数据设置为默认 URI，
        // 该 URI 访问笔记列表。
        if (intent.getData() == null) {
            intent.setData(NotePad.Notes.CONTENT_URI);
        }

        /*
         * 为 ListView 设置上下文菜单激活的回调。监听器设置为此 Activity。
         * 这样，ListView 中的项目启用了上下文菜单，菜单由 NotesList 中的方法处理。
         */
        getListView().setOnCreateContextMenuListener(this);

        /* 执行受控查询。Activity 在需要时处理光标的关闭和重新查询。
         *
         * 请参阅关于在 UI 线程上执行提供者操作的介绍性说明。
         */
        Cursor cursor = managedQuery(
                getIntent().getData(),            // 使用提供者的默认内容 URI。
                PROJECTION,                       // 返回每个笔记的 ID 和标题。
                null,                             // 没有 where 子句，返回所有记录。
                null,                             // 没有 where 子句，因此没有 where 列值。

                NotePad.Notes.DEFAULT_SORT_ORDER  // 使用默认排序顺序。
        );

        /*
         * 以下两个数组创建了一个光标列与 ListView 中视图 ID 之间的“映射”。
         * dataColumns 数组中的每个元素代表一列的名称；
         * viewIDs 数组中的每个元素代表一个视图的 ID。
         * SimpleCursorAdapter 以升序将它们映射，以确定每列值在 ListView 中出现的位置。
         */

        // 初始化为标题列，光标列名称显示在视图中的名称
        String[] dataColumns = {NotePad.Notes.COLUMN_NAME_TITLE,NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE,NotePad.Notes.COLUMN_NAME_BACK_COLOR};

        // 初始化为 noteslist_item.xml 中 TextView 的视图 ID，
        // 用于显示光标列。//
        //
        int[] viewIDs = {R.id.tv_title,R.id.tv_date};

//        // 为 ListView 创建基础适配器。
//        SimpleCursorAdapter adapter
//                = new SimpleCursorAdapter(
//                this,                             // ListView 的上下文
//                R.layout.noteslist_item,          // 指向列表项的 XML 文件
//                cursor,                           // 用于获取项目的光标
//                dataColumns,
//                viewIDs
//        );

        //
      MyCursorAdapter  adapter = new MyCursorAdapter(
                this,
                R.layout.noteslist_item,
                cursor,
                dataColumns,
                viewIDs
        );
// 自定义格式化时间
        //
        //
//        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
//            @Override
//            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
//                if (view.getId() == R.id.tv_date) {
//
//                    long timestamp = cursor.getLong(columnIndex);
//
//                    // 格式化时间
//                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
//                    String formattedDate = sdf.format(new Date(timestamp));
//
//
//                    ((TextView) view).setText(formattedDate);
//                    return true;
//                }
//                return false;
//            }
//        });
        // 设置 ListView 的适配器为刚刚创建的光标适配器。
        setListAdapter(adapter);

    }


    /**
     * Called when the user clicks the device's Menu button the first time for
     * this Activity. Android passes in a Menu object that is populated with items.
     *
     * Sets up a menu that provides the Insert option plus a list of alternative actions for
     * this Activity. Other applications that want to handle notes can "register" themselves in
     * Android by providing an intent filter that includes the category ALTERNATIVE and the
     * mimeTYpe NotePad.Notes.CONTENT_TYPE. If they do this, the code in onCreateOptionsMenu()
     * will add the Activity that contains the intent filter to its list of options. In effect,
     * the menu will offer the user other applications that can handle notes.
     * @param menu A Menu object, to which menu items should be added.
     * @return True, always. The menu should be displayed.
     */

    /**
     * 当用户第一次点击设备的菜单按钮时调用此方法，用于此 Activity。
     * Android 会传入一个已经填充了菜单项的 Menu 对象。
     * <p>
     * 设置一个菜单，其中提供插入选项以及此 Activity 的一系列其他操作。
     * 其他希望处理笔记的应用程序可以通过提供一个包括类别 ALTERNATIVE 和
     * mimeType NotePad.Notes.CONTENT_TYPE 的意图过滤器来“注册”自己。
     * 如果它们这样做，onCreateOptionsMenu() 方法中的代码将把包含意图过滤器的 Activity
     * 添加到选项列表中。实际上，菜单将向用户提供其他可以处理笔记的应用程序。
     *
     * @param menu 一个 Menu 对象，应该在其中添加菜单项。
     * @return 始终返回 True。菜单应该被显示。
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 从 XML 资源中加载菜单
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_options_menu, menu);

        //搜索监听器
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        //展开图标
        searchView.setIconifiedByDefault(true);
        //提交时被调用
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //过滤显示的笔记
                displayNotes(newText);
                return true;
            }
        });

        // 生成可以在整个列表上执行的任何其他操作。通常情况下，这里没有其他操作，
        // 但这允许其他应用程序通过自己的操作扩展我们的菜单。

        Intent intent = new Intent(null, getIntent().getData());
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
                new ComponentName(this, NotesList.class), null, intent, 0, null);


        MenuItem themeItem = menu.findItem(R.id.theme);
        // 根据当前主题设置图标
        int currentTheme = getSharedPreferences("prefs", MODE_PRIVATE)
                .getInt("current_theme", R.style.light); // 默认亮色主题

        if (currentTheme == R.style.light) {
            themeItem.setIcon(R.drawable.ic_menu_dark);  // 设置月亮图标（亮色模式）
        } else {
            themeItem.setIcon(R.drawable.ic_menu_light);   // 设置太阳图标（暗色模式）
        }


        return super.onCreateOptionsMenu(menu);


    }

// 过滤笔记
    private void displayNotes(String text) {
        // 构造查询条件和查询参数
        String selection = null;
        String[] selectionArgs = null;

        if (!TextUtils.isEmpty(text)) {
            // 按标题和内容进行模糊查询
            selection = NotePad.Notes.COLUMN_NAME_TITLE + " LIKE ? OR " + NotePad.Notes.COLUMN_NAME_NOTE + " LIKE ?";
            selectionArgs = new String[] { "%" + text + "%", "%" + text + "%" };
        }

        // 执行查询
        Cursor cursor = getContentResolver().query(
                NotePad.Notes.CONTENT_URI,        // 使用 NotePad 提供的默认内容 URI
                PROJECTION,                       // 查询的列（ID, 标题，修改时间等）
                selection,                        // 查询条件：如果有搜索文本，就用模糊查询条件
                selectionArgs,                    // 查询条件的参数
                NotePad.Notes.DEFAULT_SORT_ORDER  // 默认按修改时间排序
        );

        // 更新适配器的光标
        MyCursorAdapter adapter = (MyCursorAdapter) getListAdapter();
        if (adapter != null) {
            adapter.changeCursor(cursor); // 更新 ListView 的数据
        }
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        // 如果剪贴板上有数据，则启用粘贴菜单项。
        ClipboardManager clipboard = (ClipboardManager)
                getSystemService(Context.CLIPBOARD_SERVICE);

        MenuItem mPasteItem = menu.findItem(R.id.menu_paste);

        // 如果剪贴板上包含内容，则启用菜单中的粘贴选项。
        if (clipboard.hasPrimaryClip()) {
            mPasteItem.setEnabled(true);
        } else {
            // 如果剪贴板为空，则禁用菜单中的粘贴选项。
            mPasteItem.setEnabled(false);
        }

        // 获取当前显示的笔记数量。
        final boolean haveItems = getListAdapter().getCount() > 0;

        // 如果列表中有任何笔记（这意味着其中一个被选中），
        // 那么我们需要生成对当前选中项可以执行的操作。
        // 这将是我们自己的特定操作与任何可用的扩展操作的组合。
        if (haveItems) {

            // 这是选中的项。
            Uri uri = ContentUris.withAppendedId(getIntent().getData(), getSelectedItemId());

            // 创建一个包含一个元素的 Intent 数组。这个数组将用于根据选中的菜单项发送 Intent。
            Intent[] specifics = new Intent[1];

            // 将数组中的 Intent 设置为对选中笔记的 URI 执行编辑操作。
            specifics[0] = new Intent(Intent.ACTION_EDIT, uri);

            // 创建一个包含一个元素的菜单项数组。这个数组将包含编辑选项。
            MenuItem[] items = new MenuItem[1];

            // 创建一个没有特定操作的 Intent，使用选中笔记的 URI。
            Intent intent = new Intent(null, uri);

            /* 向 Intent 添加 ALTERNATIVE 类别，将选中笔记的 ID URI 作为其数据。
             * 这将为 Intent 准备一个位置，用于在菜单中分组替代选项。
             */
            intent.addCategory(Intent.CATEGORY_ALTERNATIVE);

            /*
             * 将替代选项添加到菜单中
             */
            menu.addIntentOptions(
                    Menu.CATEGORY_ALTERNATIVE,  // 将 Intent 作为替代组中的选项添加。
                    Menu.NONE,                  // 不需要唯一的项 ID。
                    Menu.NONE,                  // 替代项不需要有顺序。
                    null,                       // 不排除调用者的名称。
                    specifics,                  // 这些特定选项必须优先显示。
                    intent,                     // 这些 Intent 对象映射到 specifics 中的选项。
                    Menu.NONE,                  // 不需要标志。
                    items                       // 从 specifics 到 Intent 的映射生成的菜单项
            );

            // 如果存在编辑菜单项，则为其添加快捷方式。
            if (items[0] != null) {

                // 设置编辑菜单项的快捷键为数字“1”，字母“e”
                items[0].setShortcut('1', 'e');
            }
        } else {
            // 如果列表为空，移除菜单中任何现有的替代操作
            menu.removeGroup(Menu.CATEGORY_ALTERNATIVE);
        }

        // 显示菜单
        return true;
    }


    /**
     * 当用户从菜单中选择一个选项但没有选择列表中的项时调用此方法。如果选项是 INSERT，
     * 则会发送一个带有 ACTION_INSERT 动作的新 Intent。传入的 Intent 数据会被添加到新 Intent 中，
     * 实际上这会触发 NotePad 应用程序中的 NoteEditor 活动。
     * <p>
     * 如果所选项不是 INSERT，则很可能是来自其他应用程序的替代选项。调用父方法来处理该项。
     *
     * @param item 用户选择的菜单项
     * @return 如果选择了 INSERT 菜单项则返回 true；否则返回调用父方法的结果。
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add:
                /*
                 * 使用 Intent 启动新 Activity。Activity 的 Intent 过滤器需要包含 ACTION_INSERT 动作。
                 * 没有设置类别，因此默认为 DEFAULT。实际上，这将启动 NotePad 中的 NoteEditor Activity。
                 */
                startActivity(new Intent(Intent.ACTION_INSERT, getIntent().getData()));
                return true;
            case R.id.menu_paste:
                /*
                 * 使用 Intent 启动新 Activity。Activity 的 Intent 过滤器需要包含 ACTION_PASTE 动作。
                 * 没有设置类别，因此默认为 DEFAULT。实际上，这将启动 NotePad 中的 NoteEditor Activity。
                 */
                startActivity(new Intent(Intent.ACTION_PASTE, getIntent().getData()));
                return true;
            case R.id.theme:

                toggleTheme(item);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private void toggleTheme(MenuItem item) {

        int currentTheme = getSharedPreferences("prefs", MODE_PRIVATE)
                .getInt("current_theme", R.style.light);  // 默认亮色主题

        if (currentTheme == R.style.light) {
            // 切换为暗色主题
            setTheme(R.style.dark);
            item.setIcon(R.drawable.ic_menu_light);  // 设置太阳图标
            getSharedPreferences("prefs", MODE_PRIVATE).edit()
                    .putInt("current_theme", R.style.dark)
                    .apply();
        } else {
            // 切换为亮色主题
            setTheme(R.style.light);
            item.setIcon(R.drawable.ic_menu_dark);  // 设置月亮图标
            getSharedPreferences("prefs", MODE_PRIVATE).edit()
                    .putInt("current_theme", R.style.light)
                    .apply();
        }

        recreate();
    }

    /**
     * 当用户在列表中对某个笔记执行长按时调用此方法。NotesList 在其 ListView 中注册了自己作为上下文菜单的处理程序（在 onCreate() 中完成）。
     * <p>
     * 唯一可用的选项是 COPY 和 DELETE。
     * <p>
     * 长按等同于右键单击。
     *
     * @param menu     要添加菜单项的 ContextMenu 对象。
     * @param view     当前视图，正在构建上下文菜单。
     * @param menuInfo 与视图相关的数据。
     * @throws ClassCastException 如果转换失败，则抛出异常。
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {

        // 获取菜单项的数据。
        AdapterView.AdapterContextMenuInfo info;

        // 尝试获取在 ListView 中长按的项的位置。
        try {
            // 将传入的数据对象转换为适用于 AdapterView 对象的类型。
            info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        } catch (ClassCastException e) {
            // 如果菜单对象无法转换，记录错误。
            Log.e(TAG, "bad menuInfo", e);
            return;
        }

        /*
         * 获取与选定位置的项相关联的数据。getItem() 返回 ListView 的适配器与该项关联的内容。
         * 在 NotesList 中，适配器将所有笔记数据与其列表项关联。因此，getItem() 返回的是一个 Cursor。
         */
        Cursor cursor = (Cursor) getListAdapter().getItem(info.position);

        // 如果 cursor 为空，则说明适配器无法从提供者获取数据，返回 null。
        if (cursor == null) {
            // 如果由于某些原因无法获取请求的项，则不执行任何操作。
            return;
        }

        // 从 XML 资源中加载菜单
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_context_menu, menu);

        // 设置菜单标题为选中笔记的标题。
        menu.setHeaderTitle(cursor.getString(COLUMN_INDEX_TITLE));

        // 向菜单项中添加任何其他可以处理此数据的活动。此操作会查询系统中任何实现了 ALTERNATIVE_ACTION 的活动，
        // 并为每个找到的活动添加一个菜单项。
        Intent intent = new Intent(null, Uri.withAppendedPath(getIntent().getData(),
                Integer.toString((int) info.id)));
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
                new ComponentName(this, NotesList.class), null, intent, 0, null);
    }

    /**
     * 当用户从上下文菜单中选择一个项时调用此方法（见 onCreateContextMenu()）。
     * 唯一处理的菜单项是 DELETE 和 COPY。其他任何项都是替代选项，应该进行默认处理。
     *
     * @param item 选择的菜单项
     * @return 如果菜单项是 DELETE，则返回 true，表示不需要进行默认处理；否则返回 false，触发默认处理。
     * @throws ClassCastException 如果转换失败，则抛出异常。
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // 获取菜单项的数据。
        AdapterView.AdapterContextMenuInfo info;

        /*
         * 获取菜单项的额外信息。当在 Notes 列表中长按某个笔记时，会显示上下文菜单。
         * 菜单项自动获取与长按笔记相关联的数据。这些数据来自支持列表的提供者。
         *
         * 笔记的数据会通过 ContextMenuInfo 对象传递到上下文菜单创建方法中。
         *
         * 当点击其中一个上下文菜单项时，相同的数据以及笔记 ID 会通过 item 参数传递到 onContextItemSelected() 方法中。
         */
        try {
            // 将菜单项的数据对象转换为适用于 AdapterView 对象的类型。
            info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException e) {
            // 如果对象无法转换，记录错误。
            Log.e(TAG, "bad menuInfo", e);

            // 触发默认处理菜单项的操作。
            return false;
        }

        // 将选中的笔记 ID 附加到传入 Intent 的 URI 中。
        Uri noteUri = ContentUris.withAppendedId(getIntent().getData(), info.id);

        /*
         * 获取菜单项的 ID 并与已知的操作进行比较。
         */
        switch (item.getItemId()) {
            case R.id.context_open:
                // 启动活动以查看/编辑当前选中的项
                startActivity(new Intent(Intent.ACTION_EDIT, noteUri));
                return true;
//BEGIN_INCLUDE(copy)
            case R.id.context_copy:
                // 获取剪贴板服务的句柄。
                ClipboardManager clipboard = (ClipboardManager)
                        getSystemService(Context.CLIPBOARD_SERVICE);

                // 将笔记的 URI 复制到剪贴板。实际上，这会复制笔记本身。
                clipboard.setPrimaryClip(ClipData.newUri(   // 新的剪贴板项目，保存 URI
                        getContentResolver(),               // 用于获取 URI 信息的解析器
                        "Note",                             // 剪贴板项的标签
                        noteUri)                            // URI
                );

                // 返回给调用者并跳过进一步处理。
                return true;
//END_INCLUDE(copy)
            case R.id.context_delete:

                // 从提供者中删除笔记，传递一个笔记 ID 格式的 URI。
                // 请参阅开头的说明，关于在 UI 线程上执行提供者操作。
                getContentResolver().delete(
                        noteUri,  // 提供者的 URI
                        null,     // 不需要 WHERE 子句，因为只传递单个笔记 ID
                        null      // 没有 WHERE 子句，因此不需要 WHERE 参数
                );

                // 返回给调用者并跳过进一步处理。
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    /**
     * 当用户点击显示列表中的笔记时调用此方法。
     * <p>
     * 该方法处理传入的动作，可能是 PICK（从提供者获取数据）或 GET_CONTENT（获取或创建数据）。
     * 如果传入的动作是 EDIT，该方法会发送一个新的 Intent 启动 NoteEditor。
     *
     * @param l        包含被点击项的 ListView
     * @param v        单个项的视图
     * @param position 项在显示列表中的位置
     * @param id       被点击项的行 ID
     */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        // 根据传入的 URI 和行 ID 构建一个新的 URI
        Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);

        // 获取传入 Intent 的动作
        String action = getIntent().getAction();

        // 处理请求笔记数据的情况
        if (Intent.ACTION_PICK.equals(action) || Intent.ACTION_GET_CONTENT.equals(action)) {

            // 设置结果并返回给调用此 Activity 的组件。结果包含新的 URI
            setResult(RESULT_OK, new Intent().setData(uri));
        } else {

            // 发送一个 Intent 启动一个可以处理 ACTION_EDIT 动作的 Activity。
            // Intent 的数据是笔记 ID URI。实际效果是启动 NoteEdit。
            startActivity(new Intent(Intent.ACTION_EDIT, uri));
        }
    }


}
