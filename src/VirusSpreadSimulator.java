package virusSpreadSimulator3;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferStrategy;
import javax.swing.JFrame;


public class VirusSpreadSimulator extends Canvas implements Runnable{

	private static final long serialVersionUID = -4584388369897487885L;

	public static final int WIDTH = 1000, HEIGHT = WIDTH / 16 * 9;

	public boolean running = false; // true if the game is running
	private Thread gameThread; // thread where the game is updated AND drawn (single thread game)
	private Hospital hospital;

	Location city;
	private Menu menu;

	public enum STATE{
		Menu,
		Help,
		Run,
		Set,
		End
	}

	public static STATE runState = STATE.Menu;


	public VirusSpreadSimulator() {
		// hospital = new Hospital();

		
		canvasSetup();
		initialize();
		

		newWindow();

	}
	
	private void newWindow() {
		JFrame frame = new JFrame("Virus Spread Simulator");

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(true);
		frame.add(this);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

		start();
	}

	/**
	 * initialize all our game objects
	 */
	private void initialize() {
		// Initialize
		this.removeMouseListener(menu);
		city = new Location(getWidth(), getHeight(), new Hospital());
		menu = new Menu(this, city, new Hospital());
		this.addMouseListener(menu);
	}
	
	

	/**
	 * just to setup the canvas to our desired settings and sizes, setup events
	 */
	private void canvasSetup() {
		this.setSize(new Dimension(WIDTH, HEIGHT));
//		this.setMaximumSize(new Dimension(WIDTH, HEIGHT));
//		this.setMinimumSize(new Dimension(WIDTH, HEIGHT));

		this.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				int code = e.getKeyCode();

				if (code == KeyEvent.VK_R)
					initialize();
				
				if (code == KeyEvent.VK_M)
					runState = STATE.Set;

			}

		});

		// refresh size of city when this canvas changes size
		this.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent componentEvent) {
				city.setSize(getWidth(), getHeight());
			}
		});

		this.setFocusable(true);
	}
	

	public void run() {
		this.requestFocus();
		long lastTime = System.nanoTime();
		double amountOfTicks = 60.0;
		double ns = 1000000000 / amountOfTicks;
		double delta = 0;
		long timer = System.currentTimeMillis();
		int frames = 0;
		while(running) {
			long now = System.nanoTime();
			delta += (now - lastTime) / ns;
			lastTime = now;
			while(delta >= 1) {
				update();
				delta--;
			}
			if(running)
				render();
			frames++;

			if(System.currentTimeMillis() - timer > 1000) {
				timer += 1000;
				System.out.println("FPS: " + frames);
				frames = 0;
			}
		}
		stop();
	}
	
	

	private void update() {

		if(runState == STATE.Run) {
				city.update();
		}
		else if(runState == STATE.Menu || runState == STATE.End || runState == STATE.Help || runState == STATE.Set) {
			menu.tick();
		}

	}
	
	
	public synchronized void start() {
		gameThread = new Thread(this);
		/*
		 * since "this" is the "Game" Class you are in right now and it implements the
		 * Runnable Interface we can give it to a thread constructor. That thread will
		 * call the "run" method which this class inherited (it's directly above)
		 */
		gameThread.start(); // start thread
		running = true;
	}
	
	/**
	 * Stop the thread and the game
	 */
	public void stop() {
		try {
			gameThread.join();
			running = false;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void render() {
		BufferStrategy bs = this.getBufferStrategy();
		if(bs == null) {
			this.createBufferStrategy(3);
			return;
		}


		Graphics g = bs.getDrawGraphics();
		
		if(runState == STATE.Run) {
			drawBackground(g);
			city.setWalls(getWidth(), getHeight());
			city.draw(g);
		}
		else if(runState == STATE.Menu || runState == STATE.End || runState == STATE.Help || runState == STATE.Set) {
			drawBackground(g);
			menu.render(g);
		}


		g.dispose();
		bs.show();
	}
	
	private void drawBackground(Graphics g) {
		// black background
		g.setColor(Color.black);
		g.fillRect(0, 0, getWidth(), getHeight());
	}

	public static void main(String args[]) {
		new VirusSpreadSimulator();
	}


}
