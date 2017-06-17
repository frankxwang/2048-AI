package com.github.thegithubgeek.AI2048;

import java.util.ArrayList;
import java.util.Arrays;

import com.github.thegithubgeek.AI2048.Game2048.Tile;
import com.github.thegithubgeek.AI2048.Player.ArrayIndexComparator;

public class MultiLayerPlayer {
	ArrayList<Layer> layers = new ArrayList<>();
	public static final int MAX_INIT_LAYER_NUM = 2;
	public static final int MAX_NODES = 2;
	MultiLayerPlayer() {
		int layerNum = (int) Math.random() * MAX_INIT_LAYER_NUM;
		int prevNodeNum = 16;
		for (int i = 0; i < layerNum; i++) {
			layers.add(Layer.genRanLayer(prevNodeNum));
			prevNodeNum = layers.get(i).nodes.size();
		}
	}
	public static void main(String[] args) {
	}
	public void move(){
		ArrayList<Node> tileNode = new ArrayList<>();
		ArrayList<Node> finNode = new ArrayList<>();
		for (int i = 0; i < 16; i++) {
			tileNode.add(new Node(null, null));
			tileNode.get(i).value = Game2048.getTile(i);
		}
		for (int i = 0; i < 4; i++) {
			finNode.add(new Node(null, null));
			finNode.get(i).value = (float) (Math.random() * 4);
		}
		
		Layer myTiles = new Layer(tileNode);
		
		for (int i = 0; i < layers.size(); i++) {
			if (i == 0) {
				layers.get(i).computeLayer(myTiles);
				continue;
			}
			layers.get(i).computeLayer(layers.get(i-1));
		}
		
		Layer fin = new FinalLayer(finNode);
		fin.computeLayer(layers.get(layers.size()-1));
		
		Float[] vote = {0f,0f,0f,0f};
		
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
		public static float NODE_ADD_PROB = 0.05f;
		public static float NODE_REMOVAL_PROB = 0.05f;
		ArrayList<Node> nodes;
		Layer(ArrayList<Node> nodes){
			this.nodes = nodes;
		}
		public static Layer genRanLayer(int prevLayerNodeNum){
			ArrayList<Node> nodes = new ArrayList<>();
			int nodeNum = (int) (Math.random()*MAX_NODES);
			for(int i=0; i<nodeNum; i++){
				nodes.add(Node.genRanNode(prevLayerNodeNum));
			}
			return new Layer(nodes);
		}
		public Layer mutate(int prevLayerNodeNum){
			ArrayList<Node> newNodes = new ArrayList<>();
			for (int i = 0; i < nodes.size(); i++) {
				if(!chance(NODE_REMOVAL_PROB)){
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
		public Layer mutate(int prevLayerNodeNum){
			ArrayList<Node> newNodes = new ArrayList<>();
			for (int i = 0; i < nodes.size(); i++) {
				if(!chance(NODE_REMOVAL_PROB)){
					newNodes.add(nodes.get(i).mutate(prevLayerNodeNum));
				}
			}
			if(chance(NODE_ADD_PROB)){
				newNodes.add(Node.genRanNode(prevLayerNodeNum));
			}
			return new Layer(newNodes);
		}
	}
	static class Node{
		public static float MAX_CHANGE = 0.1f;
		public static float INPUT_REMOVAL_PROB = 0.05f;
		public static float INPUT_ADD_PROB = 0.05f;
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
			for (int i = 0; i < weights.size(); i++) {
				if(!chance(INPUT_REMOVAL_PROB)){
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
			for (int i = 0; i < inputs.size(); i++) {
				value += layer.nodes.get(inputs.get(i)).value * weights.get(i);
			}
		}
	}
}
