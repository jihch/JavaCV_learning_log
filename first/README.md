# 第一个例子 Smoother

把图片平滑处理，大概跟模糊化差不多

下载 javacv-platform-1.4.2-bin.zip ，解压, 用到的 jar 包 都在 javacv-platform-1.4.2-bin\javacv-bin\下

第一个例子 只把 

javacpp.jar

javacv.jar

opencv-windows-x86_64.jar

三个包加到环境变量里就行了



```java

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.*;

public class Smoother {
	public static void smooth(String filename) {
        Mat image = imread(filename);
        if (image != null) {
            GaussianBlur(image, image, new Size(3, 3), 0);
            imwrite(filename, image);
        }
    }
    
    public static void main(String[] args) {
		String filename = "C:\\Users\\Administrator\\Desktop\\20180812\\smooth_test.png";
		for (int i = 0; i < 1000; i++) {
			Smoother.smooth(filename);
		}
		
	}
}
```



给的demo里是 Smoother.smooth 了一次，处理 一次 人眼基本看不出来图片有什么差别，只有从文件属性的 大小容量上能看出来；我这里为了能 人眼分辨出来，循环处理了1000次，效果就非常明显了。



javacv-platform-1.4.2-bin\javacv-bin 里的jar 包，基本全都要加，即使 只加了上面的3个jar, 编译运行的时候，也还是会通过 javacv.jar 这个jar 里已经指定好的配置通过相对路径 在编译 和 运行的时候加载 其它jar包

所有jar包一共大概 500MB， 话说如果是Maven项目，还是 老老实实地 按 官方给的

```xml
		<dependency>
			<groupId>org.bytedeco</groupId>
			<artifactId>javacv-platform</artifactId>
			<version>1.4.2</version>
		</dependency>
```

加载依赖吧，总之，第一个sample 总算跑通了。

还是 这里的 [sample](https://github.com/bytedeco/javacv/tree/master/samples) 比较时新。