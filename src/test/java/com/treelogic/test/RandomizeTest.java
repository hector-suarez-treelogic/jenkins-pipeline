package com.treelogic.test;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.Random;

import org.junit.jupiter.api.Test;

public class RandomizeTest {

	@Test
	public void testRandom() {
		if (randomNumberGenerator() < 0.2) {
			fail("Test Fail");
		}
		System.out.println("Test Ok - ");
	}

	private double randomNumberGenerator()
	{
	    double rangeMin = 0.0f;
	    double rangeMax = 1.0f;
	    Random r = new Random();
	    double createdRanNum = rangeMin + (rangeMax - rangeMin) * r.nextDouble();
	    return(createdRanNum);
	}
}
