package model;

import java.io.Serializable;

public class TopDownload implements Serializable{
	private static final long serialVersionUID = 4775638219971535129L;
	final private String name;
	final private int count;

	public TopDownload(String name, int count) {
		this.name = name;
		this.count = count;
	}

	public String getName() {
		return name;
	}

	public int getCount() {
		return count;
	}
}
