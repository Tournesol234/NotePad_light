![image](https://github.com/user-attachments/assets/e98f6fae-1444-4bed-bc45-c5b036c2fda1)## 配置详情

### 1. `使用JDK9的版本`

### 2. `gradle-wrapper.properties`6.7.1

### 3.  `build.gradle(notepad)`
classpath请使用3.4.0:

##基本功能1：时间戳

![image](https://github.com/user-attachments/assets/9b0ba41d-971c-473f-a413-97e2f421f00d)

# 1.在noteslist_item.xml中添加一个textview
    <TextView
    android:id="@+id/tv_date"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:textSize="18sp"
    android:textColor="#009688"
    android:layout_marginBottom="16dp"
    />

# 2.在NoteList中添加修改时间的列

      private static final String[] PROJECTION = new String[] {
            NotePad.Notes._ID, // 0
            NotePad.Notes.COLUMN_NAME_TITLE, // 1
            NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE, // 2
    };

# 3.OnCreate方法中，初始化id

    int[] viewIDs = {R.id.tv_title,R.id.tv_date};  

![image](https://github.com/user-attachments/assets/32cba258-00fd-40dc-b105-31fd45fe4db2)

# 4.创建个格式化时间的方法
``adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
`            @Override
          public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (view.getId() == R.id.tv_date) {

                    long timestamp = cursor.getLong(columnIndex);

                    // 格式化时间
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    String formattedDate = sdf.format(new Date(timestamp));


                   ((TextView) view).setText(formattedDate);
                   return true;
                }
                 return false;
            }
        });```
##基本功能2：搜索（通过标题和内容）

![image](https://github.com/user-attachments/assets/45e2b0aa-9e55-4dde-af0d-9f33bb4f8f9f)

![image](https://github.com/user-attachments/assets/75ecbee6-38b4-47b4-bc9c-ace7e5f363d9)

# 1.list_options_menu.xml中新添加一个item
    <item
        android:id="@+id/search"
        android:title="@string/search"
        android:icon="@drawable/ic_menu_search"
        android:showAsAction="always"
        android:actionViewClass="android.widget.SearchView" />

# 2.OnCreateOptionMenu 中编写搜索监听器并调用query的方法
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
#  3.设置模糊查询的代码

![image](https://github.com/user-attachments/assets/5ddf4262-14dc-47e4-b60a-9b51788a5f3e)

#  4.provider里面的query方法再加上模糊查询内容和标题的代码

![image](https://github.com/user-attachments/assets/0bc667b4-9717-4213-92e5-2a64c6ec9c14)

##拓展功能1：UI界面美化

# 1.设计溢出菜单的颜色

     <style name="OverflowButtonStyle" parent="@android:style/Widget.Holo.ActionButton.Overflow">
    <!--        <item name="android:src">@drawable/ic_menu_menu</item>  -->
            <!-- 溢出菜单图标颜色-->
            <item name="android:layout_width">4dp</item>  <!-- 设置宽度 -->
            <item name="android:layout_height">4dp</item>  <!-- 设置高度 -->
            <item name="android:tint">@color/colorPrimary</item>  <!-- 设置颜色 -->
        </style>


# 2.设计亮暗两个布局，并分别在主题中引用溢出菜单样式

    <style name="light" parent="@android:style/Theme.Holo.Light">
      
        <item name="android:actionOverflowButtonStyle">@style/OverflowButtonStyle</item>
    </style>

    <style name="dark" parent="@android:style/Theme.Holo">
      
        <item name="android:actionOverflowButtonStyle">@style/OverflowButtonStyle</item>
    </style>

# 3.设计主题菜单
    <item
        android:id="@+id/theme"
        android:title="切换主题"
        android:icon="@drawable/ic_menu_dark"
    android:showAsAction="always"/>

# 4.在OnCreate里设置默认主题等
    int currentTheme = getSharedPreferences("prefs", MODE_PRIVATE)
    .getInt("current_theme", R.style.light); // 默认亮色主题
    setTheme(currentTheme); // 在 setContentView 之前设置主题
    super.onCreate(savedInstanceState);
    
            setContentView(R.layout.noteslist_item);
#  5.在OncreateOptionMenu里设置图标切换
    MenuItem themeItem = menu.findItem(R.id.theme);
    // 根据当前主题设置图标
    int currentTheme = getSharedPreferences("prefs", MODE_PRIVATE)
    .getInt("current_theme", R.style.light); // 默认亮色主题

        if (currentTheme == R.style.light) {
            themeItem.setIcon(R.drawable.ic_menu_dark);  // 设置月亮图标（亮色模式）
        } else {
            themeItem.setIcon(R.drawable.ic_menu_light);   // 设置太阳图标（暗色模式）
        }
# 6.onOptionsItemSelected添加点击事件    

          case R.id.theme:
                
                toggleTheme(item); 
                return true;

         并定义切换的逻辑
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

![image](https://github.com/user-attachments/assets/bedeb714-0cbe-426b-9fb8-85689de5fd48)
![image](https://github.com/user-attachments/assets/413f66b6-0a55-4469-ac4c-89c87dd937b4)

# 7契约类添加颜色字段
       
    public static final String COLUMN_NAME_BACK_COLOR = "color";
        
创建数据库表地方添加颜色的字段：

         @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + NotePad.Notes.TABLE_NAME + "   ("
        + NotePad.Notes._ID + " INTEGER PRIMARY KEY,"
        + NotePad.Notes.COLUMN_NAME_TITLE + " TEXT,"
        + NotePad.Notes.COLUMN_NAME_NOTE + " TEXT,"
        + NotePad.Notes.COLUMN_NAME_CREATE_DATE + " INTEGER,"
        + NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE + " INTEGER,"
        + NotePad.Notes.COLUMN_NAME_BACK_COLOR + " INTEGER" //颜色
        + ");");
       }

把颜色定义为INTEGER
    
    public static final int DEFAULT_COLOR = 0; //白
    public static final int YELLOW_COLOR = 1; //黄
    public static final int BLUE_COLOR = 2; //蓝
    public static final int GREEN_COLOR = 3; //绿
    public static final int RED_COLOR = 4; //红

由于数据库中多了一个字段，所以要在NotePadProvider中添加对其相应的处理，static{}中：
    
    sNotesProjectionMap.put(
    NotePad.Notes.COLUMN_NAME_BACK_COLOR,
    NotePad.Notes.COLUMN_NAME_BACK_COLOR);

insert中：
    
    if (values.containsKey(NotePad.Notes.COLUMN_NAME_BACK_COLOR) == false) {
    values.put(NotePad.Notes.COLUMN_NAME_BACK_COLOR, NotePad.Notes.DEFAULT_COLOR);
    }


# 8自定义一个CursorAdapter继承SimpleCursorAdapter，把格式化方法移到这里
    
    public class MyCursorAdapter extends SimpleCursorAdapter {


    public MyCursorAdapter(Context context, int layout, Cursor c,
                           String[] from, int[] to) {
        super(context, layout, c, from, to);
    }


    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        super.bindView(view, context, cursor);


        int x = cursor.getInt(cursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_BACK_COLOR));
        switch (x) {
            case NotePad.Notes.DEFAULT_COLOR:
                view.setBackgroundColor(Color.rgb(255, 255, 255));
                break;
            case NotePad.Notes.YELLOW_COLOR:
                view.setBackgroundColor(Color.rgb(247, 216, 133));
                break;
            case NotePad.Notes.BLUE_COLOR:
                view.setBackgroundColor(Color.rgb(165, 202, 237));
                break;
            case NotePad.Notes.GREEN_COLOR:
                view.setBackgroundColor(Color.rgb(161, 214, 174));
                break;
            case NotePad.Notes.RED_COLOR:
                view.setBackgroundColor(Color.rgb(244, 149, 133));
                break;
            default:
                view.setBackgroundColor(Color.rgb(255, 255, 255));
                break;
        }
        // 如果是日期 TextView，格式化时间并设置文本
        TextView dateView = (TextView) view.findViewById(R.id.tv_date);
        if (dateView != null) {
            long timestamp = cursor.getLong(cursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE));

            // 格式化时间
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String formattedDate = sdf.format(new Date(timestamp));

            // 设置日期文本
            dateView.setText(formattedDate);
        }
    }
}

# 9 NotesList中的PROJECTION添加颜色项：
    
    private static final String[] PROJECTION = new String[] {
    NotePad.Notes._ID, // 0
    NotePad.Notes.COLUMN_NAME_TITLE, // 1
    //扩展 显示时间 颜色
    NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE, // 2
    NotePad.Notes.COLUMN_NAME_BACK_COLOR,
    };
并且将NotesList中用的SimpleCursorAdapter改使用MyCursorAdapter：
    
    //修改为可以填充颜色的自定义的adapter，自定义的代码在MyCursorAdapter.java中
    adapter = new MyCursorAdapter(
    this,
    R.layout.noteslist_item,
    cursor,
    dataColumns,
    viewIDs
    );

在NoteEditor中的PROJECTION中添加颜色项：
    
    private static final String[] PROJECTION =
    new String[] {
    NotePad.Notes._ID,
    NotePad.Notes.COLUMN_NAME_TITLE,
    NotePad.Notes.COLUMN_NAME_NOTE,
    NotePad.Notes.COLUMN_NAME_BACK_COLOR
    };

onsume中添加
   
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

# 10.菜单文件添加一个图标

                <item android:id="@+id/menu_color"
        android:title="@string/menu_color"
        android:icon="@drawable/ic_menu_color"
        android:showAsAction="always"/>

        在NoteEditor中找到onOptionsItemSelected()方法，在菜单的switch中添加：
    
    //换背景颜色选项
    case R.id.menu_color:
    changeColor();
    break;
    在NoteEditor中添加函数changeColor()：
    
    //跳转改变颜色的activity，将uri信息传到新的activity
    private final void changeColor() {
    Intent intent = new Intent(null,mUri);
    intent.setClass(NoteEditor.this,NoteColor.class);
    NoteEditor.this.startActivity(intent);
    }

新建布局note_color.xml
    
    <?xml version="1.0" encoding="utf-8"?>
    <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:orientation="horizontal" android:layout_width="match_parent"
    android:layout_height="match_parent">
    <ImageButton
    android:id="@+id/color_white"
    android:layout_width="0dp"
    android:layout_height="50dp"
    android:layout_weight="1"
    android:background="@color/colorWhite"
    android:onClick="white"/>
    <ImageButton
    android:id="@+id/color_yellow"
    android:layout_width="0dp"
    android:layout_height="50dp"
    android:layout_weight="1"
    android:background="@color/colorYellow"
    android:onClick="yellow"/>
    <ImageButton
    android:id="@+id/color_blue"
    android:layout_width="0dp"
    android:layout_height="50dp"
    android:layout_weight="1"
    android:background="@color/colorBlue"
    android:onClick="blue"/>
    <ImageButton
    android:id="@+id/color_green"
    android:layout_width="0dp"
    android:layout_height="50dp"
    android:layout_weight="1"
    android:background="@color/colorGreen"
    android:onClick="green"/>
    <ImageButton
    android:id="@+id/color_red"
    android:layout_width="0dp"
    android:layout_height="50dp"
    android:layout_weight="1"
    android:background="@color/colorRed"
    android:onClick="red"/>
    </LinearLayout>

创建NoteColorActivity

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

在AndroidManifest.xml中将这个Acitvity主题定义为对话框样式
    
    <!--换背景色-->
    <activity android:name="NoteColor"
    android:theme="@android:style/Theme.Holo.Light.Dialog"
    android:label="ChangeColor"
    android:windowSoftInputMode="stateVisible"/>

![image](https://github.com/user-attachments/assets/cbb23643-8b86-4e1f-8e31-87615b179e0e)

![image](https://github.com/user-attachments/assets/6ba77bba-eafd-42c0-af0c-489bdcc87aa0)

## 拓展功能2 排序

# 1在菜单文件list_options_menu.xml中添加：
    
    <item
    android:id="@+id/menu_sort"
    android:title="@string/menu_sort"
    android:icon="@android:drawable/ic_menu_sort_by_size"
    android:showAsAction="always" >
    <menu>
    <item
    android:id="@+id/menu_sort1"
    android:title="@string/menu_sort1"/>
    <item
    android:id="@+id/menu_sort2"
    android:title="@string/menu_sort2"/>
    <item
    android:id="@+id/menu_sort3"
    android:title="@string/menu_sort3"/>
    </menu>
    </item>

# 2在NotesList菜单switch下添加case：

    case R.id.menu_sort1:
    cursor = managedQuery(
    getIntent().getData(),            
    PROJECTION,                      
    null,                          
    null,                          
    NotePad.Notes._ID
    );
    adapter = new MyCursorAdapter(
    this,
    R.layout.noteslist_item,
    cursor,
    dataColumns,
    viewIDs
    );
    setListAdapter(adapter);
    return true;
    //修改时间排序
    case R.id.menu_sort2:
    cursor = managedQuery(
    getIntent().getData(),          
    PROJECTION,                      
    null,                            
    null,                       
    NotePad.Notes.DEFAULT_SORT_ORDER
    );
    adapter = new MyCursorAdapter(
    this,
    R.layout.noteslist_item,
    cursor,
    dataColumns,
    viewIDs
    );
    setListAdapter(adapter);
    return true;
    //颜色排序
    case R.id.menu_sort3:
    cursor = managedQuery(
    getIntent().getData(),
    PROJECTION,      
    null,       
    null,       
    NotePad.Notes.COLUMN_NAME_BACK_COLOR
    );
    adapter = new MyCursorAdapter(
    this,
    R.layout.noteslist_item,
    cursor,
    dataColumns,
    viewIDs
    );
    setListAdapter(adapter);
    return true;

定义

    private MyCursorAdapter adapter;
    private Cursor cursor;
    private String[] dataColumns = { NotePad.Notes.COLUMN_NAME_TITLE ,  NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE } ;
    private int[] viewIDs = { R.id.tv_title , R.id.tv_date };

样式
![img_1.png](img_1.png)

按创建时间排序
![img_2.png](img_2.png)

修改时间
![img_3.png](img_3.png)

颜色
![img_4.png](img_4.png)