/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aplikasideteksiplat;

import com.gluonhq.charm.glisten.control.ProgressBar;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import static org.opencv.core.CvType.CV_8UC1;
import static org.opencv.core.CvType.CV_8UC3;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.MSER;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import static org.opencv.imgproc.Imgproc.FONT_HERSHEY_SIMPLEX;
import static org.opencv.imgproc.Imgproc.INTER_CUBIC;
import static org.opencv.imgproc.Imgproc.contourArea;
import static org.opencv.imgproc.Imgproc.resize;
import org.opencv.video.BackgroundSubtractorKNN;
import org.opencv.video.BackgroundSubtractorMOG2;
import org.opencv.video.Video;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

/**
 * FXML Controller class
 *
 * @author ARDI
 */
public class ProgramController implements Initializable {

    @FXML
    ImageView btnAddVideo, videoPanel, btnPlay, btnPause, btnSetting,
            btnMSER, btnROI, platim1, platim2, platim3, platim4, platim5;
    @FXML
    Label btnAddVideo2, txtNowPlaying, timeRun, timeTotal, platid1, platid2, platid3, platid4, platid5,
            txtQuality;
    @FXML
    ProgressBar progressBar;
    @FXML
    Pane paneSetting, paneHasil, qualityOp;
    @FXML
    AnchorPane parent;

    private DaemonThread myThread = null;
    int fps, postFrame = 0, time, second = 0, x, y, resolusi = 360;
    VideoCapture video = null;
    Mat frame = new Mat(), save = new Mat();
    MatOfByte mem = new MatOfByte();
    double aspectRatio, ratioTampil = 3.18, ratioframe = 1.763157;
    Rect roi;
    Mat backgroundImage = new Mat();
    double[][] backgroundImageArray;
    List<Kendaraan> kendaraan = new ArrayList<>();
    List<Kendaraan> kendaraanNow = new ArrayList<>();
    List<Point> point = new ArrayList<>();
    List<Point> pointBefore;
    int id = 0;
    boolean mserSet = true, roiSet = true;
    BackgroundSubtractorMOG2 bgSub = Video.createBackgroundSubtractorMOG2();
    String nameFile;

    JFrame jframe = new JFrame();
    JLabel jlCoba = new JLabel();
    MatOfByte mem2 = new MatOfByte();

    class DaemonThread implements Runnable {

        protected volatile boolean runnable = false;
        Calendar timeVideo = Calendar.getInstance();

        public DaemonThread() {
            timeVideo.set(Calendar.HOUR_OF_DAY, 0);
            timeVideo.set(Calendar.MINUTE, 0);
            timeVideo.set(Calendar.SECOND, second);
            jframe.setContentPane(jlCoba);
        }

        @Override
        public void run() {
            synchronized (this) {
                while (runnable) {
                    if (video.grab()) {
                        try {
                            video.retrieve(frame);
                            frame.copyTo(save);
                            if (roiSet) {
                                Imgproc.rectangle(frame, roi, new Scalar(0, 0, 255), 2, 0);
                                objectDetection(postFrame);
                            }
//                            Imgcodecs.imwrite("C:\\Users\\ARDI\\Desktop\\outputBG\\coba" + postFrame + ".jpg", frame);
                            resize(frame, frame, new Size(resolusi*ratioframe, resolusi));
                            Imgcodecs.imencode(".jpg", frame, mem);
                            java.awt.Image im = ImageIO.read(new ByteArrayInputStream(mem.toArray()));
                            BufferedImage buff = (BufferedImage) im;
                            Image image = SwingFXUtils.toFXImage(buff, null);
                            if (runnable == true) {
                                videoPanel.setImage(image);
//                                Mat submat = frame.submat(roi);
////                                if (postFrame == 89 || (postFrame >= 965 && postFrame <= 971))
//                                Imgcodecs.imwrite("C:\\Users\\ARDI\\Desktop\\outputBG\\coba" + postFrame + ".jpg", submat);
                                progressBar.setProgress(((double) (timeVideo.get(Calendar.MINUTE)*60 + 
                                        timeVideo.get(Calendar.SECOND)) / (double) time));
                                if (postFrame % fps == 0) {
                                    Platform.runLater(() -> {
                                        timeVideo.set(Calendar.SECOND, second);
                                        timeRun.setText(new SimpleDateFormat("HH:mm:ss").format(timeVideo.getTime()));
                                        if (second == 60)
                                            second = 0;
                                        second++;
                                    });
                                }
                            } else if (runnable == false) {
                                this.wait();
                            }
                        } catch (IOException | InterruptedException ex) {
                            System.out.println("Error");
                        }
                    } else {
                        videoPanel.setVisible(false);
                        btnAddVideo.setVisible(true);
                        btnAddVideo.setVisible(true);
                    }
                    postFrame++;
                }
            }
        }
    }

    @FXML
    private void addVideo() throws IOException {
        JFileChooser fc = new JFileChooser("D:\\Mata Kuliah\\Semester 8\\Tugas Akhir\\Data Video\\Pakai");
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Video Files", "mp4", "mov", "avi");
        fc.setFileFilter(filter);
        fc.setMultiSelectionEnabled(false);
        fc.setAcceptAllFileFilterUsed(false);
        int returnValue = fc.showOpenDialog(new JFrame());
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            paneSetting.setDisable(false);
            String path = fc.getSelectedFile().getAbsolutePath();
            nameFile = fc.getSelectedFile().getName();
            txtNowPlaying.setText("Now Playing: " + fc.getSelectedFile().getName());
            btnPlay.setDisable(false);
            Image img = new Image("/image/play.png");
            btnPlay.setImage(img);
            btnAddVideo.setVisible(false);
            btnAddVideo2.setVisible(false);
            videoPanel.setVisible(true);
            video = new VideoCapture(path);
            aspectRatio = video.get(Videoio.CAP_PROP_FRAME_WIDTH) / video.get(Videoio.CAP_PROP_FRAME_HEIGHT);
            fps = (int) video.get(Videoio.CV_CAP_PROP_FPS);
            time = (int) (video.get(Videoio.CV_CAP_PROP_FRAME_COUNT) / fps);
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, time);
            timeTotal.setText(new SimpleDateFormat("HH:mm:ss").format(calendar.getTime()));
            video.read(frame);
            frame.copyTo(save);
            roi = new Rect(180, 650, 1220, 310);
            Imgproc.cvtColor(backgroundImage, backgroundImage, Imgproc.COLOR_BGR2GRAY);
            Imgproc.GaussianBlur(backgroundImage, backgroundImage, new Size(5, 5), 0);
            if (roiSet) {
                Imgproc.rectangle(frame, roi, new Scalar(0, 0, 255), 2, 0);
            }
            resize(frame, frame, new Size(resolusi*ratioframe, resolusi));
            Imgcodecs.imencode(".jpg", frame, mem);
            java.awt.Image im = ImageIO.read(new ByteArrayInputStream(mem.toArray()));
            BufferedImage buff = (BufferedImage) im;
            Image image = SwingFXUtils.toFXImage(buff, null);
            videoPanel.setImage(image);
            setWindow(videoPanel);
        }
    }

    @FXML
    private void playVideo() {
        myThread = new DaemonThread();
        Thread t = new Thread(myThread);
        t.setDaemon(true);
        myThread.runnable = true;
        t.start();
        btnPlay.setVisible(false);
        btnPause.setVisible(true);
        paneHasil.setVisible(true);
    }

    @FXML
    private void btnPlayEntered() {
        if (!btnPlay.isDisabled()) {
            Image image = new Image("/image/play_entered.png");
            btnPlay.setImage(image);
        }
    }

    @FXML
    private void btnPlayExited() {
        if (!btnPlay.isDisabled()) {
            Image image = new Image("/image/play.png");
            btnPlay.setImage(image);
        }
    }

    @FXML
    private void btnPlayPressed() {
        if (!btnPlay.isDisabled()) {
            Image image = new Image("/image/play_pressed.png");
            btnPlay.setImage(image);
        }
    }

    @FXML
    private void pauseVideo() {
        myThread.runnable = false;
        btnPlay.setVisible(true);
        btnPause.setVisible(false);
    }

    @FXML
    private void btnPauseEntered() {
        Image image = new Image("/image/pause_entered.png");
        btnPause.setImage(image);
    }

    @FXML
    private void btnPauseExited() {
        Image image = new Image("/image/pause.png");
        btnPause.setImage(image);
    }

    @FXML
    private void btnPausePressed() {
        Image image = new Image("/image/pause_pressed.png");
        btnPause.setImage(image);
    }

    @FXML
    private void btnSetting() {
        if (paneSetting.isVisible()) {
            paneSetting.setVisible(false);
        } else {
            paneSetting.setVisible(true);
        }
    }

    @FXML
    private void btnMSER() {
        if (!mserSet) {
            Image image = new Image("/image/toggle_on.png");
            btnMSER.setImage(image);
            mserSet = true;
        } else {
            Image image = new Image("/image/toggle_off.png");
            btnMSER.setImage(image);
            mserSet = false;
        }
    }

    @FXML
    private void qualityPressed(){
        if (qualityOp.isVisible())
            qualityOp.setVisible(false);
        else
            qualityOp.setVisible(true);
    }
    
    @FXML
    private void pres144(){
        this.resolusi = 144;
        txtQuality.setText("144p");
    }
    
    @FXML
    private void pres240(){
        this.resolusi = 240;
        txtQuality.setText("240p");
    }
    
    @FXML
    private void pres360(){
        this.resolusi = 360;
        txtQuality.setText("360p");
    }
    
    @FXML
    private void pres480(){
        this.resolusi = 480;
        txtQuality.setText("480p");
    }
    
    @FXML
    private void pres720(){
        this.resolusi = 720;
        txtQuality.setText("720p");
    }
    
    @FXML
    private void btnROI() throws IOException {
        if (!roiSet) {
            Image image = new Image("/image/toggle_on.png");
            Mat show = new Mat();
            frame.copyTo(show);
            Imgproc.rectangle(show, roi, new Scalar(0, 0, 255), 2, 0);
            Imgcodecs.imencode(".jpg", show, mem);
            java.awt.Image im = ImageIO.read(new ByteArrayInputStream(mem.toArray()));
            BufferedImage buff = (BufferedImage) im;
            Image image1 = SwingFXUtils.toFXImage(buff, null);
            videoPanel.setImage(image1);
            btnROI.setImage(image);
            roiSet = true;
            Imgproc.cvtColor(backgroundImage, backgroundImage, Imgproc.COLOR_BGR2GRAY);
            Imgproc.GaussianBlur(backgroundImage, backgroundImage, new Size(5, 5), 0);
        } else {
            Image image = new Image("/image/toggle_off.png");
            Imgcodecs.imencode(".jpg", save, mem);
            java.awt.Image im = ImageIO.read(new ByteArrayInputStream(mem.toArray()));
            BufferedImage buff = (BufferedImage) im;
            Image image1 = SwingFXUtils.toFXImage(buff, null);
            videoPanel.setImage(image1);
            btnROI.setImage(image);
            roiSet = false;
        }
    }

    private void setWindow(ImageView iv) {
        Image img = iv.getImage();
        double w, h;
        double ratioX = iv.getFitWidth() / img.getWidth();
        double ratioY = iv.getFitHeight() / img.getHeight();
        double reducCoeff;
        if (ratioX >= ratioY) {
            reducCoeff = ratioY;
        } else {
            reducCoeff = ratioX;
        }
        w = img.getWidth() * reducCoeff;
        h = img.getHeight() * reducCoeff;
        iv.setX((iv.getFitWidth() - w) / 2);
        iv.setY((iv.getFitHeight() - h) / 2);
    }

    private void objectDetection(int post) {
        double minArea = 5000;
        Mat current = frame.submat(roi);
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.cvtColor(current, current, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(current, current, new Size(5, 5), 0);
        Mat difference = new Mat(current.rows(), current.cols(), CV_8UC1);
        Core.absdiff(current, backgroundImage, difference);
        Imgproc.blur(difference, difference, new Size(15, 15), new Point(-1, -1));
        Imgproc.threshold(difference, difference, 40, 255, Imgproc.THRESH_BINARY);
        Imgproc.findContours(difference, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        for (int i = 0; i < contours.size(); i++) {
            double area = contourArea(contours.get(i), false);
            if (area > minArea) {
                Rect r = Imgproc.boundingRect(contours.get(i));
                Imgproc.rectangle(frame, new Point(r.x + roi.x, r.y + roi.y), new Point(r.x + roi.x + r.width, r.y + roi.y + r.height), new Scalar(0, 0, 255), 2);
                double[] loc = {r.x + roi.x, r.y + roi.y, r.width, r.height};
                r.set(loc);
                Kendaraan k = new Kendaraan();
                if (kendaraan.isEmpty()) {
                    k = new Kendaraan(id, r, post);
                    kendaraanNow.add(k);
                    if (k.getRect().width < 75) {
                        k.setCategory("Motor");
                    } else {
                        k.setCategory("Mobil");
                    }
                    Imgproc.putText(frame, "id=" + k.getId(), new Point(k.getRect().x+0.5*k.getRect().width, k.getRect().y), Imgproc.FONT_HERSHEY_TRIPLEX, 2, new Scalar(39, 233, 241));
                    id++;
                } else {
                    for (int j = 0; j < kendaraan.size(); j++) {
                        if (isOverlap(r, kendaraan.get(j).getRect())) {
                            k = new Kendaraan(kendaraan.get(j).getId(), r, kendaraan.get(j).getPost());
                            kendaraanNow.add(k);
                            if (k.getRect().width < 75) {
                                k.setCategory("Motor");
                            } else {
                                k.setCategory("Mobil");
                            }
                            Imgproc.putText(frame, "id=" + k.getId(), new Point(k.getRect().x+0.5*k.getRect().width, k.getRect().y), Imgproc.FONT_HERSHEY_TRIPLEX, 2, new Scalar(39, 233, 241), 2);
                            break;
                        } else if (j == kendaraan.size() - 1 && !isOverlap(r, kendaraan.get(j).getRect())) {
                            k = new Kendaraan(id, r, post);
                            kendaraanNow.add(k);
                            if (k.getRect().width < 75) {
                                k.setCategory("Motor");
                            } else {
                                k.setCategory("Mobil");
                            }
                            Imgproc.putText(frame, "id=" + k.getId(), new Point(k.getRect().x+0.5*k.getRect().width, k.getRect().y), Imgproc.FONT_HERSHEY_TRIPLEX, 2, new Scalar(39, 233, 241), 2);
                            id++;
                        }
                    }
                }
//                }
                if (k.getPost() + 7 == post) {
                    try {
                        k.setPlat(compareHarrisWithMSER(save.submat(k.getRect()), k.getId()));
                    } catch (Exception e) {
                        Mat tdk = Imgcodecs.imread("C:\\Users\\ARDI\\Documents\\NetBeansProjects\\AplikasiDeteksiPlat\\src\\image\\tdkTerdeteksi.jpg");
                        k.setPlat(tdk);
                    }
                }
                if (k.getPlat() != null) {
//                    Imgcodecs.imwrite("C:\\Users\\ARDI\\Desktop\\output\\Video 4\\id = " + k.getId() + ".jpg", k.getPlat());
                    showPlat(k.getPlat(), k.getId());
                }
            }
        }
        if (post != 0 && !kendaraanNow.isEmpty()) {
            kendaraan = new ArrayList<>(kendaraanNow);
            kendaraanNow.clear();
        }
//        Imgcodecs.imwrite("C:\\Users\\ARDI\\Desktop\\outputBG\\coba " + post + ".jpg", frame);
        hierarchy.release();
    }

    private static boolean isOverlap(Rect r1, Rect r2) {
        Rectangle a = new Rectangle(new java.awt.Point(r1.x, r1.y), new Dimension(r1.width, r1.height));
        Rectangle b = new Rectangle(new java.awt.Point(r2.x, r2.y), new Dimension(r2.width, r2.height));
        return a.intersects(b);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    private static Mat detectHarris(Mat m, int id) {
        Mat image = new Mat();
        m.copyTo(image);
//        image.convertTo(image, image.type(), 1, 100);
        Mat imgray = new Mat();
        Imgproc.cvtColor(image, imgray, Imgproc.COLOR_RGB2GRAY);
        Mat dst, dst_norm, dst_norm_scaled;
        dst = Mat.zeros(imgray.size(), CvType.CV_32FC1);
        dst_norm = Mat.zeros(imgray.size(), CvType.CV_32FC1);
        dst_norm_scaled = Mat.zeros(imgray.size(), CvType.CV_32FC1);
        Imgproc.cornerHarris(imgray, dst, 2, 3, 0.01);
        Core.normalize(dst, dst_norm, 0, 255, Core.NORM_MINMAX, CvType.CV_32FC1, new Mat());
        Core.convertScaleAbs(dst_norm, dst_norm_scaled);
        for (int j = 0; j < dst_norm.rows(); j++) {
            for (int i = 0; i < dst_norm.cols(); i++) {
                if ((int) dst_norm.get(j, i)[0] > 50) {
                    Imgproc.circle(image, new Point(i, j), 1, new Scalar(0, 0, 255), 5, 2, 0);
                }
            }
        }
//        imshow(image, Integer.toString(id));
        return image;
    }

    private static Mat detectMSER(Mat m, int id) {
        Mat imgray = new Mat();
        Imgproc.cvtColor(m, imgray, Imgproc.COLOR_RGB2GRAY);
        Imgproc.GaussianBlur(imgray, imgray, new Size(5, 5), 0);
        MSER mser = MSER.create();
        mser.setMinArea(10);
        mser.setMaxArea(60);
        mser.setDelta(3);
        List<MatOfPoint> msers = new ArrayList<>();
        MatOfRect bboxes = new MatOfRect();
        mser.detectRegions(imgray, msers, bboxes);
        Mat imageMSER = Mat.zeros(imgray.rows(), imgray.cols(), CV_8UC1);
        List<List<Point>> v = new ArrayList<>();
        for (int i = 0; i < msers.size(); i++) {
            v.add(msers.get(i).toList());
        }
        double[] data = {255};
        for (int i = 0; i < v.size(); i++) {
            for (int j = 0; j < v.get(i).size(); j++) {
                int x = (int) v.get(i).get(j).x;
                int y = (int) v.get(i).get(j).y;
                imageMSER.put(y, x, data);
            }
        }
//        imshow(imageMSER, "mser");
        return imageMSER;
    }

    private Mat compareHarrisWithMSER(Mat m, int id) {
//        Imgcodecs.imwrite("C:\\Users\\ARDI\\Desktop\\coba.jpg", m);
        Mat harris = detectHarris(m, id);
        Mat mser = detectMSER(m, id);
        Mat imgray = new Mat();
        Imgproc.cvtColor(m, imgray, Imgproc.COLOR_RGB2GRAY);
        Mat mask = Mat.zeros(imgray.size(), CV_8UC1);
        double[] color = {0, 0, 255};
        double[] white = {255};
        for (int i = 0; i < imgray.rows(); i++) {
            for (int j = 0; j < imgray.cols(); j++) {
                if (harris.get(i, j)[0] == color[0]
                        && harris.get(i, j)[1] == color[1]
                        && harris.get(i, j)[2] == color[2]) {
                    mask.put(i, j, white);
                }
            }
        }
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarcy = new Mat();
        Imgproc.findContours(mask, contours, hierarcy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        List<Rect> listRect = new ArrayList<>();
        double maxArea = Imgproc.contourArea(contours.get(0));
        int index = 0;
        for (int i = 0; i < contours.size(); i++) {
            double area = Imgproc.contourArea(contours.get(i));
            if (maxArea < area) {
                maxArea = area;
                index = i;
            }
        }
        Rect r = Imgproc.boundingRect(contours.get(index));
        if (mserSet){
            if (containMSER(mser.submat(r))) {
                listRect.add(r);
            }
        }else{
            listRect.add(r);
        }
        contours.clear();
        Mat matplat = new Mat();
        for (int j = 0; j < listRect.size(); j++) {
            Rect plat = listRect.get(j);
            try {
                
            } catch (Exception e) {
                
            }
            if (mserSet) {
                matplat = selectionMSER(m.submat(plat));
            } else {
                matplat = m.submat(plat);
            }
        }
        resize(matplat, matplat, new Size(matplat.width()*0.892857, matplat.height()*0.578947));
        return matplat;
    }

    private static Mat compareHarrisWithoutMSER(Mat m, String cat) {
        Mat harris = detectHarris(m, 1);
        Mat imgray = new Mat();
        Imgproc.cvtColor(m, imgray, Imgproc.COLOR_RGB2GRAY);
        Mat mask = new Mat(imgray.size(), CV_8UC1);
        double[] color = {0, 0, 255};
        for (int i = 0; i < imgray.rows(); i++) {
            for (int j = 0; j < imgray.cols(); j++) {
                if (harris.get(i, j)[0] == color[0]
                        && harris.get(i, j)[1] == color[1]
                        && harris.get(i, j)[2] == color[2]) {
                    mask.put(i, j, imgray.get(i, j));
                }
            }
        }
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarcy = new Mat();
        Imgproc.findContours(mask, contours, hierarcy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        List<Rect> listRect = new ArrayList<>();
        double maxArea = Imgproc.contourArea(contours.get(0));
        int index = 0;
        for (int i = 0; i < contours.size(); i++) {
            double area = Imgproc.contourArea(contours.get(i));
            if (maxArea < area) {
                maxArea = area;
                index = i;
            }
        }
        Rect r = Imgproc.boundingRect(contours.get(index));
        listRect.add(r);
        contours.clear();
        int size = listRect.size();
        for (int i = 0; i < size; i++) {
            for (int j = 1; j < size - 1; j++) {
                if (isOverlap(listRect.get(i), listRect.get(j))) {
                    Rect rnew = getRectNew(listRect.get(i), listRect.get(j));
                    listRect.add(rnew);
                }
            }
        }
        Mat matplat = new Mat();
        for (int j = 0; j < listRect.size(); j++) {
            Rect plat = listRect.get(j);
            double[] vals = {plat.x - 20, plat.y, plat.width + 45, plat.height - 5};
            plat.set(vals);
            matplat = m.submat(plat);
        }
        return matplat;
    }

    private static boolean containMSER(Mat mat) {
        for (int i = 0; i < mat.rows(); i++) {
            for (int j = 0; j < mat.cols(); j++) {
                if (mat.get(i, j)[0] == 255) {
                    return true;
                }
            }
        }
        return false;
    }

    private static Rect getRectNew(Rect r1, Rect r2) {
        int xmin, ymin, xmax, ymax;
        if (r1.x < r2.x) {
            xmin = r1.x;
        } else {
            xmin = r2.x;
        }
        if (r1.y < r2.y) {
            ymin = r1.y;
        } else {
            ymin = r2.y;
        }
        if (r1.x + r1.width < r2.x + r2.width) {
            xmax = r2.x + r2.width;
        } else {
            xmax = r1.x + r1.width;
        }
        if (r1.y + r1.height < r2.y + r2.height) {
            ymax = r2.y + r2.height;
        } else {
            ymax = r1.y + r1.height;
        }
        return new Rect(new Point(xmin, ymin), new Point(xmax, ymax));
    }

    public void showPlat(Mat mat, int id) {
        Platform.runLater(() -> {try {
            Imgcodecs.imencode(".jpg", mat, mem2);
            java.awt.Image im = ImageIO.read(new ByteArrayInputStream(mem2.toArray()));
            BufferedImage buff = (BufferedImage) im;
            Image image = SwingFXUtils.toFXImage(buff, null);
            switch ((id + 1) % 5) {
                case 1:
                    platim1.setImage(image);
                    platid1.setText("id = " + id);
                    setWindow(platim1);
                    platim2.setImage(null);
                    platid2.setText(null);
                    platim3.setImage(null);
                    platid3.setText(null);
                    platim4.setImage(null);
                    platid4.setText(null);
                    platim5.setImage(null);
                    platid5.setText(null);
                    break;
                case 2:
                    platim2.setImage(image);
                    platid2.setText("id = " + id);
                    setWindow(platim2);
                    break;
                case 3:
                    platim3.setImage(image);
                    platid3.setText("id = " + id);
                    setWindow(platim3);
                    break;
                case 4:
                    platim4.setImage(image);
                    platid4.setText("id = " + id);
                    setWindow(platim4);
                    break;
                case 0:
                    platim5.setImage(image);
                    platid5.setText("id = " + id);
                    setWindow(platim5);
                    break;
                default:
                    break;
            }
        } catch (IOException ex) {
            System.out.println("plat tidak terdeteksi");
        }});
    }

    public static void imshow(Mat mat, String string) {
        JFrame jframe = new JFrame(string);
        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JLabel jlabel = new JLabel();
        ImageIcon icon = new ImageIcon(Mat2BufferedImage(mat));
        jframe.setContentPane(jlabel);
        jlabel.setIcon(icon);
        jframe.setSize(mat.width(), mat.height());
        jframe.show();
    }

    public static BufferedImage Mat2BufferedImage(Mat m) {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (m.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        int bufferSize = m.channels() * m.cols() * m.rows();
        byte[] b = new byte[bufferSize];
        m.get(0, 0, b); // get all the pixels
        BufferedImage image = new BufferedImage(m.cols(), m.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(b, 0, targetPixels, 0, b.length);
        return image;
    }

    private Mat selectionMSER(Mat mser) {
        Mat mat = new Mat();
        Mat asli = new Mat();
        mser.copyTo(asli);
        Imgproc.cvtColor(mser, mser, Imgproc.COLOR_RGB2GRAY);
        Imgproc.threshold(mser, mat, 70, 255, Imgproc.THRESH_BINARY);
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarcy = new Mat();
        Imgproc.findContours(mat, contours, hierarcy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        double max = Imgproc.contourArea(contours.get(0));
        int ind = 0;
        for (int i = 0; i < contours.size(); i++) {
            if (max < Imgproc.contourArea(contours.get(i))) {
                max = Imgproc.contourArea(contours.get(i));
                ind = i;
            }
        }
        return asli;
    }
}
