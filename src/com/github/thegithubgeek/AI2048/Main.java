package com.github.thegithubgeek.AI2048;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

public class Main {
	  public static void main(String[] args) {
	    JFrame game = new JFrame();
	    game.setTitle("2048 Game");
	    game.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	    game.setSize(340, 400);
	    game.setResizable(false);

	    game.add(new Game2048());

	    game.setLocationRelativeTo(null);
	    game.setVisible(true);
	  }
	  public static void getScore() {
		  return;
	  }
}
