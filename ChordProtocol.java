package chord;

import java.util.*;

class Node {

	int id;
	Node predecessor;
	Node successor;
	HashMap<Integer, Node> fingerTable;
	boolean isAlive;
	static int bits = 3;
	static int showBits = 3;
	static int numberOfNodes = 0;

	public Node(int id) {
		this.id = id;
		this.fingerTable = new HashMap<>();
	}

	void join(Node root) {
		if (root == null) {
			this.predecessor = this;
			this.successor = this;
		} else {
			Node successorNode = root.findSuccessor(this.id);
			this.predecessor = successorNode.predecessor;
			this.successor = successorNode;

			if (this.successor != null)
				this.successor.predecessor = this;
			if (this.predecessor != null)
				this.predecessor.successor = this;

			this.fixFinger();
//			this.showFingerTable();
		}
		numberOfNodes++;
		updateBits();
		isAlive = true;
	}

	private void updateBits() {
		showBits = (int) Math.ceil((Math.log(numberOfNodes) / Math.log(2)));
		System.out.println("bits " + bits);
		if (showBits < 3)
			showBits = 3;
	}

	public void leave() {
		if (this.predecessor != null) {
			this.predecessor.fingerTable.put(1, this.fingerTable.get(1));
			this.predecessor.successor = this.fingerTable.get(1);
		}
		if (this.fingerTable.get(1) != null) {
			this.fingerTable.get(1).predecessor = this.predecessor;
		}
		this.isAlive = false;
		numberOfNodes--;
		updateBits();
	}

	Node findSuccessor(int id) {
		if (id == 0) {
			if (this.id > this.successor.id)
				return this.successor;
			return this.successor.findSuccessor(id);
		}
		if (id == this.id)
			return this.successor;
		if ((this.id == this.successor.id)
				|| ((this.id > this.successor.id && (this.id < id || id <= this.successor.id))
						|| (this.id < this.successor.id && this.id < id && id <= this.successor.id))
						&& this.successor.isAlive) {
			return this.successor;

		} else {
			Node n0 = closestPrecedingNode(id);
			if (n0.id == id)
				return this;
			if (n0 == this)
				return this.successor;
			return n0.findSuccessor(id);
		}
	}

	Node closestPrecedingNode(int id2) {
		for (int i = showBits; i >= 1; i--) {
			Node finger = fingerTable.get(i);
			if (finger != null && this.id < finger.id && finger.id < id2 && finger.isAlive)
				return finger;
		}
		return this.successor;
	}

	void stabilize() {
		Node x = this.successor.predecessor;
		if (x != null && this.id < x.id && x.id < this.successor.id && x.isAlive)
			this.successor = x;
		this.successor.notify(this);
	}

	void notify(Node node) {
		if (this.predecessor == null || (this.predecessor.id < node.id && node.id < this.id))
			this.predecessor = node;
	}

	void fixFinger() {
		for (int i = 1; i <= bits; i++) {
			int id = (this.id + (int) Math.pow(2, i - 1)) % (int) Math.pow(2, bits);
			this.fingerTable.put(i, findSuccessor(id));
		}
		this.successor = this.fingerTable.get(1);
	}

	void showFingerTable() {
		System.out.println("Node :"+this.id);
		for (int i = 1; i <= showBits; i++)
			if (fingerTable.get(i) != null)
				System.out.println(this.id + "  " + ((this.id + (int) Math.pow(2, i - 1)) % (int) Math.pow(2, bits))
						+ " " + this.fingerTable.get(i).id);
	}
}

public class ChordProtocol {

	static ArrayList<Node> nodes = new ArrayList<>();

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		Node root = null;
		
		System.out.println("Enter max number of users");
		int two_pow_n = sc.nextInt();
		sc.nextLine();
		int n = (int) (Math.log(two_pow_n) / Math.log(2));

		showOptions();
		String input = sc.nextLine();
		while (!input.equalsIgnoreCase("exit")) {
			String[] parts = input.split(" ");
			String option = parts[0];
			int id = Integer.parseInt(parts[1]);
			switch (option.toUpperCase().charAt(0)) {
			case 'A': {
				Node newNode = new Node(id);
				nodes.add(newNode);
				if (nodes.size() == 1) {
					root = newNode;
					root.bits = (n<1) ? 3:n;
					newNode.join(null);
				} else
					newNode.join(root);
				break;
			}
			case 'L': {
				ArrayList<Node> removedNodes = new ArrayList<>();
				for (Node currNode : nodes) {
					if (currNode.id == id) {
						removedNodes.add(currNode);
						currNode.leave();
					}
				}
				nodes.removeAll(removedNodes);
				break;
			}
			case 'S': {
				if (id == -1)
					for (Node ns : nodes) {
						System.out.println(ns.id);
						ns.showFingerTable();
					}
				else
					for (Node currNode : nodes) {
						if (currNode.id == id)
							currNode.showFingerTable();
					}
				break;
			}
			default:
				System.out.println("Unexpected value: " + option);
			}
			showOptions();
			stabiliseAndFixTable();
			input = sc.nextLine();
		}

		sc.close();
	}

	protected static void stabiliseAndFixTable() {
		for (Node node : nodes) {
//			System.out.println("Before fix finger");
//			node.showFingerTable();
//			System.out.println("After fix finger");
			node.fixFinger();
			node.stabilize();
//			node.showFingerTable();
		}

		System.out.println("chain");
		if (nodes.size() > 0) {
			Node n = nodes.get(0);

			for (Node nx : nodes) {
				System.out.print(n.id + " " + n.successor.id + " => ");
				n = n.successor;
			}
		}
		System.out.println();
	}

	protected static void showOptions() {
		System.out.println("1.Add Node(use \033[3mAdd node_id\033[0m)");
		System.out.println("2.Leave Node(use \033[3mLeave node_id\033[0m)");
		System.out.println("3.Show table(use \033[3mShow node_id\033[0m)");
		System.out.println("4.Show all fingerTable(use \033[3mShow -1\033[0m)");
		System.out.println("5.exit (exit to quit)");
	}
}