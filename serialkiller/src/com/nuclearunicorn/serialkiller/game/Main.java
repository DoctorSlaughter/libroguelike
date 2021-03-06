package com.nuclearunicorn.serialkiller.game;

import com.nuclearunicorn.serialkiller.game.modes.in_game.InGameMode;
import com.nuclearunicorn.serialkiller.game.modes.main_menu.MainMenuMode;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 03.03.12
 * Time: 12:23
 * To change this template use File | Settings | File Templates.
 */
public class Main {


    public static SkillerGame game;

    public static InGameMode inGameMode;

    public static void main(String[] args) {
        inGameMode = new InGameMode();

        game = new SkillerGame();

        game.registerMode("mainMenu", new MainMenuMode());
        game.registerMode("inGame", inGameMode);

        game.set_state("inGame");
        game.run();

    }


}
