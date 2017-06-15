package com.github.thegithubgeek.AI2048;

import javax.swing.JFrame;
import javax.swing.WindowConstants;
import java.util.*;
public class Main {
	static int NUM_PLAYERS = 1000;
	static int KILL_RATE = 500;
	static Player[] players = new Player[NUM_PLAYERS];
	static int[] scores = new int[NUM_PLAYERS];
	static boolean lost = false;
	static Game2048 game2048;
	public static void main(String[] args) {
		JFrame game = new JFrame();
		game.setTitle("2048 Game");
		game.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	    game.setSize(340, 400);
	    game.setResizable(false);
	    game2048 = new Game2048();
	    game.add(game2048);

	    game.setLocationRelativeTo(null);
	    game.setVisible(true);
	    
	    for(int i=0; i<NUM_PLAYERS; i++){
	    	players[i]=Player.genRanPlayer();
	    }
	    try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {}
	    generation();
	    Arrays.sort(scores);
	    int max = scores[NUM_PLAYERS-1];
	    System.out.println(max);
	}
	public static void generation(){
		for (int i=0; i<players.length; i++) {
			Player player = players[i];
			run(player);
			System.out.println(i);
			scores[i] = Game2048.myScore;
			game2048.resetGame();
			lost = false;
		}
	}
	public static void run(Player p){
		while(!lost){
			p.move();
//			try {
//				Thread.sleep(1);
//			} catch (InterruptedException e) {}
		}
	}
	public static void getScore() {
		lost = true;
		System.out.println(Game2048.myScore);
	}
}
