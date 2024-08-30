package com.nowcoder.community.util;

import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    //敏感词替换符号
    private static final String REPLACEMENT = "***";

    public String filter(String text) {
        if(StringUtils.isBlank(text)) {
            return text;
        }

        StringBuilder stringBuilder = new StringBuilder();

        //指针1（指向前缀树）
        TrieNode tempNode = rootNode;

        //指针2和3（指向待过滤文本，分别指向某一个词的开头和结尾）
        int begin = 0;
        int position = 0;

        //文本过滤
        while(position < text.length()) {
            char c = text.charAt(position);

            //跳过特殊字符
            if(isSymbol(c)) {
                if(tempNode == rootNode) {
                    stringBuilder.append(c);
                    begin++;
                }
                position++;
                continue;
            }

            tempNode = tempNode.getSubNode(c);

            if(tempNode == null) {
                //如果该字符没有对应前缀树的下一个节点，说明begin指针指向的字符的所有后缀都不是敏感词，可以把该字符加入过滤后的文本
                stringBuilder.append(text.charAt(begin));
                position = ++begin;
                tempNode = rootNode;
            }else if(tempNode.isKeywordEnd()) {
                //如果该字符对应的节点被打上标记，说明begin~position是敏感词，过滤后，把指针2、3都指向position下一个字符，继续判断
                stringBuilder.append(REPLACEMENT);
                begin = ++position;
                tempNode = rootNode;
            }else {
                position++;
            }
        }
        //退出循环后，如果text末尾不是敏感词，则需要将其输出
        stringBuilder.append(text.substring(begin));
        return stringBuilder.toString();
    }

    //判断是否为符号（用于文本敏感词过滤时跳过符号判断）
    private boolean isSymbol(Character c) {
        //0x2E80~0x9FFF为东亚文字范围，因此检测该范围外的特殊字符（否则会把这些文字也识别为特殊字符）
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    //将敏感词文件构建为前缀树
    @PostConstruct
    public void init() {
        try (
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive_words.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        ){
            String keyword;
            while((keyword = reader.readLine()) != null) {
                this.addKeyword(keyword);
            }
        } catch (IOException e) {
            logger.error("加载敏感词文件失败： " + e.getMessage());
        }
    }

    //将一个敏感词添加到前缀树中
    private void addKeyword(String keyword) {
        TrieNode tempNode = rootNode;
        for(int i = 0; i < keyword.length(); i++) {
            char c = keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);

            //判断该字符是否已经是子节点，如果不是，则将该字符加入
            if(subNode == null) {
                subNode = new TrieNode();
                tempNode.addSubNode(c, subNode);
            }

            //指向子节点，进入下一轮循环
            tempNode = subNode;

            //为该敏感词最后一个字符打上标记
            if(i == keyword.length() - 1) {
                tempNode.setKeywordEnd(true);
            }
        }
    }

    //根节点
    private TrieNode rootNode = new TrieNode();

    //前缀树
    private class TrieNode {
        //关键词结束标识
        private boolean isKeywordEnd = false;

        //子节点
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        //添加子节点
        public void addSubNode(Character c, TrieNode node) {
            subNodes.put(c, node);
        }

        //获取子节点
        public TrieNode getSubNode(Character c) {
            return subNodes.get(c);
        }
    }
}
