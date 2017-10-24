import java.awt.Dimension;

import javax.swing.JFrame;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.gl2.GLUT;

public class ShadowCup implements GLEventListener {

	/**
	 * ShadowCup - this is a simple example of using a transformation to create
	 * shadows Eric McCreath 2009, 2011, 2015
	 * 
	 * You need to include the jogl jar files (gluegen-rt.jar and jogl.jar). In
	 * eclipse use "add external jars" in Project->Properties->Libaries
	 * otherwise make certain they are in the class path. In the current linux
	 * computers there files are in the /usr/share/java directory.
	 * 
	 * If you are executing from the command line then something like: javac -cp
	 * .:/usr/share/java/jogl2.jar:/usr/share/java/gluegen2-2.2.4-rt.jar
	 * ScreenSaverOGL.java java -cp
	 * .:/usr/share/java/jogl2.jar:/usr/share/java/gluegen2-2.2.4-rt.jar
	 * ScreenSaverOGL should work.
	 * 
	 * On our lab machine you may also need to check you are using Java 7. You
	 * can run it directly using: /usr/lib/jvm/java-7-openjdk-amd64/bin/javac
	 * and /usr/lib/jvm/java-7-openjdk-amd64/bin/java
	 * 
	 */

	JFrame jf;
	GLJPanel gljpanel;

	Dimension dim = new Dimension(800, 600);
	FPSAnimator animator;
	float lightpos[] = { 10.9f, 19.9f, 0.0f, 1.0f };
	float angle;

	public ShadowCup() {
		jf = new JFrame();
		gljpanel = new GLJPanel();
		gljpanel.addGLEventListener(this);
        gljpanel.requestFocusInWindow();
		jf.getContentPane().add(gljpanel);


		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.setVisible(true);
		jf.setPreferredSize(dim);
		jf.pack();
		animator = new FPSAnimator(gljpanel, 20);
		angle = 0.0f;
		animator.start();
	}

	public static void main(String[] args) {
		new ShadowCup();
	}

	// draw a simple cup
	public void drawcup(GL2 gl2, GLU glu, GLUT glut, float angle) {
		gl2.glPushMatrix();
		gl2.glTranslated(0.0, 3.0, 0.0);
		gl2.glRotated(angle, 0.2, 1.0, 0.0);
		gl2.glScaled(2.0, 2.0, 2.0);

		gl2.glPushMatrix();
		gl2.glRotated(90, 1.0, 0.0, 0.0);
		glut.glutSolidCylinder(1.0, 2.0, 100, 50);
		gl2.glPopMatrix();

		gl2.glPushMatrix();
		gl2.glTranslated(1.3, -0.7, 0.0);
		glut.glutSolidCylinder(0.5, 0.1, 100, 50);
		gl2.glPopMatrix();

		gl2.glPopMatrix();
	}

	public void init(GLAutoDrawable dr) { // set up openGL for 2D drawing
		GL2 gl2 = dr.getGL().getGL2();
		GLU glu = new GLU();
		GLUT glut = new GLUT();
		gl2.glClearColor(1.0f, 1.0f, 1.0f, 0.0f);
		gl2.glEnable(GL2.GL_DEPTH_TEST);

		gl2.glMatrixMode(GL2.GL_PROJECTION);
		gl2.glLoadIdentity();

		glu.gluPerspective(60.0, 1.0, 1.0, 50.0);

		gl2.glMatrixMode(GL2.GL_MODELVIEW);
		gl2.glLoadIdentity();
		glu.gluLookAt(5.0, 12.0, 6.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0);

		gl2.glEnable(GL2.GL_LIGHTING);

		float ac[] = { 0.2f, 0.2f, 0.2f, 1.0f };
		float dc[] = { 1.0f, 1.0f, 1.0f, 1.0f };
		gl2.glLightfv(GL2.GL_LIGHT1, GL2.GL_AMBIENT, ac, 0);

		gl2.glLightfv(GL2.GL_LIGHT1, GL2.GL_DIFFUSE, dc, 0);

		gl2.glLightfv(GL2.GL_LIGHT1, GL2.GL_POSITION, lightpos, 0);
		gl2.glEnable(GL2.GL_LIGHT1);

	}

	// multiply the current matrix with a projection matrix that will do the
	// shadow
	static void projectShadow(GL2 gl, float s[], float n[], float l[]) {
		float w, m;
		float mat[] = new float[4 * 4];

		w = (s[0] - l[0]) * n[0] + (s[1] - l[1]) * n[1] + (s[2] - l[2]) * n[2];
		m = l[0] * n[0] + l[1] * n[1] + l[2] * n[2];

		mat[index(0, 0)] = w + n[0] * l[0];
		mat[index(0, 1)] = n[1] * l[0];
		mat[index(0, 2)] = n[2] * l[0];
		mat[index(0, 3)] = -(w + m) * l[0];

		mat[index(1, 0)] = n[0] * l[1];
		mat[index(1, 1)] = w + n[1] * l[1];
		mat[index(1, 2)] = n[2] * l[1];
		mat[index(1, 3)] = -(w + m) * l[1];

		mat[index(2, 0)] = n[0] * l[2];
		mat[index(2, 1)] = n[1] * l[2];
		mat[index(2, 2)] = w + n[2] * l[2];
		mat[index(2, 3)] = -(w + m) * l[2];

		mat[index(3, 0)] = n[0];
		mat[index(3, 1)] = n[1];
		mat[index(3, 2)] = n[2];
		mat[index(3, 3)] = -m;

		gl.glMultMatrixf(mat, 0);

	}

	private static int index(int j, int i) {
		return j + 4 * i;
	}

	public void display(GLAutoDrawable dr) { // clear the screen and draw
												// "Save the Screens"
		GL2 gl2 = dr.getGL().getGL2();
		GLU glu = new GLU();
		GLUT glut = new GLUT();

		gl2.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

		// draw the cup normally
		gl2.glEnable(GL2.GL_LIGHTING);
		float df[] = { 0.3f, 0.3f, 1.0f, 0.0f };
		gl2.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, df,
				0);
		drawcup(gl2, glu, glut, angle);

		// draw the shadow
		gl2.glDisable(GL2.GL_LIGHTING);
		gl2.glEnable(GL2.GL_POLYGON_OFFSET_FILL);
		gl2.glPolygonOffset(-1.0f, -1.0f);

		float ground[] = { 0.0f, -3.0f, 0.0f };
		float groundnormal[] = { 0.0f, -1.0f, 0.0f };

		gl2.glPushMatrix();
		gl2.glColor3d(0.0, 0.0, 0.0);
		projectShadow(gl2, ground, groundnormal, lightpos);
		drawcup(gl2, glu, glut, angle);

		gl2.glPopMatrix();
		gl2.glDisable(GL2.GL_POLYGON_OFFSET_FILL);
		gl2.glEnable(GL2.GL_LIGHTING);

		// draw the floor
		float dff[] = { 0.7f, 0.3f, 1.0f, 0.0f };
		gl2.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE,
				dff, 0);
		gl2.glPushMatrix();
		gl2.glTranslated(-1.0, 1.0, 0.0);
		gl2.glBegin(GL2.GL_POLYGON);
		gl2.glVertex3d(-8.0, -4.0, -8.0);
		gl2.glVertex3d(-8.0, -4.0, 8.0);
		gl2.glVertex3d(8.0, -4.0, 8.0);
		gl2.glVertex3d(8.0, -4.0, -8.0);
		gl2.glEnd();
		gl2.glPopMatrix();

		gl2.glFlush();

		// update the angle
		angle += 0.5f;
		if (angle > 360.0f)
			angle -= 360.0f;
	}

	public void dispose(GLAutoDrawable glautodrawable) {
	}

	public void reshape(GLAutoDrawable dr, int x, int y, int width, int height) {
	}
}
