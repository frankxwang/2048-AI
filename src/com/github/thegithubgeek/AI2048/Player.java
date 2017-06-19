package com.github.thegithubgeek.AI2048;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import com.github.thegithubgeek.AI2048.Game2048.Tile;
import com.github.thegithubgeek.AI2048.OldPlayer.ArrayIndexComparator;

public class Player {
	ArrayList<Layer> layers = new ArrayList<>();
	FinalLayer finLayer;
	public static final int MAX_INIT_LAYER_NUM = 4;
	public static final float REMOVE_LAYER_PROB = 0.001f;
	public static final float ADD_LAYER_PROB = 0.01f;
	public static final int MAX_NODES = 8;

	public static float NODE_ADD_PROB = 0.5f;
	public static float NODE_REMOVAL_PROB = 0.005f;
	
	public static float MAX_CHANGE = 0.1f;
	public static float INPUT_ADD_PROB = 0.5f;
	public static float INPUT_REMOVAL_PROB = 0.005f;
	Player() {
		int layerNum = (int) Math.random() * MAX_INIT_LAYER_NUM;
		int prevNodeNum = 16;
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
		int prevNodeNum = 16;
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
		return new Player(newLayers, newFinLayer);
	}
	public void move(){
		ArrayList<Node> tileNode = new ArrayList<>();
		for (int i = 0; i < 16; i++) {
			tileNode.add(new Node(null, null));
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
			int nodeNum = (int) (Math.random()*MAX_NODES)+1;
			for(int i=0; i<nodeNum; i++){
				nodes.add(Node.genRanNode(prevLayerNodeNum));
			}
			return new Layer(nodes);
		}
		public Layer mutate(int prevLayerNodeNum){
			ArrayList<Node> newNodes = new ArrayList<>();
			for (int i = 0; i < nodes.size(); i++) {
				if(!chance(NODE_REMOVAL_PROB)&&nodes.size()>1){
					newNodes.add(nodes.get(i).mutate(prevLayerNodeNum));
				}
			}
			if(chance(NODE_ADD_PROB)){
				newNodes.add(Node.genRanNode(prevLayerNodeNum));
			}
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
				if(!chance(NODE_REMOVAL_PROB)&&nodes.size()>1){
					newNodes.add(nodes.get(i).mutate(prevLayerNodeNum));
				}
			}
			if(chance(NODE_ADD_PROB)){
				newNodes.add(Node.genRanNode(prevLayerNodeNum));
			}
			return new FinalLayer(newNodes);
		}
	}
	static class Node{
		ArrayList<Float> weights;
		ArrayList<Integer> inputs;
		float value = 0;
		Node(ArrayList<Float> weights, ArrayList<Integer> inputs){
			this.weights = weights;
			this.inputs = inputs;
		}
		public static Node genRanNode(int maxInput){
			ArrayList<Float> weights = new ArrayList<>();
			ArrayList<Integer> inputs = new ArrayList<>();
			weights.add((float) Math.random());
			inputs.add((int) (Math.random()*maxInput));
			return new Node(weights,inputs);
		}
		public Node mutate(int maxInput){
			ArrayList<Float> newWeights = new ArrayList<>();
			ArrayList<Integer> newInputs = new ArrayList<>();
			for (int i = 0; i < inputs.size(); i++) {
				if(!(chance(INPUT_REMOVAL_PROB)&&inputs.size()>1)){
					newWeights.add((float) (weights.get(i)+(Math.random()-0.5)*2*MAX_CHANGE));
					newInputs.add(inputs.get(i));
				}
			}
			if(chance(INPUT_ADD_PROB)){
				newWeights.add((float) Math.random());
				newInputs.add((int) (Math.random()*maxInput));
			}
			return new Node(newWeights, newInputs);
		}
		public void compute(Layer layer){
			ArrayList<Integer> invalid = new ArrayList<>();
			for (int i = 0; i < inputs.size(); i++) {
				if(inputs.get(i)>=layer.nodes.size()){
					invalid.add(i);
					continue;
				}
				value += layer.nodes.get(inputs.get(i)).value * weights.get(i);
			}
			for (Iterator<Integer> iterator = invalid.iterator(); iterator.hasNext();) {
				Integer val = iterator.next();
				inputs.remove(val);
				weights.remove(val);
			}
		}
	}
}