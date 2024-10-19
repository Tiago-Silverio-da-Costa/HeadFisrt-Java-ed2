import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import javax.sound.midi.*;
import java.util.*;
import java.net.*;

public class BeatBoxFinal {

    JFrame theFrame;
    JPanel mainPanel;
    JList incomingList;
    JTextField userMessage;
    ArrayList<JCheckBox> checkboxList;
    int nextNum;
    Vector<String> listVector = new Vector<String>();
    String userName;
    ObjectOutputStream out;
    ObjectInputStream in;
    HashMap<String, boolean[]> otherSeqsMap = new HashMap<String, boolean[]>();

    Sequencer sequencer;
    Sequence sequence;
    Sequence mysequence = null;
    Track track;

    String[] instrumentNames = {"Bass Drum", "Closed Hi-Hat", "Open Hi-Hat", "Acoustic Snare", "Crash Cymbal", "Hand Clap", "High Tom", "Hi Bongo", "Maracas", "Whistle", "Low Conga", "Cowbell", "Vibraslap", "Low-mid Tom", "High agogo", "Open Hi Conga"};

    int[] instruments = {35,42,36,38,49,39,50,60,70,72,64,56,58,47,67,63};

    public static void main(String[] args) {
        new BeatBoxFinal().startUp(args[0]);
    }

    public void startUp(String name) {
        userName = name;
        try {
            Socket sock = new Socket("127.0.0.1", 4242);
            out = new ObjectOutputStream(sock.getOutputStream());
            in = new ObjectInputStream(sock.getInputStream());
            Thread remote =new Thread(new RemoteReader());
            remote.start();
        } catch (Exception ex) {
            System.out.println("couldn't connect - you'll have to play alone");
        }
        setUpMidi();
        buildGUI();
    }

    public void buildGUI(){
        theframe = new JFrame("Cyber BeatBox");
        BorderLayout layout = new BorderLayout();
        JPanel background = new JPanel(layout);
        background.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        checkboxList = new ArrayList<JCheckBox>();

        Box buttonBox = new Box(BoxLayout.Y_AXIS);
        JButton start = new JButton("Start");
        start.addActionListener(new BeatBox.MyStartListener());
        buttonBox.add(start);

        JButton stop = new JButton("Stop");
        stop.addActionListener(new BeatBox.MyStopListener());
        buttonBox.add(stop);

        JButton upTempo = new JButton("Tempo Up");
        upTempo.addActionListener(new BeatBox.MyUpTempoListener());
        buttonBox.add(upTempo);

        JButton downTempo = new JButton("Tempo Down");
        downTempo.addActionListener(new BeatBox().MyUpTempoListener());
        buttonBox.add(downTempo);

        JButton sendit = new JButton("send it");
        sendit.addActionListener(new MySendListener());
        buttonBox.add(sendit);

        userMessage = new JTextField();
        buttonBox.add(userMessage);

        incomingList = new JList();
        incomingList.addListSelectionListener(new MyListSelectionListener());
        incomingList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane theList = new JScrollPane(incomingList);
        buttonBox.add(theList);
        incomingList.setListData(listVector);

        Box nameBox = new Box(BoxLayout.Y_AXIS);
        for (int i = 0; i < 16; i++) {
            nameBox.add(new Label(instrumentNames[i]));
        }

        background.add(BorderLayout.EAST, buttonBox);
        background.add(BorderLayout.WEST, nameBox);

        theFrame.getContentPane().add(background);
        GridLayout grid = new GridLayout(16,16);
        grid.setVgap(1);
        grid.setHgap(2);
        mainPanel = new JPanel(grid);
        background.add(BorderLayout.CENTER, mainPanel);

        for (int i = 0; i < 256; i++) {
            JCheckBox c = new JCheckBox();
            c.setSelected(false);
            checkboxList.add(c);
            mainPanel.add(c);
        }

        theFrame.setBounds(50,50,300,300);
        theFrame.pack();
        theFrame.setVisible(true);

    }
        public void setUpMidi() {
            try {
                sequencer = MidiSystem.getSequencer();
                sequencer.open();
                sequence = new Sequence(Sequence.PPQ, 4);
                track = sequence.createTrack();
                sequencer.setTempoInBPM(120);
            }catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        public void buildTrackAndStart() {
        ArrayList<Integer> trackList = null;
        sequence.deleteTrack(track);
        track =sequence.createTrack();

        for (int i = 0; i < 16; i++) {
            trackList = new ArrayList<Integer>();

            for (int j = 0; j < 16; j++) {
                JCheckBox jc = (JCheckBox) checkboxList.get(j + (16*i));
                if (jc.isSelected()) {
                    int key = instruments[i];
                    trackList.add(new Integer(key));
                } else {
                    trackList.add(null);
                }
                track.add(makeEvent(192,9,1,0,15));
                try {
                    sequencer.setSequence(sequence);
                    sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY);
                    sequencer.start();
                    sequencer.setTempoInBPM(120);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        }
        public class MyStartListener implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {
                buildTrackAndStart();
            }
        }

        public class MyStopListener implements ActionListener {

            @Override
            public void actionPerformed(ActionEvent e) {
                sequencer.stop();
            }
        }

        public  class MyUpTempoListener implements ActionListener {

            @Override
            public void actionPerformed(ActionEvent e) {
                float tempoFactor = sequencer.getTempoFactor();
                sequencer.setTempoFactor((float) (tempoFactor * 1.03));
            }
        }

        public class MyDownTempoListener implements ActionListener {

            @Override
            public void actionPerformed(ActionEvent e) {
                float tempoFactor = sequencer.getTempoFactor();
                sequencer.setTempoFactor((float) (tempoFactor * .97));
            }
        }

        public class MySendListener implements ActionListener {

            @Override
            public void actionPerformed(ActionEvent e) {
                boolean[] checkboxState = new boolean[256];
                for (int i = 0; i < 256; i++) {
                    JCheckBox check = (JCheckBox) checkboxState.get(i);
                    if (check.isSelected()) {
                        checkboxState[i] = true;
                    }
                }
                String messageToSend = null;
                try {
                    out.writeObject(userName + nextNum++ + ": " + userMessage.getText());
                    out.writeObject(checkboxState);
                }catch (Exception ex) {
                    System.out.println("Sorry dude. Could not send it to the server.");
                }
                userMessage.setText("");
            }
        }
}
