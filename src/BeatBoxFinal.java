import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import javax.sound.midi.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
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
        theFrame = new JFrame("Cyber BeatBox");
        BorderLayout layout = new BorderLayout();
        JPanel background = new JPanel(layout);
        background.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        checkboxList = new ArrayList<JCheckBox>();

        Box buttonBox = new Box(BoxLayout.Y_AXIS);
        JButton start = new JButton("Start");
        start.addActionListener(new MyStartListener());
        buttonBox.add(start);

        JButton stop = new JButton("Stop");
        stop.addActionListener(new MyStopListener());
        buttonBox.add(stop);

        JButton upTempo = new JButton("Tempo Up");
        upTempo.addActionListener(new MyUpTempoListener());
        buttonBox.add(upTempo);

        JButton downTempo = new JButton("Tempo Down");
        downTempo.addActionListener(new MyUpTempoListener());
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
                    trackList.add(key);
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

            // Loop through the checkboxes to set their state
            for (int i = 0; i < 256; i++) {
                JCheckBox check = (JCheckBox) checkboxList.get(i); // Assuming checkboxList is a List<JCheckBox>
                if (check.isSelected()) {
                    checkboxState[i] = true;
                }
            }

            String messageToSend = null;
            try {
                out.writeObject(userName + nextNum++ + ": " + userMessage.getText());
                out.writeObject(checkboxState);
            } catch (Exception ex) {
                System.out.println("Sorry dude. Could not send it to the server.");
                ex.printStackTrace(); // It's helpful to print the stack trace for debugging
            }

            userMessage.setText("");
        }
    }


    public class MyListSelectionListener implements ListSelectionListener {

            @Override
            public void valueChanged(ListSelectionEvent le) {
                if (!le.getValueIsAdjusting()) {
                    String selected = (String) incomingList.getSelectedValue();
                    if (selected != null) {
                        boolean[] selectedState = (boolean[]) otherSeqsMap.get(selected);
                        changeSequence(selectedState);
                        sequencer.stop();
                        buildTrackAndStart();
                    }
                }
            }

        }
            public class RemoteReader implements Runnable {
                boolean[] checkBoxState = null;
                Object obj = null;

                public void run() {
                    try {
                        while ((obj = in.readObject()) != null) {
                            System.out.println("Got an object from server");
                            System.out.println(obj.getClass());
                            String nameToShow = (String) obj;
                            checkBoxState = (boolean[]) in.readObject();
                            otherSeqsMap.put(nameToShow, checkBoxState);
                            listVector.add(nameToShow);
                            incomingList.setListData(listVector);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }

        public class MyPlayMineListener implements ActionListener {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (mysequence != null) {
                    sequence = mysequence;
                }
            }
        }

        public void changeSequence(boolean[] checkboxState) {
        for (int i = 0; i < 256; i++) {
            JCheckBox check = (JCheckBox) checkboxList.get(i);
            if (checkboxState[i]) {
                check.setSelected(true);
            } else {
                check.setSelected(false);
            }
        }
        }

        public void makeTracks(ArrayList list) {
        Iterator it = list.iterator();
        for (int i = 0; i < 16; i++) {
            Integer num = (Integer) it.next();
            if (num != null) {
                int numKey = num.intValue();
                track.add(makeEvent(144,9,numKey, 100, i));
                track.add(makeEvent(128,9,numKey, 100,i+1));
            }
        }
        }

        public MidiEvent makeEvent(int comb, int chan, int one, int two, int tick) {
        MidiEvent event = null;
        try {
            ShortMessage a = new ShortMessage();
            a.setMessage(comb, chan, one, two);
            event = new MidiEvent(a, tick);
        }catch (Exception e) { }
        return event;
        }
}
