package main;

import javax.swing.JFrame;

public class GameMain {

	public static void main(String[] args) {
		JFrame frame = new JFrame("Soul Maze");
        GamePanel panel = new GamePanel();
        frame.add(panel);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);

	}

}
