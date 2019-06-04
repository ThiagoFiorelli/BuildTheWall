package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import static com.jme3.math.FastMath.nextRandomInt;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.CartoonEdgeFilter;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.texture.Texture;

/** Sample 8 - how to let the user pick (select) objects in the scene
 * using the mouse or key presses. Can be used for shooting, opening doors, etc. */
public class Main extends SimpleApplication {

  public static void main(String[] args) {
    Main app = new Main();
    app.start();
  }
  
  private Node shootables;
  private Node nodeWall;
  private Node nodeTransparentWall;
  private Geometry mark;

  @Override
  public void simpleInitApp() {
    initCrossHairs(); // a "+" in the middle of the screen to help aiming
    initKeys();       // load custom key mappings
    initMark();       // a red sphere to mark the hit
 
    FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
    CartoonEdgeFilter borda = new CartoonEdgeFilter();
    borda.setEdgeColor(ColorRGBA.Red);
    fpp.addFilter(borda);

    viewPort.addProcessor(fpp);

    /** create four colored boxes and a floor to shoot at: */
    shootables = new Node("Shootables");
    rootNode.attachChild(shootables);
    rootNode.attachChild(makeFloor(0,-3.5f,-10));
    rootNode.attachChild(makeFloor(0,-3.5f,10));
    makeWall();
    shootables.attachChild(makeTransparentWall());
  }
  
  @Override
    public void simpleUpdate(float tpf) {
      nodeWall.move(0,0,tpf*2);

    }


  /** Declaring the "Shoot" action and mapping to its triggers. */
  private void initKeys() {
    inputManager.addMapping("Shoot",
      new KeyTrigger(KeyInput.KEY_SPACE), // trigger 1: spacebar
      new MouseButtonTrigger(MouseInput.BUTTON_LEFT)); // trigger 2: left-button click
    inputManager.addListener(actionListener, "Shoot");
  }
  /** Defining the "Shoot" action: Determine what was hit and how to respond. */
  private ActionListener actionListener = new ActionListener() {

    public void onAction(String name, boolean keyPressed, float tpf) {
      if (name.equals("Shoot") && !keyPressed) {
        // 1. Reset results list.
        CollisionResults results = new CollisionResults();
        // 2. Aim the ray from cam loc to cam direction.
        Ray ray = new Ray(cam.getLocation(), cam.getDirection());
        // 3. Collect intersections between Ray and Shootables in results list.
        // DO NOT check collision with the root node, or else ALL collisions will hit the
        // skybox! Always make a separate node for objects you want to collide with.
        shootables.collideWith(ray, results);
        // 4. Print the results
        System.out.println("----- Collisions? " + results.size() + "-----");
        for (int i = 0; i < results.size(); i++) {
          // For each hit, we know distance, impact point, name of geometry.
          float dist = results.getCollision(i).getDistance();
          Vector3f pt = results.getCollision(i).getContactPoint();
          String hit = results.getCollision(i).getGeometry().getName();
          Spatial cubo = nodeTransparentWall.getChild(results.getCollision(i).getGeometry().getName());
          System.out.println("* Collision #" + i);
          System.out.println("  You shot " + hit + " at " + pt + ", " + dist + " wu away.");
        }
        // 5. Use the results (we mark the hit object)
        if (results.size() > 0) {
          // The closest collision point is what was truly hit:
          CollisionResult closest = results.getClosestCollision();
          // Let's interact - we mark the hit with a red dot.
          mark.setLocalTranslation(closest.getContactPoint());
          rootNode.attachChild(mark);
        } else {
          // No hits? Then remove the red mark.
          rootNode.detachChild(mark);
        }
      }
    }
  };

  /** A floor to show that the "shot" can go through several objects. */
  protected Geometry makeFloor(float x, float y, float z) {
    Box box = new Box(4, .2f, 10);
    Geometry floor = new Geometry("the Floor", box);
    floor.setLocalTranslation(x, y, z);
    Material mat1 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    Texture floorTexture = assetManager.loadTexture("Textures/floor1.jpg"); 
    mat1.setTexture("ColorMap", floorTexture); 
    floor.setMaterial(mat1);
    return floor;
  }

  /** A red ball that marks the last spot that was "hit" by the "shot". */
  protected void initMark() {
    Sphere sphere = new Sphere(30, 30, 0.2f);
    mark = new Geometry("BOOM!", sphere);
    Material mark_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    mark_mat.setColor("Color", ColorRGBA.Red);
    mark.setMaterial(mark_mat);
  }

  /** A centred plus sign to help the player aim. */
  protected void initCrossHairs() {
    setDisplayStatView(false);
    guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
    BitmapText ch = new BitmapText(guiFont, false);
    ch.setSize(guiFont.getCharSet().getRenderedSize() * 2);
    ch.setText("+"); // crosshairs
    ch.setLocalTranslation( // center
      settings.getWidth() / 2 - ch.getLineWidth()/2,
      settings.getHeight() / 2 + ch.getLineHeight()/2, 0);
    guiNode.attachChild(ch);
  }
  
   public Geometry makeCube(Node wall,float x, float y, float z){
     /** An unshaded textured cube. 
    *  Uses texture from jme3-test-data library! */ 
    Box boxMesh = new Box(0.48f,0.48f,0.48f); 
    Geometry boxGeo = new Geometry("A Textured Box", boxMesh); 
    Material boxMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md"); 
    boxGeo.setLocalTranslation(x, y, z);
    Texture monkeyTex = assetManager.loadTexture("Textures/wallTexture.jpg"); 
    boxMat.setTexture("ColorMap", monkeyTex); 
    boxGeo.setMaterial(boxMat); 
    wall.attachChild(boxGeo);
    rootNode.attachChild(wall);
    return boxGeo;
  }
  
  public Node makeWall(){
      nodeWall = new Node("nodeTransparentWall");
      int blockSkip = nextRandomInt(0,2);
      int i = 0 ,j = 0;
      for(i = 0; i < 4; i++){
          for(j = -2; j < 2; j++){
              if(blockSkip != 2){
                blockSkip = nextRandomInt(0,2);
              }
              else{
                makeCube(nodeWall,i-1.5f,j,-20);
                blockSkip = nextRandomInt(0,2);
              }

           }
        }
      return nodeWall;
  }
  
  
  
  int cont=0;
  public Geometry makeTransparentCube(Node wall,float x, float y, float z){
      /** A translucent/transparent texture, similar to a window frame. */
    Box cube2Mesh = new Box( 0.48f,0.48f,0.48f);
    Geometry cube2Geo = new Geometry("cube_"+cont++, cube2Mesh);
    Material cube2Mat = new Material(assetManager,
    "Common/MatDefs/Misc/Unshaded.j3md");
    cube2Geo.setLocalTranslation(x, y, z);
    cube2Mat.setTexture("ColorMap",
        assetManager.loadTexture("Textures/red.png"));
    cube2Mat.getAdditionalRenderState().setBlendMode(BlendMode.AlphaAdditive);  // !
    cube2Geo.setQueueBucket(Bucket.Transparent);   
    cube2Geo.setMaterial(cube2Mat);
    wall.attachChild(cube2Geo);
    rootNode.attachChild(wall);
    return cube2Geo;
  }
  
  public Node makeTransparentWall(){
      nodeTransparentWall = new Node("nodeWall");
      int i = 0 ,j = 0;
      for(i = 0; i < 4; i++){
          for(j = -2; j < 2; j++){
                makeTransparentCube(nodeTransparentWall,i-1.5f,j, 4f);
           }
        }
      return nodeTransparentWall;
  }
}

