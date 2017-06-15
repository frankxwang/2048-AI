package com.github.thegithubgeek.AI2048;
import java.util.*;

import com.github.thegithubgeek.AI2048.Game2048.Tile;
public class Player {
	static int MAX_INIT_LAYER_NUM = 2;
	static float MAX_CHANGE = 0.05f;
	static float DIR_CHANGE_PROB = 0.01f;
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
			dir.add((int)( Math.random() * 4));
		}
		return new Player(weights, dir);
	}
	
	public void move() {
		Float[] vote = {0f,0f,0f,0f};
		for (int i = 0; i < 16; i++) {
			vote[dir.get(i)] += weights.get(i)*Game2048.getTile(i);
		}
		
		ArrayIndexComparator<Float> comparator = new ArrayIndexComparator<Float>(vote);
		Integer[] indexes = comparator.createIndexArray();
		Arrays.sort(indexes, comparator);
		
		Tile[] curTiles = Game2048.myTiles.clone();
		int i;
		for(i=3; Arrays.equals(curTiles,Game2048.myTiles)&&i>=0; i--){
			Game2048.move(indexes[i]);
		}
		if(i==-1){
			Main.lost=true;
		}
	}
	
	public Player mutate(){
		ArrayList<Float> newWeights = new ArrayList<>();
		ArrayList<Integer> newDir = new ArrayList<>();
		for (int i = 0; i < 16; i++) {
			newWeights.add((float) (weights.get(i)+(Math.random()-0.5)*2*MAX_CHANGE));
			if(Math.random()<=DIR_CHANGE_PROB){
				newDir.add((int) (Math.random()*4));
			}else{
				newDir.add(dir.get(i));
			}
		}
		return new Player(newWeights, newDir);
	}
	
	static class ArrayIndexComparator<T extends Comparable<T>> implements Comparator<Integer>
	{
	    private final T[] array;

	    public ArrayIndexComparator(T[] array)
	    {
	        this.array = array;
	    }

	    public Integer[] createIndexArray()
	    {
	        Integer[] indexes = new Integer[array.length];
	        for (int i = 0; i < array.length; i++)
	        {
	            indexes[i] = i; // Autoboxing
	        }
	        return indexes;
	    }

	    @Override
	    public int compare(Integer index1, Integer index2)
	    {
	         // Autounbox from Integer to int to use as array indexes
	        return array[index1].compareTo(array[index2]);
	    }
	}
}
