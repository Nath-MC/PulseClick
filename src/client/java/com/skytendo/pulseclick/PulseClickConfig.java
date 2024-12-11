package com.skytendo.pulseclick;

import eu.midnightdust.lib.config.MidnightConfig;

public class PulseClickConfig extends MidnightConfig {

    @Entry() public static Key key = Key.MOUSE_LEFT;

    @Comment public static Comment spacer1;

    @Entry() public static TimingMode timingMode = TimingMode.TICKS;

    @Comment public static Comment spacer2;

    @Comment public static Comment tickBetweenClicksComment;
    @Entry(min = 5, max = 10000) public static int ticksBetweenClicks = 100;

    @Comment public static Comment spacer3;

    @Comment public static Comment preventStarvationComment;
    @Entry() public static boolean preventStarvation = true;

    @Comment public static Comment spacer4;

    @Comment public static Comment disconnectOnLowHealthComment;
    @Entry() public static boolean disconnectOnLowHealth = false;
}
