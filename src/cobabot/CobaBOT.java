/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cobabot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import org.alicebot.ab.AB;
import org.alicebot.ab.AIMLProcessor;
import org.alicebot.ab.Bot;
import org.alicebot.ab.Category;
import org.alicebot.ab.Chat;
import org.alicebot.ab.ChatTest;
import org.alicebot.ab.Graphmaster;
import org.alicebot.ab.MagicBooleans;
import org.alicebot.ab.MagicNumbers;
import org.alicebot.ab.MagicStrings;
import org.alicebot.ab.Nodemapper;
import org.alicebot.ab.PCAIMLProcessorExtension;
import org.alicebot.ab.TestAB;
import org.alicebot.ab.Verbs;

import fi.iki.elonen.NanoHTTPD;

/**
 *
 * @author Delvin V
 */
public class CobaBOT {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        MagicStrings.setRootPath();

        AIMLProcessor.extension = new PCAIMLProcessorExtension();
        mainFunction(args);
    }

    public void runBot(String[] args){
        MagicStrings.setRootPath();

        AIMLProcessor.extension = new PCAIMLProcessorExtension();
        mainFunction(args);
    }
    
    public static void mainFunction(final String[] args) {
        //String botName = "alice2";
        String botName = "super";
        MagicBooleans.jp_tokenize = false;
        MagicBooleans.trace_mode = true;
        String action = "chat";
        //String action = "webservice";
        System.out.println(MagicStrings.program_name_version);
        for (final String s : args) {
            // System.out.println(s);
            final String[] splitArg = s.split("=");
            if (splitArg.length >= 2) {
                final String option = splitArg[0];
                final String value = splitArg[1];
                // if (MagicBooleans.trace_mode) System.out.println(option+"='"+value+"'");
                if (option.equals("bot")) {
                    botName = value;
                }
                if (option.equals("action")) {
                    action = value;
                }
                if (option.equals("trace")) {
                    if (value.equals("true")) {
                        MagicBooleans.trace_mode = true;
                    } else {
                        MagicBooleans.trace_mode = false;
                    }
                }
                if (option.equals("morph")) {
                    if (value.equals("true")) {
                        MagicBooleans.jp_tokenize = true;
                    } else {
                        MagicBooleans.jp_tokenize = false;
                    }
                }
            }
        }
        if (MagicBooleans.trace_mode) {
            System.out.println("Working Directory = " + MagicStrings.root_path);
        }
        Graphmaster.enableShortCuts = true;
        final Bot bot = new Bot(botName, MagicStrings.root_path, action); //
        if (MagicBooleans.make_verbs_sets_maps) {
            Verbs.makeVerbSetsMaps(bot);
        }
        if (bot.brain.getCategories().size() < MagicNumbers.brain_print_size) {
            bot.brain.printgraph();
        }
        if (MagicBooleans.trace_mode) {
            System.out.println("Action = '" + action + "'");
        }
        if (action.equals("chat") || action.equals("chat-app")) {
            final boolean doWrites = !action.equals("chat-app");
            TestAB.testChat(bot, doWrites, MagicBooleans.trace_mode);
        }
        else if (action.equals("webservice")) {
            new WebService();
        } else if (action.equals("ab")) {
            TestAB.testAB(bot, TestAB.sample_file);
        } else if (action.equals("aiml2csv") || action.equals("csv2aiml")) {
            convert(bot, action);
        } else if (action.equals("abwq")) {
            final AB ab = new AB(bot, TestAB.sample_file);
            ab.abwq();
        } else if (action.equals("test")) {
            TestAB.runTests(bot, MagicBooleans.trace_mode);
        } else if (action.equals("shadow")) {
            MagicBooleans.trace_mode = false;
            bot.shadowChecker();
        } else if (action.equals("iqtest")) {
            final ChatTest ct = new ChatTest(bot);
            try {
                ct.testMultisentenceRespond();
            } catch (final Exception ex) {
                ex.printStackTrace();
            }
        } else {
            System.out.println("Unrecognized action " + action);
        }
    }

    public static void convert(final Bot bot, final String action) {
        if (action.equals("aiml2csv")) {
            bot.writeAIMLIFFiles();
        } else if (action.equals("csv2aiml")) {
            bot.writeAIMLFiles();
        }
    }
   
    static class WebService extends NanoHTTPD {
        String botName = "super";
        Bot bot = new Bot(botName, ".");
        //Bot bot = new Bot("alice2", ".");
        Chat chatSession = new Chat(bot);

        public WebService() {
            super(8009);
            try {
                    start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
            } catch (final IOException e) {
                    e.printStackTrace();
            }
            System.out.println("Running WebService...");
        }

        @Override
        public Response serve(final IHTTPSession session) {
            final String q = session.getParms().get("q").replaceAll("[^a-zA-Z0-9 ?.!]", "");
            System.out.println("query: " + q);
            return newFixedLengthResponse(chatSession.multisentenceRespond(q));
        }
    }
    
}
