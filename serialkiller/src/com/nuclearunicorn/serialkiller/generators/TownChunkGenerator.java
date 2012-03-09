package com.nuclearunicorn.serialkiller.generators;

import com.nuclearunicorn.libroguelike.game.combat.BasicCombat;
import com.nuclearunicorn.libroguelike.game.ent.Entity;
import com.nuclearunicorn.libroguelike.game.ent.EntityActor;
import com.nuclearunicorn.libroguelike.game.ent.controller.MobController;
import com.nuclearunicorn.libroguelike.game.world.WorldChunk;
import com.nuclearunicorn.libroguelike.game.world.WorldTile;
import com.nuclearunicorn.libroguelike.game.world.generators.ChunkGenerator;
import com.nuclearunicorn.serialkiller.game.ai.PedestrianAI;
import com.nuclearunicorn.serialkiller.game.world.RLTile;
import com.nuclearunicorn.serialkiller.game.world.RLWorldChunk;
import com.nuclearunicorn.serialkiller.game.world.RLWorldModel;
import com.nuclearunicorn.serialkiller.game.world.entities.EnityRLHuman;
import com.nuclearunicorn.serialkiller.game.world.entities.EntDoor;
import com.nuclearunicorn.serialkiller.game.world.entities.EntFurniture;
import com.nuclearunicorn.serialkiller.render.AsciiEntRenderer;
import org.lwjgl.util.Point;
import org.newdawn.slick.Color;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 03.03.12
 * Time: 19:10
 * To change this template use File | Settings | File Templates.
 */
public class TownChunkGenerator extends ChunkGenerator {

    int seed;
    Random chunk_random;

    List<Block> districts = null;
    List<Block> roads = new ArrayList<Block>();
    private static final int ROAD_SIZE = 3;
    
    //List<Block> apartments = new ArrayList<Block>();
    Map<Block,List<Block>> apartmentRooms = new HashMap<Block, List<Block>>();
    
    RLWorldChunk chunk;

    /*
        List of Path nodes in the crossroads or corner points of town. Used for generation of patroling routes
    */

    public void generate(WorldChunk chunk){

        if (chunk instanceof RLWorldChunk){
            this.chunk = (RLWorldChunk)chunk;
        }else{
            throw new RuntimeException("trying to generate non-RLWorldChunk element");
        }

        seed = chunk.origin.getX()*10000 + chunk.origin.getY();
        chunk_random = new Random(seed);


        int x = chunk.origin.getX()* WorldChunk.CHUNK_SIZE;
        int y = chunk.origin.getY()*WorldChunk.CHUNK_SIZE;
        int size = WorldChunk.CHUNK_SIZE;

        final int OFFSET = WorldChunk.CHUNK_SIZE;

        for (int i = x - OFFSET; i<x+size+OFFSET; i++ ){
            for (int j = y - OFFSET; j<y+size+OFFSET; j++){
                if ( i>= x && i<x+size && j >=y && j < y+size){
                    addTile(i,j, chunk_random);
                }
            }
        }

        //----------debug---------
        /*Block __block = new Block( 10, 10, 50, 50);
        //traceBlock(__block);

        MapGenerator gen = new MapGenerator(__block);
        gen.setSeed(123456);
        List<Block> __blocks = new ArrayList<Block>();
        __blocks.add(__block);

        List<Block> resultBlocks = gen.process(__blocks);
        for(Block result: resultBlocks){
            traceBlock(result);
        } */

        //---------debug end-------


        //Now, time to generate sum town

        Block gameBlock = new Block(
                x + 5,
                y + 5,
                WorldChunk.CHUNK_SIZE - 10 ,
                WorldChunk.CHUNK_SIZE - 10
        );

        MapGenerator mapgen = new MapGenerator(gameBlock);
        mapgen.setSeed(seed);

        List<Block> blocks = new ArrayList<Block>();
        blocks.add(gameBlock);

        districts = mapgen.process(blocks);

        for(Block district: districts){

            //register corner nodes before scaling and tracing roads

            Point[] ms = new Point[]{
                new Point(district.getX(), district.getY()),
                new Point(district.getX(), district.getY()+district.getH()),
                new Point(district.getX()+district.getW(), district.getY()),
                new Point(district.getX()+district.getW(), district.getY()+district.getH()),
            };
            for (Point milestone: ms){
                if (!this.chunk.hasMilestone(milestone)){
                    this.chunk.addMilestone(milestone);
                }
            }

            generateRoads(district);
            district.scale(-ROAD_SIZE,-ROAD_SIZE);
        }

        //-----------------------------------------------------------
        //		randomly place safehouse
        //-----------------------------------------------------------
        Block safehouseBlock = districts.get(chunk_random.nextInt(districts.size()));
        generateSafehouse(safehouseBlock);
        districts.remove(safehouseBlock);
            /*safehouse_block = districts[libtcod.random_get_int(self.generator, 0, len(districts)-1)]
            self.GenerateSafehosue(safehouse_block)
    
            districts.remove(safehouse_block) */

        //-----------------------------------------------------------
        //		create other housing areas
        //-----------------------------------------------------------
        
        for(Block district: districts){
            fillBlock(district);
        }

        //todo: milestones

        populateMap();

    }

    /*
        Safehouse it the apartment block owned by a player.
     */
    private void generateSafehouse(Block safehouseBlock) {
        generateHousing(safehouseBlock);
        apartmentRooms.remove(safehouseBlock);  //no one dares to live in my house!

        for (int i = 0; i <= safehouseBlock.getW(); i++)
            for (int j = 0; j <= safehouseBlock.getH(); j++){
                RLTile tile = (RLTile)(getLayer().get_tile(safehouseBlock.getX()+i,safehouseBlock.getY()+j));
                tile.setExplored(true);
            }
        //TODO: save safehouse

        fillApartmentRooms(safehouseBlock);

        //store safehouse to place player there later
        Point playerPosition = this.blockGetFreeTile(safehouseBlock);
        RLWorldModel.playerSafeHouseLocation = playerPosition;
        
        /*Point playerPosition = this.blockGetFreeTile(safehouseBlock);
        Player.get_ent().move_to(playerPosition);*/
    }

    private void fillApartmentRooms(Block safehouseBlock) {
        //To change body of created methods use File | Settings | File Templates.
    }

    private void generateRoads(Block block) {
        for ( List<Point> outerWall : block.getOuterWall(block) ){
            if( outerWall != null ){
                int wlen = outerWall.size()-1;
                int x = outerWall.get(0).getX();
                int y = outerWall.get(0).getY();
                int w = outerWall.get(wlen).getX()-x;
                int h = outerWall.get(wlen).getY()-y;
                
                Block road = new Block(x,y,w,h);
                road.scale(ROAD_SIZE-2,ROAD_SIZE-2);
                roads.add(road);

                for (int i = 0; i<=road.getW(); i++)
                    for (int j = 0; j<=road.getH(); j++){
                        RLTile tile = (RLTile)(getLayer().get_tile(road.getX()+i,road.getY()+j));

                        //TODO: use textures instead of broken pseudographics
                        //String model = new String(Character.toChars(178));
                        //tile.setModel(".");

                        tile.setModelColor(new Color(127,127,0));

                    }

                //TODO : place road on a map
                /*
                    self.tiles[(road.x+i,road.y+j)].model = libtcod.CHAR_BLOCK3
                    self.tiles[(road.x+i,road.y+j)].color = libtcod.darker_yellow
                    self.tiles[(road.x+i,road.y+j)].road = True
                */

            }
        }
    }

    /**
        As legacy code says:
        #FILL MAP WITH NPC
	    #this method should be called BEFORE room structure generation!
     */
    private void populateMap() {
        for (Block road: roads){

            //add pedestrians to the road

            int npcCount = 0;

            if (chunk_random.nextInt(100) < 50){
                npcCount = 1;
            }
            for (int i = 0; i< npcCount; i++){
                Point coord = blockGetFreeTile(road);

                EnityRLHuman npc = (EnityRLHuman)placeNPC(coord.getX(), coord.getY());
                npc.set_ai(new PedestrianAI());
                npc.set_controller(new MobController());
                npc.set_combat(new BasicCombat());

                int randomApt = chunk_random.nextInt(apartmentRooms.size());
                Block apt = new ArrayList<Block>(apartmentRooms.keySet()).get(randomApt);

                for (int n = apt.getX(); n < apt.getX()+apt.getW(); n++)
                    for (int m = apt.getY(); m < apt.getY()+apt.getH(); m++){
                        RLTile tile = (RLTile)(getLayer().get_tile(n,m));
                        tile.addOwner(npc);
                    }

            }
        }
    }

    private void fillBlock(Block district){
        int chance = chunk_random.nextInt(100);
        if (chance > 20){
            generateHousing(district);
        }else{
            generatePark(district);
        }
    }

    private void generatePark(Block block) {
        //RLTile tile = (RLTile)(getLayer().get_tile(i,j));

        for(int i = 0; i<=block.getW(); i++ )
            for(int j = 0; j<=block.getH(); j++ ){
                if (chunk_random.nextInt(200) < 1){
                    EntityActor npc = placeNPC(block.getX()+i, block.getY()+j);
                    npc.set_ai(new PedestrianAI());
                    npc.set_controller(new MobController());
                    npc.set_combat(new BasicCombat());
                }

                if (chunk_random.nextInt(100) < 2){
                    Entity tree = new Entity();
                    placeEntity(block.getX() + i, block.getY() + j, tree, "tree", "T");
                }

                if (chunk_random.nextInt(100) < 15){
                    Entity grass = new Entity();
                    grass.set_blocking(false);
                    placeEntity(block.getX() + i, block.getY() + j, grass, "grass", "\"");
                }

            }
    }


    private EntityActor placeNPC(int x, int y  ) {
        EntityActor playerEnt = new EnityRLHuman();
        placeEntity(x, y, playerEnt, "NPC", "@");

        return playerEnt;
    }

    /*
        Helper function. Place given entity at given point as ascii-art RL entity
     */
    private void placeEntity(int x, int y, Entity ent, String name, String symbol){
        ent.setName(name);
        ent.setEnvironment(environment);
        ent.setRenderer(new AsciiEntRenderer(symbol));

        ent.setLayerId(z_index);
        ent.spawn(12345, new Point(x,y));
    }

    private void generateHousing(Block block) {
        traceBlock(block);

        int ROOM_COUNT = 4;

        MapGenerator gen = new MapGenerator(block);
        gen.setMinBlockSize( block.getArea() / ROOM_COUNT );

        List<Block> housePrefab = new ArrayList<Block>();
        housePrefab.add(block);

        List<Block> rooms = gen.roomProcess(housePrefab);

        //TODO: extract method traceBlock
        for(Block room: rooms){
            traceBlock(room);
            room.clearNeighbours(); //so we could correctly generate rooms
        }

        /*
            This algorythm doesn't work with buggy corner rooms like

            -----------|
            |       ___|
            |      |   |
            |      |   |
            -------|---|

            Smaller room will result in pfanthom intersection wall.
            Altho this bug is invisible, we should probably assimilate smaller room by larger one
         */

        for (Block room : rooms){
            for(Block room2 : rooms){
                if(room != room2 && room.intersect(room2)){
                    if( !room.isConnected(room2) && !room2.isConnected(room)){ //transitive FTW?
                        Block intrs = room.getIntersection(room2);
                        if (intrs != null){


                            //------------room intersection debug start-------------
                            /*for(Point debug: intrs.getTiles()){
                                Entity playerEnt = new EnityRLHuman();

                                playerEnt.setName("NPC");
                                playerEnt.setEnvironment(environment);
                                playerEnt.setRenderer(new AsciiEntRenderer("X"));

                                playerEnt.setLayerId(z_index);
                                playerEnt.spawn(12345, new Point(debug.getX(),debug.getY()));
                            }*/

                            //------------debug end---------------
                            List<Point> wall = intrs.getTiles();
                            if (wall.size() > 0){
                                Point door_coord = wall.get(chunk_random.nextInt(wall.size()));
                                clearWall(door_coord.getX(), door_coord.getY());

                                room.addNeighbour(room2);
                            }
                        }
                    }
                }
            }


            for (List<Point> outerWall : room.getOuterWall(block)){
                //int rndChar =
                if (outerWall != null && outerWall.size()>0){
                    Point windowCoord = outerWall.get(outerWall.size()/2);
                    if (chunk_random.nextInt(100) > 20){

                        //Window

                        Entity window = new EntFurniture();
                        placeEntity(windowCoord.getX(), windowCoord.getY(), window, "window", "=");
                        window.get_combat().set_hp(1);
                        clearWall(windowCoord.getX(), windowCoord.getY());
                    } else {

                        //Door

                        EntDoor door = new EntDoor();
                        placeEntity(windowCoord.getX(), windowCoord.getY(), door, "door", "+");
                        door.get_combat().set_hp(5);
                        door.lock();
                        clearWall(windowCoord.getX(), windowCoord.getY());
                    }
                }
            }
        }

        apartmentRooms.put(block, rooms);   //save apartment and rooms in in for later handling
    }

    /*
        Get random unoccupied tile inside of the block
     */
    private Point blockGetFreeTile(Block block){
       while(true){
           int x = chunk_random.nextInt( block.getW()-1 ) + block.getX()+1;
           int y = chunk_random.nextInt( block.getY()-1 ) + block.getY()+1;

           if (!isBlocked(x,y)){
               return new Point(x,y);
           }
       }
    }

    private boolean isBlocked(int x, int y) {
        RLTile tile = (RLTile)(getLayer().get_tile(x,y));

        if ( tile!=null ){
            if (tile.isWall() || tile.isBlocked()){
                return true;
            }
            return false;
        }

        return true;
    }


    /**
      Trace outer conture of block and mark every outer block as wall
     */
    private void traceBlock(Block block){
        for (int i = 0; i< block.getH()+1; i++){
            placeWall(block.getX(), block.getY()+i);
            placeWall(block.getX()+block.getW(), block.getY()+i);
        }
        for (int j = 0; j< block.getW()+1; j++){
            placeWall(block.getX()+j, block.getY());
            placeWall(block.getX()+j, block.getY()+block.getH());
        }
    }

    private void placeWall(int i, int j){
        RLTile tile = (RLTile)(getLayer().get_tile(i,j));
        tile.setWall(true);
        //TODO: add isBlockSight to RLTile

        //self.tiles[(x,y)].blocked = True
        //self.tiles[(x,y)].block_sight = True
    }

    private void clearWall(int i, int j) {
        RLTile tile = (RLTile)(getLayer().get_tile(i,j));
        tile.setWall(false);
    }


    private WorldTile addTile(int i, int j, Random chunk_random) {
        WorldTile tile = new RLTile();
        Point origin = new Point(i,j);
        tile.origin = origin;

        getLayer().set_tile(origin, tile);

        return tile;
    }
}