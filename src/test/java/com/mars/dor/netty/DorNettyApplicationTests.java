package com.mars.dor.netty;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

//https://blog.csdn.net/qq_37576449/article/details/120431031?utm_medium=distribute.pc_relevant.none-task-blog-2~default~baidujs_baidulandingword~default-0-120431031-blog-80086145.pc_relevant_default&spm=1001.2101.3001.4242.1&utm_relevant_index=2
@SpringBootTest
@RunWith(SpringRunner.class)
class DorNettyApplicationTests {

	@Resource
	RedisTool redisTool;

	@Test
	void contextLoads() {
	}
	@Test
	public void testget(){
		System.out.println(redisTool.get("online-user-info:9529"));
	}

}
