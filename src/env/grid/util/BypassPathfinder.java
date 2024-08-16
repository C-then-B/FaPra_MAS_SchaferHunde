package grid.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.javatuples.Pair;

import grid.GridModel;
import jason.environment.grid.Location;

public class BypassPathfinder extends Pathfinder {
    private static Map<Pair<Location, Location>, List<Location>> bypassCache = new HashMap<>();

    protected BypassPathfinder() {
        super(80000);
    }

    public static BypassPathfinder getInstance() {
        return Pathfinder.getInstance(BypassPathfinder.class);
    }

    @Override
    protected void excludeObstacles() {
        GridModel model = GridModel.getInstance();
        gridProcessor.processEntireGrid(loc -> model.getObjectsAt(loc).contains(GridModel.OBSTACLE),
                loc -> ds.updateCell(loc.x, loc.y, -1),
                c -> false);
    }

    private void cachePath(Location start, Location target) {
        List<Location> path = super.getPath(start, target);
        Pair<Location, Location> key = new Pair<>(start, target);
        bypassCache.put(key, path);

        // Add the reverse path as well
        bypassCache.put(new Pair<>(target, start), path.reversed());
    }

    @Override
    protected boolean targetIsWalkable(Location target) {
        return !GridModel.getInstance().getObjectsAt(target.x, target.y).contains(GridModel.OBSTACLE);
    }

    @Override
    public Location getNextPosition(Location start, Location target)
            throws UnwalkableTargetCellException, NoPathFoundException {
        try {
            Pair<Location, Location> key = new Pair<>(start, target);
            preCheck(start, target, key);

            List<Location> path = bypassCache.get(key);
            return path.size() > 1 ? path.get(1) : path.get(0);
        } finally {
            releaseInstance();
        }
    }

    public List<Location> getPath(Location start, Location target)
            throws UnwalkableTargetCellException, NoPathFoundException {
        try {
            Pair<Location, Location> key = new Pair<>(start, target);
            preCheck(start, target, key);

            return bypassCache.get(key);
        } finally {
            releaseInstance();
        }
    }

    private void preCheck(Location start, Location target, Pair<Location, Location> key)
            throws UnwalkableTargetCellException, NoPathFoundException {
        if (!targetIsWalkable(target)) {
            throw new UnwalkableTargetCellException("Target location is an obstacle");
        }

        if (!bypassCache.containsKey(key)) {
            // throw new NoPathFoundException("No path found");
            cachePath(start, target);
        }
    }

    public int getDistance(Location start, Location target)
            throws UnwalkableTargetCellException, NoPathFoundException {
        return getPath(start, target).size();
    }
}