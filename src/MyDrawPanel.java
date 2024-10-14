import javax.swing.*;
import java.awt.*;

public class MyDrawPanel extends JPanel {
    public void paintComponent(Graphics g) {

//        Image image = new ImageIcon("pai.jpg").getImage();
//        g.drawImage(image, 3,4,this);

//        g.setColor(Color.orange);
//        g.fillRect(20,50,100,100);
//        g.fillRect(0,0,this.getWidth(), this.getHeight());

//        int red = (int) (Math.random() * 255);
//        int green = (int) (Math.random() * 255);
//        int blue = (int) (Math.random() * 255);
//        Color randomColor = new Color(red, green, blue);
//        g.setColor(randomColor);
//        g.fillOval(70,70,100,100);

//        Graphics2D g2d = (Graphics2D) g;
//        GradientPaint gradient = new GradientPaint(70,70,Color.blue, 150,150, Color.orange);
//
//        g2d.setPaint(gradient);
//        g2d.fillOval(70,70,100,100);

        Graphics2D g2d = (Graphics2D) g;

        int red = (int) (Math.random() * 255);
        int green = (int) (Math.random() * 255);
        int blue = (int) (Math.random() * 255);
        Color startColor = new Color(red, green, blue);

         red = (int) (Math.random() * 255);
         green = (int) (Math.random() * 255);
         blue = (int) (Math.random() * 255);
        Color endColor = new Color(red,green, blue);

        GradientPaint gradient = new GradientPaint(70,70,startColor, 150,150,endColor);
        g2d.setPaint(gradient);
        g2d.fillOval(70,70,100,100);

    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("My Drawing Panel");
        MyDrawPanel drawPanel = new MyDrawPanel();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(drawPanel);
        frame.setSize(400, 400);
        frame.setVisible(true);;
    }
}

