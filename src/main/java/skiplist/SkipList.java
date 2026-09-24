package skiplist;

import java.util.Objects;
import java.util.Random;

// TODO: range access, estimated bytes, tombstones, concurrency...
public class SkipList {
    private static final int MAX_LEVEL = 16; // For now, we will maintain a top limit for the level

    private final Node head; // Sentinel node
    private final Random random;

    private int level;
    private int size;

    public SkipList() {
        head = new Node(null, null, MAX_LEVEL);
        random = new Random();
        level = 0;
        size = 0;
    }

    public void set(String key, String value) {
        Objects.requireNonNull(key, "A key can not be null");
        Node[] update = new Node[MAX_LEVEL + 1];
        Node current = head;

        // Iterate through each level finding the previous node on each level
        for (int i = level; i >= 0; i--) {
            // While the key to insert follows the current key, advance
            while (current.forward[i] != null && current.forward[i].key.compareTo(key) < 0) {
                current = current.forward[i];
            }
            update[i] = current;
        }

        // First node that could have the same key
        Node next = current.forward[0];

        if (next != null && next.key.equals(key)) {
            next.value = value; // Update value
            return;
        }

        int insertLevel = randomLevel();

        if (insertLevel > level) {
            for (int i = level + 1; i <= insertLevel; i++) {
                update[i] = head;
            }
            level = insertLevel;
        }

        Node newNode = new Node(key, value, insertLevel);
        for (int i = 0; i <= insertLevel; i++) {
            newNode.forward[i] = update[i].forward[i];
            update[i].forward[i] = newNode;
        }

        size++;

    }

    public boolean containsKey(String key) {
        Objects.requireNonNull(key, "A key can not be null");
        Node current = searchKey(key);
        return current != null && current.key.equals(key);
    }

    public String get(String key) {
        Objects.requireNonNull(key, "A key can not be null");
        Node current = searchKey(key);
        if (current != null && current.key.equals(key)) {
            return current.value;
        }
        return null;
    }

    private Node searchKey(String key) {
        Node current = this.head;
        for (int i = level; i >= 0; i--) {
            while (current.forward[i] != null && current.forward[i].key.compareTo(key) < 0) {
                current = current.forward[i];
            }
        }
        current = current.forward[0];
        return current;
    }

    // TODO: in the future tombstones have to be implemented
    public void delete(String key) {
        Objects.requireNonNull(key, "A key can not be null");
        Node[] update = new Node[MAX_LEVEL + 1];
        Node current = this.head;
        for (int i = level; i >= 0; i--) {
            while (current.forward[i] != null && current.forward[i].key.compareTo(key) < 0) {
                current = current.forward[i];
            }
            update[i] = current;
        }
        current = current.forward[0];

        if (current != null && current.key.equals(key)) {
            for (int i = 0; i <= level; i++) {
                if (update[i].forward[i] != current) break;
                update[i].forward[i] = current.forward[i];
            }

            while (level > 0 && head.forward[level] == null) {
                level--;
            }

            size--;
        }
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    // Higher levels are less likely to be chosen
    private int randomLevel() {
        int nodeLevel = 0;
        while (nodeLevel < MAX_LEVEL && random.nextBoolean()) {
            nodeLevel++;
        }
        return nodeLevel;
    }
}
