package com.mouldycheerio.bot;

import java.io.File;
import java.io.IOException;

import javax.security.auth.login.LoginException;

import org.json.JSONException;
import org.json.JSONObject;

import com.mouldycheerio.bot.resources.ResourceManager;
import com.mouldycheerio.dbot.CustomBot;
import com.mouldycheerio.dbot.coins.CoinController;
import com.mouldycheerio.dbot.util.PeelingUtils;

public class LemonPeel extends CustomBot {

    private CoinController coinController;
    private ResourceManager resourceManager;

    public static LemonPeel fromJSON(File file) throws LoginException, JSONException, IOException, InterruptedException {
        JSONObject loadJSON = PeelingUtils.loadJSONPretty(file);
        return new LemonPeel(loadJSON);
    }

    public LemonPeel(JSONObject config) throws IOException, LoginException, JSONException, InterruptedException {
        super(config);
        // coinController = CoinController.autoLoad(this);
        // getCommandController().removeCommand("generate");
        // TriggersManager triggersManager = coinController.getTriggersManager();
        // triggersManager.addGlobalTrigger(RobTrigger.create());

        resourceManager = new ResourceManager(this);

    }

    public static void main(String[] args) {

        try {
            init();
        } catch (LoginException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void init() throws LoginException, JSONException, IOException, InterruptedException {
        CustomBot customBot = LemonPeel.fromJSON(new File("config.json"));

    }

}
