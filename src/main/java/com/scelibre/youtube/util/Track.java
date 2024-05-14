package com.scelibre.youtube.util;

import java.io.File;

import com.scelibre.youtube.MusicVDJ;

public class Track {
	private final String url, title, thumb, artist, album, time;
	private File file;
	private MusicDownload download = null;

	public Track(String url, String title, String thumb, String artist, String album, String time, File file) {
		this.url = url;
		this.title = title;
		this.thumb = thumb;
		this.artist = artist;
		this.album = album;
		this.time = time;
		this.file = file;
	}

	public final String getUrl() {
		return url;
	}

	public final String getTitle() {
		return title;
	}

	public final String getThumb() {
		return thumb;
	}

	public final String getArtist() {
		return artist;
	}

	public final String getAlbum() {
		return album;
	}

	public final String getTime() {
		return time;
	}

	public final boolean isExists() {
		return !(this.file == null);
	}

	public final File getFile() {
		return this.file;
	}

	public final MusicDownload getMusicDownload() {
		return this.download;
	}

	protected void setFile(File file) {
		this.file = file;
	}

	public final boolean download(MusicVDJ core, final DownloadHandler handler) {
		if (this.isExists()
				|| (this.download != null && !this.download.getDownloadState().equals(MusicDownload.State.ERROR)))
			return false;
		this.download = core.download(this, handler);
		return true;
	}
}