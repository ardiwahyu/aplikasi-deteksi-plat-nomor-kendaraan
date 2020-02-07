/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aplikasideteksiplat;

import org.opencv.core.Mat;
import org.opencv.core.Rect;

public class Kendaraan {
    private int id;
    private Rect rect;
    private int post;
    private String category;
    private Mat plat;
    
    public Kendaraan(){
    }
    
    public Kendaraan(int id, Rect rect, int post){
        this.id = id;
        this.rect = rect;
        this.post = post;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Rect getRect() {
        return rect;
    }

    public void setRect(Rect rect) {
        this.rect = rect;
    }

    public int getPost() {
        return post;
    }

    public void setPost(int post) {
        this.post = post;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Mat getPlat() {
        return plat;
    }

    public void setPlat(Mat plat) {
        this.plat = plat;
    }
}
