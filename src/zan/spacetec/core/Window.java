package zan.spacetec.core;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBImage.*;
import static org.lwjgl.system.MemoryUtil.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

public class Window {

	public static class Attributes {
		public String title = "";
		public String icon = "";

		public int x = GLFW_DONT_CARE;
		public int y = GLFW_DONT_CARE;

		public int width;
		public int height;

		public boolean fullscreen = false;
		public boolean vsync = true;
		public boolean resizable = true;
		public boolean decorated = true;
		public boolean focused = true;
		public boolean autoiconify = true;
		public boolean floating = false;
		public boolean maximized = false;
		public boolean minimized = false;
		public boolean visible = true;

		public int samples = 0;

		public Attributes(int width, int height) {
			this.width = width;
			this.height = height;
		}
	}

	private final Attributes attr;

	private int width;
	private int height;

	private long handle;

	public Window(Attributes attr) {
		this.attr = attr;
	}

	public void init() {
		initHints();
		initWindow();
		initCallbacks();
		initContext();
		initFinish();
	}

	private void initHints() {
		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_VISIBLE, GL_FALSE);
		glfwWindowHint(GLFW_RESIZABLE, attr.resizable ? GL_TRUE : GL_FALSE);
		glfwWindowHint(GLFW_DECORATED, attr.decorated ? GL_TRUE : GL_FALSE);
		glfwWindowHint(GLFW_FOCUSED, attr.focused ? GL_TRUE : GL_FALSE);
		glfwWindowHint(GLFW_AUTO_ICONIFY, attr.autoiconify ? GL_TRUE : GL_FALSE);
		glfwWindowHint(GLFW_FLOATING, attr.floating ? GL_TRUE : GL_FALSE);
		glfwWindowHint(GLFW_MAXIMIZED, attr.maximized ? GL_TRUE : GL_FALSE);
		glfwWindowHint(GLFW_SAMPLES, attr.samples);
	}

	private void initWindow() {
		long monitor = glfwGetPrimaryMonitor();
		GLFWVidMode vidmode = glfwGetVideoMode(monitor);

		if (attr.fullscreen) {
			width = vidmode.width();
			height = vidmode.height();
			handle = glfwCreateWindow(width, height, attr.title, monitor, NULL);
		} else {
			width = attr.width;
			height = attr.height;
			handle = glfwCreateWindow(width, height, attr.title, NULL, NULL);
		}
		if (handle == NULL) {
			throw new RuntimeException("Failed to create the GLFW window!");
		}

		if (attr.x == GLFW_DONT_CARE && attr.y == GLFW_DONT_CARE) {
			attr.x = (vidmode.width() - attr.width) / 2;
			attr.y = (vidmode.height() - attr.height) / 2;
		}
		glfwSetWindowPos(handle, attr.x, attr.y);

		setIcon(attr.icon);
	}

	private void initCallbacks() {
		glfwSetWindowPosCallback(handle, (window, x, y) -> {
			if (!attr.fullscreen) {
				attr.x = x;
				attr.y = y;
			}
		});
		glfwSetWindowSizeCallback(handle, (window, width, height) -> {
			if (!attr.fullscreen) {
				attr.width = width;
				attr.height = height;
			}
		});
		glfwSetFramebufferSizeCallback(handle, (window, width, height) -> {
			this.width = width;
			this.height = height;
		});
	}

	private void initContext() {
		glfwMakeContextCurrent(handle);
		glfwSwapInterval(attr.vsync ? 1 : 0);
		GL.createCapabilities();
	}

	private void initFinish() {
		if (attr.minimized) glfwIconifyWindow(handle);
		if (attr.visible) glfwShowWindow(handle);
	}

	public void refresh() {
		glfwSwapBuffers(handle);
		glfwPollEvents();
	}

	public void exit() {
		glfwFreeCallbacks(handle);
		glfwDestroyWindow(handle);
	}

	public void setTitle(String title) {
		attr.title = title;
		glfwSetWindowTitle(handle, title);
	}

	public String getTitle() {
		return attr.title;
	}

	public void setIcon(String icon) {
		attr.icon = icon;
		if (!icon.isEmpty()) {
			try (MemoryStack stack = MemoryStack.stackPush()) {
				IntBuffer w = stack.mallocInt(1);
				IntBuffer h = stack.mallocInt(1);
				IntBuffer c = stack.mallocInt(1);
				ByteBuffer ico = stbi_load(icon, w, h, c, 4);
				glfwSetWindowIcon(handle, GLFWImage.mallocStack(1, stack).width(w.get(0)).height(h.get(0)).pixels(ico));
				stbi_image_free(ico);
			}
		}
	}

	public String getIcon() {
		return attr.icon;
	}

	public void setPos(int x, int y) {
		glfwSetWindowPos(handle, x, y);
	}

	public int getX() {
		return attr.x;
	}

	public int getY() {
		return attr.y;
	}

	public void setSize(int width, int height) {
		glfwSetWindowSize(handle, width, height);
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public void setFullScreen(boolean fullscreen) {
		attr.fullscreen = fullscreen;
		long monitor = glfwGetPrimaryMonitor();
		GLFWVidMode vidmode = glfwGetVideoMode(monitor);
		if (fullscreen) {
			glfwSetWindowMonitor(handle, monitor, 0, 0, vidmode.width(), vidmode.height(), vidmode.refreshRate());
		} else {
			glfwSetWindowMonitor(handle, NULL, attr.x, attr.y, attr.width, attr.height, vidmode.refreshRate());
		}
		glfwSwapInterval(attr.vsync ? 1 : 0);
		attr.fullscreen = (glfwGetWindowMonitor(handle) == monitor);
	}

	public boolean isFullScreen() {
		return attr.fullscreen;
	}

	public void setVSync(boolean vsync) {
		attr.vsync = vsync;
		glfwSwapInterval(vsync ? 1 : 0);
	}

	public boolean isVSync() {
		return attr.vsync;
	}

	public void close() {
		glfwSetWindowShouldClose(handle, true);
	}

	public boolean shouldClose() {
		return glfwWindowShouldClose(handle);
	}

	public long getHandle() {
		return handle;
	}

}
