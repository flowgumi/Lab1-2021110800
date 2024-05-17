package graph;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class TextGraph {

    public static void main(String[] args) {

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
            String text = String.join(" ", lines);
            String[] words = text.replaceAll("[^a-zA-Z ]", "").toLowerCase().split("\\s+");//按空白字符分割字符串

            for (int i = 0; i < words.length - 1; i++) {
                graph.addEdge(words[i], words[i + 1]);
            }

            // 展示有向图
            showDirectedGraph(graph);

            // 主菜单循环
            while (true) {
                System.out.println("请选择操作:");
                System.out.println("1. 查询桥接词");
                System.out.println("2. 根据bridge word生成新文本");
                System.out.println("3. 计算两个单词之间的最短路径");
                System.out.println("4. 随机游走");
                System.out.println("5. 退出");

                int choice;
                try {
                    choice = Integer.parseInt(scanner.nextLine());
                } catch (NumberFormatException e) {
                    System.out.println("无效选择，请输入数字1-5");
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
                        System.out.println("无效选择，请重试");
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
        public void addEdge(String from, String to) {
            adjList.computeIfAbsent(from, k -> new HashMap<>()).merge(to, 1, Integer::sum);
        }

        public Map<String, Map<String, Integer>> getAdjList() {
            return adjList;
        }
    }

    // 展示有向图
    static void showDirectedGraph(Graph graph) {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("graph.dot"))) {
            writer.write("digraph G {\n");
            //遍历键值对
            for (var entry : graph.getAdjList().entrySet()) {
                for (var edge : entry.getValue().entrySet()) {
                    writer.write(String.format("\"%s\" -> \"%s\" [label=\"%d\"];\n", entry.getKey(), edge.getKey(), edge.getValue()));
                }
            }
            writer.write("}\n");
        } catch (IOException e) {
            System.out.println("写入dot文件失败: " + e.getMessage());
        }

        try {
            ProcessBuilder pb = new ProcessBuilder("dot", "-Tpng", "graph.dot", "-o", "output_graph.png");
            Process p = pb.start();
            p.waitFor();
            System.out.println("图结构已生成: output_graph.png");
        } catch (IOException | InterruptedException e) {
            System.out.println("生成图形文件失败: " + e.getMessage());
        }
    }

    // 查询桥接词
    static String queryBridgeWords(Graph graph, String word1, String word2) {
        word1 = word1.toLowerCase();
        word2 = word2.toLowerCase();
        if (!graph.getAdjList().containsKey(word1) || !graph.getAdjList().containsKey(word2)) {
            return "No word1 or word2 in the graph!";
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
            return "No bridge words from " + word1 + " to " + word2 + "!";
        } else {
            return "The bridge words from " + word1 + " to " + word2 + " are: " + String.join(", ", bridges) + ".";
        }
    }

    // 根据bridge word生成新文本
    static String generateNewText(Graph graph, String inputText) {
        String[] words = inputText.replaceAll("[^a-zA-Z ]", "").toLowerCase().split("\\s+");
        StringBuilder newText = new StringBuilder(words[0]);

        for (int i = 0; i < words.length - 1; i++) {
            String bridgeWords = queryBridgeWords(graph, words[i], words[i + 1]);
            if (bridgeWords.startsWith("The bridge words")) {
                String[] parts = bridgeWords.split(": ")[1].split(", ");
                newText.append(" ").append(parts[new Random().nextInt(parts.length)]);
            }
            newText.append(" ").append(words[i + 1]);
        }

        return newText.toString();
    }

    // 计算两个单词之间的最短路径
    static String calcShortestPath(Graph graph, String word1, String word2) {
        word1 = word1.toLowerCase();
        word2 = word2.toLowerCase();

        if (!graph.getAdjList().containsKey(word1) || !graph.getAdjList().containsKey(word2)) {
            return "No word1 or word2 in the graph!";
        }

        // Dijkstra's algorithm without priority queue
        Map<String, Integer> distances = new HashMap<>();
        Map<String, String> previous = new HashMap<>();
        Set<String> nodes = new HashSet<>(graph.getAdjList().keySet());

        for (String node : nodes) {
            distances.put(node, Integer.MAX_VALUE);
        }
        distances.put(word1, 0);

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
        String current = nodes.get(new Random().nextInt(nodes.size()));
        StringBuilder path = new StringBuilder(current);
        Set<String> visitedEdges = new HashSet<>();

        while (true) {
            Map<String, Integer> edges = graph.getAdjList().get(current);
            if (edges == null || edges.isEmpty()) {
                break;
            }

            String finalCurrent = current;
            //过滤
            List<String> possibleNextNodes = edges.keySet().stream()
                    .filter(next -> !visitedEdges.contains(finalCurrent + "->" + next))
                    .toList();

            if (possibleNextNodes.isEmpty()) {
                break;
            }

            String next = possibleNextNodes.get(new Random().nextInt(possibleNextNodes.size()));
            visitedEdges.add(current + "->" + next);
            path.append(" -> ").append(next);
            current = next;
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
