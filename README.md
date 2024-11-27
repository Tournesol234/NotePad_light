## 配置详情

### 1. `使用JDK9的版本`

### 2. `gradle-wrapper.properties`6.7.1

### 3.  `build.gradle(notepad)`
classpath请使用3.4.0:

基本功能1：时间戳
![image](https://github.com/user-attachments/assets/9b0ba41d-971c-473f-a413-97e2f421f00d)

          1.在noteslist_item.xml中添加一个textview
            ![image](https://github.com/user-attachments/assets/a963db00-fcc6-4139-9440-4686d1810f60)
          2.在NoteList中添加修改时间的列
          ![image](https://github.com/user-attachments/assets/d3415673-700e-46dd-bde7-8da45c34994c)
          3.OnCreate方法中，初始化id
          ![image](https://github.com/user-attachments/assets/32cba258-00fd-40dc-b105-31fd45fe4db2)
          4.创建个格式化时间的方法
          ![image](https://github.com/user-attachments/assets/14df91b2-ae79-4c67-81ac-c94089271557)

基本功能2：搜索（通过标题和内容）
![image](https://github.com/user-attachments/assets/45e2b0aa-9e55-4dde-af0d-9f33bb4f8f9f)
![image](https://github.com/user-attachments/assets/75ecbee6-38b4-47b4-bc9c-ace7e5f363d9)

          1.list_options_menu.xml中新添加一个item
         ![image](https://github.com/user-attachments/assets/7ab5ef2b-6532-4f55-ac6a-ca9419301ad9)
         2.OnCreateOptionMenu 中编写搜索监听器并调用query的方法
         ![image](https://github.com/user-attachments/assets/f3047f2e-5a61-4f74-b051-c640e9eb788e)
         3.设置模糊查询的代码
         ![image](https://github.com/user-attachments/assets/5ddf4262-14dc-47e4-b60a-9b51788a5f3e)
         4.provider里面的query方法再加上模糊查询内容和标题的代码
         ![image](https://github.com/user-attachments/assets/0bc667b4-9717-4213-92e5-2a64c6ec9c14)

拓展功能1：UI界面美化
