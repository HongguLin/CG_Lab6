import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.gl2.GLUT;
import com.jogamp.opengl.util.texture.Texture;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.nio.IntBuffer;


public class ShaderLighting2 implements GLEventListener, MouseMotionListener {

	/**
	 * ShaderLighting - this is a simple example of drawing a teapot using a shader to
	 * do Phong shading.
	 * Eric McCreath 2009, 2011, 2015
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

	String homedir = "images/";

	Texture cgtexture[]  = new Texture[5];


	int shaderprogram, vertexshader, fragshader;

	//***
	float xc=0.0f, yc=0.0f, xe = 0.0f, ye=0.0f, xf=0.0f,yf=0.0f;

	int vertexbuffer[];
	int colorbuffer[];
	int texbuffer[];

	float xcamrot = 0.0f;
	float lightdis = 1.0f;
	float time; // in seconds
	float cycletime = 10.0f;
    static int framerate = 60;
    float lightpos[] = { 50.0f, 200.0f, 200.0f, 1.0f };


	public ShaderLighting2() {
		jf = new JFrame();
		gljpanel = new GLJPanel();
		gljpanel.addGLEventListener(this);
        gljpanel.requestFocusInWindow();
		jf.getContentPane().add(gljpanel);
		
		
		
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.setVisible(true);
		jf.setPreferredSize(dim);
		jf.pack();
		animator = new FPSAnimator(gljpanel, framerate);
		gljpanel.addMouseMotionListener(this);
		time = 0.0f;
		animator.start();
	}

	public static void main(String[] args) {
		new ShaderLighting2();
	}
// based on shaders from https://www.opengl.org/sdk/docs/tutorials/ClockworkCoders/lighting.php
	static final String vertstr[] = { 
    "varying vec3 N;\n" + 
	"varying vec3 v;\n" + 
	"void main(void)  \n" + 
	"	{     \n" + 
	"	   v = vec3(gl_ModelViewMatrix * gl_Vertex);    \n" +    
	"	   N = normalize(gl_NormalMatrix * gl_Normal);\n" + 
    "	   gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;  \n" + 
	"	}\n" };

	static int vlens[] = new int[1];
	static int flens[] = new int[1];

	static final String fragstr[] = { 
		"varying vec3 N;\n" + 
		"varying vec3 v; \n" +    
		"void main (void)  \n" + 
		"{  \n" + 
		"   vec3 L = normalize(gl_LightSource[0].position.xyz - v);   \n" + 
		"   vec3 E = normalize(-v); \n" +  // as we are in view co-ordinates the viewer is at (0,0,0) 
		"   vec3 R = normalize(-reflect(L,N));  \n" + 
		"   vec3 H = normalize(L+E);  \n" + 
		"   vec4 Iamb = gl_FrontLightProduct[0].ambient;    \n" + 
		"   vec4 Idiff = gl_FrontLightProduct[0].diffuse * max(dot(N,L), 0.0);\n" + 
		"   Idiff = clamp(Idiff, 0.0, 1.0);     \n" + 
		"   vec4 Ispec = gl_FrontLightProduct[0].specular \n" + 
		"                * pow(max(dot(R,E),0.0),gl_FrontMaterial.shininess);\n" + 
//		"                * pow(max(dot(H,N),0.0),gl_FrontMaterial.shininess);\n" + 
		"   Ispec = clamp(Ispec, 0.0, 1.0); \n" + 
		"   gl_FragColor = Iamb + Idiff + Ispec;    \n" +  
		"}\n" };
		          
	// gl_FrontLightModelProduct.sceneColor + 

	public void init(GLAutoDrawable dr) { // set up openGL for 2D drawing
		GL2 gl2 = dr.getGL().getGL2();
		GLU glu = new GLU();
		GLUT glut = new GLUT();

		// setup and load the vertex and fragment shader programs
		gl2.glEnable(GL2.GL_DEPTH_TEST);
		
		shaderprogram = gl2.glCreateProgram();

		vertexshader = gl2.glCreateShader(GL2.GL_VERTEX_SHADER);
		vlens[0] = vertstr[0].length();
		gl2.glShaderSource(vertexshader, 1, vertstr, vlens, 0);
		gl2.glCompileShader(vertexshader);
		checkok(gl2, vertexshader, GL2.GL_COMPILE_STATUS);
		gl2.glAttachShader(shaderprogram, vertexshader);

		fragshader = gl2.glCreateShader(GL2.GL_FRAGMENT_SHADER);
		flens[0] = fragstr[0].length();
		gl2.glShaderSource(fragshader, 1, fragstr, flens, 0);
		gl2.glCompileShader(fragshader);
		checkok(gl2, fragshader, GL2.GL_COMPILE_STATUS);
		gl2.glAttachShader(shaderprogram, fragshader);

		gl2.glLinkProgram(shaderprogram);

		checkok(gl2, shaderprogram, GL2.GL_LINK_STATUS);

		gl2.glValidateProgram(shaderprogram);
		checkok(gl2, shaderprogram, GL2.GL_VALIDATE_STATUS);

		gl2.glUseProgram(shaderprogram);

		gl2.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		
		gl2.glEnable(GL2.GL_LIGHTING);
		gl2.glShadeModel(GL2.GL_SMOOTH);
		
	    gl2.glEnable(GL2.GL_DEPTH_TEST);
		
		gl2.glMatrixMode(GL2.GL_PROJECTION);
		gl2.glLoadIdentity();
		glu.gluPerspective(80.0, 1.0, 50.0, 3000.0);
		gl2.glMatrixMode(GL2.GL_MODELVIEW);
		gl2.glLoadIdentity();
		glu.gluLookAt(0.0,200.0, 500.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0);
		

	}

	private void checkok(GL2 gl2, int program, int type) {
		IntBuffer intBuffer = IntBuffer.allocate(1);
		gl2.glGetProgramiv(program, type, intBuffer);
		if (intBuffer.get(0) != GL.GL_TRUE) {
			int[] len = new int[1];
			gl2.glGetProgramiv(program, GL2.GL_INFO_LOG_LENGTH, len, 0);
			if (len[0] != 0) {

				byte[] errormessage = new byte[len[0]];
				gl2.glGetProgramInfoLog(program, len[0], len, 0, errormessage,
						0);
				System.err.println("problem\n" + new String(errormessage));
				gljpanel.destroy();
				jf.dispose();
				System.exit(0);
			}
		}
	}

//	Vector3f startV = ne(6.0f,10.0f,40.0f);
//	Vector3f lookV = new Vector3f(0.0f,0.0f,2.0f);
//	Vector3f viewV = new Vector3f();

	public void drawCube(GLUT glut, GL2 gl2){
		float df[]={};
		for(int i=0;i<3;i++){
			for(int j=0;j<3;j++){
				for(int k=0;k<3;k++){
					if(j==0){
						switch (k){
							case 0:{
								float nf[]={1f,0f,0f,0f};
								df = nf;
								break;
							}
							case 1:{
								float nf[]={0f,1f,0f,0f};
								df = nf;
								break;
							}
							case 2:{
								float nf[]={0f,0f,1f,0f};
								df = nf;
								break;
							}
					}
					}
					else if(j==1){
						switch (k){
							case 0:{
								float nf[]={1f,1f,1f,0f};
								df = nf;
								break;
							}
							case 1:{
								float nf[]={1f,1f,0f,0f};
								df = nf;
								break;
							}
							case 2:{
								float nf[]={2f,1f,0f,0f};
								df = nf;
								break;
							}
						}
					}else {
						switch (k){
							case 0:{
								float nf[]={0f,0f,1f,0f};
								df = nf;
								break;
							}
							case 1:{
								float nf[]={1f,0f,0f,0f};
								df = nf;
								break;
							}
							case 2:{
								float nf[]={0f,1f,0f,0f};
								df = nf;
								break;
							}
						}
					}
					gl2.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, df, 0);


					gl2.glTranslated(i*31, j*31,k*31);
					glut.glutSolidCube(30);
					gl2.glTranslated(-i*31, -j*31,-k*31);
				}
			}
		}

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

		gl2.glClear(GL.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

		
		gl2.glEnable(GL2.GL_LIGHTING);
		gl2.glPushMatrix(); 
		//gl2.glRotated(xcamrot,1.0,0.0,0.0);
	

		// set up light 1
		
		//gl2.glLightf(GL2.GL_LIGHT0, GL2.GL_CONSTANT_ATTENUATION, 0.0f);
		//gl2.glLightf(GL2.GL_LIGHT0, GL2.GL_LINEAR_ATTENUATION, 0.0f);
	    //gl2.glLightf(GL2.GL_LIGHT0, GL2.GL_QUADRATIC_ATTENUATION, 0.001f);
		float ac[] = { 0.2f, 0.2f, 0.2f, 1.0f };
		gl2.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, ac, 0);
		gl2.glEnable(GL2.GL_LIGHT0);
		float dc[] = { 3.0f, 3.0f, 3.0f, 1.0f };
		float sc[] = { 3.0f, 3.0f, 3.0f, 1.0f };
		gl2.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, lightpos, 0);
		gl2.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, dc, 0);
		gl2.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, sc, 0);

		//***
		xf+=xe;
		yf+=ye;

		float sf[] = { 1.0f, 1.0f, 1.0f, 0.0f };
		gl2.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, sf, 0);
		gl2.glMaterialf(GL2.GL_FRONT_AND_BACK, GL2.GL_SHININESS, 120.0f);
		
		
		gl2.glPushMatrix();   // draw the spinning tea pot with a particular material
		//gl2.glRotated(time*20.0f, 0.1, 1.0, 0.0);
		//gl2.glColor3f(0.1f, 0.0f, 1.0f);

		//glut.glutSolidTeapot(100.0);
		//glut.glutSolidCube(100);


		gl2.glRotated(xc+xf, 0, 1, 0);
		gl2.glRotated(yc+yf, 1, 0, 0);
		drawCube(glut,gl2);
		gl2.glPopMatrix();
		
		
		gl2.glPushMatrix();  // draw the light source's position using a yellow sphere 
		float dfs[] = { 1.0f, 1.0f, 0.0f, 0.0f };
		gl2.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, dfs, 0);
		gl2.glTranslated(lightpos[0], lightpos[1], lightpos[2]);
		glut.glutSolidSphere(3.0, 100, 100);
		gl2.glPopMatrix();




		// draw the shadow
		gl2.glDisable(GL2.GL_LIGHTING);
		gl2.glEnable(GL2.GL_POLYGON_OFFSET_FILL);
		gl2.glPolygonOffset(-1.0f, -1.0f);

		float ground[] = { 0.0f, -200.0f, 0.0f };
		float groundnormal[] = { 0.0f, -1.0f, 0.0f };

		gl2.glPushMatrix();
		gl2.glColor3d(0.0, 0.0, 0.0);
		projectShadow(gl2, ground, groundnormal, lightpos);

		gl2.glRotated(xc+xf, 0, 1, 0);
		gl2.glRotated(yc+yf, 1, 0, 0);
		drawCube(glut,gl2);

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
		gl2.glVertex3d(-1000.0, -200.0, -1000.0);
		gl2.glVertex3d(-1000.0, -200.0, 1000.0);
		gl2.glVertex3d(1000.0, -200.0, 1000.0);
		gl2.glVertex3d(1000.0, -200.0, -1000.0);
		gl2.glEnd();
		gl2.glPopMatrix();


		
		gl2.glPopMatrix();
		
		gl2.glFlush();
		
		xe=0.9f*xe;
		ye=0.9f*ye;

		if(xe<1){xe=0;}
		if(ye<1){ye=0;}
		
	}

	public void dispose(GLAutoDrawable glautodrawable) {
	}

	public void reshape(GLAutoDrawable dr, int x, int y, int width, int height) {
	}
	
	//Float xcamrotLast, lightdisLast;
	Float xlast,ylast,xd=0.0f,yd=0.0f;
	
	@Override
	public void mouseDragged(MouseEvent me) {
		xe=0;
		ye=0;
		if(xlast!=null){
			xd=((float)(me.getX())/gljpanel.getWidth()-xlast)*360;
			yd=((float)(me.getY())/gljpanel.getHeight()-ylast)*360;

			xc+=xd;
			yc+=yd;
		}
		xlast=(float)(me.getX())/gljpanel.getWidth();
		ylast=(float)(me.getY())/gljpanel.getHeight();
	  	
		gljpanel.display();
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		if(xlast!=null){
			xe=xd;
			ye=yd;
			xd=0.0f;
			yd=0.0f;
		}
		xlast=null;
		ylast=null;
	}
	
}
