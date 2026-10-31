package skiplist;

import java.util.*;

// TODO: range access, estimated bytes, tombstones, concurrency...

// The Iterator returns key value pairs, hiding the internal Nodes
public class SkipList implements Iterable<Map.Entry<byte[], byte[]>> {
    private static final int MAX_LEVEL = 16; // For now, we will maintain a top limit for the level

    private final Node head; // Sentinel node
    private final Random random;

    private int level;
    private int size;
    private int numberOfNodes;

    public SkipList() {
        this.head = new Node(null, null, MAX_LEVEL);
        this.random = new Random();
        this.level = 0;
        this.size = 0;
        this.numberOfNodes = 0;
    }

    public void put(byte[] key, byte[] value) {
        Objects.requireNonNull(key, "A key can not be null");
        Objects.requireNonNull(value, "A value can not be null");
        Node[] update = new Node[MAX_LEVEL + 1];
        Node current = head;

        // Iterate through each level finding the previous node on each level
        for (int i = level; i >= 0; i--) {
            // While the key to insert follows the current key, advance
            while (current.forward[i] != null
                    && Arrays.compare(current.forward[i].key, key) < 0) {
                current = current.forward[i];
            }
            update[i] = current;
        }

        // First node that could have the same key
        Node next = current.forward[0];

        // Update value
        if (next != null && Arrays.equals(next.key, key)) {
            byte[] storedValue = value.clone();
            this.size = this.size - next.value.length + value.length;
            next.value = storedValue;
            return;
        }

        // Insert nodes
        int insertLevel = randomLevel();

        if (insertLevel > level) {
            for (int i = level + 1; i <= insertLevel; i++) {
                update[i] = head;
            }
            level = insertLevel;
        }

        Node newNode = new Node(key.clone(), value.clone(), insertLevel);
        size += newNode.size();
        for (int i = 0; i <= insertLevel; i++) {
            newNode.forward[i] = update[i].forward[i];
            update[i].forward[i] = newNode;
        }
        numberOfNodes++;
    }

    public boolean containsKey(byte[] key) {
        Objects.requireNonNull(key, "A key can not be null");
        Node current = searchKey(key);
        return current != null && Arrays.equals(current.key, key);
    }

    public byte[] get(byte[] key) {
        Objects.requireNonNull(key, "A key can not be null");
        Node current = searchKey(key);
        if (current != null && Arrays.equals(current.key, key)) {
            return current.value.clone();
        }
        return null;
    }

    private Node searchKey(byte[] key) {
        Node current = this.head;
        for (int i = level; i >= 0; i--) {
            while (current.forward[i] != null && Arrays.compare(current.forward[i].key, key) < 0) {
                current = current.forward[i];
            }
        }
        current = current.forward[0];
        return current;
    }

    // TODO: in the future tombstones have to be implemented
    public void delete(byte[] key) {
        Objects.requireNonNull(key, "A key can not be null");
        Node[] update = new Node[MAX_LEVEL + 1];
        Node current = this.head;
        for (int i = level; i >= 0; i--) {
            while (current.forward[i] != null && Arrays.compare(current.forward[i].key, key) < 0) {
                current = current.forward[i];
            }
            update[i] = current;
        }
        current = current.forward[0];

        if (current != null && Arrays.equals(current.key, key)) {
            size -= current.size();
            for (int i = 0; i <= level; i++) {
                if (update[i].forward[i] != current) break;
                update[i].forward[i] = current.forward[i];
            }

            while (level > 0 && head.forward[level] == null) {
                level--;
            }

            numberOfNodes--;
        }
    }

    @Override
    public Iterator<Map.Entry<byte[], byte[]>> iterator() {
        return new SkipListIterator(this.head.forward[0]);
    }

    public Iterator<Map.Entry<byte[], byte[]>> iteratorFrom(byte[] fromInclusive) {
        Objects.requireNonNull(fromInclusive, "A key can not be null");
        return new SkipListIterator(searchKey(fromInclusive));
    }

    public int size() {
        return size;
    }

    public int getNumberOfNodes() {
        return numberOfNodes;
    }

    public boolean isEmpty() {
        return numberOfNodes == 0;
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
