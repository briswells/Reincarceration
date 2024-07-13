package org.kif.reincarceration.core;

import java.sql.SQLException;

public interface Module {
    void onEnable() throws SQLException;
    void onDisable();
}