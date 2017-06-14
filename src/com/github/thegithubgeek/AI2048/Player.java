package com.github.thegithubgeek.AI2048;
import java.util.*;
public class Player {
	static int MAX_INIT_LAYER_NUM = 2;
	ArrayList<Float> weights = new ArrayList<>();
	ArrayList<Integer> dir = new ArrayList<>();
	static int index = 0;
	static float largest = Float.MIN_VALUE;
	
	public Player(ArrayList<Float> weights, ArrayList<Integer> dir){
		this.weights = weights;
		this.dir = dir;
	}
	
	public static Player genRanPlayer(){
		ArrayList<Float> weights = new ArrayList<>();
		ArrayList<Integer> dir = new ArrayList<>();
		for (int i = 0; i < 16; i++) {
			weights.add((float) Math.random());
			dir.add((int) Math.random() * 4);
		}
		return new Player(weights, dir);
	}
	
	public static void move(ArrayList<Float> weights, ArrayList<Integer> dir) {
		ArrayList<Float> board = new ArrayList<>();
		float[] vote = new float[4];
		Arrays.fill(vote, 0);
		for (int i = 0; i < 16; i++) {
			board.add(weights.get(i)*Game2048.getTile(i));
			vote[dir.get(i)] += board.get(i);
		}
		for ( int i = 0; i < vote.length; i++ )
		{
		    if ( vote[i] > largest )
		    {
		        largest = vote[i];
		        index = i;
		    }
		}
		Game2048.move(index);
	}
}
