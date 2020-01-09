package com.github.jihch;

import static org.bytedeco.opencv.global.opencv_calib3d.Rodrigues;
import static org.bytedeco.opencv.global.opencv_imgproc.CV_AA;
import static org.bytedeco.opencv.global.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.opencv.global.opencv_imgproc.CV_CHAIN_APPROX_SIMPLE;
import static org.bytedeco.opencv.global.opencv_imgproc.CV_RETR_LIST;
import static org.bytedeco.opencv.global.opencv_imgproc.CV_THRESH_BINARY;
import static org.bytedeco.opencv.global.opencv_imgproc.approxPolyDP;
import static org.bytedeco.opencv.global.opencv_imgproc.arcLength;
import static org.bytedeco.opencv.global.opencv_imgproc.cvtColor;
import static org.bytedeco.opencv.global.opencv_imgproc.drawContours;
import static org.bytedeco.opencv.global.opencv_imgproc.fillConvexPoly;
import static org.bytedeco.opencv.global.opencv_imgproc.findContours;
import static org.bytedeco.opencv.global.opencv_imgproc.rectangle;
import static org.bytedeco.opencv.global.opencv_imgproc.threshold;
import static org.bytedeco.opencv.global.opencv_imgproc.warpPerspective;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.indexer.DoubleIndexer;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameRecorder;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.RectVector;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;

public class FaceRecognizerInVideo {
	public static void main(String[] args) throws IOException {
		if (args.length < 1) {
			System.err.println("one parameters are required to run this program, that is the analized video.");
			System.exit(1);
		}
		String videoFileName = args[0];
		File videoFile = new File(videoFileName);
		if (!videoFile.exists()) {
			System.err.println("video file not exists.");
			System.exit(1);
		}

		URL url = FaceRecognizerInVideo.class.getClass().getResource("/haarcascade_frontalface_alt.xml");
		File file = Loader.extractResource(url, null, "classifier", ".xml");
		file.deleteOnExit();
		String classifierName = file.getAbsolutePath();
		CascadeClassifier classifier = new CascadeClassifier(classifierName);
		if (classifier == null) {
			System.err.println("Error loading classifier file \"" + classifierName + "\".");
			System.exit(1);
		}

		OpenCVFrameGrabber grabber = OpenCVFrameGrabber.createDefault(videoFile);
		grabber.start();

		OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
		Frame firstFrame = grabber.grab();
		Mat grabbedImage = converter.convert(firstFrame);

		int height = grabbedImage.rows();
		int width = grabbedImage.cols();

		Mat grayImage = new Mat(height, width, opencv_core.CV_8UC1);
		Mat rotatedImage = grabbedImage.clone();

		FrameRecorder recorder = FrameRecorder.createDefault("output.avi", width, height);
		recorder.start();

		CanvasFrame frame = new CanvasFrame("Some Title", CanvasFrame.getDefaultGamma() / grabber.getGamma());
		Mat randomR = new Mat(3, 3, opencv_core.CV_64FC1), randomAxis = new Mat(3, 1, opencv_core.CV_64FC1);
		DoubleIndexer Ridx = randomR.createIndexer(), axisIdx = randomAxis.createIndexer();
		axisIdx.put(0, (Math.random() - 0.5) / 4, (Math.random() - 0.5) / 4, (Math.random() - 0.5) / 4);
		Rodrigues(randomAxis, randomR);
		double f = (width + height) / 2.0;
		Ridx.put(0, 2, Ridx.get(0, 2) * f);
		Ridx.put(1, 2, Ridx.get(1, 2) * f);
		Ridx.put(2, 0, Ridx.get(2, 0) / f);
		Ridx.put(2, 1, Ridx.get(2, 1) / f);

		Point hatPoints = new Point(3);

		while (true) {
			boolean frameIsVisible = frame.isVisible();

			Frame grab = grabber.grab();
			boolean grabIsNull = (grab == null);

			grabbedImage = converter.convert(grab);
			boolean grabbedImageIsNull = (grabbedImage == null);

			if (!frameIsVisible || grabbedImageIsNull) {
				break;
			}

			// Let's try to detect some faces! but we need a grayscale image...
			cvtColor(grabbedImage, grayImage, CV_BGR2GRAY);

			RectVector faces = new RectVector();
			classifier.detectMultiScale(grayImage, faces);

			long total = faces.size();
			for (long i = 0; i < total; i++) {
				Rect r = faces.get(i);
				int x = r.x(), y = r.y(), w = r.width(), h = r.height();
				rectangle(grabbedImage, new Point(x, y), new Point(x + w, y + h), Scalar.RED, 1, CV_AA, 0);

				hatPoints.position(0).x(x - w / 10).y(y - h / 10);
				hatPoints.position(1).x(x + w * 11 / 10).y(y - h / 10);
				hatPoints.position(2).x(x + w / 2).y(y - h / 2);
				fillConvexPoly(grabbedImage, hatPoints.position(0), 3, Scalar.GREEN, CV_AA, 0);
			}

			// Let's find some contours! but first some thresholding...
			threshold(grayImage, grayImage, 64, 255, CV_THRESH_BINARY);

			// To check if an output argument is null we may call either isNull() or
			// equals(null).
			MatVector contours = new MatVector();
			findContours(grayImage, contours, CV_RETR_LIST, CV_CHAIN_APPROX_SIMPLE);
			long n = contours.size();
			for (long i = 0; i < n; i++) {
				Mat contour = contours.get(i);
				Mat points = new Mat();
				approxPolyDP(contour, points, arcLength(contour, true) * 0.02, true);
				drawContours(grabbedImage, new MatVector(points), -1, Scalar.BLUE);
			}

			warpPerspective(grabbedImage, rotatedImage, randomR, rotatedImage.size());

			Frame rotatedFrame = converter.convert(rotatedImage);
			frame.showImage(rotatedFrame);
			recorder.record(rotatedFrame);

		} // end while

		frame.dispose();
		recorder.stop();
		grabber.stop();

	}
}
