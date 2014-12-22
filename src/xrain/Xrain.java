package xrain;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;

public class Xrain {

	static JFrame frame;
	
	public static void main(String[] args) {
		frame = new JFrame("XMP nowcast");
		frame.setLayout(new BorderLayout());
		
		XMPImage topView = new XMPImage();
//		XMPImage tsukubaView = new XMPImage();
//		tsukubaView.setZoom(true);
		
		topView.setPreferredSize(new Dimension(800,1027));
//		tsukubaView.setPreferredSize(new Dimension(800,800));
		
		frame.getContentPane().add(topView, BorderLayout.WEST);
//		frame.getContentPane().add(tsukubaView, BorderLayout.EAST);
		
		frame.setBounds(0, 0, 815, 1067);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

}
