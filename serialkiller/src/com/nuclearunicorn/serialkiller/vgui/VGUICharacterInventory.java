package com.nuclearunicorn.serialkiller.vgui;

import com.nuclearunicorn.libroguelike.core.Input;
import com.nuclearunicorn.libroguelike.events.EMouseClick;
import com.nuclearunicorn.libroguelike.game.actions.IAction;
import com.nuclearunicorn.libroguelike.game.items.BaseItem;
import com.nuclearunicorn.libroguelike.game.items.EquipContainer;
import com.nuclearunicorn.libroguelike.game.player.Player;
import com.nuclearunicorn.libroguelike.game.world.WorldTile;
import com.nuclearunicorn.libroguelike.game.world.WorldView;
import com.nuclearunicorn.libroguelike.vgui.NE_GUI_FrameModern;
import com.nuclearunicorn.libroguelike.vgui.NE_GUI_Popup;
import com.nuclearunicorn.libroguelike.vgui.NE_GUI_Text;
import com.nuclearunicorn.serialkiller.game.combat.NPCStats;
import com.nuclearunicorn.serialkiller.game.combat.RLCombat;
import com.nuclearunicorn.serialkiller.game.world.entities.EntityRLPlayer;
import com.nuclearunicorn.serialkiller.game.world.entities.EntityRLHuman;
import org.lwjgl.util.Point;
import org.newdawn.slick.Color;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 */
public class VGUICharacterInventory extends NE_GUI_FrameModern {

    NE_GUI_Text items;

    public VGUICharacterInventory(){

        super(false);    //no close button

        title = "Inventory";

        items = new NE_GUI_Text(){

            @Override
            protected void e_on_line_click(int lineId, EMouseClick clickEvent) {
                super.e_on_line_click(lineId, clickEvent);

                if (clickEvent.type.equals(Input.MouseInputType.LCLICK)){
                    onLeftClick(lineId, clickEvent);
                }else{
                    contextPopup(lineId, clickEvent);
                }
            }

            
        };
        items.max_lines = 10;
        items.set_size(20, 25, 200, 200);
        items.dragable = false;

        add(items);

    }

    protected void onLeftClick(int lineId, EMouseClick clickEvent) {
        List<BaseItem> items = Player.get_ent().getContainer().getItems();
        if (lineId < 0 || items.size() <= lineId) {
            return;
        }

        BaseItem item = items.get(lineId);
        System.out.println(item);


        EntityRLHuman ent = (EntityRLHuman) Player.get_ent();
        if (ent.equipment == null) {
            System.err.println("Player's entity equipment is null!");
            return;
        }

        if (ent.equipment.hasItem(item)) {
            ent.equipment.unequip(item);
        } else {
            ent.equipment.unequipSlot(item.get_slot());
            ent.equipment.equip_item(item);
        }
    }

    private void contextPopup(int lineId, EMouseClick clickEvent) {
        //fixme, IAM copypasta


        Point tileCoord = WorldView.getTileCoord(clickEvent.origin);
        WorldTile tile = Player.get_ent().getLayer().getTile(tileCoord);
        if (tile == null){
            System.out.println("no loaded tile at this position");
            return;
        }

        List<BaseItem> items = Player.get_ent().getContainer().getItems();
        if (lineId < 0 || items.size() <= lineId){
            return;
        }

        BaseItem item = items.get(lineId);


        NE_GUI_Popup __popup = new NE_GUI_Popup();

        this.getRootElement().add(__popup);  //kinda shitty, but ok

        __popup.x = clickEvent.origin.getX() ;
        __popup.y = clickEvent.get_window_y();

        //-------------------------------------------------
        ArrayList action_list = item.get_action_list();
        //IAction<Entity>[] actions = (IAction<Entity>[]) action_list.toArray();
        Iterator<IAction> itr = action_list.iterator();

        System.out.println("Fetched "+Integer.toString(action_list.size())+" actions");

        while (itr.hasNext()){
            IAction element = itr.next();
            __popup.add_item(element);
        }

    }

    protected List<BaseItem> getItems(){
        //override me!
        return (List<BaseItem>)Player.get_ent().getContainer().getItems();
    }


    public void updateInfo(){
        items.clearLines();
        RLCombat combat = (RLCombat) Player.get_ent().get_combat();
        NPCStats npcStats = combat.getStats();

        Color color;
        for(BaseItem item: getItems()){
            EquipContainer equipment = ((EntityRLPlayer) Player.get_ent()).equipment;

            if (equipment != null && equipment.hasItem(item)){
                color = Color.white;
            }else{
                color = Color.lightGray;
            }

            String entPrefix = "";
            String entPostfix = "";

            if (item.get_count() > 1){
                entPostfix = "("+item.get_count()+")";
            }
            if (item.get_slot() == null || item.get_slot().isEmpty() || item.get_slot().equals("undefined")){
                entPrefix = "*";
            }
            items.add_line(entPrefix + item.get_type() + entPostfix, color);
        }
    }

    @Override
    public void render() {
        updateInfo();
        super.render();
    }

}
