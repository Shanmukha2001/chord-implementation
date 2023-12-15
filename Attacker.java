package chord;

import java.util.*;

class AttackerNode {

	int id;
	AttackerNode predecessor;
	AttackerNode successor;
	HashMap<Integer, AttackerNode> fingerTable;
	boolean isAlive;
	static int bits = 3;
	static int showBits = 3;
	static int numberOfNodes = 0;
	boolean isMalicious = false;

	public AttackerNode(int id) {
		this.id = id;
		this.fingerTable = new HashMap<>();
	}

	void join(AttackerNode root) {
		if (root == null) {
			this.predecessor = this;
			this.successor = this;
		} else {
			AttackerNode successorNode = root.findSuccessor(this.id);
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

	AttackerNode findSuccessor(int id) {
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
			AttackerNode n0 = closestPrecedingNode(id);
			if (n0.id == id)
				return this;
			if (n0 == this)
				return this.successor;
			return n0.findSuccessor(id);
		}
	}

	AttackerNode closestPrecedingNode(int id2) {
		for (int i = showBits; i >= 1; i--) {
			AttackerNode finger = fingerTable.get(i);
			if (finger != null && this.id < finger.id && finger.id < id2 && finger.isAlive)
				return finger;
		}
		return this.successor;
	}

	void stabilize() {
		AttackerNode x = this.successor.predecessor;
		if (x != null && this.id < x.id && x.id < this.successor.id && x.isAlive)
			this.successor = x;
		this.successor.notify(this);
	}

	void notify(AttackerNode node) {
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
		for (int i = 1; i <= showBits; i++)
			if (fingerTable.get(i) != null)
				System.out.println(this.id + "  " + ((this.id + (int) Math.pow(2, i - 1)) % (int) Math.pow(2, bits))
						+ " " + this.fingerTable.get(i).id);
	}
}
public class Attacker {

	static ArrayList<AttackerNode> nodes = new ArrayList<>();
	static ArrayList<AttackerNode> nonMalNodes = new ArrayList<>();
	static ArrayList<AttackerNode> malNodes = new ArrayList<>();

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);

		AttackerNode node1 = null;
		node1 = new AttackerNode(1);
		node1.isMalicious = true;
		node1.join(null);
		nodes.add(node1);

		AttackerNode node2 = new AttackerNode(2);
		node2.join(node1);
		nodes.add(node2);

		AttackerNode node3 = new AttackerNode(3);
		node3.isMalicious = true;
		node3.join(node1);
		nodes.add(node3);

		AttackerNode node4 = new AttackerNode(4);
		node4.join(node1);
		nodes.add(node4);

		AttackerNode node5 = new AttackerNode(5);
		node5.isMalicious = true;
		node5.join(node1);
		nodes.add(node5);

		System.out.println("Adding node 1,3,5 as malicious nodes");
		System.out.println("Adding node 2,4 as normal nodes");
		System.out.println("\n\n Fixing Fingers and stabilising");
		for (AttackerNode n : nodes) {
			n.fixFinger();
			n.stabilize();
		}

		System.out.println("Before starting attack");
		for (AttackerNode n : nodes) {
			System.out.println(n.id + " isMalicious:" + n.isMalicious);
			n.showFingerTable();
		}

		System.out.println("Starting Attack");
		AttackerNode tempRoot = node1;
		while (tempRoot.successor != node1) {
			AttackerNode tempRootSucc = null;
			if (!tempRoot.successor.isMalicious)
				tempRootSucc = findMalNode(tempRoot.successor);
			else
				tempRootSucc = tempRoot.successor;
			tempRootSucc.predecessor = tempRoot;
			tempRoot.successor = tempRootSucc;
			tempRoot = tempRootSucc;
		}
		
		for (AttackerNode n : nodes) {
			n.fixFinger();
			n.stabilize();
		}

		System.out.println("After starting attack");
		for (AttackerNode n : nodes) {
			System.out.println(n.id + " isMalicious:" + n.isMalicious);
			n.showFingerTable();
		}
		sc.close();
	}

	private static AttackerNode findMalNode(AttackerNode currNode) {
		return (currNode.successor.isMalicious) ? currNode.successor : findMalNode(currNode.successor);
	}

	protected static void stabiliseAndFixTable() {
		for (AttackerNode node : nodes) {
			node.stabilize();
			node.fixFinger();
		}
		System.out.println("chain");
		if (nodes.size() > 0) {
			AttackerNode n = nodes.get(0);

			for (AttackerNode nx : nodes) {
				System.out.print(n.id + " " + n.successor.id + " => ");
				n = n.successor;
			}
		}
	}

	protected static void showOptions() {
		System.out.println("1.Add Node(use \033[3mAdd node_id\033[0m)");
		System.out.println("2.Leave Node(use \033[3mLeave node_id\033[0m)");
		System.out.println("3.Show table(use \033[3mShow node_id\033[0m)");
		System.out.println("4.Show all fingerTable(use \033[3mShow -1\033[0m)");
		System.out.println("5.exit (exit to quit)");
	}
}