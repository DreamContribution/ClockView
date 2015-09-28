# 这是一个简单的表

我们利用最简单的 View 中的 API 来创建了一个以前经常见到的表。

使用说明:ClockView.java，attrs_clock.xml，strings.xml中的 string 数组导入工程，把 ClockView 放入你要显示的布局中即可使用。
在 attrs_clock.xml 中列出了每一个可以在 xml布局文件中使用的属性，具体对应关系可以查看 java 文件中的注释进行处理。

当实例化该View后，请调用getStart()方法让表进行实时更新时间操作。

PS：暂时没有对任何颜色进行相关的动态设置。