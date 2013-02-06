/**
 * Java Finder by petrucio@stackoverflow(828681) is licensed under a Creative Commons Attribution 3.0 Unported License.
 * Needs WinRegistry.java. Get it at: http://stackoverflow.com/questions/62289/read-write-to-windows-registry-using-java
 *
 * JavaFinder - Windows-specific classes to search for all installed versions of java on this system
 * Author: petrucio@stackoverflow (828681)
 *****************************************************************************/

import java.util.*;
import java.io.*;

/**
 * Helper class to fetch the stdout and stderr outputs from started Runtime execs
 * Modified from http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html?page=4
 *****************************************************************************/
class RuntimeStreamer extends Thread {
    InputStream is;
    String lines;

    RuntimeStreamer(InputStream is) {
        this.is = is;
        this.lines = "";
    }
    public String contents() {
        return this.lines;
    }

    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader    br  = new BufferedReader(isr);
            String line = null;
            while ( (line = br.readLine()) != null) {
                this.lines += line + "\n";
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();  
        }
    }

    /**
     * Execute a command and wait for it to finish
     * @return The resulting stdout and stderr outputs concatenated
     ****************************************************************************/
    public static String execute(String[] cmdArray) {
        try {
            Runtime runtime = Runtime.getRuntime();
            Process proc = runtime.exec(cmdArray);
            RuntimeStreamer outputStreamer = new RuntimeStreamer(proc.getInputStream());
            RuntimeStreamer errorStreamer  = new RuntimeStreamer(proc.getErrorStream());
            outputStreamer.start();
            errorStreamer.start();
            proc.waitFor();
            return outputStreamer.contents() + errorStreamer.contents();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }
    public static String execute(String cmd) {
        String[] cmdArray = { cmd };
        return RuntimeStreamer.execute(cmdArray);
    }
}

/**
 * Helper struct to hold information about one installed java version
 ****************************************************************************/
class JavaInfo {
    public String  path;        //! Full path to java.exe executable file
    public String  version;     //! Version string. "Unkown" if the java process returned non-standard version string
    public boolean is64bits;    //! true for 64-bit javas, false for 32

    /**
     * Calls 'javaPath -version' and parses the results
     * @param javaPath: path to a java.exe executable
     ****************************************************************************/
    public JavaInfo(String javaPath) {
        String versionInfo = RuntimeStreamer.execute( new String[] { javaPath, "-version" } );
        String[] tokens = versionInfo.split("\"");

        if (tokens.length < 2) this.version = "Unkown";
        else this.version = tokens[1];
        this.is64bits = versionInfo.toUpperCase().contains("64-BIT");
        this.path     = javaPath;
    }

    /**
     * @return Human-readable contents of this JavaInfo instance
     ****************************************************************************/
    public String toString() {
        return this.path + ":\n  Version: " + this.version + "\n  Bitness: " + (this.is64bits ? "64-bits" : "32-bits");
    }
}

/**
 * Windows-specific java versions finder
 *****************************************************************************/
public class JavaFinder {

    /**
     * @return: A list of javaExec paths found under this registry key (rooted at HKEY_LOCAL_MACHINE)
     * @param wow64  0 for standard registry access (32-bits for 32-bit app, 64-bits for 64-bits app)
     *               or WinRegistry.KEY_WOW64_32KEY to force access to 32-bit registry view,
     *               or WinRegistry.KEY_WOW64_64KEY to force access to 64-bit registry view
     * @param previous: Insert all entries from this list at the beggining of the results
     *************************************************************************/
    private static List<String> searchRegistry(String key, int wow64, List<String> previous) {
        List<String> result = previous;
        try {
            List<String> entries = WinRegistry.readStringSubKeys(WinRegistry.HKEY_LOCAL_MACHINE, key, wow64);
            for (int i = 0; entries != null && i < entries.size(); i++) {
                String val = WinRegistry.readString(WinRegistry.HKEY_LOCAL_MACHINE, key + "\\" + entries.get(i), "JavaHome", wow64);
                if (!result.contains(val + "\\bin\\java.exe")) {
                    result.add(val + "\\bin\\java.exe");
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return result;
    }

    /**
     * @return: A list of JavaInfo with informations about all javas installed on this machine
     * Searches and returns results in this order:
     *   HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft\Java Runtime Environment (32-bits view)
     *   HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft\Java Runtime Environment (64-bits view)
     *   HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft\Java Development Kit     (32-bits view)
     *   HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft\Java Development Kit     (64-bits view)
     *   WINDIR\system32
     *   WINDIR\SysWOW64
     ****************************************************************************/
    public static List<JavaInfo> findJavas() {
        List<String> javaExecs = new ArrayList<String>();

        javaExecs = JavaFinder.searchRegistry("SOFTWARE\\JavaSoft\\Java Runtime Environment", WinRegistry.KEY_WOW64_32KEY, javaExecs);
        javaExecs = JavaFinder.searchRegistry("SOFTWARE\\JavaSoft\\Java Runtime Environment", WinRegistry.KEY_WOW64_64KEY, javaExecs);
        javaExecs = JavaFinder.searchRegistry("SOFTWARE\\JavaSoft\\Java Development Kit",     WinRegistry.KEY_WOW64_32KEY, javaExecs);
        javaExecs = JavaFinder.searchRegistry("SOFTWARE\\JavaSoft\\Java Development Kit",     WinRegistry.KEY_WOW64_64KEY, javaExecs);

        javaExecs.add(System.getenv("WINDIR") + "\\system32\\java.exe");
        javaExecs.add(System.getenv("WINDIR") + "\\SysWOW64\\java.exe");

        List<JavaInfo> result = new ArrayList<JavaInfo>();
        for (String javaPath: javaExecs) {
            if (!(new File(javaPath).exists())) continue;
            result.add(new JavaInfo(javaPath));
        }
        return result;
    }

    /**
     * @return: The path to a java.exe that has the same bitness as the OS
     * (or null if no matching java is found)
     ****************************************************************************/
    public static String getOSBitnessJava() {
        String arch      = System.getenv("PROCESSOR_ARCHITECTURE");
        String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");
        boolean isOS64 = arch.endsWith("64") || (wow64Arch != null && wow64Arch.endsWith("64"));

        List<JavaInfo> javas = JavaFinder.findJavas();
        for (int i = 0; i < javas.size(); i++) {
            if (javas.get(i).is64bits == isOS64) return javas.get(i).path;
        }
        return null;
    }

    /**
     * Standalone testing - lists all Javas in the system
     ****************************************************************************/
    public static void main(String [] args) {
        List<JavaInfo> javas = JavaFinder.findJavas();
        for (int i = 0; i < javas.size(); i++) {
            System.out.println("\n" + javas.get(i));
        }
    }
}
