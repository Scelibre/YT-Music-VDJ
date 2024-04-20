package com.scelibre.youtube.frame;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import com.scelibre.youtube.MusicVDJ;
import com.scelibre.youtube.table.TrackTable;
import com.scelibre.youtube.util.Icons;
import com.scelibre.youtube.util.Track;
import com.scelibre.youtube.util.VDJColor;

public class SearchFrame extends JFrame {
	private static final long serialVersionUID = 6066179402787898753L;

	private final MusicVDJ core;
	private final TrackTable table;
	
	public SearchFrame(MusicVDJ core, boolean visible) {
		super("YouTube Music Search");
		
		this.core = core;
		
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		
		this.setIconImage(Icons.YTMUSIC.getImage());
		this.setType(visible ? Type.NORMAL : Type.UTILITY);
		this.setSize(1000, 400);
		this.setDefaultCloseOperation(visible ? EXIT_ON_CLOSE : HIDE_ON_CLOSE);
		this.setLocation(dim.width / 2 - this.getSize().width / 2,
				dim.height / 2 - this.getSize().height / 2);
		this.setAlwaysOnTop(true);
		this.setResizable(true);
		this.setUndecorated(false);
				
		JPanel rootPanel = new JPanel(new BorderLayout());
		rootPanel.setBackground(VDJColor.ROOT_BACKGROUND);
		rootPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
				
		final JTextField input = new JTextField();
		input.setBackground(VDJColor.PANEL_BACKGROUND);
		input.setBorder(BorderFactory.createLineBorder(VDJColor.BORDER, 1));
		input.setForeground(VDJColor.TEXT);
		input.setFont(input.getFont().deriveFont(15f));
		input.addActionListener(new AbstractAction() {
			private static final long serialVersionUID = -5034262671097950481L;

			public void actionPerformed(ActionEvent e) {
				SearchFrame.this.core.search(input.getText());
			}
		});
		
		rootPanel.add(input, BorderLayout.PAGE_START);
		
		this.add(rootPanel);
		
		JPanel tablePanel = new JPanel(new BorderLayout());
		tablePanel.setBackground(VDJColor.ROOT_BACKGROUND);
		tablePanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
		
		this.table = new TrackTable(this.core);
		
		JScrollPane pane = new JScrollPane(this.table);
		pane.getViewport().setBackground(VDJColor.PANEL_BACKGROUND);
		pane.setBorder(BorderFactory.createEmptyBorder());
		
		tablePanel.add(pane);
		
		rootPanel.add(tablePanel);
				
		this.display(new ArrayList<Track>());
		
		this.setVisible(visible);
	}

	public void display(List<Track> tracks) {
		this.table.display(tracks);
	}
}