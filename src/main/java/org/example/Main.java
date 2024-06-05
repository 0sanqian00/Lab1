package org.example;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.nio.charset.StandardCharsets;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Collection;
import java.security.SecureRandom;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.FileWriter;
/**
 * Main class for the application.
 */
public final class Main {
    private Main() { }
    /**
     * 表示图中单词之间的有向边.
     * 键是单词。值是该单词可以指向的单词集合.
     */
    private static Map<String, Set<String>> graph;

    /**
     * 存储每个单词出现的频率.
     * 键是单词，值是该单词出现的次数.
     */
    private static Map<String, Integer> wordFrequency;

    /**
     * 存储图中每条边的权重.
     * 键是起始单词，值是另一个映射，其键是目的单词，值是边的权重.
     */
    private static volatile boolean stopRandomWalk = false;
    /**
     * 存储图中每条边的权重.
     * <p>
     * 这个映射包含三个级别的数据结构：
     * <ul>
     *     <li>键（String）：表示图中的一个顶点。</li>
     *     <li>值（Map<String, Integer>）：与每个顶点相关联的映射，表示从该顶点出发到其他顶点的边的权重。</li>
     *     <li>内层映射的键（String）：表示相邻的顶点。</li>
     *     <li>内层映射的值（Integer）：表示从一个顶点到相邻顶点的边的权重。</li>
     * </ul>
     * </p>
     * 例如，{@code edgeWeights.get("A").get("B")} 将返回从顶点 A 到顶点 B 的边的权重。
     *
     * @see #graph
     */
    private static Map<String, Map<String, Integer>> edgeWeights;

    /**
     * 一个用于随机游走的Random实例.
     * 被声明为final以确保在多线程环境下的安全性.
     */
    private static final SecureRandom SECURE_RANDOM; // 使用SecureRandom代替Random
    /**
     * 定义Graphviz图形化工具的路径.
     *
     * 这个路径指向Graphviz安装目录下的可执行文件，用于将DOT语言描述的图形转换成可视化的图片格式。
     * 需要确保该路径正确指向Graphviz的安装目录，并且具有执行权限。
     */
    private static final String GRAPHVIZ_PATH; // 图形化工具路径

    /**
     * 定义生成的图形文件的存储路径.
     *
     * 这个路径用于指定生成的图形文件（如PNG格式）的存放位置。
     * 需要确保该路径是可写的，并且应用程序有足够的权限在该位置创建文件。
     */
    private static final String FILE_PATH; // 文件存储路径
    static {
        SECURE_RANDOM = new SecureRandom(); // 初始化SecureRandom
        GRAPHVIZ_PATH = "D:\\software_lab\\"
                +
                "\\Graphviz-11.0.0-win64\\bin\\dot.exe"; // 将路径设置为常量
        FILE_PATH = "D:\\software_lab\\lab3\\Lab1\\test\\test1.txt";

    }
    /**
     * 表示图中顶点的最大数量.
     * 用于初始化距离矩阵的大小.
     */
    private static int vertex;

    /**
     * 存储顶点对之间的最短路径长度.
     * 二维数组，其中dist[i][j]表示从顶点i到顶点j的最短路径长度.
     */
    private static int[][] dist;

    /**
     * 程序的主入口点，初始化数据结构，读取文件，并提供用户交互菜单.
     * <p>
     * 方法首先创建所需的数据结构，包括图、单词频率和边权重。
     * 然后，它读取并处理一个文本文件来构建图。
     * 最后，它进入一个循环，提供用户一个菜单，用户可以从中选择不同的操作，
     * 如显示有向图、查询桥接词、生成新文本、计算最短路径、执行随机游走或退出程序。
     * </p>
     *
     * @param args 命令行参数，当前未使用。
     */
    public static void main(final String[] args) {
        // 初始化图和单词频率映射
        graph = new HashMap<>();
        wordFrequency = new HashMap<>();
        edgeWeights = new HashMap<>();
        // 读取文本文件并构建图
        readTextFileAndBuildGraph(FILE_PATH);

        InputStream inputStream = System.in;
        Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8);
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
                    System.out.println(
                            "\nGraph built. Displaying directed graph...");
                    showDirectedGraph();
                }
                case '2' -> {
                    System.out.print("Enter word 1: ");
                    String word1 = scanner.nextLine();
                    System.out.print("Enter word 2: ");
                    String word2 = scanner.nextLine();
                    System.out.println("Bridge words from '" + word1 + "' to '"
                            + word2 + "': " + queryBridgeWords(word1, word2));
                }
                case '3' -> {
                    System.out.println(
                            "Enter a line of text to generate new text:");
                    String inputText = scanner.nextLine();
                    String generatedText = generateNewText(
                            inputText, SECURE_RANDOM);
                    System.out.println("Generated new text: "
                            + generatedText);
                }
                case '4' -> {
                    System.out.print("Enter word 1: ");
                    String wordA = scanner.nextLine();
                    System.out.print("Enter word 2: ");
                    String wordB = scanner.nextLine();
                    showDirectedGraphWithShortestPath(
                            graph, edgeWeights, dist,
                            vertex, wordA, wordB);
                }
                case '5' -> {
                    System.out.println(
                            "\nPerforming a random walk... Press 's' to stop.");
                    stopRandomWalk = false; // 重置停止标志
                    // 使用线程执行随机游走
                    Thread randomWalkThread = new Thread(
                            () -> randomWalk(SECURE_RANDOM));
                    randomWalkThread.start();

                    // 等待用户输入以停止游走或返回主菜单
                    while (!stopRandomWalk) {
                        System.out.print(
                                "Enter 's' to stop the random walk"
                                        +
                                        "or just press enter to continue: \n");
                        String input = scanner.nextLine().trim().toLowerCase();
                        if ("s".equals(input)) {
                            stopRandomWalk = true; // 用户请求停止随机游走
                            break;
                        }
                    }
                }
                case '6' -> System.out.println("Exiting program.");
                default -> System.out.println("Invalid choice. "
                        +
                        "Please enter a number between 1 and 6.");
            }
        } while (choice != '6');

        scanner.close();
    }

    /**
     * 从指定的文本文件中读取内容，并构建图和单词频率映射.
     * <p>
     * 该方法使用BufferedReader按行读取文件，并将每一行中的文本转换为小写，
     * 同时将所有非字母字符替换为空白。然后，它将每一行分割成单词，
     * 并将每对连续的单词视为图中的一个有向边。单词频率映射记录每个单词出现的频率，
     * 而边权重映射记录每个单词对之间边的权重。
     * </p>
     *
     * @param filePath 要读取的文本文件的路径。
     * @throws IOException 如果读取文件时发生I/O错误。
     */
    private static void readTextFileAndBuildGraph(final String filePath) {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(filePath),
                        StandardCharsets.UTF_8))) {
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
                    wordFrequency.put(
                            currentWord, wordFrequency.getOrDefault(
                                    currentWord, 0) + 1);

                    // 添加边和更新权重
                    graph.computeIfAbsent(currentWord,
                            k -> new HashSet<>()).add(nextWord);
                    updateWeight(currentWord, nextWord);
                }
            }

            vertex = graph.size();
            dist = new int[vertex][vertex];
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void updateWeight(final String from, final String to) {
        // 获取从from到to的现有权重，如果没有设置，则默认为0
        int currentWeight = edgeWeights.getOrDefault(
                from, new HashMap<>()).getOrDefault(to, 0);

        // 权重加1，因为每次调用此方法意味着A和B又相邻出现了一次
        currentWeight += 1;

        // 更新边的权重
        edgeWeights.computeIfAbsent(
                from, k -> new HashMap<>()).put(to, currentWeight);
    }

    /**
     * 显示有向图的可视化.
     * <p>
     * 该方法首先创建一个DOT文件，该文件定义了图的结构和节点/边的属性。
     * 然后，它使用Graphviz软件的命令行工具将DOT文件转换为图形表示，
     * 通常是一个PNG图像文件。
     * </p>
     * <p>
     * 节点表示单词，边表示单词之间的转移，边的权重表示转移发生的频率。
     * 生成的图像文件将保存在用户的目录下。
     * </p>
     *
     * @see #escapeDotString(String)
     * @see #graph
     * @see #edgeWeights
     */
    public static void showDirectedGraph() {
        // DOT 文件将被创建在用户目录下
        String dotFilePath = "graph.dot";
//        String graphvizPath = "D:\\software_lab"
//                +
//                "\\Graphviz-11.0.0-win64\\bin\\dot.exe";
        String pngFilePath = "D:\\software_lab\\LAB_1\\graph.png";

        // 创建DOT文件
        try {
            // 使用 Paths.get 来创建 Path 对象
            Path path = Paths.get(dotFilePath);
            // 使用 try-with-resources 确保文件被正确关闭
            try (PrintWriter out = new PrintWriter(
                    Files.newBufferedWriter(path, StandardCharsets.UTF_8))) {
                out.println("digraph G {");
                out.println("  rankdir=LR;"); // 设置图的方向从左到右

                // 添加节点
                for (String node : graph.keySet()) {
                    out.println(
                            "  \"" + escapeDotString(node)
                                    + "\" [shape=circle];");
                }

                // 添加边和权重
                for (Map.Entry<String, Set<String>> entry : graph.entrySet()) {
                    String fromNode = entry.getKey();
                    for (String toNode : entry.getValue()) {
                        // 获取边的权重
                        int weight = edgeWeights.getOrDefault(
                                fromNode,
                                new HashMap<>()).getOrDefault(toNode, 0);
                        // 将权重作为标签添加到边
                        out.printf(
                                "  \"%s\" -> \"%s\" [label=\"%d\"];%n",
                                escapeDotString(fromNode),
                                escapeDotString(toNode), weight);
                    }
                }

                out.println("}");
            } // try-with-resources 自动关闭文件
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 使用Graphviz命令行工具生成图形
        try {
            // 使用 ProcessBuilder 来避免命令注入风险
            ProcessBuilder processBuilder = new ProcessBuilder(
                    GRAPHVIZ_PATH, "-Tpng", dotFilePath, "-o", pngFilePath
            );

            // 启动进程
            Process process = processBuilder.start();

            // 等待进程执行完成
            process.waitFor();

            // 检查进程是否成功执行
            if (process.exitValue() == 0) {
                System.out.println(
                        "Graph visualization"
                                + "generated as '" + pngFilePath + "'");
            } else {
                System.err.println("Error generating graph visualization.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            // 处理进程被中断的异常
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
    }

    // 转义DOT语言特殊字符
    private static String escapeDotString(final String input) {
        return input.replace("\"", "\\\"");
    }


    /**
     * 查询两个单词之间的桥接词.
     * <p>
     * 桥接词是指在图中既直接跟随第一个单词又直接被第二个单词跟随的单词。
     * 如果图中不包含指定的单词或没有桥接词，则返回相应的信息。
     * </p>
     *
     * @param word1 第一个单词，作为桥接词搜索的起点。
     * @param word2 第二个单词，作为桥接词搜索的终点。
     * @return 包含所有桥接词的字符串，如果没有桥接词则返回相应的信息。
     */
    public static String queryBridgeWords(
            final String word1, final String word2) {
        if (!graph.containsKey(word1) || !graph.containsKey(word2)) {
            return "No " + word1 + " or " + word2 + " in the graph!";
        }

        Set<String> bridgeWords = new HashSet<>();
        Set<String> successorsWord1 = graph.get(word1);
        Set<String> predecessorsWord2
                = new HashSet<>(graph.size()); // 记录word2的前驱节点
        // 寻找word2的前驱节点
        //直接遍历 entrySet()，因为 entrySet() 迭代器提供了每个键和对应值的访问，无需额外的查找。
        for (Map.Entry<String, Set<String>> entry : graph.entrySet()) {
            if (entry.getValue().contains(word2)) {
                predecessorsWord2.add(entry.getKey());
            }
        }
//        Set<String> bridgeWords = new HashSet<>();
//        Set<String> successorsWord1 = graph.get(word1);
//        Set<String> predecessorsWord2 = new HashSet<>(); // 记录word2的前驱节点
//
//        // 寻找word2的所有前驱节点
//        for (String key : graph.keySet()) { // keySet() 方法来获取所有键的集合
//            Set<String> successors = graph.get(key); //调用了 get() 方法来获取对应的值
//            if (successors.contains(word2)) {
//                predecessorsWord2.add(key);
//            }
//        }
        // 遍历word1的所有后继节点，检查是否也是word2的前驱节点
        for (String successor : successorsWord1) {
            if (predecessorsWord2.contains(successor)) {
                bridgeWords.add(successor);
            }
        }

        if (bridgeWords.isEmpty()) {
            return "No bridge words from " + word1 + " to " + word2 + "!";
        } else {
            return "The bridge words from " + word1 + " to " + word2 + " are: "
                    + String.join(", ", bridgeWords);
        }
    }


    /**
     * 生成一个新的文本，其中在每对相邻单词之间插入一个随机的桥接词.
     *
     * 此方法接受一个输入文本，并使用SecureRandom生成随机数来选择桥接词。
     * 桥接词是通过调用queryBridgeWords方法查询得到的，该方法返回两个单词之间的所有可能桥接词。
     * 如果没有桥接词可用，则不插入任何词，直接连接相邻的单词。
     *
     * @param inputText 输入文本，将被分割成单词并插入桥接词。
     * @param random 安全随机数生成器，用于从桥接词列表中随机选择一个词。
     * @return 新生成的文本，其中每对相邻单词之间插入了一个随机的桥接词。
     */
    public static String generateNewText(
            final String inputText, final SecureRandom random) {
        // 使用空格分割输入文本，得到单词数组
        String[] words = inputText.toLowerCase().split("\\s+");

        // 结果列表，用于存储新文本的单词
        List<String> newWords = new ArrayList<>();

//        // 用于随机选择桥接词
//        Random random = new Random();

        // 遍历单词数组，查找每对相邻单词的桥接词
        for (int i = 0; i < words.length - 1; i++) {
            // 添加当前单词
            newWords.add(words[i]);

            // 查询这对相邻单词的桥接词
            String bridgeWordsResult = queryBridgeWords(words[i], words[i + 1]);
            // 如果桥接词结果以"No bridge words"开头，则表示没有桥接词
            if (bridgeWordsResult.startsWith("No")) {
                // 不插入任何单词，继续
                continue;
            }

            // 如果有桥接词，将其添加到结果列表
            // 桥接词以逗号分隔，我们需要分割并随机选择一个
            String[] bridgeWordArray = bridgeWordsResult.substring(
                    bridgeWordsResult.indexOf(':') + 2).trim().split(", ");
            // 随机选择一个桥接词
            String bridgeWord =
                    bridgeWordArray[random.nextInt(bridgeWordArray.length)];
            newWords.add(bridgeWord);
        }

        // 添加最后一个单词
        newWords.add(words[words.length - 1]);

        // 将结果列表转换为字符串，使用空格连接单词
        return String.join(" ", newWords);
    }



    /**
     * 计算图中两个单词之间的最短路径.
     *
     * 此方法使用Floyd-Warshall算法来计算图中所有顶点对之间的最短路径，
     * 然后提取两个指定单词之间的最短路径，并以字符串形式返回。
     * 如果两个单词不在图中，或者它们之间没有路径，则返回相应的错误消息。
     *
     * @param graphcalc 图的表示，键是单词，值是与该单词相连的其他单词的集合。
     * @param edgeWeightscalc 边的权重映射，键是起点单词，值是到其他单词的权重映射。
     * @param distcalc 距离矩阵，表示任意两个单词之间的最短距离。
     * @param vertexcalc 图中单词的总数。
     * @param word1 起始单词。
     * @param word2 目标单词。
     * @return 两个单词之间的最短路径的描述，或者错误消息。
     */
    public static String calcShortestPath(
            final Map<String, Set<String>> graphcalc,
            final Map<String, Map<String, Integer>>
                    edgeWeightscalc,
            final int[][] distcalc,
                                          final int vertexcalc,
                                          final String word1,
                                          final String word2) {
        if (!graphcalc.containsKey(word1) || !graphcalc.containsKey(word2)) {
            return "No " + word1 + " or " + word2 + " in the graph!";
        }
        // 转换成邻接矩阵，这里假设边的权重为1
        for (int i = 0; i < vertexcalc; i++) {
            for (int j = 0; j < vertexcalc; j++) {
                distcalc[i][j] = (i == j) ? 0 : Integer.MAX_VALUE;
            }
        }
        // 填充邻接矩阵
        for (Map.Entry<String, Set<String>> entry : graph.entrySet()) {
            String word = entry.getKey();
            Map<String, Integer> weights = edgeWeightscalc.get(word);
            if (weights != null) {
                for (
                        Map.Entry<String,
                                Integer> weightEntry : weights.entrySet()) {
                    String neighbor = weightEntry.getKey();
                    int weight = weightEntry.getValue();
                    int index1 = getIndex(word);
                    int index2 = getIndex(neighbor);
                    if (index1 != -1 && index2 != -1) {
                        distcalc[index1][index2] = weight; // 设置边的权重
                    }
                }
            }
        }
        // 弗洛伊德算法填充所有顶点对的最短路径
        for (int k = 0; k < vertexcalc; k++) {
            for (int i = 0; i < vertexcalc; i++) {
                for (int j = 0; j < vertexcalc; j++) {
                    if (distcalc[i][k] != Integer.MAX_VALUE
                            && distcalc[k][j] != Integer.MAX_VALUE
                            && dist[i][k] + distcalc[k][j] < dist[i][j]) {
                        distcalc[i][j] = distcalc[i][k] + distcalc[k][j];
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
        // 提取最短路径
        List<String> shortestPath = extractShortestPath(
                graph, dist, vertexcalc, word1, word2);
        // 检查是否有路径
        if (shortestPath.size() < 2) {
            return "No path between " + word1 + " and " + word2 + ".";
        }
        // 使用StringBuilder构建带箭头的路径字符串
        StringBuilder pathWithArrows = new StringBuilder(
                "The shortest path from ").append(word1).append(" to ")
                .append(word2).append(" is: ");
        for (int i = 0; i < shortestPath.size(); i++) {
            pathWithArrows.append(shortestPath.get(i));
            if (i < shortestPath.size() - 1) {
                pathWithArrows.append(" → ");
            }
        }
        System.out.println(pathWithArrows.toString()); // 打印带箭头的路径
        // 返回word1和word2之间的最短路径长度
        return "The shortest path distance from "
                + word1 + " to " + word2 + " is: "
                + dist[index1][index2];
    }


    /**
     * 显示有向图并高亮显示两个指定单词之间的最短路径.
     *
     * 此方法首先计算两个单词之间的最短路径，然后使用Graphviz工具生成一个图形文件，
     * 其中最短路径上的节点和边将以特殊样式显示。
     *
     * @param graphshow 图的表示，键是节点，值是与该节点相连的其他节点的集合。
     * @param edgeWeightsshow 边的权重映射，键是起点节点，值是到其他节点的权重映射。
     * @param distshow 距离矩阵，表示任意两个节点之间的最短距离。
     * @param vertexshow 图中节点的总数。
     * @param word1 起始单词节点。
     * @param word2 目标单词节点。
     */
    public static void showDirectedGraphWithShortestPath(
            final Map<String, Set<String>> graphshow,
            final Map<String, Map<String, Integer>> edgeWeightsshow,
            final int[][] distshow,
            final int vertexshow,
            final String word1, final String word2) {
        String pngFilePath = "D:\\software_lab\\LAB_1\\graph.png";
        // 首先，计算最短路径
        String shortestPathResult = calcShortestPath(
                graphshow, edgeWeightsshow, distshow, vertexshow, word1, word2);
        if (!shortestPathResult.startsWith("The shortest path distance")) {
            System.out.println(shortestPathResult);
            return;
        }

        // 然后，创建DOT文件并添加常规的图结构
        String dotFilePath = "graph.dot";
        try (PrintWriter out = new PrintWriter(
                new FileWriter(dotFilePath, StandardCharsets.UTF_8))) {
            out.println("digraph G {");
            out.println("  rankdir=LR;");
            out.println("  node[shape=circle];");

            // 添加节点
            for (String node : graphshow.keySet()) {
                out.printf(
                        "  \"%s\" [style=filled, fillcolor=lightgray];%n",
                        escapeDotString(node));
            }

            // 添加边
            for (Map.Entry<String,
                    Set<String>> entry : graphshow.entrySet()) {
                String fromNode = entry.getKey();
                for (String toNode : entry.getValue()) {
                    int weight = edgeWeightsshow.getOrDefault(
                            fromNode,
                            new HashMap<>()).getOrDefault(toNode, 0);
                    out.printf(
                            "  \"%s\" -> \"%s\" [label=\"%d\"];%n",
                            escapeDotString(fromNode),
                            escapeDotString(toNode),
                            weight);
                }
            }

            // 根据最短路径算法结果，高亮显示最短路径上的边和节点
            // 此处需要根据calcShortestPath方法的具体实现来确定最短路径上的节点和边
            // 假设 shortestPath 是一个包含最短路径上节点的列表
            List<String> shortestPath =
                    extractShortestPath(
                            graphshow,
                            distshow,
                            vertexshow,
                            word1,
                            word2); // 需要实现这个方法

            for (String node : shortestPath) {
                out.printf(
                        "  \"%s\" [style=filled, fillcolor=red];%n",
                        escapeDotString(node));
            }
            for (int i = 0; i < shortestPath.size() - 1; i++) {
                String fromNode = shortestPath.get(i);
                String toNode = shortestPath.get(i + 1);
                out.printf("  \"%s\" -> \"%s\" [color=red, style=bold];%n",
                        escapeDotString(fromNode), escapeDotString(toNode));
            }

            out.println("}");
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // 使用Graphviz命令行工具生成图形
        try {
            // 使用ProcessBuilder来避免命令行注入
            ProcessBuilder processBuilder = new ProcessBuilder(
                    GRAPHVIZ_PATH, "-Tpng", dotFilePath, "-o", pngFilePath);
            Process process = processBuilder.start();
            process.waitFor();
            System.out.println(
                    "Graph with highlighted shortest path "
                            +
                            "generated as '" + pngFilePath + "'");
        } catch (IOException | InterruptedException e) {
            // 记录错误并给用户一个通用的错误消息
            e.printStackTrace();
            // 可能还需要记录错误或通知管理员
        }
    }

    /**
     * 提取图中两个单词之间的最短路径.
     *
     * 此方法通过给定的图和距离矩阵，找到两个单词之间的最短路径。
     * 图以Map的形式给出，其中键是单词，值是与该单词相连的其他单词的集合。
     * 距离矩阵是一个二维数组，表示任意两个节点之间的距离。
     *
     * @param graphextract 表示图的Map，键是单词，值是与该单词相连的其他单词的集合。
     * @param distextract 表示节点间距离的二维数组。
     * @param vertexextract 图中节点的总数。
     * @param word1 起始单词。
     * @param word2 目标单词。
     * @return 包含最短路径上所有单词的List。
     */
    public static List<String> extractShortestPath(
            final Map<String, Set<String>> graphextract,
            final int[][] distextract,
            final int vertexextract,
            final String word1, final String word2) {
        List<String> path = new ArrayList<>();
        Map<String, Integer> indexMap = new HashMap<>();

        // 首先构建一个从索引到单词的映射
        for (int i = 0; i < vertexextract; i++) {
            String word = (String) graphextract.keySet().toArray()[i];
            indexMap.put(word, i);
        }

        int index1 = indexMap.get(word1);
        int index2 = indexMap.get(word2);

        // 从word1开始，正向追踪最短路径
        int at = index1;
        path.add(word1); // 添加起始节点
        int flag = 0;
        while (at != index2 && flag != vertexextract) {
            flag = 0;
            for (int to = 0; to < vertexextract; to++, flag++) {
                if (distextract[at][to] == 1
                        && distextract[to][index2] != Integer.MAX_VALUE) {
                    at = to;
                    path.add((String) graphextract.keySet().toArray()[at]);
                    break;
                }
            }
        }

        return path;
    }


    /**
     * 根据单词在图中查找其对应的索引.
     * <p>
     * 此方法遍历图的键（即单词），并检查每个单词是否与给定的单词匹配。
     * 如果找到匹配的单词，返回其在图中的索引。如果图中没有该单词，
     * 方法返回-1，表示单词不在图中。
     * </p>
     *
     * @param word 需要查找索引的单词。
     * @return 单词在图中的索引，如果单词不存在则返回-1。
     * @see #graph
     */
    private static int getIndex(final String word) {
        int index = 0;
        for (String w : graph.keySet()) {
            if (w.equals(word)) {
                return index;
            }
            index++;
        }
        return -1;
    }

    /**
     * 执行一个随机游走过程在图上.
     *
     * 随机游走从一个随机节点开始，然后逐步随机选择一个未访问过的邻居节点。
     * 游走持续进行，直到所有节点都被访问过或用户请求停止。
     * 每次游走的路径会被打印出来。
     *
     * @param random 安全随机数生成器，用于随机选择节点和邻居。
     */
    public static void randomWalk(final SecureRandom random) {

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
                String nextNode
                        = neighbors.isEmpty() ? null : neighbors
                        .toArray(new String[0])
                        [SECURE_RANDOM.nextInt(neighbors.size())];

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


}
