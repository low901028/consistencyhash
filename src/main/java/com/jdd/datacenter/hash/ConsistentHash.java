package com.jdd.datacenter.hash;

import java.util.Collection;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * 一致性hash
 * @author Administrator
 * http://blog.csdn.net/icefireelf/article/details/5796529 (关于字符串hash相关计算方法)
 * http://blog.csdn.net/sparkliang/article/details/5279393 (一致性hash的说明)
 * @param <T>
 */
public class ConsistentHash <T> {
	/** Hash计算对象 */
	HashAlgorithm hashFunc;
	
	/** 复制的节点个数  */
	private final int numberOfReplicas;
	
	/** 一致性Hash环 */
	private final SortedMap<Integer, T> circle = new TreeMap<Integer, T>();
	
	/**
	 * @param numberOfReplicas 复制的节点个数，增加每个节点的复制节点有利于负载均衡
	 * @param nodes 节点对象
	 */
	public ConsistentHash(int numberOfReplicas, Collection<T> nodes) {
		this.numberOfReplicas = numberOfReplicas;
		this.hashFunc = HashAlgorithm.KETAMA_HASH;
		//初始化节点
		for (T node : nodes) {
			add(node);
		}
	}

	/**
	 * 构造
	 * @param hashFunc hash算法对象
	 * @param numberOfReplicas 复制的节点个数，增加每个节点的复制节点有利于负载均衡
	 * @param nodes 节点对象
	 */
	public ConsistentHash(HashAlgorithm hashFunc, int numberOfReplicas, Collection<T> nodes) {
		this.numberOfReplicas = numberOfReplicas;
		this.hashFunc = hashFunc;
		//初始化节点
		for (T node : nodes) {
			add(node);
		}
	}

	/**
	 * 增加节点<br>
	 * 每增加一个节点，就会在闭环上增加给定复制节点数<br>
	 * 例如复制节点数是2，则每调用此方法一次，增加两个虚拟节点，这两个节点指向同一Node
	 * 由于hash算法会调用node的toString方法，故按照toString去重
	 * @param node 节点对象
	 */
	public void add(T node) {
		for (int i = 0; i < numberOfReplicas; i++) {
			circle.put(hashFunc.hash(node.toString() + "-" + i), node);
		}
	}

	/**
	 * 移除节点的同时移除相应的虚拟节点
	 * @param node 节点对象
	 */
	public void remove(T node) {
		for (int i = 0; i < numberOfReplicas; i++) {
			circle.remove(hashFunc.hash(node.toString() + "-" + i));
		}
	}

	/**
	 * 获得一个最近的顺时针节点
	 * @param key 为给定键取Hash，取得顺时针方向上最近的一个虚拟节点对应的实际节点
	 * @return 节点对象
	 */
	public T get(Object key) {
		if (circle.isEmpty()) {
			return null;
		}
		int hash = hashFunc.hash(key);
		if (!circle.containsKey(hash)) {
			SortedMap<Integer, T> tailMap = circle.tailMap(hash);	//返回此映射的部分视图，其键大于等于 hash
			hash = tailMap.isEmpty() ? circle.firstKey() : tailMap.firstKey();
		}
		//正好命中
		return circle.get(hash);
	}
	
	
	public String getNodeHashDesc(){
		return this.circle.toString() ;
	}
}
