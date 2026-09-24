package skiplist;

public class Node {
    // Each node of a skip lists has an array of the size of the node's level
    // referencing to the next node in each level
    String key; // For now, the key and the value are going to be just strings, don't know if it will change
    String value;
    Node[] forward; // this array references the next node for each level

    public Node(String key, String value, int level) {
        this.key = key;
        this.value = value;
        forward = new Node[level + 1];
    }
}
