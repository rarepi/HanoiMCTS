package tohmcts;


import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class MCTSExampleTreeNode {
    static Random r = new Random();
    static int nActions = 5;
    static double epsilon = 1e-6;

    MCTSExampleTreeNode[] children;
    double nVisits, totValue;

    public void selectAction() {
        List<MCTSExampleTreeNode> visited = new LinkedList<MCTSExampleTreeNode>();
        MCTSExampleTreeNode cur = this;
        visited.add(this);
        while (!cur.isLeaf()) {
            cur = cur.select();
            visited.add(cur);
        }
        cur.expand();
        MCTSExampleTreeNode newNode = cur.select();
        visited.add(newNode);
        double value = rollOut(newNode);
        for (MCTSExampleTreeNode node : visited) {
            // would need extra logic for n-player game
            node.updateStats(value);
        }
    }

    public void expand() {
        children = new MCTSExampleTreeNode[nActions];
        for (int i=0; i<nActions; i++) {
            children[i] = new MCTSExampleTreeNode();
        }
    }

    private MCTSExampleTreeNode select() {
        MCTSExampleTreeNode selected = null;
        double bestValue = Double.MIN_VALUE;
        for (MCTSExampleTreeNode c : children) {
            double uctValue = c.totValue / (c.nVisits + epsilon) +
                       Math.sqrt(Math.log(nVisits+1) / (c.nVisits + epsilon)) +
                           r.nextDouble() * epsilon;
            // small random number to break ties randomly in unexpanded nodes
            if (uctValue > bestValue) {
                selected = c;
                bestValue = uctValue;
            }
        }
        return selected;
    }

    public boolean isLeaf() {
        return children == null;
    }

    public double rollOut(MCTSExampleTreeNode tn) {
        // ultimately a roll out will end in some value
        // assume for now that it ends in a win or a loss
        // and just return this at random
        return r.nextInt(2);
    }

    public void updateStats(double value) {
        nVisits++;
        totValue += value;
    }

    public int arity() {
        return children == null ? 0 : children.length;
    }
}