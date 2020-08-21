public class HanoiMCTS {
    private TreeNode root;

    public void main(int constraint, int rings) {
        root = new TreeNode(rings);
        int best = Integer.MAX_VALUE;
        for(int i = 0; i < constraint; i++) {
            best = root.mcts(best);
        }
        root.printBestPlay();
    }
}
