package graph;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import graph.TextGraph.Graph;

public class BlackTest {

    private Graph graph;

    @Before
    public void setUp() {
        // 这里传入文件路径
        String filePath = "D:\\test\\Java\\software\\lab1\\textgraph\\src\\graph\\test.txt";
        graph = TextGraph.generateGraphFromFile(filePath);
    }

    @Test
    public void testCalcShortestPath_case1() {
        String result = TextGraph.calcShortestPath(graph, "still", "in");
        assertEquals("Shortest path: still -> another -> in with length 3", result);
    }

    @Test
    public void testCalcShortestPath_case2() {
        String result = TextGraph.calcShortestPath(graph, "people", "your");
        assertEquals("No \"your\" in the graph!", result);
    }

    @Test
    public void testCalcShortestPath_case3() {
        String result = TextGraph.calcShortestPath(graph, "for", "");
        assertEquals("Lack of words", result);
    }

    @Test
    public void testCalcShortestPath_case4() {
        String result = TextGraph.calcShortestPath(graph, "for", "in");
        assertEquals("Shortest path: for -> example -> in with length 3", result);
    }

    @Test
    public void testCalcShortestPath_case5() {
        String result = TextGraph.calcShortestPath(graph, "!!!", "for");
        assertEquals("Invalid input: Input strings must be alphabetic words.", result);
    }

}