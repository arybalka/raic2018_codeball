package Lama.Renderer;

import jdk.nashorn.internal.runtime.Debug;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.concurrent.CopyOnWriteArrayList;

public class DebugSnapshotBase {
    boolean rendered = false;
    public int tick;
    public ArrayList<String> debug_log;
    public HashSet<DebugFormGL.DebugGroup> groups;


    //		public static CopyOnWriteArrayList<DebugFormGL.DebugElem> elements = new CopyOnWriteArrayList<>();
    public CopyOnWriteArrayList<DebugFormGL.DebugElem> elements = new CopyOnWriteArrayList<>();

    public DebugSnapshotBase(int tick) {
        this.tick = tick;
        this.debug_log = DebugBase.debug_log.getOrDefault(tick, null);
        this.groups = new HashSet<>();
    }
}
