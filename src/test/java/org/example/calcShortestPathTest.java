package org.example;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class calcShortestPathTest {
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
    private static Map<String, Map<String, Integer>> edgeWeights;
    /**
     * 存储顶点对之间的最短路径长度.
     * 二维数组，其中dist[i][j]表示从顶点i到顶点j的最短路径长度.
     */

    private static int[][] dist;

    /**
     * 表示图中顶点的最大数量.
     * 用于初始化距离矩阵的大小.
     */
    private static int V;

    @BeforeClass
    public static void init() {
        graph = new HashMap<>();
        wordFrequency = new HashMap<>();
        edgeWeights = new HashMap<>();
        Main.readTextFileAndBuildGraph("C:\\Users\\三谦\\Desktop\\软件工程\\Lab1\\Lab1\\test\\test2.txt", graph, wordFrequency, edgeWeights);
        V = graph.size();
        dist = new int[V][V];

    }
    // 测试用例1：不存在的节点
    @Test
    public void test1() {
        String word1 = "apple";
        String word2 = "banana";

        // 假设这两个词不在我们的图中
        String result = Main.calcShortestPath(graph, edgeWeights, dist, V, word1, word2);
        assertEquals("No " + word1 + " or " + word2 + " in the graph!", result);
    }


    // 测试用例2：相同节点的最短路径
    @Test
    public void test2() {
        String word1 = "sun";
        String word2 = "sun";
        String result = Main.calcShortestPath(graph, edgeWeights, dist, 1, word1, word2);
        assertEquals(word1 + " = " + word2 + " The shortest path distance is 0.", result);
    }

    // 测试用例3：存在节点但无路径（不连通）
    @Test
    public void test3() {
        String word1 = "and";
        String word2 = "sun";
        // 假设这两个词在图中但不连通

        String result = Main.calcShortestPath(graph, edgeWeights, dist, V, word1, word2);
        assertEquals("No path between " + word1 + " and " + word2 + ".", result);
    }

    // 测试用例4：存在节点并有路径
    @Test
    public void test4() {
        String word1 = "sun";
        String word2 = "horizon";
        // 假设这两个词在图中且连通

        String result = Main.calcShortestPath(graph, edgeWeights, dist, V, word1, word2);
        assertTrue(result.startsWith("The shortest path distance ") && result.contains(word1) && result.contains(word2));
    }
}