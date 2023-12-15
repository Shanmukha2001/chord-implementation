package chord;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		System.out.print("Enter the size of network in terms of 2 power: ");
		int power = sc.nextInt(), nodeID;
		ChordNetwork chordNetwork = new ChordNetwork();
		ChordNetwork.MAX_NODES = (int) Math.pow(2, power);

		int choice;
		while (true) {
			System.out.print(
					"\nMake a choice: \n1) Add a node\n2) Print finger tables\n3) Print finger table of a node\n4) Attack\n5) Exit\nChoice: ");
			choice = sc.nextInt();
			if (choice == 5) {
				break;
			}
			Node1 temp;
			switch (choice) {
			case 1:
				System.out.print("Enter the node id: ");
				nodeID = sc.nextInt();
				if (chordNetwork.nodeList.contains(nodeID)) {
					chordNetwork.nodes.get(chordNetwork.nodeList.indexOf(nodeID)).fakeNode1 = false;
					break;
				}
				temp = new Node1(nodeID, chordNetwork, power);
				chordNetwork.addNode1(temp);
				break;
			case 2:
				for (Node1 node : chordNetwork.nodes.stream().toList()) {
					node.displayFingerTable();
				}
				break;
			case 3:
				System.out.print("Enter the id for which fingerTable is needed: ");
				nodeID = sc.nextInt();
				chordNetwork.nodes.get(chordNetwork.nodeList.indexOf(nodeID)).displayFingerTable();
				break;
			case 4:
				System.out.print("Enter the target node id: ");
				nodeID = sc.nextInt();

				for (int i = 0; i < power; i++) {
					if (chordNetwork.nodeList.contains((nodeID + (int) Math.pow(2, i)) % (int) Math.pow(2, power))) {
						chordNetwork.nodes.get(chordNetwork.nodeList
								.indexOf((nodeID + (int) Math.pow(2, i)) % (int) Math.pow(2, power))).fakeNode1 = true;
						continue;
					}
					temp = new Node1((nodeID + (int) Math.pow(2, i)) % (int) Math.pow(2, power), chordNetwork, power);
					chordNetwork.addNode1(temp);
					temp.fakeNode1 = true;
				}

				Node1 targetNode1 = chordNetwork.nodes.get(chordNetwork.nodeList.indexOf(nodeID));
				targetNode1.updateFingerTable(chordNetwork.nodeList);
				break;
			default:
				System.out.println("Wrong choice");
			}
		}
	}
}

class Node1 {
	int id;
	Node1[] fingerTable;
	ChordNetwork network;
	boolean fakeNode1 = false;

	Node1(int id, ChordNetwork network, int entries) {
		this.id = id;
		this.network = network;
		fingerTable = new Node1[entries];
	}

	public void updateFingerTable(List<Integer> nodeList) {
		for (int i = 0; i < fingerTable.length; i++) {
			int nodeToAdd = (this.id + (int) Math.pow(2, i)) % (int) Math.pow(2, fingerTable.length);
			while (!nodeList.contains(nodeToAdd)) {
				nodeToAdd++;
				if (nodeToAdd >= (int) Math.pow(2, fingerTable.length))
					nodeToAdd = 0;
			}
			fingerTable[i] = network.nodes.get(nodeList.indexOf(nodeToAdd));

		}
	}

	public void displayFingerTable() {
		if (fakeNode1)
			System.out.println("Finger table for Node1 " + id + ": (fakeNode1)");
		else
			System.out.println("Finger table for Node1 " + id + ":");

		for (int i = 0; i < fingerTable.length; i++) {
			System.out.println("Finger[" + i + "]: Node1 " + fingerTable[i].id);
		}
		System.out.println();
	}
}

class ChordNetwork {
	static int MAX_NODES;

	List<Integer> nodeList = new ArrayList<>();
	List<Node1> nodes = new ArrayList<>();

	void addNode1(Node1 node) {
		nodeList.add(node.id);
		nodes.add(node);
		for (Node1 currNode1 : nodes.stream().toList()) {
			currNode1.updateFingerTable(nodeList);
		}
	}
}