package component;

import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

public class Panel extends JPanel {

    private int roundTopLeft = 0;
    private int roundTopRight = 0;
    private int roundBottomLeft = 0;
    private int roundBottomRight = 0;

    // Constructor default
    public Panel() {
        super();
        setOpaque(false); // Membuat panel tidak buram agar dapat melihat sudut membulat
    }

    // Getter dan Setter untuk setiap sudut
    public int getRoundTopLeft() {
        return roundTopLeft;
    }

    public void setRoundTopLeft(int roundTopLeft) {
        this.roundTopLeft = roundTopLeft;
        repaint(); // Meminta komponen untuk menggambar ulang saat nilai diubah
    }

    public int getRoundTopRight() {
        return roundTopRight;
    }

    public void setRoundTopRight(int roundTopRight) {
        this.roundTopRight = roundTopRight;
        repaint();
    }

    public int getRoundBottomLeft() {
        return roundBottomLeft;
    }

    public void setRoundBottomLeft(int roundBottomLeft) {
        this.roundBottomLeft = roundBottomLeft;
        repaint();
    }

    public int getRoundBottomRight() {
        return roundBottomRight;
    }

    public void setRoundBottomRight(int roundBottomRight) {
        this.roundBottomRight = roundBottomRight;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Casting ke Graphics2D untuk fitur yang lebih lanjut
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());

        int width = getWidth();
        int height = getHeight();

        // Area untuk seluruh panel
        Area area = new Area(new Rectangle2D.Double(0, 0, width, height));

        // Membulatkan setiap sudut jika radiusnya lebih besar dari 0
        if (roundTopLeft > 0)
        {
            area.subtract(new Area(new Rectangle2D.Double(0, 0, roundTopLeft, roundTopLeft)));
            area.add(new Area(new RoundRectangle2D.Double(0, 0, roundTopLeft * 2, roundTopLeft * 2, roundTopLeft, roundTopLeft)));
        }

        if (roundTopRight > 0)
        {
            area.subtract(new Area(new Rectangle2D.Double(width - roundTopRight, 0, roundTopRight, roundTopRight)));
            area.add(new Area(new RoundRectangle2D.Double(width - roundTopRight * 2, 0, roundTopRight * 2, roundTopRight * 2, roundTopRight, roundTopRight)));
        }

        if (roundBottomLeft > 0)
        {
            area.subtract(new Area(new Rectangle2D.Double(0, height - roundBottomLeft, roundBottomLeft, roundBottomLeft)));
            area.add(new Area(new RoundRectangle2D.Double(0, height - roundBottomLeft * 2, roundBottomLeft * 2, roundBottomLeft * 2, roundBottomLeft, roundBottomLeft)));
        }

        if (roundBottomRight > 0)
        {
            area.subtract(new Area(new Rectangle2D.Double(width - roundBottomRight, height - roundBottomRight, roundBottomRight, roundBottomRight)));
            area.add(new Area(new RoundRectangle2D.Double(width - roundBottomRight * 2, height - roundBottomRight * 2, roundBottomRight * 2, roundBottomRight * 2, roundBottomRight, roundBottomRight)));
        }

        // Menggambar area dengan warna latar belakang
        g2.fill(area);
        g2.dispose();
    }
}
