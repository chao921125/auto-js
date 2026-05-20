package net.cc.stardust.core.database;

public interface TransactionCallback {
    void handleEvent(Transaction transaction);
}
