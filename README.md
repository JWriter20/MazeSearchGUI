# MazeSearchGUI
A java program that generates a random maze of a specified size using kruskal's algorithm,
then the user has the option to solve this maze manually, with a Depth First Search algorithm, or a Breadth First Search algorithm. 
This can be seen visually as it is happening. Upon completion the shortest path to solve the maze is shown in dark blue. 

![Image of Solved Maze](https://github.com/JWriter20/MazeSearchGUI/blob/main/solvedMaze.png)

Instructions for running on Eclipse IDE:

1. Open Eclipse then click on the "run" menu in the top menubar
2. click "run configurations"
3. In the main tab of your new configuration for the project name select "Maze" and under the main class type tester.Main
4. Under the Arguments tab, type "ExamplesMaze" into the program arguments section 
5. Under the Dependencies tab, click on Classpath folder then the "Add External Jars" and add both tester..jar and javalib.jar.
6. Apply changes then run the configuration.

This can easily be replicated in other IDEs by importing the included External Jars and then running the code. (You may have to use public static void main)
