package cn.org.starpivot.auth.captcha;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 彩色字符 + 细波浪干扰线，风格对齐常见管理后台验证码。
 */
public final class CaptchaImageRenderer {

    private static final int WIDTH = 160;
    private static final int HEIGHT = 52;

    private static final Color[] CHAR_COLORS = {
            new Color(59, 130, 246),
            new Color(34, 197, 94),
            new Color(249, 115, 22),
            new Color(239, 68, 68),
            new Color(20, 184, 166)
    };

    private static final Color[] LINE_COLORS = {
            new Color(147, 197, 253, 180),
            new Color(134, 239, 172, 180),
            new Color(253, 224, 71, 160),
            new Color(253, 186, 116, 160)
    };

    private CaptchaImageRenderer() {
    }

    public static BufferedImage render(String code) {
        String display = code.toUpperCase();
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            g.setColor(Color.WHITE);
            g.fillRect(0, 0, WIDTH, HEIGHT);

            ThreadLocalRandom random = ThreadLocalRandom.current();
            drawWavyLines(g, random);

            Font font = new Font("Arial", Font.BOLD, 30);
            g.setFont(font);
            FontMetrics metrics = g.getFontMetrics(font);

            int totalWidth = 0;
            for (int i = 0; i < code.length(); i++) {
                totalWidth += metrics.charWidth(display.charAt(i));
            }
            int spacing = Math.max(6, (WIDTH - totalWidth - 16) / Math.max(display.length() - 1, 1));
            int x = (WIDTH - totalWidth - spacing * (display.length() - 1)) / 2;

            for (int i = 0; i < display.length(); i++) {
                char ch = display.charAt(i);
                g.setColor(CHAR_COLORS[i % CHAR_COLORS.length]);

                int charWidth = metrics.charWidth(ch);
                int baseY = (HEIGHT + metrics.getAscent() - metrics.getDescent()) / 2;
                int y = baseY + random.nextInt(5) - 2;
                double angle = Math.toRadians(random.nextInt(21) - 10);

                g.rotate(angle, x + charWidth / 2.0, y);
                g.drawString(String.valueOf(ch), x, y);
                g.rotate(-angle, x + charWidth / 2.0, y);

                x += charWidth + spacing;
            }
        } finally {
            g.dispose();
        }
        return image;
    }

    private static void drawWavyLines(Graphics2D g, ThreadLocalRandom random) {
        for (int i = 0; i < 4; i++) {
            g.setColor(LINE_COLORS[i % LINE_COLORS.length]);
            g.setStroke(new BasicStroke(1.2f));

            Path2D path = new Path2D.Double();
            double startY = 12 + random.nextDouble() * (HEIGHT - 24);
            path.moveTo(0, startY);

            double amplitude = 3 + random.nextDouble() * 4;
            double frequency = 0.08 + random.nextDouble() * 0.04;
            for (int x = 0; x <= WIDTH; x += 4) {
                path.lineTo(x, startY + Math.sin(x * frequency) * amplitude);
            }
            g.draw(path);
        }
    }
}
