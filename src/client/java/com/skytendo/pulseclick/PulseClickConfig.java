package com.skytendo.pulseclick;

import eu.midnightdust.lib.config.MidnightConfig;

public class PulseClickConfig extends MidnightConfig {

    @Entry() public static Key key = Key.MOUSE_LEFT;

    @Comment public static Comment spacer1;

    @Entry() public static TimingMode timingMode = TimingMode.TICKS;

    @Comment public static Comment spacer2;

    @Comment public static Comment tickBetweenClicksComment;
    @Entry(min = 5, max = 10000) public static int ticksBetweenClicks = 10;

    @Comment public static Comment spacer3;

    @Comment public static Comment raidFarmModeComment;
    @Entry() public static boolean raidFarmMode = false;

    @Comment public static Comment preventStarvationComment;
    @Entry() public static boolean preventStarvation = true;

    @Comment public static Comment spacer4;

    @Comment public static Comment preventToolBreakComment;
    @Entry() public static boolean preventToolBreak = true;

    @Comment public static Comment spacer5;

    @Comment public static Comment disconnectOnLowHealthComment;
    @Entry() public static boolean disconnectOnLowHealth = false;

    @Comment public static Comment spacer6;

    @Comment public static Comment unpauseGameOnUnfocusComment;
    @Entry() public static boolean unpauseGameOnUnfocus = true;
}
