package com.radoslawzerek;

/**
 * Author: Radosław Żerek
 */

import javax.sound.midi.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SampleMaker implements MetaEventListener {
    JPanel mainPanel;
    List<JCheckBox> checkFieldList;
    Sequencer sequencer;
    Sequence sequence;
    Track track;
    JFrame mainFrame;

    String[] instrumentsName = {"Bass Drum", "Closed Hi-Hat", "Open Hi-Hat", "Acoustic Snare",
    "Crash Cymbal", "Hand Clap", "High Tom", "Hi Bongo", "Maracas", "Whistle", "Low Conga", "Cowbell",
    "Vibraslap", "Low-mid Tom", "High Agogo", "Open Hi Conga"};
    int[] instruments = {35, 42, 46, 38, 49, 39, 50, 60, 70, 72, 64, 56, 58, 47, 67, 63};

    public static void main(String[] args) {
        new SampleMaker().GUI();
    }
    public void GUI() {
        mainFrame = new JFrame("Sample-Maker");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        BorderLayout layout = new BorderLayout();
        JPanel backgroundPanel = new JPanel(layout);
        backgroundPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        checkFieldList = new ArrayList<JCheckBox>();
        Box buttonsField = new Box(BoxLayout.Y_AXIS);

        JButton start = new JButton("Start");
        start.addActionListener(new StartListener());
        buttonsField.add(start);

        JButton stop = new JButton("Stop");
        stop.addActionListener(new StopListener());
        buttonsField.add(stop);

        JButton tempoUp = new JButton("Faster");
        tempoUp.addActionListener(new FasterListener());
        buttonsField.add(tempoUp);

        JButton tempoDown = new JButton("Slower");
        tempoDown.addActionListener(new SlowerListener());
        buttonsField.add(tempoDown);

        JButton save = new JButton("Save");
        save.addActionListener(new SaveSample());
        buttonsField.add(save);

        JButton load = new JButton("Load");
        load.addActionListener(new LoadSample());
        buttonsField.add(load);

        Box namesField = new Box(BoxLayout.Y_AXIS);
        for (int i = 0; i < 16; i++) {
            namesField.add(new Label(instrumentsName[i]));
        }
        backgroundPanel.add(BorderLayout.EAST, buttonsField);
        backgroundPanel.add(BorderLayout.WEST, namesField);

        mainFrame.getContentPane().add(backgroundPanel);

        GridLayout checkFieldGrid = new GridLayout(16, 16);
        checkFieldGrid.setVgap(1);
        checkFieldGrid.setHgap(2);

        mainPanel = new JPanel(checkFieldGrid);
        backgroundPanel.add(BorderLayout.CENTER, mainPanel);

        for (int i = 0; i < 256; i++) {
            JCheckBox c = new JCheckBox();
            c.setSelected(false);
            checkFieldList.add(c);
            mainPanel.add(c);
        }
        configurationMidi();

        mainFrame.setBounds(50, 50, 300, 300);
        mainFrame.pack();
        mainFrame.setVisible(true);
    }
    public void configurationMidi() {
        try {
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
            sequence = new Sequence(Sequence.PPQ, 4);
            track = sequence.createTrack();
            sequencer.setTempoInBPM(120);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void makeTrackAndPlay() {
        int[] trackList = null;
        sequence.deleteTrack(track);
        track = sequence.createTrack();

        for (int i = 0; i < 16; i++) {
            trackList = new int[16];

            int key = instruments[i];

            for (int j = 0; j < 16; j++) {
                JCheckBox jc = (JCheckBox) checkFieldList.get(j + (16 * i));
                if (jc.isSelected()) {
                    trackList[j] = key;
                } else {
                    trackList[j] = 0;
                }
            }
            makeTrack(trackList);
            track.add(createEvent(176, 1, 127, 0, 16));
        }
        track.add(createEvent(192, 9, 1, 0, 15));
        try {
            sequencer.setSequence(sequence);
            sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY);
            sequencer.start();
            sequencer.setTempoInBPM(120);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void meta(MetaMessage meta) {

    }

    public class StartListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            makeTrackAndPlay();
        }
    }
    public class StopListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            sequencer.stop();
        }
    }
    public class FasterListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            float rate = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float) ((float) rate * 1.03));
        }
    }
    public class SlowerListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            float wspTempa = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float) ((float) wspTempa * .97));
        }
    }
    public void makeTrack(int[] list) {
        for (int i = 0; i < 16; i++) {
            int key = list[i];
            if (key != 0) {
                track.add(createEvent(144, 9, key, 100, i));
                track.add(createEvent(128, 9, key, 100, i + 1));
            }
        }
    }
    public static MidiEvent createEvent(int plc, int channel, int first, int second, int tact) {
        MidiEvent event = null;
        try {
            ShortMessage a = new ShortMessage();
            a.setMessage(plc, channel, first, second);
            event = new MidiEvent(a, tact);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return event;
    }
    public class SaveSample implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            boolean[] fieldCondition = new boolean[256];

            for (int i = 0; i < 256; i++) {
                JCheckBox field = (JCheckBox) checkFieldList.get(i);
                if (field.isSelected()) {
                    fieldCondition[i] = true;
                }
            }
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(new File("composition.ser"));
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
                objectOutputStream.writeObject(fieldCondition);
            } catch (IOException fileNotFoundException) {
                fileNotFoundException.printStackTrace();
            }
        }
    }
    public class LoadSample implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            boolean[] fieldCondition = null;
            try {
                FileInputStream fileInputStream = new FileInputStream(new File("composition.ser"));
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                fieldCondition = (boolean[]) objectInputStream.readObject();
            } catch (IOException | ClassNotFoundException fileNotFoundException) {
                fileNotFoundException.printStackTrace();
            }
            for (int i = 0; i < 256; i++) {
                JCheckBox field = (JCheckBox) checkFieldList.get(i);
                if (fieldCondition[i]) {
                    field.setSelected(true);
                } else {
                    field.setSelected(false);
                }
            }
            sequencer.stop();
            makeTrackAndPlay();
        }
    }
}
