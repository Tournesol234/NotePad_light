package com.example.android.notepad;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MyCursorAdapter extends SimpleCursorAdapter {

    public MyCursorAdapter(Context context, int layout, Cursor c,
                           String[] from, int[] to) {
        super(context, layout, c, from, to);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        super.bindView(view, context, cursor);

        // 设置背景颜色
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

        // 设置日期
        TextView dateView = (TextView) view.findViewById(R.id.tv_date);
        if (dateView != null) {
            long timestamp = cursor.getLong(cursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String formattedDate = sdf.format(new Date(timestamp));
            dateView.setText(formattedDate);
        }

        // 设置分类图标
        String category = cursor.getString(cursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_CATEGORY));
        ImageView categoryIcon = (ImageView) view.findViewById(R.id.iv_category);

        if (category != null) {
            switch (category) {
                case "学习":
                    categoryIcon.setImageResource(R.drawable.ic_category_study);  // 替换成你自己的图标资源
                    break;
                case "生活":
                    categoryIcon.setImageResource(R.drawable.ic_category_life);   // 替换成你自己的图标资源
                    break;
                case "任务":
                    categoryIcon.setImageResource(R.drawable.ic_category_task);  // 替换成你自己的图标资源
                    break;
                default:
                    categoryIcon.setImageResource(R.drawable.ic_category_task);  // 默认图标
                    break;
            }
        }
    }
}