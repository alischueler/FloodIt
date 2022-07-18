import java.util.ArrayList;
import java.util.Random;

import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;


// represents a single square of the game area

class Cell {
    static int CELL_SIZE = 20;
    // coords of the cell, origin is top left of the screen
    int x;
    int y;
    Color color;
    boolean flooded;

    // the four cells adjacent to this one
    Cell left;
    Cell top;
    Cell right;
    Cell bottom;

    // start game constructor
    Cell(int x, int y, Color color) {
        this(x, y, color, false, null, null, null, null);
    }

    // convenience constructor
    Cell(int x, int y, Color color, boolean flooded,
         Cell left, Cell top, Cell right, Cell bottom) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.flooded = flooded;
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    /* TMPLT
     * Fields
     *   Cell.CELLSIZE      static, 20
     *   this.x             int
     *   this.y             int
     *   this.color         Color
     *   this.flooded       boolean
     *   this.top           boolean
     *   this.left          boolean
     *   this.right         boolean
     *   this.bottom        boolean
     * Methods:
     *   this.renderCell(scene)      - void
     *   this.updateCell(Cell x4)    - void
     *   this.contains(int, int)     - boolean
     *   this.updateFlooded(Color)   - void
     *   this.push(ArrayList<Cell>)  - void
     *   this.addTo(ArrayList<Cell>) - void
     * Methods on Fields:
     *   this.[top, bottom, left, right]     - see above methods
     */
    // updates scene
    // EFFECT: draws this cell onto the given WorldScene
    void renderCell(WorldScene scene) {
        RectangleImage cell = new RectangleImage(CELL_SIZE, CELL_SIZE, "solid", this.color);
        scene.placeImageXY(cell, this.x, this.y);
    }

    //updates the links of this cell
    //EFFECT: updates links of this cell
    void updateCellLinks(Cell left, Cell top, Cell right, Cell bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    // does this cell contain the given x and y values?
    boolean contains(int x, int y) {
        return x > this.x - (CELL_SIZE / 2) && x <= this.x + (CELL_SIZE / 2)
                && y > this.y - (CELL_SIZE / 2) && y <= this.y + (CELL_SIZE / 2);
    }

    // EFFECT: updates the flooded value of this cell if
    // this cell's color matches the given and one of its neighbors is flooded
    void updateFlooded(Color colorClicked) {
        boolean neighborIsFlooded = false;
        if (!this.flooded) {
            if ((top != null && top.flooded) ||
                    (bottom != null && bottom.flooded) ||
                    (left != null && left.flooded) ||
                    (right != null && right.flooded)) {
                neighborIsFlooded = true;
            }
            if (neighborIsFlooded && this.color == colorClicked) {
                this.flooded = true;
            }
        }
    }

    // adds this cell to queue, as well as it's right and bottom neighbors
    //EFFECT: modifies the workList with this cell and its right and bottom neighbors
    void push(ArrayList<Cell> list) {
        boolean rightNull = (this.right == null);
        boolean bottomNull = (this.bottom == null);
        boolean leftNull = (this.left == null);
        boolean topNull = (this.top == null);

        this.addTo(list);
        if (!bottomNull) {
            this.bottom.addTo(list);
        }
        if (!rightNull) {
            this.right.addTo(list);
        }
        if (!topNull) {
            this.top.addTo(list);
        }
        if (!leftNull) {
            this.left.addTo(list);
        }

    }

    // adds this cell to workList if it is flooded
    //EFFECT: modifies the worklist by adding this cell if it is flooded
    void addTo(ArrayList<Cell> list) {
        if (this.flooded && !list.contains(this)) {
            list.add(this);
        }
    }
}

// flood it game
class FloodItWorld extends World {
    static int BOARD_SIZE = 22; // size of the board

    int width = FloodItWorld.BOARD_SIZE * Cell.CELL_SIZE;
    int height = ((FloodItWorld.BOARD_SIZE * Cell.CELL_SIZE) + 2 * Cell.CELL_SIZE);
    WorldScene scene = new WorldScene(width, height);

    ArrayList<Cell> board;      // all cells in board
    ArrayList<Color> colors;    // all possible colors
    ArrayList<Cell> workList; //keeps track of the cascading affect
    boolean startScreen;
    boolean initSetup;

    int numAllowedClicks;
    int numClicks;
    boolean lost;
    Color colorClicked;
    long startTime = System.currentTimeMillis();

    Random rand;

    // constructor for game
    FloodItWorld() {
        this.reset();
    }

    // Convenience constructor for tests
    FloodItWorld(ArrayList<Cell> board, ArrayList<Color> colors,
                 boolean startScreen, int numAllowedClicks, int numClicks, boolean lost, Color colorClicked,
                 Random rand, ArrayList<Cell> workList) {
        this.board = board;
        this.startScreen = startScreen;
        this.colors = colors;
        this.scene = new WorldScene(width, height);
        this.numAllowedClicks = numAllowedClicks;
        this.numClicks = numClicks;
        this.lost = lost;
        this.colorClicked = colorClicked;
        this.rand = rand;
        this.workList = workList;
    }

    /* TMPLT
     * Fields
     *   FloodItWorld.BOARD_SIZE      22
     *   this.width                   int
     *   this.height                  int
     *   this.scene                   WorldScene
     *   this.board                   ArrayList<Cell>
     *   this.colors                  ArrayList<Color>
     *   this.workList                ArrayList<Cell>
     *   this.startScreen             booleam
     *   this.initSetup               boolean
     *   this.numAllowedClicks        int
     *   this.numClicks               int
     *   this.lost                    boolean
     *   this.colorClicked            Color
     *   this.startTime               long
     *   this.rand                    Random
     * Methods:
     *   this.reset()             void
     *   this.reset(Random)       void
     *   this.updateLinks()       void
     *   this.renderStartScreen() void
     *   this.setupBoard()        void
     *   this.makeScene()         WorldScene
     *   this.update()            void
     *   this.onTick()            void
     *   this.onMouseClicked(Posn) void
     *   this.updateWorkList()     void
     *   this.updateFlooded()      void
     *   this.drawFlood()          void
     *   this.onKeyEvent(String)   void
     *   this.timer()              void
     *   this.counter()            void
     *   this.allFlooded()         boolean
     *
     * Methods on fIelds:
     *   see documentation for appplicable fields
     *
     */
    // resets the game
    //EFFECT: modifies the game to a new, random board
    public void reset() {
        this.board = new ArrayList<Cell>();

        this.colors = new ArrayList<Color>();
        colors.add(Color.BLUE);
        colors.add(Color.RED);
        colors.add(Color.GREEN);
        colors.add(Color.YELLOW);
        colors.add(Color.ORANGE);
        colors.add(Color.MAGENTA);

        this.workList = new ArrayList<Cell>();

        //base scene
        this.scene = new WorldScene(this.width, this.height);

        this.startScreen = true;
        this.numClicks = 0;
        this.lost = false;
        this.rand = new Random();

        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                Color color = colors.get(rand.nextInt(colors.size()));
                // offsets to middle of cell
                Cell temp = new Cell(j * Cell.CELL_SIZE + (Cell.CELL_SIZE / 2),
                        i * Cell.CELL_SIZE + (Cell.CELL_SIZE / 2), color, false, null, null, null, null);
                board.add(temp);
            }
        }


        this.colorClicked = this.board.get(0).color;
        this.numAllowedClicks = (2 * board.size() * this.colors.size()) / 150;
        this.updateLinks();
        this.board.get(0).flooded = true;   // always starts with first cell being flooded
        this.updateFlooded();
        this.workList.add(this.board.get(0));
    }

    // resets the game for TESTS
    //EFFECT: modifies the game to a new, random board given a seed
    public void reset(Random seed) {
        ArrayList<Cell> newBoard = new ArrayList<Cell>();

        this.colors = new ArrayList<Color>();
        colors.add(Color.BLUE);
        colors.add(Color.RED);
        colors.add(Color.GREEN);
        colors.add(Color.YELLOW);
        colors.add(Color.ORANGE);
        colors.add(Color.MAGENTA);

        this.workList = new ArrayList<Cell>();

        //base scene
        this.scene = new WorldScene(this.width, this.height);

        this.startScreen = true;
        this.numClicks = 0;
        this.lost = false;
        this.rand = seed;

        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                Color color = colors.get(rand.nextInt(colors.size()));
                // offsets to middle of cell
                Cell temp = new Cell(j * Cell.CELL_SIZE + (Cell.CELL_SIZE / 2),
                        i * Cell.CELL_SIZE + (Cell.CELL_SIZE / 2), color, false, null, null, null, null);
                newBoard.add(temp);
            }
        }

        board = newBoard;


        this.colorClicked = this.board.get(0).color;
        this.numAllowedClicks = (2 * board.size() * this.colors.size()) / 150;
        this.updateLinks();
        this.board.get(0).flooded = true;   // always starts with first cell being flooded
        this.updateFlooded();
        this.workList.add(this.board.get(0));
    }

    //updates the links of every cell in this board
    // EFFECT: modifies the left, right, top, and bottom of this cell's links
    public void updateLinks() {
        Cell left;
        Cell right;
        Cell top;
        Cell bottom;
        for (int i = 0; i < board.size(); i++) {
            // check bottom is not null
            if (i + BOARD_SIZE >= board.size()) {
                bottom = null;
            }
            else {
                bottom = board.get(i + BOARD_SIZE);
            }
            // check top is not null
            if (i - BOARD_SIZE < 0) {
                top = null;
            }
            else {
                top = board.get(i - BOARD_SIZE);
            }
            // check left is not null
            if (i % BOARD_SIZE == 0) {
                left = null;
            }
            else {
                left = board.get(i - 1);
            }
            // check right is not null
            if (i % BOARD_SIZE == BOARD_SIZE - 1) {
                right = null;
            }
            else {
                right = board.get(i + 1);
            }

            board.get(i).updateCellLinks(left, top, right, bottom);
        }
    }

    // initial screen
    //EFFECT: modifies the scene of this game to the start screen
    public void renderStartScreen() {
        TextImage start = new TextImage("FloodIt Game, Press Enter to Continue",  20, Color.RED);
        this.scene.placeImageXY(start, this.width / 2,
                (this.height / 2));
    }

    // initial board setup
    //EFFECT: modifies the scene of this game by drawing every cell in the board
    public void setupBoard() {
        for (int i = 0; i < board.size(); i++) {
            board.get(i).renderCell(scene);
        }

        this.initSetup = false;
    }

    // draws the scene of this game
    public WorldScene makeScene() {
        if (lost) {
            this.scene.placeImageXY(new RectangleImage(500, 30, "solid", Color.WHITE),
                    this.width / 2,
                    (this.height / 2) + Cell.CELL_SIZE);
            this.scene.placeImageXY(new TextImage("You Lose! Press r to play again", 20, Color.BLACK),
                    this.width / 2,
                    (this.height / 2) + Cell.CELL_SIZE);
        }
        return this.scene;
    }

    // updates gamestate
    //EFFECT: modifies the timer, counter and the cells that have to be drawn of this game
    public void update() {
        this.timer();
        this.counter();
        this.drawFlood();
    }

    // ticks through game
    // EFFECT: Modifies the game by changing the color of the board when prompted
    public void onTick() {
        if (startScreen) {
            this.renderStartScreen();
        }
        else if (initSetup) {
            this.setupBoard();
        }
        else {
            this.update();
        }
    }

    // on mouse click
    // EFFECT: Modifies the game by updating the board with the clicked Cell
    public void onMouseClicked(Posn mpos) {
        this.numClicks++;
        this.counter();
        for (Cell cell : this.board) {
            if (cell.contains(mpos.x, mpos.y)) {
                colorClicked = cell.color;
            }
        }
        this.updateFlooded();
        this.updateWorkList();
    }

    //changes the color of flooded cells to the color clicked
    //EFFECT: modifies the workList with every cell in the column and row
    public void updateWorkList() {
        ArrayList<Cell> copy = new ArrayList<Cell>();
        ArrayList<Cell> neighbors = new ArrayList<Cell>();
        board.get(0).push(copy);
        for (Cell b : board) {
            b.push(copy);
            for (Cell c : copy) {
                c.push(neighbors);
            }
        }
        for (Cell d : neighbors) {
            workList.add(d);
        }
    }


    //EFFECT: updates if the cell is flooded based on if it's color
    // matches the given color
    public void updateFlooded() {
        for (Cell cell : board) {
            cell.updateFlooded(colorClicked);
            if (cell.flooded) {
                cell.color = colorClicked;
            }
        }
    }

    //removes the first element in the WorkList
    //EFFECT: modifies the workList by removing the first element and drawing the updated scene
    public void drawFlood() {
        if (workList.size() > 0) {
            workList.remove(0).renderCell(scene);
        }
    }


    // on key click (r to reset)
    //EFFECT: modifies the game by restarting
    public void onKeyEvent(String key) {
        if (key.equals("enter") && this.startScreen) {
            this.startScreen = false;
            this.initSetup = true;
            this.startTime = System.currentTimeMillis();
        }
        else if ((key.equals("r") && !this.startScreen) || (key.equals("r") && !lost)) {
            this.reset();
        }
        else {
            return;
        }
    }

    // renders timer
    //EFFECT: modifies the timer of the board to the current time the player has been playing
    public void timer() {
        String timeEllapsed;
        TextImage timer;
        RectangleImage blank = new RectangleImage(200, 30, "solid", Color.WHITE);

        if (startScreen) {
            timeEllapsed =  "";
        }

        else {
            long playerTime = System.currentTimeMillis() - this.startTime;
            long playerTimeSeconds = playerTime / 1000;
            long playerTimeDisplay = playerTimeSeconds % 60;
            long playerTimeMinutes = playerTimeSeconds / 60;

            if (playerTimeDisplay < 10 && playerTimeMinutes < 10) {
                timeEllapsed = "Time Elapsed " + "0" + Long.toString(playerTimeMinutes) +
                        ":0" + Long.toString(playerTimeDisplay);
            }

            else if (playerTimeDisplay < 10 && playerTimeMinutes > 10) {
                timeEllapsed = "Time Elapsed " + Long.toString(playerTimeMinutes) +
                        ":0" + Long.toString(playerTimeDisplay);
            }

            else if (playerTimeDisplay > 10 && playerTimeMinutes < 10) {
                timeEllapsed = "Time Elapsed " + "0" + Long.toString(playerTimeMinutes) +
                        ":" + Long.toString(playerTimeDisplay);
            }

            else {
                timeEllapsed = "Time Elapsed " + Long.toString(playerTimeMinutes) +
                        ":" + Long.toString(playerTimeDisplay);
            }
        }

        timer = new TextImage(timeEllapsed, 15, Color.BLACK);
        scene.placeImageXY(blank, this.width - 9 * Cell.CELL_SIZE,
                this.height - Cell.CELL_SIZE);
        scene.placeImageXY(timer, this.width - 9 * Cell.CELL_SIZE,
                this.height - Cell.CELL_SIZE);
    }


    //the move counter
    //EFFECT: modifies the counter of the board to how many moves the player has used up
    public void counter() {
        String msg;
        TextImage counter;
        RectangleImage blank = new RectangleImage(200, 30, "solid", Color.WHITE);
        if (startScreen) {
            msg = "";
        }

        else {
            if (this.allFlooded() && numClicks <= this.numAllowedClicks) {
                msg = "You Win!";
            }

            else if (numClicks < this.numAllowedClicks) {
                msg = "Moves " + numClicks + "/" + this.numAllowedClicks;
            }

            else {
                msg = "You Lose!";
                this.lost = true;
            }
        }

        counter = new TextImage(msg, 15, Color.BLACK);
        scene.placeImageXY(blank, this.width - 19 * Cell.CELL_SIZE,
                this.height - Cell.CELL_SIZE);
        scene.placeImageXY(counter, this.width - 19 * Cell.CELL_SIZE,
                this.height - Cell.CELL_SIZE);

    }

    //determines if all of the cells in this ArrayList of Cells are flooded
    public boolean allFlooded() {
        boolean start = true;
        for (int i = 0; i < board.size(); i++) {
            start = start && board.get(i).flooded; //how do we combine each element in the list
        }
        return start;
    }
}

// examples
class ExamplesFlood {
    Cell c1;
    Cell c2;
    Cell c3;
    Cell c4;
    Cell c5;

    ArrayList<Cell> cells;
    ArrayList<Color> colors;
    ArrayList<Cell> cells2;

    ArrayList<Cell> workList;
    ArrayList<Cell> workList2;


    WorldScene scene;
    Random rand;

    FloodItWorld example;
    FloodItWorld example1;
    FloodItWorld example2;
    FloodItWorld example3;

    // initializes test cases
    void initCond() {
        c1 = new Cell(20, 20, Color.RED);
        c2 = new Cell(40, 20, Color.BLUE);
        c3 = new Cell(20, 40, Color.GREEN);
        c4 = new Cell(40, 40, Color.PINK);
        c5 = new Cell(15, 15, Color.CYAN);

        c1.bottom = c3;
        c1.right = c2;
        c2.left = c1;
        c2.bottom = c4;
        c3.top = c1;
        c3.right = c4;
        c4.left = c3;
        c4.top = c2;

        c1.flooded = true;

        scene = new WorldScene(440, 480);

        cells = new ArrayList<Cell>();
        cells2 = new ArrayList<Cell>();
        colors = new ArrayList<Color>();

        workList =  new ArrayList<Cell>();
        workList.add(c1);
        workList2 =  new ArrayList<Cell>();

        cells.add(c1);
        cells.add(c2);
        cells.add(c3);
        cells.add(c4);

        cells2.add(c5);

        colors.add(Color.BLUE);
        colors.add(Color.YELLOW);
        colors.add(Color.RED);
        colors.add(Color.BLACK);

        rand = new Random(0);

        example = new FloodItWorld();
        example1 = new FloodItWorld(cells, colors, true, 10, 0, false, Color.RED,
                rand, workList);
        example2 = new FloodItWorld(cells, colors, false, 10, 0, true, Color.BLACK,
                rand, workList);
        example3 = new FloodItWorld(cells2,
                colors,
                false,
                10,
                0,
                false,
                Color.BLUE,
                rand,
                workList2);
    }

    //big bang
    void testBigBang(Tester t) {
        FloodItWorld w = new FloodItWorld();
        int worldWidth = FloodItWorld.BOARD_SIZE * Cell.CELL_SIZE;
        int worldHeight = FloodItWorld.BOARD_SIZE * Cell.CELL_SIZE + 2 * Cell.CELL_SIZE;
        double tickRate = 0.05;
        w.bigBang(worldWidth, worldHeight, tickRate);
    }

    //tests updateLinks for board
    void testUpdateLinks(Tester t) {
        initCond();
        // updateLinks called in the constructor for example
        // top left corner
        t.checkExpect(example.board.get(0).bottom, example.board.get(22));
        t.checkExpect(example.board.get(0).top, null);
        t.checkExpect(example.board.get(0).left, null);
        t.checkExpect(example.board.get(0).right, example.board.get(1));
        t.checkExpect(example.board.get(0).x, 10);
        t.checkExpect(example.board.get(0).y, 10);

        // top right corner
        t.checkExpect(example.board.get(21).bottom, example.board.get(43));
        t.checkExpect(example.board.get(21).top, null);
        t.checkExpect(example.board.get(21).left, example.board.get(20));
        t.checkExpect(example.board.get(21).right, null);
        t.checkExpect(example.board.get(21).x, 430);
        t.checkExpect(example.board.get(21).y, 10);

        // bottom left corner
        t.checkExpect(example.board.get(462).bottom, null);
        t.checkExpect(example.board.get(462).top, example.board.get(440));
        t.checkExpect(example.board.get(462).left, null);
        t.checkExpect(example.board.get(462).right, example.board.get(463));
        t.checkExpect(example.board.get(462).x, 10);
        t.checkExpect(example.board.get(462).y, 430);

        // bottom right
        t.checkExpect(example.board.get(483).bottom, null);
        t.checkExpect(example.board.get(483).top, example.board.get(461));
        t.checkExpect(example.board.get(483).left, example.board.get(482));
        t.checkExpect(example.board.get(483).right, null);
        t.checkExpect(example.board.get(483).x, 430);
        t.checkExpect(example.board.get(483).y, 430);

        // middle
        t.checkExpect(example.board.get(121).bottom, example.board.get(143));
        t.checkExpect(example.board.get(121).top, example.board.get(99));
        t.checkExpect(example.board.get(121).left, example.board.get(120));
        t.checkExpect(example.board.get(121).right, example.board.get(122));
        t.checkExpect(example.board.get(121).x, 230);
        t.checkExpect(example.board.get(121).y, 110);
    }

    //tests updateCellLinks() for cells
    void testUpdateCellLinks(Tester t) {
        initCond();
        c5.updateCellLinks(c1, c2, c3, c4);
        t.checkExpect(c5.left, c1);
        t.checkExpect(c5.top, c2);
        t.checkExpect(c5.right, c3);
        t.checkExpect(c5.bottom, c4);
    }

    //tests renderStartScreen
    void testRenderStartScreen(Tester t) {
        this.initCond();
        WorldScene base = new WorldScene(440, 480);
        WorldScene base2 = new WorldScene(440, 480);

        base2.placeImageXY(new TextImage("FloodIt Game, Press Enter to Continue", 20,
                Color.RED), 220, 240);

        //has not gone through a tick yet so it only draws the base
        t.checkExpect(this.example1.startScreen, true);
        t.checkExpect(this.example1.scene, base);
        this.example1.renderStartScreen();
        t.checkExpect(this.example1.scene, base2);

        t.checkExpect(this.example3.startScreen, false);
        t.checkExpect(this.example3.scene, base);
        this.example3.renderStartScreen();
        t.checkExpect(this.example3.scene, base2);
    }

    //tests push
    void testPush(Tester t) {
        this.initCond();
        t.checkExpect(this.example1.workList.size(), 1);
        this.c1.push(this.example1.workList);
        t.checkExpect(this.example1.workList.size(), 1);

        this.c2.push(this.example1.workList);
        t.checkExpect(this.example1.workList.size(), 1);

        this.c3.push(this.example1.workList);
        t.checkExpect(this.example1.workList.size(), 1);

        this.c4.push(this.example1.workList);
        t.checkExpect(this.example1.workList.size(), 1);

    }

    //tests addTo
    void testAddTo(Tester t) {
        this.initCond();
        t.checkExpect(this.example1.workList.size(), 1);
        this.c1.addTo(this.example1.workList);
        t.checkExpect(this.example1.workList.size(), 1);

        this.c2.addTo(this.example1.workList);
        t.checkExpect(this.example1.workList.size(), 1);

        this.c3.addTo(this.example1.workList);
        t.checkExpect(this.example1.workList.size(), 1);

        this.c4.addTo(this.example1.workList);
        t.checkExpect(this.example1.workList.size(), 1);
    }

    //tests reset
    void testReset(Tester t) {
        this.initCond();
        Random rand1 = new Random(2);
        //this is only constructed with 4 colors and 4 tiles
        t.checkExpect(this.example1.board.size(), 4);
        t.checkExpect(this.example1.colors.size(), 4);
        t.checkExpect(this.example1.startScreen, true);
        t.checkExpect(this.example1.lost, false);

        //when it is reset it is a full board with 484 tiles and 6 colors
        this.example1.reset(rand1);
        t.checkExpect(this.example1.board.size(), 484);
        t.checkExpect(this.example1.colors.size(), 6);
        t.checkExpect(this.example1.startScreen, true);
        t.checkExpect(this.example1.lost, false);

        // check colors different
        t.checkExpect(example2.board.get(0).color, Color.RED);
        t.checkExpect(example2.board.get(1).color, Color.BLUE);
        t.checkExpect(example2.board.get(2).color, Color.GREEN);
        t.checkExpect(example2.board.get(3).color, Color.PINK);
        this.example2.reset(rand1);
        t.checkExpect(example2.board.get(0).color, Color.BLUE);
        t.checkExpect(example2.board.get(1).color, Color.YELLOW);
        t.checkExpect(example2.board.get(2).color, Color.MAGENTA);
        t.checkExpect(example2.board.get(3).color, Color.ORANGE);


        //this is the game board so it starts with 484 tiles and 6 possible colors
        t.checkExpect(this.example.board.size(), 484);
        t.checkExpect(this.example1.colors.size(), 6);
        //when it is reset it is a full board with 484 tiles and 6 colors
        this.example1.reset();
        t.checkExpect(this.example1.board.size(), 484);
        t.checkExpect(this.example1.colors.size(), 6);
    }

    //tests setUpBoard()
    void testSetUpBoard(Tester t) {
        this.initCond();
        WorldScene base = new WorldScene(440, 480);
        WorldScene base1 = new WorldScene(440, 480);

        base.placeImageXY(new RectangleImage(20, 20, OutlineMode.SOLID, Color.RED), 20, 20);
        base.placeImageXY(new RectangleImage(20, 20, OutlineMode.SOLID, Color.BLUE), 40, 20);
        base.placeImageXY(new RectangleImage(20, 20, OutlineMode.SOLID, Color.GREEN), 20, 40);
        base.placeImageXY(new RectangleImage(20, 20, OutlineMode.SOLID, Color.PINK), 40, 40);

        t.checkExpect(this.example1.scene, base1);
        this.example1.setupBoard();
        t.checkExpect(this.example1.scene, base);
    }

    //tests update
    void testUpdate(Tester t) {
        this.initCond();

        t.checkExpect(this.example1.workList.size(), 1);
        this.example1.update();
        t.checkExpect(this.example1.workList.size(), 0);

        t.checkExpect(this.example3.workList.size(), 0);
        this.example3.update();
        t.checkExpect(this.example3.workList.size(), 0);

        WorldScene base1 = new WorldScene(440, 480);

        WorldImage counter1 = new TextImage("", 15, Color.BLACK);

        WorldImage blank = new RectangleImage(200, 30, OutlineMode.SOLID, Color.WHITE);

        WorldImage timer2 = new TextImage("", 15, Color.BLACK);


        base1.placeImageXY(blank, 260, 460);
        base1.placeImageXY(counter1, 260, 460);
        base1.placeImageXY(blank, 60, 460);
        base1.placeImageXY(timer2, 60, 460);
        base1.placeImageXY(
                new RectangleImage(20, 20, OutlineMode.SOLID, Color.RED), 20, 20);


        t.checkExpect(this.example1.scene, base1);
    }

    //tests updateworkList
    void testUpdateWorkList(Tester t) {
        this.initCond();

        t.checkExpect(this.example1.workList.size(), 1);
        this.example1.updateWorkList();
        t.checkExpect(this.example1.workList.size(), 2);

        //does not change because nothing becomes flooded
        t.checkExpect(this.example3.workList.size(), 0);
        this.example3.updateWorkList();
        t.checkExpect(this.example3.workList.size(), 0);
        this.example3.updateWorkList();
        t.checkExpect(this.example3.workList.size(), 0);

    }

    //tests drawFlood
    void testDrawFlood(Tester t) {
        this.initCond();

        t.checkExpect(this.example1.workList.size(), 1);
        this.example1.drawFlood();
        t.checkExpect(this.example1.workList.size(), 0);

        t.checkExpect(this.example3.workList.size(), 0);
        this.example3.drawFlood();
        t.checkExpect(this.example3.workList.size(), 0);

    }

    //tests makeScene() (makescene itself does nothing)
    void testmakeScene(Tester t) {
        initCond();
        // expected scene
        WorldScene expectedScene = new WorldScene(440, 480);
        expectedScene.placeImageXY(new RectangleImage(500, 30, OutlineMode.SOLID, Color.WHITE),
                220, 260);
        expectedScene.placeImageXY(new TextImage("You Lose! Press r to play again", 20, Color.BLACK),
                220, 260);

        WorldScene expectedScene2 = new WorldScene(440, 480);
        WorldScene expectedScene3 = new WorldScene(440, 480);

        example1.makeScene();
        example2.makeScene();
        example3.makeScene();

        t.checkExpect(example1.scene, expectedScene3);
        t.checkExpect(example2.scene, expectedScene);
        t.checkExpect(example3.scene, expectedScene2);
    }

    //tests renderCell()
    void testRenderCell(Tester t) {
        initCond();
        // expected scene
        WorldScene expectedScene = new WorldScene(440, 480);
        expectedScene.placeImageXY(new RectangleImage(Cell.CELL_SIZE,
                        Cell.CELL_SIZE,
                        "solid",
                        Color.RED),
                20, 20);
        this.c1.renderCell(scene);
        t.checkExpect(scene, expectedScene);
        expectedScene.placeImageXY(new RectangleImage(Cell.CELL_SIZE,
                        Cell.CELL_SIZE,
                        "solid",
                        Color.BLUE),
                40, 20);
        this.c2.renderCell(scene);
        t.checkExpect(scene, expectedScene);
    }

    // tests contains()
    void testContains(Tester t) {
        initCond();
        t.checkExpect(c1.contains(20,  20), true); // middle
        t.checkExpect(c1.contains(10, 20), false); // x left bound exclude
        t.checkExpect(c1.contains(11, 20), true); // x left bound include
        t.checkExpect(c1.contains(30, 20), true); // x right bound
        t.checkExpect(c1.contains(20, 10), false); // y top bound include
        t.checkExpect(c1.contains(20, 11), true); // y left bound exclude
        t.checkExpect(c1.contains(20, 30), true); // y bottom bound
    }


    // tests updateFlooded
    void testUpdateFlooded(Tester t) {
        initCond();
        c2.updateFlooded(Color.BLACK);
        t.checkExpect(c2.flooded, false);
        c2.updateFlooded(Color.BLUE);
        t.checkExpect(c2.flooded, true);
        c3.updateFlooded(Color.DARK_GRAY);
        t.checkExpect(c3.flooded, false);
        c3.updateFlooded(Color.GREEN);
        t.checkExpect(c3.flooded, true);
        c4.updateFlooded(Color.PINK);
        t.checkExpect(c4.flooded, true);
    }

    // test flood
    void testFlood(Tester t) {
        initCond();
        example2.colorClicked = Color.RED;
        example2.updateFlooded();
        t.checkExpect(c1.flooded, true);
        t.checkExpect(c2.flooded, false);
        t.checkExpect(c3.flooded, false);
        t.checkExpect(c4.flooded, false);

        example2.colorClicked = Color.GREEN;
        example2.updateFlooded();
        t.checkExpect(c1.flooded, true);
        t.checkExpect(c2.flooded, false);
        t.checkExpect(c3.flooded, true);
        t.checkExpect(c4.flooded, false);

        example2.colorClicked = Color.BLUE;
        example2.updateFlooded();
        t.checkExpect(c1.flooded, true);
        t.checkExpect(c2.flooded, true);
        t.checkExpect(c3.flooded, true);
        t.checkExpect(c4.flooded, false);

        example2.colorClicked = Color.PINK;
        example2.updateFlooded();
        t.checkExpect(c1.flooded, true);
        t.checkExpect(c2.flooded, true);
        t.checkExpect(c3.flooded, true);
        t.checkExpect(c4.flooded, true);
    }


    // tests onTick  NOTE: empty because will be implemented for part 2
    void testOnTick(Tester t) {
        this.initCond();
        WorldScene base = new WorldScene(440, 480);
        t.checkExpect(this.example1.startScreen, true);
        t.checkExpect(this.example1.scene, base);
        t.checkExpect(this.example2.startScreen, false);
        t.checkExpect(this.example3.startScreen, false);
    }

    // tests onMouseClick
    void testOnMouseClick(Tester t) {
        initCond();
        ArrayList<Cell> expected = new ArrayList<Cell>();
        expected.add(this.c1);

        example2.onMouseClicked(new Posn(20, 20)); // clicked middle of C1
        t.checkExpect(example2.colorClicked, Color.RED);
        t.checkExpect(c1.flooded, true);
        t.checkExpect(c1.color, Color.RED);
        t.checkExpect(!c2.flooded && !c3.flooded && !c4.flooded, true);

        example2.onMouseClicked(new Posn(30, 20)); // right edge of C1/just out of bounds C2
        t.checkExpect(example2.colorClicked, Color.RED);
        t.checkExpect(c1.flooded, true);
        t.checkExpect(c1.color, Color.RED);
        t.checkExpect(!c2.flooded && !c3.flooded && !c4.flooded, true);

        example2.onMouseClicked(new Posn(50, 20)); // right edge of C2
        expected.add(this.c2);
        t.checkExpect(example2.colorClicked, Color.BLUE);
        t.checkExpect(c1.flooded && c2.flooded, true);
        t.checkExpect(c1.color, Color.BLUE);
        t.checkExpect(!c3.flooded && !c4.flooded, true);
        t.checkExpect(c2.color, Color.BLUE);

        example2.onMouseClicked(new Posn(40, 10));  // null ! just out of top range of C2
        t.checkExpect(example2.colorClicked, Color.BLUE);
        t.checkExpect(c1.flooded && c2.flooded, true);
        t.checkExpect(c1.color, Color.BLUE);
        t.checkExpect(!c3.flooded && !c4.flooded, true);
        t.checkExpect(c2.color, Color.BLUE);

        example2.onMouseClicked(new Posn(20, 30)); // bottom of C1
        t.checkExpect(example2.colorClicked, Color.BLUE);
        t.checkExpect(c1.flooded && c2.flooded, true);
        t.checkExpect(c1.color, Color.BLUE);
        t.checkExpect(!c3.flooded && !c4.flooded, true);
        t.checkExpect(c2.color, Color.BLUE);

        example2.onMouseClicked(new Posn(20, 30)); // just out of bounds of C3 top
        t.checkExpect(example2.colorClicked, Color.BLUE);
        t.checkExpect(c1.flooded && c2.flooded, true);
        t.checkExpect(c1.color, Color.BLUE);
        t.checkExpect(!c3.flooded && !c4.flooded, true);
        t.checkExpect(c2.color, Color.BLUE);

        example2.onMouseClicked(new Posn(20, 50)); // bottom of C3
        expected.add(this.c3);
        t.checkExpect(example2.colorClicked, Color.GREEN);
        t.checkExpect(c1.flooded && c2.flooded && c3.flooded, true);
        t.checkExpect(c1.color, Color.GREEN);
        t.checkExpect(!c4.flooded, true);
        t.checkExpect(c2.color, Color.GREEN);
        t.checkExpect(c3.color, Color.GREEN);

        example2.onMouseClicked(new Posn(50, 40)); // right of C4
        expected.add(this.c4);
        t.checkExpect(example2.colorClicked, Color.PINK);
        t.checkExpect(c1.flooded && c2.flooded && c3.flooded && c4.flooded, true);
        t.checkExpect(c1.color, Color.PINK);
        t.checkExpect(c2.color, Color.PINK);
        t.checkExpect(c3.color, Color.PINK);
        t.checkExpect(c4.color, Color.PINK);

        example2.onMouseClicked(new Posn(40, 50)); // bottom of C4
        t.checkExpect(example2.colorClicked, Color.PINK);

        example2.onMouseClicked(new Posn(30, 40)); // left of C4, just out of bound
        t.checkExpect(example2.colorClicked, Color.PINK);
        t.checkExpect(c1.flooded && c2.flooded && c3.flooded && c4.flooded, true);
        t.checkExpect(c1.color, Color.PINK);
        t.checkExpect(c2.color, Color.PINK);
        t.checkExpect(c3.color, Color.PINK);
        t.checkExpect(c4.color, Color.PINK);

        example2.onMouseClicked(new Posn(40, 30)); // top of C4, just out of bound
        t.checkExpect(example2.colorClicked, Color.PINK);
        t.checkExpect(c1.flooded && c2.flooded && c3.flooded && c4.flooded, true);
        t.checkExpect(c1.color, Color.PINK);
        t.checkExpect(c2.color, Color.PINK);
        t.checkExpect(c3.color, Color.PINK);
        t.checkExpect(c4.color, Color.PINK);
    }

    // tests onKeyEvent   NOTE: empty because will be implemented for part 2
    void testOnKeyEvent(Tester t) {
        this.initCond();
        t.checkExpect(this.example1.startScreen, true); //starts with startscreen
        t.checkExpect(this.example2.startScreen, false); //does not start with startscreen

        this.example1.onKeyEvent("l"); //not a valid key
        t.checkExpect(this.example1.startScreen, true);

        this.example1.onKeyEvent("r"); //resets to startscreen
        t.checkExpect(this.example1.startScreen, true);

        this.example1.onKeyEvent("enter"); //starts game
        t.checkExpect(this.example1.startScreen, false);

        this.example1.onKeyEvent("r"); //resets to startscreen
        t.checkExpect(this.example1.startScreen, true);

        this.example2.onKeyEvent("m"); //not a valid key stroke
        t.checkExpect(this.example2.startScreen, false);

        this.example2.onKeyEvent("r"); //resets to startscreen
        t.checkExpect(this.example2.startScreen, true);

        this.example2.onKeyEvent("l"); //not a valid key stroke
        t.checkExpect(this.example2.startScreen, true);

        this.example2.onKeyEvent("enter"); //starts game
        t.checkExpect(this.example2.startScreen, false);
    }

    //tests timer
    void testTimer(Tester t) {
        this.initCond();
        WorldScene base1 = new WorldScene(440, 480);
        WorldScene base2 = new WorldScene(440, 480);
        WorldScene base3 = new WorldScene(440, 480);

        WorldImage timer1 = new TextImage("Time Elapsed 00:00", 15, Color.BLACK);
        WorldImage timer2 = new TextImage("", 15, Color.BLACK);

        WorldImage blank = new RectangleImage(200, 30, OutlineMode.SOLID, Color.WHITE);

        base1.placeImageXY(blank, FloodItWorld.BOARD_SIZE * Cell.CELL_SIZE - 9 * Cell.CELL_SIZE,
                FloodItWorld.BOARD_SIZE * Cell.CELL_SIZE + Cell.CELL_SIZE);
        base1.placeImageXY(timer1, (FloodItWorld.BOARD_SIZE * Cell.CELL_SIZE) - 9 * Cell.CELL_SIZE,
                (FloodItWorld.BOARD_SIZE * Cell.CELL_SIZE) + Cell.CELL_SIZE);

        base2.placeImageXY(blank, FloodItWorld.BOARD_SIZE * Cell.CELL_SIZE - 9 * Cell.CELL_SIZE,
                FloodItWorld.BOARD_SIZE * Cell.CELL_SIZE + Cell.CELL_SIZE);
        base2.placeImageXY(timer2, (FloodItWorld.BOARD_SIZE * Cell.CELL_SIZE) - 9 * Cell.CELL_SIZE,
                (FloodItWorld.BOARD_SIZE * Cell.CELL_SIZE) + Cell.CELL_SIZE);

        t.checkExpect(this.example1.scene, new WorldScene(440, 480));

        t.checkExpect(this.example2.scene, new WorldScene(440, 480));

        this.example1.onKeyEvent("enter");
        this.example1.timer();
        t.checkExpect(this.example1.scene, base1);

        this.example1.onKeyEvent("r");
        t.checkExpect(this.example1.scene, base3);

        this.example2.onKeyEvent("r");
        t.checkExpect(this.example2.scene, base3);
    }

    //tests counter
    void testCounter(Tester t) {
        this.initCond();
        WorldScene base1 = new WorldScene(440, 480);
        WorldScene base2 = new WorldScene(440, 480);

        WorldImage counter1 = new TextImage("", 15, Color.BLACK);
        WorldImage counter2 = new TextImage("Moves 0/10", 15, Color.BLACK);

        WorldImage blank = new RectangleImage(200, 30, OutlineMode.SOLID, Color.WHITE);

        base1.placeImageXY(blank, (FloodItWorld.BOARD_SIZE * Cell.CELL_SIZE) - 19 * Cell.CELL_SIZE,
                (FloodItWorld.BOARD_SIZE * Cell.CELL_SIZE) + Cell.CELL_SIZE);
        base1.placeImageXY(counter1,
                (FloodItWorld.BOARD_SIZE * Cell.CELL_SIZE) - 19 * Cell.CELL_SIZE,
                (FloodItWorld.BOARD_SIZE * Cell.CELL_SIZE) + Cell.CELL_SIZE);

        base2.placeImageXY(blank,
                (FloodItWorld.BOARD_SIZE * Cell.CELL_SIZE) - 19 * Cell.CELL_SIZE,
                (FloodItWorld.BOARD_SIZE * Cell.CELL_SIZE) + Cell.CELL_SIZE);
        base2.placeImageXY(counter2,
                (FloodItWorld.BOARD_SIZE * Cell.CELL_SIZE) - 19 * Cell.CELL_SIZE,
                (FloodItWorld.BOARD_SIZE * Cell.CELL_SIZE) + Cell.CELL_SIZE);

        t.checkExpect(this.example1.scene, new WorldScene(440, 480));

        this.example1.counter();
        t.checkExpect(this.example1.scene, base1);


        this.example1.onKeyEvent("enter");
        this.example1.counter();
        t.checkExpect(this.example1.scene, base2);

        this.example2.counter();
        t.checkExpect(this.example2.scene, base2);
    }

    // tests all flooded
    void testAllFlooded(Tester t) {
        this.initCond();
        t.checkExpect(example2.allFlooded(), false);
        example2.board.get(0).flooded = true;
        t.checkExpect(example2.allFlooded(), false);
        example2.board.get(1).flooded = true;
        t.checkExpect(example2.allFlooded(), false);
        example2.board.get(2).flooded = true;
        t.checkExpect(example2.allFlooded(), false);
        example2.board.get(3).flooded = true;
    }
}