package edu.ucla.library.libservices.voyager.main;

import edu.ucla.library.libservices.voyager.api.core.ApiRequest;

import edu.ucla.library.libservices.voyager.api.core.ApiResponse;
import edu.ucla.library.libservices.voyager.api.core.ApiServer;

import edu.ucla.library.libservices.voyager.api.core.VoyagerException;
import edu.ucla.library.libservices.voyager.api.factory.VoyagerConnectionFactory;

import edu.ucla.library.libservices.voyager.utility.CodeIdPair;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.Properties;

public class ExtendDueDate
{
  private static Properties props;
  private static ApiServer server;
  private static String machine = null;
  private static String version = null;
  private static String dbKey = "";
  private static int port = 0;


  public ExtendDueDate()
  {
    super();
  }

  @SuppressWarnings("oracle.jdeveloper.java.nested-assignment")
  public static void main(String[] args)
  {
    loadProperties(args[0]);
    initializeVariables();
    initializeConnection();
    try
    {
      BufferedReader reader;
      BufferedWriter writer;
      String line;

      writer = new BufferedWriter(new FileWriter(new File(args[2])));
      reader = new BufferedReader(new FileReader(new File(args[1])));
      line = null;

      while ((line = reader.readLine()) != null)
      {
        String[] tokens;
        int code;

        tokens = line.split(",");
        //try to extend due date
        code = extendDate(tokens[0]).getReturnCode();
        if (code != 0)
        {
          writer.write("error extending date for item " + tokens[2] + " and patron " + tokens[1] + " : ");
          switch (code)
          {
            case 1 : writer.write("Request failed.");
              break;
            case 2 : writer.write("Item has pending recall.");
              break;
            case 5 :  writer.write("Incorrect date specified.");
              break;
          }
          writer.newLine();
        }
      }
      reader.close();
      writer.flush();
      writer.close();
    }
    catch (FileNotFoundException fnfe)
    {
      fnfe.printStackTrace();
    }
    catch (IOException ioe)
    {
      ioe.printStackTrace();
    }
    /*
     * execute query to get charges to extend--maybe via external sql/vgr_sql_run?
     * extablish vgr connection via vcl api
     * loop through results--exact data will depend on whether we do RECHARGEITEM for renewal or CHGDUEDATE to simply
     * extend due date
     *
    */
  }

  private static void loadProperties(String propFile)
  {
    props = new Properties();
    try
    {
      props.load(new FileInputStream(new File(propFile)));
    }
    catch (IOException ioe)
    {
      ioe.printStackTrace();
      System.exit(-1);
    }
  }

  private static void initializeVariables()
  {
    machine = props.getProperty("voyager.server");
    version = props.getProperty("voyager.version");
    port = Integer.parseInt(props.getProperty("voyager.circsvr"));
    dbKey = props.getProperty("voyager.dbkey");
  }

  private static void initializeConnection()
  {
    try
    {
      server = VoyagerConnectionFactory.getConnection(props, props.getProperty("voyager.appcode"));
    }
    catch (VoyagerException ve)
    {
      System.err.println("Program failed to establish Voyager connection: " + ve.getMessage());
      ve.printResponse();
      System.exit(-2);
    }
  }

  private static CodeIdPair extendDate(String transaction)
  {
    ApiRequest request;
    ApiResponse response;
    CodeIdPair results;

    results = new CodeIdPair();
    
    if (validateDate() == 0)
    {
      request = new ApiRequest(props.getProperty("voyager.appcode"), "CHGDUEDATE");
      request.addParameter("TI", transaction);
      request.addParameter("RD", props.getProperty("circ.newdate"));
      //request.addParameter("UBID", "1@".concat(dbKey));
      server.send(request.toString());
      response = new ApiResponse(server.receive());

      results.setReturnCode(response.getReturnCode());

      return results;
    }
    else
    {
      System.out.print("something wrong in validate date");
      results.setReturnCode(5);
      return results;
    }
  }
  
  private static int validateDate()
  {
    ApiRequest request;
    ApiResponse response;

    request = new ApiRequest(props.getProperty("voyager.appcode"), "VALIDATE_CAL_DATE");
    request.addParameter("LI", props.getProperty("voyager.locale"));
    request.addParameter("RD", props.getProperty("circ.newdate"));
    server.send(request.toString());
    response = new ApiResponse(server.receive());

    return response.getReturnCode();
  }
}
