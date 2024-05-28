package grid;

import java.util.logging.Logger;

import cartago.*;
import jason.environment.grid.Location;

import grid.util.Pathfinder;
import model.AgentInfo;
import service.AgentDB;
import util.PropertiesLoader;
import simulations.Simulation;

import java.util.logging.Logger;

public class GridWorld extends Artifact {
    private static final Logger logger = Logger.getLogger(GridWorld.class.getName());

    private GridView view;
    private AgentDB agentDB;
    private Simulation simulation;
    private int totalSheepCount;

    void init(int size, int corralWidth, int corralHeight, boolean drawCoords) {
        agentDB = new AgentDB();
        GridModel.create(size, corralWidth, corralHeight, agentDB);
        commonInit(drawCoords);
    }

    void init(String filePath, boolean drawCoords) {
        agentDB = new AgentDB();
        GridModel.create(filePath, agentDB);
        commonInit(drawCoords);
    }

    void commonInit(boolean drawCoords) {
        GridModel model = GridModel.getInstance();
        view = new GridView(model, agentDB, drawCoords);
    }

    @OPERATION
    void nextStep(int targetX, int targetY, OpFeedbackParam<Integer> newX, OpFeedbackParam<Integer> newY) {
        AgentInfo agent = agentDB.getAgentByCartagoId(this.getCurrentOpAgentId().getLocalId());
        GridModel model = GridModel.getInstance();
        Pathfinder pathfinder = Pathfinder.getInstance(agent.getAgentType());
        Location startPos = model.getAgPos(agent.getCartagoId());
        Location targetPos = new Location(targetX, targetY);
        Location nextPos = pathfinder.getNextPosition(startPos, targetPos);
        logger.info("nextStep called by " + agent.getJasonId() + " from " + startPos + " to " + targetPos
                + " calced next move -> "
                + nextPos);
        moveTo(agent, nextPos, newX, newY);
    }

    private void moveTo(AgentInfo agent, Location location, OpFeedbackParam<Integer> newX,
            OpFeedbackParam<Integer> newY) {
        GridModel model = GridModel.getInstance();

        // Technically the pathfinder should only calculate valid moves, but we might run into some concurrency issues
        // if (model.isFree(location)) {
        if (!model.getObstacleMap().isObstacle(location, agent.getAgentType())) {
            // logger.info("move successful");
            int agentCartagoId = agent.getCartagoId();
            model.setAgPos(agentDB.getAgentByCartagoId(agentCartagoId), location);
            newX.set(location.x);
            newY.set(location.y);
            signal("mapChanged");
        } else {
            logger.warning("MOVE FAILED!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            failed("move_failed");
        }
    }

    @OPERATION
    private void initAgent(String name, OpFeedbackParam<Integer> X, OpFeedbackParam<Integer> Y,
            OpFeedbackParam<Integer> waitTime) {
        AgentInfo agent = agentDB.addAgent(this.getCurrentOpAgentId().getLocalId(), name);
<<<<<<< HEAD
        totalSheepCount += agent.getAgentType() == GridModel.SHEEP ? 1 : 0;
=======
        waitTime.set(loadAgentWaitTime(agent));
>>>>>>> main
        Location loc = GridModel.getInstance().initAgent(agent);
        X.set(loc.x);
        Y.set(loc.y);
        moveTo(agent, loc, X, Y);
    }

    <<<<<<<HEAD

    @OPERATION
    void sheepCaptured() {
        if (simulation == null) {
            return;
        }

        AgentInfo agent = agentDB.getAgentByCartagoId(this.getCurrentOpAgentId().getLocalId());
        simulation.sheepCaptured(agent);
        if (totalSheepCount == simulation.getSheepCapturedCount()) {
            signal("simulationEnded");
        }
    }

    @OPERATION
    private void startSimulation() {
        simulation = new Simulation();
        simulation.start();
    }

    @OPERATION
    private void endSimulation() {
        simulation.end(totalSheepCount);
    }=======

    private Integer loadAgentWaitTime(AgentInfo agent) {
        PropertiesLoader loader = PropertiesLoader.getInstance();
        Integer sheepWaitTime = loader.getProperty("sheep_wait_duration", Integer.class);
        switch (agent.getAgentType()) {
            case GridModel.SHEEP:
                return sheepWaitTime;
            case GridModel.HOUND:
                Double houndWaitRatio = loader.getProperty("hound_wait_ratio", Double.class);
                Integer houndWaitTime = (int) (sheepWaitTime * houndWaitRatio);
                return houndWaitTime;
            default:
                throw new IllegalArgumentException("Invalid agent type: " + agent.getAgentType());
        }
    }>>>>>>>main
}