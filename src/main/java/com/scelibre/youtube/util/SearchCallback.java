package com.scelibre.youtube.util;

import java.util.List;

public interface SearchCallback {
	public void onSuccess(List<Track> tracks);
	public void onError(Exception exception);
}
