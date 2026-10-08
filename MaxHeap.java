public class MaxHeap {

    private StudentScore[] heap;
    private int size;
    private int capacity;

    // Constructor
    public MaxHeap(int capacity) {

        this.capacity = capacity;
        this.heap = new StudentScore[capacity];
        this.size = 0;
    }

    // Insert student into Max Heap
    public void insert(StudentScore student) {

        if (size == capacity) {
            resize();
        }

        heap[size] = student;

        int current = size;

        size++;

        // Move element upward
        while (
                current > 0 &&
                heap[current].getScore() >
                heap[parent(current)].getScore()
        ) {

            swap(
                    current,
                    parent(current)
            );

            current = parent(current);
        }
    }

    // Get highest-scoring student
    public StudentScore getMax() {

        if (size == 0) {
            return null;
        }

        return heap[0];
    }

    // Remove highest-scoring student
    public StudentScore removeMax() {

        if (size == 0) {
            return null;
        }

        StudentScore max = heap[0];

        heap[0] = heap[size - 1];

        heap[size - 1] = null;

        size--;

        heapifyDown(0);

        return max;
    }

    // Restore Max Heap property
    private void heapifyDown(int index) {

        while (true) {

            int left = leftChild(index);
            int right = rightChild(index);

            int largest = index;

            if (
                    left < size &&
                    heap[left].getScore() >
                    heap[largest].getScore()
            ) {

                largest = left;
            }

            if (
                    right < size &&
                    heap[right].getScore() >
                    heap[largest].getScore()
            ) {

                largest = right;
            }

            if (largest == index) {
                break;
            }

            swap(index, largest);

            index = largest;
        }
    }

    // Parent index
    private int parent(int index) {
        return (index - 1) / 2;
    }

    // Left child index
    private int leftChild(int index) {
        return (2 * index) + 1;
    }

    // Right child index
    private int rightChild(int index) {
        return (2 * index) + 2;
    }

    // Swap two heap elements
    private void swap(int first, int second) {

        StudentScore temp = heap[first];

        heap[first] = heap[second];

        heap[second] = temp;
    }

    // Increase heap capacity
    private void resize() {

        capacity = capacity * 2;

        StudentScore[] newHeap =
                new StudentScore[capacity];

        for (int i = 0; i < size; i++) {
            newHeap[i] = heap[i];
        }

        heap = newHeap;
    }

    // Current heap size
    public int size() {
        return size;
    }

    // Get element at heap index
    public StudentScore get(int index) {

        if (
                index < 0 ||
                index >= size
        ) {
            return null;
        }

        return heap[index];
    }

    // Clear heap
    public void clear() {

        for (int i = 0; i < size; i++) {
            heap[i] = null;
        }

        size = 0;
    }
}