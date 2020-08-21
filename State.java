import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

class Tuple<X, Y> {
    public final X first;
    public final Y second;
    public Tuple(X first, Y second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public String toString() {
        return "[" + first +", " + second + "]";
    }
}

public class State {
    private int visits;
    private double value;

    private int[][] towers;

    public State() {
        int stacks = 3;
        int rings = 3;
        this.towers = new int[stacks][rings];

        for(int i = 0;i < rings;i++) {
            this.towers[0][i] = i+1;
        }
        for(int i = 1;i < stacks;i++) {
            for(int j = 0;j < rings;j++) {
                this.towers[i][j] = 0;
            }
        }
    }

    public State(int rings) {
        int stacks = 3;
        this.towers = new int[stacks][rings];

        for(int i = 0;i < rings;i++) {
            this.towers[0][i] = i+1;
        }
        for(int i = 1;i < stacks;i++) {
            for(int j = 0;j < rings;j++) {
                this.towers[i][j] = 0;
            }
        }
    }

    //constructs a copy of the given game state, discarding visit count and score
    public State(State state) {
        int stacks = state.getTowers().length;
        int rings = state.getTowers()[0].length;

        this.towers = new int[stacks][rings];
        for(int i = 0;i < stacks;i++) {
            for(int j = 0;j < rings;j++) {
                this.towers[i][j] = state.getTowers()[i][j];
            }
        }
    }

    //returns a list of possible game states the current one can be played into
    public List<State> getPossibleNextStates() {
        List<State> states = new ArrayList<>();
        List<Tuple<Integer, Integer>> moves = findMoves();
        for(Tuple<Integer, Integer> move : moves) {
            State newState = new State(this);
            newState.move(move.first, move.second);
            states.add(newState);
        }
        return states;
    }

    //returns a complete list of tuples of possible moves in the current game state
    private List<Tuple<Integer, Integer>> findMoves() {
        //System.out.println("Finding moves in " + Arrays.deepToString(this.towers));
        List<Tuple<Integer, Integer>> moves = new ArrayList<>();
        for(int stack = 0; stack < this.towers.length; stack++) {
            for (int ring = 0; ring < this.towers[stack].length; ring++) {
                if (this.towers[stack][ring] > 0) {
                    List<Integer> targets = findValidMovesForRing(stack, this.towers[stack][ring]);
                    for(int t : targets) {
                        moves.add(new Tuple<>(stack, t));
                    }
                    break; //next stack
                }
            }
        }
        //System.out.println("findMoves results: " + moves);
        return moves;
    }

    //returns a list of stack indices the given ringSize can move to
    //TODO maybe drop fromIdx check for parameter simplicity at cost of run time?
    private List<Integer> findValidMovesForRing(int fromIdx, int ringSize) {
        //System.out.format("Finding moves to move ring %d from %d\n", ringSize, fromIdx);
        List<Integer> moveToPositions = new ArrayList<>();

        if(ringSize <= 0)
            return moveToPositions;    //invalid ring size

        for(int stack = 0; stack < this.towers.length; stack++) {
            if(stack == fromIdx)
                continue;   //skip stack we're moving from
            if(this.towers[stack][this.towers[stack].length-1] == 0) {     //empty stack is always valid
                moveToPositions.add(stack);
                continue;  //next stack
            }
            for (int ring = 0; ring < this.towers[stack].length; ring++) {
                if(this.towers[stack][ring] < ringSize && this.towers[stack][ring] != 0)  //found a smaller ring, skip stack
                    break;
                if (this.towers[stack][ring] > ringSize) {
                    moveToPositions.add(stack);
                    break;  //next stack
                }
            }
        }
        return moveToPositions;
    }

    //executes the given move on this game state
    public void move(int from, int to) {
        if (this.towers[from][this.towers[from].length-1] == 0)
            return; //invalid move, "from" stack may not be empty TODO error
        if (this.towers[to][0] != 0)
            return; //invalid move, "to" stack may not be full TODO error

        for (int rpos1 = 0; rpos1 < this.towers[from].length; rpos1++) {
            if (this.towers[from][rpos1] > 0) {                             // found top ring of stack "from"
                if(this.towers[to][this.towers[to].length-1] == 0) {        // stack "to" is empty
                    this.towers[to][this.towers[to].length-1]
                            = this.towers[from][rpos1];                     // place at bottom of stack "to"
                    this.towers[from][rpos1] = 0;                           // remove ring from stack "from"
                } else {                                                    // find top of stack "to"
                    for (int rpos2 = 0; rpos2 < this.towers[to].length; rpos2++) {
                        if (this.towers[to][rpos2] != 0) {                  // found top ring of stack "to"
                            this.towers[to][rpos2 - 1]
                                    = this.towers[from][rpos1];             // place ring on top of stack "to"
                            this.towers[from][rpos1] = 0;                   // remove ring from stack "from"
                            break;  // rings below top ring are irrelevant
                        }
                    }
                }
                break;  // rings below top ring are irrelevant
            }
        }
    }

    public void randomPlay() {
        List<Tuple<Integer, Integer>> moves = findMoves();
        Random rand = new Random();
        Tuple<Integer, Integer> randomMove = moves.get(rand.nextInt(moves.size()));
        this.move(randomMove.first, randomMove.second);
    }

    //returns whether the game is currently finished or not.
    //the game is assumed to be finished if both stack 0 and stack 1 are empty.
    public boolean isFinished() {
        return this.towers[0][this.towers[0].length - 1] == 0
                && this.towers[1][this.towers[1].length - 1] == 0;
    }

    public int getVisits() {
        return visits;
    }

    public void setVisits(int visits) {
        this.visits = visits;
    }

    void incVisits() {
        this.visits++;
    }

    public double getValue() {
        return value;
    }

    public int[][] getTowers() {
        return towers;
    }

    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "VISITS: " + visits + ", SCORE: " + value + ", BOARD: " + Arrays.deepToString(towers);
    }
}