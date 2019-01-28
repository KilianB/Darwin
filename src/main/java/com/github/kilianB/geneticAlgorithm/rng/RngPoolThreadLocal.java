package com.github.kilianB.geneticAlgorithm.rng;

import java.util.Random;
import java.util.function.Supplier;

import com.github.kilianB.pcg.fast.PcgRSFast;


/**
 * A class offering synchronized ... using multiple to avoid congestion.
 * 
 * @author Kilian
 *
 */
public class RngPoolThreadLocal implements RngPool{

	private final ThreadLocal<Random> threadRNG;
	
	/**
	 * @param supplier
	 */
	public RngPoolThreadLocal(Supplier<? extends Random> supplier) {
		threadRNG = ThreadLocal.withInitial(supplier);
	}

	@Override
	public void nextBytes(byte[] bytes) {
		threadRNG.get().nextBytes(bytes);
	}

	@Override
	public int nextInt() {
		return threadRNG.get().nextInt();
	}

	@Override
	public int nextInt(int n) {
		return threadRNG.get().nextInt(n);
	}

	@Override
	public long nextLong() {
		return threadRNG.get().nextLong();
	}

	@Override
	public boolean nextBoolean() {
		return threadRNG.get().nextBoolean();
	}

	@Override
	public float nextFloat() {
		return threadRNG.get().nextFloat();
	}

	@Override
	public double nextDouble() {
		return threadRNG.get().nextDouble();
	}
	
	@Override
	public double nextGaus() {
		//TODO NaN check for debug purposes
		double gaus = threadRNG.get().nextGaussian();
		assert !Double.isNaN(gaus) : "Gaus produced NaN value";
		return gaus;
	}

	@Override
	public Random getUnderlayingRNG() {
		return threadRNG.get();
	}
	
	
	public static RngPoolThreadLocal pcgRS() {
		return new RngPoolThreadLocal(()-> {return new PcgRSFast();});
	}
	
	public static RngPoolThreadLocal mersenneTwister() {
		return new RngPoolThreadLocal(()-> {return new  MersenneTwisterFast();});
	}
	
	
}
