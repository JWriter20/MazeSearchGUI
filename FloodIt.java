import tester.Tester;
import java.util.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;
// Represents a single square of the game area
class Cell {
  // In logical coordinates, with the origin at the top-left corner of the screen
  int x;
  int y;
  Color color;
  boolean flooded;
  // the four adjacent cells to this one
  Cell left;
  Cell top;
  Cell right;
  Cell bottom;

  Cell(int x, int y, Color color, boolean flooded) {
    this.x = x;
    this.y = y;
    this.color = color;
    this.flooded = flooded;
  }

  // finds all cells that should be flooded and makes them true
  public void adjustFlooded(Color c, int size) {
    if (this.color.equals(c) && !this.flooded) {
      this.flooded = true;
      if (this.x != 0) {
        this.left.adjustFlooded(c, size);
      }

      if (this.x != size - 1) {
        this.right.adjustFlooded(c, size);
      }

      if (this.y != 0) {
        this.top.adjustFlooded(c, size);
      }

      if (this.y != size - 1) {
        this.bottom.adjustFlooded(c, size);
      }

    }

  }
}

// represents the world Flood
class FloodIt extends World {
  int size;
  double time = 0.0;
  int numColors;
  ArrayList<Color> colors;
  ArrayList<ArrayList<Cell>> cells;
  Random rand = new Random();
  int numMoves;
  int waterfall = 1;
  int waterfall2 = 1;

  FloodIt(int size, int numColors) {
    this.size = size;
    this.numColors = numColors;
    this.colors = getColors(numColors);
    this.cells = connectCells(makeBoard(size));
    this.numMoves = calcMoves(size, numColors);
  }

  // random seed constructor
  FloodIt(int size, int numColors, Random rand) {
    this.rand = rand;
    this.size = size;
    this.numColors = numColors;
    this.colors = getColors(numColors);
    this.cells = connectCells(makeBoard(size));
    this.numMoves = calcMoves(size, numColors);
  }

  // creates a board of given size
  ArrayList<ArrayList<Cell>> makeBoard(int size) {
    ArrayList<ArrayList<Cell>> board = new ArrayList<ArrayList<Cell>>(size);
    for (int i = 0; i < size; i++) {
      ArrayList<Cell> row = new ArrayList<Cell>(size);
      for (int j = 0; j < size; j++) {
        row.add(new Cell(j, i, this.randColor(), false));
      }
      board.add(row);
    }
    return connectCells(board);
  }

  // connects all the cells of the board
  ArrayList<ArrayList<Cell>> connectCells(ArrayList<ArrayList<Cell>> board) {
    for (ArrayList<Cell> row : board) {
      for (Cell c : row) {
        if (c.x != 0) {
          c.left = board.get(c.y).get(c.x - 1);
        }

        if (c.x != board.size() - 1) {
          c.right = board.get(c.y).get(c.x + 1);
        }

        if (c.y != 0) {
          c.top = board.get(c.y - 1).get(c.x);
        }

        if (c.y != board.size() - 1) {
          c.bottom = board.get(c.y + 1).get(c.x);
        }
        if (c.x == 0 && c.y == 0) {
          c.flooded = true;
        }
      }
    }
    return board;
  }

  // draws the game
  public OverlayOffsetAlign drawWorld() {
    int squareSize = 20;
    AboveImage cols = new AboveImage(new EmptyImage());
    for (int i = 0; i < this.size; i++) {
      BesideImage row = new BesideImage(new EmptyImage());
      for (int j = 0; j < this.size; j++) {
        row = new BesideImage(row,
            new RectangleImage(squareSize, squareSize, OutlineMode.SOLID,
                this.cells.get(i).get(j).color));
      }
      cols = new AboveImage(cols, row);
    }

    return new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.MIDDLE, cols, 0, 0,
        new EmptyImage());

  }

  // draws the game onto a scene
  // added features of time, displays number of Moves left and allows users to
  // increase difficulty
  public WorldScene makeScene() {
    if (this.didWin() && this.numMoves >= 0) {
      this.endOfWorld("Win!");
      return lastScene("Win!");
    }
    else if (this.numMoves <= 0) {
      this.endOfWorld("Lose!");
      return lastScene("Lose!");
    }
    else {
      WorldScene scene = new WorldScene(this.size * 150, this.size * 150);
      scene.placeImageXY(
          new RectangleImage(this.size * 300, this.size * 300, OutlineMode.SOLID, Color.cyan), 0,
          0);
      scene.placeImageXY(this.drawWorld(), this.size * 10, this.size * 10);
      scene.placeImageXY(
          new RectangleImage(this.size, this.size * 20, OutlineMode.SOLID, Color.black),
          this.size * 20, this.size * 10);
      scene.placeImageXY(
          new RectangleImage(this.size * 21, this.size, OutlineMode.SOLID, Color.black),
          this.size * 10, this.size * 20);

      scene.placeImageXY(new TextImage("Turns Left: " + this.numMoves, Color.black), this.size * 31,
          this.size * 5);
      scene.placeImageXY(new TextImage("Time Taken: " + (int) (this.time), Color.black),
          this.size * 33, this.size * 10);

      scene.placeImageXY(
          new OverlayImage(new TextImage("Increase Difficulty", Color.cyan),
              new RectangleImage(this.size * 40, this.size * 8, OutlineMode.SOLID, Color.black)),
          this.size * 22, this.size * 30);
      return scene;
    }
  }

  // ends the program when the game is over and calls the last draw state function
  public WorldScene lastScene(String msg) {
    WorldScene scene = new WorldScene(this.size * 150, this.size * 150);
    scene.placeImageXY(this.drawEndWorld(msg), this.size * 75, this.size * 75);
    return scene;
  }

  // draws the final end of the world
  public OverlayImage drawEndWorld(String msg) {
    if (msg.equals("Win!")) {
      return new OverlayImage(
          new TextImage("You Win!", 40, Color.blue),
          new RectangleImage(this.size * 150, this.size * 150, OutlineMode.SOLID, Color.YELLOW));

    }
    else {
      return new OverlayImage(
          new TextImage("You Lose!", 40, Color.red),
          new RectangleImage(this.size * 150, this.size * 150, OutlineMode.SOLID, Color.black));

    }
  }

  // creates the amount of colors desired for the game
  ArrayList<Color> getColors(int length) {
    ArrayList<Color> colors = new ArrayList<Color>(
        Arrays.asList(Color.BLUE, Color.green, Color.yellow, Color.red, Color.pink, Color.orange));
    int colorsSize = colors.size();
    for (int i = length; i < colorsSize; i++) {
      colors.remove(colors.size() - 1);
    }
    return colors;
  }

  // assigns a random color for each cell
  Color randColor() {
    int length = this.colors.size();
    Random rand = new Random();
    return colors.get(rand.nextInt(length));
  }

  // big-bang key handler that allows user to reset the game when hitting a
  // certain key
  public void onKeyEvent(String key) {
    if (key.equals("r")) {
      this.numMoves = calcMoves(this.size, this.numColors);
      this.cells = connectCells(makeBoard(this.size));
    }
  }

  // big-bang mouse handler that allows users to click on cells to flood the game
  public void onMouseClicked(Posn pos) {
    if (pos.x > this.size * 2 && pos.y > this.size * 27 && pos.x < this.size * 42
        && pos.y < this.size * 35) {
      this.size = size + 1;
      this.numMoves = calcMoves(this.size, this.numColors);
      this.cells = connectCells(makeBoard(this.size));
    }

    if (pos.x < this.size * 20 && pos.y < this.size * 20 && this.allDone()) {
      if (this.cells.get(pos.y / 20).get(pos.x / 20).color != this.cells.get(0).get(0).color) {
        this.numMoves = this.numMoves - 1;
      }

      this.cells.get(0).get(0).right.adjustFlooded(this.cells.get(0).get(0).color, this.size);
      this.cells.get(0).get(0).bottom.adjustFlooded(this.cells.get(0).get(0).color, this.size);
      Color changeColor = this.cells.get(pos.y / 20).get(pos.x / 20).color;
      for (ArrayList<Cell> row : this.cells) {
        for (Cell c : row) {
          if (c.flooded) {
            if (c.x != 0 && !c.left.flooded) {
              c.left.adjustFlooded(changeColor, this.size);
            }
            if (c.x != this.size - 1 && !c.right.flooded) {
              c.right.adjustFlooded(changeColor, this.size);
            }
            if (c.y != 0 && !c.top.flooded) {
              c.top.adjustFlooded(changeColor, this.size);
            }
            if (c.y != this.size - 1 && !c.bottom.flooded) {
              c.bottom.adjustFlooded(changeColor, this.size);
            }
          }
        }
      }
      cells.get(0).get(0).color = cells.get(pos.y / 20).get(pos.x / 20).color;
    }
  }

  // calculates the amount of turns a user should get based on size
  // formula isn't too accurate from our linear regression due to small amount of
  // data
  public int calcMoves(int size, int numColors) {
    int turns = (int) (1.5 * size + 3 * numColors - 15.78);
    if (turns < 4 && turns > 0) {
      return turns + 3;
    }
    else if (turns <= 0) {
      return 4;
    }
    else {
      return turns;
    }
  }

  // determines if the game is over if the whole board is flooded
  public boolean didWin() {

    for (int i = 0; i < this.size; i++) {
      for (int j = 0; j < this.size; j++) {
        if (!this.cells.get(i).get(j).flooded) {
          return false;
        }
      }
    }
    return true;
  }

  // does the waterfall animation for every flood for every tick of the game
  public void onTick() {
    this.time += 0.05;
    int a = this.waterfall2;
    int j = this.waterfall;
    if (j <= this.size - 1) {
      for (int i = 0; i < this.waterfall + 1; i++) {
        if (this.cells.get(i).get(j).flooded) {
          this.cells.get(i).get(j).color = this.cells.get(0).get(0).color;
        }
        j--;
      }
      this.waterfall++;
    }
    else {
      int x = this.size - 1;
      for (int i = a; i < this.size; i++) {
        if (this.cells.get(i).get(x).flooded) {
          this.cells.get(i).get(x).color = this.cells.get(0).get(0).color;
        }
        x--;
      }
      this.waterfall2++;
    }
    if (this.allDone()) {
      this.waterfall = 1;
      this.waterfall2 = 1;
    }
  }

  // determines if all cells that need to be flooded have been flooded
  public boolean allDone() {
    Color done = this.cells.get(0).get(0).color;
    for (int i = 0; i < this.size; i++) {
      for (int j = 0; j < this.size; j++) {
        if (this.cells.get(i).get(j).flooded && !(this.cells.get(i).get(j).color.equals(done))) {
          return false;
        }
      }
    }
    return true;
  }
}

// examples of the game
class ExamplesFlood {
  ExamplesFlood() {
  }

  Random rand1 = new Random(1);
  Random rand2 = new Random(2);
  Random rand3 = new Random(3);
  Random rand4 = new Random(4);
  Random rand10 = new Random(5);
  Random rand11 = new Random(7);
  Random rand12 = new Random(6);

  FloodIt flood = new FloodIt(5, 6);
  FloodIt flood1 = new FloodIt(2, 2, this.rand1);
  FloodIt flood2 = new FloodIt(3, 3, this.rand2);
  FloodIt flood3 = new FloodIt(4, 6, this.rand3);
  FloodIt flood4 = new FloodIt(6, 6, this.rand4);
  FloodIt flood10 = new FloodIt(5, 5, this.rand10);
  FloodIt flood11 = new FloodIt(2, 3, this.rand11);
  FloodIt flood12 = new FloodIt(3, 3, this.rand12);

  ArrayList<Color> colors1 = new ArrayList<Color>();
  ArrayList<Color> colors2 = new ArrayList<Color>();
  ArrayList<Color> colors3 = new ArrayList<Color>();
  ArrayList<Color> colors10 = new ArrayList<Color>();
  ArrayList<Color> colors11 = new ArrayList<Color>();
  ArrayList<Color> colors12 = new ArrayList<Color>();

  void initData() {

    for(int i =0; i < flood1.cells.size(); i++) {
      for(int j =0; j < flood1.cells.size(); j++) {
        flood1.cells.get(i).get(j).color = Color.blue;
        colors1.add(Color.blue);
      }
    }
    
    for(int i =0; i < flood11.cells.size(); i++) {
      for(int j =0; j < flood11.cells.size(); j++) {
        if ((i + j) % 2 == 0) {
          flood11.cells.get(i).get(j).color = Color.blue;
          colors11.add(Color.blue);
        }else {
          flood11.cells.get(i).get(j).color = Color.red;
          colors11.add(Color.red);
        }
      }
    }
    
    for(int i =0; i < flood12.cells.size(); i++) {
      for(int j =0; j < flood12.cells.size(); j++) {
        if ((i + j) % 2 == 0) {
          flood12.cells.get(i).get(j).color = Color.blue;
          colors12.add(Color.blue);
        }else {
          flood12.cells.get(i).get(j).color = Color.red;
          colors12.add(Color.red);
        }
      }
    }

    for(int i =0; i < flood2.cells.size(); i++) {
      for(int j =0; j < flood2.cells.size(); j++) {
        flood2.cells.get(i).get(j).color = Color.green;
        colors2.add(Color.green);
      }
    }

    for (ArrayList<Cell> row : flood3.cells) {
      for (Cell c : row) {
        c.color = Color.red;
        colors3.add(c.color);
      }
    }
    
    for(int i =0; i < flood10.cells.size(); i++) {
      for(int j =0; j < flood10.cells.size(); j++) {
        if ((i * j) % 6 == 0) {
          flood10.cells.get(i).get(j).color = Color.red;
          colors10.add(Color.red);
        }
        
        if ((i * j) % 6 == 1) {
          flood10.cells.get(i).get(j).color = Color.green;
          colors10.add(Color.green);
        }
        
        if ((i * j) % 6 == 2) {
          flood10.cells.get(i).get(j).color = Color.yellow;
          colors10.add(Color.yellow);
        }
        
        if ((i * j) % 6 == 3) {
          flood10.cells.get(i).get(j).color = Color.blue;
          colors10.add(Color.blue);
        }
        
        if ((i * j) % 6 == 4) {
          flood10.cells.get(i).get(j).color = Color.pink;
          colors10.add(Color.pink);
        }
        
        if ((i * j) % 6 == 5) {
          flood10.cells.get(i).get(j).color = Color.orange;
          colors10.add(Color.orange);
        }
      }
    }
  }

  // tests the getColors function
  boolean testgetColors(Tester t) {
    return t.checkExpect(flood1.getColors(2),
        new ArrayList<Color>(Arrays.asList(Color.blue, Color.green)))
        && t.checkExpect(flood2.getColors(3),
            new ArrayList<Color>(Arrays.asList(Color.blue, Color.green, Color.yellow)))
        && t.checkExpect(flood3.getColors(6), new ArrayList<Color>(Arrays.asList(Color.BLUE,
            Color.green, Color.yellow, Color.red, Color.pink, Color.orange)));
  }

  // tests the randColor function
  boolean testRandColor(Tester t) {
    return t.checkOneOf(flood1.randColor(), Color.green, Color.blue)
        && t.checkOneOf(flood2.randColor(), Color.green, Color.blue, Color.yellow)
        && t.checkOneOf(flood3.randColor(), Color.BLUE, Color.green, Color.yellow, Color.red,
            Color.pink, Color.orange);
  }

  public void testDrawWorld(Tester t) { 
   initData();
    
   t.checkExpect(flood11.drawWorld(),
       new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.MIDDLE,
           new AboveImage(
               new AboveImage(
                   new AboveImage(
                       new EmptyImage(),
                       new EmptyImage()),
                   new BesideImage(
                       new BesideImage(
                           new BesideImage(
                               new EmptyImage(),
                               new EmptyImage()),
                           new RectangleImage(20, 20, OutlineMode.SOLID, Color.blue)),
                       new RectangleImage(20, 20, OutlineMode.SOLID, Color.red))),
               new BesideImage(
                   new BesideImage(
                       new BesideImage(
                           new EmptyImage(),
                           new EmptyImage()),
                       new RectangleImage(20, 20, OutlineMode.SOLID, Color.red)),
                   new RectangleImage(20, 20, OutlineMode.SOLID, Color.blue))),
           0, 0, new EmptyImage()));
   
   t.checkExpect(flood12.drawWorld(),
       new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.MIDDLE,
       new AboveImage(
           new AboveImage(
            new AboveImage(
             new AboveImage(
              new EmptyImage(),
               new EmptyImage()),
               new BesideImage(
                new BesideImage(
                 new BesideImage(
                  new BesideImage(
                   new EmptyImage(),
                    new EmptyImage()),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.blue)),
                 new RectangleImage(20, 20, OutlineMode.SOLID, Color.red)),
                new RectangleImage(20, 20, OutlineMode.SOLID, Color.blue))),
            new BesideImage(
                new BesideImage(
                 new BesideImage(
                  new BesideImage(
                   new EmptyImage(),
                    new EmptyImage()),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.red)),
                 new RectangleImage(20, 20, OutlineMode.SOLID, Color.blue)),
                new RectangleImage(20, 20, OutlineMode.SOLID, Color.red))),
           new BesideImage(
               new BesideImage(
                new BesideImage(
                 new BesideImage(
                  new EmptyImage(),
                   new EmptyImage()),
                   new RectangleImage(20, 20, OutlineMode.SOLID, Color.blue)),
                new RectangleImage(20, 20, OutlineMode.SOLID, Color.red)),
               new RectangleImage(20, 20, OutlineMode.SOLID, Color.blue))),
            0, 0, new EmptyImage()));
       

    t.checkExpect(flood10.drawWorld(), 
        new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.MIDDLE,
            new AboveImage(
                new AboveImage(
                    new AboveImage(
                        new AboveImage(
                            new AboveImage(
                                new AboveImage(
                                    new EmptyImage(),
                                    new EmptyImage()),
                                new BesideImage(
                                    new BesideImage(
                                        new BesideImage(
                                            new BesideImage(
                                                new BesideImage(
                                                    new BesideImage(
                                                        new EmptyImage(),
                                                        new EmptyImage()),
                                                    new RectangleImage(20, 20, 
                                                        OutlineMode.SOLID,
                                                        Color.red)),
                                                new RectangleImage(20, 20, 
                                                    OutlineMode.SOLID,
                                                    Color.red)),
                                            new RectangleImage(20, 20, 
                                                OutlineMode.SOLID,
                                                Color.red)),
                                        new RectangleImage(20, 20, 
                                            OutlineMode.SOLID,
                                            Color.red)),
                                    new RectangleImage(20, 20, 
                                        OutlineMode.SOLID,
                                        Color.red))),
                            new BesideImage(
                                new BesideImage(
                                    new BesideImage(
                                        new BesideImage(
                                            new BesideImage(
                                                new BesideImage(
                                                    new EmptyImage(),
                                                    new EmptyImage()),
                                                new RectangleImage(20, 20, 
                                                    OutlineMode.SOLID,
                                                    Color.red)),
                                            new RectangleImage(20, 20, 
                                                OutlineMode.SOLID,
                                                Color.green)),
                                        new RectangleImage(20, 20, 
                                            OutlineMode.SOLID,
                                            Color.yellow)),
                                    new RectangleImage(20, 20, 
                                        OutlineMode.SOLID,
                                        Color.blue)),
                                new RectangleImage(20, 20, 
                                    OutlineMode.SOLID,
                                    Color.pink))),
                        new BesideImage(
                            new BesideImage(
                                new BesideImage(
                                    new BesideImage(
                                        new BesideImage(
                                            new BesideImage(
                                                new EmptyImage(),
                                                new EmptyImage()),
                                            new RectangleImage(20, 20, 
                                                OutlineMode.SOLID,
                                                Color.red)),
                                        new RectangleImage(20, 20, 
                                            OutlineMode.SOLID,
                                            Color.yellow)),
                                    new RectangleImage(20, 20, 
                                        OutlineMode.SOLID,
                                        Color.pink)),
                                new RectangleImage(20, 20, 
                                    OutlineMode.SOLID,
                                    Color.red)),
                            new RectangleImage(20, 20, 
                                OutlineMode.SOLID,
                                Color.yellow))),
                    new BesideImage(
                        new BesideImage(
                            new BesideImage(
                                new BesideImage(
                                    new BesideImage(
                                        new BesideImage(
                                            new EmptyImage(),
                                            new EmptyImage()),
                                        new RectangleImage(20, 20, 
                                            OutlineMode.SOLID,
                                            Color.red)),
                                    new RectangleImage(20, 20, 
                                        OutlineMode.SOLID,
                                        Color.blue)),
                                new RectangleImage(20, 20, 
                                    OutlineMode.SOLID,
                                    Color.red)),
                            new RectangleImage(20, 20, 
                                OutlineMode.SOLID,
                                Color.blue)),
                        new RectangleImage(20, 20, 
                            OutlineMode.SOLID,
                            Color.red))),
                new BesideImage(
                    new BesideImage(
                        new BesideImage(
                            new BesideImage(
                                new BesideImage(
                                    new BesideImage(
                                        new EmptyImage(),
                                        new EmptyImage()),
                                    new RectangleImage(20, 20, 
                                        OutlineMode.SOLID,
                                        Color.red)),
                                new RectangleImage(20, 20, 
                                    OutlineMode.SOLID,
                                    Color.pink)),
                            new RectangleImage(20, 20, 
                                OutlineMode.SOLID,
                                Color.yellow)),
                        new RectangleImage(20, 20, 
                            OutlineMode.SOLID,
                            Color.red)),
                    new RectangleImage(20, 20, 
                        OutlineMode.SOLID,
                        Color.pink))),
            0, 0, new EmptyImage()));                            
}
  

  // Just used to test and make sure the image looked alright
  void testBigBang(Tester t) {

    int worldWidth = this.flood4.size * 150;
    int worldHeight = this.flood4.size * 150;
    double tickRate = .05;
    this.flood10.bigBang(worldWidth, worldHeight, tickRate);
  }

  // tests the makeBoard and connectCells function
  boolean testMakeBoard(Tester t) {
    initData();
    Cell cell1 = new Cell(0, 0, Color.blue, true);
    Cell cell2 = new Cell(1, 0, Color.blue, false);
    Cell cell3 = new Cell(0, 1, Color.blue, false);
    Cell cell4 = new Cell(1, 1, Color.blue, false);

    cell1.bottom = cell3;
    cell1.right = cell2;
    cell2.bottom = cell4;
    cell2.left = cell1;
    cell3.top = cell1;
    cell3.right = cell4;
    cell4.top = cell2;
    cell4.left = cell3;

    ArrayList<ArrayList<Cell>> board1 = new ArrayList<ArrayList<Cell>>(
        Arrays.asList(new ArrayList<Cell>(Arrays.asList(cell1, cell2)),
            new ArrayList<Cell>(Arrays.asList(cell3, cell4))));

    Cell cell1_1 = new Cell(0, 0, Color.green, true);
    Cell cell1_2 = new Cell(1, 0, Color.green, false);
    Cell cell1_3 = new Cell(2, 0, Color.green, false);
    Cell cell1_4 = new Cell(0, 1, Color.green, false);
    Cell cell1_5 = new Cell(1, 1, Color.green, false);
    Cell cell1_6 = new Cell(2, 1, Color.green, false);
    Cell cell1_7 = new Cell(0, 2, Color.green, false);
    Cell cell1_8 = new Cell(1, 2, Color.green, false);
    Cell cell1_9 = new Cell(2, 2, Color.green, false);

    cell1_1.bottom = cell1_4;
    cell1_1.right = cell1_2;
    cell1_2.left = cell1_1;
    cell1_2.right = cell1_3;
    cell1_2.bottom = cell1_5;
    cell1_3.bottom = cell1_6;
    cell1_3.left = cell1_2;
    cell1_4.top = cell1_1;
    cell1_4.right = cell1_5;
    cell1_4.bottom = cell1_7;
    cell1_5.top = cell1_2;
    cell1_5.left = cell1_4;
    cell1_5.bottom = cell1_8;
    cell1_5.right = cell1_6;
    cell1_6.top = cell1_3;
    cell1_6.left = cell1_5;
    cell1_6.bottom = cell1_9;
    cell1_7.top = cell1_4;
    cell1_7.right = cell1_8;
    cell1_8.top = cell1_5;
    cell1_8.left = cell1_7;
    cell1_8.right = cell1_9;
    cell1_9.top = cell1_6;
    cell1_9.left = cell1_8;

    ArrayList<ArrayList<Cell>> board2 = new ArrayList<ArrayList<Cell>>(
        Arrays.asList(new ArrayList<Cell>(Arrays.asList(cell1_1, cell1_2, cell1_3)),
            new ArrayList<Cell>(Arrays.asList(cell1_4, cell1_5, cell1_6)),
            new ArrayList<Cell>(Arrays.asList(cell1_7, cell1_8, cell1_9))));

    Cell box1 = new Cell(0, 0, Color.red, true);
    Cell box2 = new Cell(1, 0, Color.red, false);
    Cell box3 = new Cell(2, 0, Color.red, false);
    Cell box4 = new Cell(3, 0, Color.red, false);
    Cell box5 = new Cell(0, 1, Color.red, false);
    Cell box6 = new Cell(1, 1, Color.red, false);
    Cell box7 = new Cell(2, 1, Color.red, false);
    Cell box8 = new Cell(3, 1, Color.red, false);
    Cell box9 = new Cell(0, 2, Color.red, false);
    Cell box10 = new Cell(1, 2, Color.red, false);
    Cell box11 = new Cell(2, 2, Color.red, false);
    Cell box12 = new Cell(3, 2, Color.red, false);
    Cell box13 = new Cell(0, 3, Color.red, false);
    Cell box14 = new Cell(1, 3, Color.red, false);
    Cell box15 = new Cell(2, 3, Color.red, false);
    Cell box16 = new Cell(3, 3, Color.red, false);

    box1.right = box2;
    box1.bottom = box5;
    box2.left = box1;
    box2.right = box3;
    box2.bottom = box6;
    box3.left = box2;
    box3.bottom = box7;
    box3.right = box4;
    box4.left = box3;
    box4.bottom = box8;
    box5.top = box1;
    box5.right = box6;
    box5.bottom = box9;
    box6.top = box2;
    box6.left = box5;
    box6.bottom = box10;
    box6.right = box7;
    box7.top = box3;
    box7.left = box6;
    box7.bottom = box11;
    box7.right = box8;
    box8.top = box4;
    box8.left = box7;
    box8.bottom = box12;
    box9.top = box5;
    box9.right = box10;
    box9.bottom = box13;
    box10.top = box6;
    box10.left = box9;
    box10.bottom = box14;
    box10.right = box11;
    box11.top = box7;
    box11.left = box10;
    box11.bottom = box15;
    box11.right = box12;
    box12.top = box8;
    box12.left = box11;
    box12.bottom = box16;
    box13.top = box9;
    box13.right = box14;
    box14.left = box13;
    box14.top = box10;
    box14.right = box15;
    box15.left = box14;
    box15.top = box11;
    box15.right = box16;
    box16.top = box12;
    box16.left = box15;

    ArrayList<ArrayList<Cell>> board3 = new ArrayList<ArrayList<Cell>>(
        Arrays.asList(new ArrayList<Cell>(Arrays.asList(box1, box2, box3, box4)),
            new ArrayList<Cell>(Arrays.asList(box5, box6, box7, box8)),
            new ArrayList<Cell>(Arrays.asList(box9, box10, box11, box12)),
            new ArrayList<Cell>(Arrays.asList(box13, box14, box15, box16))));

    return t.checkExpect(flood1.cells, board1) && t.checkExpect(flood2.cells, board2)
        && t.checkExpect(flood3.cells, board3);

  }

  // tests the calcMoves function
  boolean testCalcMoves(Tester t) {

    return t.checkExpect(flood1.calcMoves(flood1.size, flood1.numColors), 4)
        && t.checkExpect(flood4.calcMoves(flood4.size, flood4.numColors), 11)
        && t.checkExpect(flood3.calcMoves(flood3.size, flood3.numColors), 8);
  }

  // tests the didWin function
  boolean testDidWin(Tester t) {
    FloodIt flood5 = new FloodIt(7, 6);
    FloodIt flood6 = new FloodIt(3, 6);

    for (ArrayList<Cell> row : flood5.cells) {
      for (Cell c : row) {
        c.flooded = true;
      }
    }

    for (int i = 0; i < 2; i++) {
      for (int j = 0; j < 3; j++) {
        flood6.cells.get(i).get(j).flooded = true;
      }
    }

    return t.checkExpect(flood5.didWin(), true) && t.checkExpect(flood6.allDone(), false)
        && t.checkExpect(this.flood3.didWin(), false);
  }

  // tests the allDone function
  boolean testAllDone(Tester t) {
    FloodIt flood5 = new FloodIt(4, 4);
    FloodIt flood6 = new FloodIt(5, 5);

    for (int i = 0; i < 2; i++) {
      for (int j = 0; j < 3; j++) {
        flood6.cells.get(i).get(j).color = Color.red;
      }
    }

    for (int i = 0; i < 2; i++) {
      for (int j = 0; j < 4; j++) {
        flood6.cells.get(i).get(j).flooded = true;
      }
    }

    return t.checkExpect(this.flood3.allDone(), true) && t.checkExpect(flood5.allDone(), true)
        && t.checkExpect(flood6.allDone(), false);
  }

  // tests the onKeyEvent function
  void testOnKeyEvent(Tester t) {
    initData();

    ArrayList<Color> colors4 = new ArrayList<Color>();
    ArrayList<Color> colors5 = new ArrayList<Color>();
    ArrayList<Color> colors6 = new ArrayList<Color>();

    FloodIt flood6 = new FloodIt(2, 2);
    FloodIt flood7 = new FloodIt(3, 3);
    FloodIt flood8 = new FloodIt(4, 6);

    for (ArrayList<Cell> row : flood6.cells) {
      for (Cell c : row) {
        c.color = Color.blue;
        colors4.add(c.color);
      }
    }

    for (ArrayList<Cell> row : flood7.cells) {
      for (Cell c : row) {
        c.color = Color.green;
        colors5.add(c.color);
      }
    }

    for (ArrayList<Cell> row : flood8.cells) {
      for (Cell c : row) {
        c.color = Color.red;
        colors6.add(c.color);
      }
    }

    this.flood1.onKeyEvent("r");
    this.flood2.onKeyEvent("b");
    this.flood3.onKeyEvent("r");

    for (int i = 0; i < flood1.size; i++) {
      for (int j = 0; j < flood1.size; j++) {
        t.checkExpect(flood6.cells.get(i).get(j).x == this.flood1.cells.get(i).get(j).x, true);
        t.checkExpect(flood6.cells.get(i).get(j).y == this.flood1.cells.get(i).get(j).y, true);
        t.checkExpect(
            colors1.equals(colors5), false);
        t.checkExpect(flood6.cells.get(i).get(j).flooded == this.flood1.cells.get(i).get(j).flooded,
            true);
      }
    }

    for (int i = 0; i < flood7.size; i++) {
      for (int j = 0; j < flood7.size; j++) {
        t.checkExpect(flood7.cells.get(i).get(j).x == this.flood2.cells.get(i).get(j).x, true);
        t.checkExpect(flood7.cells.get(i).get(j).y == this.flood2.cells.get(i).get(j).y, true);
        t.checkExpect(
            flood7.cells.get(i).get(j).color.equals(this.flood2.cells.get(i).get(j).color), true);
        t.checkExpect(flood7.cells.get(i).get(j).flooded == this.flood2.cells.get(i).get(j).flooded,
            true);
      }
    }

    for (int i = 0; i < flood8.size; i++) {
      for (int j = 0; j < flood8.size; j++) {
        t.checkExpect(flood8.cells.get(i).get(j).x == this.flood3.cells.get(i).get(j).x, true);
        t.checkExpect(flood8.cells.get(i).get(j).y == this.flood3.cells.get(i).get(j).y, true);
        t.checkExpect(colors3.equals(colors6), false);
        t.checkExpect(flood8.cells.get(i).get(j).flooded == this.flood3.cells.get(i).get(j).flooded,
            true);
      }
    }
  }

  // tests the onMouseClick function
  void testOnMouseClick(Tester t) {
    boolean yes;

  }
  
  
  void testDrawEndWorld(Tester t) {
    t.checkExpect(flood4.drawEndWorld("Win!"), 
        new OverlayImage( 
            new TextImage("You Win!", 40.0, FontStyle.REGULAR, Color.blue),
       new RectangleImage(900, 900, OutlineMode.SOLID, Color.yellow)));
    t.checkExpect(flood3.drawEndWorld("Lose!"),
        new OverlayImage( 
            new TextImage("You Lose!", 40.0, FontStyle.REGULAR, Color.red),
       new RectangleImage(600, 600, OutlineMode.SOLID, Color.black)));
        
  }
  
  void testLastScene(Tester t) {
    WorldScene world1 = new WorldScene(900, 900);
    WorldScene world2 = new WorldScene(600, 600);
    
    OverlayImage winScene = new OverlayImage( 
        new TextImage("You Win!", 40.0, FontStyle.REGULAR, Color.blue),
   new RectangleImage(900, 900, OutlineMode.SOLID, Color.yellow));
    
    OverlayImage loseScene = new OverlayImage( 
            new TextImage("You Lose!", 40.0, FontStyle.REGULAR, Color.red),
       new RectangleImage(600, 600, OutlineMode.SOLID, Color.black));
    
    world1.placeImageXY(winScene, 450, 450);
    world2.placeImageXY(loseScene, 300, 300);
    
    
    
    
    t.checkExpect(flood4.lastScene("Win!"), world1);
    t.checkExpect(flood3.lastScene("Lose!"), world2);
        
  }
  
  void testMakeScene(Tester t) {
    
    for(int i =0; i < flood2.size; i++) {
      for(int j =0; j< flood2.size; j++) {
        flood2.cells.get(i).get(j).flooded = true;
      }
    }
    
    flood3.numMoves = 0;
    
    WorldScene world1 = new WorldScene(450, 450);
    WorldScene world2 = new WorldScene(600, 600);
    
    OverlayImage winScene = new OverlayImage( 
        new TextImage("You Win!", 40.0, FontStyle.REGULAR, Color.blue),
   new RectangleImage(450, 450, OutlineMode.SOLID, Color.yellow));
    
    OverlayImage loseScene = new OverlayImage( 
            new TextImage("You Lose!", 40.0, FontStyle.REGULAR, Color.red),
       new RectangleImage(600, 600, OutlineMode.SOLID, Color.black));
    
    world1.placeImageXY(winScene, 225, 225);
    world2.placeImageXY(loseScene, 300, 300);
    
    t.checkExpect(flood2.makeScene(), world1);
    t.checkExpect(flood3.makeScene(), world2);
    
    OverlayOffsetAlign board = new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.MIDDLE,
        new AboveImage(new AboveImage(new AboveImage(new AboveImage(new AboveImage(new AboveImage(
        new EmptyImage(),new EmptyImage()),new BesideImage(new BesideImage(new BesideImage(
            new BesideImage(
                                            new BesideImage(
                                                new BesideImage(
                                                    new EmptyImage(),
                                                    new EmptyImage()),
                                                new RectangleImage(20, 20, 
                                                    OutlineMode.SOLID,
                                                    Color.red)),
                                            new RectangleImage(20, 20, 
                                                OutlineMode.SOLID,
                                                Color.red)),
                                        new RectangleImage(20, 20, 
                                            OutlineMode.SOLID,
                                            Color.red)),
                                    new RectangleImage(20, 20, 
                                        OutlineMode.SOLID,
                                        Color.red)),
                                new RectangleImage(20, 20, 
                                    OutlineMode.SOLID,
                                    Color.red))),
                        new BesideImage(
                            new BesideImage(
                                new BesideImage(
                                    new BesideImage(
                                        new BesideImage(
                                            new BesideImage(
                                                new EmptyImage(),
                                                new EmptyImage()),
                                            new RectangleImage(20, 20, 
                                                OutlineMode.SOLID,
                                                Color.red)),
                                        new RectangleImage(20, 20, 
                                            OutlineMode.SOLID,
                                            Color.green)),
                                    new RectangleImage(20, 20, 
                                        OutlineMode.SOLID,
                                        Color.yellow)),
                                new RectangleImage(20, 20, 
                                    OutlineMode.SOLID,
                                    Color.blue)),
                            new RectangleImage(20, 20, 
                                OutlineMode.SOLID,
                                Color.pink))),
                    new BesideImage(
                        new BesideImage(
                            new BesideImage(
                                new BesideImage(
                                    new BesideImage(
                                        new BesideImage(
                                            new EmptyImage(),
                                            new EmptyImage()),
                                        new RectangleImage(20, 20, 
                                            OutlineMode.SOLID,
                                            Color.red)),
                                    new RectangleImage(20, 20, 
                                        OutlineMode.SOLID,
                                        Color.yellow)),
                                new RectangleImage(20, 20, 
                                    OutlineMode.SOLID,
                                    Color.pink)),
                            new RectangleImage(20, 20, 
                                OutlineMode.SOLID,
                                Color.red)),
                        new RectangleImage(20, 20, 
                            OutlineMode.SOLID,
                            Color.yellow))),
                new BesideImage(
                    new BesideImage(
                        new BesideImage(
                            new BesideImage(
                                new BesideImage(
                                    new BesideImage(
                                        new EmptyImage(),
                                        new EmptyImage()),
                                    new RectangleImage(20, 20, 
                                        OutlineMode.SOLID,
                                        Color.red)),
                                new RectangleImage(20, 20, 
                                    OutlineMode.SOLID,
                                    Color.blue)),
                            new RectangleImage(20, 20, 
                                OutlineMode.SOLID,
                                Color.red)),
                        new RectangleImage(20, 20, 
                            OutlineMode.SOLID,
                            Color.blue)),
                    new RectangleImage(20, 20, 
                        OutlineMode.SOLID,
                        Color.red))),
            new BesideImage(
                new BesideImage(
                    new BesideImage(
                        new BesideImage(
                            new BesideImage(
                                new BesideImage(
                                    new EmptyImage(),
                                    new EmptyImage()),
                                new RectangleImage(20, 20, 
                                    OutlineMode.SOLID,
                                    Color.red)),
                            new RectangleImage(20, 20, 
                                OutlineMode.SOLID,
                                Color.pink)),
                        new RectangleImage(20, 20, 
                            OutlineMode.SOLID,
                            Color.yellow)),
                    new RectangleImage(20, 20, 
                        OutlineMode.SOLID,
                        Color.red)),
                new RectangleImage(20, 20, 
                    OutlineMode.SOLID,
                    Color.pink))),
        0, 0, new EmptyImage());    
    
    
    
    WorldScene world = new WorldScene(750,750);
    RectangleImage border = new RectangleImage(750, 750, OutlineMode.SOLID, Color.black);
    RectangleImage background = new RectangleImage(1500, 1500, OutlineMode.SOLID, Color.cyan);
    RectangleImage edge1 = new RectangleImage(5, 100, OutlineMode.SOLID, Color.black);
    RectangleImage edge2 = new RectangleImage(105, 5, OutlineMode.SOLID, Color.black);
    TextImage turns = new TextImage(
        "Turns Left: 6", 13.0, FontStyle.REGULAR, Color.black);
    TextImage time = new TextImage(
        "Time Taken: 0", 13.0, FontStyle.REGULAR, Color.black);
    OverlayImage button = new OverlayImage(
        new TextImage("Increase Difficulty", 13.0, FontStyle.REGULAR, Color.cyan),
        new RectangleImage(200, 40, OutlineMode.SOLID, Color.black));
    
    //world.placeImageXY(border, 375, 375);
    world.placeImageXY(background, 0, 0);
    world.placeImageXY(board, 50, 50);
    world.placeImageXY(edge1, 100, 50);
    world.placeImageXY(edge2, 50, 100);
    world.placeImageXY(turns, 155, 25);
    world.placeImageXY(time, 165, 50);
    world.placeImageXY(button, 110, 150);
    
    
      t.checkExpect(flood10.makeScene(), world);
          

  }
  
}

