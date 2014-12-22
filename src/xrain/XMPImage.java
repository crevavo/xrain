package xrain;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.Timer;

public class XMPImage extends JPanel implements ActionListener{
	public Calendar currentTime;
	private Timer timer;
	private String areaCode;
	private BufferedImage baseMapImg;
	private BufferedImage legendImg;
	private boolean zoomFlag = false;
	private boolean privateMap = true;

	XMPImage() {
		setBackground(Color.BLACK);
		currentTime = Calendar.getInstance();
		currentTime.add(Calendar.SECOND, -40);
		areaCode = new String("kanto11");
		//areaCode = new String("hokuriku01");
		baseMapImg = getBaseMapImg();
		legendImg = getLegendImg();
		timer = new Timer(60*1000, this);
		timer.start();
	}
	
	public void setZoom(boolean flag){
		zoomFlag = flag;
		if (zoomFlag = true) {
			baseMapImg = getBaseMapImg();
		}
	}

	public void paint(Graphics g){
		Image xmpImg = getGouseiImg();
//		g.drawImage(xmpImg, 0, 0, this);
		Dimension dim = null;
		if (!zoomFlag) {
			dim = getImgDimensionFull();
			int height = dim.height;
			int width = dim.width;
			g.drawImage(xmpImg, 0, 0, width, height, this);
		} else {
			dim = getImgDimension();
			int height = dim.height;
			int width = dim.width;
			g.drawImage(xmpImg, 0, 0, width, height, this);
		}
	}

	private BufferedImage getGouseiImg() {
		BufferedImage radarImg = getRadarImg();
		BufferedImage gouseiImg = null;
		if(baseMapImg == null || radarImg == null || legendImg == null) return null;
		int height = baseMapImg.getHeight();
		int width = baseMapImg.getWidth();
		try {
			gouseiImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			Graphics g = gouseiImg.createGraphics();
			((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
	                RenderingHints.VALUE_ANTIALIAS_ON);
			g.drawImage(baseMapImg, 0, 0, width, height, null);
			((Graphics2D) g).setComposite(
			           AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.4F)
			       );
			g.drawImage(radarImg, 0, 0, width, height, null);
			((Graphics2D) g).setComposite(
			           AlphaComposite.getInstance(AlphaComposite.SRC,1.0F)
			       );
			if (!zoomFlag) {
				int legWid = legendImg.getWidth();
				int legHei = legendImg.getHeight();
				g.drawImage(legendImg, 0, 0, legWid, legHei, null);
			}
			g.dispose();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return gouseiImg;
	}

	private Dimension getImgDimension() {
		Dimension imgDim = null;
		int mapHei = baseMapImg.getHeight();
		int mapWid = baseMapImg.getWidth();
		float mapAspect = (float)mapHei / (float)mapWid;
		int panelHei = super.getHeight();
		int panelWid = super.getWidth();
		float panelAspect = (float)panelHei / (float)panelWid;
		if (panelAspect < mapAspect) {
			int wid = (int) ((float)panelHei / mapAspect);
			int hei = panelHei;
			imgDim = new Dimension(wid, hei);
		} else {
			int wid = panelWid;
			int hei = (int) ((float)panelWid * mapAspect);
			imgDim = new Dimension(wid, hei);
		}
		return imgDim;
	}
	
	private Dimension getImgDimensionFull() {
		Dimension imgDim = null;
		int mapHei = baseMapImg.getHeight();
		int mapWid = baseMapImg.getWidth();
		float mapAspect = (float)mapHei / (float)mapWid;
		int panelHei = super.getHeight();
		int panelWid = super.getWidth();
		float panelAspect = (float)panelHei / (float)panelWid;
		if (panelAspect < mapAspect) {
			int wid = panelWid;
			int hei = (int) ((float)panelWid * mapAspect);
			imgDim = new Dimension(wid, hei);
		} else {
			int wid = (int) ((float)panelHei / mapAspect);
			int hei = panelHei;
			imgDim = new Dimension(wid, hei);
		}
		return imgDim;
	}

	private BufferedImage getBaseMapImg() {
		BufferedImage mapImg = null;
		if (zoomFlag) {
			try {
				mapImg = ImageIO.read(new File("data/tsukubaMap.png"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			if (privateMap) {
				try {
					mapImg = ImageIO.read(new File("data/" + areaCode + "_basemap.png"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				String url = new String();
				url = "http://www.river.go.jp/xbandradar/map/" 
						+ areaCode
						+ "/1/basemap1.png";
				mapImg = loadImage(url);
			}
		}
		return mapImg;
	}

	private BufferedImage getRadarImg() {
		String url = new String();

		String date = getDateString();
		String timePart = getTimePart();
		String timeStr = getTimeStr();

		url = "http://www.river.go.jp/xbandradar/rdimg/" + areaCode + "/"
				+ date + "/" + timePart + "/" + areaCode + timeStr + "_top.png";

		BufferedImage radarImg = loadImage(url);
		
		if (radarImg == null) {
			try {
				radarImg = ImageIO.read(new File("data/nodata.png"));
				return radarImg;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if (zoomFlag) {
			BufferedImage outputImage = new BufferedImage(600, 600, radarImg.getType());
			outputImage = radarImg.getSubimage(3035, 2740, 600, 600);
			return outputImage;
		} else {
			saveImg(radarImg, areaCode + timeStr + "_top.png");
			return radarImg;
		}
	}
	
	private void saveImg(BufferedImage img, String fileName) {
		try {
			ImageIO.write(img, "png", new File("log/" + fileName));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private BufferedImage getLegendImg(){
		String url;
		url = "http://www.river.go.jp/xbandradar/common/img/legend_cast.gif";
		return loadImage(url);
	}

	public static BufferedImage loadImage(String url) {
		try {
			URL u = new URL(url);
			BufferedImage img = ImageIO.read(u);
			return img;
		} catch (IOException e) {
			System.err.println(e);
			return null;
		}
	}

	private String getTimeStr() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmm");
		return sdf.format(currentTime.getTime());
	}

	private String getTimePart() {
		int hour = currentTime.get(Calendar.HOUR_OF_DAY);
		String timepart;
		if ((0 <= hour) && (hour < 6)) {
			timepart = "0000";
		} else {
			if ((6 <= hour) && (hour < 12)) {
				timepart = "0600";
			} else {
				if ((12 <= hour) && (hour < 18)) {
					timepart = "1200";
				} else {
					timepart = "1800";
				}
			}
		}
		return timepart;
	}

	private String getDateString() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		return sdf.format(currentTime.getTime());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		currentTime = Calendar.getInstance();
		currentTime.add(Calendar.SECOND, -30);
		repaint();
	}

}
