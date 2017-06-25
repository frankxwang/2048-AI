package com.github.thegithubgeek.AI2048;

import java.io.*;
import java.util.*;

import com.github.thegithubgeek.AI2048.Game2048.Tile;
import com.github.thegithubgeek.AI2048.OldPlayer.ArrayIndexComparator;

public class Player implements Serializable{
	ArrayList<Layer> layers = new ArrayList<>();
	FinalLayer finLayer;
	public static final int MAX_INIT_LAYER_NUM = 4;
	public static final float REMOVE_LAYER_PROB = 0.001f;
	public static final float ADD_LAYER_PROB = 0.01f;
	public static final int MAX_INIT_NODES = 8;
	public static final int MIN_NODES = 3;

	public static float NODE_ADD_PROB = 0.5f;
	public static float NODE_REMOVAL_PROB = 0.005f;
	
	public static float MAX_CHANGE = 0.1f;
	public static float INPUT_ADD_PROB = 0.5f;
	public static float INPUT_REMOVAL_PROB = 0.005f;
	
	public static int ARR_LENGTH = 16;
	Player() {
		int layerNum = (int) Math.random() * MAX_INIT_LAYER_NUM;
		int prevNodeNum = ARR_LENGTH;
		for (int i = 0; i < layerNum; i++) {
			layers.add(Layer.genRanLayer(prevNodeNum));
			prevNodeNum = layers.get(i).nodes.size();
		}
		finLayer = new FinalLayer(new ArrayList<Node>());
		for (int i = 0; i < 4; i++) {
			finLayer.nodes.add(Node.genRanNode(prevNodeNum));
		}
	}
	Player(ArrayList<Layer> layers, FinalLayer finLayer){
		this.layers = layers;
		this.finLayer = finLayer;
	}
	public Player mutate(){
		int prevNodeNum = ARR_LENGTH;
		ArrayList<Layer> newLayers = new ArrayList<>();
		for (int i = 0; i < layers.size(); i++) {
			if(!chance(REMOVE_LAYER_PROB)){
				newLayers.add(layers.get(i).mutate(prevNodeNum));
				prevNodeNum = layers.get(i).nodes.size();
			}
		}
		if(chance(ADD_LAYER_PROB)){
			newLayers.add(Layer.genRanLayer(prevNodeNum));
		}
		FinalLayer newFinLayer = finLayer.mutate(prevNodeNum);
		Main.dispGraph.run();
		return new Player(newLayers, newFinLayer);
	}
	public void move(){
		ArrayList<Node> tileNode = new ArrayList<>();
		for (int i = 0; i < ARR_LENGTH; i++) {
			tileNode.add(new Node(null));
			tileNode.get(i).value = Game2048.getTile(i);
		}
		
		Layer myTiles = new Layer(tileNode);
		
		for (int i = 0; i < layers.size(); i++) {
			if (i == 0) {
				layers.get(i).computeLayer(myTiles);
				continue;
			}
			layers.get(i).computeLayer(layers.get(i-1));
		}
		if(layers.size()>0)
			finLayer.computeLayer(layers.get(layers.size()-1));
		
		Float[] vote = {0f,0f,0f,0f};
		for(int i=0; i<vote.length; i++){
			vote[i] = finLayer.nodes.get(i).value;
		}
		Main.dispGraph.run();
		ArrayIndexComparator<Float> comparator = new ArrayIndexComparator<Float>(vote);
		Integer[] indexes = comparator.createIndexArray();
		Arrays.sort(indexes, comparator);
		
		Tile[] curTiles = Game2048.myTiles.clone();
		int i;
		for(i=3; Arrays.equals(curTiles,Game2048.myTiles)&&i>=0; i--){
			Game2048.move(indexes[i]);
			Main.dispGraph.run();
		}
		if(i==-1){
			Main.lost=true;
			Main.dispGraph.run();
		}
	}
	public static boolean chance(float prob){
		return (Math.random()<=prob);
	}
	static class Layer{
		ArrayList<Node> nodes;
		Layer(ArrayList<Node> nodes){
			this.nodes = nodes;
		}
		public static Layer genRanLayer(int prevLayerNodeNum){
			ArrayList<Node> nodes = new ArrayList<>();
			int nodeNum = (int) (Math.random()*MAX_INIT_NODES)+1;
			for(int i=0; i<nodeNum; i++){
				nodes.add(Node.genRanNode(prevLayerNodeNum));
			}
			return new Layer(nodes);
		}
		public Layer mutate(int prevLayerNodeNum){
			ArrayList<Node> newNodes = new ArrayList<>();
			for (int i = 0; i < nodes.size(); i++) {
				if(!(chance(NODE_REMOVAL_PROB)&&nodes.size()>MIN_NODES)){
					newNodes.add(nodes.get(i).mutate(prevLayerNodeNum));
				}
			}
			if(chance(NODE_ADD_PROB)){
				newNodes.add(Node.genRanNode(prevLayerNodeNum));
			}
			Main.dispGraph.run();
			return new Layer(newNodes);
		}
		public void computeLayer(Layer prevLayer){
			for (int i = 0; i < nodes.size(); i++) {
				nodes.get(i).compute(prevLayer);
			}
		}
	}
	static class FinalLayer extends Layer{
		FinalLayer(ArrayList<Node> nodes) {
			super(nodes);
			// TODO Auto-generated constructor stub
		}
		public static float NODE_ADD_PROB = 0f;
		public static float NODE_REMOVAL_PROB = 0f;
		public FinalLayer mutate(int prevLayerNodeNum){
			ArrayList<Node> newNodes = new ArrayList<>();
			for (int i = 0; i < nodes.size(); i++) {
				newNodes.add(nodes.get(i).mutate(prevLayerNodeNum));
			}
			Main.dispGraph.run();
			return new FinalLayer(newNodes);
		}
	}
	static class Node{
		ArrayList<Float> weights;
		float value = 0;
		Node(ArrayList<Float> weights){
			this.weights = weights;
		}
		public static Node genRanNode(int prevNodeNum){
			ArrayList<Float> weights = new ArrayList<>();
			for (int i = 0; i < prevNodeNum; i++) {
				weights.add((float) Math.random());
			}
			return new Node(weights);
		}
		public Node mutate(int prevNodeNum){
			ArrayList<Float> newWeights = new ArrayList<>();
			for (int i = 0; i < weights.size(); i++) {
				newWeights.add((float) (weights.get(i)+(Math.random()-0.5)*2*MAX_CHANGE));
			}
			for(int i=weights.size(); i<prevNodeNum; i++){
				newWeights.add((float) Math.random());
			}
			Main.dispGraph.run();
			return new Node(newWeights);
		}
		public void compute(Layer layer){
			for (int i = 0; i < layer.nodes.size(); i++) {
				if(weights.size()<=i){
					weights.add((float) Math.random());
				}
				value += layer.nodes.get(i).value * weights.get(i);
			}
		}
	}
}
