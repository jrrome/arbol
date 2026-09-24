package skiplist;

class Node {
    // Each node of a skip lists has an array of the size of the node's level
    // referencing to the next node in each level
    final String key; // For now, the key and the value are going to be just strings, don't know if it will change
    String value;
    final Node[] forward; // This array references the next node for each level

    Node(String key, String value, int level) {
        this.key = key;
        this.value = value;
        forward = new Node[level + 1];
    }
}
