/*
 * Copyright (C) 2009 The Android Open Source Project
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

import android.app.Activity;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.os.Bundle;
import android.provider.LiveFolders;

/**
 * 该 Activity 创建一个实时文件夹的 Intent，并将其发送回主屏幕（HOME）。
 * 从 Intent 中的数据，主屏幕创建一个实时文件夹，并在主屏幕视图中显示其图标。
 * 当用户点击该图标时，主屏幕使用从 Intent 获取的数据来从内容提供者检索信息并显示在视图中。
 *
 * 该 Activity 的意图过滤器设置为 ACTION_CREATE_LIVE_FOLDER，
 * 这是主屏幕在长按并选择实时文件夹时发送的。
 */
public class NotesLiveFolder extends Activity {

    /**
     * 所有工作都在 onCreate() 方法中完成。该 Activity 不会实际显示 UI。
     * 它会设置一个 Intent，并将其返回给调用者（主屏幕 Activity）。
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
         * 获取传入的 Intent 及其 action。如果传入的 Intent 是
         * ACTION_CREATE_LIVE_FOLDER，则创建一个包含必要数据的输出 Intent，
         * 然后返回 OK。否则，返回 CANCEL。
         */
        final Intent intent = getIntent();
        final String action = intent.getAction();

        if (LiveFolders.ACTION_CREATE_LIVE_FOLDER.equals(action)) {

            // 创建一个新的 Intent。
            final Intent liveFolderIntent = new Intent();

            /*
             * 以下语句将数据放入输出的 Intent 中。详细说明请参考
             * {@link android.provider.LiveFolders}。主屏幕根据这些数据设置实时文件夹。
             */
            // 设置内容提供者的 URI 模式，支持该文件夹。
            liveFolderIntent.setData(NotePad.Notes.LIVE_FOLDER_URI);

            // 将实时文件夹的显示名称作为额外的字符串数据添加。
            String foldername = getString(R.string.live_folder_name);
            liveFolderIntent.putExtra(LiveFolders.EXTRA_LIVE_FOLDER_NAME, foldername);

            // 将实时文件夹的显示图标作为额外资源添加。
            ShortcutIconResource foldericon =
                    Intent.ShortcutIconResource.fromContext(this, R.drawable.live_folder_notes);
            liveFolderIntent.putExtra(LiveFolders.EXTRA_LIVE_FOLDER_ICON, foldericon);

            // 添加实时文件夹的显示模式（整数）。指定的模式使文件夹显示为列表。
            liveFolderIntent.putExtra(
                    LiveFolders.EXTRA_LIVE_FOLDER_DISPLAY_MODE,
                    LiveFolders.DISPLAY_MODE_LIST);

            /*
             * 为实时文件夹列表中的项添加基本操作（Intent）。当用户点击列表中的某个笔记时，
             * 实时文件夹会触发此 Intent。
             *
             * 该 Intent 的 action 为 ACTION_EDIT，因此触发笔记编辑器 Activity。
             * 它的数据为单个笔记的 URI 模式，并且实时文件夹会自动将所选项的 ID 值
             * 添加到 URI 模式中。
             *
             * 结果是触发笔记编辑器，并通过 ID 获取单个笔记。
             */
            Intent returnIntent
                    = new Intent(Intent.ACTION_EDIT, NotePad.Notes.CONTENT_ID_URI_PATTERN);
            liveFolderIntent.putExtra(LiveFolders.EXTRA_LIVE_FOLDER_BASE_INTENT, returnIntent);

            /* 创建一个 ActivityResult 对象，并将其结果设置为 OK，
             * 返回之前创建的 live folder Intent。
             */
            setResult(RESULT_OK, liveFolderIntent);

        } else {

            // 如果原始 action 不是 ACTION_CREATE_LIVE_FOLDER，创建一个
            // ActivityResult，并将结果设置为 CANCELED，但不返回 Intent。
            setResult(RESULT_CANCELED);
        }

        // 结束该 Activity。该 Activity 对象将被返回给调用者。
        finish();
    }
}
