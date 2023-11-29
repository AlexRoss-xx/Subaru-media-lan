/*
 *  2023.
 * Alexey Rasskazov
 */

package com.hzbhd.alexross.subarulan2;

public class SourceItem {
    public static final byte EVENT_APP_IN = (byte) 1;
    public static final byte EVENT_APP_OUT = (byte) 2;
    public static final byte EVENT_BT_CALL = (byte) 100;
    private boolean isEntity = true;
    private boolean isPowerOffMarkApp = false;
    private boolean isPowerOnNeedChangeSource2Android = false;
    private boolean isSpeciallySource = false;
    private int mAppid = -1;
    private String mClassName;
    private int mDeviceType;
    private byte mEvent;
    private byte mExitMode;
    private String mPackageName;
    private byte mProitity;
    private int mSourceProperty;
    private byte mSourceType;
    private byte mSourceid = (byte) -1;

    public Object clone() throws CloneNotSupportedException {
        SourceItem sourceItem = new SourceItem();
        sourceItem.mAppid = this.mAppid;
        sourceItem.mClassName = this.mClassName;
        sourceItem.mDeviceType = this.mDeviceType;
        sourceItem.mEvent = this.mEvent;
        sourceItem.mExitMode = this.mExitMode;
        sourceItem.mPackageName = this.mPackageName;
        sourceItem.mProitity = this.mProitity;
        sourceItem.mSourceid = this.mSourceid;
        sourceItem.mSourceProperty = this.mSourceProperty;
        sourceItem.mSourceType = this.mSourceType;
        sourceItem.isEntity = this.isEntity;
        sourceItem.isPowerOffMarkApp = this.isPowerOffMarkApp;
        sourceItem.isPowerOnNeedChangeSource2Android = this.isPowerOnNeedChangeSource2Android;
        sourceItem.isSpeciallySource = this.isSpeciallySource;
        return sourceItem;
    }

    public boolean equals(Object obj) {
        return obj != null ? this.mAppid == ((SourceItem) obj).getAppid() && this.isEntity == ((SourceItem) obj).isEntity() : false;
    }

    public int getAppid() {
        return this.mAppid;
    }

    public String getClassName() {
        return this.mClassName;
    }

    public int getDeviceType() {
        return this.mDeviceType;
    }

    public byte getEvent() {
        return this.mEvent;
    }

    public byte getExitMode() {
        return this.mExitMode;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public byte getProitity() {
        return this.mProitity;
    }

    public int getSourceProperty() {
        return this.mSourceProperty;
    }

    public byte getSourceType() {
        return this.mSourceType;
    }

    public byte getSourceid() {
        return this.mSourceid;
    }

    public boolean isEntity() {
        return this.isEntity;
    }

    public boolean isPowerOffMarkApp() {
        return this.isPowerOffMarkApp;
    }

    public boolean isPowerOnNeedChangeSource2Android() {
        return this.isPowerOnNeedChangeSource2Android;
    }

    public boolean isSpeciallySource() {
        return this.isSpeciallySource;
    }

    public void setAppid(int i) {
        this.mAppid = i;
    }

    public void setClassName(String str) {
        this.mClassName = str;
    }

    public void setDeviceType(int i) {
        this.mDeviceType = i;
    }

    public void setEntity(boolean z) {
        this.isEntity = z;
    }

    public void setEvent(byte b) {
        this.mEvent = b;
    }

    public void setExitMode(byte b) {
        this.mExitMode = b;
    }

    public void setPackageName(String str) {
        this.mPackageName = str;
    }

    public void setPowerOffMarkApp(boolean z) {
        this.isPowerOffMarkApp = z;
    }

    public void setPowerOnNeedChangeSource2Android(boolean z) {
        this.isPowerOnNeedChangeSource2Android = z;
    }

    public void setProitity(byte b) {
        this.mProitity = b;
    }

    public void setSourceProperty(int i) {
        this.mSourceProperty = i;
    }

    public void setSourceType(byte b) {
        this.mSourceType = b;
    }

    public void setSourceid(byte b) {
        this.mSourceid = b;
    }

    public void setSpeciallySource(boolean z) {
        this.isSpeciallySource = z;
    }

    public String toString() {
        return "Appid = " + this.mAppid + ",SourceId=" + this.mSourceid + ",proitity=" + this.mProitity + ",deviceType=" + this.mDeviceType + ",packagename=" + this.mPackageName + ",mSourceProperty = " + this.mSourceProperty + ",isSpeciallySource" + this.isSpeciallySource + ",isPowerOffMarkApp = " + this.isPowerOffMarkApp + ",isPowerOnNeedChangeSource2Android = " + this.isPowerOnNeedChangeSource2Android + ",event = " + this.mEvent;
    }
}
