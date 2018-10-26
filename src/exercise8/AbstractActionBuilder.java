package exercise8;

import ai.abstraction.*;
import ai.abstraction.pathfinding.PathFinding;
import rts.GameState;
import rts.PhysicalGameState;
import rts.Player;
import rts.units.Unit;
import rts.units.UnitType;

import java.util.ArrayList;
import java.util.List;

public class AbstractActionBuilder {

    PathFinding pf;
    PlayerAbstractActionGenerator ai;
    int resourcesUsed;
    int player;

    int MAX_WORKERS = 3;

    public AbstractActionBuilder(PlayerAbstractActionGenerator ai, PathFinding pf, int pID)
    {
        this.pf = pf;
        this.ai = ai;
        this.player = pID;
        this.resourcesUsed = 0;
    }

    public void clearResources() {resourcesUsed = 0;}


    public Train trainAction(GameState gs, Unit builder, UnitType type)
    {
        Player p = gs.getPlayer(player);
        PhysicalGameState pgs = gs.getPhysicalGameState();
        if (p.getResources() >= type.cost + resourcesUsed) {
            resourcesUsed += type.cost;

            if ((type == ai.workerType) && (ai.typeCount.containsKey(ai.workerType)) && (ai.typeCount.get(ai.workerType) >= MAX_WORKERS))
                return null;

            return new Train(builder, type);
        }
        return null;
    }

    public Build buildAction(GameState gs, Unit builder, UnitType type)
    {
        Player p = gs.getPlayer(player);
        PhysicalGameState pgs = gs.getPhysicalGameState();

        //TODO 3: How would you build barracks far from resources, bases and other buildings?

        // build a barracks:
        if (p.getResources() >= type.cost + resourcesUsed) {
            List<Integer> reservedPositions = new ArrayList<Integer>();
            int x = builder.getX();
            int y = builder.getY();

            //buildIfNotAlreadyBuilding
            AbstractAction action = ai.getAbstractAction(builder);
            if (!(action instanceof Build)) {
                int pos = ai.findBuildingPosition(reservedPositions, x, y, p, pgs);
                Build b = new Build(builder, type, pos % pgs.getWidth(), pos / pgs.getWidth(), pf);
                reservedPositions.add(pos);
                resourcesUsed += type.cost;
                return b;
            }
        }
        return null;
    }

    public HarvestSingle harvestAction(GameState gs, Unit harvestWorker)
    {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        Player p = gs.getPlayer(player);
        HarvestSingle h = null;

        if (harvestWorker.getType().canHarvest && harvestWorker.getPlayer() == player) {

            Unit closestBase = null;
            Unit closestResource = null;
            int closestDistance = 0;
            for(Unit u2:pgs.getUnits()) {
                if (u2.getType().isResource) {
                    int d = Math.abs(u2.getX() - harvestWorker.getX()) + Math.abs(u2.getY() - harvestWorker.getY());
                    if (closestResource==null || d<closestDistance) {
                        closestResource = u2;
                        closestDistance = d;
                    }
                }
            }
            closestDistance = 0;
            for(Unit u2:pgs.getUnits()) {
                if (u2.getType().isStockpile && u2.getPlayer()==p.getID()) {
                    int d = Math.abs(u2.getX() - harvestWorker.getX()) + Math.abs(u2.getY() - harvestWorker.getY());
                    if (closestBase==null || d<closestDistance) {
                        closestBase = u2;
                        closestDistance = d;
                    }
                }
            }
            if (closestResource!=null && closestBase!=null) {
                AbstractAction aa = ai.getAbstractAction(harvestWorker);
                if (aa instanceof HarvestSingle) {
                    HarvestSingle h_aa = (HarvestSingle)aa;
                    if (h_aa.getTarget() != closestResource || h_aa.getBase()!=closestBase)
                        h = new HarvestSingle(harvestWorker, closestResource, closestBase, pf);
                    //harvest(harvestWorker, closestResource, closestBase);
                } else {
                    //harvest(harvestWorker, closestResource, closestBase);
                    h = new HarvestSingle(harvestWorker, closestResource, closestBase, pf);
                }
            }
        }

        return h;
    }


    public ArrayList<Harvest> allHarvestActions(GameState gs, Unit harvestWorker)
    {

        ArrayList<Harvest> list = new ArrayList<>();

        //TODO 1: return a list of Harvest objects with all possible combinations resource-pile -> base
        // don't forget to check the base (stockpile) is yours! Don't give resources to the enemy :)

        return list;
    }


    public Attack attackBehaviour(GameState gs, Unit u) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        Player p = gs.getPlayer(player);

        Unit closestEnemy = null;
        int closestDistance = 0;
        for (Unit u2 : pgs.getUnits()) {
            if (u2.getPlayer() >= 0 && u2.getPlayer() != p.getID()) {
                int d = Math.abs(u2.getX() - u.getX()) + Math.abs(u2.getY() - u.getY());
                if (closestEnemy == null || d < closestDistance) {
                    closestEnemy = u2;
                    closestDistance = d;
                }
            }
        }
        if (closestEnemy != null) {
            return new Attack(u, closestEnemy, pf);
        }
        return null;
    }


}
