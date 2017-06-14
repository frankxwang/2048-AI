package com.github.thegithubgeek.AI2048;
import java.util.*;
public class Player {
	static int MAX_INIT_LAYER_NUM = 2;
	ArrayList<Float> weights = new ArrayList<>();
	public Player(ArrayList<Float> weights){
		this.weights = weights;
	}
	public static void genRanPlayer(){
		ArrayList<Float> weights = new ArrayList<>();
		for (int i = 0; i < 16; i++) {
			weights.add((float) Math.random());
		}
	}
}
