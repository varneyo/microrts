package tests;

import ai.Ben.StrategyChooser;
import ai.Ben.WorkerRush2;
import ai.Ben.mattRushAi;
import ai.Ben.newAI;
import ai.abstraction.HeavyRush;
import ai.abstraction.LightRush;
import ai.abstraction.RangedRush;
import ai.abstraction.pathfinding.PathFinding;
import ai.core.AI;
import ai.*;
import ai.abstraction.WorkerRush;
import ai.abstraction.pathfinding.BFSPathFinding;
import ai.evaluation.SimpleSqrtEvaluationFunction3;
import exercise5.BotExercise5;
import gui.PhysicalGameStatePanel;
import javax.swing.JFrame;

import rsoneply.RandomSearchOnePlyAgent;
import rts.GameState;
import rts.PhysicalGameState;
import rts.PlayerAction;
import rts.units.UnitTypeTable;

/**
 * @author santi
 */
public class GameVisualSimulationTest {
    public static void main(String args[]) throws Exception {
        UnitTypeTable utt = new UnitTypeTable();

        PhysicalGameState pgs = PhysicalGameState.load("maps/16x16/basesWorkers16x16.xml", utt);  // Set map
//        PhysicalGameState pgs = MapGenerator.basesWorkers8x8Obstacle();

        GameState gs = new GameState(pgs, utt);
        int MAXCYCLES = 5000;  // Maximum length of the game
        int TIME_BUDGET = 20;  // Time budget for AIs
        boolean gameover = false;

        // Set AIs playing the game
        //AI ai1 = new BotExercise5(TIME_BUDGET, -1, utt, new BFSPathFinding());  //new WorkerRush(utt, new BFSPathFinding());

        int lookahead = 100;
        int playouts_per_cycle = -1;
        int inertiaCycles = 10;

        PathFinding pf = new BFSPathFinding();
//        AI ai1 = new StrategyChooser(lookahead, pf, new newAI(utt,pf), new WorkerRush2(utt,pf), new LightRush(utt,pf),
//                new HeavyRush(utt,pf), new RangedRush(utt,pf), new mattRushAi(utt), inertiaCycles);


        AI ai2 = new RandomBiasedAI();

        AI ai1 = new absmc.MonteCarlo(100, -1, 100, new RandomBiasedAI(), new SimpleSqrtEvaluationFunction3(), utt);
//        AI ai2 = new mc.MonteCarlo(100, -1, 100, 1000,
//                new RandomAI(), new SimpleSqrtEvaluationFunction3());


//        AI ai1 = new exercise8.MonteCarlo(100, -1, 10, 1000,
//                new RandomBiasedAI(), new SimpleSqrtEvaluationFunction3(), utt);
//        AI ai2 = new mc.MonteCarlo(100, -1, 10, 1000,
//                new RandomAI(), new SimpleSqrtEvaluationFunction3());


        JFrame w = PhysicalGameStatePanel.newVisualizer(gs,640,640,false,
                                                        PhysicalGameStatePanel.COLORSCHEME_BLACK);
//        JFrame w = PhysicalGameStatePanel.newVisualizer(gs,640,640,false,
//                                                        PhysicalGameStatePanel.COLORSCHEME_WHITE);

        // Play game
        long nextTimeToUpdate = System.currentTimeMillis() + TIME_BUDGET;
        do {
            if (System.currentTimeMillis() >= nextTimeToUpdate) {
                PlayerAction pa1 = ai1.getAction(0, gs);  // Get action from player 1
                PlayerAction pa2 = ai2.getAction(1, gs);  // Get action from player 2

                // Issue actions
                gs.issueSafe(pa1);
                gs.issueSafe(pa2);

                // Game ticks forward
                gameover = gs.cycle();
                w.repaint();
                nextTimeToUpdate+=TIME_BUDGET;
            } else {
                try {
                    Thread.sleep(1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } while (!gameover && gs.getTime() < MAXCYCLES);

        // Tell the AIs the game is over
        ai1.gameOver(gs.winner());
        ai2.gameOver(gs.winner());
        
        System.out.println("Game Over");
    }    
}
