package tests;

import ai.CapivaraBot.ai.competition.capivara.Capivara;
import ai.CapivaraBot.ai.competition.capivara.CapivaraPlusPlus;
import ai.RandomBiasedAI;
import ai.abstraction.LightRush;
import ai.abstraction.WorkerRush;
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
        int rounds = 20;                                // Number of rounds in the tournament
        int timeBudget = 100;                          // Time budget allowed per action (default 100ms)
        //int maxGameLength = 2000;                    // NOT IN USE. Maximum game length (default 2000 ticks) [See List<Integer> lengths]
        boolean fullObservability = true;              // Full or partial observability (default true)
        boolean selfMatches = false;                   // If self-play should be used (default false)
        boolean timeOutCheck = true;                   // If the game should count as a loss if a bot times out (default true)
        boolean preAnalysis = true;                    // If bots are allowed to analyse the game before starting (default true)
        int preAnalysisBudgetFirstTimeInAMap = 1000;   // Time budget for pre-analysis if playing first time on a new map (default 1s)
        int preAnalysisBudgetRestOfTimes = 1000;       // Time budget for pre-analysis for all other cases (default 1s)
        boolean runGC = false;                         // If Java Garbage Collector should be called before each player action (default false)
        int iterationBudget = -1;                      // Iteration budget, set to -1 for infinite (default: -1)
        int playOnlyWithThisAI = -1;                   //  AI index in list of AIs, if one AI should be included in all matches played (default -1)

        // Create list of AIs participating in tournament
        List<AI> AIs = new ArrayList<>();

        // Add AIs to list
//        AIs.add(new UCT(timeBudget, -1, 100, 20, new RandomBiasedAI(),
//                new SimpleEvaluationFunction()));
//        AIs.add(new NaiveMCTS(timeBudget, -1, 100, 20, 0.33f, 0.0f, 0.75f,
//                new RandomBiasedAI(), new SimpleEvaluationFunction(), true));

        UnitTypeTable utt = new UnitTypeTable(UnitTypeTable.VERSION_ORIGINAL, UnitTypeTable.MOVE_CONFLICT_RESOLUTION_CANCEL_BOTH);
        AIs.add(new LightRush(utt));
        AIs.add(new WorkerRush(utt));

        // Create list of maps for tournament
        List<String> maps = new ArrayList<>();
        maps.add("maps/BroodWar/(4)BloodBath.scmD.xml");
        maps.add("maps/BroodWar/(4)Andromeda.scxE.xml");
        //maps.add("maps/16x16/TwoBasesBarracks16x16.xml");
        //maps.add("maps/NoWhereToRun9x8.xml");

        List<Integer> lengths = new ArrayList<>();
        lengths.add(5000);
        lengths.add(10000);
        lengths.add(5000);
        lengths.add(2000);

        // Initialize result writing
        String folderForReadWriteFolders = "readwrite";

        String traceOutputFolder = "traces";
        //String traceOutputFolder = null;  // Ignore traces

//        Writer out = new BufferedWriter(new FileWriter(new File("results.txt")));  // Print to file
        Writer out = new PrintWriter(System.out);  // Print to console

//        Writer progress = new BufferedWriter(new FileWriter(new File("progress.txt")));  // Write progress to file
        Writer progress = new PrintWriter(System.out);  // Write progress to console
//        Writer progress = null;  // Ignore progress

        // Run tournament
        runTournament(AIs,playOnlyWithThisAI, maps, rounds, lengths, timeBudget, iterationBudget,
                preAnalysisBudgetFirstTimeInAMap, preAnalysisBudgetRestOfTimes, fullObservability, selfMatches,
                timeOutCheck, runGC, preAnalysis, utt, traceOutputFolder, out,
                progress, folderForReadWriteFolders);
    }
}
