package com.scelibre.youtube.util;

import com.scelibre.youtube.util.MusicDownload.State;

public interface DownloadHandler {
	public abstract void stateUpdated(Track track, State state);
}
