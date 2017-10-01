package zan.spacetec.core;

import static org.lwjgl.glfw.GLFW.*;

import org.lwjgl.glfw.GLFWErrorCallback;

public class Engine implements Runnable {

	private static final long NANOS_PER_SECOND = 1_000_000_000L;

	private Window window;
	private Input input;
	private Module module;

	private int targetFPS;
	private int targetUPS;

	private int currentFPS;
	private int currentUPS;

	public Engine(Window window, Input input, Module module, int targetFPS, int targetUPS) {
		this.window = window;
		this.input = input;
		this.module = module;
		this.targetFPS = targetFPS;
		this.targetUPS = targetUPS;
	}

	public Engine(int targetFPS, int targetUPS) {
		this(null, null, null, targetFPS, targetUPS);
	}

	public void start() {
		Thread thread = new Thread(this, "engine");
		String os = System.getProperty("os.name");
		if (os.contains("Mac")) {
			thread.run();
		} else {
			thread.start();
		}
	}

	@Override
	public void run() {
		init();
		loop();
		exit();
	}

	private void init() {
		GLFWErrorCallback.createPrint(System.err).set();
		if (!glfwInit()) throw new IllegalStateException("Unable to initialize GLFW!");
		if (window != null) window.init();
		if (input != null) input.init();
		if (module != null) module.init();
	}

	private void loop() {
		long lastTime = System.nanoTime();
		long updateTime = 0L;
		long nextFrame = lastTime + NANOS_PER_SECOND / targetFPS;
		long counter = 0L;
		int countFPS = 0;
		int countUPS = 0;

		while (window != null && !window.shouldClose()) {
			long deltaFPS = NANOS_PER_SECOND / targetFPS;
			long deltaUPS = NANOS_PER_SECOND / targetUPS;
			long currentTime = System.nanoTime();
			long elapsedTime = currentTime - lastTime;
			lastTime = currentTime;
			updateTime += elapsedTime;
			nextFrame += deltaFPS;

			while (updateTime >= deltaUPS) {
				if (module != null) module.update();
				if (input != null) input.clear();
				updateTime -= deltaUPS;
				countUPS++;
			}

			if (module != null) module.render();
			window.refresh();
			countFPS++;

			counter += elapsedTime;
			if (counter >= NANOS_PER_SECOND) {
				currentFPS = countFPS;
				currentUPS = countUPS;
				countFPS = 0;
				countUPS = 0;
				counter -= NANOS_PER_SECOND;
			}

			if (!window.isVSync()) {
				while (System.nanoTime() < nextFrame) {
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {

					}
				}
			}
		}
	}

	private void exit() {
		if (module != null) module.exit();
		if (input != null) input.exit();
		if (window != null) window.exit();
		glfwTerminate();
		glfwSetErrorCallback(null).free();
	}

	public void setWindow(Window window) {
		this.window = window;
	}

	public Window getWindow() {
		return window;
	}

	public void setInput(Input input) {
		this.input = input;
	}

	public Input getInput() {
		return input;
	}

	public void setModule(Module module) {
		this.module = module;
	}

	public Module getModule() {
		return module;
	}

	public void setTargetFPS(int targetFPS) {
		this.targetFPS = targetFPS;
	}

	public int getTargetFPS() {
		return targetFPS;
	}

	public void setTargetUPS(int targetUPS) {
		this.targetUPS = targetUPS;
	}

	public int getTargetUPS() {
		return targetUPS;
	}

	public int getCurrentFPS() {
		return currentFPS;
	}

	public int getCurrentUPS() {
		return currentUPS;
	}

}
