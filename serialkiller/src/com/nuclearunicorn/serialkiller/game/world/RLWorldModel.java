package com.nuclearunicorn.serialkiller.game.world;

import com.nuclearunicorn.libroguelike.game.player.Player;
import com.nuclearunicorn.libroguelike.game.world.WorldModel;
import com.nuclearunicorn.libroguelike.game.world.WorldTile;
import com.nuclearunicorn.libroguelike.game.world.layers.WorldLayer;
import rlforj.los.ILosBoard;

import java.util.ArrayList;
import java.util.List;

public class RLWorldModel extends WorldModel implements ILosBoard {
    
    private List<RLTile> fovTiles = new ArrayList<RLTile>();

    public RLWorldModel(int layersCount) {
        super(layersCount);
    }

    /**
        Reset fov-checked tiles.
        Since iterating them all can be heavy operation,
        we will store fov-visited tiles and reset them directly from this list
     */
    public void resetFov(){
        for(RLTile tile: fovTiles){
            if (tile != null){  //FTW?
                tile.setVisible(false);
                tile.setExplored(true);
            }
        }
        fovTiles.clear();
    }


    /*
        Whether we can check FOV radius or not
        Usually we shuld allways return true, except
        corner cases when dynamic chunk loading is prohibited

        TODO: further investigation
     */
    @Override
    public boolean contains(int x, int y) {
        return true;
    }

    @Override
    public boolean isObstacle(int x, int y) {
        RLTile tile = getRLTile(x, y);

        /*if (tile.get_actor() != null && tile.get_actor() instanceof EntityPlayer){
            return false;
        }*/

        return tile.isWall() || tile.is_blocked();
        //return tile.get_height() > 0;
    }

    @Override
    public void visit(int x, int y) {
        RLTile visibleTile = getRLTile(x,y);
        if (visibleTile != null){
            fovTiles.add(visibleTile);
            visibleTile.setVisible(true);
        }
    }
    
    private RLTile getRLTile(int x, int y){
        
        WorldLayer layer = this.getLayer(Player.get_zindex());
        WorldTile tile = layer.get_tile(x,y);

        if(tile instanceof RLTile){
            return (RLTile)tile;
        }
        return null;
    }
}