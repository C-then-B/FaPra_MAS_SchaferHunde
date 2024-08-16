package env.grid.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import grid.GridModel;
import grid.util.BypassPathfinder;
import grid.util.AgentPathfinder;
import grid.util.Pathfinder;
import grid.util.Pathfinder.NoPathFoundException;
import jason.environment.grid.Location;
import model.AgentInfo;

public class PathfinderTest {
    @Test
    public void testPathfinderBounds1() {
        GridModel.create("src/test/resources/templates/3_test.txt");
        Pathfinder pathfinder = AgentPathfinder.getInstance();
        Location startPos = new Location(0, 0);
        Location targetPos = new Location(2, 2);
        Location nextPos = pathfinder.getNextPosition(startPos, targetPos);
        assertEquals(nextPos, startPos);
    }

    @Test
    public void testPathfinderBounds2() {
        GridModel.create("src/test/resources/templates/4_test.txt");
        Pathfinder pathfinder = AgentPathfinder.getInstance();
        Location startPos = new Location(4, 4);
        Location targetPos = new Location(2, 2);
        Location nextPos = pathfinder.getNextPosition(startPos, targetPos);
        assertEquals(nextPos, startPos);
    }

    @Test
    public void testPathfinderBlocked() {
        GridModel model = GridModel.create("src/test/resources/templates/5_test.txt");
        AgentInfo sheep = new AgentInfo(GridModel.SHEEP, 5, "sheep");
        AgentInfo hound = new AgentInfo(GridModel.HOUND, 10, "hound");
        model.initAgent(sheep);
        model.initAgent(hound);
        Location startPos = new Location(0, 0);
        model.setAgPos(sheep, startPos);
        Location targetPos = new Location(4, 0);
        model.setAgPos(hound, targetPos);

        Pathfinder pathfinder = AgentPathfinder.getInstance();
        Location nextPos = pathfinder.getNextPosition(startPos, targetPos);
        assertEquals(startPos, nextPos);
    }

    @Test
    public void testPathfinderNotBlocked() {
        GridModel model = GridModel.create("src/test/resources/templates/5_test.txt");
        AgentInfo sheep = new AgentInfo(GridModel.SHEEP, 5, "sheep");
        AgentInfo hound = new AgentInfo(GridModel.HOUND, 10, "hound");
        model.initAgent(sheep);
        model.initAgent(hound);
        Location startPos = new Location(0, 0);
        model.setAgPos(sheep, startPos);
        Location targetPos = new Location(4, 0);
        model.setAgPos(hound, targetPos);
        targetPos = new Location(4, 1);

        Pathfinder pathfinder = AgentPathfinder.getInstance();
        Location nextPos = pathfinder.getNextPosition(startPos, targetPos);
        assertNotEquals(startPos, nextPos);
    }

    @Test
    public void testPathfinderFar() {
        GridModel model = GridModel.create("src/test/resources/templates/6_test.txt");
        AgentInfo sheep = new AgentInfo(GridModel.SHEEP, 5, "sheep");
        model.initAgent(sheep);
        Location startPos = new Location(1, 20);
        model.setAgPos(sheep, startPos);
        Location targetPos = new Location(10, 10);

        assertTrue(model.getObjectsAt(startPos).contains(GridModel.SHEEP));
        assertTrue(model.getObjectsAt(targetPos).contains(GridModel.CORRAL));

        Pathfinder pathfinder = AgentPathfinder.getInstance();
        Location nextPos = pathfinder.getNextPosition(startPos, targetPos);
        assertNotEquals(startPos, nextPos);
    }

    @Test
    public void testPathfinderBypass() {
        GridModel model = GridModel.create("src/test/resources/templates/5_test.txt");
        AgentInfo sheep = new AgentInfo(GridModel.SHEEP, 5, "sheep");
        AgentInfo hound = new AgentInfo(GridModel.HOUND, 10, "hound");
        model.initAgent(sheep);
        model.initAgent(hound);
        Location startPos = new Location(0, 0);
        model.setAgPos(sheep, startPos);
        Location targetPos = new Location(4, 0);
        model.setAgPos(hound, targetPos);

        BypassPathfinder pathfinder = BypassPathfinder.getInstance();
        Location nextPos = pathfinder.getNextPosition(startPos, targetPos);
        assertNotEquals(startPos, nextPos);
    }

    @Test
    public void testPathfinderBypass2() {
        GridModel model = GridModel.create("src/test/resources/templates/7_test.txt");

        AgentInfo sheep = new AgentInfo(GridModel.SHEEP, 5, "sheep");
        model.initAgent(sheep);
        model.setAgPos(sheep, new Location(6, 13));

        AgentInfo sheep2 = new AgentInfo(GridModel.SHEEP, 6, "sheep");
        model.initAgent(sheep2);
        model.setAgPos(sheep2, new Location(7, 12));

        AgentInfo sheep3 = new AgentInfo(GridModel.SHEEP, 7, "sheep");
        model.initAgent(sheep3);
        model.setAgPos(sheep3, new Location(7, 13));

        AgentInfo sheep4 = new AgentInfo(GridModel.SHEEP, 8, "sheep");
        model.initAgent(sheep4);
        model.setAgPos(sheep4, new Location(7, 14));

        AgentInfo sheep5 = new AgentInfo(GridModel.SHEEP, 9, "sheep");
        model.initAgent(sheep5);
        model.setAgPos(sheep5, new Location(8, 13));

        Location startPos = new Location(7, 13);
        Location targetPos = new Location(7, 8);

        BypassPathfinder bypassPathfinder = BypassPathfinder.getInstance();
        Pathfinder regularPathfinder = AgentPathfinder.getInstance();
        Location nextPosBypass = bypassPathfinder.getNextPosition(startPos, targetPos);
        Location nextPosRegular = regularPathfinder.getNextPosition(startPos, targetPos);
        assertEquals(new Location(7, 12), nextPosBypass);
        assertNotEquals(nextPosBypass, nextPosRegular);
    }

    @Test
    public void testPathfinderBypass3() {
        GridModel model = GridModel.create("src/test/resources/templates/9_test.txt");
        AgentInfo sheep = new AgentInfo(GridModel.SHEEP, 5, "sheep");
        model.initAgent(sheep);
        Location startPos = new Location(0, 0);
        model.setAgPos(sheep, startPos);
        Location targetPos = new Location(2, 0);

        BypassPathfinder pathfinder = BypassPathfinder.getInstance();
        Location nextPos = pathfinder.getNextPosition(startPos, targetPos);
        assertEquals(new Location(0, 1), nextPos);
    }

    @Test
    public void testPathfinderBypass4() {
        GridModel model = GridModel.create("src/test/resources/templates/9_test.txt");
        AgentInfo sheep = new AgentInfo(GridModel.SHEEP, 5, "sheep");
        model.initAgent(sheep);
        Location startPos = new Location(4, 0);
        model.setAgPos(sheep, startPos);
        Location targetPos = new Location(2, 0);

        BypassPathfinder pathfinder = BypassPathfinder.getInstance();
        Location nextPos = pathfinder.getNextPosition(startPos, targetPos);
        assertEquals(new Location(3, 0), nextPos);
    }

    @Test
    public void testPathfinderBypass5() {
        GridModel model = GridModel.create("src/test/resources/templates/9_test.txt");
        AgentInfo sheep = new AgentInfo(GridModel.SHEEP, 5, "sheep");
        model.initAgent(sheep);
        Location startPos = new Location(4, 2);
        model.setAgPos(sheep, startPos);
        Location targetPos = new Location(2, 0);

        BypassPathfinder pathfinder = BypassPathfinder.getInstance();
        assertThrows(NoPathFoundException.class, () -> pathfinder.getNextPosition(startPos, targetPos));
    }
}
