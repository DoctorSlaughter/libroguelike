package com.nuclearunicorn.negame.client.game.modes.in_game;

import com.nuclearunicorn.libroguelike.core.client.ClientEventManager;
import com.nuclearunicorn.libroguelike.core.client.ClientGameEnvironment;
import com.nuclearunicorn.libroguelike.events.*;
import com.nuclearunicorn.libroguelike.events.network.EEntityMove;
import com.nuclearunicorn.libroguelike.events.network.EPlayerSpawn;
import com.nuclearunicorn.libroguelike.events.network.ESelectCharacter;
import com.nuclearunicorn.libroguelike.game.GameEnvironment;
import com.nuclearunicorn.libroguelike.game.ent.EntityPlayer;
import com.nuclearunicorn.libroguelike.game.modes.AbstractGameMode;
import com.nuclearunicorn.libroguelike.game.player.CharacterInfo;
import com.nuclearunicorn.libroguelike.game.player.Player;
import com.nuclearunicorn.libroguelike.game.ui.IUserInterface;
import com.nuclearunicorn.libroguelike.game.world.WorldChunk;
import com.nuclearunicorn.libroguelike.game.world.WorldCluster;
import com.nuclearunicorn.libroguelike.game.world.WorldView;
import com.nuclearunicorn.libroguelike.game.world.WorldViewCamera;
import com.nuclearunicorn.libroguelike.game.world.layers.WorldLayer;
import com.nuclearunicorn.libroguelike.render.TilesetRenderer;
import com.nuclearunicorn.libroguelike.render.layers.AbstractLayerRenderer;
import com.nuclearunicorn.libroguelike.render.overlay.DebugOverlay;
import com.nuclearunicorn.libroguelike.render.overlay.OverlaySystem;
import com.nuclearunicorn.libroguelike.utils.NLTimer;
import com.nuclearunicorn.libroguelike.utils.Timer;
import com.nuclearunicorn.libroguelike.vgui.effects.EffectsSystem;
import com.nuclearunicorn.negame.client.NEGame;
import com.nuclearunicorn.negame.client.NEGameClient;
import com.nuclearunicorn.negame.client.clientIo.NettyClient;
import com.nuclearunicorn.negame.client.game.controllers.NetworkPlayerController;
import com.nuclearunicorn.negame.client.game.world.NEWorldModel;
import com.nuclearunicorn.negame.client.game.world.NEWorldView;
import com.nuclearunicorn.negame.client.generators.NEClientGroundChunkGenerator;
import com.nuclearunicorn.negame.client.render.ASCIISpriteEntityRenderer;
import com.nuclearunicorn.negame.client.render.TilesetVoxelRenderer;
import com.nuclearunicorn.negame.client.render.overlays.NEDebugOverlay;
import com.nuclearunicorn.serialkiller.game.social.SocialController;
import com.nuclearunicorn.serialkiller.game.world.RLWorldModel;
import com.nuclearunicorn.serialkiller.render.ConsoleRenderer;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.Point;
import org.newdawn.slick.Color;
import rlforj.los.IFovAlgorithm;
import rlforj.los.PrecisePermissive;

import java.util.ArrayList;

/**
 * Main game mode
 */
public class InGameMode extends AbstractGameMode implements IEventListener {

    private OverlaySystem overlay;
    IUserInterface wgt = null;

    private WorldView view;
    private NEWorldModel model;
    private EffectsSystem fx;

    //IFovAlgorithm fov = new ShadowCasting();
    IFovAlgorithm fov = new PrecisePermissive();

    private static GameEnvironment clientGameEnvironment;
    private int turnNumber = 0;

    /*
     * Is game in client-server mode or offline single player, etc
     */
    public static final boolean NETWORK_MODE = true;

    @Override
    public void run() {
        overlay = new OverlaySystem();
        fx = new EffectsSystem();

        clientGameEnvironment = new GameEnvironment("ne-client-game-environment"){
            @Override
            public EventManager getEventManager(){
                return ClientEventManager.getEventManager();
            }
        };
        ClientGameEnvironment.setEnvironment(clientGameEnvironment);
        //RLMessages.setEventManager(clientGameEnvironment.getEventManager());

        model = new NEWorldModel(5);
        clientGameEnvironment.setWorld(model);
        model.setName("client world");

        WorldCluster.CLUSTER_SIZE = 3;

        ArrayList<WorldLayer> layers = new ArrayList<WorldLayer>(model.getLayers());
        layers.get(0).registerGenerator(new NEClientGroundChunkGenerator());
        Timer.init();

        final AbstractLayerRenderer renderer = new TilesetVoxelRenderer();
        WorldView.ISOMETRY_MODE = false;
        WorldView.ISOMETRY_ANGLE = 0.0f;
        WorldView.ISOMETRY_Y_SCALE = 1.0f;

        view  = new NEWorldView(model){
            public AbstractLayerRenderer getLayerRenderer(){
                return renderer;
            }
        };

        //tileset render affects camera positioning. Todo:fix this shit
        TilesetRenderer.TILE_SIZE = ConsoleRenderer.TILE_SIZE;

        //WorldChunk.CHUNK_SIZE = 128;
        WorldChunk.CHUNK_SIZE = 32;
        
        //spawn_player(new Point(0,0));

        
        //hack begin

        /**
         * This ugly hack fixes player position.
         * First, we create player entity, as it requeres local environment
         * Then, we force game to make player turn in order to detect chunks and pre-build them
         * As an effect of chunk building, generator sets player location inside of generated safehouses
         * Then, we finally able to use pre-generated player and place it into the safehouse
         */
        model.update();

        if (RLWorldModel.playerSafeHouseLocation != null){
            Player.get_ent().move_to(RLWorldModel.playerSafeHouseLocation);
        }
        //hack end

        //loading misc services
        SocialController.init();

        //Init client layer
        if (NETWORK_MODE){
            NettyClient.connect();
        }
    }

    @Override
    public void update() {
        DebugOverlay.renderTime = 0;
        NLTimer timer = new NLTimer();
        timer.push();

        super.update();

        get_ui().update();
        model.update();
        fx.update();
        fovUpdate();

        DebugOverlay.updateTime = timer.popDiff();
        timer.push();

        view.render();
        fx.render();
        get_ui().render();


        DebugOverlay.debugPathfinding();    //heavy, but very useful
        overlay.render();

        NEDebugOverlay.render();
        DebugOverlay.frameTime = timer.popDiff();

        if (NETWORK_MODE){
            NEGame neGame = (NEGame)this.getGameManager();
            if (neGame != null){
                neGame.updateServerSession();
            }else{
                System.err.println("failed to update server session, Main.game is null");
            }
        }
    }

    private void fovUpdate() {
        model.resetFov();
        //int fovRadius = ((RLCombat)Player.get_ent().get_combat()).getFovRadius();
        //fov.visitFieldOfView(model, Player.get_ent().x(), Player.get_ent().y(), fovRadius);
    }

    @Override
    public IUserInterface get_ui(){
        if (wgt!=null){
            return wgt;
        }
        wgt = new InGameUI();

        return wgt;
    }

    @Override
    public void e_on_event(Event event) {
        boolean isNextTurn = false;

        //allow to press ESC even if player is dead
        if (event instanceof EKeyPress){
            if (((EKeyPress) event).key == Keyboard.KEY_ESCAPE){
                NEGame game = NEGameClient.getNEGame();

                game.set_state("mainMenu");
                game.initStateUI();
                return;
            }
        }

        if (event instanceof EKeyPress){
            switch(((EKeyPress) event).key){
                case Keyboard.KEY_UP: case Keyboard.KEY_W:
                    TilesetVoxelRenderer.camera.lift(1.0f);
                break;
                case Keyboard.KEY_DOWN: case Keyboard.KEY_S:
                    TilesetVoxelRenderer.camera.dive(1.0f);
                break;
                case Keyboard.KEY_LEFT: case Keyboard.KEY_A:
                    TilesetVoxelRenderer.camera.strafeLeft(1.0f);
                break;
                case Keyboard.KEY_RIGHT: case Keyboard.KEY_D:
                    TilesetVoxelRenderer.camera.strafeRight(1.0f);
                break;
                case Keyboard.KEY_SPACE:

                break;
            }
        }

        if (event instanceof EPlayerAuthorise){
            //logon
            CharacterInfo chrInfo = new CharacterInfo();
            chrInfo.name = "Red";

            ESelectCharacter selectChrEvent = new ESelectCharacter(chrInfo);
            selectChrEvent.post();
        }
        if (event instanceof EPlayerSpawn){
            EPlayerSpawn logonEvent = (EPlayerSpawn)event;
            spawnPlayer(logonEvent.origin, logonEvent.uid);
        }
        //we need to recalculate view lighting on entity move/spawn
        if (event instanceof EEntitySpawn){
            TilesetVoxelRenderer.invalidateGeometry();
        }
        if (event instanceof EEntityMove){
            TilesetVoxelRenderer.invalidateGeometry();
        }
    }

    public static void spawnPlayer(Point location, String uid){

        EntityPlayer playerEnt = new EntityPlayer();
        //playerEnt.set_combat(new RLCombat());

        playerEnt.setName("Player");
        playerEnt.setEnvironment(clientGameEnvironment);
        //playerEnt.setRenderer(new VoxelEntityRenderer());
        playerEnt.setRenderer(new ASCIISpriteEntityRenderer("@", Color.white));

        //TODO: extract player information from the event
        //clientGameEnvironment.getEntityManager().add(player_ent, Player.get_zindex());

        playerEnt.setLayerId(Player.get_zindex());
        playerEnt.spawn(uid, location);

        WorldViewCamera.target.setLocation(location);

        playerEnt.set_controller(new NetworkPlayerController());
        //playerEnt.set_ai(new PlayerAI());
        Player.set_ent(playerEnt);

        //NPCGenerator.generateNPCStats(new Random(), playerEnt);
        playerEnt.setName("Player");
    }
}
