#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

if ! android list avd | grep -q Pixel_3a_API_30; then
    echo "No emulator for screenshot tests found, creating one..."
    $DIR/create_emulator.sh
fi

if adb devices -l | grep -q emulator; then
    echo "Emulator already running"
    exit 0
fi

echo "Starting emulator..."
echo "no" | $ANDROID_HOME/emulator/emulator "-avd" "Pixel_3a_API_30" "-no-audio" "-no-boot-anim" "-gpu" "swiftshader_indirect"
$DIR/disable_animation.sh
$DIR/wait_for_emulator.sh

echo "Emulator ready, configuring for Screenshot Testing!"
$DIR/screenshot_config_emulator.sh
$DIR/wait_for_emulator.sh

echo "Emulator started and ready to rock!"
