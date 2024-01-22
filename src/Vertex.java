import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Collections;
import java.util.ArrayDeque;
import java.util.function.BiFunction;

import javalib.impworld.*;
import javalib.worldimages.*;
import java.awt.Color;

import tester.*;

/*
 * User Documentation:
 * 
 * To start the maze solving you can press d for depth first search or b for bredth first search
 * or press p to walk through it yourself
 * 
 * If playing manually you can use the arrow keys or wasd as your controls
 * when you want to exit this mode press p again and you have the same three options as before
 * 
 * To create a new random maze in the middle of the maze press r and a random maze will be made
 * 
 */

class Maze extends World {
  private ArrayList<ArrayList<Vertex>> vertices;
  private ArrayList<Edge> minTree;
  private final int width;
  private final int height;

  private final HashSet<Vertex> alreadySeen;
  // TODO replace already seen with a hash set and make sure we add 
  // and clear the solution with the alreadySeen
  private final ArrayDeque<Vertex> solution;
  private final ArrayDeque<Vertex> worklist;

  private String mode;

  private Vertex current;

  private final Random random;

  Maze(int width, int height, Random random) {
    this.random = random;

    this.createMaze(width, height);

    // this.minTree = this.createMinTree();
    this.width = width;
    this.height = height;

    this.current = this.vertices.get(0).get(0);
    this.alreadySeen = new HashSet<Vertex>();
    this.solution = new ArrayDeque<Vertex>();
    this.worklist = new ArrayDeque<Vertex>();
    this.worklist.addFirst(current);

    this.mode = "";

  }

  Maze(int width, int height) {
    this(width, height, new Random());

  }

  // resets the maze to create a new random maze
  public void resetMaze() {

    this.createMaze(width, height);

    this.alreadySeen.clear();
    this.solution.clear();
    this.worklist.clear();
    this.worklist.addFirst(this.vertices.get(0).get(0));
  }

  // resets the color of the maze
  public void makeGrey() {
    for (ArrayList<Vertex> arr : this.vertices) {
      for (Vertex ver : arr) {
        if (ver.xPos == 0 && ver.yPos == 0) {
          ver.color = Color.GREEN;
        }
        else if (ver.xPos == this.width - 1 && ver.yPos == this.height - 1) {
          ver.color = Color.MAGENTA;
        }
        else {
          ver.color = Color.LIGHT_GRAY;
        }
      }
    }
  }

  // handles the key events
  public void onKeyEvent(String key) {

    if (key.equals("r") || key.equals("R")) {
      this.resetMaze();
    }

    if (key.equals("p") || key.equals("P")) {
      if (this.mode.equals("p")) {
        this.mode = "";
        this.makeGrey();

        this.alreadySeen.clear();
        this.solution.clear();
        this.worklist.clear();
        this.worklist.add(this.vertices.get(0).get(0));
        this.current = this.vertices.get(0).get(0);

        this.current.color = Color.green;
      }

      else {

        this.mode = "p";

        this.makeGrey();

        this.alreadySeen.clear();
        this.solution.clear();
        this.worklist.clear();
        this.worklist.add(this.vertices.get(0).get(0));
        this.current = this.vertices.get(0).get(0);

        this.current.color = Color.red;
        this.alreadySeen.add(current);
        this.solution.add(current);
      }

    }

    if (key.equals("w") || key.equals("W") || key.equals("up")) {
      if (this.mode.equals("p")) {
        this.movePlayer("up");
      }

    }

    else if (key.equals("a") || key.equals("A") || key.equals("left")) {
      if (this.mode.equals("p")) {
        this.movePlayer("left");
      }

      else {
        this.movePlayer("left");
      }
    }

    else if (key.equals("s") || key.equals("S") || key.equals("down")) {
      if (this.mode.equals("p")) {
        this.movePlayer("down");
      }

    }

    else if (key.equals("d") || key.equals("D") || key.equals("right")) {
      if (!this.mode.equals("p") && ((key.equals("d") || key.equals("D")))) {
        this.mode = "d";
      }

      else if (this.mode.equals("p")) {
        this.movePlayer("right");
      }

    }

    else if (key.equals("b") || key.equals("B")) {
      if (!this.mode.equals("p")) {
        this.mode = "b";
      }

    }

  }

  // moves the player given the key input
  public void movePlayer(String key) {
    if (key.equals("up")) {
      if (new CanMove(this.minTree, this.vertices).apply(current, key)) {
        this.current.color = new Color(150, 150, 250);
        this.current = this.vertices.get(current.xPos).get(current.yPos - 1);
        this.current.color = Color.red;
      }
    }

    if (key.equals("down")) {
      if (new CanMove(this.minTree, this.vertices).apply(current, key)) {
        this.current.color = new Color(150, 150, 250);
        this.current = this.vertices.get(current.xPos).get(current.yPos + 1);
        this.current.color = Color.red;

      }
    }

    if (key.equals("right")) {
      if (new CanMove(this.minTree, this.vertices).apply(current, key)) {
        this.current.color = new Color(150, 150, 250);
        this.current = this.vertices.get(current.xPos + 1).get(current.yPos);
        this.current.color = Color.red;
      }
    }

    if (key.equals("left")) {
      if (new CanMove(this.minTree, this.vertices).apply(current, key)) {
        this.current.color = new Color(150, 150, 250);
        this.current = this.vertices.get(current.xPos - 1).get(current.yPos);
        this.current.color = Color.red;
      }
    }

    if (!this.alreadySeen.contains(current)) {
      this.solution.addFirst(current);
      this.alreadySeen.add(current);
    }

    if (current.xPos == this.width - 1 && current.yPos == this.height - 1) {
      this.getSolution();
      this.mode = "";
    }
  }

  // creates the maze including the vertices and the min tree
  public void createMaze(int width, int height) {
    ArrayList<ArrayList<Vertex>> result = new ArrayList<>();
    for (int i = 0; i < width; i += 1) {
      result.add(new ArrayList<Vertex>());
      for (int j = 0; j < height; j += 1) {
        if (i == width - 1 && j == height - 1) {
          result.get(i).add(new Vertex(i, j, Color.magenta));
        }
        else if (i == 0 && j == 0) {
          result.get(i).add(new Vertex(i, j, Color.green));
        }
        else {
          result.get(i).add(new Vertex(i, j));
        }

      }
    }

    this.vertices = result;

    this.minTree = this.createMinTree(this.createEdges(result));

  }

  // creates edges between all the vertices pointing only right and down with
  // random weights
  public ArrayList<Edge> createEdges(ArrayList<ArrayList<Vertex>> graph) {
    ArrayList<Edge> outEdges = new ArrayList<>();
    for (int i = 0; i < graph.size(); i += 1) {
      for (int j = 0; j < graph.get(i).size(); j += 1) {
        Vertex cur = graph.get(i).get(j);
        if (j == graph.get(i).size() - 1 && i == graph.size() - 1) {
          // case where its in the bottom right so no more edges need to be made
        }
        else if (i == graph.size() - 1) {
          Vertex down = graph.get(i).get(j + 1);
          outEdges.add(new Edge(this.random.nextInt(10000), down, cur));
        }

        else if (j == graph.get(i).size() - 1) {
          Vertex right = graph.get(i + 1).get(j);
          outEdges.add(new Edge(this.random.nextInt(10000), right, cur));
        }

        else {
          Vertex down = graph.get(i).get(j + 1);
          outEdges.add(new Edge(this.random.nextInt(10000), down, cur));

          Vertex right = graph.get(i + 1).get(j);
          outEdges.add(new Edge(this.random.nextInt(10000), right, cur));

        }
      }
    }

    return outEdges;
  }

  // creates a min spanning tree given the current graph of vertices
  public ArrayList<Edge> createMinTree(ArrayList<Edge> worklist) {
    Collections.sort(worklist);
    HashMap<Vertex, Vertex> rep = new HashMap<>();
    ArrayList<Edge> treeEdges = new ArrayList<>();
    int currentEdge = 0;

    for (Edge e : worklist) {
      rep.put(e.to, e.to);
      rep.put(e.from, e.from);
    }

    while (this.incompleteTree(rep)) { // && currentEdge <
      // worklist.size()) {
      Vertex next = worklist.get(currentEdge).to;
      Vertex nextFrom = worklist.get(currentEdge).from;

      if (this.find(rep, next).equals(this.find(rep, nextFrom))) {
        currentEdge += 1;
      }

      else {
        this.union(rep, this.find(rep, next), this.find(rep, nextFrom));
        treeEdges.add(worklist.get(currentEdge));
        nextFrom.outEdges.add(worklist.get(currentEdge));
        next.outEdges.add(worklist.get(currentEdge));

      }
    }

    return treeEdges;
  }

  // combines two trees reps
  public void union(HashMap<Vertex, Vertex> rep, Vertex rep1, Vertex rep2) {
    rep.put(rep1, rep2);
  }

  // finds the end rep of the given node
  public Vertex find(HashMap<Vertex, Vertex> rep, Vertex v) {
    Vertex current = rep.get(v);

    while (current != rep.get(current)) {
      current = rep.get(current);
    }

    return current;
  }

  // checks if the miniumum spanning tree is incomplete
  public boolean incompleteTree(HashMap<Vertex, Vertex> rep) {
    Vertex parent = this.find(rep, this.vertices.get(0).get(0));

    for (ArrayList<Vertex> arr : this.vertices) {
      for (Vertex ver : arr) {
        if (!parent.equals(this.find(rep, ver))) {
          return true;
        }
      }
    }
    return false;
  }

  // draws the maze
  public WorldImage draw() {
    WorldImage image = new EmptyImage();
    for (int i = 0; i < height; i += 1) {
      WorldImage row = new EmptyImage();
      // result.add(new ArrayList<Vertex>());
      for (int j = 0; j < width; j += 1) {

        Color curColor = this.vertices.get(j).get(i).color;
        WorldImage square = new RectangleImage(8, 8, OutlineMode.SOLID, curColor);
        WorldImage bottom = new EmptyImage();

        if (!this.containsEdge(new Vertex(j, i), new Vertex(j + 1, i))) {
          square = new BesideImage(square, new RectangleImage(2, 8, OutlineMode.SOLID, Color.gray));
          bottom = new BesideImage(new RectangleImage(8, 2, OutlineMode.SOLID, curColor),
              new RectangleImage(2, 2, OutlineMode.SOLID, Color.gray));
        }
        else {
          square = new BesideImage(square, new RectangleImage(2, 8, OutlineMode.SOLID, curColor));
          bottom = new BesideImage(new RectangleImage(10, 2, OutlineMode.SOLID, curColor));
        }

        if (!this.containsEdge(new Vertex(j, i), new Vertex(j, i + 1))) {
          square = new AboveImage(square, new RectangleImage(10, 2, OutlineMode.SOLID, Color.gray));
        }
        else {
          square = new AboveImage(square, bottom);
        }

        row = new BesideImage(row, square);

      }
      image = new AboveImage(image, row);
    }
    image = new BesideImage(new RectangleImage(2, 10 * this.height, OutlineMode.SOLID, Color.gray),
        image);
    image = new AboveImage(new RectangleImage(10 * this.width, 2, OutlineMode.SOLID, Color.gray),
        image);
    return image;
  }

  // checks to see if the edge is in the minimum spanning tree
  // TODO Look into this method and drawing to speed things up
  public boolean containsEdge(Vertex testFrom, Vertex testTo) {
    for (Edge e : minTree) {
      if (e.to.xPos == testTo.xPos && e.to.yPos == testTo.yPos && e.from.xPos == testFrom.xPos
          && e.from.yPos == testFrom.yPos) {
        return true;
      }
    }
    return false;
  }

  // creates the scene for the maze
  public WorldScene makeScene() {
    WorldScene s = new WorldScene(width * 10, height * 10);
    s.placeImageXY(this.draw(), 10 * width / 2, 10 * height / 2);
    return s;
  }

  // does one iteration of depth first search in the maze
  public void depthSearch() {

    if (this.worklist.size() == 0) {
      return;
    }

    Vertex next = this.worklist.removeFirst();
    next.color = new Color(150, 150, 250);

    if (this.alreadySeen.contains(next)) {
      this.depthSearch();
    }

    else {
      for (Edge e : next.outEdges) {
        worklist.addFirst(e.from);
        worklist.addFirst(e.to);
      }
      // add next to alreadySeen, since we're done with it
      this.alreadySeen.add(next);
      this.solution.addFirst(next);
    }


    if (next.xPos == this.width - 1 && next.yPos == this.height - 1) {
      this.worklist.clear();
      this.alreadySeen.add(next);
      this.solution.addFirst(next);
      this.getSolution();
    }

  }

  // does one iteration of bredth first search in the maze
  public void bredthSearch() {
    if (this.worklist.size() == 0) {
      return;
    }

    Vertex next = this.worklist.removeFirst();
    next.color = new Color(150, 150, 250);
    // }

    if (this.alreadySeen.contains(next)) {
      this.bredthSearch();
    }

    else {
      for (Edge e : next.outEdges) {
        worklist.addLast(e.to);
        worklist.addLast(e.from);

      }
      // add next to alreadySeen, since we're done with it
      this.alreadySeen.add(next);
      this.solution.addFirst(next);
    }

    if (next.xPos == this.width - 1 && next.yPos == this.height - 1) {

      this.alreadySeen.add(next);
      this.solution.addFirst(next);
      this.worklist.clear();
      this.getSolution();
    }

  }

  // paints the path to the end blue, showing the solution
  public void getSolution() {

    Vertex last = this.solution.removeFirst();
    last.color = Color.blue;

    while (this.solution.size() > 0) {
      Vertex nextLast = this.solution.removeFirst();
      if (this.containsEdge(nextLast, last) || this.containsEdge(last, nextLast)) {
        nextLast.color = Color.blue;
        last = nextLast;
      }
    }

  }

  // handles the world with each tick
  public void onTick() {
    if (this.mode.equals("d")) {
      this.depthSearch();
    }
    else if (this.mode.equals("b")) {
      this.bredthSearch();// false, 0, 0);
    }

  }

}

// function object to determine if the player can move a certain direction
class CanMove implements BiFunction<Vertex, String, Boolean> {
  private ArrayList<Edge> minTree;
  private ArrayList<ArrayList<Vertex>> vertices;

  CanMove(ArrayList<Edge> minTree, ArrayList<ArrayList<Vertex>> vertices) {
    this.minTree = minTree;
    this.vertices = vertices;
  }

  // determines if the player can move a certain direction
  public Boolean apply(Vertex t, String u) {
    if (u.equals("w") || u.equals("W") || u.equals("up")) {
      if (t.yPos > 0) {
        return new HasEdge(this.minTree).apply(this.vertices.get(t.xPos).get(t.yPos - 1), t);
      }
      else {
        return false;
      }
    }

    else if (u.equals("a") || u.equals("A") || u.equals("left")) {
      if (t.xPos > 0) {
        return new HasEdge(this.minTree).apply(this.vertices.get(t.xPos - 1).get(t.yPos), t);
      }

      else {
        return false;
      }
    }

    else if (u.equals("s") || u.equals("S") || u.equals("down")) {
      if (t.yPos < this.vertices.get(0).size() - 1) {
        return new HasEdge(this.minTree).apply(t, this.vertices.get(t.xPos).get(t.yPos + 1));
      }

      else {
        return false;
      }
    }

    else if (u.equals("d") || u.equals("D") || u.equals("right")) {
      if (t.xPos < this.vertices.size() - 1) {
        return new HasEdge(this.minTree).apply(t, this.vertices.get(t.xPos + 1).get(t.yPos));
      }

      else {
        return false;
      }
    }

    else {
      return false;
    }

  }

}

// function object to determine if a list of edges as an edge
class HasEdge implements BiFunction<Vertex, Vertex, Boolean> {
  private ArrayList<Edge> minTree;

  HasEdge(ArrayList<Edge> minTree) {
    this.minTree = minTree;
  }

  // determines if a list of edges has an edge
  public Boolean apply(Vertex from, Vertex to) {
    for (Edge e : minTree) {
      if (e.to.equals(to) && e.from.equals(from)) {
        return true;
      }
    }

    return false;
  }

}

// class to represent a vertex of a graph
class Vertex {
  // These fields are public since when working with a graph you need the x y
  // coordinates of the vertices
  final int xPos;
  final int yPos;

  // This field is public since there needs to be some way for the main class to
  // update the status of a vertex like if it has been visited or not or if its in
  // the final solution
  Color color;

  // This field is public since when working with graphs you need to see where
  // each vertex is going to especially when it comes to searching
  ArrayList<Edge> outEdges;

  Vertex(int xPos, int yPos, Color color) {
    this.xPos = xPos;
    this.yPos = yPos;
    this.color = Color.lightGray;
    this.outEdges = new ArrayList<>();
    this.color = color;
  }

  Vertex(int xPos, int yPos) {
    this(xPos, yPos, Color.LIGHT_GRAY);
  }
}

// class to represent an edge in a graph
class Edge implements Comparable<Edge> {
  private final int weight;
  // These methods are not private since when dealing with a graph the classes
  // using the graph
  // need to know where the edges point to and from other
  final Vertex to;
  final Vertex from;

  Edge(int weight, Vertex to, Vertex from) {
    this.weight = weight;
    this.from = from;
    this.to = to;
  }

  // method to compare two edges
  public int compareTo(Edge o) {
    if (this.weight > o.weight) {
      return 1;
    }

    else if (this.weight < o.weight) {
      return -1;
    }
    else {
      return 0;
    }
  }
}

class ExamplePixels {
  Maze test3x3;
  Maze test5x10;
  Maze test3x3Seed;
  FrozenImage t3x3SImage;

  void initMaze() {
    test3x3 = new Maze(3, 3);
    test5x10 = new Maze(5, 10);
    test3x3Seed = new Maze(3, 3, new Random(5));
    t3x3SImage = new FrozenImage(test3x3Seed.draw());

  }

  void testCreateMinTree(Tester t) {
    // We can't test this method since it is void and effects a field that is
    // private
    // There is also no way to test this method since it is a method that is apart
    // of initalizing the class
    // It is meant to be under the hood for no one to see and since it is like that
    // its hard to test its effect
    // this method is also tested as a side effect of testing other methods
  }

  void testResetMaze(Tester t) {
    this.initMaze();

    WorldScene initialMaze = this.test5x10.makeScene();

    for (int x = 0; x < 200; x += 1) {
      test5x10.depthSearch();
    }

    WorldScene depthSearchedMaze = this.test5x10.makeScene();

    test5x10.resetMaze();

    WorldScene resetMaze = this.test5x10.makeScene();

    t.checkExpect(!initialMaze.equals(depthSearchedMaze), true);
    t.checkExpect(!resetMaze.equals(depthSearchedMaze), true);
    t.checkExpect(!initialMaze.equals(resetMaze), true);
    t.checkExpect(initialMaze.width, resetMaze.width);
    t.checkExpect(initialMaze.height, resetMaze.height);
    t.checkExpect(initialMaze.width, depthSearchedMaze.width);
    t.checkExpect(initialMaze.height, depthSearchedMaze.height);

  }

  void testMakeGrey(Tester t) {
    this.initMaze();

    WorldScene initialMaze = this.test3x3.makeScene();

    for (int x = 0; x < 50; x += 1) {
      test3x3.depthSearch();
    }

    WorldScene depthSearchedMaze = this.test3x3.makeScene();

    test3x3.makeGrey();

    WorldScene greyedMaze = this.test3x3.makeScene();

    t.checkExpect(!initialMaze.equals(depthSearchedMaze), true);
    t.checkExpect(!greyedMaze.equals(depthSearchedMaze), true);
    t.checkExpect(initialMaze, greyedMaze);

  }

  void testOnKeyEvent(Tester t) {
    this.initMaze();

    WorldScene initialMaze = this.test5x10.makeScene();

    test5x10.onKeyEvent("r");

    WorldScene resetMaze = this.test5x10.makeScene();

    t.checkExpect(!initialMaze.equals(resetMaze), true);
    t.checkExpect(initialMaze.width, resetMaze.width);
    t.checkExpect(initialMaze.height, resetMaze.height);

    this.initMaze();

    initialMaze = this.test5x10.makeScene();

    test5x10.onKeyEvent("right");
    test5x10.onKeyEvent("down");

    WorldScene moveMaze = this.test5x10.makeScene();

    t.checkExpect(initialMaze, moveMaze);

    test5x10.onKeyEvent("p");

    WorldScene initialMoveMaze = this.test5x10.makeScene();

    t.checkExpect(!initialMoveMaze.equals(initialMaze), true);
    t.checkExpect(initialMoveMaze.width, initialMaze.width);
    t.checkExpect(initialMoveMaze.height, initialMaze.height);

    test5x10.onKeyEvent("right");
    test5x10.onKeyEvent("down");

    moveMaze = this.test5x10.makeScene();

    t.checkExpect(!initialMoveMaze.equals(moveMaze), true);

    test5x10.onKeyEvent("up");
    test5x10.onKeyEvent("left");

    WorldScene moveMaze2 = this.test5x10.makeScene();

    t.checkExpect(!initialMoveMaze.equals(moveMaze2), true);
    t.checkExpect(!moveMaze.equals(moveMaze2), true);

    test5x10.onKeyEvent("p");

    WorldScene initialMoveMaze2 = this.test5x10.makeScene();

    t.checkExpect(!initialMoveMaze2.equals(moveMaze2), true);
    t.checkExpect(!initialMoveMaze2.equals(moveMaze), true);
    t.checkExpect(!initialMoveMaze2.equals(initialMoveMaze), true);
    t.checkExpect(initialMoveMaze2, initialMaze);

  }

  void testMovePlayer(Tester t) {

    this.initMaze();

    WorldScene initialMaze = this.test5x10.makeScene();

    test5x10.onKeyEvent("right");
    test5x10.onKeyEvent("down");

    WorldScene moveMaze = this.test5x10.makeScene();

    t.checkExpect(initialMaze, moveMaze);

    test5x10.onKeyEvent("p");

    WorldScene initialMoveMaze = this.test5x10.makeScene();

    t.checkExpect(!initialMoveMaze.equals(initialMaze), true);
    t.checkExpect(initialMoveMaze.width, initialMaze.width);
    t.checkExpect(initialMoveMaze.height, initialMaze.height);

    test5x10.onKeyEvent("right");
    test5x10.onKeyEvent("down");

    moveMaze = this.test5x10.makeScene();

    t.checkExpect(!initialMoveMaze.equals(moveMaze), true);

    test5x10.onKeyEvent("up");
    test5x10.onKeyEvent("left");

    WorldScene moveMaze2 = this.test5x10.makeScene();

    t.checkExpect(!initialMoveMaze.equals(moveMaze2), true);
    t.checkExpect(!moveMaze.equals(moveMaze2), true);

    test5x10.onKeyEvent("p");

    WorldScene initialMoveMaze2 = this.test5x10.makeScene();

    t.checkExpect(!initialMoveMaze2.equals(moveMaze2), true);
    t.checkExpect(!initialMoveMaze2.equals(moveMaze), true);
    t.checkExpect(!initialMoveMaze2.equals(initialMoveMaze), true);
    t.checkExpect(initialMoveMaze2, initialMaze);

  }

  void testContainsEdge(Tester t) {

    this.initMaze();

    t.checkExpect(test3x3.containsEdge(new Vertex(0, 0), new Vertex(0, 1))
        || test3x3.containsEdge(new Vertex(0, 0), new Vertex(1, 0)), true);
    t.checkExpect(test3x3.containsEdge(new Vertex(0, 0), new Vertex(0, 2)), false);
    t.checkExpect(test3x3.containsEdge(new Vertex(1, 1), new Vertex(23, 11)), false);
    t.checkExpect(test5x10.containsEdge(new Vertex(2, 7), new Vertex(2, 8))
        || test5x10.containsEdge(new Vertex(2, 7), new Vertex(3, 7))
        || test5x10.containsEdge(new Vertex(1, 7), new Vertex(2, 7))
        || test5x10.containsEdge(new Vertex(2, 6), new Vertex(2, 7)), true);
    t.checkExpect(test5x10.containsEdge(new Vertex(4, 8), new Vertex(4, 9))
        || test5x10.containsEdge(new Vertex(3, 9), new Vertex(4, 9)), true);

    test3x3.resetMaze();
    test5x10.resetMaze();

    t.checkExpect(test3x3.containsEdge(new Vertex(0, 0), new Vertex(0, 1))
        || test3x3.containsEdge(new Vertex(0, 0), new Vertex(1, 0)), true);
    t.checkExpect(test3x3.containsEdge(new Vertex(0, 0), new Vertex(0, 2)), false);
    t.checkExpect(test3x3.containsEdge(new Vertex(1, 1), new Vertex(23, 11)), false);
    t.checkExpect(test5x10.containsEdge(new Vertex(2, 7), new Vertex(2, 8))
        || test5x10.containsEdge(new Vertex(2, 7), new Vertex(3, 7))
        || test5x10.containsEdge(new Vertex(1, 7), new Vertex(2, 7))
        || test5x10.containsEdge(new Vertex(2, 6), new Vertex(2, 7)), true);
    t.checkExpect(test5x10.containsEdge(new Vertex(4, 8), new Vertex(4, 9))
        || test5x10.containsEdge(new Vertex(3, 9), new Vertex(4, 9)), true);

  }

  void testDepthSearch(Tester t) {
    this.initMaze();

    Color start = Color.green;

    Color finish = Color.magenta;

    Color touched = new Color(150, 150, 250);

    Color empty = Color.LIGHT_GRAY;

    Color done = Color.blue;

    // column1
    t.checkExpect(this.t3x3SImage.getColorAt(5, 5), start);

    t.checkExpect(this.t3x3SImage.getColorAt(5, 15), empty);

    t.checkExpect(this.t3x3SImage.getColorAt(5, 25), empty);

    // column 2
    t.checkExpect(this.t3x3SImage.getColorAt(15, 5), empty);

    t.checkExpect(this.t3x3SImage.getColorAt(15, 15), empty);

    t.checkExpect(this.t3x3SImage.getColorAt(15, 25), empty);

    // column 3
    t.checkExpect(this.t3x3SImage.getColorAt(25, 5), empty);

    t.checkExpect(this.t3x3SImage.getColorAt(25, 15), empty);

    t.checkExpect(this.t3x3SImage.getColorAt(25, 25), finish);

    test3x3Seed.depthSearch();

    this.t3x3SImage = new FrozenImage(test3x3Seed.draw());

    // column1
    t.checkExpect(this.t3x3SImage.getColorAt(5, 5), touched);

    t.checkExpect(this.t3x3SImage.getColorAt(5, 15), empty);

    t.checkExpect(this.t3x3SImage.getColorAt(5, 25), empty);

    // column 2
    t.checkExpect(this.t3x3SImage.getColorAt(15, 5), empty);

    t.checkExpect(this.t3x3SImage.getColorAt(15, 15), empty);

    t.checkExpect(this.t3x3SImage.getColorAt(15, 25), empty);

    // column 3
    t.checkExpect(this.t3x3SImage.getColorAt(25, 5), empty);

    t.checkExpect(this.t3x3SImage.getColorAt(25, 15), empty);

    t.checkExpect(this.t3x3SImage.getColorAt(25, 25), finish);

    test3x3Seed.depthSearch();

    this.t3x3SImage = new FrozenImage(test3x3Seed.draw());

    // column1
    t.checkExpect(this.t3x3SImage.getColorAt(5, 5), touched);

    t.checkExpect(this.t3x3SImage.getColorAt(5, 15), empty);

    t.checkExpect(this.t3x3SImage.getColorAt(5, 25), empty);

    // column 2
    t.checkExpect(this.t3x3SImage.getColorAt(15, 5), touched);

    t.checkExpect(this.t3x3SImage.getColorAt(15, 15), empty);

    t.checkExpect(this.t3x3SImage.getColorAt(15, 25), empty);

    // column 3
    t.checkExpect(this.t3x3SImage.getColorAt(25, 5), empty);

    t.checkExpect(this.t3x3SImage.getColorAt(25, 15), empty);

    t.checkExpect(this.t3x3SImage.getColorAt(25, 25), finish);

    test3x3Seed.depthSearch();

    this.t3x3SImage = new FrozenImage(test3x3Seed.draw());

    // column1
    t.checkExpect(this.t3x3SImage.getColorAt(5, 5), touched);

    t.checkExpect(this.t3x3SImage.getColorAt(5, 15), empty);

    t.checkExpect(this.t3x3SImage.getColorAt(5, 25), empty);

    // column 2
    t.checkExpect(this.t3x3SImage.getColorAt(15, 5), touched);

    t.checkExpect(this.t3x3SImage.getColorAt(15, 15), touched);

    t.checkExpect(this.t3x3SImage.getColorAt(15, 25), empty);

    // column 3
    t.checkExpect(this.t3x3SImage.getColorAt(25, 5), empty);

    t.checkExpect(this.t3x3SImage.getColorAt(25, 15), empty);

    t.checkExpect(this.t3x3SImage.getColorAt(25, 25), finish);

    test3x3Seed.depthSearch();

    this.t3x3SImage = new FrozenImage(test3x3Seed.draw());

    // column1
    t.checkExpect(this.t3x3SImage.getColorAt(5, 5), touched);

    t.checkExpect(this.t3x3SImage.getColorAt(5, 15), empty);

    t.checkExpect(this.t3x3SImage.getColorAt(5, 25), empty);

    // column 2
    t.checkExpect(this.t3x3SImage.getColorAt(15, 5), touched);

    t.checkExpect(this.t3x3SImage.getColorAt(15, 15), touched);

    t.checkExpect(this.t3x3SImage.getColorAt(15, 25), touched);

    // column 3
    t.checkExpect(this.t3x3SImage.getColorAt(25, 5), empty);

    t.checkExpect(this.t3x3SImage.getColorAt(25, 15), empty);

    t.checkExpect(this.t3x3SImage.getColorAt(25, 25), finish);

    test3x3Seed.depthSearch();

    this.t3x3SImage = new FrozenImage(test3x3Seed.draw());

    // column1
    t.checkExpect(this.t3x3SImage.getColorAt(5, 5), done);

    t.checkExpect(this.t3x3SImage.getColorAt(5, 15), empty);

    t.checkExpect(this.t3x3SImage.getColorAt(5, 25), empty);

    // column 2
    t.checkExpect(this.t3x3SImage.getColorAt(15, 5), done);

    t.checkExpect(this.t3x3SImage.getColorAt(15, 15), done);

    t.checkExpect(this.t3x3SImage.getColorAt(15, 25), done);

    // column 3
    t.checkExpect(this.t3x3SImage.getColorAt(25, 5), empty);

    t.checkExpect(this.t3x3SImage.getColorAt(25, 15), empty);

    t.checkExpect(this.t3x3SImage.getColorAt(25, 25), done);

  }

  void testBredthSearch(Tester t) {
    this.initMaze();

    Color start = Color.green;

    Color finish = Color.magenta;

    Color touched = new Color(150, 150, 250);

    Color empty = Color.LIGHT_GRAY;

    Color done = Color.blue;

    // column1
    t.checkExpect(this.t3x3SImage.getColorAt(5, 5), start);

    t.checkExpect(this.t3x3SImage.getColorAt(5, 15), empty);

    t.checkExpect(this.t3x3SImage.getColorAt(5, 25), empty);

    // column 2
    t.checkExpect(this.t3x3SImage.getColorAt(15, 5), empty);

    t.checkExpect(this.t3x3SImage.getColorAt(15, 15), empty);

    t.checkExpect(this.t3x3SImage.getColorAt(15, 25), empty);

    // column 3
    t.checkExpect(this.t3x3SImage.getColorAt(25, 5), empty);

    t.checkExpect(this.t3x3SImage.getColorAt(25, 15), empty);

    t.checkExpect(this.t3x3SImage.getColorAt(25, 25), finish);

    test3x3Seed.bredthSearch();

    this.t3x3SImage = new FrozenImage(test3x3Seed.draw());

    // column1
    t.checkExpect(this.t3x3SImage.getColorAt(5, 5), touched);

    t.checkExpect(this.t3x3SImage.getColorAt(5, 15), empty);

    t.checkExpect(this.t3x3SImage.getColorAt(5, 25), empty);

    // column 2
    t.checkExpect(this.t3x3SImage.getColorAt(15, 5), empty);

    t.checkExpect(this.t3x3SImage.getColorAt(15, 15), empty);

    t.checkExpect(this.t3x3SImage.getColorAt(15, 25), empty);

    // column 3
    t.checkExpect(this.t3x3SImage.getColorAt(25, 5), empty);

    t.checkExpect(this.t3x3SImage.getColorAt(25, 15), empty);

    t.checkExpect(this.t3x3SImage.getColorAt(25, 25), finish);

    test3x3Seed.bredthSearch();

    this.t3x3SImage = new FrozenImage(test3x3Seed.draw());

    // column1
    t.checkExpect(this.t3x3SImage.getColorAt(5, 5), touched);

    t.checkExpect(this.t3x3SImage.getColorAt(5, 15), empty);

    t.checkExpect(this.t3x3SImage.getColorAt(5, 25), empty);

    // column 2
    t.checkExpect(this.t3x3SImage.getColorAt(15, 5), touched);

    t.checkExpect(this.t3x3SImage.getColorAt(15, 15), empty);

    t.checkExpect(this.t3x3SImage.getColorAt(15, 25), empty);

    // column 3
    t.checkExpect(this.t3x3SImage.getColorAt(25, 5), empty);

    t.checkExpect(this.t3x3SImage.getColorAt(25, 15), empty);

    t.checkExpect(this.t3x3SImage.getColorAt(25, 25), finish);

    test3x3Seed.bredthSearch();

    this.t3x3SImage = new FrozenImage(test3x3Seed.draw());

    // column1
    t.checkExpect(this.t3x3SImage.getColorAt(5, 5), touched);

    t.checkExpect(this.t3x3SImage.getColorAt(5, 15), empty);

    t.checkExpect(this.t3x3SImage.getColorAt(5, 25), empty);

    // column 2
    t.checkExpect(this.t3x3SImage.getColorAt(15, 5), touched);

    t.checkExpect(this.t3x3SImage.getColorAt(15, 15), touched);

    t.checkExpect(this.t3x3SImage.getColorAt(15, 25), empty);

    // column 3
    t.checkExpect(this.t3x3SImage.getColorAt(25, 5), empty);

    t.checkExpect(this.t3x3SImage.getColorAt(25, 15), empty);

    t.checkExpect(this.t3x3SImage.getColorAt(25, 25), finish);

    test3x3Seed.bredthSearch();

    this.t3x3SImage = new FrozenImage(test3x3Seed.draw());

    // column1
    t.checkExpect(this.t3x3SImage.getColorAt(5, 5), touched);

    t.checkExpect(this.t3x3SImage.getColorAt(5, 15), empty);

    t.checkExpect(this.t3x3SImage.getColorAt(5, 25), empty);

    // column 2
    t.checkExpect(this.t3x3SImage.getColorAt(15, 5), touched);

    t.checkExpect(this.t3x3SImage.getColorAt(15, 15), touched);

    t.checkExpect(this.t3x3SImage.getColorAt(15, 25), touched);

    // column 3
    t.checkExpect(this.t3x3SImage.getColorAt(25, 5), empty);

    t.checkExpect(this.t3x3SImage.getColorAt(25, 15), empty);

    t.checkExpect(this.t3x3SImage.getColorAt(25, 25), finish);

    test3x3Seed.bredthSearch();

    this.t3x3SImage = new FrozenImage(test3x3Seed.draw());

    // column1
    t.checkExpect(this.t3x3SImage.getColorAt(5, 5), touched);

    t.checkExpect(this.t3x3SImage.getColorAt(5, 15), empty);

    t.checkExpect(this.t3x3SImage.getColorAt(5, 25), touched);

    // column 2
    t.checkExpect(this.t3x3SImage.getColorAt(15, 5), touched);

    t.checkExpect(this.t3x3SImage.getColorAt(15, 15), touched);

    t.checkExpect(this.t3x3SImage.getColorAt(15, 25), touched);

    // column 3
    t.checkExpect(this.t3x3SImage.getColorAt(25, 5), empty);

    t.checkExpect(this.t3x3SImage.getColorAt(25, 15), empty);

    t.checkExpect(this.t3x3SImage.getColorAt(25, 25), finish);

    test3x3Seed.bredthSearch();

    this.t3x3SImage = new FrozenImage(test3x3Seed.draw());

    // column1
    t.checkExpect(this.t3x3SImage.getColorAt(5, 5), done);

    t.checkExpect(this.t3x3SImage.getColorAt(5, 15), empty);

    t.checkExpect(this.t3x3SImage.getColorAt(5, 25), touched);

    // column 2
    t.checkExpect(this.t3x3SImage.getColorAt(15, 5), done);

    t.checkExpect(this.t3x3SImage.getColorAt(15, 15), done);

    t.checkExpect(this.t3x3SImage.getColorAt(15, 25), done);

    // column 3
    t.checkExpect(this.t3x3SImage.getColorAt(25, 5), empty);

    t.checkExpect(this.t3x3SImage.getColorAt(25, 15), empty);

    t.checkExpect(this.t3x3SImage.getColorAt(25, 25), done);

  }

  void testGetSolution(Tester t) {
    this.initMaze();

    Color empty = Color.LIGHT_GRAY;

    Color done = Color.blue;

    for (int i = 0; i < 5; i += 1) {
      test3x3Seed.depthSearch();
    }

    this.t3x3SImage = new FrozenImage(test3x3Seed.draw());

    // column1
    t.checkExpect(this.t3x3SImage.getColorAt(5, 5), done);

    t.checkExpect(this.t3x3SImage.getColorAt(5, 15), empty);

    t.checkExpect(this.t3x3SImage.getColorAt(5, 25), empty);

    // column 2
    t.checkExpect(this.t3x3SImage.getColorAt(15, 5), done);

    t.checkExpect(this.t3x3SImage.getColorAt(15, 15), done);

    t.checkExpect(this.t3x3SImage.getColorAt(15, 25), done);

    // column 3
    t.checkExpect(this.t3x3SImage.getColorAt(25, 5), empty);

    t.checkExpect(this.t3x3SImage.getColorAt(25, 15), empty);

    t.checkExpect(this.t3x3SImage.getColorAt(25, 25), done);

  }

  void testCompareTo(Tester t) {
    this.initMaze();

    Edge big = new Edge(150, new Vertex(0, 1), new Vertex(1, 1));

    Edge med = new Edge(100, new Vertex(0, 1), new Vertex(1, 1));

    Edge small = new Edge(50, new Vertex(0, 1), new Vertex(1, 1));

    t.checkExpect(big.compareTo(small), 1);

    t.checkExpect(small.compareTo(big), -1);

    t.checkExpect(med.compareTo(small), 1);

    t.checkExpect(big.compareTo(big), 0);

    t.checkExpect(med.compareTo(med), 0);

    t.checkExpect(small.compareTo(small), 0);

  }

  void testUnion(Tester t) {

    this.initMaze();

    HashMap<Vertex, Vertex> exampleHash = new HashMap<>();
    HashMap<Vertex, Vertex> exampleHashUnion = new HashMap<>();

    Vertex v1 = new Vertex(0, 0);
    Vertex v2 = new Vertex(0, 1);
    Vertex v3 = new Vertex(1, 1);

    t.checkExpect(exampleHash, exampleHashUnion);

    exampleHash.put(v1, v2);

    test3x3.union(exampleHashUnion, v1, v2);

    t.checkExpect(exampleHash, exampleHashUnion);

    exampleHash.put(v2, v3);

    test3x3.union(exampleHashUnion, v2, v3);

    t.checkExpect(exampleHash, exampleHashUnion);

  }

  void testFind(Tester t) {

    this.initMaze();

    HashMap<Vertex, Vertex> exampleHashUnion = new HashMap<>();

    Vertex v1 = new Vertex(0, 0);
    Vertex v2 = new Vertex(0, 1);
    Vertex v3 = new Vertex(1, 1);
    Vertex v4 = new Vertex(1, 2);
    Vertex v5 = new Vertex(2, 2);
    Vertex v6 = new Vertex(1, 0);

    test3x3.union(exampleHashUnion, v1, v2);
    test3x3.union(exampleHashUnion, v2, v3);
    test3x3.union(exampleHashUnion, v3, v3);
    test3x3.union(exampleHashUnion, v4, v5);
    test3x3.union(exampleHashUnion, v5, v5);
    test3x3.union(exampleHashUnion, v6, v3);

    t.checkExpect(test3x3.find(exampleHashUnion, v1), v3);
    t.checkExpect(test3x3.find(exampleHashUnion, v2), v3);
    t.checkExpect(test3x3.find(exampleHashUnion, v3), v3);
    t.checkExpect(test3x3.find(exampleHashUnion, v4), v5);
    t.checkExpect(test3x3.find(exampleHashUnion, v5), v5);
    t.checkExpect(test3x3.find(exampleHashUnion, v6), v3);

  }

  void testIncompleteTree(Tester t) {
    // this method can't be tested since its a step in creating the minTree which is
    // private
    // The result of this method only effects the output of minTree
    // It is still needed since its a very important step of kruskal's algorithm
  }

  void tesetCreateMaze(Tester t) {
    this.initMaze();

    WorldImage draw1 = this.test3x3.draw();

    this.test3x3.createMaze(3, 3);

    WorldImage draw2 = this.test3x3.draw();

    t.checkExpect(draw1.equals(draw2), false);

    this.initMaze();

    draw1 = this.test3x3.draw();

    double height1 = draw1.getHeight();
    double width1 = draw1.getWidth();

    test3x3.createMaze(4, 4);

    draw2 = this.test3x3.draw();

    double height2 = draw2.getHeight();
    double width2 = draw2.getWidth();

    t.checkExpect(draw1.equals(draw2), false);

    t.checkExpect(height1 == height2, false);

    t.checkExpect(width1 == width2, false);

  }

  void testCreateEdges(Tester t) {
    this.initMaze();

    Vertex x00 = new Vertex(0, 0);
    Vertex x10 = new Vertex(1, 0);
    Vertex x01 = new Vertex(0, 1);
    Vertex x11 = new Vertex(1, 1);

    ArrayList<Vertex> col0 = new ArrayList<Vertex>();
    col0.add(x00);
    col0.add(x01);

    ArrayList<Vertex> col1 = new ArrayList<Vertex>();
    col1.add(x10);
    col1.add(x11);

    ArrayList<ArrayList<Vertex>> vertices = new ArrayList<>();
    vertices.add(col0);
    vertices.add(col1);

    ArrayList<Edge> edges = test3x3.createEdges(vertices);

    t.checkExpect(edges.size(), 4);

    t.checkExpect(edges.get(0).from, x00);
    t.checkExpect(edges.get(0).to, x01);

    t.checkExpect(edges.get(1).from, x00);
    t.checkExpect(edges.get(1).to, x10);

    t.checkExpect(edges.get(2).from, x01);
    t.checkExpect(edges.get(2).to, x11);

    t.checkExpect(edges.get(3).from, x10);
    t.checkExpect(edges.get(3).to, x11);

  }

  void testCanMove(Tester t) {
    this.initMaze();

    Vertex x00 = new Vertex(0, 0);
    Vertex x10 = new Vertex(1, 0);
    Vertex x20 = new Vertex(2, 0);

    Vertex x01 = new Vertex(0, 1);
    Vertex x11 = new Vertex(1, 1);
    Vertex x21 = new Vertex(2, 1);

    Vertex x02 = new Vertex(0, 2);
    Vertex x12 = new Vertex(1, 2);
    Vertex x22 = new Vertex(2, 2);

    ArrayList<ArrayList<Vertex>> vertices = new ArrayList<>();

    ArrayList<Vertex> col1 = new ArrayList<>();
    ArrayList<Vertex> col2 = new ArrayList<>();
    ArrayList<Vertex> col3 = new ArrayList<>();

    col1.add(x00);
    col1.add(x01);
    col1.add(x02);

    col2.add(x10);
    col2.add(x11);
    col2.add(x12);

    col2.add(x20);
    col2.add(x21);
    col2.add(x22);

    vertices.add(col1);
    vertices.add(col2);
    vertices.add(col3);

    ArrayList<Edge> minTree = new ArrayList<>();

    minTree.add(new Edge(1, x01, x00));
    minTree.add(new Edge(1, x02, x01));
    minTree.add(new Edge(1, x11, x10));
    minTree.add(new Edge(1, x21, x20));
    minTree.add(new Edge(1, x12, x01));
    minTree.add(new Edge(1, x22, x21));
    minTree.add(new Edge(1, x11, x01));

    CanMove canMove = new CanMove(minTree, vertices);

    // border test
    t.checkExpect(canMove.apply(x00, "left"), false);
    t.checkExpect(canMove.apply(x00, "up"), false);
    t.checkExpect(canMove.apply(x02, "down"), false);
    t.checkExpect(canMove.apply(x20, "right"), false);

    // wall test
    t.checkExpect(canMove.apply(x10, "left"), false);
    t.checkExpect(canMove.apply(x12, "up"), false);
    t.checkExpect(canMove.apply(x00, "right"), false);
    t.checkExpect(canMove.apply(x11, "down"), false);

    // work test
    t.checkExpect(canMove.apply(x11, "left"), true);
    t.checkExpect(canMove.apply(x02, "up"), true);
    t.checkExpect(canMove.apply(x00, "down"), true);
    t.checkExpect(canMove.apply(x01, "right"), true);

  }

  void testHasEdge(Tester t) {
    this.initMaze();

    Vertex x00 = new Vertex(0, 0);
    Vertex x10 = new Vertex(1, 0);
    Vertex x20 = new Vertex(2, 0);

    Vertex x01 = new Vertex(0, 1);
    Vertex x11 = new Vertex(1, 1);
    Vertex x21 = new Vertex(2, 1);

    Vertex x02 = new Vertex(0, 2);
    Vertex x12 = new Vertex(1, 2);
    Vertex x22 = new Vertex(2, 2);

    ArrayList<ArrayList<Vertex>> vertices = new ArrayList<>();

    ArrayList<Vertex> col1 = new ArrayList<>();
    ArrayList<Vertex> col2 = new ArrayList<>();
    ArrayList<Vertex> col3 = new ArrayList<>();

    col1.add(x00);
    col1.add(x01);
    col1.add(x02);

    col2.add(x10);
    col2.add(x11);
    col2.add(x12);

    col2.add(x20);
    col2.add(x21);
    col2.add(x22);

    vertices.add(col1);
    vertices.add(col2);
    vertices.add(col3);

    ArrayList<Edge> minTree = new ArrayList<>();

    minTree.add(new Edge(1, x01, x00));
    minTree.add(new Edge(1, x02, x01));
    minTree.add(new Edge(1, x11, x10));
    minTree.add(new Edge(1, x21, x20));
    minTree.add(new Edge(1, x12, x01));
    minTree.add(new Edge(1, x22, x21));
    minTree.add(new Edge(1, x11, x01));

    HasEdge hasEdge = new HasEdge(minTree);

    t.checkExpect(hasEdge.apply(x10, x11), true);
    t.checkExpect(hasEdge.apply(x21, x22), true);
    t.checkExpect(hasEdge.apply(x01, x12), true);
    t.checkExpect(hasEdge.apply(x01, x11), true);

    t.checkExpect(hasEdge.apply(x10, x00), false);
    t.checkExpect(hasEdge.apply(x11, x12), false);
    t.checkExpect(hasEdge.apply(x12, x22), false);
    t.checkExpect(hasEdge.apply(x12, x11), false);

  }

  // TODO figure out the size thing to make sure it fits the screen
  void testBigBang(Tester t) {
    this.initMaze();

    int width = 100;
    int height = 25;
    Maze newMaze = new Maze(width, height);
    // Make sure you mulitple times 10 so the maze fits the window
    newMaze.bigBang(width * 10, height *10, .0001);
  }

}
