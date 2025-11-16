package battle;

import java.util.Arrays;

public class Node {

    // you can add a state representation attribute or any other attributes you need
    public int value;

    // Team A state
    public int[] h0; // A healths
    public int[] d0; // A damages

    // Team B state
    public int[] h1; // B healths
    public int[] d1; // B damages

    // Whose turn: 'A' or 'B'
    public char turn;

    // Optional bookkeeping for plan reconstruction
    public Node parent;
    public String actionFromParent; // e.g., "A(0,1)" or "B(2,0)"
    
    // Default constructor
    public Node() {
    }
    // use copyOf 3ashan ma yb2ash fe reference l arrays nafsaha w y3ml shallow copy
    public Node(int[] h0, int[] d0, int[] h1, int[] d1, char turn) {
        this.h0 = (h0 != null) ? Arrays.copyOf(h0, h0.length) : null;
        this.d0 = (d0 != null) ? Arrays.copyOf(d0, d0.length) : null;
        this.h1 = (h1 != null) ? Arrays.copyOf(h1, h1.length) : null;
        this.d1 = (d1 != null) ? Arrays.copyOf(d1, d1.length) : null;
        this.turn = turn;
    }

    // Copy constructor - will be used to make copies of nodes and test changes to them like simulating actions/paths
    public Node(Node other) {
        if (other == null)
            return;
        this.value = other.value;
        this.h0 = (other.h0 != null) ? Arrays.copyOf(other.h0, other.h0.length) : null;
        this.d0 = (other.d0 != null) ? Arrays.copyOf(other.d0, other.d0.length) : null;
        this.h1 = (other.h1 != null) ? Arrays.copyOf(other.h1, other.h1.length) : null;
        this.d1 = (other.d1 != null) ? Arrays.copyOf(other.d1, other.d1.length) : null;
        this.turn = other.turn;
        this.parent = other.parent;
        this.actionFromParent = other.actionFromParent;
    }

    public int getValue() {
        // you are allowed to modify this implementation, but it must return the value of this node.
        return value;
    }
}