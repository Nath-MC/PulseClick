package com.skytendo.pulseclick;

import eu.midnightdust.lib.config.MidnightConfig;

public class PulseClickConfig extends MidnightConfig {

    @Entry() public static Key key = Key.MOUSE_LEFT;

    @Comment public static Comment spacer1;

    @Entry() public static TimingMode timingMode = TimingMode.TICKS;

    @Comment public static Comment spacer2;

    @Comment public static Comment tickBetweenClicksComment;
    @Entry(min = 1, max = 5000) public static int ticksBetweenClicks = 100;
}
