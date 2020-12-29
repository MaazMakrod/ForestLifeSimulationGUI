/*
    Summative Project
    ------------------
    Authors: Ahmad Sheikh, Maaz Makrod
    Date: January 20, 2020
    Class: ICS 4U
    Teacher: Mr. Jay
    Description: A life simulation game where the user can play god and interact in the environment
 */

import java.awt.*;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.io.File;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.Random;

public class LifeSimulationGUI extends JFrame implements ActionListener, ChangeListener
{
   //Variables to adjust window size
    static int playableWidth = 750, playableHeight = 750;//500x500 works best
    static int gridWidth = playableWidth / 20, gridHeight = (playableHeight-40) / 20;

    //Booleans to adjust behaviour of elements when using certain disasters or effects
    boolean ifReset = false;
    boolean ifDrought = false;
    boolean ifEarthquake = false;
    boolean ifFastBreed = false;
    int healthyBirthRate = 1; //changes birthrate to this constant if the quick-breed effect is used


    static Colony colony = new Colony (gridWidth, gridHeight);
    static JSlider speedSldr = new JSlider ();
    static Timer t;
    JButton simulateBtn, toolBtn, applyBtn, resetBtn, playMusic;
    JComboBox tools, elements;
    String[] toolChoices = {"Tools", "Marquee Tool", "Pencil", "Earthquake", "Tsunami", "Drought" , "Disease", "Quick-Breed"};
    String[] elementChoices = {"Elements", "People", "Foxes", "Water", "Trees", "Rocks", "Erase"};
    Movement moveColony = new Movement (colony); // ActionListener
    JLabel generation, foxPopulation, peoplePopulation, title;
    Clip clip;

    int toolSelected;
    int pressX, pressY, dragX, dragY;

    //======================================================== constructor
    public LifeSimulationGUI ()
    {
        // 1... Create/initialize components
        simulateBtn = new JButton ("Simulate");
        resetBtn = new JButton("Reset");
        toolBtn = new JButton("Select Tool");
        applyBtn = new JButton("Apply");
        playMusic = new JButton("Play Music");
        tools = new JComboBox(toolChoices);
        elements = new JComboBox(elementChoices);
        title = new JLabel ("FOREST LIFE SIMULATION", JLabel.CENTER);
        generation = new JLabel ("Generation: -");
        foxPopulation = new JLabel("Fox Population: -");
        peoplePopulation = new JLabel("People Population: -");

        simulateBtn.addActionListener (this);
        resetBtn.addActionListener(this);
        tools.addActionListener(this);
        toolBtn.addActionListener(this);
        elements.addActionListener(this);
        applyBtn.addActionListener(this);
        playMusic.addActionListener(this);
        speedSldr.addChangeListener (this);
        t = new Timer (1, moveColony); // set up timer
        title.setFont(new Font("Times New Roman", Font.BOLD, 14));
        title.setForeground(Color.BLACK);
        speedSldr.setOpaque(false);

        // 2... Create content pane, set layout
        JPanel content = new JPanel ();        // Create a content pane
        content.setLayout (new BorderLayout ()); // Use BorderLayout for panel
        JPanel east = new JPanel ();
        east.setLayout (new FlowLayout ()); // Use FlowLayout for input area
        east.setPreferredSize(new Dimension(200, playableHeight));
        east.setBackground(Color.WHITE);

        DrawArea board = new DrawArea (playableWidth, playableHeight);

        // 3... Add the components to the input area.

        east.add(title);
        east.add (simulateBtn);
        east.add(resetBtn);
        east.add(tools);
        east.add(elements);
        east.add(toolBtn);
        east.add(applyBtn);
        east.add(playMusic);
        east.add (speedSldr);
        east.add (generation);
        east.add(foxPopulation);
        east.add(peoplePopulation);

        MouseActions mouseActions = new MouseActions();
        board.addMouseListener(mouseActions);
        board.addMouseMotionListener(mouseActions);

        content.add (east, "East"); // Input area
        content.add (board, "West"); // Output area

        // 4... Set this window's attributes.
        setContentPane (content);
        pack ();
        setTitle ("Forest Life Simulation");
        setSize (playableWidth + 200, playableHeight);
        setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo (null);           // Center window.
    }

    public void stateChanged (ChangeEvent e)
    {
        if (t != null)
            t.setDelay (600 - 6 * speedSldr.getValue ()); // 0 to 600 ms
    }

    public void playMusic ()
    {
        AudioInputStream audioInputStream = null;
        try {
            audioInputStream = AudioSystem.getAudioInputStream(new File("src/TwistAndShout.wav").getAbsoluteFile());
        } catch (UnsupportedAudioFileException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        clip = null;
        try {
            clip = AudioSystem.getClip();
        } catch (LineUnavailableException ex) {
            ex.printStackTrace();
        }
        try {
            clip.open(audioInputStream);
        } catch (LineUnavailableException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        clip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    public void pauseMusic()
    {
        clip.stop();
    }

    public void actionPerformed (ActionEvent e)
    {
        if (e.getActionCommand ().equals ("Simulate"))
        {
            t.start (); // start simulation
            simulateBtn.setText("Stop");
        }
        else if (e.getActionCommand ().equals ("Stop"))
        {
            t.stop (); // start simulation
            simulateBtn.setText("Simulate");
        }
        else if (e.getActionCommand().equals("Play Music")){
            playMusic();
            playMusic.setText("Mute Music");
        }
        else if (e.getActionCommand().equals("Mute Music")){
            pauseMusic();
            playMusic.setText("Play Music");
        }
        else if (e.getActionCommand().equals("Apply")){
            colony.populateMarquee(colony.getxPos(), colony.getyPos(), colony.getWidth(), colony.getHeight(), elements);

            tools.setSelectedItem("Tools");
            elements.setSelectedItem("Elements");
            toolSelected = 0;
        }
        else if (e.getActionCommand().equals("Finish")){
            tools.setSelectedItem("Tools");
            elements.setSelectedItem("Elements");
            toolSelected = 0;
            applyBtn.setText("Apply");
        }
        else if (e.getActionCommand().equals("Select Tool")){
            if (tools.getSelectedItem().equals("Marquee Tool")){
                toolSelected = 1; //selects tool 1
            }
            else if (tools.getSelectedItem().equals("Pencil")){
                applyBtn.setText("Finish");
                toolSelected = 2; //selects tool 2
            }
            else if (tools.getSelectedItem().equals("Tools")){
                //do nothing
            }
            applyBtn.setText("Apply"); //when switching from pencil to marquee tool without clicking finish
        }
        else if (e.getActionCommand().equals("Select Effect")){
            if (tools.getSelectedItem().equals("Earthquake")) {
                colony.earthquake(); //causes earthquake
                ifEarthquake = true; //stops regeneration of water and trees unless generation number % 100 is 0
            }
            if (tools.getSelectedItem().equals("Tsunami")) {
                colony.tsunami(); //causes a tsunami
                ifDrought = false; //allows water to spawn again instantly
            }
            if (tools.getSelectedItem().equals("Drought")) {
                colony.drought();
                ifDrought = true; //stops water from regenerating unless generation number % 150 = 0
            }
            if (tools.getSelectedItem().equals("Disease")){
                colony.disease(); //spreads a disease and kills off a random amount of living craetures
                ifFastBreed = false; //makes quick-breed stop
            }
            if (tools.getSelectedItem().equals("Quick-Breed")){
                ifFastBreed = true; //allows living creatures to have a higher chance of breeding
            }

            tools.setSelectedItem("Tools");
            toolBtn.setText("Select Tool");
        }
        else if (tools.getSelectedItem().equals("Earthquake") || tools.getSelectedItem().equals("Tsunami") || tools.getSelectedItem().equals("Drought") || tools.getSelectedItem().equals("Disease") || tools.getSelectedItem().equals("Quick-Breed")){
            toolBtn.setText("Select Effect"); //change button if selecting an effect
        }
        //resets entire board
        else if (e.getActionCommand().equals("Reset")){
            colony.spawn(); //spawns new elements
            ifReset = true; //boolean to reset tick
            //set all effects to false
            ifFastBreed = false;
            ifDrought = false;
            ifEarthquake = false;
        }
        repaint ();            // refresh display of deck
    }

    class MouseActions implements MouseListener, MouseMotionListener { //listens to the mouses actions
        //ONLY NEED mousePressed AND mouseDragged BUT OTHER METHODS MUST BE IMPLEMENTED
        public void mouseClicked(MouseEvent e) {

        }

        public void mousePressed(MouseEvent e) { //use pressed since we are not releasing
            if (toolSelected == 1) { //if marquee is chosen
                pressX = e.getX();
                pressY = e.getY();
            }
            else{
                pressX = 0;
                pressY = 0;
            }
        }

        public void mouseReleased(MouseEvent e) {

        }

        public void mouseMoved(MouseEvent e) {

        }

        public void mouseDragged(MouseEvent e) {//listens to motion of mouse
            if (toolSelected == 1) { //if marquee is chosen
                dragX = e.getX();
                dragY = e.getY();
            }
            else if (toolSelected == 2){
                //pencil tool
                dragX = e.getX();
                dragY = e.getY();

                if (dragX < gridWidth*20 && dragY < gridHeight*20 && dragX >= 0 && dragY >= 0)
                    colony.populatePencil(dragX, dragY, gridWidth, gridHeight, elements);
            }
            else{
                dragX = 0;
                dragY = 0;
            }
        }

        public void mouseEntered(MouseEvent e) {

        }

        public void mouseExited(MouseEvent e) {

        }
    }

    class DrawArea extends JPanel
    {
        public DrawArea (int width, int height) {
            this.setPreferredSize(new Dimension(width, height)); //size of the draw area
        }

        public void paintComponent (Graphics g)//Accepts graphics so that it can send it to the show method in the colony class
        {
            try {//Uses a try-catch because the show class throws an IOException
                colony.show (g);//calls the method

                //--------TOOLS------------

                if (toolSelected == 0) {
                    //do nothing
                }
                else if (toolSelected == 1){//use the marquee tool
                    //set variables for pressing and dragging
                    colony.marquee(g, pressX, pressY, dragX, dragY, playableWidth, playableHeight);
                }

                //-----END TOOLS-----------

            } catch (IOException e) {//Catches if there is any error loading the image
                e.printStackTrace();
            }
        }
    }

    //Calls the advance method in the colony class that controls all the movement
    class Movement implements ActionListener
    {
        private Colony colony;
        private int i = 0;


        public Movement (Colony col)
        {
            colony = col;
        }//Constructor method sets a colony

        public void actionPerformed (ActionEvent event)
        {
            colony.advance (gridWidth, gridHeight, ifDrought, ifFastBreed, healthyBirthRate, ifEarthquake);//colony advances to the next generation
            components [][] grid = colony.getGrid();//IDK IF WE ARE GOING TO KEEP THIS
            int amountOfPeople = 0, amountOfFoxes = 0;
            i++;

            for (int r = 0 ; r < grid.length ; r++)
                for (int c = 0 ; c < grid [0].length ; c++) {
                    if (grid[r][c].getType() == 1)
                        amountOfPeople++;
                    if (grid[r][c].getType() == 2)
                        amountOfFoxes++;
                }

            generation.setText("Generation: " + i);
            foxPopulation.setText("Fox Population: " + amountOfFoxes);
            peoplePopulation.setText("People Population: " + amountOfPeople);//" || F Left: " + amountOfFoxes + " || P left: " + amountOfPeople);
            repaint ();//Calls the paint method

            //makes effects of drought and quick-breed stop after every 150 generations
            if (i % 150 == 0) {
                ifDrought = false;
                ifFastBreed = false;
            }

            //makes effects of earthquake stop after every 100 generations
            if (i % 100 == 0){
                ifEarthquake = false;
            }

            //if the game is reset, the generation count resets to 0
            if (ifReset){
                i = 0;
                ifReset = false;
            }
        }

    }

    //======================================================== method main
    public static void main (String[] args)
    {
        LifeSimulationGUI window = new LifeSimulationGUI ();//Creates a life simulation GUI window
        window.setVisible (true);//Sets the window to visible so that the program is visible
    }
}

//Class that contains all methods relating to the colony (advance, live, properties, etc.)
class Colony
{
    private int xPos, yPos, width, height;
    private components grid[] [];//components 2D array that is the 'grid' of the draw area....Everything moves based off of this array

    //Is the default constructor that creates a random colony
    public Colony (int gridWidth, int gridHeight)
    {
        this.grid = new components [gridHeight] [gridWidth];

        //spawns elements on board
        spawn();

        //This loops through and ensures that there are no overlapping images...This only occurs because of the trees that in the program use 1 cell but look like they take up 4
        for (int r = 0 ; r < grid.length ; r++) {
            for (int c = 0; c < grid[0].length; c++) {
                if(grid[r][c].getType() == 3){//If the type is a tree
                    if(c+1 < grid[r].length && grid[r][c+1].getType() != 0)//If there is something on its right
                        grid[r][c+1] = new components();//Delete it
                    if(r+1 < grid.length && c+1 < grid[r].length && grid[r+1][c+1].getType() == 5)//If there is water under 'the bushy part' delete it
                        grid[r+1][c+1] = new components();
                    if(r+1 < grid.length && grid[r+1][c].getType() == 5)//If there is water under the 'right bushy part' delete it
                        grid[r+1][c] = new components();
                }
            }
        }
    }

    //method to spawn all elements
    public void spawn(){
        for (int r = 0 ; r < grid.length ; r++)//Loops through the 2D array
        {
            for (int c = 0 ; c < grid [0].length ; c++)
            {
                double rand = Math.random();//Math.random() generates a number between 0 and 1

                if(rand <= 0.58)//58% chance of the area being a empty space
                {
                    grid[r][c] = new components();
                }
                else if(rand <= 0.6)//2% chance of it being a rock
                {
                    grid[r][c] = new rock(4);
                }
                else if(rand <= 0.62)//2% chance of it being a tree
                {
                    grid[r][c] = new tree (3);
                }
                else if(rand <= 0.9)//28% chance of it being water
                {
                    grid[r][c] = new water (5);
                }
                else if(rand <= 0.97)//7% chance of it being a person
                {
                    grid[r][c] = new person (1);
                }
                else//Last choice, 3% chance of it being a fox
                {
                    grid[r][c] = new fox (2);
                }
            }
        }
    }

    //This displays all the graphics of the program
    public void show (Graphics g) throws IOException  { // Throws IOException so that when trying to find files it is fine

        Image background = ImageIO.read(new File("src/Background.png"));//Loads the background file
        g.drawImage(background, 0, 0,null);//Prints out the background image

        //constant images
        Image waterImg, rockImg, treeImg; // personImg, foxImg;

        //loading all images beforehand
        waterImg = ImageIO.read(new File("src/Water.png"));//Water URL is constant
        rockImg = ImageIO.read(new File("src/rock1.png"));//Rock URL is constant
        treeImg = ImageIO.read(new File("src/tree_withFood.png"));//Tree URL is always constant
        //personImg = ImageIO.read(new File("src/BoyF_Run2.png"));//Get the URL of the person and draw it out
        //foxImg = ImageIO.read(new File("src/BoyFoxF_Stand.png"));//Get the URL of the person and draw it out

        for (int row = 0 ; row < grid.length ; row++)//Goes through the loop and sets the graphics
            for (int col = 0 ; col < grid [0].length ; col++)
            {
                Image img;//Image img is set for all sprites that have animation

                if (grid[row][col].getType() == 1) //Person
                {
                    if(((person)grid[row][col]).getAlive())//Check if it is alive
                    {
                        img = ImageIO.read(new File(((person) grid[row][col]).getURL()));//Get the URL of the person and draw it out
                        g.drawImage(img, col*20+5, row*20+2, null);//image, starting x, starting y, image observer
                    }
                }
                else if (grid[row][col].getType() == 2) //Fox
                {
                    if(((fox)grid[row][col]).getAlive())//Check if it is alive
                    {
                        img = ImageIO.read(new File(((fox) grid[row][col]).getURL()));//Get the image URL of the fox
                        g.drawImage(img, col*20+2, row*20+2, 20, 20, null);//Image, starting x, starting y, width x, width y, image observer
                    }
                }
                else if (grid[row][col].getType() == 3) //Tree
                {
                    if(((tree)grid[row][col]).getActive())
                    {
                        g.drawImage(treeImg, col*20+2, row*20+2, 40, 40, null);//Image, starting x, starting y, width x, width y, image observer
                    }
                }
                else if (grid[row][col].getType() == 4) //Rock
                {
                    if(((rock)grid[row][col]).getActive())
                    {
                        g.drawImage(rockImg, col*20+5, row*20+5, 10, 10, null);//Image, starting x, starting y, width x, width y, image observer
                    }
                }
                else if (grid[row][col].getType() == 5) //Water
                {
                    g.drawImage(waterImg, col * 20 + 2, row * 20 + 2,null); //image, starting x, starting y, image observer);
                }
            }
    }

    //---TOOLS METHODS---

    //box select tool. Only works for pos coordinates
    public void marquee (Graphics g, int pressX, int pressY, int dragX, int dragY, int playableWidth, int playableHeight){
        //initialize width and height variable for selection
        int width, height;

        //create and set selection colour with alpha composite
        Color selection = new Color(245, 255, 181);
        AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f); //set opacity for selection
        Graphics2D g2d = (Graphics2D) g;
        g2d.setComposite(ac);
        g.setColor(selection);

        //make selections alligned to the grid
        for (; pressX % 20 != 0; pressX++){}

        for (; pressY % 20 != 0; pressY++){}

        width = dragX - pressX;
        height = dragY - pressY;

        for (int i = 0; width % 20 != 0; i++)
            width++;

        for (int i = 0; height % 20 != 0; i++)
            height++;

        xPos = pressX - 15;
        yPos = pressY - 15;
        this.width = width;
        this.height = height;

        //drawing rectangle
        if (xPos + width <= playableWidth && yPos + height<= playableHeight)
            g.fillRect(xPos-3, yPos-3, width, height);
        else{
            xPos = 0;
            yPos = 0;
        }
    }

    public int getxPos() {
        return xPos;
    }

    public int getyPos() {
        return yPos;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    //---END TOOLS METHOD---

    //spawns elements chosen by marquee tool
    public void populateMarquee(int xPos, int yPos, int width, int height, JComboBox elements){

        //keep numbers within bounds of tiles
        xPos = xPos / 20;
        yPos = yPos / 20;
        width = width / 20;
        height = height / 20;

        for (int y = 0; y < height; y++){
            for (int x = 0; x < width; x++){
                if (elements.getSelectedItem().equals("People"))//add people
                    grid[yPos+y][xPos+x] = new person(1);
                else if (elements.getSelectedItem().equals("Foxes"))
                    grid[yPos+y][xPos+x] = new fox(2);
                else if (elements.getSelectedItem().equals("Trees"))
                    grid[yPos+y][xPos+x] = new tree(3);
                else if (elements.getSelectedItem().equals("Rocks"))
                    grid[yPos+y][xPos+x] = new rock(4);
                else if (elements.getSelectedItem().equals("Water"))
                    grid[yPos+y][xPos+x] = new water(5);
                else if (elements.getSelectedItem().equals("Erase"))
                    grid[yPos+y][xPos+x] = new components();
            }
        }
    }

    //spawns elements wherever the cursor is being dragged
    public void populatePencil(int xPos, int yPos, int gridWidth, int gridHeight, JComboBox elements){
        //keep numbers within bounds of tiles
        xPos = xPos / 20;
        yPos = yPos / 20;

        int playableWidth = gridWidth*20;
        int playableHeight = gridHeight*20;

        if (elements.getSelectedItem().equals("People"))//add people
            grid[yPos][xPos] = new person(1);
        else if (elements.getSelectedItem().equals("Foxes"))
            grid[yPos][xPos] = new fox(2);
        else if (elements.getSelectedItem().equals("Trees")) {
            grid[yPos][xPos] = new tree(3);

            if(xPos + 1 < grid[0].length && grid[yPos][xPos+1].getType() != 0)
                grid[yPos][xPos+1] = new components();
            if(yPos + 1 < grid.length && grid[yPos+1][xPos].getType() == 5)
                grid[yPos+1][xPos] = new components();
            if(xPos + 1 < grid[0].length && yPos + 1 < grid.length && grid[yPos+1][xPos+1].getType() == 5)
                grid[yPos+1][xPos+1] = new components();
        }
        else if (elements.getSelectedItem().equals("Rocks"))
            grid[yPos][xPos] = new rock(4);
        else if (elements.getSelectedItem().equals("Water")) {
            //FIX ERROR AT BOUNDS
            if (xPos - 20 >= 20 && xPos + 20 < playableWidth && yPos - 20 >= 20 && yPos + 20 < playableHeight)
                grid[yPos-1][xPos-1] = new water(5);
            if(yPos - 1 >= 0)
                grid[yPos-1][xPos] = new water(5);
            if(xPos - 1 >= 0)
                grid[yPos][xPos-1] = new water(5);
            if(yPos + 1 < grid.length)
                grid[yPos+1][xPos] = new water(5);
            if(xPos < grid[0].length)
                grid[yPos][xPos+1] = new water(5);
            if(yPos < grid.length && xPos + 1 < grid[0].length)
                grid[yPos+1][xPos+1] = new water(5);

            grid[yPos][xPos] = new water(5);

            if(xPos - 1 >= 0 && grid[yPos][xPos-1].getType() == 3)
                grid[yPos][xPos-1] = new water(5);
            if(yPos - 1 >= 0 && grid[yPos-1][xPos].getType() == 3)
                grid[yPos-1][xPos] = new water(5);
            if(xPos - 1 >= 0 && yPos - 1 >= 0 && grid[yPos-1][xPos-1].getType() == 3)
                grid[yPos-1][xPos-1] = new water(5);
        }
        else if (elements.getSelectedItem().equals("Erase"))
            grid[yPos][xPos] = new components();
    }

    Random r = new Random();

    //destroys all elements randomly throughout the board
    public void earthquake(){
        for (int i = 0; i < grid.length; i++){
            for (int j = 0; j < grid[0].length; j++){
                int random = r.nextInt(3);

                if (random == 0) { //33% chance of a tile being set to no element
                    grid[i][j] = new components();
                }
            }
        }
    }

    //causes water to spawn randomly around the board and kills some people
    public void tsunami(){
        for (int i = 0; i < grid.length; i++){
            for (int j = 0; j < grid[0].length; j++){
                int randomWater = r.nextInt(2);
                int randomDeath = r.nextInt(5);

                if (randomWater == 0) //50% chance of water showing up ANYWHERE (so it can go over living things)
                    grid[i][j] = new water(5);
                if ((grid[i][j].getType() == 1 || grid[i][j].getType() == 2) && randomDeath == 0) //20% chance of living things irregardless if water lands on them
                    grid[i][j] = new components();

            }
        }
    }

    //causes water to go away around the board.
    public void drought(){
        for (int i = 0; i < grid.length; i++){
            for (int j = 0; j < grid[0].length; j++){
                int random = r.nextInt(5);

                if (grid[i][j].getType() == 5 && random == 0) {
                    grid[i][j] = new components();
                }
            }
        }
    }

    //has a 33% chance of killing living things
    public void disease(){
        for (int i = 0; i < grid.length; i++){
            for (int j = 0; j < grid[0].length; j++){
                int random = r.nextInt(3);

                if ((grid[i][j].getType() == 1 || grid[i][j].getType() == 2) && random == 0) {
                    grid[i][j] = new components();
                }
            }
        }
    }

    //Live method decides what will survive to the next generation
    public components [][] peopleLive (components [] [] nextGen, int r, int c, int peoplePopulation, int foxPopulation, int waterAvailable, int gridWidth, int gridHeight, boolean ifFastBreed, int healthyBirthRate)//Accepts the nextGen grid, row, col, and population of people, foxes, and water
    {
        boolean changed = false;//Checks if the cell was already changed, if so it cannot be changed again
        int energy = ((person) grid[r][c]).getEnergy() - 1;//Energy of person, Subtract one from the energy because they survived to the next generation
        ((person)nextGen[r][c]).setLife(true);//The life is true for now

        if (r - 1 >= 0) {//If there is a cell above them
            if (grid[r - 1][c].getType() == 5) { //If it is water add energy and changed equals true
                energy += 2;
                changed = true;
            }
            else if(grid[r-1][c].getType() == 3) { //If it is a tree add energy and changed equals true
                energy += 2;
                changed = true;
            }
            else if(grid[r-1][c].getType() == 2){ //If it is a fox determine fight/death status
                double rand = Math.random();

                if(rand <= 0.5)//50% chance the fox kills the human and gains 10 energy - NOTE THERE IS A 30% CHANCE NOTHING HAPPENS
                {
                    ((person)nextGen[r][c]).setLife(false);
                    ((fox)nextGen[r-1][c]).setEnergy(((fox)nextGen[r-1][c]).getEnergy() + 10);
                    changed = true;
                }
                else if(rand <= 0.7)//20% chance the human kills the fox and gains 10 energy
                {
                    ((fox)nextGen[r-1][c]).setLife(false);
                    ((person)nextGen[r][c]).setEnergy(((person)nextGen[r][c]).getEnergy() + 10);
                    changed = true;
                }
            }
            else if(grid[r-1][c].getType() == 1)//A person is above them
            {
                if (!(((person)grid[r-1][c]).getGender().equals(((person)grid[r][c]).getGender())) && waterAvailable > (peoplePopulation + foxPopulation) * 1.5)//If the person is of opposite gender and there is plenty of water available
                {
                    double rand = Math.random();
                    double percent;

                    //makes all living elements have a higher chance of breeding regardless of population
                    if (ifFastBreed)
                        percent = healthyBirthRate;
                    else if(peoplePopulation < (gridWidth*gridHeight * .08)) //If the population dropped below 100
                        percent = 0.5;//Chance of reproduction is 50%
                    else//Else it is 10%
                        percent = 0.1;

                    if(rand <= percent)//If random number generated is less than the percent
                    {
                        rand = (Math.random() * 3) + 1;//Generates random num between 1 and 3 to choose an area to put the new person
                        changed = true;

                        if(rand <= 1 && r+1 < grid.length && grid[r+1][c].getType() == 0)//If the space below is available, put the person
                            nextGen[r+1][c] = new person(1);
                        else if(rand <= 2  && c+1 < grid[r].length && grid[r][c+1].getType() == 0)//If the space to the right is available put the person
                            nextGen[r][c+1] = new person (1);
                        else if(rand <= 3 && c-1 >= 0 && grid[r][c-1].getType() == 0)//If the space to the left is available, put the person
                            nextGen[r][c-1] = new person (1);
                    }
                }
            }
        }

        if (r + 1 < grid.length && !changed) {//If the person was never changed and there is a space above it, repeat the code for the space above
            if (grid[r + 1][c].getType() == 5) {//If it is water add energy and changed equals true
                energy += 2;
                changed = true;
            }
            else if(grid[r+1][c].getType() == 3) {//If it is tree add energy and changed equals true
                energy += 2;
                changed = true;
            }
            else if(grid[r+1][c].getType() == 2){//If it is a fox determine fight/death status
                double rand = (Math.random() * 10) + 1;

                if(rand <= 5)//50% chance the fox kills the human and gains 10 energy - NOTE THERE IS A 30% CHANCE NOTHING HAPPENS
                {
                    ((person)nextGen[r][c]).setLife(false);
                    ((fox)nextGen[r+1][c]).setEnergy(((fox)nextGen[r+1][c]).getEnergy() + 10);
                    changed = true;
                }
                else if(rand <= 7)//20% chance the human kills the fox and gains 10 energy
                {
                    ((fox)nextGen[r+1][c]).setLife(false);
                    ((person)nextGen[r][c]).setEnergy(((person)nextGen[r][c]).getEnergy() + 10);
                    changed = true;
                }
            }
            else if(grid[r+1][c].getType() == 1)//A person is above them
            {
                if (!(((person)grid[r+1][c]).getGender().equals(((person)grid[r][c]).getGender())) && waterAvailable > (peoplePopulation + foxPopulation) * 1.5)//If the person is of opposite gender and there is plenty of water available
                {
                    double rand = Math.random();
                    double percent;

                    //makes all living elements have a higher chance of breeding regardless of population
                    if (ifFastBreed)
                        percent = healthyBirthRate;
                    else if(peoplePopulation < (gridWidth*gridHeight * .08)) //If the population dropped below 100
                        percent = 0.5;//Chance of reproduction is 50%
                    else//Else it is 10%
                        percent = 0.1;

                    if(rand <= percent)//If random number generated is less than the percent
                    {
                        rand = (Math.random() * 3) + 1;//Generates random num between 1 and 3 to choose an area to put the new person
                        changed = true;

                        if(rand <= 1 && r-1 >= 0 && grid[r-1][c].getType() == 0)//If there is a available spot above put person there
                            nextGen[r-1][c] = new person(1);
                        else if(rand <= 2  && c+1 < grid[r].length && grid[r][c+1].getType() == 0)//If there is a available spot to the right put person there
                            nextGen[r][c+1] = new person (1);
                        else if(rand <= 3 && c-1 >= 0 && grid[r][c-1].getType() == 0)//If there is a available spot to the left put person there
                            nextGen[r][c-1] = new person (1);
                    }
                }
            }
        }

        if (c + 1 < grid[r].length && !changed) {//If there is a space to the left and not changed
            if (grid[r][c + 1].getType() == 5) {//If it is water add energy and changed is true
                energy += 2;
                changed = true;
            }
            else if(grid[r][c+1].getType() == 3) {//If it is a tree add energy and changed is true
                energy += 2;
                changed = true;
            }
            else if(grid[r][c+1].getType() == 2){//If it is a fox determine fight/death status
                double rand = (Math.random() * 10) + 1;

                if(rand <= 5)//50% chance the fox kills the human and gains 10 energy - NOTE THERE IS A 30% CHANCE NOTHING HAPPENS
                {
                    ((person)nextGen[r][c]).setLife(false);
                    ((fox)nextGen[r][c+1]).setEnergy(((fox)nextGen[r][c+1]).getEnergy() + 10);
                    changed = true;
                }
                else if(rand <= 7)//20% chance that the person kills the fox and gains 10 energy
                {
                    ((fox)nextGen[r][c+1]).setLife(false);
                    ((person)nextGen[r][c]).setEnergy(((person)nextGen[r][c]).getEnergy() + 10);
                    changed = true;
                }
            }
            else if(grid[r][c+1].getType() == 1)//If it is a person
            {
                if (!(((person)grid[r][c+1]).getGender().equals(((person)grid[r][c]).getGender())) && waterAvailable > (peoplePopulation + foxPopulation) * 1.5)//Opposite gender and water can support new person
                {
                    double rand = Math.random();
                    double percent;

                    //makes all living elements have a higher chance of breeding regardless of population
                    if (ifFastBreed)
                        percent = healthyBirthRate;
                    else if(peoplePopulation < (gridWidth*gridHeight * .08)) //If the population dropped below 100
                        percent = 0.5;//Chance of reproduction is 50%
                    else//Else it is 10%
                        percent = 0.1;

                    if(rand <= percent)//Chooses where to put person is rand is less than/equal to percent
                    {
                        rand = (Math.random() * 3) + 1;
                        changed = true;

                        if(rand <= 1 && r+1 < grid.length && grid[r+1][c].getType() == 0)//If there is a spot below that is available put person there
                            nextGen[r+1][c] = new person(1);
                        else if(rand <= 2  && r-1 >= 0 && grid[r-1][c].getType() == 0)//If there is a spot above that is available put person there
                            nextGen[r-1][c] = new person (1);
                        else if(rand <= 3 && c-1 >= 0 && grid[r][c-1].getType() == 0)//If there is a spot to the left that is available put person there
                            nextGen[r][c-1] = new person (1);
                    }
                }
            }
        }

        if (c - 1 >= 0 && !changed) {//If the spot to the left is available and it is unchanged
            if (grid[r][c - 1].getType() == 5) {//If it is water add 2 energy
                energy += 2;
            }
            else if(grid[r][c-1].getType() == 3) {//If it is a tree add 2 energy
                energy += 2;
            }
            else if(grid[r][c-1].getType() == 2){//If it is a fox
                double rand = (Math.random() * 10) + 1;

                if(rand <= 5)//50% chance of death for the person - 30% CHANCE NOTHING HAPPENS
                {
                    ((person)nextGen[r][c]).setLife(false);
                    ((fox)nextGen[r][c-1]).setEnergy(((fox)nextGen[r][c-1]).getEnergy() + 10);
                    changed = true;
                }
                else if(rand <= 7)//20% chance that the person kills the fox
                {
                    ((fox)nextGen[r][c-1]).setLife(false);
                    ((person)nextGen[r][c]).setEnergy(((person)nextGen[r][c]).getEnergy() + 10);
                    changed = true;
                }
            }
            else if(grid[r][c-1].getType() == 1)//If it is also a person
            {
                if (!(((person)grid[r][c-1]).getGender().equals(((person)grid[r][c]).getGender())) && waterAvailable > (peoplePopulation + foxPopulation) * 1.5)//check if water can support another peron and they are opposite gender
                {
                    double rand = Math.random();
                    double percent;

                    //makes all living elements have a higher chance of breeding regardless of population
                    if (ifFastBreed)
                        percent = healthyBirthRate;
                    else if(peoplePopulation < (gridWidth*gridHeight * .08)) //If the population dropped below 100
                        percent = 0.5;//Chance of reproduction is 50%
                    else//Else it is 10%
                        percent = 0.1;

                    if(rand <= percent)//If rand number generated is less than percent
                    {
                        rand = (Math.random() * 3) + 1;
                        changed = true;

                        if(rand <= 1 && r+1 < grid.length && grid[r+1][c].getType() == 0)//Check if spot below is available and put person there
                            nextGen[r+1][c] = new person(1);
                        else if(rand <= 2  && c+1 < grid[r].length && grid[r][c+1].getType() == 0)//Check if spot to the right is available and put person there
                            nextGen[r][c+1] = new person (1);
                        else if(rand <= 3 && r-1 >= 0 && grid[r-1][c].getType() == 0)//Check if spot above is available and put person there
                            nextGen[r-1][c] = new person (1);
                    }
                }
            }
        }

        if (c - 2 >= 0 && !changed && grid[r][c-2].getType() == 3) {//If the person was never changed, check if it is to the right of a tree's (type 3) 'bushy part' and give it energy...The reason this is done is because the busy part in the second cell is type 0, so it is never accounted for
            energy += 2;
            changed = true;
        }

        if (c - 1 >= 0 && r+1 < grid.length && !changed && grid[r+1][c-1].getType() == 3) {//If the person was never changed, check if it is below and to the right of a tree's 'bushy part' and give it energy
            energy += 2;
            changed = true;
        }

        if (c - 1 >= 0 && r-1 >= 0 && !changed && grid[r-1][c-1].getType() == 3) {//If the person was never changed, check if it is above and to the right of a tree's 'bushy part' and give it energy
            energy += 2;
        }

        if (energy > 100)//If the energy is greater than 100, set it to 100
            energy = 100;

        if (((person) grid[r][c]).getAlive()) {//If the person is still alive, set the energy to the new energy
            ((person) grid[r][c]).setEnergy(energy);
        }

        if(energy < 1)//If energy is lees than 1, person is dead
            ((person)nextGen[r][c]).setLife(false);

        return nextGen;//Return the grid
    }

    //Live method decides what will survive to the next generation
    public components [][] foxLive (components [] [] nextGen, int r, int c, int peoplePopulation, int foxPopulation, int waterAvailable, int gridWidth, int gridHeight, boolean ifFastBreed, int healthyBirthRate)//Accepts the nextGen grid, row, col, and population of people, foxes, and water
    {
        boolean changed = false;
        int energy = ((fox)grid[r][c]).getEnergy();

        ((fox)nextGen[r][c]).setLife(true);//Set life to true for now
        energy = ((fox) grid[r][c]).getEnergy() - 1;//Energy of fox is got, -1 for surviving to nextGen

        if (r - 1 >= 0) {//If there is a space above
            if (grid[r - 1][c].getType() == 5) {//If it is water, add 2 energy to the fox and changed is treu
                energy += 2;
                changed = true;
            }
            else if(grid[r-1][c].getType() == 3) {//If it is a tree, do the same
                energy += 2;
                changed = true;
            }
            else if(grid[r-1][c].getType() == 2)//If it is another fox
            {
                if (!(((fox)grid[r-1][c]).getGender().equals(((fox)grid[r][c]).getGender())) && waterAvailable > (peoplePopulation + foxPopulation) * 1.5)//check if opposite gender and water can support another life form
                {
                    double rand = Math.random();
                    double percent;

                    //makes all living elements have a higher chance of breeding regardless of population
                    if (ifFastBreed)
                        percent = healthyBirthRate;
                    else if(foxPopulation < (gridWidth*gridHeight * .035)) //If the population dropped below 3.5% of the board
                        percent = 0.5;//Chance of reproduction is 50%
                    else//Else it is 10%
                        percent = 0.1;

                    if(rand <= percent)//If the random number generated is less than the percent
                    {
                        rand = (Math.random() * 3) + 1;
                        changed = true;

                        if(rand <= 1 && r+1 < grid.length && grid[r+1][c].getType() == 0)//Check if there is a space below and put the fox there
                            nextGen[r+1][c] = new fox(2);
                        else if(rand <= 2  && c+1 < grid[r].length && grid[r][c+1].getType() == 0)//Check if there is a space to the right and put the fox there
                            nextGen[r][c+1] = new fox (2);
                        else if(rand <= 3 && c-1 >= 0 && grid[r][c-1].getType() == 0)//Check if there is a space to the left and put the fox there
                            nextGen[r][c-1] = new fox (2);
                    }
                }
            }
        }

        if (r + 1 < grid.length && !changed) {//Checks if there is a space below and if the fox was not already changed
            if (grid[r + 1][c].getType() == 5) {//If there is water add 2 energy and changed is true
                energy += 2;
                changed = true;
            }
            else if(grid[r+1][c].getType() == 3) {//If there is a tree add 2 energy and changed is true
                energy += 2;
                changed = true;
            }
            else if(grid[r+1][c].getType() == 2)//If it is another fox
            {
                if (!(((fox)grid[r+1][c]).getGender().equals(((fox)grid[r][c]).getGender())) && waterAvailable > (peoplePopulation + foxPopulation) * 1.5)//Check if the foxes are different gender and water can support a new fox
                {
                    double rand = Math.random();
                    double percent;

                    //makes all living elements have a higher chance of breeding regardless of population
                    if (ifFastBreed)
                        percent = healthyBirthRate;
                    else if(foxPopulation < (gridWidth*gridHeight * .035)) //If the population dropped below 100
                        percent = 0.5;//Chance of reproduction is 50%
                    else//Else it is 10%
                        percent = 0.1;

                    if(rand <= percent)//If the random number generated is less than/equal to the percent
                    {
                        rand = (Math.random() * 3) + 1;
                        changed = true;

                        if(rand <= 1 && r-1 >= 0 && grid[r-1][c].getType() == 0)//Check if there is a space above and put fox there
                            nextGen[r-1][c] = new fox(2);
                        else if(rand <= 2  && c+1 < grid[r].length && grid[r][c+1].getType() == 0)//Check if there is a space to the right and put fox there
                            nextGen[r][c+1] = new fox (2);
                        else if(rand <= 3 && c-1 >= 0 && grid[r][c-1].getType() == 0)//Check if there is a to the left above and put fox there
                            nextGen[r][c-1] = new fox (2);
                    }
                }
            }
        }

        if (c + 1 < grid[r].length && !changed) {//check if there is a space to the right and if it wasn't already changed
            if (grid[r][c + 1].getType() == 5) {//If it is water add 2 energy and changed is true
                energy += 2;
                changed = true;
            }
            else if(grid[r][c+1].getType() == 3) {//If it is a tree add 2 energy and changed is true
                energy += 2;
                changed = true;
            }
            else if(grid[r][c+1].getType() == 2)//If it is a fox
            {
                if (!(((fox)grid[r][c+1]).getGender().equals(((fox)grid[r][c]).getGender())) && waterAvailable > (peoplePopulation + foxPopulation) * 1.5)//Check if it is of opposite gender and water can support new life form
                {
                    double rand = Math.random();
                    double percent;

                    //makes all living elements have a higher chance of breeding regardless of population
                    if (ifFastBreed)
                        percent = healthyBirthRate;
                    else if(foxPopulation < (gridWidth*gridHeight * .035)) //If the population dropped below 100
                        percent = 0.5;//Chance of reproduction is 50%
                    else//Else it is 10%
                        percent = 0.1;

                    if(rand <= percent)//check if chance to reproduce
                    {
                        rand = (Math.random() * 3) + 1;
                        changed = true;

                        if(rand <= 1 && r+1 < grid.length && grid[r+1][c].getType() == 0)//If there is a space below and it is of type 0 put the fox there
                            nextGen[r+1][c] = new fox(2);
                        else if(rand <= 2  && r-1 >= 0 && grid[r-1][c].getType() == 0)//If there is a space above and it is of type 0 put the fox there
                            nextGen[r-1][c] = new fox (2);
                        else if(rand <= 3 && c-1 >= 0 && grid[r][c-1].getType() == 0)//If there is a space to the left and it is of type 0 put the fox there
                            nextGen[r][c-1] = new fox (2);
                    }
                }
            }
        }

        if (c - 1 >= 0 && !changed) {//If there is a space to the left and not already changed
            if (grid[r][c - 1].getType() == 5) {//If it is water add 2 energy and changed is true
                energy += 2;
            }
            else if(grid[r][c-1].getType() == 3) {//If it is a tree add 2 energy and changed is true
                energy += 2;
            }
            else if(grid[r][c-1].getType() == 2)//If it is another fox
            {
                if (!(((fox)grid[r][c-1]).getGender().equals(((fox)grid[r][c]).getGender())) && waterAvailable > (peoplePopulation + foxPopulation) * 1.5)//Check if reproduction is possible
                {
                    double rand = Math.random();
                    double percent;

                    //makes all living elements have a higher chance of breeding regardless of population
                    if (ifFastBreed)
                        percent = healthyBirthRate;
                    else if(foxPopulation < (gridWidth*gridHeight * .035)) //If the population dropped below 100
                        percent = 0.5;//Chance of reproduction is 50%
                    else//Else it is 10%
                        percent = 0.1;

                    if(rand <= percent)//If reproduce
                    {
                        rand = (Math.random() * 3) + 1;

                        if(rand <= 1 && r+1 < grid.length && grid[r+1][c].getType() == 0)//If there is a space below that is of type 0 put the fox there
                            nextGen[r+1][c] = new fox(2);
                        else if(rand <= 2  && r-1 >= 0 && grid[r-1][c].getType() == 0)//If there is a space above that is of type 0 put the fox there
                            nextGen[r-1][c] = new fox (2);
                        else if(rand <= 3 && c+1 < grid[r].length && grid[r][c+1].getType() == 0)//If there is a to the right below that is of type 0 put the fox there
                            nextGen[r][c+1] = new fox (2);
                    }
                }
            }
        }

        if (c - 2 >= 0 && !changed && grid[r][c-2].getType() == 3) {//If was never changed check if there is type 3 (tree) 2 units left, if so add energy and changed is true
            energy += 2;
            changed = true;
        }

        if (c - 1 >= 0 && r+1 < grid.length && !changed && grid[r+1][c-1].getType() == 3) {//If there is a tree below and to the left add 2 energy and changed is true
            energy += 2;
            changed = true;
        }

        if (c - 1 >= 0 && r-1 >= 0 && !changed && grid[r-1][c-1].getType() == 3) {//If there is a tree above and to the left add 2 energy
            energy += 2;
        }

        if (energy > 100)//If energy is greater than 100 it is 100
            energy = 100;

        if (((fox) grid[r][c]).getAlive()) {//If the fox is alive set the energy
            ((fox) grid[r][c]).setEnergy(energy);
        }

        if(energy < 1)//If the fox doesn't have enough energy it dies
            ((fox)nextGen[r][c]).setLife(false);

        return nextGen;
    }

    //Determines if a tree will live ro die to the next generation
    public boolean treeProperties (int r, int c)
    {
        int food = ((tree)grid[r][c]).getFood();//The food that the tree currently has is obtained

        if (r - 1 >= 0) {//If there is a row above
            if (grid[r - 1][c].getType() == 1) {//If it is a person
                if (((person) grid[r - 1][c]).getEnergy() == 99 || ((person) grid[r - 1][c]).getEnergy() == 100)//if the person has 99 or 100 energy
                    food -= 1;//Subtract one
                else//Else subtract two
                    food -= 2;
            }
            else if (grid[r - 1][c].getType() == 2) {//If it is a fox, test the same thing
                if (((fox) grid[r - 1][c]).getEnergy() == 99 || ((fox) grid[r - 1][c]).getEnergy() == 100)
                    food -= 1;
                else
                    food -= 2;
            }
        }

        if (r + 1 < grid.length) {//If there is a space below
            if (grid[r + 1][c].getType() == 1) {//if there is a person
                if (((person) grid[r + 1][c]).getEnergy() == 99 || ((person) grid[r + 1][c]).getEnergy() == 100)//If the person has 99/100 energy
                    food -= 1;//Subtract 1
                else//Else subtract 2
                    food -= 2;
            }
            if (grid[r + 1][c].getType() == 2) {//If it is a fox, test the same things
                if (((fox) grid[r + 1][c]).getEnergy() == 99 || ((fox) grid[r + 1][c]).getEnergy() == 100)
                    food -= 1;
                else
                    food -= 2;
            }
        }

        if (c + 1 < grid[r].length) {//If there is a column to the right
            if (grid[r][c + 1].getType() == 1) {//If it is a person
                if (((person) grid[r][c + 1]).getEnergy() == 99 || ((person) grid[r][c + 1]).getEnergy() == 100)//If the person has 99/100 energy
                    food -= 1;//subtract one
                else//Else subtract 2
                    food -= 2;
            }
            if (grid[r][c + 1].getType() == 2) {//If it is a fox test the same thing
                if (((fox) grid[r][c + 1]).getEnergy() == 99 || ((fox) grid[r][c + 1]).getEnergy() == 100)
                    food -= 1;
                else
                    food -= 2;
            }
        }

        if (c - 1 >= 0) {//If there is a space to the left
            if (grid[r][c - 1].getType() == 1) {//If it is a person
                if (((person) grid[r][c - 1]).getEnergy() == 99 || ((person) grid[r][c - 1]).getEnergy() == 100)//If it has 99/100 energy
                    food -= 1;//Subtract 1
                else//Else Subtract 2
                    food -= 2;
            }
            if (grid[r][c - 1].getType() == 2) {//Test the same things for a fox
                if (((fox) grid[r][c - 1]).getEnergy() == 99 || ((fox) grid[r][c - 1]).getEnergy() == 100)
                    food -= 1;
                else
                    food -= 2;
            }
        }

        ((tree)grid[r][c]).setFood(food);//Set the food calculated for the tree

        return food > 0;//Return if the tree will continue to live
    }

    //Determines the properties of a tree and water
    public boolean waterProperties (int r, int c)
    {
        int amountAround = 0;//Check the amount of water surrounding it, the water is only stable if it has atleast 3 other pieces of water surrounding it

        if (r - 1 >= 0 && grid[r - 1][c].getType() == 5)//Check each cell around and add
            amountAround++;
        if (r + 1 < grid.length && grid[r + 1][c].getType() == 5)
            amountAround++;
        if (c - 1 >= 0 && grid[r][c - 1].getType() == 5)
            amountAround++;
        if (c + 1 < grid[r].length && grid[r][c + 1].getType() == 5)
            amountAround++;
        if (r - 1 >= 0 && c - 1 >= 0 && grid[r - 1][c - 1].getType() == 5)
            amountAround++;
        if (r - 1 >= 0 && c + 1 < grid[r].length && grid[r - 1][c + 1].getType() == 5)
            amountAround++;
        if (r + 1 < grid.length && c - 1 >= 0 && grid[r + 1][c - 1].getType() == 5)
            amountAround++;
        if (r + 1 < grid.length && c + 1 < grid[r].length && grid[r + 1][c + 1].getType() == 5)
            amountAround++;

        if (((water) grid[r][c]).getActive() && amountAround >= 3) {//If the water is active and there are atleast 3 around it
            int water = ((water) grid[r][c]).getWater();//Get the amount of water in the cell

            if (r - 1 >= 0) {//If there is a space above
                if (grid[r - 1][c].getType() == 1) {//If it is a person
                    if (((person) grid[r - 1][c]).getEnergy() == 99 || ((person) grid[r - 1][c]).getEnergy() == 100)//If the person has 99/10 water
                        water -= 1;//only subtract one
                    else//Else subtract 2
                        water -= 2;
                }
                else if (grid[r - 1][c].getType() == 2) {//If it is a fox
                    if (((fox) grid[r - 1][c]).getEnergy() == 99 || ((fox) grid[r - 1][c]).getEnergy() == 100)//Do the same thing as a person
                        water -= 1;
                    else
                        water -= 2;
                }
            }

            if (r + 1 < grid.length) {//If there is an available space below
                if (grid[r + 1][c].getType() == 1) {//If it is a person
                    if (((person) grid[r + 1][c]).getEnergy() == 99 || ((person) grid[r + 1][c]).getEnergy() == 100)//If the person already has 99 or 100 energy
                        water -= 1;//Subtract one
                    else//Else subtract 2
                        water -= 2;
                }
                else if (grid[r + 1][c].getType() == 2) {//If it is a fox, do the same thing
                    if (((fox) grid[r + 1][c]).getEnergy() == 99 || ((fox) grid[r + 1][c]).getEnergy() == 100)
                        water -= 1;
                    else
                        water -= 2;
                }
            }

            if (c + 1 < grid[r].length) {//If there is a space to the right
                if (grid[r][c + 1].getType() == 1) {//If it is a person
                    if (((person) grid[r][c + 1]).getEnergy() == 99 || ((person) grid[r][c + 1]).getEnergy() == 100)//If the person has 99 or 100 energy
                        water -= 1;//Subtract 1
                    else//Else subtract 2
                        water -= 2;
                }
                else if (grid[r][c + 1].getType() == 2) {//If it is a fox, do the same thing
                    if (((fox) grid[r][c + 1]).getEnergy() == 99 || ((fox) grid[r][c + 1]).getEnergy() == 100)
                        water -= 1;
                    else
                        water -= 2;
                }
            }

            if (c - 1 >= 0) {//If there is a space to the right
                if (grid[r][c - 1].getType() == 1) {//If it is a person
                    if (((person) grid[r][c - 1]).getEnergy() == 99 || ((person) grid[r][c - 1]).getEnergy() == 100)//If the person has 99 or 100 energy
                        water -= 1;//Subtract 1
                    else//Else subtract 2
                        water -= 2;
                }
                if (grid[r][c - 1].getType() == 2) {//If it is a fox, do the same thing
                    if (((fox) grid[r][c - 1]).getEnergy() == 99 || ((fox) grid[r][c - 1]).getEnergy() == 100)
                        water -= 1;
                    else
                        water -= 2;
                }
            }

            ((water) grid[r][c]).setWater(water);//Set the water to the calculated water

            return water > 0;//Return if water is greater than 0
        }

        return false;//Return false (dead)
    }

    //This method chooses how a character will move
    public void move(components [] [] nextGen)
    {
        for (int r = 0 ; r < grid.length ; r++)
        {
            for (int c = 0 ; c < grid [0].length ; c++)
            {
                if(grid[r][c].getType() == 1)//If the character is a person...Important because you need to know how to cast it
                {
                    if(((person)grid[r][c]).getAlive())//If the person is alive
                    {
                        person temp = new person();//Create a temporary person
                        temp.setTo(((person)grid[r][c]));//Set the temporary person to the original
                        String move = ((person)grid[r][c]).getLastMoved();//Get the last moved of the person
                        boolean cantMove = false;//Set the cantMOve variable to false, if true, the person could not move
                        int count = ((person)grid[r][c]).getCount();//Get the count to determine which image will be used
                        ((person)nextGen[r][c]).setLife(false);//Set the original person's life to false

                        //Checks a series of if statements to determine which way a character will move
                        if(c+1 < grid[r].length && move.equals("Right") && grid[r][c+1].getType() == 0 && (!grid[r][c+1].getTaken()))//If there is a space to the right, the character last moved right, the space to the right contains nothing, and it is not already taken (another character is going there) proceed
                        {
                            nextGen[r][c+1] = new person();//Create a new person there in the nextGenGrid
                            ((person)nextGen[r][c+1]).setTo(temp);//Set it to the temp person
                            grid[r][c+1].setTaken(true);//Set the taken of the cell to true
                            count++;//Add one to the count

                            //IMPORTANT..THE COUNT DETERMINES THE IMAGE URL TO BE USED
                            //THIS ALLOWS THE IMAGE TO LOOK MORE REALISTIC AND AS IF IT IS MOVING
                            //THIS DOES NOT WORK AS WELL ON SOME COMPUTERS BECAUSE THE PROCESSOR CANT RUN THE CODE FAST ENOUGH, SO IT STILL LOOKS LIKE IT IS 'JUMPING'
                            //ON A GOOD COMPUTER IT WILL BE MORE REALISTIC
                            //CAN PROVIDE AN EXAMPLE TO MR. JAY
                            if(count == 0)//If the count is 0
                            {
                                if(((person)nextGen[r][c+1]).getGender().equals("Male"))//If the person is male
                                    ((person)nextGen[r][c+1]).setURL("src/BoyR_Run2.png");//Set the according image URl
                                else//Else set the according female image URL
                                    ((person)nextGen[r][c+1]).setURL("src/GirlR_Run2.png");
                            }
                            else if(count == 1)//Do the same thing for count 1, 2, and 3
                            {
                                if(((person)nextGen[r][c+1]).getGender().equals("Male"))
                                    ((person)nextGen[r][c+1]).setURL("src/BoyR_Run1.png");
                                else
                                    ((person)nextGen[r][c+1]).setURL("src/GirlR_Run1.png");
                            }
                            else if(count == 2)
                            {
                                if(((person)nextGen[r][c+1]).getGender().equals("Male"))
                                    ((person)nextGen[r][c+1]).setURL("src/BoyR_Run2.png");
                                else
                                    ((person)nextGen[r][c+1]).setURL("src/GirlR_Run2.png");
                            }
                            else
                            {
                                count = 0;

                                if(((person)nextGen[r][c+1]).getGender().equals("Male"))
                                    ((person)nextGen[r][c+1]).setURL("src/BoyR_Run3.png");
                                else
                                    ((person)nextGen[r][c+1]).setURL("src/GirlR_Run3.png");
                            }
                            ((person)nextGen[r][c+1]).setCount(count);//Set the count to what it was made
                        }
                        else if(r-1 >= 0 && move.equals("Up") && grid[r-1][c].getType() == 0 && (!grid[r-1][c].getTaken()))//If there is a space above, the last moved was up, the type of the above space is 0, and it is not already taken, proceed
                        {
                            nextGen[r-1][c] = new person();//Set the next gen at that spot to a person
                            ((person)nextGen[r-1][c]).setTo(temp);//Set it to the same characteristics as the temp
                            grid[r-1][c].setTaken(true);//Set that spot as taken
                            count++;//Add to the count

                            //Determine which image will be used based on the gender and count
                            if(count == 0)
                            {
                                if(((person)nextGen[r-1][c]).getGender().equals("Male"))
                                    ((person)nextGen[r-1][c]).setURL("src/BoyB_Run2.png");
                                else
                                    ((person)nextGen[r-1][c]).setURL("src/GirlB_Run2.png");
                            }
                            else if(count == 1)
                            {
                                if(((person)nextGen[r-1][c]).getGender().equals("Male"))
                                    ((person)nextGen[r-1][c]).setURL("src/BoyB_Run1.png");
                                else
                                    ((person)nextGen[r-1][c]).setURL("src/GirlB_Run1.png");
                            }
                            else if(count == 2)
                            {
                                if(((person)nextGen[r-1][c]).getGender().equals("Male"))
                                    ((person)nextGen[r-1][c]).setURL("src/BoyB_Run2.png");
                                else
                                    ((person)nextGen[r-1][c]).setURL("src/GirlB_Run2.png");
                            }
                            else
                            {
                                count = 0;

                                if(((person)nextGen[r-1][c]).getGender().equals("Male"))
                                    ((person)nextGen[r-1][c]).setURL("src/BoyB_Run3.png");
                                else
                                    ((person)nextGen[r-1][c]).setURL("src/GirlB_Run3.png");
                            }
                            ((person)nextGen[r-1][c]).setCount(count);//Set the count to what it was previously made to
                        }
                        else if(c-1 >= 0 && move.equals("Left") && grid[r][c-1].getType() == 0 && (!grid[r][c-1].getTaken()))//If there is a space ot the left, the lst moved was left, the type of cell to the left is 0, and it is not already taken proceed
                        {
                            nextGen[r][c-1] = new person();//Set the nextGen to a new person
                            ((person)nextGen[r][c-1]).setTo(temp);//Set it to the same characteristics as the temp
                            grid[r][c-1].setTaken(true);//Set the taken in that area to true
                            count++;//Add one to the count

                            //Determine the image URL to be used based off of the count and the gender
                            if(count == 0)
                            {
                                if(((person)nextGen[r][c-1]).getGender().equals("Male"))
                                    ((person)nextGen[r][c-1]).setURL("src/BoyL_Run2.png");
                                else
                                    ((person)nextGen[r][c-1]).setURL("src/GirlL_Run2.png");
                            }
                            else if(count == 1)
                            {
                                if(((person)nextGen[r][c-1]).getGender().equals("Male"))
                                    ((person)nextGen[r][c-1]).setURL("src/BoyL_Run1.png");
                                else
                                    ((person)nextGen[r][c-1]).setURL("src/GirlL_Run1.png");
                            }
                            else if(count == 2)
                            {
                                if(((person)nextGen[r][c-1]).getGender().equals("Male"))
                                    ((person)nextGen[r][c-1]).setURL("src/BoyL_Run2.png");
                                else
                                    ((person)nextGen[r][c-1]).setURL("src/GirlL_Run2.png");
                            }
                            else
                            {
                                count = 0;

                                if(((person)nextGen[r][c-1]).getGender().equals("Male"))
                                    ((person)nextGen[r][c-1]).setURL("src/BoyL_Run3.png");
                                else
                                    ((person)nextGen[r][c-1]).setURL("src/GirlL_Run3.png");
                            }
                            ((person)nextGen[r][c-1]).setCount(count);//Set the count to what was previously calculated
                        }
                        else if(r+1 < grid.length && move.equals("Down") && grid[r+1][c].getType() == 0 && (!grid[r+1][c].getTaken()))//Lastly if there is a space down, the character last moved down, the space is available, and not already taken proceed
                        {
                            nextGen[r+1][c] = new person();//Set the nextGen to a new person
                            ((person)nextGen[r+1][c]).setTo(temp);//Set the new person to the temp
                            grid[r+1][c].setTaken(true);//Set the taken as true
                            count++;//Add one to the count

                            //Based off of the count and gender choose which image to proceed with
                            if(count == 0)
                            {
                                if(((person)nextGen[r+1][c]).getGender().equals("Male"))
                                    ((person)nextGen[r+1][c]).setURL("src/BoyF_Run2.png");
                                else
                                    ((person)nextGen[r+1][c]).setURL("src/GirlF_Run2.png");
                            }
                            else if(count == 1)
                            {
                                if(((person)nextGen[r+1][c]).getGender().equals("Male"))
                                    ((person)nextGen[r+1][c]).setURL("src/BoyF_Run1.png");
                                else
                                    ((person)nextGen[r+1][c]).setURL("src/GirlF_Run1.png");
                            }
                            else if(count == 2)
                            {
                                if(((person)nextGen[r+1][c]).getGender().equals("Male"))
                                    ((person)nextGen[r+1][c]).setURL("src/BoyF_Run2.png");
                                else
                                    ((person)nextGen[r+1][c]).setURL("src/GirlF_Run2.png");
                            }
                            else
                            {
                                count = 0;

                                if(((person)nextGen[r+1][c]).getGender().equals("Male"))
                                    ((person)nextGen[r+1][c]).setURL("src/BoyF_Run3.png");
                                else
                                    ((person)nextGen[r+1][c]).setURL("src/GirlF_Run3.png");
                            }
                            ((person)nextGen[r+1][c]).setCount(count);//Set the count to what it was previously made
                        }
                        else//Else it cant move
                            cantMove = true;

                        if(cantMove)//If the person could not move in any direction
                        {
                            double rand = (Math.random() * (10)) + 1;//Make a rand variable to choose a random direction

                            if(rand <= 2.5 && r+1 < grid.length && grid[r+1][c].getType() == 0 && (!grid[r+1][c].getTaken()))//If the random variable is 2.5 or less, there is a space available below, it si of type 0, and not already taken
                            {
                                nextGen[r+1][c] = new person();//Create a new person there
                                ((person)nextGen[r+1][c]).setTo(temp);//Set it to the temp
                                ((person)nextGen[r+1][c]).setLastMoved("Down");//Set the last moved to down
                                grid[r+1][c].setTaken(true);//Set taken to true
                                ((person)nextGen[r+1][c]).setCount(0);//Set the count to zero

                                if(((person)nextGen[r+1][c]).getGender().equals("Male"))//If male use male URL
                                    ((person)nextGen[r+1][c]).setURL("src/BoyF_Run2.png");
                                else//Else use female URL
                                    ((person)nextGen[r+1][c]).setURL("src/GirlF_Run2.png");
                            }
                            //Do the same respective things for all other directions, if can move right left or up do and chosen randomly do so
                            else if(rand <= 5 && r-1 >= 0 && grid[r-1][c].getType() == 0 && (!grid[r-1][c].getTaken()))
                            {
                                nextGen[r-1][c] = new person();
                                ((person)nextGen[r-1][c]).setTo(temp);
                                ((person)nextGen[r-1][c]).setLastMoved("Up");
                                grid[r-1][c].setTaken(true);
                                ((person)nextGen[r-1][c]).setCount(0);

                                if(((person)nextGen[r-1][c]).getGender().equals("Male"))
                                    ((person)nextGen[r-1][c]).setURL("src/BoyB_Run2.png");
                                else
                                    ((person)nextGen[r-1][c]).setURL("src/GirlB_Run2.png");
                            }
                            else if(rand <= 7.5 && c+1 < grid[r].length && grid[r][c+1].getType() == 0 && (!grid[r][c+1].getTaken()))
                            {
                                nextGen[r][c+1] = new person();
                                ((person)nextGen[r][c+1]).setTo(temp);
                                ((person)nextGen[r][c+1]).setLastMoved("Right");
                                grid[r][c+1].setTaken(true);
                                ((person)nextGen[r][c+1]).setCount(0);

                                if(((person)nextGen[r][c+1]).getGender().equals("Male"))
                                    ((person)nextGen[r][c+1]).setURL("src/BoyR_Run2.png");
                                else
                                    ((person)nextGen[r][c+1]).setURL("src/GirlR_Run2.png");
                            }
                            else if(c-1>=0 && grid[r][c-1].getType() == 0 && (!grid[r][c-1].getTaken()))
                            {
                                nextGen[r][c-1] = new person();
                                ((person)nextGen[r][c-1]).setTo(temp);
                                ((person)nextGen[r][c-1]).setLastMoved("Left");
                                grid[r][c-1].setTaken(true);
                                ((person)nextGen[r][c-1]).setCount(0);

                                if(((person)nextGen[r][c-1]).getGender().equals("Male"))
                                    ((person)nextGen[r][c-1]).setURL("src/BoyL_Run2.png");
                                else
                                    ((person)nextGen[r][c-1]).setURL("src/GirlL_Run2.png");
                            }
                            else//Else could not move again so remains where it is
                            {
                                nextGen[r][c] = new person ();//New person in the same location
                                ((person)nextGen[r][c]).setTo(temp);//Set it to the temp
                                ((person)nextGen[r][c]).setLastMoved("");//Set last moved to nothing
                                grid[r][c].setTaken(true);//Set taken to true

                                if(((person)nextGen[r][c]).getGender().equals("Male"))//If male use male URL
                                    ((person)nextGen[r][c]).setURL("src/BoyR_Run2.png");
                                else//Else use female URL
                                    ((person)nextGen[r][c]).setURL("src/GirlR_Run2.png");
                            }
                        }
                    }
                }
                else if(grid[r][c].getType() == 2)//Do the exact same thing for fox, test the same variables, same comments
                {
                    if(((fox)grid[r][c]).getAlive())//If the fox is alive
                    {
                        fox temp = new fox();//Create a new temp fox
                        temp.setTo(((fox)grid[r][c]));//Set the temp to the original
                        String move = ((fox)grid[r][c]).getLastMoved();//Get the direction that fox last moved
                        boolean cantMove = false;//Set that it cant move to false
                        int count = ((fox)grid[r][c]).getCount();//Set the count to whatever it was before
                        ((fox)nextGen[r][c]).setLife(false);//Set the original to false

                        if(c+1 < grid[r].length && move.equals("Right") && grid[r][c+1].getType() == 0 && (!grid[r][c+1].getTaken()))//If there is a space to the right, the fox was moving right, it si of type 0, and not taken proceed
                        {
                            nextGen[r][c+1] = new fox();//Make a new fox in that spot
                            ((fox)nextGen[r][c+1]).setTo(temp);//Set it to the temp fox
                            grid[r][c+1].setTaken(true);//Set the taken of that spot to true
                            count++;//Add one to the count

                            //Based off of the count and gender a image URL will be set
                            if(count == 0)
                            {
                                if(((fox)nextGen[r][c+1]).getGender().equals("Male"))
                                    ((fox)nextGen[r][c+1]).setURL("src/BoyFoxR_Stand.png");
                                else
                                    ((fox)nextGen[r][c+1]).setURL("src/GirlFoxR_Stand.png");
                            }
                            else if(count == 1)
                            {
                                if(((fox)nextGen[r][c+1]).getGender().equals("Male"))
                                    ((fox)nextGen[r][c+1]).setURL("src/BoyFoxR_Run1.png");
                                else
                                    ((fox)nextGen[r][c+1]).setURL("src/GirlFoxR_Run1.png");
                            }
                            else if(count == 2)
                            {
                                if(((fox)nextGen[r][c+1]).getGender().equals("Male"))
                                    ((fox)nextGen[r][c+1]).setURL("src/BoyFoxR_Run2.png");
                                else
                                    ((fox)nextGen[r][c+1]).setURL("src/GirlFoxR_Run2.png");
                            }
                            else
                            {
                                count = 0;

                                if(((fox)nextGen[r][c+1]).getGender().equals("Male"))
                                    ((fox)nextGen[r][c+1]).setURL("src/BoyFoxR_Run3.png");
                                else
                                    ((fox)nextGen[r][c+1]).setURL("src/GirlFoxR_Run3.png");
                            }
                            ((fox)nextGen[r][c+1]).setCount(count);
                        }
                        else if(r-1 >= 0 && move.equals("Up") && grid[r-1][c].getType() == 0 && (!grid[r-1][c].getTaken()))//If there is a space above, the fox was moving up, it is of type 0 and not taken
                        {
                            nextGen[r-1][c] = new fox();//Make a new fox in this space
                            ((fox)nextGen[r-1][c]).setTo(temp);//Set it to the temp fox
                            grid[r-1][c].setTaken(true);//Set the taken of the spot to true
                            count++;//Add one to the count

                            //Based off of the count and gender an image URL will be set
                            if(count == 0)
                            {
                                if(((fox)nextGen[r-1][c]).getGender().equals("Male"))
                                    ((fox)nextGen[r-1][c]).setURL("src/BoyFoxB_Stand.png");
                                else
                                    ((fox)nextGen[r-1][c]).setURL("src/GirlFoxB_Stand.png");
                            }
                            else if(count == 1)
                            {
                                if(((fox)nextGen[r-1][c]).getGender().equals("Male"))
                                    ((fox)nextGen[r-1][c]).setURL("src/BoyFoxB_Run1.png");
                                else
                                    ((fox)nextGen[r-1][c]).setURL("src/GirlFoxB_Run1.png");
                            }
                            else if(count == 2)
                            {
                                if(((fox)nextGen[r-1][c]).getGender().equals("Male"))
                                    ((fox)nextGen[r-1][c]).setURL("src/BoyFoxB_Run2.png");
                                else
                                    ((fox)nextGen[r-1][c]).setURL("src/GirlFoxB_Run2.png");
                            }
                            else
                            {
                                count = 0;

                                if(((fox)nextGen[r-1][c]).getGender().equals("Male"))
                                    ((fox)nextGen[r-1][c]).setURL("src/BoyFoxB_Run3.png");
                                else
                                    ((fox)nextGen[r-1][c]).setURL("src/GirlFoxB_Run3.png");
                            }
                            ((fox)nextGen[r-1][c]).setCount(count);
                        }
                        else if(c-1 >= 0 && move.equals("Left") && grid[r][c-1].getType() == 0 && (!grid[r][c-1].getTaken()))//If there is a spot available to the left, the fox last moved left, it is of type 0, and it is taken
                        {
                            nextGen[r][c-1] = new fox();//Set a new fox to that spot
                            ((fox)nextGen[r][c-1]).setTo(temp);//Set that fox to have the same properties as the temp
                            grid[r][c-1].setTaken(true);//Set the taken of that spot to true
                            count++;//Add one to the count

                            //Based off of the count and the gender an image URL will be set
                            if(count == 0)
                            {
                                if(((fox)nextGen[r][c-1]).getGender().equals("Male"))
                                    ((fox)nextGen[r][c-1]).setURL("src/BoyFoxL_Stand.png");
                                else
                                    ((fox)nextGen[r][c-1]).setURL("src/GirlFoxL_Stand.png");
                            }
                            else if(count == 1)
                            {
                                if(((fox)nextGen[r][c-1]).getGender().equals("Male"))
                                    ((fox)nextGen[r][c-1]).setURL("src/BoyFoxL_Run1.png");
                                else
                                    ((fox)nextGen[r][c-1]).setURL("src/GirlFoxL_Run1.png");
                            }
                            else if(count == 2)
                            {
                                if(((fox)nextGen[r][c-1]).getGender().equals("Male"))
                                    ((fox)nextGen[r][c-1]).setURL("src/BoyFoxL_Run2.png");
                                else
                                    ((fox)nextGen[r][c-1]).setURL("src/GirlFoxL_Run2.png");
                            }
                            else
                            {
                                count = 0;

                                if(((fox)nextGen[r][c-1]).getGender().equals("Male"))
                                    ((fox)nextGen[r][c-1]).setURL("src/BoyFoxL_Run3.png");
                                else
                                    ((fox)nextGen[r][c-1]).setURL("src/GirlFoxL_Run3.png");
                            }
                            ((fox)nextGen[r][c-1]).setCount(count);
                        }
                        else if(r+1 < grid.length && move.equals("Down") && grid[r+1][c].getType() == 0 && (!grid[r+1][c].getTaken()))//Check if there is a spot available below, if the fox last moved was down, the spot is of type 0 and it is not already taken
                        {
                            nextGen[r+1][c] = new fox();//Set that spot to a new fox
                            ((fox)nextGen[r+1][c]).setTo(temp);//set that fox's properties to the temp variables
                            grid[r+1][c].setTaken(true);//Set he taken of that spot to true
                            count++;//Add one to the count

                            //Based off of the count and the gender of the fox an image URL will be set
                            if(count == 0)
                            {
                                if(((fox)nextGen[r+1][c]).getGender().equals("Male"))
                                    ((fox)nextGen[r+1][c]).setURL("src/BoyFoxF_Stand.png");
                                else
                                    ((fox)nextGen[r+1][c]).setURL("src/GirlFoxF_Stand.png");
                            }
                            else if(count == 1)
                            {
                                if(((fox)nextGen[r+1][c]).getGender().equals("Male"))
                                    ((fox)nextGen[r+1][c]).setURL("src/BoyFoxF_Run1.png");
                                else
                                    ((fox)nextGen[r+1][c]).setURL("src/GirlFoxF_Run1.png");
                            }
                            else if(count == 2)
                            {
                                if(((fox)nextGen[r+1][c]).getGender().equals("Male"))
                                    ((fox)nextGen[r+1][c]).setURL("src/BoyFoxF_Run2.png");
                                else
                                    ((fox)nextGen[r+1][c]).setURL("src/GirlFoxF_Run2.png");
                            }
                            else
                            {
                                count = 0;

                                if(((fox)nextGen[r+1][c]).getGender().equals("Male"))
                                    ((fox)nextGen[r+1][c]).setURL("src/BoyFoxF_Run3.png");
                                else
                                    ((fox)nextGen[r+1][c]).setURL("src/GirlFoxF_Run3.png");
                            }
                            ((fox)nextGen[r+1][c]).setCount(count);
                        }
                        else//The fox could not move so a new direction must be set
                            cantMove = true;

                        if(cantMove)//If it could not move
                        {
                            double rand = (Math.random() * (10)) + 1;//Used to choose a random direction to go, if it cant move it stays

                            if(rand <= 2.5 && r+1 < grid.length && grid[r+1][c].getType() == 0 && (!grid[r+1][c].getTaken()))//Checks if it can move down
                            {
                                nextGen[r+1][c] = new fox();//Makes a new fox in that spot
                                ((fox)nextGen[r+1][c]).setTo(temp);//Sets that fox to the same as the temp
                                ((fox)nextGen[r+1][c]).setLastMoved("Down");//Sets the last moved direction to down
                                grid[r+1][c].setTaken(true);//Sets the taken of that spot to true
                                ((fox)nextGen[r+1][c]).setCount(0);//Sets the count to 0

                                //Determines an URL to be used
                                if(((fox)nextGen[r+1][c]).getGender().equals("Male"))
                                    ((fox)nextGen[r+1][c]).setURL("src/BoyFoxF_Stand.png");
                                else
                                    ((fox)nextGen[r+1][c]).setURL("src/GirlFoxF_Stand.png");
                            }
                            else if(rand <= 5 && r-1 >= 0 && grid[r-1][c].getType() == 0 && (!grid[r-1][c].getTaken()))//Checks if the fox can move up
                            {
                                nextGen[r-1][c] = new fox();//Make a new fox in that spot
                                ((fox)nextGen[r-1][c]).setTo(temp);//Set it to the temp fox
                                ((fox)nextGen[r-1][c]).setLastMoved("Up");//Change the last moved to up
                                grid[r-1][c].setTaken(true);//Set that spot's taken to true
                                ((fox)nextGen[r-1][c]).setCount(0);//Set count to 0

                                //Determine the image URL
                                if(((fox)nextGen[r-1][c]).getGender().equals("Male"))
                                    ((fox)nextGen[r-1][c]).setURL("src/BoyFoxB_Stand.png");
                                else
                                    ((fox)nextGen[r-1][c]).setURL("src/GirlFoxB_Stand.png");
                            }
                            else if(rand <= 7.5 && c+1 < grid[r].length && grid[r][c+1].getType() == 0 && (!grid[r][c+1].getTaken()))//Check if the fox can move to the right
                            {
                                nextGen[r][c+1] = new fox();//Create a new fox in that area
                                ((fox)nextGen[r][c+1]).setTo(temp);//Set it to the same thing as the temp
                                ((fox)nextGen[r][c+1]).setLastMoved("Right");//Set the last moved to right
                                grid[r][c+1].setTaken(true);//Set the taken to true
                                ((fox)nextGen[r][c+1]).setCount(0);//Set the count to 0

                                //Determine the URL
                                if(((fox)nextGen[r][c+1]).getGender().equals("Male"))
                                    ((fox)nextGen[r][c+1]).setURL("src/BoyFoxR_Stand.png");
                                else
                                    ((fox)nextGen[r][c+1]).setURL("src/GirlFoxR_Stand.png");
                            }
                            else if(c-1>=0 && grid[r][c-1].getType() == 0 && (!grid[r][c-1].getTaken()))//Check if the fox can move left and do the same thing
                            {
                                nextGen[r][c-1] = new fox();
                                ((fox)nextGen[r][c-1]).setTo(temp);
                                ((fox)nextGen[r][c-1]).setLastMoved("Left");
                                grid[r][c-1].setTaken(true);
                                ((fox)nextGen[r][c-1]).setCount(0);

                                if(((fox)nextGen[r][c-1]).getGender().equals("Male"))
                                    ((fox)nextGen[r][c-1]).setURL("src/BoyFoxL_Stand.png");
                                else
                                    ((fox)nextGen[r][c-1]).setURL("src/GirlFoxL_Stand.png");
                            }
                            else//The fox could not move, so it stays
                            {
                                nextGen[r][c] = new fox();
                                ((fox)nextGen[r][c]).setTo(temp);
                                ((fox)nextGen[r][c]).setLastMoved("");
                                grid[r][c].setTaken(true);

                                if(((fox)nextGen[r][c]).getGender().equals("Male"))
                                    ((fox)nextGen[r][c]).setURL("src/BoyFoxR_Stand.png");
                                else
                                    ((fox)nextGen[r][c]).setURL("src/GirlFoxR_Stand.png");
                            }
                        }
                    }
                }
            } // This is second for-loop bracket
        }

        for (int r = 0 ; r < grid.length ; r++)//Sets all the properties that were made in this method to the grid
        {
            for (int c = 0 ; c < grid [0].length ; c++)
            {
                if(nextGen[r][c].getType() == 1)//Person in nextGen is now transferred to the grid, the same thing will happen for everything, so that in the next gen the creatures move
                {
                    grid[r][c] = new person();
                    ((person)grid[r][c]).setTo(((person)nextGen[r][c]));
                }
                else if(nextGen[r][c].getType() == 2)//Fox
                {
                    grid[r][c] = new fox();
                    ((fox)grid[r][c]).setTo(((fox)nextGen[r][c]));
                }
                else if(nextGen[r][c].getType() == 3)//tree
                {
                    //grid[r][c] = new tree();
                    if(grid[r][c].getType() == 3)
                        ((tree)grid[r][c]).setActive(true);
                    else {
                        grid[r][c] = new tree();
                        ((tree) grid[r][c]).setTo(((tree)nextGen[r][c]));//((water)grid[r][c]).setTo(((water)nextGen[r][c]));
                    }
                }
                else if(nextGen[r][c].getType() == 4)//rock
                {
                    grid[r][c] = new rock();
                    ((rock)grid[r][c]).setActive(true);
                }
                else if(nextGen[r][c].getType() == 5)//Water, set whatever you want, expand, contract, die out
                {
                    if(grid[r][c].getType() == 5)
                        ((water)grid[r][c]).setActive(true);
                    else {
                        grid[r][c] = new water();
                        ((water) grid[r][c]).setTo(((water)nextGen[r][c]));//((water)grid[r][c]).setTo(((water)nextGen[r][c]));
                    }
                }
                else if(nextGen[r][c].getType() == 0)
                {
                    grid[r][c] = new components ();
                }
            }
        }
    }

    //Rain method adds water when it gets low
    public components [] [] rain (components [][] nextGen)
    {
        for (int r = 0 ; r < grid.length ; r++) {
            for (int c = 0; c < grid[0].length; c++) {
                double rand = Math.random();//Random num

                if(nextGen[r][c].getType() == 0  && rand <= 0.5) {//If the location is type 0 and the rand is less than or equal to 0.5
                    nextGen[r][c] = new water(5);//Set it to water

                    //If the water overlaps an image it will get deleted
                    if (c - 1 >= 0 && nextGen[r][c - 1].getType() == 3)
                        ((water) nextGen[r][c]).setActive(false);
                    else if (c - 1 >= 0 && r - 1 >= 0 && nextGen[r - 1][c - 1].getType() == 3)
                        ((water) nextGen[r][c]).setActive(false);
                    else if (r - 1 >= 0 && nextGen[r - 1][c].getType() == 3)
                        ((water) nextGen[r][c]).setActive(false);
                }
            }
        }

        return nextGen;//Return the grid
    }

    //Adds trees when their population gets low
    public components [] [] grow (components [][] nextGen)
    {
        for (int r = 0 ; r < grid.length ; r++) {
            for (int c = 0; c < grid[0].length; c++) {
                double rand = Math.random();

                if(nextGen[r][c].getType() == 0  && rand <= 0.07) {//If the location is of type 0 there is a 10% chance a tree will grow
                    nextGen[r][c] = new tree(3);

                    //Deletes any trees that overlap an image
                    if (c + 1 < grid[r].length && nextGen[r][c+1].getType() != 0)
                        ((tree) nextGen[r][c]).setActive(false);
                    else if (c + 1 < grid[r].length && r + 1 < grid.length && nextGen[r+1][c+1].getType() != 0)
                        ((tree) nextGen[r][c]).setActive(false);
                    else if (r + 1 < grid.length && nextGen[r+1][c].getType() != 0)
                        ((tree) nextGen[r][c]).setActive(false);
                }
            }
        }

        return nextGen;//Returns the grid
    }

    //Is called to make the simulation move forward
    public void advance (int gridWidth, int gridHeight, boolean ifDrought, boolean ifFastBreed, int healthyBirthRate, boolean ifEarthquake)
    {
        components nextGen [] [] = new components [gridHeight] [gridWidth]; // create next generation of life forms
        int amountOfWater = 0, amountOfPeople = 0, amountOfFoxes = 0, amountofTrees = 0;//Set variables

        for (int row = 0 ; row < grid.length ; row++)//Resets the 'taken' values all to false, unless it is to the right of a type 3 (tree)
            for (int col = 0 ; col < grid [0].length ; col++)
            {
                if(grid[row][col].getType() == 1)
                    grid[row][col].setTaken(false);
                else if(grid[row][col].getType() == 2)
                    grid[row][col].setTaken(false);
                else if(grid[row][col].getType() == 3 && col+1 < grid[row].length)
                    grid[row][col+1].setTaken(true);
            }

        for (int row = 0 ; row < grid.length ; row++)//Creates the next gen grid to have the same components as the original grid, so where ever there is a person it is the same, a fox the same, a tree the same, etc
            for (int col = 0 ; col < grid [0].length ; col++)
            {
                if(grid[row][col].getType() == 1) {
                    nextGen[row][col] = new person();
                    amountOfPeople++;
                }
                else if(grid[row][col].getType() == 2) {
                    nextGen[row][col] = new fox();
                    amountOfFoxes++;
                }
                else if(grid[row][col].getType() == 3) {
                    nextGen[row][col] = new tree();
                    amountofTrees++;
                }
                else if(grid[row][col].getType() == 4) {
                    nextGen[row][col] = new rock();
                }
                else if(grid[row][col].getType() == 5) {
                    nextGen[row][col] = new water();
                    amountOfWater++;
                }
                else
                    nextGen[row][col] = new components();
            }

        for (int row = 0 ; row < grid.length ; row++)//Checks what will live to the next generation and saves the grid to that
            for (int col = 0 ; col < grid [0].length ; col++) {
                if(grid[row][col].getType() == 1)
                    nextGen = peopleLive(nextGen, row, col, amountOfPeople, amountOfFoxes, amountOfWater, gridWidth, gridHeight, ifFastBreed, healthyBirthRate);
                else if(grid[row][col].getType() == 2)
                    nextGen = foxLive(nextGen, row, col, amountOfPeople, amountOfFoxes, amountOfWater, gridWidth, gridHeight, ifFastBreed, healthyBirthRate);
                else if(grid[row][col].getType() == 3)
                    ((tree) nextGen[row][col]).setActive(treeProperties(row, col));
                else if(grid[row][col].getType() == 4)
                    ((rock)nextGen[row][col]).setActive(true);
                else if(grid[row][col].getType() == 5)
                    ((water) nextGen[row][col]).setActive(waterProperties(row, col));

            }

        for (int row = 0 ; row < grid.length ; row++)//Gives the original grid the same properties for life as the nextGen grid
            for (int col = 0 ; col < grid [0].length ; col++)
            {
                if(grid[row][col].getType() == 1)
                    ((person)grid[row][col]).setLife(((person)nextGen[row][col]).getAlive());
                else if(grid[row][col].getType() == 2)
                    ((fox)grid[row][col]).setLife(((fox)nextGen[row][col]).getAlive());
                else if (grid[row][col].getType() == 3)
                    ((tree)grid[row][col]).setActive(((tree)nextGen[row][col]).getActive());
                else if (grid[row][col].getType() == 4)
                    ((rock)grid[row][col]).setActive(((rock)nextGen[row][col]).getActive());
                else if(grid[row][col].getType() == 5)
                    ((water)grid[row][col]).setActive(((water)nextGen[row][col]).getActive());
            }

        if(ifEarthquake == false && amountofTrees < (gridWidth*gridHeight * .01))//If the amount of trees is less than 1% of the area, make more grow. Don't grow trees if earthquake just happened
            nextGen = grow(nextGen);
        if(ifEarthquake == false && ifDrought == false && amountOfWater < (gridWidth*gridHeight * .2))//If the amount of water is less than 20% of the area, make it rain. Also dont rain if an earthquake or drought happened
            nextGen = rain(nextGen);

        move(nextGen);//Move to the next generation
    }

    public components [][] getGrid()//If you want to access the grid you can get it
    {
        return grid;
    }
}

//Main components class that has multiple subclasses
class components
{
    protected int type;//Protected variables that are passed on to subclasses
    protected boolean taken;

    public components ()//Default constructor
    {
        taken = false;
        type = 0;
    }

    public components (int type)//Constructor that gets a type
    {
        taken = true;
        this.type = type;
    }

    public int getType ()
    {
        return type;
    }//Get type method

    public void setTaken(boolean ans)
    {
        taken = ans;
    }//Set taken method

    public boolean getTaken()
    {
        return taken;
    }//Get taken method
}

//Abstract class that extends components for each creature
abstract class creatures extends components
{
    protected boolean alive;//Protectd variables that are passed on to the subclass
    protected int energy, count;
    protected String lastMoved, URL, gender;

    public creatures(int x)//Constructor method that uses the parent method
    {
        super(x);
        alive = true;
        energy = 100;
        count = 0;
        lastMoved = "Right";
    }

    public creatures()//Default constructor, sets everything to 0, false, or blank
    {
        super();
        alive = false;
        energy = 0;
        count = 0;
        URL = "";
        lastMoved = "";
    }

    //Series of methods the subclasses will employ
    public abstract void setEnergy(int x);
    public abstract int getEnergy();
    public abstract void setCount(int x);
    public abstract int getCount();
    public abstract void setLife(boolean x);
    public abstract boolean getAlive();
    public abstract String getLastMoved();
    public abstract String getURL();
}

//Landscpaes abstract method that landscapes will use eg, water, trees, and rocks
abstract class landscapes extends components
{
    protected boolean active;

    //Constructor methods
    public landscapes (int x)
    {
        super(x);
        active = true;
    }

    //Default constructor method
    public landscapes()
    {
        super();
        active = false;
    }

    //Method that a sub class will employ
    public abstract void setActive (boolean x);
    public abstract boolean getActive ();
}

//Person class that has multiple getter and setter methods to set values
class person extends creatures
{
    public person(int x)//Constructor
    {
        super(x);
        double rand = Math.random();

        if(rand <= 0.5) {
            gender = "Male";
            URL = "src/BoyR_Run2.png";
        }
        else {
            gender = "Female";
            URL = "src/GirlR_Run2.png";
        }
    }

    public person()
    {
        super();
    }//Default constructor

    //A series of getter and setter methods
    public void setEnergy (int x)
    {
        energy = x;
    }

    public int getEnergy ()
    {
        return energy;
    }

    public void setCount(int x)
    {
        count = x;
    }

    public int getCount()
    {
        return count;
    }

    public void setLife(boolean isItAlive)
    {
        alive = isItAlive;

        if(!isItAlive)//If dead set values to 0
        {
            energy = 0;
            type = 0;
        }
        else
            type = 1;
    }

    public boolean getAlive()
    {
        return alive;
    }

    public void setURL(String x)
    {
        URL = x;
    }

    public String getURL ()
    {
        return URL;
    }

    public void setLastMoved(String move)
    {
        lastMoved = move;
    }

    public String getLastMoved()
    {
        return lastMoved;
    }

    public String getGender ()
    {
        return gender;
    }

    public void setTo (creatures setTo)//Sets an object to have the same properties
    {
        this.alive = setTo.alive;
        this.energy = setTo.energy;
        this.type = setTo.type;
        this.lastMoved = setTo.lastMoved;
        this.count = setTo.count;
        this.URL = setTo.URL;
        this.gender = setTo.gender;
    }
}

//Class fox is similar to class person, just for a different creature
class fox extends creatures
{
    public fox(int x)//Constructor
    {
        super(x);
        double rand = Math.random();

        if(rand <= 0.5) {
            gender = "Male";
            URL = "src/BoyFoxR_Stand.png";
        }
        else {
            gender = "Female";
            URL = "src/GirlFoxR_Stand.png";
        }

    }

    public fox()
    {
        super();
    }//Default constructor

    //Series of getter and setter methods
    public void setEnergy(int x)
    {
        energy = x;
    }

    public int getEnergy()
    {
        return energy;
    }

    public void setCount(int x)
    {
        count = x;
    }

    public int getCount()
    {
        return count;
    }

    public void setLife(boolean isItAlive)
    {
        alive = isItAlive;

        if(!isItAlive)
        {
            energy = 0;
            type = 0;
        }
        else
            type = 2;
    }

    public boolean getAlive()
    {
        return alive;
    }

    public void setURL(String x)
    {
        URL = x;
    }

    public String getURL ()
    {
        return URL;
    }

    public void setLastMoved(String move)
    {
        lastMoved = move;
    }

    public String getLastMoved()
    {
        return lastMoved;
    }

    public String getGender() {return gender;}

    public void setTo (creatures setTo)
    {
        this.alive = setTo.alive;
        this.energy = setTo.energy;
        this.type = setTo.type;
        this.lastMoved = setTo.lastMoved;
        this.count = setTo.count;
        this.URL = setTo.URL;
        this.gender = setTo.gender;
    }
}

//Class water that has getter and setter values
class water extends landscapes
{
    private int availableResource;//Amount of resource the cell has

    public water (int x)
    {
        super(x);
        availableResource = 50;
    }

    public water ()
    {
        super();
        availableResource = 0;
    }

    public void setWater (int x)
    {
        availableResource = x;
    }

    public int getWater ()
    {
        return availableResource;
    }

    public void setActive (boolean x)
    {
        active = x;

        if(!x)
        {
            availableResource = 0;
            type = 0;
        }
        else
            type = 5;
    }

    public boolean getActive ()
    {
        return active;
    }

    public void setTo (water setTo)
    {
        this.availableResource = setTo.availableResource;
        this.active = setTo.active;
        this.type = setTo.type;
    }
}

class rock extends landscapes
{
    private String URL;

    public rock (int x)
    {
        super(x);
        URL = "src/rock1.png";
    }

    public rock ()
    {
        super();
        URL = "";
    }

    public void setActive (boolean x)
    {
        active = x;

        if(!x)
        {
            type = 0;
        }
        else
            type = 4;
    }

    public boolean getActive ()
    {
        return active;
    }
}

class tree extends landscapes
{
    private int availableResource;
    private String URL;

    public tree (int x)
    {
        super(x);
        availableResource = 50;
        URL = "src/tree_withFood.png";
    }

    public tree ()
    {
        super();
        availableResource = 0;
        URL = "";
    }

    public void setFood (int x)
    {
        availableResource = x;
    }

    public int getFood ()
    {
        return availableResource;
    }

    public void setActive (boolean x)
    {
        active = x;

        if(!x)
        {
            availableResource = 0;
            type = 0;
        }
        else
            type = 3;
    }

    public boolean getActive ()
    {
        return active;
    }

    public void setTo (tree setTo)
    {
        this.availableResource = setTo.availableResource;
        this.active = setTo.active;
        this.type = setTo.type;
    }
}