package com.github.jihch.javacv_test_first;

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
		Smoother.smooth(filename);
		
	}
}
