package grid;

import cartago.*;
import jason.environment.grid.Location;

import grid.util.AgentMoveListener;
import grid.util.Pathfinder;
import model.AgentInfo;
import service.AgentDB;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class GridWorld extends Artifact {
    private static final Logger logger = Logger.getLogger(GridWorld.class.getName());

    GridView view;
    AgentDB agentDB;
    private final List<AgentMoveListener> listeners = new ArrayList<>();

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
        defineObsProperty("gridSize", model.getWidth());
    }

    public void addAgentMoveListener(AgentMoveListener listener) {
        listeners.add(listener);
    }

    private void notifyAgentMoved(Location prevLoc, Location newLoc) {
        synchronized (listeners) {
            for (AgentMoveListener listener : listeners) {
                listener.onAgentMoved(prevLoc, newLoc);
            }
        }
    }

    @OPERATION
    void nextStep(int targetX, int targetY, OpFeedbackParam<Integer> newX, OpFeedbackParam<Integer> newY) {
        // get id
        AgentInfo agent = agentDB.getAgentById(this.getCurrentOpAgentId().getLocalId());
        GridModel model = GridModel.getInstance();
        Pathfinder pathfinder = Pathfinder.getInstance(agent);
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
        if (model.isFree(location)) {
            // logger.info("move successful");
            int agentCartagoId = agent.getCartagoId();
            model.setAgPos(agentDB.getAgentById(agentCartagoId), location);
            newX.set(location.x);
            newY.set(location.y);
            // Location prevPos = model.getAgPos(agentCartagoId);
            // notifyAgentMoved(prevPos, location);
            signal("mapChanged");
        } else {
            logger.warning("MOVE FAILED!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            failed("move_failed");
        }
    }

    @OPERATION
    private void initAgent(String name, OpFeedbackParam<Integer> X, OpFeedbackParam<Integer> Y) {
        AgentInfo agent = agentDB.addAgent(this.getCurrentOpAgentId().getLocalId(), name);
        Pathfinder.createForAgent(this, agent);
        Location loc = GridModel.getInstance().initAgent(agent);
        X.set(loc.x);
        Y.set(loc.y);
        moveTo(agent, loc, X, Y);
    }
}