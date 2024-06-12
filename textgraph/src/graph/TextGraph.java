package graph;


import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.List;

public class TextGraph {

    public static void main(String[] args) {
        // 创建Scanner对象用于接收用户输入
        Scanner scanner = new Scanner(System.in);
        String filePath = args.length > 0 ? args[0] : "";

        // 如果没有通过命令行参数提供文件路径，则提示用户输入
        if (filePath.isEmpty()) {
            System.out.println("请输入文本文件路径:");
            filePath = scanner.nextLine();
        }

        Graph graph = new Graph();

        // 读取文件并生成图
        try {
            List<String> lines = Files.readAllLines(Paths.get(filePath));
            String text = String.join(" ", lines);   //毁成一行
            String[] words = text.replaceAll("[^a-zA-Z ]", "").toLowerCase().split("\\s+");  //这里不区分大小写

            for (int i = 0; i < words.length - 1; i++) {
                graph.addEdge(words[i], words[i + 1]);
            }
            graph.addEdge(words[words.length-1],null);

            // 展示有向图
            System.out.println("请输出生成图片名称");
            String fileName = scanner.nextLine();
            showDirectedGraph(graph,fileName);

            // 主菜单循环
            while (true) {
                System.out.println("请选择操作:");
                System.out.println("1. 查询桥接词");
                System.out.println("2. 根据bridge word生成新文本");
                System.out.println("3. 计算两个单词之间的最短路径");
                System.out.println("4. 随机游走");
                System.out.println("5. 退出");
                System.out.println("请选择操作:");

                int choice;
                try {
                    choice = Integer.parseInt(scanner.nextLine());
                } catch (NumberFormatException e) {
                    System.out.println("无效选择，请输入数字。");
                    continue;
                }

                String word1, word2;
                switch (choice) {
                    case 1:
                        System.out.println("请输入两个单词:");
                        word1 = scanner.next();
                        word2 = scanner.next();
                        scanner.nextLine();  // 清除缓冲区
                        System.out.println(queryBridgeWords(graph, word1, word2));
                        break;
                    case 2:
                        System.out.println("请输入一行新文本:");
                        String newText = scanner.nextLine();
                        System.out.println(generateNewText(graph, newText));
                        break;
                    case 3:
                        System.out.println("请输入两个单词:");
                        word1 = scanner.next();
                        word2 = scanner.next();
                        scanner.nextLine();  // 清除缓冲区
                        System.out.println(calcShortestPath(graph, word1, word2));
                        break;
                    case 4:
                        System.out.println("随机游走结果:");
                        System.out.println(randomWalk(graph));
                        break;
                    case 5:
                        System.out.println("退出程序");
                        return;
                    default:
                        System.out.println("无效选择，请重试.");
                }
            }

        } catch (IOException e) {
            System.out.println("文件读取失败: " + e.getMessage());
        }
    }

    // 图结构类
    static class Graph {
        private final Map<String, Map<String, Integer>> adjList = new HashMap<>();

        // 添加边到图中
        public void addEdge(String begin, String end) {
            if (!adjList.containsKey(begin)) {
                adjList.put(begin, new HashMap<>());
            }
            Map<String, Integer> innerMap = adjList.get(begin);
            if (innerMap.containsKey(end)) {
                innerMap.put(end, innerMap.get(end) + 1);
            } else if(end != null){
                innerMap.put(end, 1);
            }

        }

        public Map<String, Map<String, Integer>> getAdjList() {
            return adjList;
        }
    }

    // 展示有向图
    static void showDirectedGraph(Graph graph, String outputFileName) {
        // 定义输出 PNG 文件名
        String pngFileName = outputFileName + ".png";
        // 定义 dot 文件名
        String dotFileName = outputFileName + ".dot";
        // 写入 dot 文件
        // 缓冲写文件
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(dotFileName))) {
            writer.write("digraph G {\n");
            Map<String, Map<String, Integer>> adjList = graph.getAdjList();
            for (Map.Entry<String, Map<String, Integer>> entry : adjList.entrySet()){
                String from = entry.getKey();
                Map<String, Integer> edges = entry.getValue();
                for (Map.Entry<String, Integer> edge : edges.entrySet()){

                    String to = edge.getKey();
                    int weight = edge.getValue();
                    writer.write(String.format("\"%s\" -> \"%s\" [label=\"%d\"];\n", from, to, weight));
                }
            }
            writer.write("}\n");
        } catch (IOException e) {
            System.out.println("写入 dot 文件失败: " + e.getMessage());
            return;
        }

        // 使用 dot 命令生成 PNG 文件
        try {
            //创建一个 ProcessBuilder 对象，用于启动一个外部进程来执行指定的命令
            //ProcessBuilder 用于执行 dot 命令
            ProcessBuilder pb = new ProcessBuilder("dot", "-Tpng", dotFileName, "-o", pngFileName);
            //根据配置启动进程
            Process p = pb.start();
            //等待进程结束
            p.waitFor();
            System.out.println("图结构已生成: " + pngFileName);
        } catch (IOException | InterruptedException e) {
            System.out.println("生成图形文件失败: " + e.getMessage());
        }
    }


    // 查询桥接词
    static String queryBridgeWords(Graph graph, String word1, String word2) {
        word1 = word1.toLowerCase();
        word2 = word2.toLowerCase();
        if (!graph.getAdjList().containsKey(word1) && graph.getAdjList().containsKey(word2)) {
            return "No \"" + word1 + "\" in the graph!";
        } else if (graph.getAdjList().containsKey(word1) && !graph.getAdjList().containsKey(word2)) {
            return "No \"" + word2 + "\" in the graph!";
        } else if (!graph.getAdjList().containsKey(word1) && !graph.getAdjList().containsKey(word2)) {
            return "No \"" + word1 +"\" and \"" + word2 + "\" in the graph!";
        }

        Set<String> bridges = new HashSet<>();
        if (graph.getAdjList().get(word1) != null) {
            for (String intermediate : graph.getAdjList().get(word1).keySet()) {
                if (graph.getAdjList().get(intermediate) != null && graph.getAdjList().get(intermediate).containsKey(word2)) {
                    bridges.add(intermediate);
                }
            }
        }

        if (bridges.isEmpty()) {
            return "No bridge words from \"" + word1 + "\" to \"" + word2 + "\"!";
        } else {
            try {
                writeDotFileWithHighlight(graph, word1, word2, bridges);
            } catch (IOException e) {
                System.out.println("生成dot文件失败: " + e.getMessage());
                return "Failed to write DOT file with highlights!";
            }
            return "The bridge words from \"" + word1 + "\" to \"" + word2 + "\" are: " + String.join(", ", bridges);
        }
    }

    static void writeDotFileWithHighlight(Graph graph, String word1, String word2, Set<String> bridges) throws IOException {
        String dotFilePath = "graph.dot";
        String dotFileContent = generateDotFileWithHighlight(graph, word1, word2, bridges);
        Files.write(Paths.get(dotFilePath), dotFileContent.getBytes());
        // 使用 dot 命令生成 PNG 文件
        try {
            //创建一个 ProcessBuilder 对象，用于启动一个外部进程来执行指定的命令
            //ProcessBuilder 用于执行 dot 命令
            ProcessBuilder pb = new ProcessBuilder("dot", "-Tpng", "graph.dot", "-o", "highlight.png");
            //根据配置启动进程
            Process p = pb.start();
            //等待进程结束
            p.waitFor();
        } catch (IOException | InterruptedException e) {
            System.out.println("生成图形文件失败: " + e.getMessage());
        }
    }

    static String generateDotFileWithHighlight(Graph graph, String word1, String word2, Set<String> bridges) {
        StringBuilder dotFileContent = new StringBuilder();
        dotFileContent.append("digraph G {\n");

        for (String node : graph.getAdjList().keySet()) {
            if (node.equals(word1)) {
                dotFileContent.append("    ").append(node).append(" [label=<<font color=\"blue\"><b>").append(node).append("</b></font>>];\n");
            } else if (node.equals(word2)) {
                dotFileContent.append("    ").append(node).append(" [label=<<font color=\"blue\"><b>").append(node).append("</b></font>>];\n");
            } else if (bridges.contains(node)) {
                dotFileContent.append("    ").append(node).append(" [label=<<font color=\"green\"><b>").append(node).append("</b></font>>];\n");
            } else {
                dotFileContent.append("    ").append(node).append(";\n");
            }
        }

        for (Map.Entry<String, Map<String, Integer>> entry : graph.getAdjList().entrySet()) {
            String from = entry.getKey();
            Map<String, Integer> edges = entry.getValue();
            for (Map.Entry<String, Integer> edge : edges.entrySet()) {
                String to = edge.getKey();
                dotFileContent.append("    ").append(from).append(" -> ").append(to).append(" [label=\"").append(edge.getValue()).append("\"];\n");
            }
        }

        dotFileContent.append("}");
        return dotFileContent.toString();
    }



    // 根据bridge word生成新文本
    static String generateNewText(Graph graph, String inputText) {
        String[] words = inputText.replaceAll("[^a-zA-Z ]", "").toLowerCase().split("\\s+"); //字符串按照一个或多个空白字符进行分割。
        StringBuilder newText = new StringBuilder(words[0]);

        for (int i = 0; i < words.length - 1; i++) {
            String bridgeWords = queryBridgeWords(graph, words[i], words[i + 1]);
            if (bridgeWords.startsWith("The bridge words")) {
                String[] parts = bridgeWords.split(": ")[1].split(", ");
                newText.append(" ").append(parts[new Random().nextInt(parts.length)]);  //new Random().nextInt(parts.length); 生成一个随机索引
            }
            newText.append(" ").append(words[i + 1]);
        }

        return newText.toString();
    }

    // 计算两个单词之间的最短路径
    static String calcShortestPath(Graph graph, String word1, String word2) {
        word1 = word1.toLowerCase();
        word2 = word2.toLowerCase();

        if (!graph.getAdjList().containsKey(word1) && graph.getAdjList().containsKey(word2)) {
            return "No \"" + word1 + "\" in the graph!";
        } else if (graph.getAdjList().containsKey(word1) && !graph.getAdjList().containsKey(word2)) {
            return "No \"" + word2 + "\" in the graph!";
        } else if (!graph.getAdjList().containsKey(word1) && !graph.getAdjList().containsKey(word2)) {
            return "No \"" + word1 +"\" and \"" + word2 + "\" in the graph!";
        }

        // Dijkstra 算法 具体实现
        // 在git第二次提交的时候添加的注释
        Map<String, Integer> distances = new HashMap<>();
        Map<String, String> previous = new HashMap<>();
        Set<String> nodes = new HashSet<>(graph.getAdjList().keySet());
        // 初始化
        for (String node : nodes) {
            distances.put(node, Integer.MAX_VALUE);
        }
        distances.put(word1, 0);   //计算和word1距离

        while (!nodes.isEmpty()) {
            // Find the node with the smallest distance
            String closest = null;
            int minDistance = Integer.MAX_VALUE;
            for (String node : nodes) {
                if (distances.get(node) < minDistance) {
                    minDistance = distances.get(node);
                    closest = node;
                }
            }

            if (closest == null || distances.get(closest) == Integer.MAX_VALUE) {
                break;
            }

            nodes.remove(closest);

            if (closest.equals(word2)) {
                break;
            }

            for (Map.Entry<String, Integer> neighbor : graph.getAdjList().get(closest).entrySet()) {
                int alt = distances.get(closest) + neighbor.getValue();
                if (alt < distances.get(neighbor.getKey())) {
                    distances.put(neighbor.getKey(), alt);
                    previous.put(neighbor.getKey(), closest);
                }
            }
        }

        if (!previous.containsKey(word2)) {
            return "No path from " + word1 + " to " + word2 + "!";
        }

        List<String> path = new ArrayList<>();
        for (String at = word2; at != null; at = previous.get(at)) {
            path.add(at);
        }
        Collections.reverse(path);

        return "Shortest path: " + String.join(" -> ", path) + " with length " + distances.get(word2);
    }

    // 随机游走
    static String randomWalk(Graph graph) {
        List<String> nodes = new ArrayList<>(graph.getAdjList().keySet());
        if (nodes.isEmpty()) {
            return "Graph is empty";
        }
        String current = nodes.get(new Random().nextInt(nodes.size()));
        System.out.println(current);
        StringBuilder path = new StringBuilder(current);
        Set<String> visitedEdges = new HashSet<>();
        boolean[] stopFlag = {false};

        // Create a separate thread to listen for user input
        Thread inputThread = new Thread(() -> {
            try {
                System.in.read();
                stopFlag[0] = true;
            } catch (IOException e) {
                System.out.println("Error: " + e.getMessage());
            }
        });
        inputThread.start();

        while (!stopFlag[0]) {
            Map<String, Integer> edges = graph.getAdjList().get(current);
            if (edges == null || edges.isEmpty()) {
                break;
            }

            String finalCurrent = current;
            List<String> possibleNextNodes = new ArrayList<>();
            for (String next : edges.keySet()) {
                String edge = current + "->" + next;
                if (!visitedEdges.contains(edge)) {
                    possibleNextNodes.add(next);
                }
            }

            if (possibleNextNodes.isEmpty()) {
                break;
            }

            String next = possibleNextNodes.get(new Random().nextInt(possibleNextNodes.size()));
            visitedEdges.add(current + "->" + next);
            path.append(" -> ").append(next);
            current = next;

            System.out.println(current);
            try {
                Thread.sleep(1000); // Delay of 1 second between steps
            } catch (InterruptedException e) {
                System.out.println("生成图形文件失败: " + e.getMessage());
            }
        }

        String result = path.toString();
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("random_walk.txt"))) {
            writer.write(result);
        } catch (IOException e) {
            System.out.println("写入随机游走结果文件失败: " + e.getMessage());
        }

        return result;
    }

}
