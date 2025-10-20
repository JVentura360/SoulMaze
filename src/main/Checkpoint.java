package main;

import java.awt.Color;
import java.awt.Graphics;

public class Checkpoint {
	public final int x;
	public final int y;
	public final int size;
	public final Question question;
	public boolean solved = false;

	public Checkpoint(int x, int y, int size, Question question) {
		this.x = x;
		this.y = y;
		this.size = size;
		this.question = question;
	}

	public void draw(Graphics g) {
		if (solved) return;
		g.setColor(new Color(255, 215, 0));
		// Draw a small yellow dot centered in the tile
		int dot = Math.max(6, size / 4);
		int cx = x + (size - dot) / 2;
		int cy = y + (size - dot) / 2;
		g.fillOval(cx, cy, dot, dot);
	}
}


