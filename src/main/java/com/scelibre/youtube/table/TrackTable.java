package com.scelibre.youtube.table;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;

import com.scelibre.youtube.MusicVDJ;
import com.scelibre.youtube.util.DownloadHandler;
import com.scelibre.youtube.util.FileSelection;
import com.scelibre.youtube.util.Icons;
import com.scelibre.youtube.util.MusicDownload.State;
import com.scelibre.youtube.util.Track;
import com.scelibre.youtube.util.VDJColor;

public class TrackTable extends JTable implements DropTargetListener, DragSourceListener, DragGestureListener, DownloadHandler {
	private static final long serialVersionUID = -42013453939943101L;

	private final TrackTableModel model;
	private final DragSource dragSource = DragSource.getDefaultDragSource();

	public TrackTable(final MusicVDJ core) {
		super(new TrackTableModel());

		this.model = (TrackTableModel) this.getModel();

		this.setLayout(new BorderLayout());
		this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.setBackground(VDJColor.PANEL_BACKGROUND);
		this.setForeground(VDJColor.TEXT);
		this.getTableHeader().setBackground(VDJColor.ROOT_BACKGROUND);
		this.getTableHeader().setReorderingAllowed(false);
		this.setShowGrid(false);
		this.setIntercellSpacing(new Dimension(0, 0));
		this.getColumnModel().getColumn(0).setMaxWidth(16);
		this.getColumnModel().getColumn(4).setMaxWidth(80);
		this.setRowSelectionAllowed(true);
		this.setDefaultRenderer(String.class, new DisableFocus());
		this.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent mouseEvent) {
				if (mouseEvent.getClickCount() == 2 && TrackTable.this.getSelectedRow() != -1) {
					Track track = TrackTable.this.model.getTrack(TrackTable.this.getSelectedRow());
					if (track.download(core, TrackTable.this))
						TrackTable.this.model.setValueAt(Icons.DOWNLOADING, TrackTable.this.getSelectedRow(), 0);
				}
			}
		});

		final DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setOpaque(false);
		renderer.setBorder(null);
		renderer.setForeground(VDJColor.TEXT);

		this.dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE, this);

		this.getTableHeader().setDefaultRenderer(renderer);
	}
	
	@Override
	public void stateUpdated(Track track, State state) {
		final int row = this.model.getIndex(track);
		if (row != -1)
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					TrackTable.this.model.setValueAt(state.getIcon(), row, 0);
				}
			});
	}

	public void display(List<Track> tracks) {
		this.model.setTracks(tracks);
		this.revalidate();
		this.repaint();
	}

	public void dragEnter(DropTargetDragEvent dropTargetDragEvent) {
		dropTargetDragEvent.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
	}

	public void dragGestureRecognized(DragGestureEvent dragGestureEvent) {
		int index = this.getSelectedRow();
		if (index < 0)
			return;
		Track track = this.model.getTrack(index);
		if (track.isExists()) {
			FileSelection transferable = new FileSelection(track.getFile());
			dragGestureEvent.startDrag(DragSource.DefaultCopyDrop, transferable, this);
		}
	}

	public class DisableFocus extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			setBorder(noFocusBorder);
			return this;
		}
	}

	public synchronized void drop(DropTargetDropEvent dropTargetDropEvent) {
		// Ignore
	}

	public void dragDropEnd(DragSourceDropEvent DragSourceDropEvent) {
		// Ignore
	}

	public void dragEnter(DragSourceDragEvent DragSourceDragEvent) {
		// Ignore
	}

	public void dragExit(DragSourceEvent DragSourceEvent) {
		// Ignore
	}

	public void dragOver(DragSourceDragEvent DragSourceDragEvent) {
		// Ignore
	}

	public void dropActionChanged(DragSourceDragEvent DragSourceDragEvent) {
		// Ignore
	}

	public void dragExit(DropTargetEvent dropTargetEvent) {
		// Ignore
	}

	public void dragOver(DropTargetDragEvent dropTargetDragEvent) {
		// Ignore
	}

	public void dropActionChanged(DropTargetDragEvent dropTargetDragEvent) {
		// Ignore
	}
}
