package com.minisudoku2;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.URL;
import java.util.Date;

public class GUI extends JFrame {
    public static final int WIDTH = 640;
    public static final int HEIGHT = 640;
    public static final int GAP_MARGIN = 10;
    public static final Font BOX_FONT = new Font("Times New Roman",Font.BOLD,32);
    public static final Font STATUS_FONT = new Font("Times New Roman",Font.ITALIC,18);

    private String matStr;
    private final JTextField[][] Boxes = new JTextField[9][9];
    private final JButton[] Buttons = new JButton[5];
    private final JLabel StatusText = new JLabel("Enter digits and press Calculate to start. Press TAB to go to the next field.");
    private final JPanel leftPanel = new JPanel();
    private final JPanel rightPanel = new JPanel();
    private final JFileChooser chooser = new JFileChooser();

    //Constructors
    public GUI(String prompt){
        this.matStr = prompt;

        //Set Up JFrame
        this.setTitle("Mini Sudoku 2");
        this.setSize(WIDTH,HEIGHT);//can use setBound as well
        this.setLocationRelativeTo(null);//centralize the window
        this.setBackground(Color.white);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        URL iconURL = getClass().getResource("/com/minisudoku2/icon.png");
        if(iconURL!=null){
            ImageIcon icon = new ImageIcon(iconURL);
            this.setIconImage(icon.getImage());
        }else{
            logStatus("Failed to load icon!");
        }

        setVisible(true);
        this.setLayout(new BorderLayout());

        //Set Up components
        setupLayout();
        setupText();
        setupBoxes();
        setupButtons();
        setupFileChooser();

        //Refresh Size
        setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
        setExtendedState(Frame.NORMAL);
        setResizable(false);
    }
    public GUI(){
        this("000000000000000012003045000000000036000000400570008000000100000000900020706000500");
    }
    private void setupLayout(){
        leftPanel.setBackground(Color.white);
        leftPanel.setLayout(new GridLayout(9,9,GAP_MARGIN,GAP_MARGIN));
        leftPanel.setBorder(BorderFactory.createTitledBorder("WorkSpace"));
        this.add(leftPanel,BorderLayout.CENTER);

        rightPanel.setBackground(Color.lightGray);
        rightPanel.setLayout(new GridLayout(5, 1, GAP_MARGIN,GAP_MARGIN));
        rightPanel.setBorder(BorderFactory.createTitledBorder("Control"));
        this.add(rightPanel,BorderLayout.EAST);
    }
    private void setupText(){
        this.add(StatusText,BorderLayout.SOUTH);
        StatusText.setFont(STATUS_FONT);
        StatusText.setHorizontalAlignment(SwingConstants.LEFT);
        StatusText.setBorder(BorderFactory.createEmptyBorder(0,GAP_MARGIN,0,GAP_MARGIN));
    }
    private void setupBoxes(){
        for(int row = 0; row < 9; row ++){
            for (int col = 0; col < 9; col ++){
                JTextField Box = new JTextField(coordTOidx(row,col),1);
                Boxes[row][col] = Box;
                Box.setHorizontalAlignment(JTextField.CENTER);
                Box.setFont(BOX_FONT);
                Box.setBorder(BorderFactory.createEmptyBorder());
                Box.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyTyped(KeyEvent e) {
                        char c = e.getKeyChar();
                        if (c <= '9' && c >= '0'){
                            e.consume();
                            Box.setText(String.valueOf(c));
                        }else{
                            e.consume();
                        }
                    }
                });
                leftPanel.add(Box);
            }
        }
    }
    private void setupButtons(){
        JButton CalButton = new JButton("Calculate");
        CalButton.setAlignmentX(CENTER_ALIGNMENT);
        Buttons[0] = CalButton;
        CalButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CalculateCallBack();
            }
        });
        rightPanel.add(CalButton);

        JButton ClearButton = new JButton("Clear All");
        ClearButton.setAlignmentX(CENTER_ALIGNMENT);
        Buttons[1] = ClearButton;
        ClearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ClearAllCallback();
            }
        });
        rightPanel.add(ClearButton);

        JButton LoadButton = new JButton("Load from File");
        LoadButton.setAlignmentX(CENTER_ALIGNMENT);
        Buttons[2] = LoadButton;
        LoadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                LoadFileCallback();
            }
        });
        rightPanel.add(LoadButton);

        JButton ExitButton = new JButton("Exit");
        ExitButton.setAlignmentX(CENTER_ALIGNMENT);
        Buttons[3] = ExitButton;
        ExitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        rightPanel.add(ExitButton);
    }
    private void setupFileChooser(){
        chooser.setFileFilter(new FileNameExtensionFilter("TEXT FILES", "txt", "text"));
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    }

    //Helpers
    private String coordTOidx(int row, int col){
        int idx = row * 9 + col;
        return matStr.substring(idx, idx+1);
    }
    private String getMatStr() {
        StringBuilder sb = new StringBuilder("");
        for(int row = 0; row < 9; row ++){
            for (int col = 0; col < 9; col ++){
                sb.append(Boxes[row][col].getText());
            }
        }
        matStr = sb.toString();
        return matStr;
    }
    private void setMatStr(String answer){
        matStr = answer;
        for(int row = 0; row < 9; row ++){
            for (int col = 0; col < 9; col ++){
                Boxes[row][col].setText(coordTOidx(row,col));
            }
        }
    }
    private void safetyLock(boolean isLocked){
        leftPanel.setBackground(isLocked?Color.gray:Color.white);
        Buttons[0].setEnabled(!isLocked);
        for(JTextField[] row: Boxes){
            for(JTextField ele: row){
                ele.setEnabled(!isLocked);
            }
        }
    }
    private void logStatus(String log){
        StatusText.setText(log);
    }
    private String txtParser(File file) throws IllegalArgumentException, IOException{
        if(!file.exists() || !file.isFile() ||!file.canRead())
            throw new IllegalArgumentException("Load error! The file is corrupted!");
        StringBuilder sb = new StringBuilder();
        char buffer;
        boolean skip = false;
        FileInputStream fis = new FileInputStream(file);
        InputStreamReader isr = new InputStreamReader(fis);
        while ((buffer = (char) isr.read()) != -1) {
            if (skip){
                if (buffer == '\n')
                    skip = false;
                continue;
            }
            if (buffer == '*'){
                skip = true;
            }
            else if(buffer >= '0' && buffer <= '9') {
                sb.append(buffer);
                if (sb.length() >= 81) {
                    fis.close();
                    isr.close();
                    return sb.toString();
                }
            }
        }
        fis.close();
        isr.close();
        while (sb.length() < 81) {
            sb.append("0");
        }
        return sb.toString();
    }

    //Callbacks
    private void CalculateCallBack(){
        safetyLock(true);
        Grid game;
        try{
            game = new Grid(getMatStr());
        }catch(IllegalArgumentException e){
            logStatus(e.getMessage());
            safetyLock(false);
            return;
        }
        Date begin = new Date();
        logStatus("Calculating...");
        game.calculate();
        if(game.isSolved()){
            logStatus(String.format("Solved! Time used: %d ms.",new Date().getTime()-begin.getTime()));
        }else{
            logStatus("Invalid input! No solution.");
        }
        setMatStr(game.getAnsString());
        safetyLock(false);

    }
    private void ClearAllCallback(){
        setMatStr("000000000000000000000000000000000000000000000000000000000000000000000000000000000");
    }
    private void LoadFileCallback(){
        logStatus("Select a Text File for prompt!");
        chooser.showDialog(new JLabel(), "Load");
        File txtFile = chooser.getSelectedFile();
        if (txtFile == null || !txtFile.exists() || !txtFile.isFile()) {
            logStatus("File selection aborted!");
            return;
        }
        logStatus("Loaded " + txtFile.toString() + "! Parsing ...");
        String strObtained;
        try{
            strObtained = txtParser(txtFile);
        }catch (IllegalArgumentException e){
            logStatus(e.getMessage());
            return;
        }catch (IOException e){
            e.printStackTrace();
            logStatus(e.getMessage());
            return;
        }
        setMatStr(strObtained);
        logStatus("Parsed " + txtFile.toString() + "!");
    }
}
