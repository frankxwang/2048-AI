package com.github.thegithubgeek.AI2048;

import javax.swing.*;

import com.github.thegithubgeek.AI2048.OldPlayer.ArrayIndexComparator;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
public class Main {
	static final int NUM_PLAYERS = 1000;
	static final int KILL_RATE = 500;
	static final int NUM_TRIAL = 5;
	static final int GEN_NUM = 100;
	static Player[] players = new Player[NUM_PLAYERS];
	static Integer[] scores = new Integer[NUM_PLAYERS];
	static boolean lost = false;
	static Game2048 game2048;
	static int player;
	static boolean running = true;
	static Thread evolve;
	public static void main(String[] args) {
		System.err.close();//turn off errors
		JFrame game = new JFrame();
		game.setTitle("2048 Game");
		game.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	    game.setSize(340, 400);
	    game.setResizable(false);
	    game.setLayout(new BorderLayout());
	    
	    JPanel buttons = new JPanel();
	    JButton start = new JButton("Start");
	    JButton pause = new JButton("Pause");
	    JButton cont = new JButton("Continue");
	    buttons.add(start);
	    buttons.add(pause);
	    buttons.add(cont);
	    start.addActionListener(new ActionListener()
	    {
			@Override
			public void actionPerformed(ActionEvent e) {
				evolve = new Evolve();
				evolve.start();
			}
	    });
	    
	    pause.addActionListener(new ActionListener()
	    {
			@Override
			public void actionPerformed(ActionEvent e) {
				//					evolve.wait();
				runBest();
			}
	    });
	    
	    cont.addActionListener(new ActionListener()
	    {
			@Override
			public void actionPerformed(ActionEvent e) {
				evolve.notify();
			}
	    });
	    
	    game.add(buttons, BorderLayout.NORTH);
	    game2048 = new Game2048();
	    game.add(game2048, BorderLayout.CENTER);

	    game.setLocationRelativeTo(null);
	    game.setVisible(true);
	}
	public static class Evolve extends Thread{
		
		public void run(){
		    for(int i=0; i<NUM_PLAYERS; i++){
		    	players[i]= new Player();
		    }
		    for(int i=0; i<GEN_NUM; i++){
	//		    try {
	//				Thread.sleep(2000);
	//			} catch (InterruptedException e) {}
			    generation();
			    System.out.println("GEN: "+i);
		    }
		    runBest();
		}

		public static void generation(){
			for (int i=0; i<players.length; i++) {
				scores[i] = 0;
				for (int j = 0; j < NUM_TRIAL; j++) {
					Player player = players[i];
					Main.run(player, 0);
					scores[i] += Game2048.myScore;
					game2048.resetGame();
					lost = false;
				}
				scores[i]/=NUM_TRIAL;
				if(i%100==0){
//					System.out.println(i);
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
	        Game2048.prevScore = Game2048.meanScore;
		    Game2048.meanScore = average;
		    Game2048.difference = Game2048.prevScore-Game2048.meanScore;
	        nextGen();
		}
		//kill off the bad players and reproduce
		public static void nextGen(){
			for (int i = 0; i < KILL_RATE; i++) {
				players[i] = players[i+KILL_RATE].mutate();
			}
			for (int i = 0; i < KILL_RATE; i++) {
				players[i+KILL_RATE] = players[i+KILL_RATE].mutate();
			}
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
	public static void runBest(){
	    run(players[player], 100);
	}
	public static void getScore() {
		lost = true;
		System.out.println(Game2048.myScore);
	}
}
