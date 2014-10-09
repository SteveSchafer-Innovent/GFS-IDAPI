/*

Actuate Client Example

Command line argument processing class

*/
package example;

import java.util.Vector;
import java.util.Enumeration;
import java.util.NoSuchElementException;

public class Arguments
{

    // default server settings
    String serverURL = null;
    String username = null;
    String password = null;
    String targetVolume = null;
    String targetServer = null;
    boolean embeddedDownload = false;

    Vector<String> arguments = new Vector<String>();

    Enumeration enumeration;

    Arguments(String args[])
    {
        try
        {
            // check each argument if it's '-h http://somethinge' then take them as option
            for (int i = 0; i < args.length; i++)
            {
                if (args[i].startsWith("-"))
                {
                    char c = args[i].charAt(1);
                    switch (c)
                    {
                        case 'h' :
                        case 'H' :
                            serverURL = args[++i];
                            break;
                        case 'u' :
                        case 'U' :
                            username = args[++i];
                            break;
                        case 'p' :
                        case 'P' :
                            password = args[++i];
                            break;
                        case 'e' :
                        case 'E' :
                            embeddedDownload = true;
                            break;
                        case 'v' :
                        case 'V' :
                            targetVolume = args[++i];;
                            break;
                        case 's' :
                        case 'S' :
                            targetServer = args[++i];;
                            break;
                        case '?' :
                            printUsage();
                            System.exit(0);
                            break;
                    }
                }
                else
                {
                    arguments.add(args[i]);
                }
            }
            enumeration = arguments.elements();
        }
        catch (Exception e)
        {
            printUsage();
            System.exit(0);
        }

    }

    String getArgument()
    {

        String result = null;
        try
        {
            result = (String) enumeration.nextElement();
        }
        catch (NoSuchElementException e)
        {
            printUsage();
            System.exit(0);
        }

        return result;
    }

    String getOptionalArgument(String defaultValue)
    {

        String result = null;
        try
        {
            result = (String) enumeration.nextElement();
        }
        catch (NoSuchElementException e)
        {
        }
        return (result != null) ? result : defaultValue;
    }

    static String usage = "java ClassName [options] ...\n";
    static String commonUsage =
            "Common options are: \n"+
            "     -h hostname    SOAP endpoint, default 'http://localhost:8000'\n"+
            "     -u username    specify username, default 'Administrator'\n"+
            "     -p password    specify password, default ''\n"+
            "     -v volume      specify target volume\n"+
            "     -? print this usage\n"+
            "\n";

    static void printUsage()
    {
        System.out.println(usage);

        System.out.println(commonUsage);
    }

    String getURL()
    {
        return serverURL;
    }

    String getUsername()
    {
        return username;
    };

    String getPassword()
    {
        return password;
    };

    String getTargetVolume()
    {
        return targetVolume;
    };

    String getTargetServer()
    {
        return targetServer;
    };

    /**
     * Returns the embeddedDownload.
     * @return boolean
     */
    public boolean isEmbeddedDownload()
    {
        return embeddedDownload;
    }

}
