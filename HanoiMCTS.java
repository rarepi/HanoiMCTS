public class HanoiMCTS {
    private TreeNode root;

    public void main(int constraint, int rings) {
        long startTime = System.currentTimeMillis();
        root = new TreeNode(rings);
        int best = Integer.MAX_VALUE;
        for(int i = 0; i < constraint; i++) {
            best = root.mcts(best);
        }
        long endTime = System.currentTimeMillis();
        System.out.format("Finished %d MCTS runs in %d ms.\n", constraint, endTime-startTime);
        root.printBestPlay();
    }
}
