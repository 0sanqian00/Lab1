package org.example;

import javax.swing.*;
import java.io.*;
import java.util.*;
public class Main {
    private static Map<String, Set<String>> graph;
    private static Map<String, Integer> wordFrequency;
    private static Map<String, Map<String, Integer>> edgeWeights;

    private static volatile boolean stopRandomWalk = false;// 增加一个 volatile 标记来控制随机游走的停止
    // 定义一个Random实例以便在类中重复使用
    private static final Random random = new Random();
    private static int V = 0; // 最大顶点数
    private static int[][] dist; // 保存最短路径长度

    public static void main(String[] args) {
        // 初始化图和单词频率映射
        graph = new HashMap<>();
        wordFrequency = new HashMap<>();
        edgeWeights = new HashMap<>();

        // 读取文本文件并构建图
        readTextFileAndBuildGraph("C:\\Users\\三谦\\Desktop\\软件工程\\Lab1\\Lab1\\test\\test1.txt");

        Scanner scanner = new Scanner(System.in);
        char choice;

        do {
            System.out.println("\n--- Main Menu ---");
            System.out.println("1. Show Directed Graph");
            System.out.println("2. Query Bridge Words");
            System.out.println("3. Generate New Text");
            System.out.println("4. Calculate Shortest Path");
            System.out.println("5. Perform Random Walk");
            System.out.println("6. Exit");
            System.out.print("Enter your choice (1-6): ");
            choice = scanner.next().charAt(0);
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case '1' -> {
                    System.out.println("\nGraph built. Displaying directed graph...");
                    showDirectedGraph();
                }
                case '2' -> {
                    System.out.print("Enter word 1: ");
                    String word1 = scanner.nextLine();
                    System.out.print("Enter word 2: ");
                    String word2 = scanner.nextLine();
                    System.out.println("Bridge words from '" + word1 + "' to '" + word2 + "': " + queryBridgeWords(word1, word2));
                }
                case '3' -> {
                    System.out.println("Enter a line of text to generate new text:");
                    String inputText = scanner.nextLine();
                    System.out.println("Generated new text: " + generateNewText(inputText));
                }
                case '4' -> {
                    System.out.print("Enter word 1: ");
                    String wordA = scanner.nextLine();
                    System.out.print("Enter word 2: ");
                    String wordB = scanner.nextLine();
                    showDirectedGraphWithShortestPath(wordA, wordB);
                }
                case '5' -> {
                    System.out.println("\nPerforming a random walk... Press 's' to stop.");
                    stopRandomWalk = false; // 重置停止标志
                    // 使用线程执行随机游走
                    Thread randomWalkThread = new Thread(Main::randomWalk);
                    randomWalkThread.start();

                    // 等待用户输入以停止游走或返回主菜单
                    while (!stopRandomWalk) {
                        System.out.print("Enter 's' to stop the random walk or just press enter to continue: \n");
                        String input = scanner.nextLine().trim().toLowerCase();
                        if ("s".equals(input)) {
                            stopRandomWalk = true; // 用户请求停止随机游走
                            break;
                        }
                    }
                }
                case '6' -> System.out.println("Exiting program.");
                default -> System.out.println("Invalid choice. Please enter a number between 1 and 6.");
            }
        } while (choice != '6');

        scanner.close();
    }

    // 读取文本文件并构建有向图
    private static void readTextFileAndBuildGraph(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                // 转换为小写，并将非字母字符替换为空格
                line = line.toLowerCase().replaceAll("[^a-z ]", " ");

                // 分割单词
                String[] words = line.split("\\s+");

                for (int i = 0; i < words.length - 1; i++) {
                    String currentWord = words[i];
                    String nextWord = words[i + 1];

                    // 更新单词频率
                    wordFrequency.put(currentWord, wordFrequency.getOrDefault(currentWord, 0) + 1);

                    // 添加边和更新权重
                    graph.computeIfAbsent(currentWord, k -> new HashSet<>()).add(nextWord);
                    updateWeight(currentWord, nextWord);
                }
            }

            V = graph.size();
            dist = new int[V][V];
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void updateWeight(String from, String to) {
        // 获取从from到to的现有权重，如果没有设置，则默认为0
        int currentWeight = edgeWeights.getOrDefault(from, new HashMap<>()).getOrDefault(to, 0);

        // 权重加1，因为每次调用此方法意味着A和B又相邻出现了一次
        currentWeight += 1;

        // 更新边的权重
        edgeWeights.computeIfAbsent(from, k -> new HashMap<>()).put(to, currentWeight);
    }

    // 展示有向图
    public static void showDirectedGraph() {
        // DOT 文件将被创建在用户目录下
        String dotFilePath = "graph.dot";
        String graphvizPath = "C:\\Users\\三谦\\Desktop\\软件工程\\Lab1\\Graphviz-11.0.0-win64\\bin\\dot.exe";
        String pngFilePath = "C:\\Users\\三谦\\Desktop\\软件工程\\Lab1\\Lab1\\graph.png";

        // 创建DOT文件
        try (PrintWriter out = new PrintWriter(new FileWriter(dotFilePath))) {
            out.println("digraph G {");
            out.println("  rankdir=LR;"); // 设置图的方向从左到右

            // 添加节点
            for (String node : graph.keySet()) {
                out.println("  \"" + escapeDotString(node) + "\" [shape=circle];");
            }

            // 添加边和权重
            for (Map.Entry<String, Set<String>> entry : graph.entrySet()) {
                String fromNode = entry.getKey();
                for (String toNode : entry.getValue()) {
                    // 获取边的权重
                    int weight = edgeWeights.getOrDefault(fromNode, new HashMap<>()).getOrDefault(toNode, 0);
                    // 将权重作为标签添加到边
                    out.printf("  \"%s\" -> \"%s\" [label=\"%d\"];\n", escapeDotString(fromNode), escapeDotString(toNode), weight);
                }
            }

            out.println("}");
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // 使用Graphviz命令行工具生成图形
        try {
            // 构建dot命令
            String command = graphvizPath + " -Tpng " + dotFilePath + " -o " + pngFilePath;
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();
            System.out.println("Graph visualization generated as '" + pngFilePath + "'");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // 转义DOT语言特殊字符
    private static String escapeDotString(String input) {
        return input.replace("\"", "\\\"");
    }

    // 计算两个单词之间的最短路径
    public static String calcShortestPath(String word1, String word2) {
        if (!graph.containsKey(word1) || !graph.containsKey(word2)) {
            return "No " + word1 + " or " + word2 + " in the graph!";
        }

        // 转换成邻接矩阵，这里假设边的权重为1

        for (int i = 0; i < V; i++) {
            for (int j = 0; j < V; j++) {
                dist[i][j] = (i == j) ? 0 : Integer.MAX_VALUE;
            }
        }

        // 填充邻接矩阵
        for (Map.Entry<String, Set<String>> entry : graph.entrySet()) {
            String word = entry.getKey();
            Set<String> neighbors = entry.getValue();
            for (String neighbor : neighbors) {
                int index1 = getIndex(word);
                int index2 = getIndex(neighbor);
                if (index1 != -1 && index2 != -1) {
                    dist[index1][index2] = 1;
                }
            }
        }

        // 弗洛伊德算法填充所有顶点对的最短路径
        for (int k = 0; k < V; k++) {
            for (int i = 0; i < V; i++) {
                for (int j = 0; j < V; j++) {
                    if (dist[i][k] != Integer.MAX_VALUE && dist[k][j] != Integer.MAX_VALUE
                            && dist[i][k] + dist[k][j] < dist[i][j]) {
                        dist[i][j] = dist[i][k] + dist[k][j];
                    }
                }
            }
        }

        // 找到word1和word2对应的索引
        int index1 = getIndex(word1);
        int index2 = getIndex(word2);

        // 如果最短距离是无穷大，说明word1和word2不相连
        if (dist[index1][index2] == Integer.MAX_VALUE) {
            return "No path between " + word1 + " and " + word2 + ".";
        }

        // 返回word1和word2之间的最短路径长度
        return "The shortest path distance from " + word1 + " to " + word2 + " is: " + dist[index1][index2];
    }


    public static void showDirectedGraphWithShortestPath(String word1, String word2) {
        // 首先，计算最短路径
        String shortestPathResult = calcShortestPath(word1, word2);
        if (!shortestPathResult.startsWith("The shortest path distance")) {
            System.out.println(shortestPathResult);
            return;
        }

        // 然后，创建DOT文件并添加常规的图结构
        String dotFilePath = "graph.dot";
        String graphvizPath = "C:\\Users\\三谦\\Desktop\\软件工程\\Lab1\\Graphviz-11.0.0-win64\\bin\\dot.exe";
        String pngFilePath = "C:\\Users\\三谦\\Desktop\\软件工程\\Lab1\\Lab1\\graph_with_shortest_path.png";

        try (PrintWriter out = new PrintWriter(new FileWriter(dotFilePath))) {
            out.println("digraph G {");
            out.println("  rankdir=LR;");
            out.println("  node[shape=circle];");

            // 添加节点
            for (String node : graph.keySet()) {
                out.printf("  \"%s\" [style=filled, fillcolor=lightgray];\n", escapeDotString(node));
            }

            // 添加边
            for (Map.Entry<String, Set<String>> entry : graph.entrySet()) {
                String fromNode = entry.getKey();
                for (String toNode : entry.getValue()) {
                    int weight = edgeWeights.getOrDefault(fromNode, new HashMap<>()).getOrDefault(toNode, 0);
                    out.printf("  \"%s\" -> \"%s\" [label=\"%d\"];\n", escapeDotString(fromNode), escapeDotString(toNode), weight);
                }
            }

            // 根据最短路径算法结果，高亮显示最短路径上的边和节点
            // 此处需要根据calcShortestPath方法的具体实现来确定最短路径上的节点和边
            // 假设 shortestPath 是一个包含最短路径上节点的列表
            List<String> shortestPath = extractShortestPath(word1, word2); // 需要实现这个方法

            for (String node : shortestPath) {
                out.printf("  \"%s\" [style=filled, fillcolor=red];\n", escapeDotString(node));
            }
            for (int i = 0; i < shortestPath.size() - 1; i++) {
                String fromNode = shortestPath.get(i);
                String toNode = shortestPath.get(i + 1);
                out.printf("  \"%s\" -> \"%s\" [color=red, style=bold];\n", escapeDotString(fromNode), escapeDotString(toNode));
            }

            out.println("}");
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // 使用Graphviz命令行工具生成图形
        try {
            String command = graphvizPath + " -Tpng " + dotFilePath + " -o " + pngFilePath;
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();
            System.out.println("Graph with highlighted shortest path generated as '" + pngFilePath + "'");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static List<String> extractShortestPath(String word1, String word2) {
        List<String> path = new ArrayList<>();
        Map<String, Integer> indexMap = new HashMap<>();

        // 首先构建一个从索引到单词的映射
        for (int i = 0; i < V; i++) {
            String word = (String) graph.keySet().toArray()[i];
            indexMap.put(word, i);
        }

        int index1 = indexMap.get(word1);
        int index2 = indexMap.get(word2);

        // 从word1开始，正向追踪最短路径
        int at = index1;
        path.add(word1); // 添加起始节点

        while (at != index2) {
            for (int to = 0; to < V; to++) {
                if (dist[at][to] == 1 && dist[to][index2] != Integer.MAX_VALUE) {
                    at = to;
                    path.add((String) graph.keySet().toArray()[at]);
                    break;
                }
            }
        }

        return path;
    }



    // 辅助函数，用于获取单词在邻接矩阵中的索引
    private static int getIndex(String word) {
        int index = 0;
        for (String w : graph.keySet()) {
            if (w.equals(word)) {
                return index;
            }
            index++;
        }
        return -1;
    }


    // 查询桥接词
    public static String queryBridgeWords(String word1, String word2) {
        if (!graph.containsKey(word1) || !graph.containsKey(word2)) {
            return "No " + word1 + " or " + word2 + " in the graph!";
        }

        Set<String> bridgeWords = new HashSet<>();
        // 遍历word1的所有后继节点
        for (String successor : graph.get(word1)) {
            // 检查这些后继节点是否也是word2的前驱
            if (graph.get(word2).contains(successor)) {
                bridgeWords.add(successor);
            }
        }

        if (bridgeWords.isEmpty()) {
            return "No bridge words from " + word1 + " to " + word2 + "!";
        } else {
            // 将桥接词转换为字符串，单词之间用逗号和空格分隔
            return "The bridge words from " + word1 + " to " + word2 + " are: " +
                    String.join(", ", bridgeWords);
        }
    }

    // 根据bridge word生成新文本
    public static String generateNewText(String inputText) {
        // 使用空格分割输入文本，得到单词数组
        String[] words = inputText.toLowerCase().split("\\s+");

        // 结果列表，用于存储新文本的单词
        List<String> newWords = new ArrayList<>();

        // 用于随机选择桥接词
        Random random = new Random();

        // 遍历单词数组，查找每对相邻单词的桥接词
        for (int i = 0; i < words.length - 1; i++) {
            // 添加当前单词
            newWords.add(words[i]);

            // 查询这对相邻单词的桥接词
            String bridgeWordsResult = queryBridgeWords(words[i], words[i + 1]);
            // 如果桥接词结果以"No bridge words"开头，则表示没有桥接词
            if (bridgeWordsResult.startsWith("No bridge words")) {
                // 不插入任何单词，继续
                continue;
            }

            // 如果有桥接词，将其添加到结果列表
            // 桥接词以逗号分隔，我们需要分割并随机选择一个
            String[] bridgeWordArray = bridgeWordsResult.substring(bridgeWordsResult.indexOf(':') + 2).trim().split(", ");
            // 随机选择一个桥接词
            String bridgeWord = bridgeWordArray[random.nextInt(bridgeWordArray.length)];
            newWords.add(bridgeWord);
        }

        // 添加最后一个单词
        newWords.add(words[words.length - 1]);

        // 将结果列表转换为字符串，使用空格连接单词
        return String.join(" ", newWords);
    }



    // 随机游走
    public static void randomWalk() {

        try {
            Set<String> visitedEdges = new HashSet<>();
            List<String> walkPath = new ArrayList<>();

            if (graph.isEmpty()) {
                System.out.println("The graph is empty!");
            }

            // 获取所有节点
            Collection<String> allNodes = graph.keySet();

            // 随机选择一个起始节点
            String startNode = allNodes.stream()
                    .skip(random.nextInt(allNodes.size()))
                    .findFirst()
                    .orElseThrow();

            walkPath.add(startNode);

            String currentNode = startNode;
            while (currentNode != null && !stopRandomWalk) {
                // 获取当前节点的所有邻居节点
                Set<String> neighbors = graph.get(currentNode);
                // 随机选择一个邻居节点作为下一步
                String nextNode = neighbors.isEmpty() ? null : neighbors.toArray(new String[0])[random.nextInt(neighbors.size())];

                // 检查是否已经访问过从当前节点到该邻居的边
                String edgeKey = currentNode + "-" + nextNode; // 简化的边表示方法
                if (visitedEdges.contains(edgeKey)) {
                    break; // 如果遇到重复边，则停止游走
                }

                if (nextNode != null) {
                    walkPath.add(nextNode);
                    visitedEdges.add(edgeKey);
                    currentNode = nextNode;
                }
            }

            // 如果用户请求停止，则立即退出
            if (stopRandomWalk) {
                System.out.println("Random walk stopped by user.");
            }

            // 将游走路径转换为字符串
            System.out.println(String.join(" -> ", walkPath));
        } catch (Exception e) {
            System.out.println("No edge exist ,please enter 's'");
        }


    }



    //测试git

    //测试B2


    //测试IDEA
}
