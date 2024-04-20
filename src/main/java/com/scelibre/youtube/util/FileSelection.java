package com.scelibre.youtube.util;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Vector;

public class FileSelection extends Vector<File> implements Transferable {
	private static final long serialVersionUID = 9111888111811828935L;
	
	final static int FILE = 0;
	final static int STRING = 1;
	final static int PLAIN = 2;
	@SuppressWarnings("deprecation")
	DataFlavor flavors[] = { DataFlavor.javaFileListFlavor, DataFlavor.stringFlavor, DataFlavor.plainTextFlavor };

	public FileSelection(File file) {
		addElement(file);
	}

	/* Returns the array of flavors in which it can provide the data. */
	public synchronized DataFlavor[] getTransferDataFlavors() {
		return flavors;
	}

	/* Returns whether the requested flavor is supported by this object. */
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		boolean b = false;
		b |= flavor.equals(flavors[FILE]);
		b |= flavor.equals(flavors[STRING]);
		b |= flavor.equals(flavors[PLAIN]);
		return (b);
	}

	/**
	 * If the data was requested in the "java.lang.String" flavor, return the String
	 * representing the selection.
	 */
	public synchronized Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		if (flavor.equals(flavors[FILE])) {
			return this;
		} else if (flavor.equals(flavors[PLAIN])) {
			return new StringReader(((File) elementAt(0)).getAbsolutePath());
		} else if (flavor.equals(flavors[STRING])) {
			return ((File) elementAt(0)).getAbsolutePath();
		} else {
			throw new UnsupportedFlavorException(flavor);
		}
	}
}