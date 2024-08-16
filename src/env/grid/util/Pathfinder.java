package grid.util;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jason.environment.grid.Location;

import grid.GridModel;

public abstract class Pathfinder {
    public class UnwalkableTargetCellException extends RuntimeException {
        public UnwalkableTargetCellException(String message) {
            super(message);
        }
    }

    public class NoPathFoundException extends RuntimeException {
        public NoPathFoundException(String message) {
            super(message);
        }
    }

    protected Set<Location> customExcludedObjects = new HashSet<>();
    private static final Logger logger = Logger.getLogger(Pathfinder.class.getName());

    protected CustomDStarLite ds;
    protected static GridProcessor gridProcessor;
    private static final ConcurrentHashMap<Pathfinder, AtomicBoolean> instances = new ConcurrentHashMap<>();

    protected Pathfinder(int maxSteps) {
        logger.setLevel(Level.SEVERE);
        ds = new CustomDStarLite(maxSteps);
        GridModel model = GridModel.getInstance();
        gridProcessor = new GridProcessor(model.getWidth(), model.getHeight());
    }

    protected abstract void excludeObstacles();

    protected abstract boolean targetIsWalkable(Location target);

    protected static <T extends Pathfinder> T getInstance(Class<T> clazz) {
        for (var entry : instances.entrySet()) {
            Pathfinder pf = entry.getKey();
            if (pf.getClass() == clazz && entry.getValue().compareAndSet(false, true)) {
                @SuppressWarnings("unchecked")
                T instance = (T) pf;
                return instance;
            }
        }

        synchronized (Pathfinder.class) {
            try {
                T pf = clazz.getDeclaredConstructor().newInstance();
                instances.put(pf, new AtomicBoolean(true));
                return pf;
            } catch (Exception e) {
                throw new RuntimeException("Unable to create instance of " + clazz.getName(), e);
            }
        }
    }

    protected void releaseInstance() {
        customExcludedObjects.clear();
        AtomicBoolean inUse = instances.get(this);
        if (inUse != null) {
            inUse.set(false);
        }
    }

    private void makeVirtualWall() {
        GridModel model = GridModel.getInstance();
        for (int x = -1; x <= model.getWidth(); x++) {
            for (int y = -1; y <= model.getHeight(); y++) {
                if (x < 0 || x >= model.getWidth() || y < 0 || y >= model.getHeight()) {
                    ds.updateCell(x, y, -1);
                }
            }
        }
    }

    public Location getNextPosition(Location start, Location target)
            throws UnwalkableTargetCellException, NoPathFoundException {
        try {
            List<Location> path = getPath(start, target);
            return path.size() > 1 ? path.get(1) : path.get(0);
        } catch (UnwalkableTargetCellException | NoPathFoundException e) {
            logger.info(e.getMessage() + " -> Returning start location instead");
            return start;
        } finally {
            releaseInstance();
        }
    }

    protected List<Location> getPath(Location start, Location target)
            throws UnwalkableTargetCellException, NoPathFoundException {
        if (start.equals(target)) {
            return List.of(start);
        }

        if (!targetIsWalkable(target)) {
            throw new UnwalkableTargetCellException("Target location is an obstacle");
        }

        GridModel model = GridModel.getInstance();
        ds.init(start.x, start.y, target.x, target.y);

        makeVirtualWall();
        excludeObstacles();

        if (!ds.replan() || ds.getPath().stream().anyMatch(s -> s.x < 0 || s.y < 0
                || s.x >= model.getWidth() || s.y >= model.getHeight())) {
            throw new NoPathFoundException("No path found");
        }
        return ds.getPath().stream().map(s -> new Location(s.x, s.y)).collect(Collectors.toList());
    }
}