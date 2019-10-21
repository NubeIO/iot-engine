#!/usr/bin/perl 

#
# Pump some random-walk values into the given bacnet server
# See bacnet-stack-0.8.6/demo/perl/Documentation/index.html for details on the perl bindings
#
# The perl bindings in bacnet-stck would be best but they're not in the mood to compile today
# and I'm not in the mood to debug them.
#
#    bacwp 200001 1 3 85 16 -1 4 3.1415
#    WriteProperty Acknowledged!
#
#    bacrp 200001 2 0 85
#    39.914135
#
#    bacrp 200001 2 1 85
#    59.480858
#
use strict;
use warnings;

# default server params
my $DEVICE = int($ENV{'DEVICE_NUMBER'});
my $PRIORITY = 16;

# update this often
my $FREQ_SEC = 60;

# Object Types
my $OBJECT_ANALOG_INPUT = 0;
my $OBJECT_ANALOG_OUTPUT = 1;
my $OBJECT_ANALOG_VALUE = 2;

# Application Tags
my $BACNET_APPLICATION_TAG_NULL = 0;
my $BACNET_APPLICATION_TAG_BOOLEAN = 1;
my $BACNET_APPLICATION_TAG_UNSIGNED_INT = 2;
my $BACNET_APPLICATION_TAG_SIGNED_INT = 3;
my $BACNET_APPLICATION_TAG_REAL = 4;
my $BACNET_APPLICATION_TAG_DOUBLE = 5;
my $BACNET_APPLICATION_TAG_OCTET_STRING = 6;
my $BACNET_APPLICATION_TAG_CHARACTER_STRING = 7;
my $BACNET_APPLICATION_TAG_BIT_STRING = 8;
my $BACNET_APPLICATION_TAG_ENUMERATED = 9;
my $BACNET_APPLICATION_TAG_DATE = 10;
my $BACNET_APPLICATION_TAG_TIME = 11;
my $BACNET_APPLICATION_TAG_OBJECT_ID = 12;

# Property IDs
my $PROP_PRESENT_VALUE = 85;

# Seed some initial values into current. Inst num -> value
my @current_values = (44, 55, 66, 77);
my $nvals = scalar @current_values;
my $maxval = 1000;
my $minval = 10;

while (1) {
    for (my $idx = 0; $idx < $nvals; $idx++) {
        # Move a random amount in a random direction, maximum of 10% of current value.
        my $oldval = $current_values[$idx];
        my $sign = (rand 1 > 0.5) ? 1 : -1;
        my $amt = rand(.1 * $current_values[$idx]);
        my $newval = $oldval + $sign * $amt;
        if ($newval < $minval) {
            $newval = $minval + $amt;
        }
        elsif ($newval > $maxval) {
            $newval = $maxval - $amt;
        }
        $current_values[$idx] = $newval;

        print "$idx: $oldval -> $newval\n";

        # bacwp device-instance object-type object-instance property priority index tag value [tag value...]
        my $cmd = "bacwp $DEVICE $OBJECT_ANALOG_VALUE $idx $PROP_PRESENT_VALUE $PRIORITY -1 $BACNET_APPLICATION_TAG_REAL $newval";
        print("$cmd\n");
        `$cmd`;
    }

    sleep $FREQ_SEC;
}
