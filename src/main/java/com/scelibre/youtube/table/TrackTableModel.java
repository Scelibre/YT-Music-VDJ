package com.scelibre.youtube.table;

import java.util.List;

import javax.swing.Icon;
import javax.swing.table.DefaultTableModel;

import com.scelibre.youtube.util.Icons;
import com.scelibre.youtube.util.Track;

public class TrackTableModel extends DefaultTableModel {
	private static final long serialVersionUID = -4245983689743723971L;
	
	private List<Track> tracks = null;

	public TrackTableModel() {
		this.addColumn("");
		this.addColumn("Title");
		this.addColumn("Artist");
		this.addColumn("Album");
		this.addColumn("Duration");
	}
	
	public void setTracks(List<Track> tracks) {
		this.setRowCount(0);
		this.tracks = tracks;
		for (Track track : tracks) {
			Object[] o = {
				getIcon(track),
				track.getTitle(),
				track.getArtist(),
				track.getAlbum(),
				track.getTime()
			};
			this.addRow(o);
		}
	}
	
	public Icon getIcon(Track track) {
		if (track.isExists()) {
			return Icons.DOWNLOADED;
		} else if (track.isDownloading()) {
			return Icons.DOWNLOADING;
		} else {
			return Icons.EMPTY;
		}
	}
	
	@Override
	public Class<?> getColumnClass(int id) {
		switch (id) {
			case 0:
				return Icon.class;
	
			default:
				return String.class;
		}
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	public final Track getTrack(int index) {
		return this.tracks.get(index);
	}

	public int getIndex(Track track) {
		for (int i = 0; i < this.tracks.size(); i++) {
			if (track.getUrl().equals(this.tracks.get(i).getUrl()))
				return i;
		}
		return -1;
	}

}
