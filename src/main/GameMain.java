package main;

import javax.swing.JFrame;

public class GameMain {

	public static void main(String[] args) {
		JFrame frame = new JFrame("Soul Maze");
        MainMenu mainMenu = new MainMenu(frame);
        frame.add(mainMenu);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);

	}

}
