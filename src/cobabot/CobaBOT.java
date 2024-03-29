/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cobabot;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

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
    
    public static void mainFunction(final String[] args) {
        //String botName = "alice2";
        String botName = "super";
        //String botName = "JJ";
        //String botName = "roboto";
        MagicBooleans.jp_tokenize = false;
        MagicBooleans.trace_mode = true;
        //String action = "chat";
        String action = "webservice";
        //String action = "aiml2csv";
        //String action = "jojo";

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
        //inisialisasi objek bot dari class bot dengan konstrakor ber parameter
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
            convert(bot, "aiml2csv");
            new WebService();
        }else if (action.equals("jojo")) {
            convert(bot, "aiml2csv");
            //bot.writeAIMLIFFiles();
            //tambahkan method writeAIMLIFFiles(); pada object bot
            final boolean doWrites = false;
            TestAB.testChat(bot, doWrites, MagicBooleans.trace_mode);
        }  else if (action.equals("ab")) {
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
        //String botName = "roboto";
        String botName = "super";
        //String botName = "alice2";
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

            Response response = null;
            NanoHTTPD.Method method = session.getMethod();
            String uri = session.getUri();
            System.out.println("Uri : "+uri);
            try {
                switch (uri) {
                    case "/":
                        if(Method.GET.equals(method)) {
                            //response = newFixedLengthResponse(readFile("web/index.html"));
                            response = newFixedLengthResponse(readFile("web/index.html"));
                            System.out.println("running /");
                        }
                        break;
                    case "/Bot":
                        if(Method.GET.equals(method)) {
                            final String q = session.getParms().get("q").replaceAll("[^a-zA-Z0-9 ?.!]", "");
                            System.out.println("query: " + q);
                            response = newFixedLengthResponse(chatSession.multisentenceRespond(q));
                            System.out.println("running /Bot");
                        }else if (Method.POST.equals(method)){
                            Map<String, String> data = new HashMap<String, String>();
                            session.parseBody(data);
                            String message = session.getParms().get("message");
                            System.out.println("Pesan Manusia: "+message);
                            String responseBot = chatSession.multisentenceRespond(message);
                            System.out.println("Pesan BOT: "+responseBot);

                            // Membuat kondisi ketika response bot adalah akhir response
                            Scanner scanner = new Scanner(responseBot);
                            String line = scanner.nextLine();
                            StringBuilder specifications = new StringBuilder("");
                            if (line.contains("INI KOMPONEN") ) {
                                Scanner result = Example_Local.querySPARQL_DL(responseBot);
                                while (result.hasNextLine()) {
                                    line = result.nextLine();
                                    line = line.substring(line.lastIndexOf("#") + 1);
                                    line = line.replace("_", " ");
                                    System.out.println(line);
                                    specifications.append(line+" <br/>");
                                    System.out.println(specifications);

                                }
                                response = newFixedLengthResponse(specifications.toString());
                            }else{
                                response = newFixedLengthResponse(responseBot);
                            }
                            //System.out.println("running /Bot");
                            //SPLIT STRING//
                            System.out.println("---------------Human Response------------------");
                            String[] splitMessageHuman = message.split("\\s");
                            for (String w:splitMessageHuman) {
                                System.out.println(w);

                            }

                            /*
                            System.out.println("---------------Bot Response-------------");
                            String[] splitMessageBot = responseBot.split("\\s");
                            for (String w:splitMessageBot) {
                                System.out.println(w);
                            }*/
                        }
                        break;
                    case "/Load":
                        System.out.println("running /Load");

                        break;
                    //default:
                        //throw new IllegalStateException("Unexpected value: " + uri);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return response;
            /*

            final String q = session.getParms().get("q").replaceAll("[^a-zA-Z0-9 ?.!]", "");
            System.out.println("query: " + q);
            return newFixedLengthResponse(chatSession.multisentenceRespond(q));
            */
        }
        private static String readFile(String path) throws IOException {
            BufferedReader br = new BufferedReader(new FileReader(path));
            try {
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();

                while (line != null) {
                    sb.append(line);
                    sb.append("\n");
                    line = br.readLine();
                }
                return sb.toString();
            } finally {
                br.close();
            }
        }
    }
}
