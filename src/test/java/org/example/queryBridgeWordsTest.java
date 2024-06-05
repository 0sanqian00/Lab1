package org.example;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class queryBridgeWordsTest {
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



    @BeforeClass
    public static void init() {
        graph = new HashMap<>();
        wordFrequency = new HashMap<>();
        edgeWeights = new HashMap<>();
        Main.readTextFileAndBuildGraph("C:\\Users\\三谦\\Desktop\\软件工程\\Lab1\\Lab1\\test\\test1.txt", graph, wordFrequency, edgeWeights);

    }

    // 测试用例1: word1和word2都在图中，存在桥接词
    @Test
    public void test1() {
        String result = Main.queryBridgeWords(graph,"sun", "horizon");
        System.out.println(result);
        assertTrue(result.contains("is")&&result.contains("was"));
    }

    // 测试用例2: word1和word2都在图中，不存在桥接词
    @Test
    public void test2() {
        String result = Main.queryBridgeWords(graph,"sky", "evening");
        System.out.println(result);
        assertEquals("No bridge words from sky to evening!", result);
    }

    // 测试用例3: word1和word2都在图中，存在桥接词
    @Test
    public void test3() {
        String result = Main.queryBridgeWords(graph,"breeze", "wind");
        System.out.println(result);
        assertTrue(result.contains("evening")&&result.contains("in"));
    }

    // 测试用例4: word1和word2都在图中，不存在桥接词
    @Test
    public void test4() {
        String result = Main.queryBridgeWords(graph,"tranquil", "canvas");
        System.out.println(result);
        assertEquals("No bridge words from tranquil to canvas!", result);
    }

    // 测试用例5: word1和word2都不是英语单词（无效输入）
    @Test
    public void test5() {
        String result = Main.queryBridgeWords(graph,"123", "456");
        System.out.println(result);
        assertEquals("No 123 or 456 in the graph!", result);
    }

    // 测试用例6: word1和word2都是图中不存在的单词
    @Test
    public void test6() {
        String result = Main.queryBridgeWords(graph,"test", "abc");
        System.out.println(result);
        assertEquals("No test or abc in the graph!", result);
    }
    // 测试用例7: word1是图中不存在的单词，而word2是图中存在的单词
    @Test
    public void test7() {
        String result = Main.queryBridgeWords(graph,"xyz", "horizon");
        System.out.println(result);
        assertEquals("No xyz or horizon in the graph!", result);
    }

    // 测试用例8: Word2是图中存在的单词，而word1是图中不存在的单词
    @Test
    public void test8() {
        String result = Main.queryBridgeWords(graph,"sky", "abc");
        System.out.println(result);
        assertEquals("No sky or abc in the graph!", result);
    }

}