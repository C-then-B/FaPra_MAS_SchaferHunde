package grid;

import cartago.*;
import grid.util.GridProcessor;
import jason.environment.grid.Location;
import jia.Pathfinder;

public class GridWorld extends Artifact {
    GridModel model;
    GridView view;
    Pathfinder pathFinder;

    void init(int size, int corralWidth, int corralHeight) {
        model = new GridModel(size, corralWidth, corralHeight);
        commonInit(model);
    }

    void init(String filePath) {
        model = new GridModel(filePath);
        commonInit(model);
    }

    void commonInit(GridModel model) {
        view = new GridView(model);
        defineObsProperty("gridSize", model.getWidth());
        //defineObsProperty("lastSeenBy", "Observer","SeenAgent", new Location(0, 0));
        pathFinder = new Pathfinder(model);
    }

    @OPERATION
    void dstar() {
        int agentId = this.getCurrentOpAgentId().getLocalId();
        // int newX = Math.random() < 0.5 ? 1 : -1;
        // int newY = Math.random() < 0.5 ? 1 : -1;
        // move(agentId, newX, newY);

        Location startPos = model.getAgPos(agentId);
        // Location targetPos = model.getFreePos();
        Location targetPos = new Location(model.getWidth() - 1, model.getHeight() - 1);
        Location nextPos = pathFinder.getNextPosition(startPos, targetPos);
        moveTo(agentId, nextPos);
    }

    /**
     * This method moves the current agent to the next cell on his way to the specified destination defined by the specified X and Y value.
     * @param targetX Value of the final destination on the X-axis.
     * @param targetY Value of the final destination on the Y-axis.
     * @param newX Out-Parameter to inform the agent of his new position on the X-axis.
     * @param newY Out-Parameter to inform the agent of his new position on the Y-axis.
     */
    @OPERATION
    void nextStep(int targetX, int targetY, OpFeedbackParam<Integer> newX, OpFeedbackParam<Integer> newY) {
        int agentId = this.getCurrentOpAgentId().getLocalId();
        Location startPos = model.getAgPos(agentId);
        Location targetPos = new Location(targetX, targetY);
        Location nextPos = pathFinder.getNextPosition(startPos, targetPos);
        moveTo(agentId, nextPos);
        newX.set(nextPos.x);
        newY.set(nextPos.y);
    }

    private void moveTo(int agentId, Location location) {
        try {
            if (model.isFree(location.x, location.y)) {
                model.setAgPos(agentId, location.x, location.y);
                this.signal("agentMoved", agentId, location.x, location.y);
            }
        } catch (Exception e) {
            failed("move_failed");
        }
    }

    @OPERATION
    void place_sheep() {
        placeAgent();
    }

    @OPERATION
    void place_hound() {
        placeAgent();
    }

    private void placeAgent() {
        int agentId = this.getCurrentOpAgentId().getLocalId();

        System.out.println("HEREEEEEEEEEEE");
        GridProcessor gridProcessor = new GridProcessor(model.getWidth(), model.getHeight());
        gridProcessor.processEntireGrid(
                loc -> model.isFree(loc),
                loc -> {
                    model.setAgPos(agentId, loc);
                },
                c -> c == 1);
    }

    /**
     * This method return the identifier, that is used to identify the agent
     * in the grid.
     * @param ID Identifier of the current agent in the grid artifact.
     */
    @OPERATION
    public void getOwnID(OpFeedbackParam<Integer> ID) {
        ID.set(this.getCurrentOpAgentId().getLocalId());
    }

    /**
     * This method returns the position of the current agent in the grid
     * by setting output parameters.
     * @param X Output-parameter for the coordinates in X axis.
     * @param Y Output-parameter for the coordinates in Y axis.
     */
    @OPERATION
    public void getOwnLocation(OpFeedbackParam<Integer> X, OpFeedbackParam<Integer> Y) {
        int agentId = this.getCurrentOpAgentId().getLocalId();
        var loc = model.getAgPos(agentId);
        X.set(loc.x);
        Y.set(loc.y);
    }
}