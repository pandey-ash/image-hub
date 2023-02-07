package com.cloud.assignment;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.cloud.controller.HomeController;

@SpringBootTest
@AutoConfigureMockMvc
class AssignmentApplicationTests
{

	@Autowired
	HomeController homeController;
	
	@Test
	void contextLoads() {
	}
	
	@Test
	public void health()
	{
		assertEquals( "", homeController.getResponse());
	}

}
