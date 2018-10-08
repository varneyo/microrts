package exercise8;

import ai.abstraction.*;
import ai.abstraction.pathfinding.AStarPathFinding;
import ai.core.AI;
import ai.core.ParameterSpecification;
import rts.*;
import rts.units.Unit;
import rts.units.UnitType;
import rts.units.UnitTypeTable;
import util.Pair;

import java.util.*;

public class PlayerAbstractActionGenerator extends AbstractionLayerAI{

    static Random r = new Random();

    GameState gameState;
    PhysicalGameState physicalGameState;
    ResourceUsage base_ru;
    List<Pair<Unit,List<AbstractAction>>> choices;
    PlayerAction lastAction = null;
    long size;  // this will be capped at Long.MAX_VALUE;
    long generated;
    int choiceSizes[];
    int currentChoice[];
    boolean moreActions;

    //Variables to hold all types of units (this is useful for functions, and also
    // faster than comparing Strings.
    public UnitType workerType, baseType, barracksType;
    public UnitType rangedType, lightType, heavyType;
    public UnitType resourceType;


    // Helper class to create abstract actions
    AbstractActionBuilder actBuilder;

    // We count here how many units of each type we have.
    public HashMap<UnitType, Integer> typeCount;

    /**
     * Generating all possible actions for a player in a given state
     *
     * @throws Exception
     */
    public PlayerAbstractActionGenerator(UnitTypeTable utt) {
        super(new AStarPathFinding(), -1,-1);

        //Get unit types.
        workerType = utt.getUnitType("Worker");
        baseType = utt.getUnitType("Base");
        barracksType = utt.getUnitType("Barracks");
        rangedType = utt.getUnitType("Ranged");
        lightType = utt.getUnitType("Light");
        heavyType = utt.getUnitType("Heavy");
        resourceType = utt.getUnitType("Resource");

    }

    public boolean reset(GameState a_gs, int pID) throws Exception {

        moreActions = true;
        size = 1;  // this will be capped at Long.MAX_VALUE;
        generated = 0;
        choiceSizes = null;
        currentChoice = null;

        // Generate the reserved resources:
        base_ru = new ResourceUsage();
        gameState = a_gs;
        physicalGameState = gameState.getPhysicalGameState();
        actBuilder = new AbstractActionBuilder(this, new AStarPathFinding(), pID);

        this.determineCounts(a_gs, pID);

        for (Unit u : physicalGameState.getUnits()) {
            UnitActionAssignment uaa = gameState.getUnitActions().get(u);
            if (uaa != null) {
                ResourceUsage ru = uaa.action.resourceUsage(u, physicalGameState);
                base_ru.merge(ru);
            }
        }

        //This will hold all the actions that can be executed by this player.
        choices = new ArrayList<>();
        for (Unit u : physicalGameState.getUnits()) {
            if (u.getPlayer() == pID) {
                if ((gameState.getUnitActions().get(u) == null) && (this.getAbstractAction(u) == null))
                {

                    //List of all the actions this unit can execute (in the given game state).
                    List<AbstractAction> l = getAbstractActions(gameState, u);


                    // unit u can do actions in list l. Add to the list of choices.
                    choices.add(new Pair<>(u, l));

                    // make sure we don't overflow (Long.MAX_VALUE/size is the maximum number of pairs we want to consider)
                    long tmp = l.size();
                    if (Long.MAX_VALUE / size <= tmp) {
                        size = Long.MAX_VALUE;
                    } else {
                        size *= (long) l.size();
                    }
                }
            }
        }

        //This shouldn't happen. If I have no units, I should've lost, and all units should have NONE as a possible action
        if (choices.size() != 0) {
//            System.err.println("Problematic game state:");
//            System.err.println(a_gs);
//            throw new Exception(
//                    "Move generator for player " + pID + " created with no units that can execute actions! (status: "
//                            + a_gs.canExecuteAnyAction(0) + ", " + a_gs.canExecuteAnyAction(1) + ")"
//            );
            //We compute the number of possible actions per unit, plus another array to select a current choice for each one of them
            choiceSizes = new int[choices.size()];
            currentChoice = new int[choices.size()];
            int i = 0;
            for(Pair<Unit,List<AbstractAction>> choice:choices) {
                choiceSizes[i] = choice.m_b.size();
                currentChoice[i] = 0;
                i++;
            }

            return true;
        }

        return false;
    }

    /**
     * This function fills the hashmap 'typeCount' with the number of units of each type.
     * It also counts how many resource units are in the map.
     * @param gs The current game state.
     */
    private void determineCounts(GameState gs, int player)
    {
        typeCount = new HashMap<>();
        //All available units
        for(Unit u:gs.getUnits()) {
            //If it's our unit or of type resource
            if (u.getPlayer() == player || u.getType() == resourceType) {
                int value = 0;
                if(typeCount.containsKey(u.getType()))
                    value = typeCount.get(u.getType());

                //Add one to this type.
                typeCount.put(u.getType(), value + 1);
            }
        }
    }

    private List<AbstractAction> getAbstractActions(GameState gs, Unit u) {

        List<AbstractAction> actions = new ArrayList<>();

        //Depending on the type, we check for possible actions
        if(u.getType() == workerType)
        {
            //HARVEST, MOVE, BUILD, ATTACK
            HarvestSingle h = actBuilder.harvestAction(gs, u);
            if (h != null) actions.add(h);

//            Attack att = actBuilder.attackBehaviour(gs, u);
//            if (att != null) actions.add(att);

//            ArrayList<HarvestSingle> allHarvest = actBuilder.allHarvestActions(gs, u);
//            actions.addAll(allHarvest);

            Build b = actBuilder.buildAction(gs, u, barracksType);
            if (b != null) actions.add(b);

        }else if(u.getType() == baseType)
        {
            // TRAIN WORKERS
            Train t = actBuilder.trainAction(gs, u, workerType);
            if (t != null) actions.add(t);

        }else if(u.getType() == barracksType)
        {
            // TRAIN FIGHTING UNITS
            Train t = actBuilder.trainAction(gs, u, lightType);
            if (t != null) actions.add(t);

            t = actBuilder.trainAction(gs, u, heavyType);
            if (t != null) actions.add(t);

            t = actBuilder.trainAction(gs, u, rangedType);
            if (t != null) actions.add(t);

        }
        //TODO 2: Add another condition here so light, heavy and range attack units create Attack abstract actions and
        // includes them in the possible actions for MonteCarlo to consider.





        if(u.getType() != workerType) {
            //We always can do Idle
            Idle idle = new Idle(u);
            actions.add(idle);
        }

        return actions;
    }


    /**
     * Shuffles the list of choices
     */
    public void randomizeOrder() {
        for (Pair<Unit, List<AbstractAction>> choice : choices) {
            Collections.shuffle(choice.m_b);
        }
    }

    /**
     * Increases the index that tracks the next action to be returned
     *
     * @param startPosition
     */
    public void incrementCurrentChoice(int startPosition) {

        //Advance the current choice
        for (int i = 0; i < startPosition; i++)
            currentChoice[i] = 0;
        currentChoice[startPosition]++;

        //check for limits
        if (currentChoice[startPosition] >= choiceSizes[startPosition]) {
            if (startPosition < currentChoice.length - 1) {
                incrementCurrentChoice(startPosition + 1);
            } else {
                //we've reached the last action.
                moreActions = false;
            }
        }
    }

    /**
     * Returns the next PlayerAction for the state stored in this object
     * @param cutOffTime time to stop generationg the action
     * @return
     * @throws Exception
     */
    public PlayerAction getNextAction(HashMap<Unit, AbstractAction> _abs, long cutOffTime) throws Exception {
        int count = 0;
        while(moreActions) {
            boolean consistent = true;

            //Initialize the PlayerAction with no actions assigned and resources being booked previously.
            PlayerAction pa = new PlayerAction();
            pa.setResourceUsage(base_ru.clone());
            _abs.clear();

            int i = choices.size();
            if (i == 0)
                throw new Exception("Move generator created with no units that can execute actions!");

            //While there are units left, we keep iterating through all of them adding an action per unit to our PlayerAction
            while (i > 0) {
                i--;

                //Take the list of actions of this unit (with index 'i')
                Pair<Unit, List<AbstractAction>> unitChoices = choices.get(i);
                // ... and the current action choice for said unit.
                int choice = currentChoice[i];
                Unit u = unitChoices.m_a;
                AbstractAction aa = unitChoices.m_b.get(choice);

                //Let's get the first unit action from an abstract action.
                GameState gsCopy = gameState.clone();
                UnitAction ua = aa.execute(gsCopy);

                if(ua == null)
                {
                    //No action selected. Create WAIT and ... wait.
                    UnitAction wait = new UnitAction(UnitAction.TYPE_NONE);
                    pa.addUnitAction(u, wait);
                    //_abs.put(u,aa);
                }else {

                    //This action may use some resources and positions. We have to make sure those are consistent with
                    //the actions executed before (i.e. don't move a worker where a barracks is being built).
                    ResourceUsage r2 = ua.resourceUsage(u, physicalGameState);
                    if (pa.getResourceUsage().consistentWith(r2, gameState)) {
                        //If it's consistent, add the action to the pa.
                        pa.getResourceUsage().merge(r2);
                        pa.addUnitAction(u, ua);
                        _abs.put(u,aa);
                    } else {
                        //If it's not consistent, we don't add the action and stop the loop
                        consistent = false;
                        break;
                    }
                }

                //if we haven't reached the 'break', iterate to next unit.
            }

            //move the iterator ahead
            incrementCurrentChoice(i);

            //if we ended up with a consistent pa, return.
            if (consistent) {
                lastAction = pa;
                generated++;
                return pa;
            }

            // check if we are over time (only check once every 1000 actions, since currenttimeMillis is a slow call):
            if (cutOffTime > 0 && (count % 1000 == 0) && System.currentTimeMillis() > cutOffTime) {
                lastAction = null;
                return null;
            }

            //Reaching this point means that the current group of actions was not consistent. Let's try the next as long
            // as there are more availabe (moreActions is true).
            count++;
        }
        lastAction = null;
        return null;
    }

    /**
     * Returns a random player action for the game state in this object
     * @return
     */
    public PlayerAction getRandom(HashMap<Unit, AbstractAction> _abs) {
        Random r = new Random();
        _abs.clear();
        PlayerAction pa = new PlayerAction();
        pa.setResourceUsage(base_ru.clone());
        for (Pair<Unit, List<AbstractAction>> unitChoices : choices) {
            List<AbstractAction> l = new LinkedList<AbstractAction>();
            l.addAll(unitChoices.m_b);
            Unit u = unitChoices.m_a;

            boolean consistent = false;
            do {
                AbstractAction aa = l.remove(r.nextInt(l.size()));

                GameState gsCopy = gameState.clone();
                UnitAction ua = aa.execute(gsCopy);

                ResourceUsage r2 = ua.resourceUsage(u, physicalGameState);

                if (pa.getResourceUsage().consistentWith(r2, gameState)) {
                    pa.getResourceUsage().merge(r2);
                    pa.addUnitAction(u, ua);
                    consistent = true;
                    _abs.put(u,aa);
                }
            } while (!consistent);
        }
        return pa;
    }

    /**
     * Finds the index of a given PlayerAction within the list of PlayerActions
     * @param a
     * @return
     */
    public long getActionIndex(PlayerAction a) {
        int choice[] = new int[choices.size()];
        for (Pair<Unit, UnitAction> ua : a.getActions()) {
            int idx = 0;
            Pair<Unit, List<AbstractAction>> ua_choice = null;
            for (Pair<Unit, List<AbstractAction>> c : choices) {
                if (ua.m_a == c.m_a) {
                    ua_choice = c;
                    break;
                }
                idx++;
            }
            if (ua_choice == null)
                return -1;
            choice[idx] = ua_choice.m_b.indexOf(ua.m_b);

        }
        long index = 0;
        long multiplier = 1;
        for (int i = 0; i < choice.length; i++) {
            index += choice[i] * multiplier;
            multiplier *= choiceSizes[i];
        }
        return index;
    }


    @Override
    public PlayerAction getAction(int player, GameState gs) throws Exception {
        return null;
    }

    @Override
    public AI clone() {
        return null;
    }

    @Override
    public List<ParameterSpecification> getParameters() {
        return null;
    }

    public String toString() {
        String ret = "PlayerActionGenerator:\n";
        for(Pair<Unit,List<AbstractAction>> choice:choices) {
            ret = ret + "  (" + choice.m_a + "," + choice.m_b.size() + ")\n";
        }
        ret += "currentChoice: ";
        for(int i = 0;i<currentChoice.length;i++) {
            ret += currentChoice[i] + " ";
        }
        ret += "\nactions generated so far: " + generated;
        return ret;
    }

    /**
     *
     * @return
     */
    public long getGenerated() {
        return generated;
    }

    public long getSize() {
        return size;
    }

    public PlayerAction getLastAction() {
        return lastAction;
    }

    public List<Pair<Unit,List<AbstractAction>>> getChoices() {
        return choices;
    }


    public void inform(HashMap<Unit, AbstractAction> abs) {

        for(Map.Entry<Unit, AbstractAction> ent : abs.entrySet())
        {
            Unit u = ent.getKey();
            AbstractAction aa = ent.getValue();
            aa.reset();

            actions.put(u,aa);
        }

    }
}
