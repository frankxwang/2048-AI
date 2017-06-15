package com.github.thegithubgeek.AI2048;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import com.github.thegithubgeek.AI2048.Player.ArrayIndexComparator;

import java.util.*;
public class Main {
	static int NUM_PLAYERS = 1000;
	static int KILL_RATE = 500;
	static int NUM_TRIAL = 5;
	static int GEN_NUM = 100;
	static Player[] players = new Player[NUM_PLAYERS];
	static Integer[] scores = new Integer[NUM_PLAYERS];
	static boolean lost = false;
	static Game2048 game2048;
	static int player;
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
	    for(int i=0; i<GEN_NUM; i++){
		    try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {}
		    generation();
		    System.out.println("GEN: "+i);
	    }
	    run(players[player], 100);
	}
	public static void generation(){
		for (int i=0; i<players.length; i++) {
			scores[i] = 0;
			for (int j = 0; j < NUM_TRIAL; j++) {
				Player player = players[i];
				run(player, 0);
				scores[i] += Game2048.myScore;
				game2048.resetGame();
				lost = false;
			}
			scores[i]/=NUM_TRIAL;
			if(i%100==0){
				System.out.println(i);
			}
		}
	    ArrayIndexComparator<Integer> comparator = new ArrayIndexComparator<Integer>(scores);
		Integer[] indexes = comparator.createIndexArray();
		Arrays.sort(indexes, comparator);
		Arrays.sort(scores);
	    int max = scores[NUM_PLAYERS-1];
	    System.out.println("Score: "+max);
	    player = indexes[NUM_PLAYERS-1];
	    System.out.println("Player: "+player);
	    int sum = 0;
        for(int i=0; i < scores.length ; i++)
                sum = sum + scores[i];
        double average = sum / scores.length;
        System.out.println("Mean Score: "+average);
	    nextGen();
	}
	//kill off the bad players and reproduce
	public static void nextGen(){
		for (int i = 0; i < KILL_RATE; i++) {
			players[i] = players[i+KILL_RATE].mutate();
		}
	}
	public static void run(Player p, int delay){
		while(!lost){
			p.move();
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {}
		}
	}
	public static void getScore() {
		lost = true;
		System.out.println(Game2048.myScore);
	}
}
