package photomosaik;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.opencv.*;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

public class mosaik {
	
	static int w = 96;
	static ArrayList<Mat> ilis = new ArrayList<Mat>();

	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		//rectify();
		//lookuptable_gs();
		//4961 x 7016
		mosaic();
		//Mat i1 = Highgui.imread("pics/gs/1.jpg",Highgui.CV_LOAD_IMAGE_GRAYSCALE);
		//Mat i2 = Highgui.imread("pics/gs/1.jpg",Highgui.CV_LOAD_IMAGE_GRAYSCALE);
		//double s = getSSIM_gs(i1,i2);
		
		
	}

	private static void mosaic() {
		
		ArrayList<ArrayList<Mat>> big_l = new ArrayList<ArrayList<Mat>>();
		for(int i = 0; i < 8 ; i++){			
			big_l.add(new ArrayList<Mat>());
		}
		for(int i = 1;i <=21654;i++){
			Mat load = Highgui.imread("pics/rect96/"+i+".jpg");
			int hash = getHash(load);
			big_l.get(hash).add(load);
		}
		/*for(int i = 0; i < 8 ; i++){			
			
			System.out.println(big_l.get(i).size());
		}*/
		
		Mat img = Highgui.imread("pics/side_cut.jpg");		
		int cols = img.cols();
		int rows = img.rows();
		int max_cols = (int) Math.floor(cols/w);
		int max_rows = (int) Math.floor(rows/w);
		Mat dst = new Mat(new Size(max_cols*w,max_rows*w), img.type());
		Mat white = Highgui.imread("pics/rect96/"+10610+".jpg");
		
		for(int i = 1; i*w < rows;i++){
			for(int j = 1; j*w < cols; j++){
				Mat sub_img = img.submat((i-1)*w, i*w, (j-1)*w, j*w);
				Scalar means = Core.mean(sub_img);						
				if (means.val[0] <= 254 || means.val[1] <= 254 || means.val[2] <= 254){
				Mat little_img = getNearestMSE(sub_img, big_l);
				little_img.copyTo(dst.submat((i-1)*w, i*w, (j-1)*w, j*w));
				//little_img.release();				
				sub_img.release();
				}
				else white.copyTo(dst.submat((i-1)*w, i*w, (j-1)*w, j*w));
				
				double state = ((double)((i-1)*max_cols + j))/((double)(max_rows*max_cols));
				System.out.println(state);
			}
		}
		Highgui.imwrite("side-96-dina1-300-raw.jpg", dst);
	}
	
	static int getHash(Mat img){
		int re = 0;
		Scalar means = Core.mean(img);
		int r = 0;
		int b = 0;
		int g = 0;
		if(means.val[0]>124) r = 1;
		if(means.val[1]>126) g = 1;
		if(means.val[2]>114) b = 1;
		re = r*1 + g*2 +b*4;
		//System.out.println(means.val[2]);
		return re;		
	}
	
	private static void mosaic_gs() {
		Mat img = Highgui.imread("pics/greyscale_big.jpg",Highgui.CV_LOAD_IMAGE_GRAYSCALE);	
		//21732
		for(int i = 1;i <=21732;i++){
			Mat load = Highgui.imread("pics/gs/"+i+".jpg",Highgui.CV_LOAD_IMAGE_GRAYSCALE);
			ilis.add(load);
		}
		int cols = img.cols();
		int rows = img.rows();
		int max_cols = (int) Math.floor(cols/w);
		int max_rows = (int) Math.floor(rows/w);
		Mat dst = new Mat(new Size(max_cols*w,max_rows*w), img.type());	
		Mat white = Highgui.imread("pics/gs/"+10649+".jpg",Highgui.CV_LOAD_IMAGE_GRAYSCALE);
		//ilis.add(0,white);
		
		for(int i = 1; i*w < rows;i++){
			for(int j = 1; j*w < cols; j++){
				Mat sub_img = img.submat((i-1)*w, i*w, (j-1)*w, j*w);
				Scalar means = Core.mean(sub_img);
				double m = means.val[0];
				//10649
				
				if (m <= 254){
				int nearest = getNearestMSE_gs(sub_img);
				Mat little_img = ilis.get(nearest);				
				little_img.copyTo(dst.submat((i-1)*w, i*w, (j-1)*w, j*w));
				ilis.remove(nearest);
				System.out.println(nearest);
				little_img.release();
				}
				else white.copyTo(dst.submat((i-1)*w, i*w, (j-1)*w, j*w));
				//little_img.release();
				
				
				sub_img.release();
				double state = ((double)((i-1)*max_cols + j))/((double)(max_rows*max_cols));
				System.out.println(state+ " "+ ilis.size());
			}
		}
		Highgui.imwrite("greyscale-big-mse3.jpg", dst);
	}	
	
	static String getNearest_gs(double m, List<File_tuple> l) {
		String fname = "";		
		ArrayList<String> lis = new ArrayList<String>();
		int index = Collections.binarySearch(l, new File_tuple("0",m));
		Random rand = new Random();
		int pos = (Math.abs(index-10)) + rand.nextInt(20);
		while(pos<0) pos++;
		while(pos>=l.size()) pos--;
		File_tuple n = l.get(pos);
		Double d1 = 5.0;
		Double d2 = 6.0;
		//System.out.println(n.name + " " + n.mean + " of " + pos);
		return n.name;
	}

	static void rectify(){
		
		double prog = 0.0;
		//<5434
		for(int i = 1; i <5434;i++){
			Mat img = Highgui.imread("pics/all/"+i+".jpg");	
			int cols = img.cols();
			int rows = img.rows();
			int width = 0;
			if(cols > rows){
				width = rows;				
			}
			else{
				width = cols;				
			}
			Mat out = img.submat(0,width,0,width);
			Mat rs = new Mat();
			Imgproc.resize( out, rs, new Size(96,96),0,0, Imgproc.INTER_AREA);
			//transpose+flip(1)=CW
			Mat cw = new Mat();
			Core.transpose(rs, cw);  
			Core.flip(cw, cw, 1);
			//transpose+flip(0)=CCW
			Mat ccw = new Mat();
			Core.transpose(rs, ccw);  
			Core.flip(ccw, ccw, 0);
			//flip(-1)=180 
			Mat ud = new Mat();
			Core.flip(rs, ud, -1);		
			
			Highgui.imwrite("pics/rect96/"+i+"-1.jpg", rs);
			Highgui.imwrite("pics/rect96/"+i+"-2.jpg", cw);
			Highgui.imwrite("pics/rect96/"+i+"-3.jpg", ccw);
			Highgui.imwrite("pics/rect96/"+i+"-4.jpg", ud);
			rs.release();
			cw.release();
			ccw.release();
			ud.release();
			out.release();
			img.release();
			if(i%10==0) {
				prog = i/5433.0;
				System.out.println(prog);}
			
		}
	}
	
	static void lookuptable(){
		
		
		try{
		    PrintWriter writer = new PrintWriter("lookup_gs.txt", "UTF-8");
		    for(int i = 1; i <5;i++){
				Mat img = Highgui.imread("pics/gs/"+i+".jpg",Highgui.CV_LOAD_IMAGE_GRAYSCALE);	
				Scalar means = Core.mean(img);
				String line = i +" " + means.val[0]+" " + means.val[1]+" " + means.val[2];
				System.out.println(line);
				writer.println(line);
				img.release();
				}
		    
		    writer.close();
		} catch (IOException e) {
		   // do something
		}
		
		
			
	}
	
@SuppressWarnings("unchecked")
	static void lookuptable_gs(){
		
	ArrayList<File_tuple> l = new ArrayList<File_tuple>();
		
		try{
		    PrintWriter writer = new PrintWriter("lookup_gs.txt", "UTF-8");
		    for(int i = 1; i <=21732;i++){
				Mat img = Highgui.imread("pics/gs/"+i+".jpg",Highgui.CV_LOAD_IMAGE_GRAYSCALE);	
				Scalar means = Core.mean(img);
				double m = means.val[0];
				File_tuple ft = new File_tuple(i+"",m);
				l.add(ft);
				
				img.release();
				}
		    Collections.sort(l);
		    
		    for(int i= 0; i < l.size();i++){
		    	File_tuple ft = l.get(i);
		    	String line = ft.mean + " " +ft.name;
				System.out.println(line);
				writer.println(line);
		    }
		    writer.close();
		} catch (IOException e) {
		   // do something
		}
		
		
			
}
	
	static Mat getNearestMSE(Mat img, ArrayList<ArrayList<Mat>> l){
		int hash = getHash(img);
		ArrayList<Mat> bucket = l.get(hash);
		if(bucket.size() <= 0) System.out.println("BUCKET EMPTY_________________________________________________");
		int re = 0;
		double min = getMSE(img,bucket.get(0));
		double mse = min;
		for(int i = 0; i < bucket.size(); i++){
			Mat sub = bucket.get(i);
			mse = getMSE(img,sub);
			if(mse<min){
				min = mse;
				re = i;
			}			
			//sub.release();
		}
		img.release();
		Mat reM = bucket.get(re);
		bucket.remove(reM);
		return reM;
	}
	
	static int getNearestMSE_gs(Mat img){

		int re = 0;
		double min = getMSE_gs(img,ilis.get(0));
		double mse = min;
		for(int i = 0; i < ilis.size(); i++){			
			mse = getMSE_gs(img,ilis.get(i));
			if(mse<min){
				min = mse;
				re = i;
			}						
		}
		img.release();
		return re;
	}
	
	static int getNearestSSIM_gs(Mat img){

		int re = 0;
		double max = 0;
		double ssim = 0;
		for(int i = 0; i < ilis.size(); i++){			
			ssim = getSSIM_gs(img,ilis.get(i));
			if(ssim>max){
				max = ssim;
				re = i;
			}						
		}
		img.release();
		return re;
	}
	
	static double getMSE(Mat i1,Mat i2){
		double mse = 0;
		Mat d = new Mat();
		//System.out.println(i1.total());
		//System.out.println(i2.total());
		Core.absdiff(i1, i2, d);
		//Mat e = new Mat(i1.size(),CvType.CV_32F);
		d.convertTo(d, CvType.CV_32F); 
		
		//d.convertTo(d, CvType.CV_32F);  // cannot make a square on 8 bits
		Core.multiply(d, d, d);
		Scalar sum = Core.sumElems(d);
		mse = (sum.val[0] + sum.val[1] +sum.val[2])/(3*w*w);
		//e.release();
		d.release();
		//i1.release();
		//i2.release();
		return mse;
		
	}
	
	static double getMSE_gs(Mat i1,Mat i2){
		double mse = 0;
		Mat d = new Mat(i1.size(),CvType.CV_32F);
		
		//System.out.println(i2.total());
		Core.absdiff(i1, i2, d);
		Mat e = new Mat(i1.size(),CvType.CV_32F);
		d.convertTo(e, CvType.CV_32F);  // cannot make a square on 8 bits
		//System.out.println(d.get(0, 0)[0]);
		e = e.mul(e);
		//System.out.println(e.get(0, 0)[0]);
		Scalar sum = Core.sumElems(e);
		mse = sum.val[0]/(w*w);
		d.release();
		e.release();
		//i1.release();
		//i2.release();
		return mse;		
	}
	
	static double getSSIM_gs(Mat i1, Mat i2){
		double ssim = 0.0;
		
		i1.convertTo(i1, CvType.CV_32F);
		i2.convertTo(i2, CvType.CV_32F);
		//System.out.println(i1.get(4, 4)[0]);
		Mat i1_2 = i1.mul(i1);
		//System.out.println(i1_2.get(4, 4)[0]);
		Mat i2_2 = i2.mul(i2);
		Mat i1_i2 = i1.mul(i2);
		
		Mat mu1 = new Mat();
		Mat mu2 = new Mat();
		
		Imgproc.GaussianBlur(i1, mu1, new Size(11,11), 1.5);
		Imgproc.GaussianBlur(i2, mu2, new Size(11,11), 1.5);
		
		Mat mu1_2 = mu1.mul(mu1);
		Mat mu2_2 = mu2.mul(mu2);
		Mat mu1_mu2 = mu1.mul(mu2);
		
		Mat sigma1_2 = new Mat();
		Mat sigma2_2 = new Mat();
		Mat sigma12 = new Mat();
		
		Imgproc.GaussianBlur(i1_2, sigma1_2, new Size(11,11), 1.5);
		Core.subtract(sigma1_2, mu1_2, sigma1_2);
		
		Imgproc.GaussianBlur(i2_2, sigma2_2, new Size(11,11), 1.5);
		Core.subtract(sigma2_2, mu2_2, sigma2_2);
		
		Imgproc.GaussianBlur(i1_i2, sigma12, new Size(11,11), 1.5);
		Core.subtract(sigma12, mu1_mu2, sigma12);
		
		Mat t1= new Mat(i1.size(), i1.type());
		for(int i = 0; i< t1.rows();i++){
			for (int j = 0; j <t1.cols(); j++){
				double[] value = mu1_mu2.get(i, j);
				value[0] = value[0]*2 + 6.5;
				t1.put(i, j, value);
			}
		}
		
		Mat t2= new Mat(i1.size(), i1.type());
		for(int i = 0; i< t1.rows();i++){
			for (int j = 0; j <t1.cols(); j++){
				double[] value = sigma12.get(i, j);
				value[0] = value[0]*2 + 58.5225;
				t2.put(i, j, value);
			}
		}
		
		Mat t3 = t2.mul(t1);
		
		Mat u1= new Mat(i1.size(), i1.type());
		for(int i = 0; i< t1.rows();i++){
			for (int j = 0; j <t1.cols(); j++){
				double[] mu1_2_v = mu1_2.get(i, j);
				double[] mu2_2_v = mu2_2.get(i, j);
				double[] sigma1_2_v = sigma1_2.get(i, j);
				double[] sigma2_2_v = sigma2_2.get(i, j);
				
				mu1_2_v[0] = (mu1_2_v[0] + mu2_2_v[0] + 6.5) * (sigma1_2_v[0] + sigma2_2_v[0] + 58.5225);
				u1.put(i, j, mu1_2_v);
			}
		}
		
		Mat ssim_map = new Mat();
		Core.divide(t3, u1, ssim_map);
		
		Scalar sme = Core.mean(ssim_map);
		//System.out.println(sme.toString());
		
		return sme.val[0];
	}
	static int getNearest(Scalar scl){
		int re = 0;
		double min = 500.0;
		double r1 = scl.val[0];
		double g1 = scl.val[1];
		double b1 = scl.val[2];
		double range = 10;
		ArrayList<Integer> lis = new ArrayList<Integer>();
		try {
			BufferedReader in = new BufferedReader(new FileReader("lookup.txt"));
			String line;
			while((line = in.readLine()) != null)
			{
			    String[] linesplit = line.split(" ");
			    Integer name = Integer.valueOf(linesplit[0]);
			    double r = Double.parseDouble(linesplit[1]);
			    double g = Double.parseDouble(linesplit[2]);
			    double b = Double.parseDouble(linesplit[3]);
			    double diff = Math.sqrt((r-r1)*(r-r1) + (g-g1)*(g-g1) + (b-b1)*(b-b1));
			    if (diff<=range){
			    	lis.add(name);
			    }
			    if (diff<min){
			    	min = diff;
			    	re = name;
			    }
			    
			}
			in.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(!lis.isEmpty()){
			Random rand = new Random();
			int pos = rand.nextInt(lis.size());
			re = lis.get(pos);
			}
		return re;
	}
	
	static Mat constMat(double v, Mat img){
		Mat re = new Mat(img.size(), img.type());
		for(int i = 0; i< re.rows();i++){
			for (int j = 0; j <re.cols(); j++){
				re.put(i, j, v);
			}
		}
		return re;
	}
}

class File_tuple implements Comparable<File_tuple>{
	
	String name;
	double mean;
	
	File_tuple(String name1, double mean1){
		this.name = name1;
		this.mean = mean1;
	}

	@Override
	public int compareTo(File_tuple o) {
		Double d1 = this.mean;
		Double d2 = o.mean;
		return d1.compareTo(d2);
	}
}



