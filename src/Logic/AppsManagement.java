package Logic;

import java.util.ArrayList;

public class AppsManagement {

    public static ArrayList<NxApp> addApps() {
        ArrayList<NxApp> apps = new ArrayList<>();
        apps.add(new NxApp("Atmosphere", "https://github.com/Atmosphere-NX/Atmosphere"));
        apps.add(new NxApp("Hekate", "https://github.com/CTCaer/hekate"));
        apps.add(new NxApp("MissionControl", "https://github.com/ndeadly/MissionControl"));
        apps.add(new NxApp("sys-patch", "https://github.com/impeeza/sys-patch"));
        apps.add(new NxApp("nx-ovlloader", "https://github.com/ppkantorski/nx-ovlloader"));
        apps.add(new NxApp("Ultrahand-Overlay", "https://github.com/ppkantorski/Ultrahand-Overlay"));
        apps.add(new NxApp("ovl-sysmodules", "https://github.com/ppkantorski/ovl-sysmodules"));
        apps.add(new NxApp("Status-Monitor-Overlay", "https://github.com/ppkantorski/Status-Monitor-Overlay"));
        apps.add(new NxApp("FPSLocker", "https://github.com/ppkantorski/FPSLocker"));
        apps.add(new NxApp("sys-clk", "https://github.com/ppkantorski/sys-clk"));
        apps.add(new NxApp("ReverseNX-RT", "https://github.com/ppkantorski/ReverseNX-RT"));
        apps.add(new NxApp("sysdvr-overlay", "https://github.com/ppkantorski/sysdvr-overlay"));
        apps.add(new NxApp("SysDVR", "https://github.com/exelix11/SysDVR"));
        apps.add(new NxApp("emuiibo", "https://github.com/XorTroll/emuiibo"));
        apps.add(new NxApp("sphaira", "https://github.com/ITotalJustice/sphaira"));
        apps.add(new NxApp("JKSV", "https://github.com/J-D-K/JKSV"));
        apps.add(new NxApp("linkalho", "https://github.com/impeeza/linkalho"));
        apps.add(new NxApp("SwitchThemeInjector", "https://github.com/exelix11/SwitchThemeInjector"));
        apps.add(new NxApp("themezer-nx", "https://github.com/suchmememanyskill/themezer-nx"));
        apps.add(new NxApp("Switch_90DNS_tester", "https://github.com/meganukebmp/Switch_90DNS_tester"));
        return apps;
    }

}
