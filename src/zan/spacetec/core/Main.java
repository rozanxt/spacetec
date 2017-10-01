package zan.spacetec.core;

public class Main {

	public static void main(String[] args) {
		Engine engine = new Engine(60, 50);
		Window.Attributes attr = new Window.Attributes(800, 600);
		attr.resizable = false;
		Window window = new Window(attr);
		Input input = new Input(window);
		engine.setWindow(window);
		engine.setInput(input);
		engine.start();
	}

}
