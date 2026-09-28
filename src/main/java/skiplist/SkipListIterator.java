package skiplist;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

class SkipListIterator implements Iterator<Map.Entry<String, String>> {

    Node nextNode;

    SkipListIterator(Node firstNode) {
        this.nextNode = firstNode;
    }

    @Override
    public boolean hasNext() {
        return nextNode != null;
    }

    @Override
    public Map.Entry<String, String> next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        Node current = nextNode;
        nextNode = nextNode.forward[0];
        return new AbstractMap.SimpleImmutableEntry<>(current.key, current.value);
    }
}