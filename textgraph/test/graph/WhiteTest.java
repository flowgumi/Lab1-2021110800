package graph;
import org.junit.Before;
import org.junit.Test;
import graph.TextGraph.Graph;
import java.io.IOException;
import static org.junit.Assert.assertEquals;


public class WhiteTest {

    private Graph graph;

    @Before
    public void setUp() {
        // 这里传入文件路径
        String filePath = "D:\\test\\Java\\software\\lab1\\textgraph\\src\\graph\\test.txt";
        graph = TextGraph.generateGraphFromFile(filePath);
    }

    @Test
    public void testQueryBridgeWords_Case1() {
        String result = TextGraph.queryBridgeWords(graph, "", "for");
        assertEquals("Lack of words", result);
    }

    @Test
    public void testQueryBridgeWords_Case2() {
        String result = TextGraph.queryBridgeWords(graph, "!!!", "for");
        assertEquals("Invalid input: Input strings must be alphabetic words.", result);
    }

    @Test
    public void testQueryBridgeWords_Case3() {
        String result = TextGraph.queryBridgeWords(graph, "your", "for");
        assertEquals("No \"your\" in the graph!", result);
    }

    @Test
    public void testQueryBridgeWords_Case4() {
        String result = TextGraph.queryBridgeWords(graph, "for", "name");
        assertEquals("No \"name\" in the graph!", result);
    }

    @Test
    public void testQueryBridgeWords_Case5() {
        String result = TextGraph.queryBridgeWords(graph, "your", "name");
        assertEquals("No \"your\" and \"name\" in the graph!", result);
    }

    @Test
    public void testQueryBridgeWords_Case6() {
        String result = TextGraph.queryBridgeWords(graph, "kind", "for");
        assertEquals("No bridge words from \"kind\" to \"for\"!", result);
    }

    @Test
    public void testQueryBridgeWords_Case7() {
        String result = TextGraph.queryBridgeWords(graph, "still", "for");
        assertEquals("No bridge words from \"still\" to \"for\"!", result);
    }

    @Test
    public void testQueryBridgeWords_Case8() {
        String result = TextGraph.queryBridgeWords(graph, "in", "for");
        assertEquals("No bridge words from \"in\" to \"for\"!", result);
    }

    @Test
    public void testQueryBridgeWords_Case9() {
        String result = TextGraph.queryBridgeWords(graph, "most", "in");
        assertEquals("The bridge words from \"most\" to \"in\" are: the, another", result);
    }

    @Test
    public void testQueryBridgeWords_Case10() throws IOException {
        String result = TextGraph.queryBridgeWords(graph, "with", "of");
        assertEquals("Failed to write DOT file with highlights!", result);
    }
}