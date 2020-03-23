package edu.ucla.library.libservices.voyager.api.factory;

import edu.ucla.library.libservices.voyager.api.core.ApiParameter;
import edu.ucla.library.libservices.voyager.api.core.ApiRequest;
import edu.ucla.library.libservices.voyager.api.core.ApiResponse;
import edu.ucla.library.libservices.voyager.api.core.ApiServer;
import edu.ucla.library.libservices.voyager.api.core.VoyagerException;

import java.util.Properties;

public class VoyagerConnectionFactory
{
  public VoyagerConnectionFactory()
  {
    super();
  }

  public static ApiServer getConnection( Properties props, String appCode )
    throws VoyagerException
  {
    ApiRequest request;
    ApiResponse response;
    ApiServer server;

    server = 
        new ApiServer( props.getProperty( "voyager.server" ), 
                       Integer.parseInt( props.getProperty( "voyager.circsvr" ) ) );
    

    request = new ApiRequest( appCode, "INIT" );
    request.addParameter( new ApiParameter( "AP", "CIRC" ) );
    request.addParameter( new ApiParameter( "VN", props.getProperty( "voyager.version" ) ) );
    request.addParameter( new ApiParameter( "ENCRYPT", "N" ) );

    server.send( request.toString() );
    response = new ApiResponse( server.receive() );
    System.out.println( "response = " + response.toString() );
    if ( response.getReturnCode() != 0 )
    {
      throw new VoyagerException( response );
    }
    else
    {
      return server;
    }
  }
}
