package com.scelibre.youtube.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ProcessBuilder.Redirect;
import java.net.URL;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.downloader.request.RequestVideoFileDownload;
import com.github.kiulian.downloader.downloader.request.RequestVideoInfo;
import com.github.kiulian.downloader.downloader.response.Response;
import com.github.kiulian.downloader.model.videos.VideoInfo;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v24Tag;
import com.mpatric.mp3agic.Mp3File;
import com.scelibre.youtube.MusicVDJ;

public class MusicDownload extends Thread {
	private final Track track;
	private final DownloadHandler handler;

	public MusicDownload(Track track, DownloadHandler handler) {
		this.track = track;
		this.handler = handler;
		System.out.println("Downloading " + this.track.getTitle());
		this.start();
	}

	private static final String INVALID_CHARS_REGEX = "[<>:\"/\\|?*]";

	public static String validateFileName(String fileName) {
		Pattern pattern = Pattern.compile(INVALID_CHARS_REGEX);
		Matcher matcher = pattern.matcher(fileName);
		return matcher.replaceAll(Character.toString('-'));
	}

	public void run() {
		try {
			File temp = Files.createTempDirectory("YTMusicVDJ").toFile();

			YoutubeDownloader downloader = new YoutubeDownloader();

			RequestVideoInfo request = new RequestVideoInfo(this.track.getUrl());
			Response<VideoInfo> response = downloader.getVideoInfo(request);
			VideoInfo video = response.data();
			RequestVideoFileDownload downloadRequest = new RequestVideoFileDownload(video.bestAudioFormat())
					.saveTo(temp).renameTo(this.track.getUrl()).overwriteIfExists(true);

			Response<File> downloadResponse = downloader.downloadVideoFile(downloadRequest);
			File data = downloadResponse.data();

			System.out.println("Downloading " + this.track.getTitle() + " complete, stating convertion");
			
			File file = new File(temp, this.track.getUrl() + ".mp3");						
			
			ProcessBuilder pb = new ProcessBuilder("ffmpeg", "-y", "-i", "\"" + data.getAbsolutePath() + "\"", "\"" + file.getAbsolutePath() + "\"");
			pb.redirectOutput(Redirect.INHERIT);
			pb.redirectError(Redirect.INHERIT);
			Process process = pb.start();
						
			/*BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null)
			    System.out.println(line);*/
			process.waitFor();


			System.out.println("Conversion finished, downloading thumb");
			
			data.delete();

			URL url = new URL(this.track.getThumb());
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			InputStream is = null;
			try {
				is = url.openStream();
				byte[] byteChunk = new byte[4096]; // Or whatever size you want to read in at a time.
				int n;

				while ((n = is.read(byteChunk)) > 0) {
					baos.write(byteChunk, 0, n);
				}
			} catch (IOException e) {
				System.err.printf("Failed while reading bytes from %s: %s", url.toExternalForm(), e.getMessage());
				e.printStackTrace();
			} finally {
				if (is != null) {
					is.close();
				}
			}
			
			System.out.println("Thumb downloaded, making the MP3...");

			Mp3File mp3file = new Mp3File(file);
			ID3v2 id3v2Tag;
			if (mp3file.hasId3v2Tag()) {
				id3v2Tag = mp3file.getId3v2Tag();
			} else {
				id3v2Tag = new ID3v24Tag();
				mp3file.setId3v2Tag(id3v2Tag);
			}
			if (this.track.getArtist() != null)
				id3v2Tag.setArtist(this.track.getArtist());
			if (this.track.getTitle() != null)
				id3v2Tag.setTitle(this.track.getTitle());
			if (this.track.getAlbum() != null)
				id3v2Tag.setAlbum(this.track.getAlbum());
			id3v2Tag.setUrl("https://music.youtube.com/watch?v=" + this.track.getUrl());
			id3v2Tag.setAlbumImage(baos.toByteArray(), "image/jpeg");

			File folder = new File(MusicVDJ.DOWNLOAD_DIR, this.track.getUrl());
			folder.mkdirs();

			File mp3 = new File(folder, validateFileName((this.track.getArtist() == null) ? this.track.getTitle()
					: this.track.getArtist() + " - " + this.track.getTitle()) + ".mp3");

			mp3file.save(mp3.getAbsolutePath());

			file.delete();
			
			temp.delete();
			
			System.out.println("Song ready to use");
			
			this.track.setFile(mp3);
			this.handler.success(this.track);
		} catch (Exception exception) {
			exception.printStackTrace();
			System.err.println("Error while downloading " + this.track.getTitle());
			this.handler.error(this.track, DownloadError.ERROR);
		}
	}
}
