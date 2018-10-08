/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mc;

import ai.RandomBiasedAI;
import ai.core.AI;
import ai.core.AIWithComputationBudget;
import ai.core.InterruptibleAI;
import ai.core.ParameterSpecification;
import ai.evaluation.EvaluationFunction;
import ai.evaluation.SimpleSqrtEvaluationFunction3;
import rts.GameState;
import rts.PlayerAction;
import rts.PlayerActionGenerator;
import rts.units.UnitTypeTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author santi
 */
public class MonteCarlo extends AIWithComputationBudget implements InterruptibleAI {

    // Function to evaluate the quality of a state after a rollout.
    EvaluationFunction ef = null;
    
    // Inner class to hold cumulative score (accum_evaluation) and number of rollouts (visit_count)
    // that started from a PlayerAction (pa)
    public class PlayerActionTableEntry {
        PlayerAction pa;
        float accum_evaluation = 0;
        int visit_count = 0;
    }
    //and a list with all the entries.
    List<PlayerActionTableEntry> actions = null;


    // Random number generator.
    Random r = new Random();

    //This is a policy
    AI randomAI = new RandomBiasedAI();
    long max_actions_so_far = 0;
    
    PlayerActionGenerator  moveGenerator = null;
    boolean allMovesGenerated = false;

    GameState gs_to_start_from = null; //Starting game state
    int run = 0;
    int playerForThisComputation;
    
    // statistics:
    public long total_runs = 0;
    public long total_cycles_executed = 0;
    public long total_actions_issued = 0;
        
    long MAXACTIONS = 100;
    int MAXSIMULATIONTIME = 1024;
    
    
    public MonteCarlo(UnitTypeTable utt) {
        this(100, -1, 100,
             new RandomBiasedAI(), 
             new SimpleSqrtEvaluationFunction3());
    }

    /**
     * Creates the monte carlo player.
     * @param available_time Time available to make a decision
     * @param playouts_per_cycle Number of iterations allowed for the algorithm.
     * @param lookahead Number of steps in the future the rollout goes for
     * @param policy Policy for the rollouts.
     * @param a_ef Evaluation function.
     */
    public MonteCarlo(int available_time, int playouts_per_cycle, int lookahead, AI policy, EvaluationFunction a_ef) {
        super(available_time, playouts_per_cycle);
        MAXACTIONS = -1;
        MAXSIMULATIONTIME = lookahead;
        randomAI = policy;
        ef = a_ef;
    }

    /**
     * Creates the monte carlo player.
     * @param available_time Time available to make a decision
     * @param playouts_per_cycle Number of iterations allowed for the algorithm.
     * @param lookahead How far in the future the rollout goes.
     * @param maxactions maximum number of actions.
     * @param policy Policy for the rollouts.
     * @param a_ef Evaluation function.
     */
    public MonteCarlo(int available_time, int playouts_per_cycle, int lookahead, long maxactions, AI policy, EvaluationFunction a_ef) {
        super(available_time, playouts_per_cycle);
        MAXACTIONS = maxactions;
        MAXSIMULATIONTIME = lookahead;
        randomAI = policy;
        ef = a_ef;
    }
    
    
    public void printStats() {
        if (total_cycles_executed>0 && total_actions_issued>0) {
            System.out.println("Average runs per cycle: " + ((double)total_runs)/total_cycles_executed);
            System.out.println("Average runs per action: " + ((double)total_runs)/total_actions_issued);
        }
    }
    
    public void reset() {
        moveGenerator = null;
        actions = null;
        gs_to_start_from = null;
        run = 0;
    }    
    
    public AI clone() {
        return new MonteCarlo(TIME_BUDGET, ITERATIONS_BUDGET, MAXSIMULATIONTIME, MAXACTIONS, randomAI, ef);
    }

    /**
     * Gets an action at every tick
     * @param player ID of the player to move. Use it to check whether units are yours or enemy's
     * @param gs the game state where the action should be performed
     * @return PlayerAction with all actions for this step.
     * @throws Exception
     */
    public final PlayerAction getAction(int player, GameState gs) throws Exception
    {
        //Only execute an action if the player can execute any.
        if (gs.canExecuteAnyAction(player)) {

            //Reset MonteCarlo
            startNewComputation(player,gs.clone());

            //Iterate MC as much as possible according to budget
            computeDuringOneGameFrame();

            //Decide on the best action and return it
            return getBestActionSoFar();
        } else {
            //Nothing to do: empty player action
            return new PlayerAction();        
        }       
    }

    /**
     * Resets Monte Carlo to start a new search.
     * @param a_player the current player
     * @param gs the game state where the action will be taken
     * @throws Exception
     */
    public void startNewComputation(int a_player, GameState gs) throws Exception {

        playerForThisComputation = a_player;
        gs_to_start_from = gs;
        moveGenerator = new PlayerActionGenerator(gs,playerForThisComputation);
        moveGenerator.randomizeOrder();
        allMovesGenerated = false;
        actions = null;  
        run = 0;
    }    
    
    
    public void resetSearch() {
        gs_to_start_from = null;
        moveGenerator = null;
        actions = null;
        run = 0;
    }

    /**
     * Iterates
     * @throws Exception
     */
    public void computeDuringOneGameFrame() throws Exception {
        long start = System.currentTimeMillis();
        int nruns = 0;
        long cutOffTime = (TIME_BUDGET>0 ? System.currentTimeMillis() + TIME_BUDGET:0);
        if (TIME_BUDGET<=0) cutOffTime = 0;

        //1. Fill the actions table
        if (actions==null) {
            actions = new ArrayList<>();
            if (MAXACTIONS>0 && moveGenerator.getSize()>2*MAXACTIONS) {
                for(int i = 0;i<MAXACTIONS;i++) {
                    MonteCarlo.PlayerActionTableEntry pate = new MonteCarlo.PlayerActionTableEntry();
                    pate.pa = moveGenerator.getRandom();
                    actions.add(pate);
                }
                max_actions_so_far = Math.max(moveGenerator.getSize(),max_actions_so_far);

            } else {
                PlayerAction pa;
                long count = 0;
                do{
                    pa = moveGenerator.getNextAction(cutOffTime);
                    if (pa!=null) {
                        MonteCarlo.PlayerActionTableEntry pate = new MonteCarlo.PlayerActionTableEntry();
                        pate.pa = pa;
                        actions.add(pate);
                        count++;
                        if (MAXACTIONS>0 && count>=2*MAXACTIONS) break; // this is needed since some times, moveGenerator.size() overflows
                    }
                }while(pa!=null);
                max_actions_so_far = Math.max(actions.size(),max_actions_so_far);

                while(MAXACTIONS>0 && actions.size()>MAXACTIONS) actions.remove(r.nextInt(actions.size()));
            }
        }

        //2. Until the budget is over, do another monte carlo rollout.
        while(true) {
            if (TIME_BUDGET>0 && (System.currentTimeMillis() - start)>=TIME_BUDGET) break;
            if (ITERATIONS_BUDGET>0 && nruns>=ITERATIONS_BUDGET) break;
            monteCarloRun(playerForThisComputation, gs_to_start_from);
            nruns++;
        }

        total_cycles_executed++;
    }

    /**
     * Executes a monte carlo rollout.
     * @param player  this player
     * @param gs state to roll the state from.
     * @throws Exception
     */
    public void monteCarloRun(int player, GameState gs) throws Exception {
        int idx = run%actions.size();
        // Take the next ActionTableEntry to execute
        PlayerActionTableEntry pate = actions.get(idx);

        // Given the current game state, execute the starting PlayerAction and clone the state
        GameState gs2 = gs.cloneIssue(pate.pa);

        //Make a copy of the resultant state for the rollout
        GameState gs3 = gs2.clone();

        //Perform random actions until time is up for a simulation or the game is over.
        simulate(gs3,gs3.getTime() + MAXSIMULATIONTIME);

        //time holds the difference in time ticks between the initial state and the one reached at the end.
        int time = gs3.getTime() - gs2.getTime();

        //Evaluate the state reached at the end (g3) and correct with a discount factor
        pate.accum_evaluation += ef.evaluate(player, 1-player, gs3)*Math.pow(0.99,time/10.0);
        pate.visit_count++;
        run++;
        total_runs++;
    }

    /**
     * Simulates, according to a policy (this.randomAI), actions until the game is over or we've reached
     * the limited depth specified (time)
     * @param gs Game state to start the simulation from.
     * @param time depth, or number of game ticks, that limits the simulation.
     * @throws Exception
     */
    public void simulate(GameState gs, int time) throws Exception {
        boolean gameover = false;

        do{
            //isComplete() returns true if actions for all units have been issued. When this happens,
            // it advances the state forward.
            if (gs.isComplete()) {
                //cycle() advances the state forward AND returns true if the game is over.
                gameover = gs.cycle();
            } else {
                //Issue actions for BOTH players.
                gs.issue(randomAI.getAction(0, gs));
                gs.issue(randomAI.getAction(1, gs));
            }
        //Continue until the game is over or we've reached the desired depth.
        }while(!gameover && gs.getTime()<time);
    }

    /**
     * Out of all the PlayerActions tried from the current state, this returns the one with the highest average return
     * @return the best PlayerAction found.
     */
    public PlayerAction getBestActionSoFar() {

        PlayerActionTableEntry best = null;

        // Find the best. For each action in the table:
        for(PlayerActionTableEntry pate:actions) {
            //If the average return is higher (better) than the current best, keep this one as current best.
            if (best==null || (pate.accum_evaluation/pate.visit_count)>(best.accum_evaluation/best.visit_count)) {
                best = pate;
            }
        }

        //This shouldn't happen. Essentially means there's no entry in the table. Escape by applying random actions.
        if (best==null) {
            MonteCarlo.PlayerActionTableEntry pate = new MonteCarlo.PlayerActionTableEntry();
            pate.pa = moveGenerator.getRandom();
            System.err.println("MonteCarlo.getBestActionSoFar: best action was null!!! action.size() = " + actions.size());
        }
        
        total_actions_issued++;

        //Return best action.
        return best.pa;        
    }

    
    
    public String toString() {
        return getClass().getSimpleName() + "(" + TIME_BUDGET + "," + ITERATIONS_BUDGET + "," +  MAXSIMULATIONTIME + "," + MAXACTIONS + ", " + randomAI + ", " + ef + ")";
    }
    
    
    @Override
    public List<ParameterSpecification> getParameters()
    {
        List<ParameterSpecification> parameters = new ArrayList<>();
        
        parameters.add(new ParameterSpecification("TimeBudget",int.class,100));
        parameters.add(new ParameterSpecification("IterationsBudget",int.class,-1));
        parameters.add(new ParameterSpecification("PlayoutLookahead",int.class,100));
        parameters.add(new ParameterSpecification("MaxActions",long.class,100));
        parameters.add(new ParameterSpecification("playoutAI",AI.class, randomAI));
        parameters.add(new ParameterSpecification("EvaluationFunction", EvaluationFunction.class, new SimpleSqrtEvaluationFunction3()));
        
        return parameters;
    }       
    
    
    public int getPlayoutLookahead() {
        return MAXSIMULATIONTIME;
    }
    
    
    public void setPlayoutLookahead(int a_pola) {
        MAXSIMULATIONTIME = a_pola;
    }


    public long getMaxActions() {
        return MAXACTIONS;
    }
    
    
    public void setMaxActions(long a_ma) {
        MAXACTIONS = a_ma;
    }


    public AI getplayoutAI() {
        return randomAI;
    }
    
    
    public void setplayoutAI(AI a_dp) {
        randomAI = a_dp;
    }
    
    
    public EvaluationFunction getEvaluationFunction() {
        return ef;
    }
    
    
    public void setEvaluationFunction(EvaluationFunction a_ef) {
        ef = a_ef;
    }      
}
