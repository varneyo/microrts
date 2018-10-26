package tournaments;

import java.util.ArrayList;
import java.util.List;

public class TournamentMapConfig {

    // Set tournament settings
    public Boolean useDefaults = false;
    public String map="maps/8x8/basesWorkers8x8.xml";
    public int timeBudget = 100;                          // Time budget allowed per action (default 100ms)
    public int maxGameLength = 2000;                      // Maximum game length (default 2000 ticks)
    public String shortMapName="";
    int mapsize=0;
    public TournamentMapConfig(String m,  int tb , int mgl, int mapheight, String shortMapName ){
        map = m;
        timeBudget = tb;                          // Time budget allowed per action (default 100ms)
        maxGameLength = mgl;                      // Maximum game length (default 2000 ticks)
        mapsize = mapheight;
        this.shortMapName = shortMapName;
        //setMapName();
    }

    public List<String> getMapAsList(){
        List<String> maps = new ArrayList<>();
        maps.add(map);
        return maps;
    }

    public void setMapName(){
        shortMapName = map.substring(map.indexOf("/",5)+1,map.indexOf(".",5));
    }

    public TournamentMapConfig(String m, Boolean useDefaults, int mapheight, String shortMapName){
        map = m;
        mapsize = mapheight;
        this.useDefaults = useDefaults;
        if (useDefaults){
            setSpecificMapDefaults();
        }
        this.shortMapName = shortMapName;
    }

    public void setGenericMapSizeDefaults(){
        System.out.println("The map size is: "+mapsize);
        switch(mapsize){
            case (8):
                timeBudget = 100;
                maxGameLength = 3000;
                break;
            case (9):
                timeBudget = 100;
                maxGameLength = 3000;
                break;
            case(10):
                timeBudget = 100;
                maxGameLength = 2000;
                break;
            case(12):
                timeBudget = 100;
                maxGameLength = 2000;
                break;
            case(16):
                timeBudget = 100;
                maxGameLength = 4000;
            case(24):
                timeBudget = 100;
                maxGameLength = 2000;
                break;
            case(32):
                timeBudget = 100;
                maxGameLength = 8000;
                break;
            case(64):
                timeBudget = 100;
                maxGameLength = 8000;
                break;
            case(128):
                timeBudget = 100;
                maxGameLength = 12000;
                break;
            default:
                timeBudget = 100;
                maxGameLength = 3000;
                break;
        }

    }

    public void setSpecificMapDefaults(){
        switch(map){
            case ("map1test.xml"):
                timeBudget = 100;
                maxGameLength = 2000;
            case("map2test.xml"):
                timeBudget = 100;
                maxGameLength = 2000;
            default:
                setGenericMapSizeDefaults();

        }


    }
}
