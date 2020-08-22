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
        //System.out.println("Starting MCTS.");
        //System.out.println(Arrays.deepToString(this.getState().getTowers()));
        TreeNode selected = this;
        //System.out.println("Selecting best leaf.");
        int loops = 0;
        while(!selected.isLeaf()) {
            loops++;
            //System.out.format("Looping for best child. (at depth: %d)\n", loops);
            selected = selected.select();
        }
        //System.out.println("Best leaf found.");
        //System.out.println("Expanding best leaf.");
        selected.expand();
        //System.out.println("Simulating random playout.");
        int turns = selected.simulateRandomPlayout();               // turn count of random playout, -1 if bad playout
        turns = turns < 0 ? Integer.MAX_VALUE : turns + loops;      // turn count from root state to end state of random playout
        int score = (bestTurns >= turns) ? 1 : 0;
        //System.out.format("Simulation finished with %d turns.\n", turns);
        //System.out.println("Propagating back.");
        selected.backPropagate(score);
        //System.out.println("MCTS finished.\n\n");
        return Math.min(bestTurns, turns);
    }

    public void printBestPlay() {
        System.out.println("Resulting path by UCT:");
        TreeNode node = this;
        System.out.println(node.getState());
        int turns = 0;
        while(!node.isLeaf()) {
            TreeNode bestNode = null;
            double bestValue = -Double.MAX_VALUE;

            int pVisits = node.getState().getVisits();
            for (TreeNode child : node.getChildren()) {
                double uctValue = uctValue(pVisits, child.getState().getValue(), child.getState().getVisits());
                if (uctValue > bestValue) {
                    bestNode = child;
                    bestValue = uctValue;
                }
            }
            node = bestNode;
            System.out.println(node.getState());
            turns++;
        }
        System.out.format("%d turns total.\n", turns);
    }

    // PHASE 1: SELECTION
    // select a leaf node using repeated UCB comparison
    private TreeNode select() {
        TreeNode selected = null;
        double bestValue = -Double.MAX_VALUE;

        int pVisits = this.getState().getVisits();
        //System.out.format("%d nodes available.\n", children.size());
        //System.out.format("Comparing UCT values:");
        for (TreeNode child : children) {
            double uctValue = uctValue(pVisits, child.getState().getValue(), child.getState().getVisits());
            //System.out.format("\n%f (" + child.getState() + ")", uctValue);
            if (uctValue > bestValue) {
                selected = child;
                bestValue = uctValue;
            }
        }
        //System.out.format("\nSelected %f.\n", bestValue);
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

        // get all states on this path so we can remove them from move consideration
        List<State> previousStates = this.getStatesOnPath();
        possibleStates.removeAll(previousStates);

        // expand node with all considered followup states
        for(State state : possibleStates) {
            this.children.add(new TreeNode(state, this));
        }
        //System.out.format("Determined %d possible plays.\n", possibleStates.size());
    }

    // PHASE 3: SIMULATION
    // simulate turns until done
    // returns the number of random plays it took to finish the game starting from the given node
    // returns -1 if the playout is considered a loss
    private int simulateRandomPlayout() {
        State tempState = new State(this.getState());
        int turnCount = 0;
        if(tempState.isFinished()) {
            return turnCount;
        }
        List<State> previousStates = this.getStatesOnPath();

        while(!tempState.isFinished()) {
            List<State> possibleStates = tempState.getPossibleNextStates();
            possibleStates.removeAll(previousStates);
            if(possibleStates.size() == 0)  // simulation reached a suboptimal state, where we'd have to move to a previous state. This is considered a loss.
                return -1;
            //System.out.println(possibleStates);
            Random rand = new Random();
            tempState = possibleStates.get(rand.nextInt(possibleStates.size()));
            previousStates.add(tempState);
            turnCount++;
        }
        return turnCount;
    }

    // PHASE 4: BACKPROPAGATION
    private void backPropagate(double lastScore) {
        TreeNode tempNode = this;
        while(tempNode != null) {   // loop up till tree node
            tempNode.getState().incVisits();
            //System.out.println(tempNode.getState());
            double newScore = tempNode.getState().getValue() + lastScore; // TODO: determine good measure
            //System.out.println("New score: " + newScore);
            tempNode.getState().setValue(newScore);
            tempNode = tempNode.getParent();
        }
    }

    private List<State> getStatesOnPath() {
        List<State> previousStates = new ArrayList<>();
        TreeNode tempNode = this;
        while(tempNode != null) {   // loop up till tree node
            previousStates.add(tempNode.getState());
            tempNode = tempNode.getParent();
        }
        return previousStates;
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