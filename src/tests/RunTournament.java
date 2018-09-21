package tests;

import ai.RandomBiasedAI;
import ai.core.AI;
import ai.evaluation.SimpleEvaluationFunction;
import ai.mcts.naivemcts.NaiveMCTS;
import ai.mcts.uct.UCT;
import rts.units.UnitTypeTable;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static tournaments.RoundRobinTournament.runTournament;

public class RunTournament {
    public static void main(String args[]) throws Exception {

        // Set tournament settings
        int rounds = 2;  // Number of rounds in the tournament
        int timeBudget = 100;
        int maxGameLength = 2000;
        boolean fullObservability = true;
        boolean selfMatches = false;  // If true, AIs will be paired up against themselves as well as the others
        boolean timeOutCheck = true;
        boolean preAnalysis = false;  // If true, AIs will have a budget for pre analysis before game starts
        int preAnalysisBudgetFirstTimeInAMap = 0;
        int preAnalysisBudgetRestOfTimes = 0;
        boolean runGC = false;  // If true, Garbage Collector is called at every game tick before every AI action call
        int iterationBudget = -1;  // Set to -1 for infinite
        int playOnlyWithThisAI = -1;  // Set to an index in the AI array if one AI should be in all games

        // Create list of AIs participating in tournament
        List<AI> AIs = new ArrayList<>();

        // Add AIs to list
        AIs.add(new UCT(timeBudget, -1, 100, 20, new RandomBiasedAI(),
                new SimpleEvaluationFunction()));
        AIs.add(new NaiveMCTS(timeBudget, -1, 100, 20, 0.33f, 0.0f, 0.75f,
                new RandomBiasedAI(), new SimpleEvaluationFunction(), true));

        // Create list of maps for tournament
        List<String> maps = new ArrayList<>();
        maps.add("maps/8x8/basesWorkers8x8.xml");

        // Initialize result writing
        String folderForReadWriteFolders = "readwrite";

//        String traceOutputFolder = "traces";
        String traceOutputFolder = null;  // Ignore traces

//        Writer out = new BufferedWriter(new FileWriter(new File("results.txt")));  // Print to file
        Writer out = new PrintWriter(System.out);  // Print to console

//        Writer progress = new BufferedWriter(new FileWriter(new File("progress.txt")));  // Write progress to file
        Writer progress = new PrintWriter(System.out);  // Write progress to console
//        Writer progress = null;  // Ignore progress

        // Run tournament
        runTournament(AIs,playOnlyWithThisAI, maps, rounds, maxGameLength, timeBudget, iterationBudget,
                preAnalysisBudgetFirstTimeInAMap, preAnalysisBudgetRestOfTimes, fullObservability, selfMatches,
                timeOutCheck, runGC, preAnalysis, new UnitTypeTable(), traceOutputFolder, out,
                progress, folderForReadWriteFolders);
    }
}
