import java.util.*;
import java.lang.Math;

public class TreeNode {
    private State state;
    private TreeNode parent;
    private List<TreeNode> children;

    private static final double sqrt2 = Math.sqrt(2);

    public TreeNode() {
        this.state = new State();
        children = new ArrayList<>();
    }

    public TreeNode(int rings) {
        this.state = new State(rings);
        children = new ArrayList<>();
    }

    public TreeNode(State state) {
        this.state = state;
        children = new ArrayList<>();
    }

    public TreeNode(State state, TreeNode parent) {
        this.state = state;
        this.parent = parent;
        children = new ArrayList<>();
    }

    public TreeNode(State state, TreeNode parent, List<TreeNode> children) {
        this.state = state;
        this.parent = parent;
        this.children = children;
    }

    public TreeNode(TreeNode node) {
        this.children = new ArrayList<>();
        this.state = new State(node.getState());
        if (node.getParent() != null)
            this.parent = node.getParent();
        List<TreeNode> childArray = node.getChildren();
        for (TreeNode child : childArray) {
            this.children.add(new TreeNode(child));
        }
    }

    public int mcts(int bestTurns) {
        System.out.println("Starting MCTS.");
        System.out.println(Arrays.deepToString(this.getState().getTowers()));
        TreeNode selected = this;
        System.out.println("Selecting best leaf.");
        int loops = 0;
        while(!selected.isLeaf()) {
            System.out.format("Looping for best child. (at depth: %d)\n", ++loops);
            selected = selected.select();
        }
        System.out.println("Best leaf found.");
        System.out.println("Expanding best leaf.");
        selected.expand();
        System.out.println("Simulating random playout.");
        int turns = selected.simulateRandomPlayout();
        int score = (bestTurns > turns) ? 1 : 0;
        System.out.format("Simulation finished with %d turns.\n", turns);
        System.out.println("Propagating back.");
        selected.backPropagate(score);
        System.out.println("MCTS finished.\n\n\n");
        return turns;
    }

    public void printBestPlay() {
        System.out.println("(Debug) Resulting path by score (not by UCT):");
        TreeNode node = this;
        do {
            double bestScore = -1;
            System.out.println(node.getState().toString());

            for(TreeNode child : node.getChildren()) {
                double score = child.getState().getValue();
                if (score > bestScore) {
                    node = child;
                    bestScore = score;
                }
            }
        } while(!node.isLeaf());
    }

    // PHASE 1: SELECTION
    // select a leaf node using repeated UCB comparison
    private TreeNode select() {
        TreeNode selected = null;
        double bestValue = -Double.MAX_VALUE;

        int pVisits = this.getState().getVisits();
        System.out.format("%d nodes available.\n", children.size());
        System.out.format("Comparing UCT values:");
        for (TreeNode child : children) {
            double uctValue = uctValue(pVisits, child.getState().getValue(), child.getState().getVisits());
            System.out.format("\n%f (" + child.getState() + ")", uctValue);
            if (uctValue > bestValue) {
                selected = child;
                bestValue = uctValue;
            }
        }
        System.out.format("\nSelected %f.\n", bestValue);
        return selected;
    }

    // calculate UCT value as defined by the default UCB formula
    public static double uctValue(int parentVisits, double nodeScore, int nodeVisits) {
        // visit unvisited node, don't divide by zero
        if (nodeVisits == 0) {
            return Integer.MAX_VALUE;
        }
        return (nodeScore / (double) nodeVisits) + sqrt2 * Math.sqrt(Math.log(parentVisits) / (double) nodeVisits);
    }

    // PHASE 2: EXPANSION
    // expand the selected node for every possible move
    private void expand() {
        if(this.getState().isFinished())
            return;

        List<State> possibleStates = this.getState().getPossibleNextStates();
        for(State state : possibleStates) {
            this.children.add(new TreeNode(state, this));
        }
        System.out.format("Detected %d possible plays.", possibleStates.size());
    }

    // PHASE 3: SIMULATION
    // simulate turns until done
    // returns the number of random plays it took to finish the game starting from the given node
    private int simulateRandomPlayout() {
        State tempState = new State(this.getState());
        int turnCount = 0;
        while(!tempState.isFinished()) {
            tempState.randomPlay();
            turnCount++;
        }
        return turnCount;
    }

    // PHASE 4: BACKPROPAGATION
    private void backPropagate(double lastScore) {
        TreeNode tempNode = this;
        while(tempNode != null) {   // loop up till tree node
            tempNode.getState().incVisits();
            System.out.println(tempNode.getState());
            double newScore = tempNode.getState().getValue() + lastScore; // TODO: determine good measure
            System.out.println("New score: " + newScore);
            tempNode.getState().setValue(newScore);
            tempNode = tempNode.getParent();
        }
    }

    public boolean isLeaf() {
        return this.getChildren().size() == 0;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public TreeNode getParent() {
        return parent;
    }

    public void setParent(TreeNode parent) {
        this.parent = parent;
    }

    public List<TreeNode> getChildren() {
        return children;
    }

    public void setChildren(List<TreeNode> children) {
        this.children = children;
    }

    public TreeNode getRandomChildNode() {
        int noOfPossibleMoves = this.children.size();
        int selectRandom = (int) (Math.random() * noOfPossibleMoves);
        return this.children.get(selectRandom);
    }

    public TreeNode getMostVisited() {
        return Collections.max(this.children, Comparator.comparing(c -> { return c.getState().getVisits(); }));
    }

}