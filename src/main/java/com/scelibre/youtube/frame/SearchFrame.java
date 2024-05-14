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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.scelibre.youtube.MusicVDJ;
import com.scelibre.youtube.table.TrackTable;
import com.scelibre.youtube.util.Icons;
import com.scelibre.youtube.util.MusicSearch;
import com.scelibre.youtube.util.SearchCallback;
import com.scelibre.youtube.util.Track;
import com.scelibre.youtube.util.VDJColor;

public class SearchFrame extends JFrame implements SearchCallback {
	private static final long serialVersionUID = 6066179402787898753L;

	private final MusicVDJ core;
	private final TrackTable table;
	private final JLabel message;
	private MusicSearch searchThread = null;
	
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

			@SuppressWarnings("deprecation")
			public void actionPerformed(ActionEvent e) {
				SearchFrame self = SearchFrame.this;
				if (self.searchThread != null && self.searchThread.isAlive())
					self.searchThread.stop();
				self.clearTracks();
				self.displayMessage("Chargement en cours...");
				self.searchThread = new MusicSearch(self.core, self, input.getText());
			}
		});
		
		rootPanel.add(input, BorderLayout.PAGE_START);
		
		JPanel tablePanel = new JPanel(new BorderLayout());
		tablePanel.setBackground(VDJColor.ROOT_BACKGROUND);
		tablePanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
		
		this.table = new TrackTable(this.core);
		
		this.message = new JLabel("", SwingConstants.CENTER);
		this.message.setForeground(VDJColor.BORDER);
		
		this.table.add(this.message, BorderLayout.CENTER);
		this.table.setFillsViewportHeight(true);
		
		JScrollPane scrollPane = new JScrollPane(this.table);
		scrollPane.getViewport().setBackground(VDJColor.PANEL_BACKGROUND);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		
		
		tablePanel.add(scrollPane);
		rootPanel.add(tablePanel);
		
		this.add(rootPanel);
		
	    //this.getLayeredPane().add(messageLabel, new Integer(1));
		
		this.onSuccess(null); // Reinit
		
		this.setVisible(visible);
	}
	
	private void displayMessage(String message) {
		this.message.setText(message == null ? "" : message);
	}
	
	private void clearMessage() {
		this.displayMessage(null);
	}
	
	private void displayTracks(List<Track> tracks) {
		if (tracks == null) {
			this.clearTracks();
			return;
		}
		this.table.display(tracks);
	}
	
	private void clearTracks() {
		this.displayTracks(new ArrayList<Track>());
	}

	@Override
	public void onSuccess(List<Track> tracks) {
		if (tracks == null || tracks.isEmpty()) {
			this.clearTracks();
			this.displayMessage("Pas d'éléments à afficher");
		} else {
			this.clearMessage();
			this.displayTracks(tracks);
		}
	}

	@Override
	public void onError(Exception exception) {
		exception.printStackTrace();
		this.displayMessage("Erreur : " + exception.getMessage());
	}
}