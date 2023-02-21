package bgu.spl.net.impl.rci;

import java.io.Serializable;
import bgu.spl.net.srv.Connections;

public interface Command<T> extends Serializable {
    Serializable execute(T arg);
}
