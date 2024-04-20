package com.scelibre.youtube.util;

public interface DownloadHandler {
	public abstract void error(Track track, DownloadError error);
	public abstract void success(Track track);
}
