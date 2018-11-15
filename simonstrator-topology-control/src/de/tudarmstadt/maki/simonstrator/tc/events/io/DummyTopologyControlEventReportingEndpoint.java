package de.tudarmstadt.maki.simonstrator.tc.events.io;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.commons.io.IOUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class DummyTopologyControlEventReportingEndpoint
{
   public static void main(String[] args)
   {
      ServerSocket welcomeSocket = null;
      try
      {
         int port = 24032;
         welcomeSocket = new ServerSocket(port);
         while (true)
         {
            System.out.println("DummyTopologyControlEventReportingEndpoint: Waiting for connections...");
            Socket connectionSocket = welcomeSocket.accept();
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            final StringBuilder data = new StringBuilder();
            {
               String line = inFromClient.readLine();
               while (line != null)
               {
                  data.append(line);
                  line = inFromClient.readLine();
               }
            }
            final JsonParser jsonParser = new JsonParser();
            final JsonElement jsonElement = jsonParser.parse(data.toString());
            final JsonObject messageObject = jsonElement.getAsJsonObject();
            JsonArray events = messageObject.get("events").getAsJsonArray();
            for (final JsonElement eventElement : events)
            {
               final JsonObject event = eventElement.getAsJsonObject();
               System.out.println("Event: " + event);
            }
         }
      } catch (Exception e)
      {
         e.printStackTrace();
      } finally
      {
         IOUtils.closeQuietly(welcomeSocket);
      }

   }
}
