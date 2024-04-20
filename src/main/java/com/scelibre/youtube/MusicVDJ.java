package com.scelibre.youtube;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

import com.scelibre.youtube.frame.ButtonFrame;
import com.scelibre.youtube.frame.SearchFrame;
import com.scelibre.youtube.util.DownloadHandler;
import com.scelibre.youtube.util.MusicDownload;
import com.scelibre.youtube.util.MusicSearch;
import com.scelibre.youtube.util.Track;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class MusicVDJ implements Runnable {
	public static File DOWNLOAD_DIR;

	public static void main(String[] args) {
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		
		ArgumentParser parser = ArgumentParsers.newFor("YouTube Music for VirtualDJ").build().defaultHelp(true)
				.description("Download and drag song directly from YouTube Music.");
		
		parser.addArgument("-m", "--mode").choices("vdj", "standalone").setDefault("vdj").help("Just show the window without the VDJ Button");
		parser.addArgument("-p", "--position").nargs(2).type(Integer.class).setDefault(45, dim.height - 40).help("Position (x; y) of the VDJ Button");
		parser.addArgument("-d", "--directory").nargs("*").required(true).help("Download directory");

		Namespace ns = null;
		try {
			ns = parser.parseArgs(args);
		} catch (ArgumentParserException e) {
			parser.handleError(e);
			System.exit(1);
		}

		String path = "";
		for (String name : ns.<String>getList("directory")) {
			path = path + " " + name;
		}
		File file = new File(path.substring(1));
		if (!file.exists() || !file.isDirectory()) try {
			file.mkdirs();
		} catch (Exception exception) {
			System.err.println("Could not create the download directory.");
			System.exit(1);
		}
		
		DOWNLOAD_DIR = file;
		
		Integer x = (Integer) ns.getList("position").get(0);
		Integer y = (Integer) ns.getList("position").get(1);
		if (x >= 0 && y >= 0) {
			SwingUtilities.invokeLater(new MusicVDJ(x, y, ns.getString("mode").equalsIgnoreCase("standalone")));
		} else {
			System.err.println("Invalid position.");
			System.exit(1);
		}
	}

	private final int x, y;
	private final boolean standalone;
	private final Map<String, MusicDownload> downloads;
	private MusicSearch searchThread = null;
	private SearchFrame search;

	public MusicVDJ(Integer x, Integer y, boolean standalone) {
		this.downloads = new HashMap<String, MusicDownload>();
		this.x = x;
		this.y = y;
		this.standalone = standalone;
	}

	public void run() {
		this.search = new SearchFrame(this, this.standalone);
		if (!this.standalone)
			new ButtonFrame(this.search, this.x, this.y);
	}

	@SuppressWarnings("deprecation")
	public void search(String text) {
		if (this.searchThread != null && this.searchThread.isAlive())
			this.searchThread.stop();
		this.searchThread = new MusicSearch(this, text);
	}

	public boolean download(Track track, DownloadHandler handler) {
		if (this.downloads.containsKey(track.getUrl()) || track.isExists())
			return false;
		this.downloads.put(track.getUrl(), new MusicDownload(track, handler));
		return true;
	}

	public boolean isDownloading(String url) {
		return this.downloads.containsKey(url);
	}

	public File isExists(String url) {
		File file = new File(DOWNLOAD_DIR, url);
		if (file.exists() && file.isDirectory())
			return file.listFiles()[0];
		return null;
	}

	public void display(List<Track> tracks) {
		this.search.display(tracks);
	}
}
