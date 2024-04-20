package com.scelibre.youtube.frame;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import com.scelibre.youtube.util.Icons;
import com.scelibre.youtube.util.VDJColor;

public class ButtonFrame extends JFrame implements MouseListener {
	private static final long serialVersionUID = 3431103684657222496L;
	
	private final SearchFrame search;
	
	public ButtonFrame(SearchFrame search, int x, int y) {
		this.search = search;
				
		this.setIconImage(Icons.YTMUSIC.getImage());
		this.setType(Type.UTILITY);
		this.setLocation(x, y);
		this.setSize(100, 25);
		this.setAlwaysOnTop(true);
		this.setResizable(false);
		this.setUndecorated(true);
		this.getContentPane().setBackground(VDJColor.PANEL_BACKGROUND);
		
		JLabel text = new JLabel("YouTube Music", SwingConstants.CENTER);
		text.setSize(this.getSize().height, this.getSize().width);
		text.setForeground(VDJColor.TEXT);
		
		this.add(text);
		this.addMouseListener(this);
		this.setVisible(true);
	}
	
	public void mousePressed(MouseEvent e) {
		if (e.getButton() == 3 && e.getClickCount() == 4) {
			this.setVisible(false);
			this.search.setVisible(false);
			this.search.dispose();
			this.dispose();
		}
		if (e.getButton() == 1)
			this.search.setVisible(!this.search.isVisible());
	}
	
	public void mouseClicked(MouseEvent e) {
		// Ignore
	}
	
	public void mouseEntered(MouseEvent e) {
		// Ignore
	}
	
	public void mouseExited(MouseEvent e) {
		// Ignore
	}
	
	public void mouseReleased(MouseEvent e) {
		// Ignore
	}
}