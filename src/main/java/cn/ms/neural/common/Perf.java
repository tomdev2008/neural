package cn.ms.neural.common;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class Perf implements Closeable {
	
	public static abstract class TaskInThread implements Closeable {
		public void initTask() throws Exception {
		}

		public abstract void doTask() throws Exception;

		public void close() throws IOException {
		}
	}

	private static final Logger log = LogManager.getLogger(Perf.class);
	public int threadCount = 16;
	public int loopCount = 1000000;
	public int logInterval = 0;
	public long startTime;
	public AtomicLong counter = new AtomicLong(0);
	public AtomicLong failCounter = new AtomicLong(0);

	public abstract TaskInThread buildTaskInThread();

	public void run() throws Exception {
		this.startTime = System.currentTimeMillis();
		TaskThread[] tasks = new TaskThread[threadCount];
		for (int i = 0; i < tasks.length; i++) {
			TaskInThread t = buildTaskInThread();
			t.initTask();
			tasks[i] = new TaskThread(t);
		}

		for (TaskThread task : tasks) {
			task.start();
		}
		for (TaskThread task : tasks) {
			task.join();
			task.task.close();
		}

		log.info("===done===");
	}

	public void close() throws IOException {

	}

	class TaskThread extends Thread {
		private TaskInThread task;

		public TaskThread(TaskInThread task) {
			this.task = task;
		}

		public void run() {
			long logInterval = Perf.this.logInterval;
			if (logInterval <= 0) {
				logInterval = threadCount * loopCount / 10;
			}
			for (int i = 0; i < loopCount; i++) {
				try {
					long count = counter.incrementAndGet();
					task.doTask();
					if (count % logInterval == 0) {
						long end = System.currentTimeMillis();
						String qps = String.format("%.4f", count * 1000.0 / (end - startTime));
						log.info("QPS: " + qps + ", Failed/Total=" + failCounter.get() + "/" + counter.get() + "(" + failCounter.get() * 1.0 / counter.get() * 100 + ")");
					}
				} catch (Exception e) {
					failCounter.incrementAndGet();
					log.info(e.getMessage(), e);
					log.info("total failure " + failCounter.get() + " of " + counter.get() + " request");
				}
			}
		}
	}

}
