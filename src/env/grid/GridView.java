package grid;

import jason.environment.grid.GridWorldView;
import model.AgentInfo;
import service.AgentDB;
import jason.environment.grid.GridWorldModel;

import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;

public class GridView extends GridWorldView {
    private static final Logger logger = Logger.getLogger(GridModel.class.getName());

    private Image sheepImage, houndImage;
    private boolean drawCoords;

    public GridView(GridWorldModel model, boolean drawCoords) {
        super(model, "Grid World", 800);
        this.drawCoords = drawCoords;
        setVisible(true);

        try {
            sheepImage = ImageIO.read(new File("src/resources/sheep.png"));
            houndImage = ImageIO.read(new File("src/resources/hound.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        repaint();
        drawInitialCoordinates();
    }

    @Override
    public void draw(Graphics g, int x, int y, int object) {
        switch (object) {
            case GridModel.CORRAL:
                drawFill(g, x, y, Color.GREEN);
                drawCoordinate(g, x, y);
                break;
            case GridModel.SHEEP:
                drawAgent(g, x, y, Color.GRAY, object);
                break;
            case GridModel.HOUND:
                drawAgent(g, x, y, Color.RED, object);
                break;
            default:
                throw new IllegalArgumentException("Invalid draw type");
        }
    }

    @Override
    public void drawEmpty(Graphics g, int x, int y) {
        super.drawEmpty(g, x, y);
        drawCoordinate(g, x, y);
    }

    @Override
    public void drawObstacle(Graphics g, int x, int y) {
        super.drawObstacle(g, x, y);
        drawCoordinate(g, x, y);
    }

    @Override
    public void drawAgent(Graphics g, int x, int y, Color c, int id) {
        // g.setColor(c);
        // g.fillOval(x * this.cellSizeW + 2, y * this.cellSizeH + 2, this.cellSizeW - 4, this.cellSizeH - 4);

        AgentInfo agent = AgentDB.getInstance().getAgentByLocation(x, y);
        // No idea why this is needed, but a sheep is (sometimes) null, when it lands on a corral that a another sheep previously occupied
        // In any case, we just forego drawing the agent in this case, it's just for one frame anyhow
        if (agent != null) {
            switch (agent.getAgentType()) {
                case GridModel.SHEEP:
                    g.drawImage(sheepImage, x * this.cellSizeW, y * this.cellSizeH, this.cellSizeW,
                            (int) (this.cellSizeH * 0.8),
                            null);
                    break;
                case GridModel.HOUND:
                    g.drawImage(houndImage, x * this.cellSizeW, y * this.cellSizeH, this.cellSizeW,
                            (int) (this.cellSizeH * 0.8),
                            null);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid agent type");
            }
            id = agent.getShortName();
            // if (id >= 0) {
            g.setColor(Color.black);
            this.drawString(g, x, y, this.defaultFont, String.valueOf(id));
            // }
        }
    }

    @Override
    public void drawString(Graphics g, int x, int y, Font f, String s) {
        g.setFont(f);
        FontMetrics metrics = g.getFontMetrics();
        int width = metrics.stringWidth(s);
        int height = metrics.getHeight();
        g.drawString(s, x * this.cellSizeW + (this.cellSizeW / 2 - width / 2),
                // (int) ((y * this.cellSizeH + this.cellSizeH / 2 + height / 2) * 0.8));
                y * this.cellSizeH + this.cellSizeH / 2 + height / 2);
    }

    public void drawFill(Graphics g, int x, int y, Color color) {
        g.setColor(color);
        g.fillRect(x * cellSizeW, y * cellSizeH, cellSizeW, cellSizeH);
    }

    public void drawCircle(Graphics g, int x, int y, Color color) {
        g.setColor(color);
        g.fillOval(x * this.cellSizeW + 2, y * this.cellSizeH + 2, this.cellSizeW - 4, this.cellSizeH - 4);
    }

    private void drawInitialCoordinates() {
        for (int x = 0; x < model.getWidth(); x++) {
            for (int y = 0; y < model.getHeight(); y++) {
                update(x, y);
            }
        }
    }

    private void drawCoordinate(Graphics g, int x, int y) {
        if (!drawCoords)
            return;

        String str = x + "," + y;
        g.setColor(Color.BLACK);
        Font f = new Font("Arial", Font.BOLD, 10);
        drawString(g, x, y, f, str);
    }
}