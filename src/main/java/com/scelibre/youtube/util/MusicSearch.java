package com.scelibre.youtube.util;

import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.scelibre.youtube.MusicVDJ;

public class MusicSearch extends Thread {
	private final MusicVDJ core;
	private final SearchCallback callback;
	private final String text;
	
	public MusicSearch(MusicVDJ core, SearchCallback callback, String text) {
		this.core = core;
		this.callback = callback;
		this.text = text;
		this.start();
	}

	public void run() {
		List<Track> tracks;
		try {
			tracks = getYoutubeData(this.text);
			this.callback.onSuccess(tracks);
			sleep(0);
		} catch (InterruptedException ingnore) {
			// Ignore
		} catch (Exception exception) {
			this.callback.onError(exception);
		}
	}
	
	public List<Track> getYoutubeData(String query) throws IOException {
		String encodedQuery = URLEncoder.encode(query, "UTF-8");
		String yturl = "https://music.youtube.com/search?q=" + encodedQuery;

		List<Track> dataList = new ArrayList<>();

		String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36";
		String accept = "text/html";
		String acceptLanguage = "fr-FR,fr;q=0.9";
		String cacheControl = "max-age=0";
		String cookie = "CONSENT=PENDING+124;";
		String referer = "https://consent.youtube.com/";
		String secChUa = "\"Not A(Brand\";v=\"99\", \"Google Chrome\";v=\"121\", \"Chromium\";v=\"121\"";
		String secChUaArch = "x86";
		String secChUaBitness = "64";
		String secChUaFullVersion = "121.0.6167.141";
		String secChUaFullVersionList = "\"Not A(Brand\";v=\"99.0.0.0\", \"Google Chrome\";v=\"121.0.6167.141\", \"Chromium\";v=\"121.0.6167.141\"";
		String secChUaPlatform = "Windows";
		String secChUaPlatformVersion = "10.0.0";
		String secFetchDest = "document";
		String secFetchMode = "navigate";
		String secFetchSite = "same-origin";
		String serviceWorkerNavigationPreload = "true";
		String upgradeInsecureRequests = "1";

		HttpURLConnection connection = (HttpURLConnection) new URL(yturl).openConnection();
		connection.setRequestProperty("User-Agent", userAgent);
		connection.setRequestProperty("Accept", accept);
		connection.setRequestProperty("Accept-Language", acceptLanguage);
		connection.setRequestProperty("Cache-Control", cacheControl);
		connection.setRequestProperty("Cookie", cookie);
		connection.setRequestProperty("Referer", referer);
		connection.setRequestProperty("Sec-Ch-Ua", secChUa);
		connection.setRequestProperty("Sec-Ch-Ua-Arch", secChUaArch);
		connection.setRequestProperty("Sec-Ch-Ua-Bitness", secChUaBitness);
		connection.setRequestProperty("Sec-Ch-Ua-Full-Version", secChUaFullVersion);
		connection.setRequestProperty("Sec-Ch-Ua-Full-Version-List", secChUaFullVersionList);
		connection.setRequestProperty("Sec-Ch-Ua-Platform", secChUaPlatform);
		connection.setRequestProperty("Sec-Ch-Ua-Platform-Version", secChUaPlatformVersion);
		connection.setRequestProperty("Sec-Fetch-Dest", secFetchDest);
		connection.setRequestProperty("Sec-Fetch-Mode", secFetchMode);
		connection.setRequestProperty("Sec-Fetch-Site", secFetchSite);
		connection.setRequestProperty("Service-Worker-Navigation-Preload", serviceWorkerNavigationPreload);
		connection.setRequestProperty("Upgrade-Insecure-Requests", upgradeInsecureRequests);

		StringBuilder response = new StringBuilder();
		try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), Charset.forName("UTF-8")))) {
			String inputLine;
			while ((inputLine = in.readLine()) != null)
				response.append(inputLine);
		}

		Pattern pattern = Pattern.compile("data: '([^']+)'");
		Matcher matcher = pattern.matcher(response.toString());
		List<String> dataListStr = new ArrayList<>();
		while (matcher.find()) {
			dataListStr.add(matcher.group(1));
		}
		
		String dataJson = UnicodeEscapedStringConverter.convertUnicodeEscapes(dataListStr.get(1)).replace("\\\\", "\\");
		
		JsonObject jsonObject = JsonParser.parseString(dataJson).getAsJsonObject();
		JsonObject contents = jsonObject.getAsJsonObject("contents");
		JsonArray tabs = contents.getAsJsonObject("tabbedSearchResultsRenderer").getAsJsonArray("tabs");
		JsonObject tab = tabs.get(0).getAsJsonObject();
		JsonObject content = tab.getAsJsonObject("tabRenderer").getAsJsonObject("content");
		JsonArray sectionList = content.getAsJsonObject("sectionListRenderer").getAsJsonArray("contents");
		for (JsonElement item : sectionList) {
			JsonObject itemObj = item.getAsJsonObject();
			if (itemObj.has("musicCardShelfRenderer")) {
				try {
					JsonObject temp = itemObj.getAsJsonObject("musicCardShelfRenderer");
					String title = temp.getAsJsonObject("title").getAsJsonArray("runs").get(0).getAsJsonObject().get("text")
							.getAsString();
					String thumb = this.getThumb(temp);
					String[] tags = this.getTags(temp.getAsJsonObject("subtitle"));
					if (tags[2] != null) {
						String url = temp.getAsJsonObject("title").getAsJsonArray("runs").get(0).getAsJsonObject()
								.getAsJsonObject("navigationEndpoint").getAsJsonObject("watchEndpoint").get("videoId")
								.getAsString();
						
						MusicDownload download = this.core.getMusicDownload(url);
						dataList.add((download == null) ? new Track(url, title, thumb, tags[0], tags[1], tags[2], this.core.isExists(url)) : download.getTrack());
					}
				} catch (Exception exception) {
					// Ignore
				}
			} else if (itemObj.has("musicShelfRenderer")) {
				JsonObject temp = itemObj.getAsJsonObject("musicShelfRenderer");
				if (!temp.has("title"))
					continue;
				String tp = temp.getAsJsonObject("title").getAsJsonArray("runs").get(0).getAsJsonObject().get("text")
						.getAsString();
				if (!tp.equals("Titres") && !tp.equals("Videos") && !tp.equals("Vidéos"))
					continue;
				
				for (JsonElement subItem : temp.getAsJsonArray("contents")) {
					try {
						JsonObject subItemObj = subItem.getAsJsonObject();
						String url = null;
						String title = null;
						String thumb = null;
						String artist = null;
						String album = null;
						String time = null;
						if (!subItemObj.has("musicResponsiveListItemRenderer"))
							continue;
	
						JsonObject subItemTemp = subItemObj.getAsJsonObject("musicResponsiveListItemRenderer");
						thumb = this.getThumb(subItemTemp);
									
						for (JsonElement tagElement : subItemTemp.getAsJsonArray("flexColumns")) {
							JsonObject tag = tagElement.getAsJsonObject();
							if (!tag.has("musicResponsiveListItemFlexColumnRenderer"))
								continue;
							JsonObject tagTemp = tag.getAsJsonObject("musicResponsiveListItemFlexColumnRenderer");
							if (tagTemp.getAsJsonObject("text").getAsJsonArray("runs").size() > 1) {
								String[] tempTags = this.getTags(tagTemp.getAsJsonObject("text"));
								artist = tempTags[0];
								album = tempTags[1];
								time = tempTags[2];
							} else  {
								JsonObject runs = tagTemp.getAsJsonObject("text").getAsJsonArray("runs").get(0).getAsJsonObject();
								if (runs.has("navigationEndpoint")) {
									title = runs.get("text").getAsString();
									url = runs.getAsJsonObject("navigationEndpoint").getAsJsonObject("watchEndpoint")
											.get("videoId").getAsString();
								}
							}
						}
						if (time != null && url != null) {
							MusicDownload download = this.core.getMusicDownload(url);
							dataList.add((download == null) ? new Track(url, title, thumb, artist, album, time, this.core.isExists(url)) : download.getTrack());
						}
					} catch (Exception exception) {
						// Ignore
					}
				}
			}
		}

		return dataList;
	}

	private String getThumb(JsonObject data) {
		for (JsonElement item : data.getAsJsonObject("thumbnail").getAsJsonObject("musicThumbnailRenderer")
				.getAsJsonObject("thumbnail").getAsJsonArray("thumbnails")) {
			JsonObject thumbnail = item.getAsJsonObject();
			if (thumbnail.get("width").getAsInt() > 100) {
				return thumbnail.get("url").getAsString();
			}
		}
		return null;
	}

	private String[] getTags(JsonObject data) {
		String artist = null;
		String album = null;
		String time = null;
		for (JsonElement item : data.getAsJsonArray("runs")) {
			JsonObject itemObj = item.getAsJsonObject();
			if (itemObj.has("navigationEndpoint")) {
				JsonObject browseEndpoint = itemObj.getAsJsonObject("navigationEndpoint")
						.getAsJsonObject("browseEndpoint");
				JsonObject browseEndpointContextSupportedConfigs = browseEndpoint
						.getAsJsonObject("browseEndpointContextSupportedConfigs");
				JsonObject browseEndpointContextMusicConfig = browseEndpointContextSupportedConfigs
						.getAsJsonObject("browseEndpointContextMusicConfig");
				String tp = browseEndpointContextMusicConfig.get("pageType").getAsString();
				if (tp.equals("MUSIC_PAGE_TYPE_ARTIST")
						|| (artist == null && tp.equals("MUSIC_PAGE_TYPE_USER_CHANNEL"))) {
					artist = itemObj.get("text").getAsString();
				} else if (tp.equals("MUSIC_PAGE_TYPE_ALBUM")) {
					album = itemObj.get("text").getAsString();
				}
			} else if (timePattern.matcher(itemObj.get("text").getAsString()).matches()) {
				time = itemObj.get("text").getAsString();
			}
		}
		return new String[] { artist, album, time };
	}

	private static final Pattern timePattern = Pattern.compile("^(\\d{1,2}):(\\d{2})(?::(\\d{2}))?$");
}
