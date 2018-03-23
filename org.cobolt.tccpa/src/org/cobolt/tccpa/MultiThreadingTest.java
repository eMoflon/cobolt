package org.cobolt.tccpa;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * ?? Is reading concurrently from an EMF model safe? I guess not. ?? Wouldn't
 * it be better to use a database (e.g., NeoEMF)?
 *
 * Basic idea: * Establish a global lock for the entire model * This is
 * different from the lock on the model level! * Question 1: Where to place lock
 * and unlock pairs? * We cannot simply lock before and after each 'pattern'
 * invocation. * See the following pseudo-code LOCK create match pattern_A_black
 * if(failed) throw pattern_A_green pattern_A_red UNLOCK
 *
 * LOCK create match if/while(pattern_A_black) { pattern_A_green pattern_A_red
 * bind variables UNLOCK // ... }
 *
 * LOCK create match pattern do {pattern_A_black) { pattern_A_green
 * pattern_A_red bind variables UNLOCK
 *
 * LOCK pattern_A_black }
 *
 * * Question 2: How to insert the locks? * Modify code generator (would be
 * parallel to what we did until now)
 *
 * * Question 3: How to realize local views? * Create separate Resources for
 * each process * Synchronization * Target platform: No problems with
 * concurrency. Just use synchronized lists. Allows to use network simulator (<-
 * is this sensible? CPU speed is vastly different from simulation speed!) *
 * Using GT: Allows static analysis, stochastic GT, ... Fits the spirit of MDE
 * better. * Holistic approach: hide behind EOperation: Pure spec.: use GT;
 * Simulation: use simulator; Testbed: use messaging API of sensor OS
 * 
 * @author Roland Kluge - Initial implementation
 *
 */
public class MultiThreadingTest {
	private static final ReentrantLock mutex = new ReentrantLock();

	// create a fair read/write lock
	final static ReadWriteLock rwLock = new ReentrantReadWriteLock(true);

	// the main thread grabs the write lock to exclude other threads
	final static Lock writeLock = rwLock.writeLock();

	// All other threads hold the read lock whenever they do
	// *anything* to make sure the writer is exclusive when
	// it is running. NOTE: the other threads must also
	// occasionally *drop* the lock so the writer has a chance
	// to run!
	final static Lock readLock = rwLock.readLock();

	public static void main(String[] args) {
		// Thread interruptor1 = new Thread(() -> interruptorMethod());
		// interruptor1.start();
		new Thread(() -> modificationOperation("A")).start();
		new Thread(() -> modificationOperation("B")).start();
		new Thread(() -> modificationOperation("C")).start();
		new Thread(() -> modificationOperation("D")).start();
		new Thread(() -> modificationOperation("E")).start();
		new Thread(() -> modificationOperation("F")).start();
		new Thread(() -> modificationOperation("G")).start();
		new Thread(() -> modificationOperation("H")).start();

	}

	private static void modificationOperation(String name) {
		while (true) {
			mutex.lock();
			try {
				System.out.print(name + "...");
				long sum = 0;
				int maxValue = (int) 1e9;
				for (int i = 0; i < maxValue; ++i) {
					sum += i;
				}
				System.out.println(name + ": " + sum);
			} finally {
				mutex.unlock();
			}
			// try
			// {
			// Thread.sleep(50);
			// } catch (InterruptedException e)
			// {
			// e.printStackTrace();
			// }
		}
	}
}