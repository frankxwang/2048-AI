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
	static dispGraph disp;
	static int player;
	static boolean running = false;
	static Thread evolve;
	static ArrayList<Double> scoreList = new ArrayList<>();
	static ArrayList<Integer> best = new ArrayList<>();
	
	public static void main(String[] args) {
		System.err.close();// turn off errors
		JFrame game = new JFrame();
		game.setTitle("2048 Game");
		game.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		game.setSize(700, 700);
		game.setResizable(false);
		game.setLayout(new BorderLayout());

		JPanel buttons = new JPanel();
		JButton start = new JButton("Start");
		JButton pause = new JButton("Pause");
		JButton cont = new JButton("Continue");
		buttons.add(start);
		buttons.add(pause);
		buttons.add(cont);

		scoreList.add(234d);
		scoreList.add(345d);
		scoreList.add(1122d);

		start.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				evolve = new Evolve();
				running = false;
				evolve.start();
				evolve.notify();
			}
		});

		pause.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				evolve.interrupt();
				running = true;
				evolve.notify();
			}
		});

		cont.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			}
		});

		game.add(buttons, BorderLayout.NORTH);
		game2048 = new Game2048();
		game.add(game2048, BorderLayout.CENTER);
		disp = new dispGraph(scoreList);
		game.add(disp, BorderLayout.EAST);
		game.pack();
		game.setLocationRelativeTo(null);
		game.setVisible(true);
	}

	public static class Evolve extends Thread {

		public void run() {
			synchronized (this) {
				while (running) {
					try {
						wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			for (int i = 0; i < NUM_PLAYERS; i++) {
				players[i] = new Player();
			}
			for (int i = 0; i < GEN_NUM; i++) {
				try {
					// Thread.sleep(2000);
					// } catch (InterruptedException e) {}
					Main.dispGraph.run();
					generation();
					System.out.println("GEN: " + i);
					Game2048.gen = i + 2;
				} catch (Exception e) {}
			}
			Main.dispGraph.run();
			runBest();
		}

		public static void generation() {
			for (int i = 0; i < players.length; i++) {
				scores[i] = 0;
				for (int j = 0; j < NUM_TRIAL; j++) {
					Player player = players[i];
					Main.run(player, 0);
					Main.dispGraph.run();
					scores[i] += Game2048.myScore;
					game2048.resetGame();
					lost = false;
				}
				scores[i] /= NUM_TRIAL;
				if (i % 100 == 0) {
					// System.out.println(i);
				}
			}
			ArrayIndexComparator<Integer> comparator = new ArrayIndexComparator<Integer>(scores);
			Integer[] indexes = comparator.createIndexArray();
			Arrays.sort(indexes, comparator);
			Arrays.sort(scores);
			int max = scores[NUM_PLAYERS - 1];
			System.out.println("Score: " + max);
			player = indexes[NUM_PLAYERS - 1];
			System.out.println("Player: " + player);
			int sum = 0;
			for (int i = 0; i < scores.length; i++)
				sum = sum + scores[i];
			double average = sum / scores.length;
			scoreList.add(average);
			System.out.println("ADDED");
			best.add(max);
			System.out.println("Mean Score: " + average);
			Game2048.prevScore = Game2048.meanScore;
			Game2048.meanScore = average;
			Game2048.diff = Game2048.meanScore - Game2048.prevScore;
			dispGraph.run();
			nextGen();
		}

		// kill off the bad players and reproduce
		public static void nextGen() {
			Main.dispGraph.run();
			for (int i = 0; i < KILL_RATE; i++) {
				Main.dispGraph.run();
				players[i] = players[i + KILL_RATE].mutate();
			}
			for (int i = 0; i < KILL_RATE; i++) {
				Main.dispGraph.run();
				players[i + KILL_RATE] = players[i + KILL_RATE].mutate();
			}
		}
	}

	public static void run(Player p, int delay) {
		while (!lost) {
			p.move();
			Main.dispGraph.run();
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
			}
		}
	}

	public static void runBest() {
		run(players[player], 100);
	}

	public static void getScore() {
		lost = true;
		System.out.println(Game2048.myScore);
	}
	
	public static class dispGraph extends JPanel {
		ArrayList<Double> median = new ArrayList<Double>();
		dispGraph(ArrayList<Double> i) {
			setSize(new Dimension(340, 340));
			median = i;
		}

		public Dimension getPreferredSize(){
			return new Dimension(340, 340);
		}
		
		public double getYNum() {
			return Collections.max(median).doubleValue();
		}
		
		
		public void paintComponent(Graphics g) {
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, 340, 340);
			g.setColor(Color.GRAY);
			g.fillRect(0, 0, 340, 10);
			g.fillRect(0, 330, 340, 10);
			g.fillRect(0, 10, 10, 320);
			g.fillRect(330, 10, 10, 320);
			g.setColor(Color.DARK_GRAY);
			g.setFont(new Font("SansSerif", Font.PLAIN, 18));
			g.drawString("Graph of Results", 100, 40);
			g.setColor(Color.LIGHT_GRAY);
			g.fillRect(50, 60, 2, 230);
			g.fillRect(50, 290, 230, 2);
			for (int i = 0; i < median.size(); i++) {
				g.setColor(Color.BLACK);
				g.fillOval((int) (i*(230/(median.size()-1)) + 45), (int) (-(median.get(i)/getYNum())*230+285), 10, 10);
				if (median.size() > 1) {
					if (i > 0) {
						g.drawLine((int) ((i-1)*(230/(median.size()-1)) + 50), (int) (-(median.get(i-1)/getYNum())*230+290), 
								(int) (i*(230/(median.size()-1)) + 50), (int) (-(median.get(i)/getYNum())*230+290));
					}
				}
			}
		}
		public static void run(){
			try {
				//Main.disp.revalidate();
				Main.disp.repaint();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
