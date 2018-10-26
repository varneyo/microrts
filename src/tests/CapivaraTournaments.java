package tests;
import ai.core.ContinuingAI;
import ai.microRTSbot.src.standard.StrategyTactics;
import  src.tournaments.CustomRoundRobinTournament;

import ai.CapivaraBot.ai.competition.capivara.CapivaraPlusPlus;
import ai.RandomBiasedAI;
import ai.abstraction.LightRush;
import ai.abstraction.WorkerRush;
import ai.ahtn.AHTNAI;
import ai.core.AI;
import ai.evaluation.EvaluationFunction;
import ai.evaluation.SimpleSqrtEvaluationFunction3;
import ai.mcts.naivemcts.NaiveMCTS;
import ai.scv.SCV;
import rts.units.UnitTypeTable;
import tournaments.TournamentMapConfig;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class CapivaraTournaments    {
    public static void main(String args[]) throws Exception {
        int rounds = 5;                                // Number of rounds in the tournament
        boolean fullObservability = true;              // Full or partial observability (default true)
        boolean selfMatches = false;                   // If self-play should be used (default false)
        boolean timeOutCheck = true;                   // If the game should count as a loss if a bot times out (default true)
        boolean preAnalysis = true;                    // If bots are allowed to analyse the game before starting (default true)
        int preAnalysisBudgetFirstTimeInAMap = 2000;   // Time budget for pre-analysis if playing first time on a new map (default 1s)
        int preAnalysisBudgetRestOfTimes = 1000;       // Time budget for pre-analysis for all other cases (default 1s)
        boolean runGC = false   ;                         // If Java Garbage Collector should be called before each player action (default false)
        int iterationBudget = -1;                      // Iteration budget, set to -1 for infinite (default: -1)
        int playOnlyWithThisAI = -1;                   //  AI index in list
        // of AIs, if one AI should be included in all matches played (default -1)

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String todays_date = sdf.format(new Date());
        // Create list of AIs participating in tournament
        List<AI> AIs = new ArrayList<>();
        UnitTypeTable utt = new UnitTypeTable();

        List<Thread> threads= new ArrayList<>();

        // Set Capi Gridsearch features
        int [] controlUnits = {1,2,3,4,5};
        String [] controlUnitBehaviours = {
                "ManagerClosestEnemy",
                "ManagerLessDPS",
                "ManagerLessLife",
                "ManagerMoreDPS",
                "ManagerMoreLife",
                "ManagerRandom",
                "ManagerUnitsMelee"
        };
        List<EvaluationFunction> evaluationFunctionsList = new ArrayList<>();
        evaluationFunctionsList.add(new SimpleSqrtEvaluationFunction3());
        List<AI> capiAIs = new ArrayList<>();
        capiAIs.add(new SCV(utt));
        Boolean performGridSearch = false;

        // Add AIs to list
        // If true perform a gridsearch
        if (performGridSearch){
            for(int c_u:controlUnits){
                for(String b:controlUnitBehaviours) {
                    for(EvaluationFunction eval :evaluationFunctionsList){
                        AIs.add(new CapivaraPlusPlus(utt, 100, -1, 100, 1, 0.3f, 0.0f, 0.4f, 0, new RandomBiasedAI(utt), eval,
                                true, b, c_u, capiAIs, false));
                    }
                }
            }
        }else {
            // Standard Capi
            AIs.add((new CapivaraPlusPlus(utt)));
        }
        //AIs.add(new AN2(utt));
        //AIs.add(new AN1(utt));
        AIs.add(new SCV(utt));
        //AIs.add(new ai.puppet.PuppetSearchMCTS(utt));
        //AIs.add((new StrategyTactics(utt)));
        AIs.add(new LightRush(utt));
        AIs.add(new WorkerRush(utt));
        AIs.add((new NaiveMCTS(utt)));
        //AIs.add(new AHTNAI(utt));

        // Create list of maps for tournament
        TournamentMapConfig map_1 =  new TournamentMapConfig("maps/8x8/basesWorkers8x8A.xml",  true,8,"basesWorkers8x8A");
        TournamentMapConfig map_2 =  new TournamentMapConfig("maps/8x8/FourBasesWorkers8x8.xml", true,8,"FourBasesWorkers8x8");
        TournamentMapConfig map_3 =  new TournamentMapConfig("maps/NoWhereToRun9x8.xml", true,9,"NoWhereToRun9x8");
        TournamentMapConfig map_4 =  new TournamentMapConfig("maps/16x16/TwoBasesBarracks16x16.xml", true,16,"TwoBasesBarracks16x16");
        TournamentMapConfig map_5 =  new TournamentMapConfig("maps/16x16/basesWorkers16x16A.xml", true,24,"basesWorkers16x16A");
        TournamentMapConfig map_6 =  new TournamentMapConfig("maps/DoubleGame24x24.xml", true,24,"DoubleGame24x24");
        TournamentMapConfig map_7 =  new TournamentMapConfig("maps/24x24/basesWorkers24x24A.xml", true,24,"basesWorkers24x24A");
        //TournamentMapConfig map_8 =  new TournamentMapConfig("maps/32x32/basesWorkers32x32A.xml", true,32);
        TournamentMapConfig map_9 =  new TournamentMapConfig("maps/BWDistantResources32x32.xml", true,32,"BWDistantResources32x32");
        TournamentMapConfig map_10 =  new TournamentMapConfig("maps/BroodWar/(4)BloodBath.scmA.xml", true,64,"(4)BloodBathA");
        TournamentMapConfig map_11 =  new TournamentMapConfig("maps/BroodWar/(4)BloodBath.scmB.xml", true,64,"(4)BloodBathB");
        TournamentMapConfig map_12 =  new TournamentMapConfig("maps/BroodWar/(4)BloodBath.scmC.xml", true,64,"(4)BloodBathC");
        TournamentMapConfig map_13 =  new TournamentMapConfig("maps/BroodWar/(4)BloodBath.scmD.xml", true,64,"(4)BloodBathD");
        TournamentMapConfig map_14 =  new TournamentMapConfig("maps/BroodWar/(4)Andromeda.scxE.xml", true,128,"(4)AndromedaE");
        TournamentMapConfig map_15 =  new TournamentMapConfig("maps/BroodWar/(4)CircuitBreaker.scxF.xml", true,128,"(4)CircuitBreakerF");
        TournamentMapConfig map_16 =  new TournamentMapConfig("maps/BroodWar/(4)Fortress.scxA.xml", true,128,"(4)FortressA");
        TournamentMapConfig map_17 =  new TournamentMapConfig("maps/BroodWar/(4)Python.scxB.xml", true,128,"(4)PythonB");
        TournamentMapConfig map_18 =  new TournamentMapConfig("maps/BroodWar/(2)Destination.scxA.xml", true,128,"(2)DestinationA");

        List<TournamentMapConfig> tmapconfig = new ArrayList<>();
        tmapconfig.add(map_1);
        tmapconfig.add(map_2);
        tmapconfig.add(map_3);
        tmapconfig.add(map_4);
        tmapconfig.add(map_5);
        tmapconfig.add(map_6);
        /*tmapconfig.add(map_7);
        //tmapconfig.add(map_8);
        tmapconfig.add(map_9);
        tmapconfig.add(map_10);
        tmapconfig.add(map_11);*/
        /*tmapconfig.add(map_12);
        tmapconfig.add(map_13);
        tmapconfig.add(map_14);
        tmapconfig.add(map_15);
        tmapconfig.add(map_16);
        tmapconfig.add(map_17);
        tmapconfig.add(map_18);*/



        // Initialize result writing
        String folderForReadWriteFolders = "readwrite";
        //String traceOutputFolder = "";  // Ignore traces


        int c_c, cpp_c, svc_c,ps_c, stt_c,lr_c,wr_c,nav_c, ahtn_c;
        c_c= cpp_c= svc_c=ps_c= stt_c=lr_c=wr_c=nav_c= ahtn_c=0;

        String aiNamesConcat="1" ;
        String shortName;
        for (AI ai:AIs) {
            shortName="";
            if (ai.toString().contains("(")){
                //System.out.println(ai.toString());
                shortName = ai.toString().substring(0,ai.toString().indexOf("(",0));

            }else {
                System.out.println(ai.toString());
                shortName = ai.toString();
            }
            if (aiNamesConcat.length()<250){
                switch(shortName){
                    case("Capivara"):
                        aiNamesConcat+=(c_c>0)?"":"_CAPI";
                        c_c++;
                        break;
                    case("CapivaraPlusPlus"):
                        aiNamesConcat+=(cpp_c>0)?"":"_CAPIPP";
                        cpp_c++;
                        break;
                    case("SCV"):
                        aiNamesConcat+="_SCV";
                        svc_c++;
                        break;
                    case("PuppetSearchMCTS"):
                        aiNamesConcat+="_PSMCTS";
                        ps_c++;
                        break;
                    case("StrategyTactics"):
                        aiNamesConcat+="_STT";
                        stt_c++;
                        break;
                    case("LightRush"):
                        aiNamesConcat+="_LR";
                        lr_c++;
                        break;
                    case("WorkerRush"):
                        aiNamesConcat+="_WR";
                        wr_c++;
                        break;
                    case ("NaiveMCTS"):
                        aiNamesConcat+="_NMCTS";
                        nav_c++;
                        break;
                    case ("AHTNAI"):
                        aiNamesConcat+="_AHT";
                        ahtn_c++;
                        break;
                    default:
                        aiNamesConcat+="_"+shortName;
                        break;
                }

            }
        }


        String folder_path = addDirectory("results/"+todays_date.toString(),0);
        String run_path = addDirectory("results/"+todays_date.toString()+"/Run_",1);

        tmapconfig.parallelStream().forEach(tournamentMapConfig ->{} );
        List<CustomRoundRobinTournament> rr = new ArrayList<>();
        for (TournamentMapConfig tmc : tmapconfig) {
            // Create a new folder for the run

            String file_name = aiNamesConcat+"_"+todays_date+".txt";
            System.out.println("The short file name is: " +tmc.shortMapName);
            String mapfolder = addDirectory(run_path+"/"+tmc.shortMapName,0) ;
            String traceOutputFolder = addDirectory(mapfolder+"/traces",0) ;


            Writer out = new BufferedWriter(new FileWriter(new File(mapfolder+"/"+file_name)));  // Print to file
            //Writer out = new PrintWriter(System.out);  // Print to console

            //        Writer progress = new BufferedWriter(new FileWriter(new File("progress.txt")));  // Write progress to file
            Writer progress = new PrintWriter(System.out);  // Write progress to console
            //        Writer progress = null;  // Ignore progress


            /*CustomRoundRobinTournament rr = new CustomRoundRobinTournament(AIs, playOnlyWithThisAI, tmc.getMapAsList(), rounds, tmc.maxGameLength, 1000, iterationBudget,
                    preAnalysisBudgetFirstTimeInAMap, preAnalysisBudgetRestOfTimes, fullObservability, selfMatches,
                    timeOutCheck, runGC, preAnalysis, utt, traceOutputFolder, out,
                    progress, folderForReadWriteFolders);
               */
            rr.add(new CustomRoundRobinTournament(AIs, playOnlyWithThisAI, tmc.getMapAsList(), rounds, tmc.maxGameLength, 100, iterationBudget,
                    preAnalysisBudgetFirstTimeInAMap, preAnalysisBudgetRestOfTimes, fullObservability, selfMatches,
                    timeOutCheck, runGC, preAnalysis, utt, traceOutputFolder, out,
                    progress, folderForReadWriteFolders));

            //out.close();

        }
        for(CustomRoundRobinTournament crr:rr){
            crr.start();
        }
    }



    public static String  addDirectory(String filepath, int runid){
        String newpath ="";
        if (runid>0) {
            newpath =filepath+runid;
        }
        else {
            newpath =filepath;
        }
        Path path = Paths.get(newpath);

        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                //fail to create directory
                e.printStackTrace();
            }
            return (runid>0)?filepath+runid:filepath;
        } else{
            if(runid>0){
                filepath = addDirectory(filepath,runid+1);
            }
        }
        return filepath;
    }




}
