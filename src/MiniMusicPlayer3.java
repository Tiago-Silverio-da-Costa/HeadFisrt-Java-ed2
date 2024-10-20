import javax.sound.midi.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import javax.swing.*;
import java.awt.*;

public class MiniMusicPlayer3 {

    static JFrame f = new JFrame("My First Music Video");
    static MyDrawPanel ml;

    public static void main(String[] args) {
        MiniMusicPlayer3 mini = new MiniMusicPlayer3();
        mini.go();
    }
    public void setUpGui() {
        ml = new MyDrawPanel();
        f.setContentPane(ml);
        f.setBounds(30,30,300,300);
        f.setVisible(true);
    }

    public void go() {
        setUpGui();

        try {
            Sequencer sequencer = MidiSystem.getSequencer();
            sequencer.open();
            sequencer.addControllerEventListener(ml, new int[] {127});
            Sequence seq = new Sequence(Sequence.PPQ, 4);
            Track track = seq.createTrack();

            int r = 0;
            for (int i = 0; i < 60; i+= 4) {
                r = (int) ((Math.random() * 50));
                track.add(makeEvent(144,1,r,100,i));
                track.add(makeEvent(176,1,127,0,i));
                track.add(makeEvent(128,1,r,100,i + 2));
            }
            sequencer.setSequence(seq);
            sequencer.start();
            sequencer.setTempoInBPM(120);

        }catch (Exception ex) {ex.printStackTrace();}
    }

    public MidiEvent makeEvent(int comb, int chan, int one, int two, int tick) {
        MidiEvent event = null;
        try {
            ShortMessage a = new ShortMessage();
            a.setMessage(comb,chan, one, two);
            event = new MidiEvent(a, tick);
        } catch (Exception e) {}
        return event;
    }

    class MyDrawPanel extends JPanel implements ControllerEventListener {
        boolean msg = false;

        @Override
        public void controlChange(ShortMessage event) {
            msg = true;
            repaint();
        }

        public void paintComponent(Graphics g) {

            int r = (int) ((Math.random() * 250));
            int gr = (int) ((Math.random() * 250));
            int b = (int) ((Math.random() * 250));

            g.setColor(new Color(r,gr, b));

            int height = (int) ((Math.random() * 120) * 10);
            int width = (int) ((Math.random() * 120) * 10);

            int x = (int) ((Math.random() * 40) * 10);
            int y = (int) ((Math.random() * 40) * 10);

            g.fillRect(x,y,height,width);
            msg = false;
        }
    }

//    public class MySendListener implements ActionListener {
//
//        @Override
//        public void actionPerformed(ActionEvent e) {
//            boolean [] checkboxState = new boolean[256];
//
//            for (int i = 0; i < 256; i++) {
//                JCheckBox check = (JCheckBox) checkboxList.get(i);
//                if (check.isSelected()) {
//                    checkboxState[i] = true;
//                }
//            }
//
//            try {
//                FileOutputStream fileStream = new FileOutputStream(new File("Checkbox.ser"));
//                ObjectOutputStream os = new ObjectOutputStream(fileStream);
//                os.writeObject(checkboxState);
//            }catch (Exception ex) {
//                ex.printStackTrace();
//            }
//        }
//    }
//
//    public class MyReadListner implements ActionListener {
//
//        @Override
//        public void actionPerformed(ActionEvent e) {
//            boolean [] checkboxState = null;
//
//            try {
//                FileInputStream fileIn = new FileInputStream(new File("Checkbox.ser"));
//                ObjectInputStream is = new ObjectInputStream(fileIn);
//                checkboxState = (boolean[])  is.readObject();
//
//            }catch (Exception ex) {ex.printStackTrace();}
//
//            for (int i = 0; i < 256; i++) {
//                JCheckBox check = (JCheckBox) checkboxList.get(i);
//                if(checkboxState[i]) {
//                    check.setSelected(true);
//                } else {
//                    check.setSelected(false);
//                }
//            }
//
//            sequencer.stop();
//            buildTrackAndStart();
//        }
//    }
}
