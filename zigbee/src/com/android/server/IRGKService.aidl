package com.android.server;

/**
 * System private API for RGK service.
 *
 * {@hide}
 */
interface IRGKService
{
    int runSysCommand(in int timeoutSeconds, in String[] cmdLine);
    void setZigbeeUserName(String name);
	boolean getZigbeeUserName(String name);
}
