#!/bin/bash

# Fix the CircleCI path
export PATH="$ANDROID_HOME/platform-tools:$ANDROID_HOME/tools:$PATH"
export PATH="$NDK_HOME/bin:$PATH"

DEPS="$ANDROID_HOME/installed_dependencies"

if [ ! -e $DEPS ]; then
  echo y | android update sdk -a  --no-ui --filter android-19
  echo y | android update sdk -a  --no-ui --filter tools
  echo y | android update sdk -a  --no-ui --filter platform-tools 
  echo y | android update sdk -a  --no-ui --filter build-tools-20.0.0
  echo y | android update sdk -a  --no-ui --filter extra-android-m2repository
  echo y | android update sdk -a  --no-ui --filter extra-android-support
  echo y | android update sdk -a  --no-ui --filter extra-google-m2repository
  echo y | android update sdk -a  --no-ui --filter addon-google_gdk-google-19
  
  # Install NDK
  wget https://dl.google.com/android/ndk/android-ndk32-r10-linux-x86_64.tar.bz2 > /dev/null;
  tar -jxf --directory /usr/local android-ndk32-r10-linux-x86_64.tar.bz2

  touch $DEPS
fi
