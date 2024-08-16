package grid;

import java.util.logging.Logger;
import java.util.logging.Level;

import cartago.*;
import jason.environment.grid.Location;
import jia.util.common.init_agent;
import grid.util.BypassPathfinder;
import grid.util.HoundPathfinder;
import grid.util.Pathfinder;
import grid.util.SheepPathfinder;
import grid.util.Pathfinder.UnwalkableTargetCellException;
import model.AgentInfo;
import model.ScenarioInfo;
import service.AgentDB;
import util.PropertiesLoader;
import simulations.SimStarter;
import simulations.Simulation;

public class GridWorld extends Artifact {
    private static final Logger logger = Logger.getLogger(GridWorld.class.getName());

    private Simulation simulation;
    private ScenarioInfo scenarioInfo;

    void init(int size, int corralWidth, int corralHeight) {
        GridModel.create(size, corralWidth, corralHeight);
        commonInit();
    }

    void init(String filePath) {
        GridModel.create(filePath);
        commonInit();
    }

    private void commonInit() {
        logger.setLevel(Level.SEVERE);
        GridModel model = GridModel.getInstance();
        PropertiesLoader loader = PropertiesLoader.getInstance();
        Boolean drawCoords = loader.getProperty("draw_coords", Boolean.class);
        new GridView(model, drawCoords);

        Integer sheepWaitTime = loader.getProperty("sheep_wait_duration", Integer.class);
        Double houndWaitRatio = 0.0;
        try {
            houndWaitRatio = init_agent.getWait_ratio();
        } catch (java.rmi.RemoteException e) {
            e.printStackTrace();
        }
        Integer houndWaitTime = (int) (sheepWaitTime * houndWaitRatio);
        scenarioInfo = new ScenarioInfo(sheepWaitTime, houndWaitTime, houndWaitRatio);
    }

    @OPERATION
    void nextStep(int targetX, int targetY, OpFeedbackParam<Integer> newX, OpFeedbackParam<Integer> newY) {
        AgentInfo agent = AgentDB.getInstance().getAgentByCartagoId(this.getCurrentOpAgentId().getLocalId());
        GridModel model = GridModel.getInstance();
        Pathfinder pathfinder = null;
        switch (agent.getAgentType()) {
            case GridModel.SHEEP:
                pathfinder = SheepPathfinder.getInstance();
                break;
            case GridModel.HOUND:
                pathfinder = HoundPathfinder.getInstance();
                break;
            default:
                logger.warning("Unknown agent type!");
                failed("move_failed");
                return;
        }
        Location startPos = model.getAgPos(agent.getCartagoId());
        Location targetPos = new Location(targetX, targetY);
        //logger.info("GridWorld::nextStep called by " + agent.getJasonId() + " from " + startPos + " to " + targetPos);
        try {
            Location nextPos = pathfinder.getNextPosition(startPos, targetPos);
            logger.info("nextStep called by " + agent.getJasonId() + " from " + startPos + " to " + targetPos
                    + " calced next move -> "
                    + nextPos);
            moveTo(agent, startPos, nextPos, newX, newY);
        } catch (UnwalkableTargetCellException e) {
            logger.info("nextStep called by " + agent.getJasonId() + " from " + startPos + " to " + targetPos
                    + " - NO PATH FOUND. Destination is unwalkable.");
            failed("move_failed");
        }
    }

    private void moveTo(AgentInfo agent, Location currentLoc, Location targetLoc, OpFeedbackParam<Integer> newX,
            OpFeedbackParam<Integer> newY) {
        GridModel model = GridModel.getInstance();

        if (!model.getObstacleMap().isBlocked(targetLoc, agent.getAgentType())) {
            int agentCartagoId = agent.getCartagoId();
            agent.onAgentMoved(currentLoc, targetLoc);
            model.setAgPos(AgentDB.getInstance().getAgentByCartagoId(agentCartagoId), targetLoc);
            newX.set(targetLoc.x);
            newY.set(targetLoc.y);
            signal("mapChanged");
        } else {
            logger.warning("MOVE FAILED!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            failed("move_failed");
        }
    }

    @OPERATION
    void initAgent(String name, OpFeedbackParam<Integer> X, OpFeedbackParam<Integer> Y) {
        AgentInfo agent = AgentDB.getInstance().addAgent(this.getCurrentOpAgentId().getLocalId(), name);
        scenarioInfo.addAgent(agent);
        Location loc = GridModel.getInstance().initAgent(agent);
        X.set(loc.x);
        Y.set(loc.y);
        moveTo(agent, new Location(-99, -99), loc, X, Y);
    }

    @OPERATION
    void sheepCaptured() {
        if (simulation == null) {
            return;
        }

        AgentInfo agent = AgentDB.getInstance().getAgentByCartagoId(this.getCurrentOpAgentId().getLocalId());
        simulation.sheepCaptured(agent);
        if (scenarioInfo.getTotalSheepCount() == simulation.getSheepCapturedCount()) {
            signal("simulationEnded");
        }
    }

    @OPERATION
    void startSimulation() {
        simulation = new Simulation(scenarioInfo);
        simulation.start();
    }

    @OPERATION
    void endSimulation() {
        simulation.end();
        SimStarter.endSimulation();
    }
}