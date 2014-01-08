package model;

import java.io.Serializable;
import java.util.Comparator;
import java.util.PriorityQueue;

public class TopDownloads implements Serializable {
	final static public int LIST_MAX = 100;

	private static final long serialVersionUID = -8157251005612970351L;
	private PriorityQueue<TopDownload> list = new PriorityQueue<TopDownload>(LIST_MAX, new DownloadComparator());

	private class DownloadComparator implements Comparator<TopDownload>, Serializable {
		private static final long serialVersionUID = 2163796579913007371L;
		@Override
		public int compare(TopDownload a, TopDownload b) {
			return b.getCount() - a.getCount();
		}
	}

	public void add(TopDownload d) {
		list.add(d);
	}

	public int size() {
		return list.size();
	}

	public TopDownloads trimmedSet(int count) {
		TopDownloads ds = new TopDownloads();
		for (int i=0 ;i<count; i++) {
			TopDownload elem = list.poll();
			if (elem == null) {
				break;
			} else {
				ds.add(elem);
			}
		}
		return ds;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		int i = 1;
		for(TopDownload d: list) {
			sb.append(String.format("%d. %-20.20s %d\n", i++, d.getName(), d.getCount()));
		}
		return sb.toString();
	}
}
