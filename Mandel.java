// Use the Complex.java class to draw the
// Mandelbrot set.

import java.awt.event.*;
import java.awt.image.*;
import java.awt.*;
import java.util.concurrent.*;
import java.util.*;

public class Mandel extends Frame 
    implements MouseListener, MouseMotionListener {
 
    public Color velocityToColor(int velocity) {
        Color c = null;
        
        if (velocity <= 0) {
            c = Color.white;
        } 
        else if (velocity >= iterationLimit) {
            c = Color.black;
        } 
        else {
            int red = (velocity * 2) % 255;
            int green = (velocity * 7) % 255;
            int blue = (velocity * 11) % 255;
            c = new Color(red, green, blue);
        }
        
        return c;
    }
    
    
    int escapeVelocity(Complex pt) {
        Complex v = new Complex(pt);
        int     orbits = 0;
        
        while (v.norm2() <= 4.0 && orbits < iterationLimit)
        {
            v = v.times(v).plus(pt);
            orbits += 1;
        }
        
        return orbits;
    }
    
    
    
    public class SubImage {
        Complex     upperLeft;
        Complex     lowerRight;
        Rectangle   rect;
        
        public SubImage(Complex ul, Rectangle r)
        {
            upperLeft = ul;
            lowerRight = ul.plus(hDelta.times(Math.round(r.getWidth() - 1))).plus(vDelta.times(Math.round(r.getHeight() - 1)));
            rect = new Rectangle(r);
        }
        
        
        public SubImage(Rectangle r) {
            
            upperLeft = fullPicture.upperLeft.plus(hDelta.times(Math.round(r.getX()))).plus(vDelta.times(r.getY()));
            lowerRight = fullPicture.upperLeft.plus(hDelta.times(Math.round(r.getX() + r.getWidth() - 1))).plus(vDelta.times(Math.round(r.getY() + r.getHeight() - 1)));
            rect = new Rectangle(r);
        }
        
        public int getX() { return (int) Math.round(rect.getX()); }
        public int getY() { return (int) Math.round(rect.getY()); }
        public int getWidth() { return (int) Math.round(rect.getWidth()); }
        public int getHeight() { return (int) Math.round(rect.getHeight()); }
        
        public void draw(Graphics g, Color velocity) {
            int     offset = (Mandel.inset ? 1 : 0);
            
            g.setColor(velocity);
            
            if (getWidth() > 0 && getHeight() > 0)
            {
                g.fillRect(getX() + offset, getY() + offset, getWidth() - offset, getHeight() - offset);
            }
            else
            {
                g.drawRect(getX(), getY(), 1, 1);
            }
        }
        
        
        public void drawAllPoints()
        {
            Graphics    g = getGraphics();
            
            for(int i = 0; i < getWidth(); i++) {
                for (int j = 0; j < getHeight(); j++) {
                    g.setColor(imageBuffer[i][j]);
                    g.drawRect(i, j, 1, 1);
                }
            }       
        }
        
        
        public boolean subdivide() {
            boolean mustSubdivide = false;
            
            if (getHeight() > 1 && getWidth() > 1)
            {
                int     rightX = getX() + getWidth() - 1;
                int     lowerY = getY() + getHeight() - 1;
            
                Color   base = imageBuffer[getX()][getY()];
                
                mustSubdivide =  !base.equals(imageBuffer[getX()][lowerY])
                                 ||
                                 !base.equals(imageBuffer[rightX][getY()])
                                 ||
                                 !base.equals(imageBuffer[rightX][lowerY]);
                               
                if (!mustSubdivide)
                {
                    for (int i = 1; i < getWidth() - 1; i++) {
                        mustSubdivide = !base.equals(imageBuffer[getX() + i][getY()]);
                        if (mustSubdivide) break;
                    }
                }
                
                if (!mustSubdivide)
                {
                    for (int i = 0; i < getHeight() - 1; i++) {
                        mustSubdivide = !base.equals(imageBuffer[getX()][getY() + i]);
                        if (mustSubdivide) break;
                    }
                }
                
                if (!mustSubdivide)
                {
                    for (int i = 0; i < getHeight() - 1; i++) {
                        mustSubdivide = !base.equals(imageBuffer[rightX][getY() + i]);
                        if (mustSubdivide) break;
                    }
                }

                if (!mustSubdivide)
                {
                    for (int i = 0; i < getWidth() - 1; i++) {
                        mustSubdivide = !base.equals(imageBuffer[getX() + i][lowerY]);
                        if (mustSubdivide) break;
                    }
                }
            }
            
            return mustSubdivide;  
        }
        
        
        public void complete() {
            // Assumption: the boundary of the rect has been evaluated,
            // and all points on the boundary have the same potential.
            // This function will fill in all of the interior cells.
            for (int i = 1; i + 1 < getWidth(); i++) {
                for (int j = 1; j + 1 < getHeight(); j++) {
                    imageBuffer[getX() + i][getY() + j] = imageBuffer[getX()][getY()];
                }
            }
        }
        
        
        public SubImage[] subRects() {
            int halfWidthRight = getWidth() >> 1;
            int halfHeightDown = getHeight() >> 1;
            int halfWidthLeft = getWidth() - halfWidthRight;
            int halfHeightUp = getHeight() - halfHeightDown;
            
            SubImage    r1 = null, r2 = null, r3 = null, r4 = null;
            
            // If the horizontal width is even, then halfWidthLeft == halfWidthRight
            // and similarly if vertical height is even, halfHeightUp == halfHeightDown.
            // Otherwise halfWidthLeft >= halfWidthRight and halfHeightUp >= halfHeightDown.
            // If halfWidthRight == 0 or halfHeightDown == 0, then ONLY the upper left 
            // quadrant will have a non-degenerate area.
            
            if (halfWidthLeft > 0 && halfHeightUp > 0)
            {
                Rectangle   s1 = new Rectangle(getX(), getY(), halfWidthLeft, halfHeightUp);
                r1 = new SubImage(s1);
            }

            if (halfWidthRight > 0 && halfHeightDown > 0)
            {
                // All 4 subquadrants are non-degenerate
                Rectangle   s2 = new Rectangle(getX() + halfWidthLeft, getY(), halfWidthRight, halfHeightUp);
                r2 = new SubImage(s2);

                Rectangle   s3 = new Rectangle(getX(), getY() + halfHeightUp, halfWidthLeft, halfHeightDown);
                r3 = new SubImage(s3);

                Rectangle   s4 = new Rectangle(getX() + halfWidthLeft, getY() + halfHeightUp, halfWidthRight, halfHeightDown);
                r4 = new SubImage(s4);
            }

            SubImage returnVal[] = new SubImage [ 4 ];
            
            returnVal[0] = r1;
            returnVal[1] = r2;
            returnVal[2] = r3;
            returnVal[3] = r4;
            
            return returnVal;
        }
        
        public Complex lowerLeft() {
            return fullPicture.upperLeft.plus(hDelta.times(Math.round(getX()))).plus(vDelta.times(Math.round(getY() + getHeight() - 1)));
        }
        
        public Complex upperRight() {
            return fullPicture.upperLeft.plus(hDelta.times(Math.round(getX() + getWidth() - 1))).plus(vDelta.times(Math.round(getY())));
        }    
    }
    
    
    int                 mandelWidth;
    int                 mandelHeight;
    int                 iterationLimit;
    Complex             vDelta;
    Complex             hDelta;
    Color               imageBuffer[][];
    SubImage            fullPicture;
    Semaphore           sem;
    Queue<SubImage>     drawList;
    Queue<SubImage>     holdList;
    String              msg;
    int                 numDrawRects;
    int                 numHeldRects;
    static boolean      inset;
    static boolean      inDraw;
    boolean             mouseIsDown;
    Point               subRectStart;
    Point               subRectEnd;
    
    public void setLimit(int limit)
    {
        iterationLimit = limit;
    }
    
    
    class mandelWindowAdapter extends WindowAdapter {
        public void windowClosing(WindowEvent we){
            System.exit(0);
        }
    }
    
    
    public void mouseClicked(MouseEvent me) {
        if (me.getClickCount() > 1)
        {
            // Get rid of the old sub images
            try
            {
                sem.acquire();
                SubImage    s;
                while ((s = drawList.poll()) != null);
                while ((s = holdList.poll()) != null);
                sem.release();
            }
            
            catch(InterruptedException excpt)
            {
            }
            
            // Zoom in to the clicked point
            Complex c = point(fullPicture, Math.round(me.getX()), Math.round(me.getY()));
            Complex d = c.minus(hDelta.times(fullPicture.getWidth()/8.0)).minus(vDelta.times(fullPicture.getHeight()/8.0));
            Complex e = c.plus(hDelta.times(fullPicture.getWidth()/8.0)).plus(vDelta.times(fullPicture.getHeight()/8.0));
            
            initialize(d, e);
            
            Graphics g = getGraphics();
            g.clearRect(fullPicture.getX(), fullPicture.getY(), fullPicture.getWidth(), fullPicture.getHeight());
            imageBuffer = new Color [mandelWidth][mandelHeight];
            g.fillRect(fullPicture.getX(), fullPicture.getY(), fullPicture.getWidth(), fullPicture.getHeight());
            inDraw = true;
            drawPicture();
            inDraw = false;
        
            fullPicture.drawAllPoints();
        }
    }
    
    public void mouseEntered(MouseEvent me) {
    }
    
    public void mouseExited(MouseEvent me) {
    }
    
    public void mousePressed(MouseEvent me) {
        mouseIsDown  = true;
        subRectStart = me.getPoint();
        subRectEnd   = me.getPoint();
        repaint();
    }
    
    public void mouseReleased(MouseEvent me) {
        mouseIsDown = false;
        subRectEnd   = new Point(me.getX(), me.getY());
        repaint();
    }
    
    public void mouseMoved(MouseEvent me) {
    }
    
    public void mouseDragged(MouseEvent me) {
        mouseIsDown  = true;
        subRectEnd   = me.getPoint();
    }
    
    public Mandel(Dimension d) {
        setSize(d);
        mandelWidth = (int) d.getWidth();
        mandelHeight = (int) d.getHeight();
        iterationLimit = 100;
        imageBuffer = new Color [mandelWidth][mandelHeight];
        fullPicture = null;
        drawList = null;
        holdList = null;
        numDrawRects = 0;
        numHeldRects = 0;
        sem = new Semaphore(1);
        
        // Add a listeners to the frame
        addWindowListener(new mandelWindowAdapter());
        addMouseListener(this);
        addMouseMotionListener(this);
    }
    
    
    public void update(Graphics g) {
        Rectangle   clip = g.getClipBounds();
        
        if (inDraw)
        {
            if (sem != null) {
                try
                {
                    sem.acquire();
                    SubImage    s;
            
                    while ((s = drawList.poll()) != null) {
                        numDrawRects -= 1;
                        if (clip.intersects(s.rect))
                        {
                            s.draw(g, imageBuffer[s.getX()][s.getY()]);
                        }
                        holdList.add(s);
                        numHeldRects += 1;
                    }
            
                    Queue<SubImage> dummy = holdList;
                    int             d = numHeldRects;
                
                    numHeldRects = numDrawRects;
                    holdList = drawList;
                
                    numDrawRects = d;
                    drawList = dummy;
            
                    sem.release();
                }
            
                catch (InterruptedException e)
                {
                    if (fullPicture != null) fullPicture.drawAllPoints();
                }
            }
        }
        else
        {
            fullPicture.drawAllPoints();
        }
        
        /*
        g.setXORMode(Color.black);
        g.drawString("draw rects = " + numDrawRects + " hold rects = " + numHeldRects, 100, 100);
        g.setPaintMode();
        */
    }
    
    
    public Complex point(SubImage s, int i, int j)
    {
        Complex pt = null;
        
        if (0 <= i && 0 <= j && i < s.getWidth() && j < s.getHeight())
        {
            Complex origin = fullPicture.upperLeft;
            
            pt = origin.plus(hDelta.times(s.getX() + i));
            pt = pt.plus(vDelta.times(s.getY() + j));
        }
        
        return pt;
    }
    
    
    public void drawRight(SubImage s) {
        for (int i = 1; i + 1 < s.getHeight(); i++) {
            Complex pt = point(s, s.getWidth() - 1, i);
            
            if (pt != null) {
                imageBuffer[s.getX() + s.getWidth() - 1][s.getY() + i] = velocityToColor(escapeVelocity(pt));
            }
        }
    }

    
    public void drawLeft(SubImage s) {
        for (int i = 1; i + 1 < s.getHeight(); i++) {
            Complex pt = point(s, 0, i);
            
            if (pt != null) {
                imageBuffer[s.getX()][s.getY() + i] = velocityToColor(escapeVelocity(pt));
            }
        }
    }

    
    public void drawBottom(SubImage s) {
        for (int i = 1; i + 1 < s.getWidth(); i++) {
            Complex pt = point(s, i, s.getHeight() - 1);
            
            if (pt != null) {
                imageBuffer[s.getX() + i][s.getY() + s.getHeight() - 1] = velocityToColor(escapeVelocity(pt));
            }
        }
    }
    
    
    public void drawTop(SubImage s) {
        for (int i = 1; i + 1 < s.getWidth(); i++) {
            Complex pt = point(s, i, 0);
            
            if (pt != null) {
                imageBuffer[s.getX() + i][s.getY()] = velocityToColor(escapeVelocity(pt));
            }
        }
    }
    
    
    public void drawLowerRightSubImage(SubImage s) {
        // Assupmtion: the right and bottom sides of s
        // have already been drawn...
        
        // The upper left corner needs to be drawn
        imageBuffer[s.getX()][s.getY()] = velocityToColor(escapeVelocity(s.upperLeft));
        
        // Compute the points on the left and top sides
        drawTop(s);
        drawLeft(s);
    }
    
    
    public void drawLowerLeftSubImage(SubImage s) {
        // Assumption: the left and bottom sides of s
        // have already been drawn...
        
        int rightX = s.getX() + s.getWidth() - 1;
        
        // The upper right corner needs to be drawn
        imageBuffer[rightX][s.getY()] = velocityToColor(escapeVelocity(s.upperRight()));
        
        // Compute the points on the top and right sides
        drawTop(s);
        drawRight(s);
    }
    
    
    public void drawUpperRightSubImage(SubImage s) {
        // Assumption: the top and right sides of s
        // have already been drawn...
        
        int bottomY = s.getY() + s.getHeight() - 1;
        
        // The lower left corner needs to be drawn
        imageBuffer[s.getX()][bottomY] = velocityToColor(escapeVelocity(s.lowerLeft()));
        
        // Compute the points on the left and bottom sides
        drawLeft(s);
        drawBottom(s);
        
        
    }
    
    
    public void drawUpperLeftSubImage(SubImage s) {
        // Assumption:  The left and top sides of s
        // have already been drawn...

        int rightX = s.getX() + s.getWidth() - 1;
        int bottomY = s.getY() + s.getHeight() - 1;
        
        // The lower right corner hasn't been drawn.
        imageBuffer[rightX][bottomY] = velocityToColor(escapeVelocity(s.lowerRight));
        
        // Compute all of the points on the right and bottom sides
        drawRight(s);
        drawBottom(s);
    }
    
    
    public void drawSubImageBoundary(SubImage s) {
        // Compute the correct escape times for points
        // on the boundary of the sub-image.  If all of
        // these points have the same escape time, then
        // the sub-image interior must have the same escape 
        // time at each point.  This is because the level
        // curves for the potential function of a connected
        // compact set all bound connected regions of the 
        // complex plane.
        
        int rightX = s.getX() + s.getWidth() - 1;
        int bottomY = s.getY() + s.getHeight() - 1;
        
        // Get the escape times for the upper left corner
        imageBuffer[s.getX()][s.getY()] = velocityToColor(escapeVelocity(s.upperLeft));
        
        // Get escape times for the lower right corner
        imageBuffer[rightX][bottomY] = velocityToColor(escapeVelocity(s.lowerRight));
        
        // Get escape times for the upper right corner
        imageBuffer[rightX][s.getY()] = velocityToColor(escapeVelocity(s.upperRight()));
        
        // Get escape times for the lower left corner
        imageBuffer[s.getX()][bottomY] = velocityToColor(escapeVelocity(s.lowerLeft()));
        
        // Compute all of the points on the boundary
        drawTop(s);
        drawBottom(s);
        drawLeft(s);
        drawRight(s);
    }
    
    public void completeAndDraw(SubImage s) {
        if (!s.subdivide())
        {
            s.complete();
            try
            {
                sem.acquire();
                drawList.add(s);
                numDrawRects += 1;
                sem.release();
                repaint(s.getX(), s.getY(), s.getWidth(), s.getHeight());
            }
            
            catch(InterruptedException e)
            {
                drawList.add(s);
                numDrawRects += 1;
                repaint(s.getX(), s.getY(), s.getWidth(), s.getHeight());
            }
        }
        else
        {
            fillInSubImage(s);
        }
    }
    

    public void fillInSubImage(SubImage s) {
        SubImage        quadrants[] = s.subRects();
        
        // quadrant[0] = upper left
        // quadrant[1] = upper right
        // quadrant[2] = lower left
        // quadrant[3] = lower right
        
        if (quadrants[0] != null)
        {
            drawUpperLeftSubImage(quadrants[0]);
            completeAndDraw(quadrants[0]);
        }
        
        if (quadrants[1] != null)
        {
            drawUpperRightSubImage(quadrants[1]);
            completeAndDraw(quadrants[1]);
        }
        
        if (quadrants[2] != null)
        {
            drawLowerLeftSubImage(quadrants[2]);
            completeAndDraw(quadrants[2]);
        }
        
        if (quadrants[3] != null)
        {
            drawLowerRightSubImage(quadrants[3]);
            completeAndDraw(quadrants[3]);
        }
    }
    
    
    public void drawPicture() {
        drawSubImageBoundary(fullPicture);
        fillInSubImage(fullPicture);
    }
    
    
    
    public void initialize(Complex ul, Complex lr) {
        Rectangle   r = new Rectangle(0, 0, mandelWidth, mandelHeight);
        
        vDelta = new Complex(0.0, (lr.imag() - ul.imag())/(1.0 * mandelHeight));
        hDelta = new Complex((lr.real() - ul.real())/(1.0 * mandelWidth));
        drawList = new LinkedList<SubImage>();
        holdList = new LinkedList<SubImage>();
        
        fullPicture = new SubImage(ul, r);
    }
    
    
    
    public static void main(String args[]) {
        int         w = 768;
        int         h = 768;
        int         maxIter = 256;
        Complex     c1 = new Complex(-2.1, 2.1);
        Complex     c2 = new Complex(2.1, -2.1);    
        
        /*
        if (args[6] != null)
        {
            c2 = new Complex(Double.parseDouble(args[5]), Double.parseDouble(args[6]));
        }
        
        if (args[4] != null)
        {
            c1 = new Complex(Double.parseDouble(args[3]), Double.parseDouble(args[4]));
        }
        
        if (args[2] != null)
        {
            maxIter = Integer.parseInt(args[2]);
        }
        
        if (args[1] != null)
        {
            h = Integer.parseInt(args[1]);
            w = Integer.parseInt(args[0]);
        }
        */
        
        if (args != null && args.length > 0 &&  args[0] != null) {
            maxIter = Integer.parseInt(args[0]);
        }
        
        inset = true;
        
        Mandel  applicationWindow = new Mandel(new Dimension(w, h));
        applicationWindow.setTitle("Mandelbrot Set");
        applicationWindow.setVisible(true);
        
        applicationWindow.setLimit(maxIter);
        applicationWindow.initialize(c1, c2);
        
        inDraw = true;
        applicationWindow.drawPicture();
        inDraw = false;
        
        applicationWindow.fullPicture.drawAllPoints();
    }

}

