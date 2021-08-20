import java.util.*;
import tester.Tester;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;


// represents the edge that connects to verticies
class Edge implements Comparable<Edge> {
  Vertex start;
  Vertex end;
  int weight;
  boolean isWall;

  Edge(Vertex start, Vertex end, int weight, boolean isWall) {
    this.start = start;
    this.end = end;
    this.weight = weight;
    this.isWall = isWall;
  }

  @Override
  // compares two edges by weight
  public int compareTo(Edge o) {
    if (this.weight >= o.weight) {
      return 1;
    }
    else {
      return -1;
    }
  }

}

// represents the square or position in the maze
class Vertex {
  int x;
  int y;
  Color c;
  Vertex left;
  Vertex right;
  Vertex top;
  Vertex bottom;
  
  Vertex(int x, int y, Color c) {
    this.x = x;
    this.y = y;
    this.c = c;
  }
  
  Vertex(int x, int y, Color c, Vertex left, Vertex right, Vertex top, Vertex bottom) {
    this.x = x;
    this.y = y;
    this.c = c;
  }
  
  public boolean isBlocked(Vertex endVertex, Maze m) {
    for(Edge edge: m.edges) {
      if (edge.start == this && edge.end == endVertex || 
          edge.start == endVertex && edge.end == this) {
        return edge.isWall;
      }
    }
    return true;
  }
}

// represents the maze
class Maze extends World {
  Vertex curr;
  boolean isManual; 
  Posn size;
  ArrayList<ArrayList<Vertex>> verticies;
  ArrayList<Edge> edges;
  ArrayList<Edge> walls;
  boolean isBFS;
  boolean isDFS;
  Random rand = new Random();
  Deque<Vertex> q = new LinkedList<Vertex>();
  Stack<Vertex> s = new Stack<Vertex>();
  HashMap<Vertex, Boolean> visited = new HashMap<Vertex, Boolean>();
  HashMap<Vertex, Vertex> prev = new HashMap<Vertex, Vertex>();
  

  Maze(Posn size) {
    this.size = size;
    this.verticies = createVerticies();
    this.edges = makeMaze();
    this.curr = this.verticies.get(0).get(0);
    
    for(ArrayList<Vertex> row: this.verticies) {
      for(Vertex curr: row) {
        this.visited.put(curr, false);
        this.prev.put(curr, null);
      }
    }
   
    
    q.add(this.verticies.get(0).get(0));   
    s.push(this.verticies.get(0).get(0)); 
    visited.put(this.verticies.get(0).get(0), true);   
  }

  // creates a seeded maze
  Maze(Random rand, Posn size) {
    
    this.rand = rand;
    this.size = size;
    this.verticies = createVerticies();
    this.edges = makeMaze();
    this.curr = this.verticies.get(0).get(0);
    
    for(ArrayList<Vertex> row: this.verticies) {
      for(Vertex curr: row) {
        this.visited.put(curr, false);
        this.prev.put(curr, null);
      }
    }
    
    q.add(this.verticies.get(0).get(0)); 
    s.push(this.verticies.get(0).get(0)); 
    visited.put(this.verticies.get(0).get(0), true);
  }

  // creates the walls for the maze
  ArrayList<Edge> makeMaze() {
    ArrayList<Edge> edgeList = new ArrayList<Edge>();

    for (int i = 0; i < this.size.y; i++) {
      for (int j = 0; j < this.size.x; j++) {
        
        if (j > 0) {
          this.verticies.get(i).get(j).left = this.verticies.get(i).get(j - 1);
        }

        if (j < this.size.x - 1) {
          this.verticies.get(i).get(j).right = this.verticies.get(i).get(j + 1);
        }

        if (i > 0) {
          this.verticies.get(i).get(j).top = this.verticies.get(i - 1).get(j);
        }

        if (i < this.size.y - 1) {
          this.verticies.get(i).get(j).bottom = this.verticies.get(i + 1).get(j);

        }
        
        
        if (i < this.size.y - 1) {
          edgeList.add(new Edge(verticies.get(i).get(j), verticies.get(i + 1).get(j),
              this.rand.nextInt(this.size.x * this.size.y), true));
        }

        if (j < this.size.x - 1) {
          edgeList.add(new Edge(verticies.get(i).get(j), verticies.get(i).get(j + 1),
              this.rand.nextInt(this.size.x * this.size.y), true));
        }
      }
    }
    
    this.edges = edgeList;
    this.addSortedWeight();
    this.createWalls();
    return edgeList;
  }

  // creates the verticies for the maze
  public ArrayList<ArrayList<Vertex>> createVerticies() {
    ArrayList<ArrayList<Vertex>> board = new ArrayList<ArrayList<Vertex>>();
    for (int i = 0; i < this.size.y; i++) {
      ArrayList<Vertex> row = new ArrayList<Vertex>();
      for (int j = 0; j < this.size.x; j++) {
        if (j ==0 && i ==0) row.add(new Vertex(j, i, Color.GREEN));
        else if (j == this.size.x-1 && i == this.size.y-1) row.add(new Vertex(j, i, Color.MAGENTA));
        else row.add(new Vertex(j, i, Color.LIGHT_GRAY));
            
      }
      
      board.add(row);
    }
    
    return board;
  }

  // sorts the edges by weight to perform Kruskal's algorithm on
  void addSortedWeight() {
    Collections.sort(this.edges);
  }

  // performs Kruskal's algorithm so edges become walls
  void createWalls() {
    HashMap<Vertex, Integer> trees = new HashMap<Vertex, Integer>();
    ArrayList<Edge> edgesInTree = new ArrayList<Edge>();
    for (ArrayList<Vertex> row : this.verticies) {
      for (Vertex v : row) {
        trees.put(v, v.x * 1000 + v.y);
      }
    }

    int i = 0;
    // System.out.println(this.verticies.size()*this.verticies.get(0).size());
    // System.out.println(edges.size());
    while (edgesInTree.size() < this.verticies.size() * this.verticies.get(0).size() - 1) {
      if (!(trees.get(this.edges.get(i).start).equals(trees.get(this.edges.get(i).end)))) {
        this.edges.get(i).isWall = false;
        edgesInTree.add(this.edges.get(i));
        int valueToReplace = trees.get(this.edges.get(i).end);
        int replaceWith = trees.get(this.edges.get(i).start);
        trees.forEach((k, v) -> {
          if (v == valueToReplace) {
            trees.replace(k, v, replaceWith);
          }
        });
      }
      i++;
    }
  }

  // (0,0) --> (0,0)
  // (0,1) --> (0,1)

  // draws the game
  public WorldScene makeScene() {
    int scale = 600 / this.size.y;
    WorldScene scene = new WorldScene(600 * this.size.x / this.size.y, 600);
    for (ArrayList<Vertex> row : this.verticies) {
      for (Vertex v : row) {
        scene.placeImageXY(new RectangleImage(scale, scale, OutlineMode.SOLID, v.c),
            v.x * scale + scale / 2, v.y * scale + scale / 2);

      }
    }

    for (Edge w : this.edges) {
      if (w.isWall) {
        // System.out.println("Start: (" + w.start.x + "," + w.start.y + ")");
        // System.out.println("End: (" + w.end.x + "," + w.end.y + ")");
        if (w.end.y - w.start.y == 1) {
          scene.placeImageXY(
              new LineImage(new Posn((w.end.y - w.start.y) * scale, (w.end.x - w.start.x) * scale),
                  Color.black),
              w.end.x * scale + scale / 2, w.end.y * scale);
        }
        else {
          scene.placeImageXY(
              new LineImage(new Posn((w.end.y - w.start.y) * scale, (w.end.x - w.start.x) * scale),
                  Color.black),
              w.end.x * scale, w.end.y * scale + scale / 2);
        }

      }
    }

    return scene;
  }
  
  
  public void onTick() {
    if(this.isBFS) {
      this.BFSHelper();
      Vertex start = this.verticies.get(0).get(0);
      Vertex end = this.verticies.get(this.size.y-1).get(this.size.x-1);
      if (this.prev.get(end) != null) {
        this.isBFS = false;
        this.drawPath(prev, start, end);
      }
    }
    
    if(this.isDFS) {
      this.DFSHelper();
      Vertex start = this.verticies.get(0).get(0);
      Vertex end = this.verticies.get(this.size.y-1).get(this.size.x-1);
      if (this.prev.get(end) != null) {
        this.isDFS = false;
        this.drawPath(prev, start, end);
      }
    }
  }
  
  public void BFSHelper() {
      Vertex vert = this.q.pollFirst();
      
      List<Vertex> neighbors = new ArrayList<Vertex>(
          Arrays.asList(vert.left, vert.right, vert.top, vert.bottom));
      
      neighbors.removeIf(s -> vert.isBlocked(s, this));
      
      // **** possibly remove the != null
      for (Vertex next: neighbors) {
        if(next != null && !this.visited.get(next)) {
          this.q.add(next);
          this.visited.put(next, true);
          next.c = Color.cyan;
          this.prev.put(next, vert);
        }
      }
    }
  
  public void DFSHelper() {
    Vertex vert = this.s.pop();
    
    List<Vertex> neighbors = new ArrayList<Vertex>(
        Arrays.asList(vert.left, vert.right, vert.top, vert.bottom));
    
    neighbors.removeIf(a -> vert.isBlocked(a, this));
    
    // **** possibly remove the != null
    for (Vertex next: neighbors) {
      if(next != null && !this.visited.get(next)) {
        this.s.push(next);
        System.out.println(this.s);
        this.visited.put(next, true);
        next.c = Color.cyan;
        this.prev.put(next, vert);
      }
    }
  }
 
  
  public void drawPath(HashMap<Vertex, Vertex> prev, Vertex start, Vertex end) {
    for(Vertex current = end; current != null; current = prev.get(current)) {
      current.c = Color.BLUE;
    }
  }
  
  
  public void onKeyEvent(String key) {
    if (key.equals("b")) {
      this.isBFS = true;
    }
    
    if (key.equals("d")) {
      this.isDFS = true;
    }
    
    if (key.equals("r")) {
      this.verticies = createVerticies();
      this.edges = makeMaze();
      this.isBFS = false;
      this.isDFS = false;
      this.curr = verticies.get(0).get(0);
      
      for(ArrayList<Vertex> row: this.verticies) {
        for(Vertex curr: row) {
          this.visited.put(curr, false);
          this.prev.put(curr, null);
        }
      }
      
      q.add(this.verticies.get(0).get(0));   
      visited.put(this.verticies.get(0).get(0), true);
    }
    
    if(key.equals("m")) {
      this.isManual = true;
    }
    
    if(isManual) {
      if (key.equals("left")) {
        Vertex prevCurr = this.curr;
        this.move(new Posn(-1, 0));
        if(!this.visited.get(this.curr)) {
          this.visited.put(this.curr, true);
          this.prev.put(this.curr, prevCurr);
        
        }
        
      }
      
      if (key.equals("right")) {
        Vertex prevCurr = this.curr;
        this.move(new Posn(1, 0));
        if(!this.visited.get(this.curr)) {
          this.visited.put(this.curr, true);
          this.prev.put(this.curr, prevCurr);
        
        }
        
      } 
      
      if (key.equals("up")) {
        Vertex prevCurr = this.curr;
        this.move(new Posn(0, -1)); 
        if(!this.visited.get(this.curr)) {
          this.visited.put(this.curr, true);
          this.prev.put(this.curr, prevCurr);
        
        }
        
      }
      
      if (key.equals("down")) {
        Vertex prevCurr = this.curr;
        this.move(new Posn(0, 1));
        if(!this.visited.get(this.curr)) {
          this.visited.put(this.curr, true);
          this.prev.put(this.curr, prevCurr);
        
        } 
        
      }
      
      if(this.curr.x == this.size.x -1 && this.curr.y == this.size.y -1) {
        Vertex start = this.verticies.get(0).get(0);
        Vertex end = this.verticies.get(this.size.y-1).get(this.size.x-1);
        this.drawPath(this.prev, start, end);
      }
    }
   }
  
  //abstracted method
  public void move(Posn dir) {
    if(this.curr.x + dir.x >=0 && this.curr.y + dir.y >=0 && this.curr.x + dir.x <= this.size.x - 1 && this.curr.y + dir.y <= this.size.y - 1) {
      if(!curr.isBlocked(this.verticies.get(this.curr.y + dir.y).get(this.curr.x + dir.x), this)) {
        this.curr.c = Color.cyan;
        this.curr = this.verticies.get(this.curr.y + dir.y).get(this.curr.x + dir.x);
        this.curr.c = Color.black;
      }
    }
  }
  

}

// represents examples of games;
class ExamplesMaze {
  ExamplesMaze() {}

  Maze maze = new Maze(new Posn(100, 60));
  // same examples as ones below but tests randoms
  Maze maze1 = new Maze(new Posn(3, 3));
  Maze maze2 = new Maze(new Posn(4, 8));
  Maze maze3 = new Maze(new Posn(5, 4));

  // same examples as ones above but seeds it
  Maze maze4 = new Maze(new Random(1), new Posn(3, 3));
  Maze maze5 = new Maze(new Random(56), new Posn(4, 8));
  Maze maze6 = new Maze(new Random(77), new Posn(5, 4));

  Edge edge1 = new Edge(new Vertex(1, 4, Color.LIGHT_GRAY), 
      new Vertex(2, 4, Color.LIGHT_GRAY), 7, false);
  Edge edge2 = new Edge(new Vertex(3, 2, Color.LIGHT_GRAY), 
      new Vertex(3, 3, Color.LIGHT_GRAY), 8, true);
  Edge edge3 = new Edge(new Vertex(5, 4, Color.LIGHT_GRAY), 
      new Vertex(6, 4, Color.LIGHT_GRAY), 7, true);

  void testBigBang(Tester t) {
    this.maze.bigBang(600 * maze.size.x / maze.size.y, 600, 0.00001);
  }

  void testCompareTo(Tester t) {
    t.checkExpect(edge1.compareTo(edge2), -1);
    t.checkExpect(edge2.compareTo(edge3), 1);
    t.checkExpect(edge3.compareTo(edge1), 1);
  }

  // tests the createVerticies method
  public boolean testCreateVerticies(Tester t) {
    return t.checkExpect(maze1.verticies, new ArrayList<ArrayList<Vertex>>(Arrays.asList(
        new ArrayList<Vertex>(Arrays.asList(new Vertex(0, 0, Color.GREEN), 
            new Vertex(1, 0, Color.LIGHT_GRAY), new Vertex(2, 0, Color.LIGHT_GRAY))),
        new ArrayList<Vertex>(Arrays.asList(new Vertex(0, 1, Color.LIGHT_GRAY),
            new Vertex(1, 1, Color.LIGHT_GRAY), new Vertex(2, 1, Color.LIGHT_GRAY))),
        new ArrayList<Vertex>(
            Arrays.asList(new Vertex(0, 2, Color.LIGHT_GRAY), 
                new Vertex(1, 2, Color.LIGHT_GRAY), new Vertex(2, 2, Color.LIGHT_GRAY))))))
        && t.checkExpect(maze2.verticies,
            new ArrayList<ArrayList<Vertex>>(Arrays.asList(
                new ArrayList<Vertex>(Arrays.asList(new Vertex(0, 0, Color.GREEN), 
                    new Vertex(1, 0, Color.LIGHT_GRAY),
                    new Vertex(2, 0, Color.LIGHT_GRAY), 
                    new Vertex(3, 0, Color.LIGHT_GRAY))),
                new ArrayList<Vertex>(Arrays.asList(new Vertex(0, 1, Color.LIGHT_GRAY),
                    new Vertex(1, 1, Color.LIGHT_GRAY),
                    new Vertex(2, 1, Color.LIGHT_GRAY),
                    new Vertex(3, 1, Color.LIGHT_GRAY))),
                new ArrayList<Vertex>(Arrays.asList(new Vertex(0, 2, Color.LIGHT_GRAY), 
                    new Vertex(1, 2, Color.LIGHT_GRAY),
                    new Vertex(2, 2, Color.LIGHT_GRAY), 
                    new Vertex(3, 2, Color.LIGHT_GRAY))),
                new ArrayList<Vertex>(Arrays.asList(new Vertex(0, 3, Color.LIGHT_GRAY), 
                    new Vertex(1, 3, Color.LIGHT_GRAY),
                    new Vertex(2, 3, Color.LIGHT_GRAY), 
                    new Vertex(3, 3, Color.LIGHT_GRAY))),
                new ArrayList<Vertex>(Arrays.asList(new Vertex(0, 4, Color.LIGHT_GRAY), 
                    new Vertex(1, 4, Color.LIGHT_GRAY),
                    new Vertex(2, 4, Color.LIGHT_GRAY), 
                    new Vertex(3, 4, Color.LIGHT_GRAY))),
                new ArrayList<Vertex>(Arrays.asList(new Vertex(0, 5, Color.LIGHT_GRAY), 
                    new Vertex(1, 5, Color.LIGHT_GRAY),
                    new Vertex(2, 5, Color.LIGHT_GRAY), 
                    new Vertex(3, 5, Color.LIGHT_GRAY))),
                new ArrayList<Vertex>(Arrays.asList(new Vertex(0, 6, Color.LIGHT_GRAY), 
                    new Vertex(1, 6, Color.LIGHT_GRAY),
                    new Vertex(2, 6, Color.LIGHT_GRAY), 
                    new Vertex(3, 6, Color.LIGHT_GRAY))),
                new ArrayList<Vertex>(Arrays.asList(new Vertex(0, 7, Color.LIGHT_GRAY), 
                    new Vertex(1, 7, Color.LIGHT_GRAY),
                    new Vertex(2, 7, Color.LIGHT_GRAY), 
                    new Vertex(3, 7, Color.LIGHT_GRAY))))))
        && t.checkExpect(maze3.verticies,
            new ArrayList<ArrayList<Vertex>>(Arrays.asList(
                new ArrayList<Vertex>(
                    Arrays.asList(new Vertex(0, 0, Color.GREEN), 
                        new Vertex(1, 0, Color.LIGHT_GRAY), new Vertex(2, 0, Color.LIGHT_GRAY),
                        new Vertex(3, 0, Color.LIGHT_GRAY), new Vertex(4, 0, Color.LIGHT_GRAY))),
                new ArrayList<Vertex>(
                    Arrays.asList(new Vertex(0, 1, Color.LIGHT_GRAY), 
                        new Vertex(1, 1, Color.LIGHT_GRAY), new Vertex(2, 1, Color.LIGHT_GRAY),
                        new Vertex(3, 1, Color.LIGHT_GRAY), new Vertex(4, 1, Color.LIGHT_GRAY))),
                new ArrayList<Vertex>(
                    Arrays.asList(new Vertex(0, 2, Color.LIGHT_GRAY), 
                        new Vertex(1, 2, Color.LIGHT_GRAY), new Vertex(2, 2, Color.LIGHT_GRAY),
                        new Vertex(3, 2, Color.LIGHT_GRAY), new Vertex(4, 2, Color.LIGHT_GRAY))),
                new ArrayList<Vertex>(
                    Arrays.asList(new Vertex(0, 3, Color.LIGHT_GRAY), 
                        new Vertex(1, 3, Color.LIGHT_GRAY), new Vertex(2, 3, Color.LIGHT_GRAY),
                        new Vertex(3, 3, Color.LIGHT_GRAY), new Vertex(4, 3, Color.LIGHT_GRAY))))));
  }

  // tests to see if the edges are turned into walls correctly using Kruskals's
  // algorithm
  public boolean testWalls(Tester t) {
    // checks to see if there are the correct number of edges
    t.checkExpect(maze1.edges.size(), 12);
    t.checkExpect(maze2.edges.size(), 52);
    t.checkExpect(maze3.edges.size(), 31);

    // theses next tests check to make sure that the number of walls are the amount
    // of edges
    // minus the number of edges in the minimal spanning tree found using Kruskal's
    // algorithm
    // walls = # of edges - minimal spanning tree or( # of verticies - 1)
    int walls1 = 0;
    int walls2 = 0;
    int walls3 = 0;

    for (Edge e : maze1.edges) {
      if (e.isWall) {
        walls1++;
      }
    }
    t.checkExpect(walls1, 4);

    for (Edge e : maze2.edges) {
      if (e.isWall) {
        walls2++;
      }
    }
    t.checkExpect(walls2, 21);

    for (Edge e : maze3.edges) {
      if (e.isWall) {
        walls3++;
      }
    }
    t.checkExpect(walls3, 12);

    // checks to make sure all the edges weights are randomized so the mazes are
    // created with
    // random walls every times using Kruskal's algorithm
    for (Edge e : maze1.edges) {
      t.checkOneOf(e.weight, 0, 1, 2, 3, 4, 5, 6, 7, 8);
    }

    for (Edge e : maze2.edges) {
      t.checkOneOf(e.weight, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
          20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31);
    }

    for (Edge e : maze3.edges) {
      t.checkOneOf(e.weight, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19);
    }

    // tests belows check to see if walls were created right and are of the right
    // number
    ArrayList<Edge> connectors1 = new ArrayList<Edge>();
    ArrayList<Edge> connectors2 = new ArrayList<Edge>();
    ArrayList<Edge> connectors3 = new ArrayList<Edge>();

    for (Edge e : this.maze4.edges) {
      if (e.isWall) {
        connectors1.add(e);
      }
    }

    t.checkExpect(connectors1, new ArrayList<Edge>(Arrays
        .asList(new Edge(maze4.verticies.get(0).get(0), maze4.verticies.get(1).get(0), 6, true),
            new Edge(maze4.verticies.get(2).get(0), maze4.verticies.get(2).get(1), 7, true),
            new Edge(maze4.verticies.get(2).get(1), maze4.verticies.get(2).get(2), 7, true),
            new Edge(maze4.verticies.get(0).get(2), maze4.verticies.get(1).get(2), 8, true))));

    for (Edge e : this.maze5.edges) {
      if (e.isWall) {
        connectors2.add(e);
      }
    }

    t.checkExpect(connectors2,
        new ArrayList<Edge>(Arrays.asList(
            new Edge(maze5.verticies.get(5).get(1), maze5.verticies.get(5).get(2), 15, true),
            new Edge(maze5.verticies.get(3).get(2), maze5.verticies.get(4).get(2), 20, true),
            new Edge(maze5.verticies.get(7).get(0), maze5.verticies.get(7).get(1), 20, true),
            new Edge(maze5.verticies.get(3).get(1), maze5.verticies.get(3).get(2), 21, true),
            new Edge(maze5.verticies.get(2).get(0), maze5.verticies.get(2).get(1), 22, true),
            new Edge(maze5.verticies.get(3).get(3), maze5.verticies.get(4).get(3), 22, true),
            new Edge(maze5.verticies.get(5).get(0), maze5.verticies.get(6).get(0), 22, true),
            new Edge(maze5.verticies.get(6).get(2), maze5.verticies.get(7).get(2), 22, true),
            new Edge(maze5.verticies.get(0).get(0), maze5.verticies.get(1).get(0), 23, true),
            new Edge(maze5.verticies.get(1).get(3), maze5.verticies.get(2).get(3), 23, true),
            new Edge(maze5.verticies.get(3).get(0), maze5.verticies.get(4).get(0), 23, true),
            new Edge(maze5.verticies.get(4).get(0), maze5.verticies.get(4).get(1), 23, true),
            new Edge(maze5.verticies.get(0).get(2), maze5.verticies.get(0).get(3), 24, true),
            new Edge(maze5.verticies.get(0).get(1), maze5.verticies.get(0).get(2), 26, true),
            new Edge(maze5.verticies.get(2).get(2), maze5.verticies.get(3).get(2), 26, true),
            new Edge(maze5.verticies.get(1).get(2), maze5.verticies.get(2).get(2), 27, true),
            new Edge(maze5.verticies.get(5).get(2), maze5.verticies.get(5).get(3), 28, true),
            new Edge(maze5.verticies.get(6).get(1), maze5.verticies.get(6).get(2), 28, true),
            new Edge(maze5.verticies.get(2).get(0), maze5.verticies.get(3).get(0), 28, true),
            new Edge(maze5.verticies.get(6).get(1), maze5.verticies.get(7).get(1), 30, true),
            new Edge(maze5.verticies.get(5).get(3), maze5.verticies.get(6).get(3), 31, true))));

    for (Edge e : this.maze6.edges) {
      if (e.isWall) {
        connectors3.add(e);
      }
    }

    t.checkExpect(connectors3,
        new ArrayList<Edge>(Arrays.asList(
            new Edge(maze6.verticies.get(1).get(2), maze6.verticies.get(2).get(2), 9, true),
            new Edge(maze6.verticies.get(1).get(2), maze6.verticies.get(1).get(3), 11, true),
            new Edge(maze6.verticies.get(2).get(0), maze6.verticies.get(2).get(1), 11, true),
            new Edge(maze6.verticies.get(1).get(0), maze6.verticies.get(2).get(0), 13, true),
            new Edge(maze6.verticies.get(3).get(3), maze6.verticies.get(3).get(4), 13, true),
            new Edge(maze6.verticies.get(0).get(1), maze6.verticies.get(0).get(2), 14, true),
            new Edge(maze6.verticies.get(1).get(4), maze6.verticies.get(2).get(4), 14, true),
            new Edge(maze6.verticies.get(2).get(2), maze6.verticies.get(3).get(2), 15, true),
            new Edge(maze6.verticies.get(3).get(1), maze6.verticies.get(3).get(2), 15, true),
            new Edge(maze6.verticies.get(1).get(3), maze6.verticies.get(2).get(3), 17, true),
            new Edge(maze6.verticies.get(1).get(3), maze6.verticies.get(1).get(4), 17, true),
            new Edge(maze6.verticies.get(0).get(1), maze6.verticies.get(1).get(1), 18, true))));

    return true;
  }

  // created different methods for make scene tests so it easier to handle
  public boolean testMakeScene1(Tester t) {
    WorldScene world = new WorldScene(600, 600);
    RectangleImage rect1 = new RectangleImage(600, 600, OutlineMode.OUTLINE, Color.black);
    RectangleImage rect2 = new RectangleImage(200, 200, OutlineMode.SOLID, Color.green);
    RectangleImage rect3 = new RectangleImage(200, 200, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect4 = new RectangleImage(200, 200, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect5 = new RectangleImage(200, 200, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect6 = new RectangleImage(200, 200, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect7 = new RectangleImage(200, 200, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect8 = new RectangleImage(200, 200, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect9 = new RectangleImage(200, 200, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect10 = new RectangleImage(200, 200, OutlineMode.SOLID, Color.magenta);

    LineImage line1 = new LineImage(new Posn(200, 0), Color.black);
    LineImage line2 = new LineImage(new Posn(0, 200), Color.black);
    LineImage line3 = new LineImage(new Posn(0, 200), Color.black);
    LineImage line4 = new LineImage(new Posn(200, 0), Color.black);

    world.placeImageXY(rect1, 300, 300);
    world.placeImageXY(rect2, 100, 100);
    world.placeImageXY(rect3, 300, 100);
    world.placeImageXY(rect4, 500, 100);
    world.placeImageXY(rect5, 100, 300);
    world.placeImageXY(rect6, 300, 300);
    world.placeImageXY(rect7, 500, 300);
    world.placeImageXY(rect8, 100, 500);
    world.placeImageXY(rect9, 300, 500);
    world.placeImageXY(rect10, 500, 500);
    world.placeImageXY(line1, 100, 200);
    world.placeImageXY(line2, 200, 500);
    world.placeImageXY(line3, 400, 500);
    world.placeImageXY(line4, 500, 200);

    return t.checkExpect(maze4.makeScene(), world);
  }

  public boolean testMakeScene2(Tester t) {
    WorldScene world = new WorldScene(300, 600);
    RectangleImage rect2 = new RectangleImage(75, 75, OutlineMode.SOLID, Color.green);
    RectangleImage rect3 = new RectangleImage(75, 75, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect4 = new RectangleImage(75, 75, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect5 = new RectangleImage(75, 75, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect6 = new RectangleImage(75, 75, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect7 = new RectangleImage(75, 75, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect8 = new RectangleImage(75, 75, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect9 = new RectangleImage(75, 75, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect10 = new RectangleImage(75, 75, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect11 = new RectangleImage(75, 75, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect12 = new RectangleImage(75, 75, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect13 = new RectangleImage(75, 75, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect14 = new RectangleImage(75, 75, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect15 = new RectangleImage(75, 75, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect16 = new RectangleImage(75, 75, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect17 = new RectangleImage(75, 75, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect18 = new RectangleImage(75, 75, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect19 = new RectangleImage(75, 75, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect20 = new RectangleImage(75, 75, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect21 = new RectangleImage(75, 75, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect22 = new RectangleImage(75, 75, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect23 = new RectangleImage(75, 75, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect24 = new RectangleImage(75, 75, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect25 = new RectangleImage(75, 75, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect26 = new RectangleImage(75, 75, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect27 = new RectangleImage(75, 75, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect28 = new RectangleImage(75, 75, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect29 = new RectangleImage(75, 75, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect30 = new RectangleImage(75, 75, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect31 = new RectangleImage(75, 75, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect32 = new RectangleImage(75, 75, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect33 = new RectangleImage(75, 75, OutlineMode.SOLID, Color.magenta);

    LineImage line1 = new LineImage(new Posn(0, 75), Color.black);
    LineImage line2 = new LineImage(new Posn(75, 0), Color.black);
    LineImage line3 = new LineImage(new Posn(0, 75), Color.black);
    LineImage line4 = new LineImage(new Posn(0, 75), Color.black);
    LineImage line5 = new LineImage(new Posn(0, 75), Color.black);
    LineImage line6 = new LineImage(new Posn(75, 0), Color.black);
    LineImage line7 = new LineImage(new Posn(75, 0), Color.black);
    LineImage line8 = new LineImage(new Posn(75, 0), Color.black);
    LineImage line9 = new LineImage(new Posn(75, 0), Color.black);
    LineImage line10 = new LineImage(new Posn(75, 0), Color.black);
    LineImage line11 = new LineImage(new Posn(75, 0), Color.black);
    LineImage line12 = new LineImage(new Posn(0, 75), Color.black);
    LineImage line13 = new LineImage(new Posn(0, 75), Color.black);
    LineImage line14 = new LineImage(new Posn(0, 75), Color.black);
    LineImage line15 = new LineImage(new Posn(75, 0), Color.black);
    LineImage line16 = new LineImage(new Posn(75, 0), Color.black);
    LineImage line17 = new LineImage(new Posn(0, 75), Color.black);
    LineImage line18 = new LineImage(new Posn(0, 75), Color.black);
    LineImage line19 = new LineImage(new Posn(75, 0), Color.black);
    LineImage line20 = new LineImage(new Posn(75, 0), Color.black);
    LineImage line21 = new LineImage(new Posn(75, 0), Color.black);

    world.placeImageXY(rect2, 37, 37);
    world.placeImageXY(rect3, 112, 37);
    world.placeImageXY(rect4, 187, 37);
    world.placeImageXY(rect5, 262, 37);
    world.placeImageXY(rect6, 37, 112);
    world.placeImageXY(rect7, 112, 112);
    world.placeImageXY(rect8, 187, 112);
    world.placeImageXY(rect9, 262, 112);
    world.placeImageXY(rect10, 37, 187);
    world.placeImageXY(rect11, 112, 187);
    world.placeImageXY(rect12, 187, 187);
    world.placeImageXY(rect13, 262, 187);
    world.placeImageXY(rect14, 37, 262);
    world.placeImageXY(rect15, 112, 262);
    world.placeImageXY(rect16, 187, 262);
    world.placeImageXY(rect17, 262, 262);
    world.placeImageXY(rect18, 37, 337);
    world.placeImageXY(rect19, 112, 337);
    world.placeImageXY(rect20, 187, 337);
    world.placeImageXY(rect21, 262, 337);
    world.placeImageXY(rect22, 37, 412);
    world.placeImageXY(rect23, 112, 412);
    world.placeImageXY(rect24, 187, 412);
    world.placeImageXY(rect25, 262, 412);
    world.placeImageXY(rect26, 37, 487);
    world.placeImageXY(rect27, 112, 487);
    world.placeImageXY(rect28, 187, 487);
    world.placeImageXY(rect29, 262, 487);
    world.placeImageXY(rect30, 37, 562);
    world.placeImageXY(rect31, 112, 562);
    world.placeImageXY(rect32, 187, 562);
    world.placeImageXY(rect33, 262, 562);

    world.placeImageXY(line1, 150, 412);
    world.placeImageXY(line2, 187, 300);
    world.placeImageXY(line3, 75, 562);
    world.placeImageXY(line4, 150, 262);
    world.placeImageXY(line5, 75, 187);
    world.placeImageXY(line6, 262, 300);
    world.placeImageXY(line7, 37, 450);
    world.placeImageXY(line8, 187, 525);
    world.placeImageXY(line9, 37, 75);
    world.placeImageXY(line10, 262, 150);
    world.placeImageXY(line11, 37, 300);
    world.placeImageXY(line12, 75, 337);
    world.placeImageXY(line13, 225, 37);
    world.placeImageXY(line14, 150, 37);
    world.placeImageXY(line15, 187, 225);
    world.placeImageXY(line16, 187, 150);
    world.placeImageXY(line17, 225, 412);
    world.placeImageXY(line18, 150, 487);
    world.placeImageXY(line19, 37, 225);
    world.placeImageXY(line20, 112, 525);
    world.placeImageXY(line21, 262, 450);

    return t.checkExpect(maze5.makeScene(), world);
  }

  public boolean testMakeScene3(Tester t) {
    WorldScene world = new WorldScene(750, 600);
    RectangleImage rect1 = new RectangleImage(300, 600, OutlineMode.OUTLINE, Color.black);
    RectangleImage rect2 = new RectangleImage(150, 150, OutlineMode.SOLID, Color.green);
    RectangleImage rect3 = new RectangleImage(150, 150, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect4 = new RectangleImage(150, 150, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect5 = new RectangleImage(150, 150, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect6 = new RectangleImage(150, 150, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect7 = new RectangleImage(150, 150, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect8 = new RectangleImage(150, 150, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect9 = new RectangleImage(150, 150, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect10 = new RectangleImage(150, 150, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect11 = new RectangleImage(150, 150, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect12 = new RectangleImage(150, 150, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect13 = new RectangleImage(150, 150, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect14 = new RectangleImage(150, 150, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect15 = new RectangleImage(150, 150, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect16 = new RectangleImage(150, 150, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect17 = new RectangleImage(150, 150, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect18 = new RectangleImage(150, 150, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect19 = new RectangleImage(150, 150, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect20 = new RectangleImage(150, 150, OutlineMode.SOLID, Color.lightGray);
    RectangleImage rect21 = new RectangleImage(150, 150, OutlineMode.SOLID, Color.magenta);

    LineImage line1 = new LineImage(new Posn(150, 0), Color.black);
    LineImage line2 = new LineImage(new Posn(0, 150), Color.black);
    LineImage line3 = new LineImage(new Posn(0, 150), Color.black);
    LineImage line4 = new LineImage(new Posn(150, 0), Color.black);
    LineImage line5 = new LineImage(new Posn(0, 150), Color.black);
    LineImage line6 = new LineImage(new Posn(0, 150), Color.black);
    LineImage line7 = new LineImage(new Posn(150, 0), Color.black);
    LineImage line8 = new LineImage(new Posn(150, 0), Color.black);
    LineImage line9 = new LineImage(new Posn(0, 150), Color.black);
    LineImage line10 = new LineImage(new Posn(150, 0), Color.black);
    LineImage line11 = new LineImage(new Posn(0, 150), Color.black);
    LineImage line12 = new LineImage(new Posn(150, 0), Color.black);

    world.placeImageXY(rect1, 375, 300);
    world.placeImageXY(rect2, 75, 75);
    world.placeImageXY(rect3, 225, 75);
    world.placeImageXY(rect4, 375, 75);
    world.placeImageXY(rect5, 525, 75);
    world.placeImageXY(rect6, 675, 75);
    world.placeImageXY(rect7, 75, 225);
    world.placeImageXY(rect8, 225, 225);
    world.placeImageXY(rect9, 375, 225);
    world.placeImageXY(rect10, 525, 225);
    world.placeImageXY(rect11, 675, 225);
    world.placeImageXY(rect12, 75, 375);
    world.placeImageXY(rect13, 225, 375);
    world.placeImageXY(rect14, 375, 375);
    world.placeImageXY(rect15, 525, 375);
    world.placeImageXY(rect16, 675, 375);
    world.placeImageXY(rect17, 75, 525);
    world.placeImageXY(rect18, 225, 525);
    world.placeImageXY(rect19, 375, 525);
    world.placeImageXY(rect20, 525, 525);
    world.placeImageXY(rect21, 675, 525);

    world.placeImageXY(line1, 375, 300);
    world.placeImageXY(line2, 450, 225);
    world.placeImageXY(line3, 150, 375);
    world.placeImageXY(line4, 75, 300);
    world.placeImageXY(line5, 600, 525);
    world.placeImageXY(line6, 300, 75);
    world.placeImageXY(line7, 675, 300);
    world.placeImageXY(line8, 375, 450);
    world.placeImageXY(line9, 300, 525);
    world.placeImageXY(line10, 525, 300);
    world.placeImageXY(line11, 600, 225);
    world.placeImageXY(line12, 225, 150);

    return t.checkExpect(maze6.makeScene(), world);
  }

}
  

  /*
  public HashMap<Vertex, Vertex> BFS() {
    Deque<Vertex> q = new LinkedList<Vertex>();
    HashMap<Vertex, Boolean> visited = new HashMap<Vertex, Boolean>();
    HashMap<Vertex, Vertex> prev = new HashMap<Vertex, Vertex>();
    
    for(ArrayList<Vertex> row: this.verticies) {
      for(Vertex curr: row) {
        visited.put(curr, false);
      }
    }
    
    for(ArrayList<Vertex> row: this.verticies) {
      for(Vertex curr: row) {
        prev.put(curr, null);
      }
    }
    
    q.add(this.verticies.get(0).get(0));   
    visited.put(this.verticies.get(0).get(0), true);
    
    while (!( q.size() == 0) && !((q.peekFirst().x == this.size.x - 1) && 
        (q.peekFirst().y == this.size.y - 1))) {
      Vertex vert = q.pollFirst();
      List<Vertex> neighbors = new ArrayList<Vertex>(
          Arrays.asList(vert.left, vert.right, vert.top, vert.bottom));
      
      neighbors.removeIf(s -> vert.isBlocked(s, this));
      
      // **** possibly remove the != null
      for (Vertex next: neighbors) {
        if(next != null && !visited.get(next)) {
          q.add(next);
          visited.put(next, true);
          next.c = Color.cyan;
          prev.put(next, vert);
        }
      }
    }
    return prev;
    
  }
  
  public void drawPath(HashMap<Vertex, Vertex> prev, Vertex start, Vertex end) {
    for(Vertex curr = end; curr != null; curr = prev.get(curr)) {
      curr.c = Color.BLUE;
    }
  }
  
  
  public void onKeyEvent(String key) {
    if (key.equals("b")) {
      Vertex start = this.verticies.get(0).get(0);
      Vertex end = this.verticies.get(this.size.y-1).get(this.size.x-1);
      HashMap<Vertex, Vertex> prev = this.BFS();
      this.drawPath(prev, start, end);
    }
  }
  
  */

