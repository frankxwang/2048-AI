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
	static ArrayList<Double> best = new ArrayList<>();
	
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
		disp = new dispGraph(scoreList, best);
		game.add(disp, BorderLayout.EAST);
		game.pack();
		game.setLocationRelativeTo(null);
		game.setVisible(true);
		
//		scoreList.add(742d);
//		scoreList.add(784d);
//		scoreList.add(790d);
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
			best.add((double) max);
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
		ArrayList<Double> best = new ArrayList<Double>();
		ArrayList<Double> scale = new ArrayList<Double>();
		dispGraph(ArrayList<Double> i, ArrayList<Double> j) {
			setSize(new Dimension(340, 340));
			median = i;
			best = j;
			scale = i;
			scale.addAll(j);
		}

		public Dimension getPreferredSize(){
			return new Dimension(340, 340);
		}
		
		public double getDiffAbs(ArrayList<Double> arr) {
			if (arr.size() <= 1) {
				return 1;
			} else {
				return Collections.max(arr).doubleValue() - Collections.min(arr).doubleValue();
			}
		}
		
		public int getSizeAbs(ArrayList<Double> arr) {
			if (arr.size() <= 1) {
				return 1;
			} else {
				return arr.size() - 1;
			}
		}
		
		public void paintComponent(Graphics g) {
			g.setColor(Color.GRAY);
			g.fillRect(0, 0, 340, 340);
			
			g.setColor(Color.WHITE);
			g.fillRoundRect(10, 10, 320, 320, 20, 20);
			
			g.setColor(Color.DARK_GRAY);
			g.setFont(new Font("SansSerif", Font.PLAIN, 18));
			g.drawString("Graph of Results", 100, 40);
			
			g.setColor(Color.GRAY);
			g.fillRect(50, 60, 2, 240);
			g.fillRect(40, 290, 240, 2);
			
			g.setColor(Color.LIGHT_GRAY);
			g.fillRect(40, 60, 240, 2);
			g.fillRect(40, 117, 240, 2);
			g.fillRect(40, 175, 240, 2);
			g.fillRect(40, 232, 240, 2);
			
			g.fillRect(108, 60, 2, 240);
			g.fillRect(165, 60, 2, 240);
			g.fillRect(223, 60, 2, 240);
			g.fillRect(280, 60, 2, 240);
			
			if (median.size() == 0) {
				g.setFont(new Font("Arial", Font.ROMAN_BASELINE, 48));
				g.fillRoundRect(110, 145, 110, 58, 25, 25);
				g.setColor(Color.WHITE);
				g.drawString("N/A", 125, 190);
			}
			for (int i = 0; i < median.size(); i++) {
				double min = Collections.min(scale).doubleValue();
				int regX = (int) Math.ceil(i*(230d/getSizeAbs(median)));
				int regY = (int) (-((median.get(i) - min)/getDiffAbs(scale))*230);
				int regYB = (int) (-((best.get(i) - min)/getDiffAbs(scale))*230);
				
//				System.out.println("(" + regX + ", " + regY + ")");
				
				g.setColor(Color.BLUE);
				g.fillOval(regX + 48, regY+288, 5, 5);
				g.setColor(Color.BLACK);
				g.fillOval(regX + 47, regYB+287, 5, 5);
				g.setColor(Color.GRAY);
				g.setFont(new Font("Arial", Font.PLAIN, 12));
				g.drawString((int)min+"", 15, 296);
				if (median.size() > 1) {
					if (i > 0) {
						int regXP = (int) Math.ceil((i-1)*(230d/getSizeAbs(median)));
						int regYP = (int) (-((median.get(i-1) - min)/getDiffAbs(scale))*230);
						int regYPB = (int) (-((best.get(i-1) - min)/getDiffAbs(scale))*230);
						
						g.setColor(Color.BLUE);
						g.drawLine(regX + 50, regY+290, regXP + 50, regYP+290);
						g.setColor(Color.BLACK);
						g.drawLine(regX + 50, regYB+290, regXP + 50, regYPB+290);
						g.setColor(Color.GRAY);
						g.drawString((int)(min+getDiffAbs(scale))+"", 15, 66);
						g.drawString((int)(min+0.75*getDiffAbs(scale))+"", 15, 123);
						g.drawString((int)(min+0.5*getDiffAbs(scale))+"", 15, 181);
						g.drawString((int)(min+0.25*getDiffAbs(scale))+"", 15, 238);
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
